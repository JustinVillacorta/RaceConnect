package com.example.raceconnect.Model

data class Notification(
    val profilePic: Int, // Resource ID for profile picture
    val userName: String,
    val action: String,
    val timestamp: String
)