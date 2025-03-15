package com.example.raceconnect.model

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
    val conversation_id: String?,
    val sender_id: String?,
    val receiver_id: String?,
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
    val id: Int?,
    val buyer_id: Int,
    val seller_id: Int,
    val product_id: Int,
    val last_message: String?,
    val last_message_time: String?,
    val created_at: String?,
    val status: String?,
    val unread_count_buyer: Int?,
    val unread_count_seller: Int?,
    val last_activity_at: String?
)

