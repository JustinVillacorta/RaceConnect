package com.example.raceconnect.viewmodel

import android.util.Log
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

    // Simple validation function
    fun isLoginInputValid(username: String, password: String): Boolean {
        return username.isNotEmpty() && password.isNotEmpty()
    }

    // Login function to call API
    fun validateLogin(username: String, password: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val loginRequest = LoginRequest(username, password)
                val response: LoginResponse = RetrofitInstance.api.login(loginRequest)

                if (response.success && response.data != null) {
                    loggedInUser.value = response.data
                    Log.d("AuthenticationViewModel", "Login successful: ${loggedInUser.value}")
                } else {
                    errorMessage.value = response.message ?: "Login failed"
                    Log.d("AuthenticationViewModel", "Login failed: ${response.message}")
                }
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "An unexpected error occurred"
                Log.e("AuthenticationViewModel", "Error during login", e)
            } finally {
                isLoading.value = false
            }
        }
    }


    // Temporary validateSignup function for testing
    fun validateSignup(username: String, password: String): Boolean {
        return username.isNotEmpty() && password.length >= 6
    }
}
