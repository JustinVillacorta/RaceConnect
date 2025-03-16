import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.WebSocketManager
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.style.TextAlign
import com.example.raceconnect.model.MessageData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSellerScreen(
    itemId: Int,
    conversationId: Int,
    sellerId: Int,
    navController: NavController,
    userPreferences: UserPreferences, // Inject UserPreferences
    onClose: () -> Unit = { navController.popBackStack() }
) {
    val context = LocalContext.current
    val currentUser by userPreferences.user.collectAsState(initial = null)
    val currentUserId = currentUser?.id ?: 0

    // WebSocket messages
    val messages by WebSocketManager.incomingMessages.collectAsState()
    val chatMessages = messages.filter { it.conversation_id == conversationId }
        .map { it.toChatMessage() }
        .sortedBy { it.timestamp }

    // Local state for input and photo
    var messageInput by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> selectedPhotoUri = uri }
    )

    // Connect to WebSocket when screen is composed
    DisposableEffect(currentUserId) {
        if (currentUserId != 0) {
            WebSocketManager.connect(currentUserId.toString())
            WebSocketManager.fetchMessages(conversationId.toString())
        }
        onDispose {
            WebSocketManager.disconnect()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat Seller") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Red,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Chat Header (Placeholder for seller info)
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = "https://via.placeholder.com/60", // Replace with actual seller image URL
                    contentDescription = "Seller Profile",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Item #$itemId", // Replace with actual item title
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // Chat Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatMessages) { message ->
                    ChatBubble(
                        message = message,
                        isSender = message.senderId == currentUserId,
                        sellerImageUrl = "https://via.placeholder.com/24" // Replace with seller image URL
                    )
                }
            }

            // Message Input Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Attach Photo",
                        tint = Color(0xFFD32F2F)
                    )
                }
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    placeholder = { Text("Message", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                IconButton(
                    onClick = {
                        if (messageInput.isNotBlank() || selectedPhotoUri != null) {
                            val messageContent = messageInput.ifBlank { "Photo" }
                            WebSocketManager.sendMessage(
                                conversationId = conversationId.toString(),
                                senderId = currentUserId.toString(),
                                receiverId = sellerId.toString(),
                                message = messageContent,
                                messageType = if (selectedPhotoUri != null) "image" else "text",
                                mediaUrl = selectedPhotoUri?.toString()
                            )
                            messageInput = ""
                            selectedPhotoUri = null
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Message",
                        tint = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

// Convert MessageData to ChatMessage
fun MessageData.toChatMessage(): ChatMessage {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val date = try {
        timestamp?.let { dateFormat.parse(it) } ?: Date()
    } catch (e: Exception) {
        Date()
    }
    return ChatMessage(
        id = this.message_id ?: 0,
        senderId = this.sender_id ?: 0,
        content = this.message ?: "",
        timestamp = date,
        photoUri = this.media_url?.let { Uri.parse(it) }
    )
}

// ChatMessage and ChatBubble remain the same as provided
data class ChatMessage(
    val id: Int,
    val senderId: Int,
    val content: String,
    val timestamp: Date,
    val photoUri: Uri? = null
)

@Composable
fun ChatBubble(message: ChatMessage, isSender: Boolean, sellerImageUrl: String) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val timeString = timeFormat.format(message.timestamp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isSender) Arrangement.End else Arrangement.Start
    ) {
        if (!isSender) {
            AsyncImage(
                model = sellerImageUrl,
                contentDescription = "Seller Profile",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .padding(end = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .widthIn(max = 250.dp)
                .background(
                    if (isSender) Color(0xFFE57373) else Color.LightGray,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSender) Color.White else Color.Black,
                maxLines = 10,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeString,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSender) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                textAlign = TextAlign.End
            )
            message.photoUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Chat Photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}