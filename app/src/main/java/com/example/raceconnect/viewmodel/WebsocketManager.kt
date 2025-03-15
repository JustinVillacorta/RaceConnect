package com.example.raceconnect.viewmodel

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.raceconnect.model.MessageData
import com.example.raceconnect.model.NotificationData
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit

object WebSocketManager {
    // Replace with your server IP and port from .env (e.g., ws://your-server-ip:8080)
    private const val BASE_URL = "ws://localhost:8080"
    private const val TAG = "WebSocketManager"

    private val client = OkHttpClient.Builder()
        .pingInterval(10, TimeUnit.SECONDS) // Keep connection alive
        .build()

    private var webSocket: WebSocket? = null
    private val _incomingMessages = MutableStateFlow<List<MessageData>>(emptyList())
    val incomingMessages = _incomingMessages.asStateFlow()

    private val _notifications = MutableStateFlow<NotificationData?>(null)
    val notifications = _notifications.asStateFlow()

    private val gson = Gson()

    fun connect(userId: String) {
        if (webSocket != null) {
            Log.d(TAG, "WebSocket already connected.")
            return
        }

        // Append user_id to the WebSocket URL as a query parameter
        val url = "$BASE_URL?user_id=$userId"
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected for user $userId!")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received message: $text")
                try {
                    val jsonObject = gson.fromJson(text, Map::class.java)
                    val type = jsonObject["type"] as? String
                    when (type) {
                        "new_message" -> {
                            val messageData = gson.fromJson(text, MessageData::class.java)
                            _incomingMessages.update { currentMessages -> listOf(messageData) + currentMessages }
                        }
                        "conversation_history" -> {
                            val messages = jsonObject["messages"] as? List<Map<String, Any>> ?: emptyList()
                            val messageList = messages.map { map ->
                                MessageData(
                                    message_id = (map["id"] as? Double)?.toInt(),
                                    conversation_id = map["conversation_id"] as? String,
                                    sender_id = map["sender_id"] as? String,
                                    receiver_id = map["receiver_id"] as? String,
                                    message_type = map["message_type"] as? String,
                                    message = map["message"] as? String,
                                    media_url = map["media_url"] as? String,
                                    status = map["status"] as? String,
                                    timestamp = map["created_at"] as? String
                                )
                            }
                            _incomingMessages.update { messageList }
                        }
                        "message_status" -> {
                            // Handle read status updates if needed
                            Log.d(TAG, "Message status update: $text")
                        }
                        "typing" -> {
                            // Handle typing indicator if needed
                            Log.d(TAG, "Typing indicator: $text")
                        }
                        "error" -> {
                            Log.e(TAG, "Server error: ${jsonObject["message"]}")
                        }
                        "notification" -> {
                            val data = jsonObject["data"] as? Map<*, *>
                            val title = data?.get("notification_title") as? String
                            val message = data?.get("message") as? String
                            _notifications.value = NotificationData(title, message)
                        }
                    }
                } catch (e: JsonSyntaxException) {
                    Log.e(TAG, "Failed to parse WebSocket message: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure: ${t.message}", t)
                this@WebSocketManager.webSocket = null
                Handler(Looper.getMainLooper()).postDelayed({
                    Log.d(TAG, "Reconnecting WebSocket...")
                    connect(userId)
                }, 5000)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $reason")
                webSocket.close(code, reason)
                this@WebSocketManager.webSocket = null
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $reason")
                this@WebSocketManager.webSocket = null
            }
        })
    }

    fun sendMessage(
        conversationId: String,
        senderId: String,
        receiverId: String,
        message: String,
        messageType: String = "text",
        mediaUrl: String? = null
    ) {
        val json = gson.toJson(mapOf(
            "type" to "send_message",
            "conversation_id" to conversationId,
            "sender_id" to senderId,
            "receiver_id" to receiverId,
            "message" to message,
            "message_type" to messageType,
            "media_url" to mediaUrl
        ))
        if (webSocket != null) {
            val success = webSocket?.send(json) == true
            Log.d(TAG, if (success) "Sent: $json" else "Failed to send: $json")
        } else {
            Log.e(TAG, "WebSocket is not connected. Cannot send message: $json")
        }
    }

    fun fetchMessages(conversationId: String) {
        val json = gson.toJson(mapOf(
            "type" to "fetch_messages",
            "conversation_id" to conversationId
        ))
        if (webSocket != null) {
            val success = webSocket?.send(json) == true
            Log.d(TAG, if (success) "Sent fetch request: $json" else "Failed to send fetch request: $json")
        } else {
            Log.e(TAG, "WebSocket is not connected. Cannot fetch messages: $json")
        }
    }

    fun markMessageAsRead(messageId: Int, userId: String) {
        val json = gson.toJson(mapOf(
            "type" to "message_read",
            "message_id" to messageId,
            "user_id" to userId
        ))
        if (webSocket != null) {
            val success = webSocket?.send(json) == true
            Log.d(TAG, if (success) "Sent read status: $json" else "Failed to send read status: $json")
        } else {
            Log.e(TAG, "WebSocket is not connected. Cannot mark message as read: $json")
        }
    }

    fun sendTypingIndicator(conversationId: String, userId: String) {
        val json = gson.toJson(mapOf(
            "type" to "typing",
            "conversation_id" to conversationId,
            "user_id" to userId
        ))
        if (webSocket != null) {
            val success = webSocket?.send(json) == true
            Log.d(TAG, if (success) "Sent typing indicator: $json" else "Failed to send typing indicator: $json")
        } else {
            Log.e(TAG, "WebSocket is not connected. Cannot send typing indicator: $json")
        }
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting WebSocket...")
        webSocket?.close(1000, "Normal Closure")
        webSocket = null
        Log.d(TAG, "WebSocket disconnected.")
    }
}