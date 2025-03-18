package com.example.raceconnect.view.Screens.MarketplaceScreens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.Message
import com.example.raceconnect.model.MessageData
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.ui.theme.Red
import com.google.gson.Gson
import okhttp3.*
import okio.ByteString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSellerScreen(
    itemId: Int,
    conversationId: Int,
    sellerId: Int,
    itemTitle: String,
    itemImage: String?,
    navController: NavController,
    userPreferences: UserPreferences,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var userId by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(Unit) {
        userId = userPreferences.getUserId()
    }
    val messages = remember { mutableStateListOf<Message>() }
    val messageIds = remember { mutableSetOf<Int>() }
    var inputText by remember { mutableStateOf("") }
    var attachedImageUri by remember { mutableStateOf<Uri?>(null) }
    var webSocket by remember { mutableStateOf<WebSocket?>(null) }
    val gson = Gson()
    val client = remember { OkHttpClient() }
    val listState = rememberLazyListState()

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                attachedImageUri = uri
                Log.d("ChatSellerScreen", "Image selected: $uri")
            }
        } else {
            Log.w("ChatSellerScreen", "Image selection failed with result code: ${result.resultCode}")
        }
    }

    class ChatWebSocketListener(
        private val coroutineScope: CoroutineScope,
        private val messages: MutableList<Message>,
        private val messageIds: MutableSet<Int>,
        private val listState: LazyListState,
        private val userId: Int?,
        private val conversationId: Int,
        private val gson: Gson
    ) : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
            super.onOpen(webSocket, response)
            Log.d("ChatSellerScreen", "WebSocket opened for user $userId, conversation $conversationId")
            val fetchMessageRequest = mapOf(
                "type" to "fetch_messages",
                "conversation_id" to conversationId,
                "limit" to 50,
                "offset" to 0
            )
            webSocket.send(gson.toJson(fetchMessageRequest))
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val data = gson.fromJson(text, Map::class.java)
            Log.d("ChatSellerScreen", "Received message: $text")
            when (data["type"]) {
                "conversation_history" -> {
                    val msgList = gson.fromJson(
                        gson.toJson(data["messages"]),
                        Array<Message>::class.java
                    ).toList()
                    coroutineScope.launch {
                        messages.clear()
                        messageIds.clear()
                        msgList.forEach { msg ->
                            val adjustedMsg = msg.copy(
                                created_at = adjustTimestamp(msg.created_at),
                                images = msg.images
                            )
                            adjustedMsg.id?.let {
                                if (messageIds.add(it)) {
                                    messages.add(adjustedMsg)
                                }
                            }
                        }
                        messages.sortBy { it.created_at }
                        coroutineScope.launch {
                            if (messages.isNotEmpty()) {
                                listState.scrollToItem(messages.size - 1)
                            }
                        }
                    }
                }
                "message_sent", "new_message" -> {
                    val msg = gson.fromJson(text, MessageData::class.java)
                    coroutineScope.launch {
                        val adjustedTimestamp = adjustTimestamp(msg.timestamp)
                        val newMessage = Message(
                            id = msg.message_id,
                            conversation_id = msg.conversation_id ?: 0,
                            sender_id = msg.sender_id ?: 0,
                            receiver_id = msg.receiver_id ?: 0,
                            message_type = msg.message_type ?: "text",
                            message = msg.message ?: "",
                            media_url = msg.media_url,
                            images = msg.images ?: if (msg.media_url != null) listOf(msg.media_url) else null,
                            status = msg.status,
                            created_at = adjustedTimestamp,
                            delivered_at = null,
                            read_at = null,
                            is_deleted = false
                        )
                        newMessage.id?.let {
                            if (messageIds.add(it)) {
                                messages.add(newMessage)
                                coroutineScope.launch {
                                    listState.scrollToItem(messages.size - 1)
                                }
                            } else {
                                Log.d("ChatSellerScreen", "Duplicate message ID filtered: $it")
                            }
                        }
                    }
                }
                "error" -> {
                    Log.e("ChatSellerScreen", "Server error: ${data["message"]}")
                }
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null)
            Log.d("ChatSellerScreen", "WebSocket closing: $code $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
            Log.e("ChatSellerScreen", "WebSocket error: ${t.message}", t)
        }
    }

    LaunchedEffect(userId, conversationId) {
        if (userId == null) {
            onClose()
            return@LaunchedEffect
        }

        webSocket?.close(NORMAL_CLOSURE_STATUS, null)

        val request = Request.Builder()
            .url("ws://192.168.5.157:8080?user_id=${userId!!}")
            .build()

        webSocket = client.newWebSocket(
            request,
            ChatWebSocketListener(
                coroutineScope = coroutineScope,
                messages = messages,
                messageIds = messageIds,
                listState = listState,
                userId = userId,
                conversationId = conversationId,
                gson = gson
            )
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            coroutineScope.launch {
                webSocket?.close(NORMAL_CLOSURE_STATUS, null)
                client.dispatcher.executorService.shutdown()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        itemImage?.let {
                            AsyncImage(
                                model = it,
                                contentDescription = "Item Image",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .padding(end = 8.dp)
                            )
                        }
                        Text(itemTitle, color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Red)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                items(messages) { message ->
                    val isSentByUser = message.sender_id == userId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = if (isSentByUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSentByUser) Red else Color.LightGray
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp, 6.dp)) {
                                if (message.message.isNotBlank()) {
                                    Text(
                                        text = message.message,
                                        color = if (isSentByUser) Color.White else Color.Black,
                                        style = TextStyle(fontSize = 16.sp)
                                    )
                                }
                                message.images?.forEach { imageUrl ->
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(imageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Sent image",
                                        modifier = Modifier
                                            .size(100.dp)
                                            .padding(4.dp)
                                            .clickable {
                                                // Navigate to FullScreenImage route with a dummy postId (e.g., 0) since it's not tied to a post
                                                navController.navigate(
                                                    NavRoutes.FullScreenImage.createRoute(
                                                        postId = 0, // Using 0 as a placeholder since chat images aren't tied to posts
                                                        imageUrl = imageUrl
                                                    )
                                                )
                                            }
                                    )
                                }
                                Text(
                                    text = formatTime(message.created_at),
                                    color = Color.Gray,
                                    style = TextStyle(fontSize = 12.sp),
                                    modifier = Modifier.align(if (isSentByUser) Alignment.End else Alignment.Start)
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    attachedImageUri?.let { uri ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(modifier = Modifier.size(50.dp)) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(uri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Attached image preview",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                IconButton(
                                    onClick = { attachedImageUri = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove image",
                                        tint = Color.White,
                                        modifier = Modifier.background(Color.Red, RoundedCornerShape(50))
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_PICK)
                                intent.type = "image/*"
                                launcher.launch(intent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Attach Image",
                                tint = Red
                            )
                        }

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp, end = 8.dp),
                            label = { Text("Type a message...") },
                            textStyle = TextStyle(fontSize = 16.sp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Red,
                                unfocusedBorderColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        IconButton(
                            onClick = {
                                if ((inputText.isNotBlank() || attachedImageUri != null) && userId != null) {
                                    if (attachedImageUri != null) {
                                        sendImageToServer(
                                            attachedImageUri!!,
                                            context,
                                            userId,
                                            conversationId,
                                            sellerId,
                                            webSocket,
                                            gson,
                                            coroutineScope,
                                            inputText
                                        )
                                        attachedImageUri = null
                                    } else {
                                        val messageData = mapOf(
                                            "type" to "send_message",
                                            "conversation_id" to conversationId,
                                            "sender_id" to userId!!,
                                            "receiver_id" to sellerId,
                                            "message" to inputText
                                        )
                                        if (webSocket?.send(gson.toJson(messageData)) == true) {
                                            Log.d("ChatSellerScreen", "Text message sent: $inputText")
                                        } else {
                                            Log.e("ChatSellerScreen", "Failed to send text message")
                                        }
                                    }
                                    inputText = ""
                                } else {
                                    Log.w("ChatSellerScreen", "Cannot send: userId=$userId, inputText=$inputText, attachedImageUri=$attachedImageUri")
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Red)
                        }
                    }
                }
            }
        }
    }
}
// Function to adjust timestamp
private fun adjustTimestamp(timestamp: String?): String? {
    return timestamp?.let {
        try {
            val dateFormat = if (timestamp.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            }
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            val utcDate = dateFormat.parse(timestamp) ?: return timestamp

            val outputFormat = if (timestamp.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            }
            outputFormat.timeZone = TimeZone.getTimeZone("UTC")

            outputFormat.format(utcDate)
        } catch (e: Exception) {
            Log.e("ChatSellerScreen", "Error adjusting timestamp: ${e.message}, raw: $timestamp", e)
            timestamp
        }
    }
}

// Function to format the timestamp to show local date and time
private fun formatTime(timestamp: String?): String {
    return if (timestamp != null) {
        try {
            val utcFormat = if (timestamp.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            }
            utcFormat.timeZone = TimeZone.getTimeZone("UTC")

            val date = utcFormat.parse(timestamp)
            val localFormat = SimpleDateFormat("MMM dd, yyyy, h:mm a", Locale.getDefault())
            localFormat.timeZone = TimeZone.getDefault()

            localFormat.format(date ?: Date())
        } catch (e: Exception) {
            Log.e("ChatSellerScreen", "Error parsing timestamp for display: ${e.message}, raw: $timestamp")
            timestamp
        }
    } else {
        "N/A"
    }
}

// Function to send image to server via WebSocket
private fun sendImageToServer(
    uri: Uri,
    context: Context,
    userId: Int?,
    conversationId: Int,
    receiverId: Int,
    webSocket: WebSocket?,
    gson: Gson,
    coroutineScope: CoroutineScope,
    message: String
) {
    try {
        // Load the bitmap from the URI
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        // Compress the bitmap to JPEG
        val outputStream = ByteArrayOutputStream()
        val success = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        if (!success) {
            Log.e("ChatSellerScreen", "Failed to compress bitmap")
            return
        }
        val byteArray = outputStream.toByteArray()
        val base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)
        Log.d("ChatSellerScreen", "Base64 image size: ${base64Image.length} characters")

        if (userId != null) {
            val messageData = mapOf(
                "type" to "send_message",
                "conversation_id" to conversationId,
                "sender_id" to userId,
                "receiver_id" to receiverId,
                "message_type" to "image",
                "image_data" to base64Image,
                "message" to message
            )
            if (webSocket?.send(gson.toJson(messageData)) == true) {
                Log.d("ChatSellerScreen", "Image message sent: $message")
            } else {
                Log.e("ChatSellerScreen", "Failed to send image message via WebSocket")
            }
        } else {
            Log.e("ChatSellerScreen", "User ID is null, cannot send image")
        }
    } catch (e: Exception) {
        Log.e("ChatSellerScreen", "Error in sendImageToServer: ${e.message}", e)
    }
}

const val NORMAL_CLOSURE_STATUS = 1000