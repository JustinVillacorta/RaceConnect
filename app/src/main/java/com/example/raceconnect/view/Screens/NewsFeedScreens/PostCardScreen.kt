package com.example.raceconnect.view.Screens.NewsFeedScreens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel

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
    onUserActionClick: (Int, String, String?) -> Unit
) {
    var isLiked by remember { mutableStateOf(post.isLiked) }
    var likeCount by remember { mutableStateOf(post.like_count) }
    val postImagesMap by viewModel.postImages.collectAsState()
    val imageUrls = postImagesMap[post.id] ?: post.images ?: emptyList()
    var showReportDialog by remember { mutableStateOf(false) }
    var showUserDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val user by userPreferences.user.collectAsState(initial = null)
    val loggedInUserId = user?.id

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
                                val destination = if (loggedInUserId != null && loggedInUserId == post.user_id) {
                                    NavRoutes.ProfileView.createRoute(loggedInUserId)
                                } else {
                                    NavRoutes.ProfileView.createRoute(post.user_id)
                                }
                                Log.d("PostCard", "Navigating to $destination")
                                navController.navigate(destination)
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
                            text = post.created_at ?: "Just now",
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
                            .clickable { onShowFullScreenImage(imageUrls.first()) }
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

    if (showReportDialog) {
        var selectedOption by remember { mutableStateOf("") }
        var otherText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(text = "Report Post") },
            text = {
                Column {
                    Text(text = "Please select a reason for reporting this post:")
                    Spacer(modifier = Modifier.height(16.dp))

                    val reportOptions = listOf("Not related", "Nudity", "Inappropriate", "Others")
                    reportOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedOption = option },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = { selectedOption = option }
                            )
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    if (selectedOption == "Others") {
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = otherText,
                            onValueChange = { otherText = it },
                            label = { Text("Please specify the reason") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Text(
                    text = "Confirm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedOption.isNotEmpty() &&
                        (selectedOption != "Others" || otherText.isNotEmpty()))
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable {
                            if (selectedOption.isNotEmpty()) {
                                if (selectedOption == "Others" && otherText.isEmpty()) {
                                    Toast.makeText(context,
                                        "Please specify the reason",
                                        Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context,
                                        "Post reported!",
                                        Toast.LENGTH_SHORT).show()
                                    onReportClick(post.id, selectedOption,
                                        if (selectedOption == "Others") otherText else null)
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
            },
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showUserDialog) {
        var selectedOption by remember { mutableStateOf("") }
        var otherText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showUserDialog = false },
            title = { Text(text = "User Actions") },
            text = {
                Column {
                    Text(text = "Please select an action for this user:")
                    Spacer(modifier = Modifier.height(16.dp))

                    // Removed "Block User" and "Mute User", kept "Report User" and "Others"
                    val userOptions = listOf("Report User", "Others")
                    userOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedOption = option },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = { selectedOption = option }
                            )
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    if (selectedOption == "Others") {
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
                    color = if (selectedOption.isNotEmpty() &&
                        (selectedOption != "Others" || otherText.isNotEmpty()))
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable {
                            if (selectedOption.isNotEmpty()) {
                                if (selectedOption == "Others" && otherText.isEmpty()) {
                                    Toast.makeText(context,
                                        "Please specify the action",
                                        Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context,
                                        "User action performed: $selectedOption",
                                        Toast.LENGTH_SHORT).show()
                                    onUserActionClick(post.user_id, selectedOption,
                                        if (selectedOption == "Others") otherText else null)
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
            },
            shape = RoundedCornerShape(12.dp)
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
    var isAnimating by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }
    val velocityTracker = remember { VelocityTracker() }
    var isDragging by remember { mutableStateOf(false) }

    val animatedScale by animateFloatAsState(
        targetValue = if (isAnimating) if (scale == 1f) 2.5f else 1f else scale,
        animationSpec = tween(durationMillis = 200),
        finishedListener = { isAnimating = false }
    )

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
                        onDoubleTap = { centroid ->
                            isAnimating = true
                            scale = if (scale <= 1f) 2.5f else 1f
                            offset = Offset.Zero
                        },
                        onPress = {
                            velocityTracker.resetTracking()
                            isDragging = false
                        }
                    )

                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        velocityTracker.addPosition(System.currentTimeMillis(), centroid)
                        isDragging = true
                        if (!isAnimating) {
                            scale = (scale * zoom).coerceIn(0.5f, 5f)
                            offset += pan
                            val maxOffsetX = (size.width * (scale - 1)) / 2
                            val maxOffsetY = (size.height * (scale - 1)) / 2
                            offset = Offset(
                                offset.x.coerceIn(-maxOffsetX, maxOffsetX),
                                offset.y.coerceIn(-maxOffsetY, maxOffsetY)
                            )
                        }
                    }

                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            if (scale == 1f && !isAnimating) {
                                offset += dragAmount
                                velocityTracker.addPosition(System.currentTimeMillis(), change.position)
                            }
                        },
                        onDragEnd = {
                            val velocity = velocityTracker.calculateVelocity()
                            if (isDragging && velocity.y > 1000f && scale == 1f) {
                                onDismiss()
                            } else {
                                if (scale == 1f) offset = Offset.Zero
                            }
                            velocityTracker.resetTracking()
                            isDragging = false
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
                        scaleX = animatedScale,
                        scaleY = animatedScale,
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