package com.example.raceconnect.model
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
