package com.anujsinghdev.anujtodo.ui.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anujsinghdev.anujtodo.data.local.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    var name = mutableStateOf("")
    var email = mutableStateOf("")
    var password = mutableStateOf("")

    var loginSuccess = mutableStateOf(false)

    fun onLoginClick() {
        viewModelScope.launch {
            if (name.value.isNotBlank() && email.value.isNotBlank() && password.value.isNotBlank()) {
                // Now saving Email too
                repository.saveUser(name.value, email.value, password.value)
                loginSuccess.value = true
            }
        }
    }
}