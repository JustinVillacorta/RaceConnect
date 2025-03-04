package com.example.raceconnect.model

data class Friend(
    val id: Int,
    val name: String,
    val profileImageUrl: String = "", // Optional, default to empty if no image
    val status: FriendStatus // Enum for friend request, friend, or explore status
)

enum class FriendStatus {
    REQUEST, // Friend request (pending)
    FRIEND,  // Confirmed friend
    EXPLORE  // Potential friend to add
}