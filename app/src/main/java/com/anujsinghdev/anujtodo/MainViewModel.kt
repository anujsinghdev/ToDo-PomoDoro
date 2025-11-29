package com.anujsinghdev.anujtodo

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anujsinghdev.anujtodo.data.local.UserPreferencesRepository
import com.anujsinghdev.anujtodo.ui.util.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    // Helper state to show a blank screen while we check the database
    var isLoading = mutableStateOf(true)
        private set

    // Default to Login, but will change to TodoList if user exists
    var startDestination = mutableStateOf(Screen.LoginScreen.route)
        private set

    init {
        viewModelScope.launch {
            // Check the database for the user's name
            val name = repository.userName.first()

            if (!name.isNullOrBlank()) {
                // User exists, skip login
                startDestination.value = Screen.TodoListScreen.route
            } else {
                // No user, show login
                startDestination.value = Screen.LoginScreen.route
            }
            isLoading.value = false
        }
    }
}