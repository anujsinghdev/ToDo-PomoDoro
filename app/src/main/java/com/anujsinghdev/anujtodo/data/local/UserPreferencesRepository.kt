package com.anujsinghdev.anujtodo.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anujsinghdev.anujtodo.ui.list_detail.SortOption
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_NAME = stringPreferencesKey("user_name")
    private val KEY_EMAIL = stringPreferencesKey("user_email")
    private val KEY_PASSWORD = stringPreferencesKey("user_password")

    // --- TIMER KEYS ---
    private val KEY_TIMER_END_TIME = longPreferencesKey("timer_end_time")
    private val KEY_TIMER_IS_RUNNING = booleanPreferencesKey("timer_is_running")
    private val KEY_TIMER_REMAINING_PAUSED = longPreferencesKey("timer_remaining_paused")

    // --- CUSTOM DURATIONS KEY ---
    private val KEY_CUSTOM_DURATIONS = stringSetPreferencesKey("custom_durations")

    // --- SORT OPTION KEY ---
    private val KEY_SORT_OPTION = stringPreferencesKey("sort_option")

    // User Info
    suspend fun saveUser(name: String, email: String, password: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NAME] = name
            preferences[KEY_EMAIL] = email
            preferences[KEY_PASSWORD] = password
        }
    }

    val userName: Flow<String?> = context.dataStore.data.map { it[KEY_NAME] }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[KEY_EMAIL] }
    val userPassword: Flow<String?> = context.dataStore.data.map { it[KEY_PASSWORD] }

    // --- TIMER FUNCTIONS ---

    // Save the timestamp when the timer should end
    suspend fun saveTimerState(endTime: Long, isRunning: Boolean) {
        context.dataStore.edit { pref ->
            pref[KEY_TIMER_END_TIME] = endTime
            pref[KEY_TIMER_IS_RUNNING] = isRunning
        }
    }

    // Save the remaining time when the user pauses
    suspend fun savePausedState(remainingTime: Long) {
        context.dataStore.edit { pref ->
            pref[KEY_TIMER_REMAINING_PAUSED] = remainingTime
            pref[KEY_TIMER_IS_RUNNING] = false
        }
    }

    // Reset everything
    suspend fun clearTimerState() {
        context.dataStore.edit { pref ->
            pref[KEY_TIMER_END_TIME] = 0L
            pref[KEY_TIMER_IS_RUNNING] = false
            pref[KEY_TIMER_REMAINING_PAUSED] = 0L
        }
    }

    val timerEndTime: Flow<Long?> = context.dataStore.data.map { it[KEY_TIMER_END_TIME] }
    val isTimerRunning: Flow<Boolean?> = context.dataStore.data.map { it[KEY_TIMER_IS_RUNNING] }
    val timerRemainingPaused: Flow<Long?> = context.dataStore.data.map { it[KEY_TIMER_REMAINING_PAUSED] }

    // --- CUSTOM DURATIONS FUNCTIONS ---

    // Get custom durations as a Flow
    val customDurations: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[KEY_CUSTOM_DURATIONS] ?: emptySet()
    }

    // Add a custom duration
    suspend fun saveCustomDuration(minutes: Int) {
        context.dataStore.edit { preferences ->
            val currentSet = preferences[KEY_CUSTOM_DURATIONS] ?: emptySet()
            preferences[KEY_CUSTOM_DURATIONS] = currentSet + minutes.toString()
        }
    }

    // Remove a custom duration
    suspend fun removeCustomDuration(minutes: Int) {
        context.dataStore.edit { preferences ->
            val currentSet = preferences[KEY_CUSTOM_DURATIONS] ?: emptySet()
            preferences[KEY_CUSTOM_DURATIONS] = currentSet - minutes.toString()
        }
    }

    // --- SORT OPTION FUNCTIONS ---
    val sortOption: Flow<String> = context.dataStore.data.map { preferences ->
        // Default to CREATION_DATE if not set
        preferences[KEY_SORT_OPTION] ?: SortOption.CREATION_DATE.name
    }

    suspend fun saveSortOption(option: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SORT_OPTION] = option
        }
    }
}