package com.example.raceconnect.model

data class MarketplaceDataClassItem(
    val id: Int,
    val seller_id: Int,
    val title: String,
    val description: String,
    val price: String, // Keeping as String to handle formatting (e.g., "10.00")
    val category: String,
    val image_url: String = "", // Default to empty if no image provided
    val favorite_count: Int = 0, // Default to 0 if not specified
    val status: String = "Available", // Default to "Available"
    val report: String = "None", // Default to "None"
    val reported_at: String? = null, // Nullable, default to null
    val previous_status: String? = null, // Nullable, default to null
    val listing_status: String = "Available", // Default to "Available"
    val created_at: String = "", // Default to empty string, filled by backend
    val updated_at: String = "" // Default to empty string, filled by backend
)
data class itemPostResponse(
    val message: String
    )