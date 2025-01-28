package com.example.raceconnect.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.model.LoginRequest
import com.example.raceconnect.model.LoginResponse
import com.example.raceconnect.model.User
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.launch

class AuthenticationViewModel : ViewModel() {
    // State variables
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val loggedInUser = mutableStateOf<User?>(null)

    // Login function
    fun login(username: String, password: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val loginRequest = LoginRequest(username, password)
                val response: LoginResponse = RetrofitInstance.api.login(loginRequest)
                if (response.success && response.data != null) {
                    loggedInUser.value = response.data
                } else {
                    errorMessage.value = response.message ?: "Login failed"
                }
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                isLoading.value = false
            }
        }
    }
}
