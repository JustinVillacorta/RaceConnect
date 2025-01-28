package com.example.raceconnect.model

data class LoginRequest(
    val username: String,
    val password: String
)


data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: User? = null // Includes user details if successful
)



data class User(
    val id: Int,
    val username: String,
    val email: String,
    val token: String // Token for authentication
)