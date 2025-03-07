package com.example.raceconnect.model

import com.google.gson.annotations.SerializedName

// Data class representing a User object
data class users(
    val id: Int,
    val username: String,
    val email: String,
    val birthdate: String?,
    val number: String?,
    val address: String?,
    val age: Int?,
    @SerializedName("profile_picture") val profilePicture: String?,
    val bio: String?,
    @SerializedName("favorite_categories") val favoriteCategories: List<String>?,
    @SerializedName("favorite_marketplace_items") val favoriteMarketplaceItems: List<String>?,
    @SerializedName("friends_list") val friendsList: List<Int>?,
    @SerializedName("friend_privacy") val friendPrivacy: String?,
    @SerializedName("last_online") val lastOnline: String?,
    val status: String?,
    val report: String?,
    @SerializedName("suspension_end_date") val suspensionEndDate: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class UpdateUserRequest(
    val username: String,
    val birthdate: String?,
    val number: String?,
    val address: String?,
    val bio: String?
)

data class UploadProfilePictureResponse(
    val message: String,
    @SerializedName("image_url") val imageUrl: String?
)

data class ProfileImage(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("image_url") val imageUrl: String
)

data class UserSimpleResponse(val message: String) // Single definition

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

// Sign up
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

// Logout
data class LogoutRequest(val token: String)
data class LogoutResponse(val message: String)

// Forgot password
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