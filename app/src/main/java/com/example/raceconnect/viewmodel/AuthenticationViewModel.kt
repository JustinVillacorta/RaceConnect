package com.example.raceconnect.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.LoginRequest
import com.example.raceconnect.model.LoginResponse
import com.example.raceconnect.model.users
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthenticationViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)
    val loggedInUser = MutableStateFlow<users?>(null)

    init {
        loadUser()
    }

    // Validate Login and Save User Data
    fun validateLogin(username: String, password: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val loginRequest = LoginRequest(username, password)
                val response: LoginResponse = RetrofitInstance.api.login(loginRequest)

                if (response.token != null && response.user != null) {
                    loggedInUser.value = response.user
                    Log.d("AuthenticationViewModel", "Login successful: ${loggedInUser.value}")

                    // Save user data to DataStore
                    userPreferences.saveUser(
                        response.user.id,
                        response.user.username,
                        response.user.email,
                        response.token
                    )
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

    // Load User Data from DataStore
    private fun loadUser() {
        viewModelScope.launch {
            loggedInUser.value = userPreferences.user.first()
            Log.d("AuthenticationViewModel", "Loaded user: ${loggedInUser.value}")
        }
    }

    // Logout and Clear Data
    fun logout() {
        viewModelScope.launch {
            userPreferences.clearUser()
            loggedInUser.value = null
            Log.d("AuthenticationViewModel", "User logged out")
        }
    }
}
