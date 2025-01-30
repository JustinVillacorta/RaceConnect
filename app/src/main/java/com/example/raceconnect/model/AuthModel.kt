package com.example.raceconnect.model
// Data class representing a User object
data class users(
    val id: Int,
    val username: String,
    val email: String,
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

