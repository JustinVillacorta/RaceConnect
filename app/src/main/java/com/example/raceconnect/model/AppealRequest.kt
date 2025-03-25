package com.example.raceconnect.model

data class AppealRequest(
    val id: Int? = null,
    val username: String,
    val email: String,
    val concernType: ConcernType,
    val postId: Int? = null,
    val itemId: Int? = null,
    val description: String,
    val status: AppealStatus = AppealStatus.PENDING,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

enum class ConcernType {
    ACCOUNT_PENALTY,
    POST_PENALTY,
    ITEM_POST_PENALTY
}

enum class AppealStatus {
    PENDING,
    APPROVED,
    REJECTED
}
