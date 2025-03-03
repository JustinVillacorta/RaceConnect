package com.example.raceconnect.view.Screens.NewsFeedScreens

import android.util.Log
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel

@Composable
fun PostCard(
    post: NewsFeedDataClassItem,
    navController: NavController,
    onCommentClick: () -> Unit,
    onLikeClick: (Boolean) -> Unit,
    viewModel: NewsFeedViewModel
) {
    Log.d("PostCard", "📝 Rendering post with ID: ${post.id}, Content: ${post.content}")

    var isLiked by remember { mutableStateOf(post.isLiked) }
    var likeCount by remember { mutableStateOf(post.like_count) }

    // Get images for this post
    val postImagesMap by viewModel.postImages.collectAsState()
    val imageUrls = postImagesMap[post.id] ?: emptyList()

    // State to control full-screen image viewer
    var showFullScreenImage by remember { mutableStateOf<String?>(null) }

    // Fetch images for this post
    LaunchedEffect(post.id) {
        Log.d("PostCard", "🔄 Fetching images for post ID: ${post.id}")
        viewModel.getPostImages(post.id)
    }

    Card(
        shape = RoundedCornerShape(12.dp), // Rounded corners like Facebook
        elevation = CardDefaults.cardElevation(2.dp), // Subtle elevation
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp) // Match Facebook margins
            .background(Color.White) // White background like Facebook
    ) {
        Column(modifier = Modifier.padding(16.dp)) { // Consistent padding inside card
            // Profile Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp) // Circular profile avatar size
                        .clip(CircleShape)
                        .background(Color.Gray) // Placeholder color
                        .clickable {
                            navController.navigate("profileView")
                            Log.d("PostCard", "📌 Navigating to profile")
                        }
                )
                Spacer(modifier = Modifier.width(12.dp)) // More spacing like Facebook
                Column(modifier = Modifier.weight(1f)) { // Allow text to expand
                    Text(
                        text = "Anonymous",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Just now",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray // Gray timestamp like Facebook
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // Increased spacing

            // Post Content
            Text(
                text = post.content ?: "No content available.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp)) // Spacing before image

            // Display Image (single image, dynamically sized, clickable for full screen)
            if (imageUrls.isNotEmpty()) {
                Log.d("PostCard", "🖼️ Displaying Image: ${imageUrls.first()}")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp)) // Rounded corners for image
                        .clickable { showFullScreenImage = imageUrls.first() } // Click to show full screen
                ) {
                    AsyncImage(
                        model = imageUrls.first(), // Use the first image
                        contentDescription = "Post image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.91f), // Default to Facebook's 1.91:1 (horizontal), can be dynamic
                        contentScale = ContentScale.Crop // Crop to fill while maintaining aspect ratio
                    )
                }
            } else {
                Log.d("PostCard", "🚫 No images found for post ID: ${post.id}")
            }

            Spacer(modifier = Modifier.height(12.dp)) // Spacing before reactions

            // Reaction Bar
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
                        Log.d("PostCard", if (isLiked) "❤️ Post liked" else "💔 Post unliked")
                    }
                )
                Text(
                    text = "$likeCount", // Show count directly
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp, end = 12.dp) // More spacing
                )
                ReactionIcon(icon = Icons.Default.ChatBubble, onClick = { onCommentClick(); Log.d("PostCard", "💬 Comment button clicked") })
                Spacer(modifier = Modifier.width(8.dp)) // Reduced spacing
                ReactionIcon(icon = Icons.Default.Repeat)
            }
        }
    }

    // Show full-screen image viewer if triggered
    showFullScreenImage?.let { imageUrl ->
        FullScreenImageViewer(
            imageUrl = imageUrl,
            onDismiss = { showFullScreenImage = null },
            onLikeClick = { isLiked -> onLikeClick(isLiked) }, // Pass like callback
            onCommentClick = onCommentClick // Pass comment callback
        )
    }
}

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
    var isLiked by remember { mutableStateOf(false) } // Track like state for the full-screen viewer
    val velocityTracker = remember { VelocityTracker() }
    var isDragging by remember { mutableStateOf(false) }

    // Animate scale for double-tap zoom
    val animatedScale by animateFloatAsState(
        targetValue = if (isAnimating) if (scale == 1f) 3f else 1f else scale,
        animationSpec = tween(durationMillis = 200),
        finishedListener = { isAnimating = false }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Solid black background like Facebook
            .pointerInput(Unit) {

                detectTapGestures(
                    onDoubleTap = { cent -> // Double-tap to zoom in/out
                        isAnimating = true
                        scale = if (scale == 1f) 3f else 1f
                        offset = Offset.Zero // Reset offset on double-tap
                    },
                    onPress = { offset ->
                        velocityTracker.resetTracking()
                        isDragging = false
                    }
                )

                detectTransformGestures { centroid, pan, zoom, rotation ->
                    velocityTracker.addPosition(System.currentTimeMillis(), centroid)
                    isDragging = true
                    if (!isAnimating) {
                        scale = (scale * zoom).coerceIn(0.5f, 5f) // Limit zoom
                        offset += pan
                        // Limit panning to prevent image from moving off-screen
                        val maxOffset = (size.width * (scale - 1)) / 2
                        offset = Offset(
                            offset.x.coerceIn(-maxOffset, maxOffset),
                            offset.y.coerceIn(-maxOffset, maxOffset)
                        )
                    }
                }

                // Handle gesture end for swipe-down dismiss
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        if (scale == 1f && !isAnimating) { // Only allow dismiss when not zoomed
                            offset += dragAmount
                            velocityTracker.addPosition(System.currentTimeMillis(), change.position)
                        }
                    },
                    onDragEnd = {
                        val velocity = velocityTracker.calculateVelocity()
                        if (isDragging && velocity.y > 1000f && scale == 1f) { // Swipe down to dismiss
                            onDismiss()
                        }
                        velocityTracker.resetTracking()
                        isDragging = false
                        offset = Offset.Zero // Reset offset after dismiss
                    }
                )
            }
    ) {
        // Full-screen image with zoom and pan, filling the screen
        AsyncImage(
            model = imageUrl,
            contentDescription = "Full-screen image",
            modifier = Modifier
                .fillMaxSize() // Fill the entire screen
                .graphicsLayer(
                    scaleX = animatedScale,
                    scaleY = animatedScale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Crop // Crop to fill the screen while maintaining aspect ratio
        )

        // Close button (top-right, like the screenshot)
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(24.dp)
                .clickable { onDismiss() }
                .clip(CircleShape) // Circular shape for consistency with Facebook
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(4.dp),
            tint = Color.White
        )

        // Action buttons (bottom, like the screenshot)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .background(Color.Black, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Like Button
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
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Like",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Comments Button
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
                    Spacer(modifier = Modifier.width(4.dp))
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

@Composable
fun ReactionIcon(icon: ImageVector, isLiked: Boolean = false, onClick: (() -> Unit)? = null) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
            .size(24.dp) // Larger icons like Facebook
            .clickable { onClick?.invoke() },
        tint = if (isLiked) Color.Red else Color.Gray
    )
}