package com.example.raceconnect.view.Screens.MenuScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.Conversation
import com.example.raceconnect.network.RetrofitInstance
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.view.Navigation.NavRoutes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    navController: NavController,
    userPreferences: UserPreferences // Inject UserPreferences to get logged-in user
) {
    val scope = rememberCoroutineScope()
    val currentUser by userPreferences.user.collectAsState(initial = null)
    val currentUserId = currentUser?.id ?: 0

    // State to hold conversations
    var conversations by remember { mutableStateOf<List<Conversation>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch conversations when user ID is available
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
                title = { Text("Conversations") },
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
                                        sellerId = if (currentUserId == conversation.buyerId) conversation.sellerId else conversation.buyerId
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
                text = conversation.lastMessageTime?.split(" ")?.get(1) ?: "", // Simple time formatting
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}