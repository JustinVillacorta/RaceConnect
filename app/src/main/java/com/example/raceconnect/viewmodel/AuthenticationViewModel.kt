package com.example.raceconnect.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.LoginRequest
import com.example.raceconnect.model.LoginResponse
import com.example.raceconnect.model.LogoutRequest
import com.example.raceconnect.model.SignupRequest
import com.example.raceconnect.model.users
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    // sign up

    fun signUp(username: String, email: String, password: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val signupRequest = SignupRequest(username, email, password)
                val response = RetrofitInstance.api.signup(signupRequest)

                if (response.token != null && response.user != null) {
                    loggedInUser.value = response.user
                    Log.d("AuthenticationViewModel", "Signup successful: ${loggedInUser.value}")

                    // Save user data to DataStore
                    userPreferences.saveUser(
                        response.user.id,
                        response.user.username,
                        response.user.email,
                        response.token
                    )
                } else {
                    errorMessage.value = response.message ?: "Signup failed"
                    Log.d("AuthenticationViewModel", "Signup failed: ${response.message}")
                }

            } catch (e: Exception) {
                errorMessage.value = e.message ?: "An unexpected error occurred"
                Log.e("AuthenticationViewModel", "Error during signup", e)
            } finally {
                isLoading.value = false
            }
        }
    }






    // Logout and Clear Data
    fun logout() {
        viewModelScope.launch {
            try {
                val token = withContext(Dispatchers.IO) { userPreferences.getToken() }

                if (!token.isNullOrEmpty()) {
                    val authHeader = "Bearer $token"
                    Log.d("AuthenticationViewModel", "Sending logout request with Authorization: $authHeader")

                    val response = RetrofitInstance.api.logout(authHeader) // Send token in header

                    if (response.isSuccessful) {
                        userPreferences.clearUser()
                        loggedInUser.value = null
                        Log.d("AuthenticationViewModel", "User logged out successfully")
                    } else {
                        val errorResponse = response.errorBody()?.string()
                        Log.e("AuthenticationViewModel", "Logout failed: $errorResponse")
                    }
                } else {
                    Log.d("AuthenticationViewModel", "No token found, clearing session")
                    userPreferences.clearUser()
                    loggedInUser.value = null
                }
            } catch (e: Exception) {
                Log.e("AuthenticationViewModel", "Error during logout", e)
            }
        }
    }







}
