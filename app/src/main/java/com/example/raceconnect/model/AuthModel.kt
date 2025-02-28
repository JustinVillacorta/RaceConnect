package com.example.raceconnect.model

import com.google.gson.annotations.SerializedName

// Data class representing a User object
data class users(
    val id: Int,
    val username: String,
    val email: String,
//    val birthdate: String,
//    val number: String,
//    val address: String,
//    val age: Int,
//    val profile_picture: String,
//    val bio: String,
//    val favorite_categories: List<String>,
//    val favorite_marketplace_items: List<String>
)

// Data class for login request
data class LoginRequest(
    val username: String,
    val password: String
)

// Data class for login response
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: users?,
    val token: String?
)
// sign up
data class SignupRequest(
    val username: String,
    val email: String,
    val password: String
)


data class SignupResponse(
    val token: String?,
    val user: users?,
    val message: String?
)



//logout

data class LogoutRequest(val token: String)
data class LogoutResponse(val message: String)


// forgot password

data class ForgotPasswordRequest(val email: String)
data class ForgotPasswordResponse(val message: String)

data class VerifyOtpRequest(val email: String, val otp: String)
data class VerifyOtpResponse(val message: String, val verified: Boolean)

data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("confirm_password") val confirmPassword: String
)


data class ResetPasswordResponse(val message: String)


data class ApiResponse(
    val message: String
)




