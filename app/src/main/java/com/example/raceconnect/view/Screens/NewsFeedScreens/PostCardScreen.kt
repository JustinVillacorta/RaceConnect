package com.example.raceconnect.view.Screens.NewsFeedScreens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import java.text.SimpleDateFormat
import java.util.*

// Utility function to format time relative to now
fun formatTime(createdAt: String?): String {
    if (createdAt.isNullOrEmpty()) return "Just now"

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val date = try {
        sdf.parse(createdAt)
    } catch (e: Exception) {
        Log.e("PostCard", "Error parsing date $createdAt: ${e.message}")
        return createdAt ?: "Just now"
    } ?: return "Just now"

    val now = Calendar.getInstance()
    val created = Calendar.getInstance().apply { time = date }
    val diffInMillis = now.timeInMillis - created.timeInMillis
    val diffInSeconds = diffInMillis / 1000
    val diffInMinutes = diffInSeconds / 60
    val diffInHours = diffInMinutes / 60
    val diffInDays = diffInHours / 24

    return when {
        diffInMinutes < 1 -> "Just now"
        diffInMinutes < 60 -> "${diffInMinutes} minute${if (diffInMinutes > 1) "s" else ""} ago"
        diffInHours < 24 -> "${diffInHours} hour${if (diffInHours > 1) "s" else ""} ago"
        diffInDays < 7 -> "${diffInDays} day${if (diffInDays > 1) "s" else ""} ago"
        else -> sdf.format(date) // Fall back to full date for older posts
    }
}

@Composable
fun PostCard(
    post: NewsFeedDataClassItem,
    navController: NavController,
    onCommentClick: () -> Unit,
    onLikeClick: (Boolean) -> Unit,
    viewModel: NewsFeedViewModel,
    onShowFullScreenImage: (String) -> Unit,
    userPreferences: UserPreferences,
    onReportClick: (Int, String, String?) -> Unit,
    onShowRepostScreen: (NewsFeedDataClassItem) -> Unit,
    onUserActionClick: (Int, String, String?) -> Unit,
    onShowPostCardProfile: (Int) -> Unit, // New callback
    context: Context = LocalContext.current
) {
    var isLiked by remember { mutableStateOf(post.isLiked) }
    var likeCount by remember { mutableStateOf(post.like_count) }
    val postImagesMap by viewModel.postImages.collectAsState()
    val imageUrls = postImagesMap[post.id] ?: post.images ?: emptyList()
    var showReportDialog by remember { mutableStateOf(false) }
    var showUserDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("") }
    var otherText by remember { mutableStateOf("") }
    val user by userPreferences.user.collectAsState(initial = null)

    LaunchedEffect(post.id) {
        viewModel.getPostImages(post.id)
        Log.d("PostCard", "Post ID: ${post.id}, Image URLs: $imageUrls")
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .background(Color.White)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .clickable {
                                onShowPostCardProfile(post.user_id) // Use overlay callback
                            }
                    ) {
                        AsyncImage(
                            model = "https://via.placeholder.com/40",
                            contentDescription = "User Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = post.username ?: "Anonymous",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatTime(post.created_at),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = post.content ?: "No content available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (imageUrls.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                Log.d("PostCard", "Image clicked, URL: ${imageUrls.first()}")
                                onShowFullScreenImage(imageUrls.first())
                            }
                    ) {
                        AsyncImage(
                            model = imageUrls.first(),
                            contentDescription = "Post image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.91f),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReactionIcon(
                        icon = Icons.Default.Favorite,
                        isLiked = isLiked,
                        onClick = {
                            isLiked = !isLiked
                            likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                            onLikeClick(isLiked)
                        }
                    )
                    Text(
                        text = "$likeCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                    )
                    ReactionIcon(icon = Icons.Default.ChatBubble, onClick = onCommentClick)
                    Spacer(modifier = Modifier.width(8.dp))
                    ReactionIcon(
                        icon = Icons.Default.Repeat,
                        onClick = { onShowRepostScreen(post) }
                    )
                }
            }

            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { menuExpanded = true }
                        .padding(8.dp),
                    tint = Color.Gray
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Report Post") },
                        onClick = {
                            menuExpanded = false
                            showReportDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("User Actions") },
                        onClick = {
                            menuExpanded = false
                            showUserDialog = true
                        }
                    )
                }
            }
        }
    }

    // Report Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(text = "Report Post") },
            text = {
                Column {
                    Text(text = "Please select a reason for reporting this post:")
                    Spacer(modifier = Modifier.height(16.dp))

                    val reportOptions = listOf("Not related", "Nudity", "Inappropriate", "Others")
                    reportOptions.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedReason = reason },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason }
                            )
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    if (selectedReason == "Others") {
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = otherText,
                            onValueChange = { otherText = it },
                            label = { Text("Please specify") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Text(
                    text = "Confirm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedReason.isNotEmpty() && (selectedReason != "Others" || otherText.isNotEmpty()))
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable {
                            if (selectedReason.isNotEmpty()) {
                                if (selectedReason == "Others" && otherText.isEmpty()) {
                                    Toast.makeText(context, "Please specify the reason", Toast.LENGTH_SHORT).show()
                                } else {
                                    onReportClick(post.id, selectedReason, if (selectedReason == "Others") otherText else null)
                                    showReportDialog = false
                                }
                            }
                        }
                        .padding(8.dp)
                )
            },
            dismissButton = {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clickable { showReportDialog = false }
                        .padding(8.dp)
                )
            }
        )
    }

    // User Action Dialog
    if (showUserDialog) {
        AlertDialog(
            onDismissRequest = { showUserDialog = false },
            title = { Text("User Actions") },
            text = {
                Column {
                    Text("Please select an action for this user:")
                    Spacer(modifier = Modifier.height(16.dp))
                    val userOptions = listOf("Report User", "Others")
                    userOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedReason = option },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedReason == option,
                                onClick = { selectedReason = option }
                            )
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    if (selectedReason == "Others") {
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = otherText,
                            onValueChange = { otherText = it },
                            label = { Text("Please specify the action") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Text(
                    text = "Confirm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedReason.isNotEmpty() && (selectedReason != "Others" || otherText.isNotEmpty()))
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable {
                            if (selectedReason.isNotEmpty()) {
                                if (selectedReason == "Others" && otherText.isEmpty()) {
                                    Toast.makeText(context, "Please specify the action", Toast.LENGTH_SHORT).show()
                                } else {
                                    onUserActionClick(post.user_id, selectedReason, if (selectedReason == "Others") otherText else null)
                                    showUserDialog = false
                                }
                            }
                        }
                        .padding(8.dp)
                )
            },
            dismissButton = {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clickable { showUserDialog = false }
                        .padding(8.dp)
                )
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit,
    onLikeClick: (Boolean) -> Unit,
    onCommentClick: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isLiked by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            scale = if (scale <= 1f) 2.5f else 1f
                            offset = Offset.Zero
                        }
                    )
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 5f)
                        offset += pan
                        val maxOffsetX = (size.width * (scale - 1)) / 2
                        val maxOffsetY = (size.height * (scale - 1)) / 2
                        offset = Offset(
                            offset.x.coerceIn(-maxOffsetX, maxOffsetX),
                            offset.y.coerceIn(-maxOffsetY, maxOffsetY)
                        )
                    }
                    detectDragGestures(
                        onDrag = { _, dragAmount ->
                            if (scale == 1f) offset += dragAmount
                        },
                        onDragEnd = {
                            if (offset.y > 200f && scale == 1f) onDismiss()
                            else if (scale == 1f) offset = Offset.Zero
                        }
                    )
                }
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Full-screen image",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Fit
            )

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(6.dp)
                    .clickable { onDismiss() },
                tint = Color.White
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .background(Color.Black.copy(alpha = 0.8f), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                isLiked = !isLiked
                                onLikeClick(isLiked)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Like",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onCommentClick() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Comments",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Comments",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReactionIcon(icon: ImageVector, isLiked: Boolean = false, onClick: (() -> Unit)? = null) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
            .size(24.dp)
            .clickable { onClick?.invoke() },
        tint = if (isLiked) Color.Red else Color.Gray
    )
}