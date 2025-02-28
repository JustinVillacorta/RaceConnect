package com.example.raceconnect.viewmodel.Authentication

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.ApiResponse
import com.example.raceconnect.model.ForgotPasswordRequest
import com.example.raceconnect.model.LoginRequest
import com.example.raceconnect.model.LoginResponse
import com.example.raceconnect.model.ResetPasswordRequest
import com.example.raceconnect.model.SignupRequest
import com.example.raceconnect.model.users
import com.example.raceconnect.network.ApiService
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response

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

    fun signUp(context: Context, username: String, email: String, password: String, onToast: (String) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val signupRequest = SignupRequest(username, email, password)
                val response = RetrofitInstance.api.signup(signupRequest) // Expecting Response<SignupResponse>

                Log.d("AuthenticationViewModel", "Server response code: ${response.code()}")
                Log.d("AuthenticationViewModel", "Raw response: ${response.raw()}")

                if (response.isSuccessful || response.code() == 201) { // ✅ Handle HTTP 201
                    val userResponse = response.body()
                    Log.d("AuthenticationViewModel", "Response body: $userResponse")

                    if (userResponse?.token != null) { // Ensure response is valid
                        loggedInUser.value = userResponse.user
                        Log.d("AuthenticationViewModel", "Signup successful: ${loggedInUser.value}")

                        // Save user data to DataStore
                        userPreferences.saveUser(
                            userResponse.user!!.id,
                            userResponse.user!!.username,
                            userResponse.user!!.email,
                            userResponse.token
                        )

                        onToast("Account Created Successfully!")
                    } else {
                        Log.e("AuthenticationViewModel", "Error: response.body() is null despite 201 status")
                        onToast("Account Created Successfully!") // ✅ Show success even if body is null
                    }

                } else {
                    val errorBody = response.errorBody()?.string() ?: "Signup failed"
                    Log.e("AuthenticationViewModel", "Signup failed: $errorBody")

                    val detailedMsg = when {
                        errorBody.contains("username", ignoreCase = true) -> "Username is already taken. Please try another."
                        errorBody.contains("email", ignoreCase = true) -> "Email is already in use. Try logging in instead."
                        response.code() == 400 -> "Signup failed: Invalid input or user already exists."
                        response.code() == 500 -> "Server error. Please try again later."
                        else -> errorBody
                    }

                    errorMessage.value = detailedMsg
                    onToast(detailedMsg)
                }

            } catch (e: Exception) {
                val errorMsg = e.message ?: "An unexpected error occurred. Please try again."
                errorMessage.value = errorMsg
                onToast(errorMsg)
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



    //forgot password
    fun forgotPassword(context: Context, email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            Log.d("ForgotPassword", "Sending forgot password request for email: $email")

            try {
                val response: retrofit2.Response<ApiResponse> = RetrofitInstance.api.forgotPassword(
                    ForgotPasswordRequest(email)
                )

                Log.d("ForgotPassword", "Server response code: ${response.code()}")
                Log.d("ForgotPassword", "Raw response: ${response.raw()}")

                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Check your email for OTP"
                    Log.d("ForgotPassword", "OTP sent successfully. Server message: $message")
                    onResult(true, message)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to send OTP"
                    Log.e("ForgotPassword", "Error: $errorMsg")
                    onResult(false, errorMsg)
                }
            } catch (e: Exception) {
                Log.e("ForgotPassword", "Exception: ${e.message}")
                onResult(false, "Network error. Please try again.")
            }
        }
    }

    fun resetPassword(context: Context, email: String, otp: String, newPassword: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            Log.d("ResetPassword", "Sending reset password request for email: $email with OTP: $otp")

            try {
                val response: retrofit2.Response<ApiResponse> = RetrofitInstance.api.resetPassword(
                    ResetPasswordRequest(email, otp, newPassword)
                )

                Log.d("ResetPassword", "Server response code: ${response.code()}")
                Log.d("ResetPassword", "Raw response: ${response.raw()}")

                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Password reset successfully!"
                    Log.d("ResetPassword", "Password reset successful. Server message: $message")
                    onResult(true, message)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to reset password"
                    Log.e("ResetPassword", "Error: $errorMsg")
                    onResult(false, errorMsg)
                }
            } catch (e: Exception) {
                Log.e("ResetPassword", "Exception: ${e.message}")
                onResult(false, "Network error. Please try again.")
            }
        }
    }

}