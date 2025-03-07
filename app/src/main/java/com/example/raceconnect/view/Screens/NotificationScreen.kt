package com.example.raceconnect.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.raceconnect.R
import com.example.raceconnect.model.Notification
import com.example.raceconnect.network.RetrofitInstance
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.NotificationViewModel
import com.example.raceconnect.viewmodel.NotificationViewModelFactory
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(userId: Int = 1) {
    val viewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(RetrofitInstance.api)
    )
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchNotifications(userId)
        Log.d("NotificationsScreen", "Initial notifications: $notifications")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White, style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    IconButton(onClick = { /* Handle search */ }) {
                        Icon(painterResource(id = R.drawable.baseline_search_24), contentDescription = "Search", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Red, titleContentColor = Color.White)
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(text = error ?: "Unknown error", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
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
                        Log.d("NotificationsScreen", "Rendering notification: $notification")
                        NotificationItem(
                            notification = notification,
                            onMarkAsRead = { viewModel.markAsRead(notification.id) },
                            onDelete = { viewModel.deleteNotification(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

// Rest of the file (NotificationItem and formatTimestamp) remains the same
@Composable
fun NotificationItem(
    notification: Notification,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (notification.isRead) Color(0xFFF5F5F5) else Color(0xFFFFEAEA))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.baseline_account_circle_24),
            contentDescription = "Profile Picture",
            modifier = Modifier.size(48.dp).clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "User ${notification.userId}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = notification.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Type: ${notification.type}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatTimestamp(notification.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
            if (!notification.isRead) {
                TextButton(onClick = onMarkAsRead) {
                    Text("Mark as Read")
                }
            }
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        }
    }
}

private fun formatTimestamp(date: Date): String {
    val diff = Date().time - date.time
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m"
        diff < 86_400_000 -> "${diff / 3_600_000}h"
        else -> "${diff / 86_400_000}d"
    }
}