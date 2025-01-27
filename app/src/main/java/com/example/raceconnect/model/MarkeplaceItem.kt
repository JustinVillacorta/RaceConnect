package com.example.raceconnect.model



data class MarkeplaceItem(
    val id: Int,
    val title: String,
    val price: String,
    val description: String,
    val images: List<String> // Change from List<Int> to List<String>
)