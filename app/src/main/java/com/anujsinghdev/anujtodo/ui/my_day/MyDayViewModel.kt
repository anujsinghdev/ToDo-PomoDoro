package com.anujsinghdev.anujtodo.ui.my_day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anujsinghdev.anujtodo.domain.model.RepeatMode
import com.anujsinghdev.anujtodo.domain.model.TodoItem
import com.anujsinghdev.anujtodo.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MyDayViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {

    // Helper to get start of today (Midnight)
    private val startOfToday: Long
        get() {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }

    // Helper to get end of today
    private val endOfToday: Long
        get() {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            return calendar.timeInMillis
        }

    val state = combine(repository.getAllTodos()) { todos ->
        val activeTodos = todos[0].filter { !it.isCompleted }

        val overdue = activeTodos.filter {
            it.dueDate != null && it.dueDate!! < startOfToday
        }

        val today = activeTodos.filter {
            (it.dueDate != null && it.dueDate!! >= startOfToday && it.dueDate!! <= endOfToday)
        }

        MyDayState(overdueTasks = overdue, todayTasks = today)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MyDayState())

    fun toggleTask(todo: TodoItem) {
        viewModelScope.launch {
            repository.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    // --- NEW FUNCTION ---
    fun addTask(title: String, dueDate: Long?, repeatMode: RepeatMode) {
        viewModelScope.launch {
            // Default to TODAY if no date is picked, ensuring it shows up in "My Day"
            val effectiveDate = dueDate ?: System.currentTimeMillis()

            repository.insertTodo(
                TodoItem(
                    title = title,
                    dueDate = effectiveDate,
                    repeatMode = repeatMode,
                    listId = null // Active in root/default
                )
            )
        }
    }
}

data class MyDayState(
    val overdueTasks: List<TodoItem> = emptyList(),
    val todayTasks: List<TodoItem> = emptyList()
)