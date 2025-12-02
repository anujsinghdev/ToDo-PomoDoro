package com.anujsinghdev.anujtodo.ui.stats

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anujsinghdev.anujtodo.data.local.UserPreferencesRepository
import com.anujsinghdev.anujtodo.domain.model.BackupData
import com.anujsinghdev.anujtodo.domain.model.FocusSession
import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.domain.repository.TodoRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class UserStats(
    val level: Int,
    val title: String,
    val progress: Float,
    val nextLevelHours: Int,

    // New Metrics
    val todayFocusMinutes: Int,
    val totalFocusMinutes: Int,
    val todayTasksCompleted: Int,
    val totalTasksCompleted: Int
)

data class ChartDataPoint(
    val label: String,
    val value: Int,
    val isToday: Boolean = false
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: TodoRepository,
    private val userPrefs: UserPreferencesRepository, // Inject UserPrefs
    @ApplicationContext private val context: Context  // Inject Context for file ops
) : ViewModel() {

    private val _showCelebration = MutableStateFlow(false)
    val showCelebration = _showCelebration.asStateFlow()

    private var previousLevel: Int? = null

    // User Stats
    val userStats = combine(
        repository.getFocusSessions(0, Long.MAX_VALUE),
        repository.getAllTodos() // Using all todos to filter for 'today'
    ) { sessions, todos ->
        val stats = calculateStats(sessions, todos)

        // Check for level up
        if (previousLevel != null && stats.level > previousLevel!!) {
            _showCelebration.value = true
        }
        previousLevel = stats.level

        stats
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        UserStats(1, "Novice", 0f, 10, 0, 0, 0, 0)
    )

    // Weekly Data (kept for the chart)
    val weeklyData = repository.getFocusSessions(0, Long.MAX_VALUE).map { sessions ->
        processWeeklyData(sessions)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Monthly Data
    val monthlyData = repository.getFocusSessions(0, Long.MAX_VALUE).map { sessions ->
        processMonthlyData(sessions)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Yearly Data
    val yearlyData = repository.getFocusSessions(0, Long.MAX_VALUE).map { sessions ->
        processYearlyData(sessions)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Lifetime Data
    val lifetimeData = repository.getFocusSessions(0, Long.MAX_VALUE).map { sessions ->
        processLifetimeData(sessions)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onCelebrationShown() {
        _showCelebration.value = false
    }

    // --- IMPORT / EXPORT LOGIC ---

    fun exportData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Gather all data
                // Note: repository calls must return lists directly via .first()
                val backup = BackupData(
                    userName = userPrefs.userName.first(),
                    userEmail = userPrefs.userEmail.first(),
                    folders = repository.getAllFolders().first(),
                    lists = repository.getAllLists().first() + repository.getArchivedLists().first(),
                    todos = repository.getAllTodos().first(),
                    focusSessions = repository.getFocusSessions(0, Long.MAX_VALUE).first()
                )

                // 2. Convert to JSON
                val gson = Gson()
                val jsonString = gson.toJson(backup)

                // 3. Save to Downloads using MediaStore
                val fileName = "AnujTodo_Backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        OutputStreamWriter(outputStream).use { writer ->
                            writer.write(jsonString)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Backup saved to Downloads!", Toast.LENGTH_LONG).show()
                    }
                } ?: run {
                    throw Exception("Could not create file in Downloads")
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Export Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Read File
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.reader().readText()
                } ?: throw Exception("Could not read file")

                // 2. Parse JSON
                val gson = Gson()
                val backup = gson.fromJson(jsonString, BackupData::class.java)

                // 3. Restore Data (Clear DB first to prevent duplicates/conflicts)
                // Ensure you have added clearAllData() to your Repository as per Step 4
                repository.clearAllData()

                // 4. Insert User Prefs
                if (backup.userName != null) {
                    // Reusing the existing saveUser method
                    userPrefs.saveUser(backup.userName, backup.userEmail ?: "", "")
                }

                // 5. Insert DB Data
                backup.folders.forEach { repository.insertFolder(it) }
                // Insert lists. Note: If your DAO insert method auto-generates IDs and ignores the input ID,
                // you might lose specific ID linkages (folders <-> lists).
                // Ideally, use @Insert(onConflict = OnConflictStrategy.REPLACE) in DAO which respects provided IDs.
                backup.lists.forEach { repository.insertList(it) }
                backup.todos.forEach { repository.insertTodo(it) }
                backup.focusSessions.forEach { repository.saveFocusSession(it) }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Data imported successfully!", Toast.LENGTH_LONG).show()
                    // Optional: Navigate to Home or recreate activity to refresh UI thoroughly
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Import Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }

    // --- Calculation Logic ---

    private fun calculateStats(sessions: List<FocusSession>, todos: List<TodoItem>): UserStats {
        // 1. Focus Calculations
        val totalMinutes = sessions.sumOf { it.durationMinutes }
        val todayStart = getStartOfDay(Calendar.getInstance())
        val todayEnd = getEndOfDay(Calendar.getInstance())

        val todayMinutes = sessions
            .filter { it.timestamp in todayStart..todayEnd }
            .sumOf { it.durationMinutes }

        // 2. Task Calculations
        val completedTasks = todos.filter { it.isCompleted }
        val totalTasksCount = completedTasks.size

        val todayTasksCount = completedTasks.count {
            // Check completedAt if available, otherwise fallback to updatedAt logic if we had it.
            // Since we just added completedAt, older tasks might be null.
            // We count them if completedAt is today.
            it.completedAt != null && it.completedAt >= todayStart && it.completedAt <= todayEnd
        }

        // 3. Level Calculations
        val minutesPerLevel = 600
        val currentLevel = (totalMinutes / minutesPerLevel) + 1
        val minutesInCurrentLevel = totalMinutes % minutesPerLevel
        val progress = minutesInCurrentLevel / minutesPerLevel.toFloat()
        val hoursForNextLevel = 10 - (minutesInCurrentLevel / 60)

        val title = when (currentLevel) {
            in 1..10 -> "Novice"; in 11..50 -> "Apprentice"; in 51..100 -> "Adept"
            in 101..300 -> "Expert"; in 301..600 -> "Master"; in 601..999 -> "Grandmaster"; else -> "Legend"
        }

        return UserStats(
            level = currentLevel,
            title = title,
            progress = progress,
            nextLevelHours = hoursForNextLevel,
            todayFocusMinutes = todayMinutes,
            totalFocusMinutes = totalMinutes,
            todayTasksCompleted = todayTasksCount,
            totalTasksCompleted = totalTasksCount
        )
    }

    // --- Chart Helpers (Unchanged) ---

    private fun processLifetimeData(sessions: List<FocusSession>): List<ChartDataPoint> {
        if (sessions.isEmpty()) return emptyList()
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val minTimestamp = sessions.minOf { it.timestamp }
        calendar.timeInMillis = minTimestamp
        val startYear = calendar.get(Calendar.YEAR)
        val years = mutableListOf<ChartDataPoint>()

        for (year in startYear..currentYear) {
            val start = getStartOfYear(year)
            val end = getEndOfYear(year)
            val yearMinutes = sessions.filter { it.timestamp in start..end }.sumOf { it.durationMinutes }
            years.add(ChartDataPoint(year.toString(), yearMinutes, year == currentYear))
        }
        if (years.isEmpty()) years.add(ChartDataPoint(currentYear.toString(), 0, true))
        return years
    }

    private fun processWeeklyData(sessions: List<FocusSession>): List<ChartDataPoint> {
        val calendar = Calendar.getInstance()
        val days = mutableListOf<ChartDataPoint>()
        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis(); calendar.add(Calendar.DAY_OF_YEAR, -i)
            val s = getStartOfDay(calendar); val e = getEndOfDay(calendar)
            val min = sessions.filter { it.timestamp in s..e }.sumOf { it.durationMinutes }
            val label = if (i == 0) "Today" else getDayName(calendar.get(Calendar.DAY_OF_WEEK))
            days.add(ChartDataPoint(label, min, i == 0))
        }
        return days
    }

    private fun processMonthlyData(sessions: List<FocusSession>): List<ChartDataPoint> {
        val calendar = Calendar.getInstance()
        val days = mutableListOf<ChartDataPoint>()
        for (i in 5 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis(); calendar.add(Calendar.DAY_OF_YEAR, -(i * 5))
            val s = getStartOfDay(calendar); calendar.add(Calendar.DAY_OF_YEAR, 4); val e = getEndOfDay(calendar)
            val min = sessions.filter { it.timestamp in s..e }.sumOf { it.durationMinutes }
            days.add(ChartDataPoint(if (i == 0) "Now" else "${i * 5}d", min, i == 0))
        }
        return days
    }

    private fun processYearlyData(sessions: List<FocusSession>): List<ChartDataPoint> {
        val calendar = Calendar.getInstance()
        val months = mutableListOf<ChartDataPoint>()
        for (i in 11 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis(); calendar.add(Calendar.MONTH, -i); calendar.set(Calendar.DAY_OF_MONTH, 1)
            val s = getStartOfDay(calendar); calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)); val e = getEndOfDay(calendar)
            val min = sessions.filter { it.timestamp in s..e }.sumOf { it.durationMinutes }
            months.add(ChartDataPoint(getMonthName(calendar.get(Calendar.MONTH)), min, i == 0))
        }
        return months
    }

    private fun getStartOfYear(year: Int): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, year); c.set(Calendar.DAY_OF_YEAR, 1)
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
    private fun getEndOfYear(year: Int): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, year); c.set(Calendar.MONTH, Calendar.DECEMBER); c.set(Calendar.DAY_OF_MONTH, 31)
        c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59); c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }
    private fun getDayName(day: Int) = when(day) { Calendar.MONDAY->"M"; Calendar.TUESDAY->"T"; Calendar.WEDNESDAY->"W"; Calendar.THURSDAY->"T"; Calendar.FRIDAY->"F"; Calendar.SATURDAY->"S"; Calendar.SUNDAY->"S"; else->"" }
    private fun getMonthName(month: Int) = when(month) { Calendar.JANUARY->"Jan"; Calendar.FEBRUARY->"Feb"; Calendar.MARCH->"Mar"; Calendar.APRIL->"Apr"; Calendar.MAY->"May"; Calendar.JUNE->"Jun"; Calendar.JULY->"Jul"; Calendar.AUGUST->"Aug"; Calendar.SEPTEMBER->"Sep"; Calendar.OCTOBER->"Oct"; Calendar.NOVEMBER->"Nov"; Calendar.DECEMBER->"Dec"; else->"" }
    private fun getStartOfDay(cal: Calendar): Long { val c = cal.clone() as Calendar; c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0); return c.timeInMillis }
    private fun getEndOfDay(cal: Calendar): Long { val c = cal.clone() as Calendar; c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59); c.set(Calendar.MILLISECOND, 999); return c.timeInMillis }
}