package com.example.raceconnect.view

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.raceconnect.R
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.Notification
import com.example.raceconnect.network.RetrofitInstance
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.NotificationClickedViewModel
import com.example.raceconnect.viewmodel.NotificationViewModel
import com.example.raceconnect.viewmodel.NotificationViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

fun parseItemTitleFromContent(content: String): String? {
    val regex = Regex("about '(.+?)'")
    val match = regex.find(content)
    return match?.groupValues?.get(1)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(context: Context, navController: NavController) {
    val userPreferences = UserPreferences(context)
    val scope = rememberCoroutineScope()
    var userId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            val user = userPreferences.user.first()
            userId = user?.id
        }
    }

    if (userId == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Please log in to view notifications",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val viewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(RetrofitInstance.api)
    )
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(userId) {
        userId?.let { viewModel.fetchNotifications(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notifications",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Red,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = notifications, key = { it.id }) { notification ->
                        NotificationItem(
                            notification = notification,
                            onMarkAsRead = { viewModel.markAsRead(notification.id) },
                            onDelete = { viewModel.deleteNotification(notification.id) },
                            onClick = {
                                if (!notification.isRead) {
                                    viewModel.markAsRead(notification.id)
                                }
                                if (notification.convoId != null && notification.marketplaceItemId != null && notification.triggerUserId != null) {
                                    val itemTitle =
                                        parseItemTitleFromContent(notification.content) ?: "Chat"
                                    navController.navigate(
                                        NavRoutes.ChatSeller.createRoute(
                                            itemId = notification.marketplaceItemId!!,
                                            conversationId = notification.convoId!!,
                                            sellerId = notification.triggerUserId!!,
                                            itemTitle = itemTitle,
                                            itemImage = null
                                        )
                                    )
                                } else {
                                    notification.postId?.let { postId ->
                                        if (notification.repostId != null) {
                                            Log.d(
                                                "NotificationsScreen",
                                                "Navigating to Repost: postId=$postId, repostId=${notification.repostId}"
                                            )
                                            if (postId > 0 && notification.repostId!! > 0) {
                                                navController.navigate(
                                                    NavRoutes.Repost.createRoute(
                                                        postId,
                                                        notification.repostId!!
                                                    )
                                                )
                                            } else {
                                                Log.e(
                                                    "NotificationsScreen",
                                                    "Invalid IDs: postId=$postId, repostId=${notification.repostId}"
                                                )
                                            }
                                        } else {
                                            Log.d(
                                                "NotificationsScreen",
                                                "Navigating to Post: postId=$postId"
                                            )
                                            if (postId > 0) {
                                                navController.navigate(
                                                    NavRoutes.Post.createRoute(
                                                        postId
                                                    )
                                                )
                                            } else {
                                                Log.e(
                                                    "NotificationsScreen",
                                                    "Invalid postId: $postId"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val viewModel: NotificationClickedViewModel = viewModel()
    val repost by viewModel.repost.collectAsState()
    val originalPost by viewModel.originalPost.collectAsState()

    // Fetch repost and original post if this is a repost notification
    LaunchedEffect(notification.repostId, notification.postId) {
        if (notification.repostId != null && notification.postId != null) {
            viewModel.fetchPost(notification.postId, notification.repostId)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (notification.isAdmin) Color(0xFFFFF3E0)
                else if (notification.isRead) Color(0xFFF5F5F5)
                else Color(0xFFFFEAEA)
            )
            .then(
                if (!notification.isAdmin) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Picture
        if (notification.triggerProfilePicture != null && !notification.isAdmin) {
            Image(
                painter = rememberAsyncImagePainter(notification.triggerProfilePicture),
                contentDescription = "${notification.triggerUsername}'s Profile Picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
        } else {
            Image(
                painter = painterResource(id = if (notification.isAdmin) R.drawable.baseline_admin_panel_settings_24 else R.drawable.baseline_account_circle_24),
                contentDescription = if (notification.isAdmin) "Admin Notification" else "Default Profile Picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (notification.repostId != null && repost != null && originalPost != null) {
                // Repost-specific UI
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${repost!!.username} reposted",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    originalPost!!.profilePicture?.let { url ->
                        Image(
                            painter = rememberAsyncImagePainter(url),
                            contentDescription = "Original User Profile",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                    } ?: Image(
                        painter = painterResource(id = R.drawable.baseline_account_circle_24),
                        contentDescription = "Original User Profile",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = originalPost!!.username,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = originalPost!!.content.take(50) + if (originalPost!!.content.length > 50) "..." else "",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            } else {
                // Regular notification UI
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (notification.isAdmin) "Admin" else (notification.triggerUsername ?: "Unknown User"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (notification.isAdmin) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Admin Badge",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = notification.content,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatTimestamp(notification.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
            Row {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

internal fun formatTimestamp(date: Date): String {
    val diff = Date().time - date.time
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m"
        diff < 86_400_000 -> "${diff / 3_600_000}h"
        else -> "${diff / 86_400_000}d"
    }
}