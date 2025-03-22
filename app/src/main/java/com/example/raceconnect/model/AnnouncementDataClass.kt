package com.example.raceconnect.model

data class AnnouncementDataClass(
    val id: Int,
    val title: String,
    val content: String,
    val image_url: String?,
    val status: String,
    val created_at: String
)