package com.example.raceconnect.model

import com.google.gson.annotations.SerializedName;

data class NotificationData(
    val title: String?,
    val message: String?
)

data class Message(
    val id: Int?,
    val conversation_id: Int,
    val sender_id: Int,
    val receiver_id: Int,
    val message_type: String?,
    val message: String,
    val media_url: String?,
    val status: String?,
    val created_at: String?,
    val delivered_at: String?,
    val read_at: String?,
    val is_deleted: Boolean?
)

data class MessageData(
    val message_id: Int?,
    val conversation_id: Int?,
    val sender_id: Int?,
    val receiver_id: Int?,
    val message_type: String?,
    val message: String?,
    val media_url: String?,
    val status: String?,
    val timestamp: String?
)

data class SendMessageRequest(
    val buyer_id: Int,
    val seller_id: Int,
    val product_id: Int,
    val sender_id: Int,
    val message: String,
    val message_type: String? = "text",
    val media_url: String? = null
)

data class SendMessageResponse(
    val success: Boolean,
    val conversation_id: Int?,
    val message: Message?,
    val error: String? // Optional field for error message
)

data class WebSocketClient(
    val user_id: String,
    val connection_id: Int,
    val created_at: String?
)

data class Conversation(
    @SerializedName("conversation_id") val conversationId: Int,
    @SerializedName("buyer_id") val buyerId: Int,
    @SerializedName("seller_id") val sellerId: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("last_message") val lastMessage: String?,
    @SerializedName("last_message_time") val lastMessageTime: String?,
    @SerializedName("last_activity_at") val lastActivityAt: String?,
    @SerializedName("buyer_username") val buyerUsername: String?,
    @SerializedName("seller_username") val sellerUsername: String?,
    @SerializedName("product_title") val productTitle: String?
)

data class ConversationsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("conversations") val conversations: List<Conversation>?,
    @SerializedName("error") val error: String?
)
