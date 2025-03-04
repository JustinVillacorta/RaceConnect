package com.example.raceconnect.view.Screens.MarketplaceScreens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSellerScreen(itemId: Int, navController: NavController) {
    // Simulate seller and item data (replace with actual data source)
    val sellerImageUrl = "https://example.com/mclaren_polo.jpg" // Seller profile image
    val itemTitle = "McLaren 2024 Team Polo"
    val currentUserId = 1 // Mock user ID (buyer)
    val sellerId = 2 // Mock seller ID

    // State for chat messages and selected photo
    var messages by remember { mutableStateOf<List<ChatMessage>>(listOf(
        ChatMessage(
            id = 1,
            senderId = currentUserId, // Buyer’s message
            content = "Hi there, is the product still available?",
            timestamp = Date()
        )
    ))}
    var messageInput by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher for picking an image from the gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedPhotoUri = uri // Store the selected photo URI
            // Add the photo as a message at the bottom
            uri?.let { photoUri ->
                messages = messages + ChatMessage(
                    id = messages.size + 1,
                    senderId = currentUserId,
                    content = "Photo", // Placeholder for photo
                    timestamp = Date(),
                    photoUri = photoUri
                )
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Custom Top Bar for ChatSellerScreen (red with "Chat Seller" and back arrow)
        TopAppBar(
            title = { Text("Chat Seller") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFD32F2F) // Exact red as in the image
            )
        )

        // Chat Content (Header, Messages, and Input)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp) // Adjusted padding for clean look
        ) {
            // Chat Header (Seller Profile and Product Title)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Seller Profile Image (circular, centered)
                AsyncImage(
                    model = sellerImageUrl,
                    contentDescription = "Seller Profile",
                    modifier = Modifier
                        .size(60.dp) // Smaller size for messenger-like design
                        .clip(CircleShape)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Product Title (centered)
                Text(
                    text = itemTitle,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            // Chat Messages (Scrollable, new messages at bottom)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 0.dp), // Remove horizontal padding to match the image's border
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(
                        message = message,
                        isSender = message.senderId == currentUserId,
                        sellerImageUrl = sellerImageUrl
                    )
                }
            }

            // Message Input Field (messenger-style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(8.dp)) // White background for input
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    photoPickerLauncher.launch("image/*")
                }) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Attach Photo",
                        tint = Color(0xFFD32F2F) // Red to match the image
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
                        cursorColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                IconButton(
                    onClick = {
                        if (messageInput.isNotBlank() || selectedPhotoUri != null) {
                            messages = messages + ChatMessage(
                                id = messages.size + 1,
                                senderId = currentUserId,
                                content = messageInput.ifBlank { "Photo" }, // Use "Photo" if no text but photo selected
                                timestamp = Date(),
                                photoUri = selectedPhotoUri
                            )
                            messageInput = "" // Clear input
                            selectedPhotoUri = null // Clear photo selection
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Message",
                        tint = Color(0xFFD32F2F) // Red to match the image
                    )
                }
            }
        }
    }
}

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
                .widthIn(max = 250.dp) // Limit bubble width for readability
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

            // Display photo if available
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

// Preview function for ChatSellerScreen
@Preview(showBackground = true, name = "Chat Seller Screen")
@Composable
fun ChatSellerScreenPreview() {
    MaterialTheme {
        ChatSellerScreen(
            itemId = 1,
            navController = rememberNavController()
        )
    }
}