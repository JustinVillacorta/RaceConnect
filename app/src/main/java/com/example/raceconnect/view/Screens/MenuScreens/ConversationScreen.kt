package com.example.raceconnect.view.Screens.MenuScreens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.Conversation
import com.example.raceconnect.network.RetrofitInstance
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.ui.theme.White
import com.example.raceconnect.view.ui.theme.fontFamily
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    navController: NavController,
    userPreferences: UserPreferences
) {
    val scope = rememberCoroutineScope()
    val currentUser by userPreferences.user.collectAsState(initial = null)
    val currentUserId = currentUser?.id ?: 0

    var conversations by remember { mutableStateOf<List<Conversation>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUserId) {
        if (currentUserId != 0) {
            scope.launch {
                try {
                    val response = RetrofitInstance.api.getUserConversations(currentUserId)
                    if (response.success) {
                        conversations = response.conversations
                    } else {
                        errorMessage = response.error ?: "Failed to load conversations"
                    }
                } catch (e: Exception) {
                    errorMessage = "Error: ${e.message}"
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Marketplace Conversations",
                        fontFamily = fontFamily,
                        color = Color.White,
                        fontSize = 24.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = White // Set the back arrow to white
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Red,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (currentUserId == 0) {
                Text(
                    text = "Please log in to view conversations",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (conversations == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (conversations!!.isEmpty()) {
                Text(
                    text = "No conversations found",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(conversations!!) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            currentUserId = currentUserId,
                            onClick = {
                                navController.navigate(
                                    NavRoutes.ChatSeller.createRoute(
                                        itemId = conversation.productId,
                                        conversationId = conversation.conversationId,
                                        sellerId = if (currentUserId == conversation.buyerId) conversation.sellerId else conversation.buyerId,
                                        itemTitle = conversation.productTitle.toString(),
                                        itemImage = conversation.productImageUrl
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    currentUserId: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = conversation.productImageUrl ?: "https://via.placeholder.com/60",
                contentDescription = "Item Image",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                val otherUser = if (currentUserId == conversation.buyerId) {
                    conversation.sellerUsername
                } else {
                    conversation.buyerUsername
                }
                Text(
                    text = "$otherUser - ${conversation.productTitle}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = conversation.lastMessage ?: "No messages yet",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatTime(conversation.lastMessageTime),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Function to format timestamp assuming it's already in GMT+8
private fun formatTime(timestamp: String?): String {
    return if (timestamp != null) {
        try {
            val inputFormat = if (timestamp.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            }
            inputFormat.timeZone = TimeZone.getTimeZone("Asia/Manila") // Input is already GMT+8

            val date = inputFormat.parse(timestamp)
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getTimeZone("Asia/Manila") // Output in GMT+8

            val adjustedDate = Date(date.time + (3 * 60 * 1000)) // Add 4 minutes
            val formattedTime = outputFormat.format(adjustedDate)

            // Log for diagnostics
            val currentSystemTime = outputFormat.format(Date())
            Log.d("ConversationTime", "Raw timestamp: $timestamp")
            Log.d("ConversationTime", "Formatted time: $formattedTime")
            Log.d("ConversationTime", "Current system time (GMT+8): $currentSystemTime")

            formattedTime
        } catch (e: Exception) {
            Log.e("ConversationTime", "Error parsing timestamp: ${e.message}, raw: $timestamp")
            timestamp // Fallback to raw timestamp if parsing fails
        }
    } else {
        ""
    }
}