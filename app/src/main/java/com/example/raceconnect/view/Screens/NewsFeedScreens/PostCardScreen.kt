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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import kotlin.String

@Composable
fun PostCard(
    post: NewsFeedDataClassItem,
    navController: NavController,
    onCommentClick: () -> Unit,
    onLikeClick: (Boolean) -> Unit,
    viewModel: NewsFeedViewModel,
    onShowFullScreenImage: (String) -> Unit,
    onShowProfileView: () -> Unit,
    onReportClick: () -> Unit,
    onShowRepostScreen: (NewsFeedDataClassItem) -> Unit // Add callback for repost screen
) {
    var isLiked by remember { mutableStateOf(post.isLiked) }
    var likeCount by remember { mutableStateOf(post.like_count) }
    val postImagesMap by viewModel.postImages.collectAsState()
    val imageUrls = postImagesMap[post.id] ?: emptyList()
    var showReportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(post.id) {
        viewModel.getPostImages(post.id)
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
                            .clickable { onShowProfileView() }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Anonymous",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Just now",
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
                        onClick = { onShowRepostScreen(post) } // Navigate to repost screen
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Report,
                contentDescription = "Report post",
                modifier = Modifier
                    .size(34.dp)
                    .clickable { showReportDialog = true }
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                tint = Color.Gray
            )
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(text = "Report Post") },
            text = { Text(text = "Are you sure you want to report this post?") },
            confirmButton = {
                Text(
                    text = "Confirm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable {
                            Toast.makeText(context, "Post reported!", Toast.LENGTH_SHORT).show()
                            onReportClick()
                            showReportDialog = false
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit,
    onLikeClick: (Boolean) -> Unit,
    onCommentClick: () -> Unit
) {
    // Existing code (unchanged)
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

@Preview(showBackground = true)
@Composable
fun PreviewPostCard() {
    val dummyPost = NewsFeedDataClassItem(
        id = 1,
        user_id = 1,
        content = "This is a sample post for preview purposes.",
        isLiked = false,
        like_count = 10,
        img_url = "https://example.com/image.jpg",
        title = "Sample Post"
    )

    PostCard(
        post = dummyPost,
        navController = NavController(LocalContext.current),
        onCommentClick = {},
        onLikeClick = {},
        viewModel = NewsFeedViewModel(UserPreferences(LocalContext.current)),
        onShowFullScreenImage = {},
        onShowProfileView = {},
        onReportClick = {},
        onShowRepostScreen = {} // Add the callback for preview
    )
}