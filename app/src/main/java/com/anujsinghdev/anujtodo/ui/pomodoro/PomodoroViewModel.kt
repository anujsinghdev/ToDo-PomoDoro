package com.anujsinghdev.anujtodo.ui.pomodoro

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anujsinghdev.anujtodo.data.local.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    var initialTimeInMillis = mutableStateOf(25 * 60 * 1000L)
    var timeLeftInMillis = mutableStateOf(25 * 60 * 1000L)
    var isTimerRunning = mutableStateOf(false)

    // Custom durations as StateFlow
    val customDurations: StateFlow<List<Int>> = repository.customDurations
        .map { set ->
            set.mapNotNull { it.toIntOrNull() }.sorted()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val endTime = repository.timerEndTime.first() ?: 0L
            val isRunning = repository.isTimerRunning.first() ?: false
            val savedRemaining = repository.timerRemainingPaused.first() ?: 0L

            if (isRunning && endTime > System.currentTimeMillis()) {
                isTimerRunning.value = true
                startTicker(endTime)
            } else if (isRunning && endTime <= System.currentTimeMillis()) {
                resetTimer()
            } else if (!isRunning && savedRemaining > 0L) {
                timeLeftInMillis.value = savedRemaining
                isTimerRunning.value = false
            }
        }
    }

    fun startTimer() {
        val endTime = System.currentTimeMillis() + timeLeftInMillis.value
        isTimerRunning.value = true

        viewModelScope.launch {
            repository.saveTimerState(endTime, true)
        }

        startTicker(endTime)
    }

    private fun startTicker(endTime: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val currentTime = System.currentTimeMillis()
                val remaining = endTime - currentTime

                if (remaining > 0) {
                    timeLeftInMillis.value = remaining
                } else {
                    timeLeftInMillis.value = 0
                    isTimerRunning.value = false
                    repository.clearTimerState()
                    break
                }
                delay(100)
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        isTimerRunning.value = false

        viewModelScope.launch {
            repository.savePausedState(timeLeftInMillis.value)
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        isTimerRunning.value = false
        timeLeftInMillis.value = initialTimeInMillis.value

        viewModelScope.launch {
            repository.clearTimerState()
        }
    }

    fun updateDuration(minutes: Int) {
        resetTimer()
        val newTime = minutes * 60 * 1000L
        initialTimeInMillis.value = newTime
        timeLeftInMillis.value = newTime
    }

    fun addCustomDuration(minutes: Int) {
        viewModelScope.launch {
            repository.saveCustomDuration(minutes)
        }
    }

    fun removeCustomDuration(minutes: Int) {
        viewModelScope.launch {
            repository.removeCustomDuration(minutes)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
