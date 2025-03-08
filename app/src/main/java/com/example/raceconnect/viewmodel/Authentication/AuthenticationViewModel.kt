package com.example.raceconnect.viewmodel.Authentication

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.ForgotPasswordRequest
import com.example.raceconnect.model.LoginRequest
import com.example.raceconnect.model.LoginResponse
import com.example.raceconnect.model.ResetPasswordRequest
import com.example.raceconnect.model.SignupRequest
import com.example.raceconnect.model.VerifyOtpRequest
import com.example.raceconnect.model.users
import com.example.raceconnect.network.RetrofitInstance
import com.example.raceconnect.viewmodel.MenuViewModel.MenuViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class AuthenticationViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)

    val isLoading = MutableStateFlow(false)
    val ErrorMessage = MutableStateFlow<String?>(null)
    val loggedInUser = MutableStateFlow<users?>(null)

    private val _otpSent = MutableStateFlow(false)
    val otpSent: StateFlow<Boolean> = _otpSent

    private val _otpVerified = MutableStateFlow(false)
    val otpVerified: StateFlow<Boolean> = _otpVerified

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadUser()
    }

    // Validate Login and Save User Data
    fun validateLogin(username: String, password: String) {
        viewModelScope.launch {
            isLoading.value = true
            ErrorMessage.value = null
            try {
                val loginRequest = LoginRequest(username, password)
                val response: LoginResponse = RetrofitInstance.api.login(loginRequest)

                if (response.token != null && response.user != null) {
                    loggedInUser.value = response.user
                    Log.d("AuthenticationViewModel", "Login successful: ${loggedInUser.value}")

                    // Save all user data to DataStore
                    userPreferences.saveUser(
                        userId = response.user.id,
                        username = response.user.username,
                        email = response.user.email,
                        token = response.token,
                        birthdate = response.user.birthdate,
                        number = response.user.number,
                        address = response.user.address,
                        age = response.user.age,
                        profilePicture = response.user.profilePicture,
                        bio = response.user.bio,
                        favoriteCategories = response.user.favoriteCategories?.joinToString(","),
                        favoriteMarketplaceItems = response.user.favoriteMarketplaceItems?.joinToString(",")
                    )
                } else {
                    ErrorMessage.value = response.message ?: "Login failed"
                    Log.d("AuthenticationViewModel", "Login failed: ${response.message}")
                }
            } catch (e: Exception) {
                ErrorMessage.value = e.message ?: "An unexpected error occurred"
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

    // Sign up
    fun signUp(context: Context, username: String, email: String, password: String, onToast: (String) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            ErrorMessage.value = null
            try {
                val signupRequest = SignupRequest(username, email, password)
                val response = RetrofitInstance.api.signup(signupRequest)

                Log.d("AuthenticationViewModel", "Server response code: ${response.code()}")
                Log.d("AuthenticationViewModel", "Raw response: ${response.raw()}")

                if (response.isSuccessful || response.code() == 201) {
                    val userResponse = response.body()
                    Log.d("AuthenticationViewModel", "Response body: $userResponse")

                    if (userResponse?.token != null) {
                        loggedInUser.value = userResponse.user
                        Log.d("AuthenticationViewModel", "Signup successful: ${loggedInUser.value}")

                        // Save user data to DataStore
                        userPreferences.saveUser(
                            userId = userResponse.user!!.id,
                            username = userResponse.user!!.username,
                            email = userResponse.user!!.email,
                            token = userResponse.token,
                            birthdate = userResponse.user!!.birthdate,
                            number = userResponse.user!!.number,
                            address = userResponse.user!!.address,
                            age = userResponse.user!!.age,
                            profilePicture = userResponse.user!!.profilePicture,
                            bio = userResponse.user!!.bio,
                            favoriteCategories = userResponse.user!!.favoriteCategories?.joinToString(","),
                            favoriteMarketplaceItems = userResponse.user!!.favoriteMarketplaceItems?.joinToString(",")
                        )

                        onToast("Account Created Successfully!")
                    } else {
                        Log.e("AuthenticationViewModel", "Error: response.body() is null despite 201 status")
                        onToast("Account Created Successfully!")
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

                    ErrorMessage.value = detailedMsg
                    onToast(detailedMsg)
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "An unexpected error occurred. Please try again."
                ErrorMessage.value = errorMsg
                onToast(errorMsg)
                Log.e("AuthenticationViewModel", "Error during signup", e)
            } finally {
                isLoading.value = false
            }
        }
    }

    fun logout(menuViewModel: MenuViewModel, onLogoutResult: () -> Unit) {
        viewModelScope.launch {
            try {
                val token = withContext(Dispatchers.IO) { userPreferences.getToken() }
                if (!token.isNullOrEmpty()) {
                    val authHeader = "Bearer $token"
                    Log.d("AuthenticationViewModel", "Sending logout request with Authorization: $authHeader")
                    val response = RetrofitInstance.api.logout(authHeader)
                    if (response.isSuccessful) {
                        userPreferences.clearUser()
                        loggedInUser.value = null
                        menuViewModel.clearData()
                        Log.d("AuthenticationViewModel", "✅ User logged out successfully")
                        onLogoutResult()
                    } else {
                        val errorResponse = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("AuthenticationViewModel", "❌ Logout failed: $errorResponse")
                    }
                } else {
                    Log.d("AuthenticationViewModel", "⚠️ No token found, clearing session")
                    userPreferences.clearUser()
                    loggedInUser.value = null
                    menuViewModel.clearData()
                    onLogoutResult()
                }
            } catch (e: Exception) {
                Log.e("AuthenticationViewModel", "❌ Error during logout", e)
            }
        }
    }

    // Forgot Password
    fun requestOtp(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            Log.d("AuthenticationViewModel", "🔄 Requesting OTP for email: $email")

            try {
                val response = RetrofitInstance.api.requestOtp(ForgotPasswordRequest(email))

                Log.d("AuthenticationViewModel", "🔍 Raw Response: ${response.raw()}")

                if (response.isSuccessful) {
                    _otpSent.value = true
                    Log.d("AuthenticationViewModel", "✅ OTP sent successfully to $email")
                    onResult(true, email)
                } else {
                    _otpSent.value = false
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("AuthenticationViewModel", "❌ Failed to send OTP: $errorBody")
                    onResult(false, "")
                }
            } catch (e: Exception) {
                _otpSent.value = false
                Log.e("AuthenticationViewModel", "❌ Network error while requesting OTP: ${e.message}", e)
                onResult(false, "")
            }
        }
    }

    fun verifyOtp(email: String, otp: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            Log.d("AuthenticationViewModel", "🔄 Verifying OTP for email: $email, OTP: $otp")

            try {
                val response = RetrofitInstance.api.verifyOtp(VerifyOtpRequest(email, otp))

                Log.d("AuthenticationViewModel", "🔍 Raw Response: ${response.raw()}")

                val responseBody = response.body()
                Log.d("AuthenticationViewModel", "✅ Response Body: $responseBody")

                if (response.isSuccessful && (responseBody?.verified == true || responseBody?.message?.contains("OTP verified", ignoreCase = true) == true)) {
                    _otpVerified.value = true
                    Log.d("AuthenticationViewModel", "✅ OTP verification successful for email: $email")
                    onResult(true, email)
                } else {
                    _otpVerified.value = false
                    Log.e("AuthenticationViewModel", "❌ OTP verification failed: ${responseBody?.message ?: "Invalid response format"}")
                    onResult(false, "")
                }
            } catch (e: Exception) {
                _otpVerified.value = false
                Log.e("AuthenticationViewModel", "❌ Network error while verifying OTP: ${e.message}", e)
                onResult(false, "")
            }
        }
    }

    fun resetPassword(email: String, password: String, confirmPassword: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            Log.d("ResetPasswordScreen", "🔄 Attempting to reset password for email: $email")
            Log.d("ResetPasswordScreen", "📤 Request Body: { email: \"$email\", newPassword: \"$password\", confirmPassword: \"$confirmPassword\" }")

            try {
                val requestBody = ResetPasswordRequest(email, password, confirmPassword)
                val response = RetrofitInstance.api.resetPassword(requestBody)

                Log.d("ResetPasswordScreen", "🔍 Raw Response: ${response.raw()}")

                if (response.isSuccessful) {
                    Log.d("ResetPasswordScreen", "✅ Password reset successful for $email")
                    onResult(true, "Password reset successfully.")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("ResetPasswordScreen", "❌ Failed to reset password: $errorBody")
                    onResult(false, errorBody)
                }
            } catch (e: Exception) {
                Log.e("ResetPasswordScreen", "❌ Network error while resetting password: ${e.message}", e)
                onResult(false, "Network error: ${e.message}")
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}