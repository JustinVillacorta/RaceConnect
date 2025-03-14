package com.example.raceconnect.model

import com.google.gson.annotations.SerializedName

data class MarketplaceDataClassItem(
    @SerializedName("id") val id: Int,
    @SerializedName("seller_id") val seller_id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("price") val price: String,
    @SerializedName("listing_status") val listing_status: String = "Available",
    @SerializedName("status") val status: String = "Active",
    @SerializedName("image_url") val image_url: String?, // Made nullable
    @SerializedName("favorite_count") val favorite_count: Int = 0,
    @SerializedName("archived_at") val archived_at: String? = null,
    @SerializedName("report") val report: String = "none",
    @SerializedName("reported_at") val reported_at: String? = null,
    @SerializedName("created_at") val created_at: String,
    @SerializedName("updated_at") val updated_at: String,
    @SerializedName("previous_status") val previous_status: String? = null
)

data class MarketplaceItemLike(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("marketplace_item_id") val marketplaceItemId: Int,
    @SerializedName("owner_id") val ownerId: Int,
    @SerializedName("created_at") val createdAt: String
)

data class MarketplaceImageResponse(
    @SerializedName("image_url") val image_url: String
)

data class itemPostResponse(
    val message: String,
    val item_id: String
)