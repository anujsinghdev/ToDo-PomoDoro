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

    var loginSuccess = mutableStateOf(false)

    fun onLoginClick() {
        viewModelScope.launch {
            if (name.value.isNotBlank()) {
                // Only saving Name now.
                // Ensure your repository.saveUser function is updated to handle just the name,
                // or pass default/empty strings for email/password if the signature hasn't changed yet.
                repository.saveUser(name.value, "", "")
                loginSuccess.value = true
            }
        }
    }
}
