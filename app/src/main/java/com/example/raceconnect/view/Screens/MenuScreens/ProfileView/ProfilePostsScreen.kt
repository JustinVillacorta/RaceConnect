package com.example.raceconnect.view.Screens.MenuScreens.ProfileView

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.raceconnect.R
import com.example.raceconnect.model.NewsFeedDataClassItem
import java.text.SimpleDateFormat
import java.util.*

// Utility function to format timestamp (unchanged)
fun formatTime(timestamp: String?): String {
    if (timestamp.isNullOrEmpty()) return "Just now"
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val date = try {
        sdf.parse(timestamp) ?: return "Just now"
    } catch (e: Exception) {
        Log.e("ProfilePostsScreen", "Error parsing date $timestamp: ${e.message}")
        return "Just now"
    }
    val now = Date()
    val diff = now.time - date.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        days > 0 -> "$days days ago"
        hours > 0 -> "$hours hours ago"
        minutes > 0 -> "$minutes minutes ago"
        else -> "Just now"
    }
}

@Composable
fun PostsSection(
    posts: LazyPagingItems<NewsFeedDataClassItem>?,
    postImages: Map<Int, List<String>>,
    onEditPost: (NewsFeedDataClassItem) -> Unit,
    onDeletePost: (NewsFeedDataClassItem) -> Unit,
    onFetchPostImages: (Int) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (posts != null) {
            items(posts.itemCount) { index ->
                posts[index]?.let { post ->
                    if (post.isRepost != true) {
                        LaunchedEffect(post.id) {
                            onFetchPostImages(post.id)
                        }
                        var showHiddenPost by remember(post.id, post.status) { mutableStateOf(false) }
                        var showConfirmationDialog by remember { mutableStateOf(false) }
                        var showDropdown by remember { mutableStateOf(false) }

                        if (post.status?.lowercase() == "archived") {
                            Log.w("PostsSection", "Archived post ID: ${post.id} reached UI")
                            return@let
                        }

                        Card(
                            shape = RectangleShape,
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                        ) {
                                            if (post.profile_picture != null) {
                                                AsyncImage(
                                                    model = post.profile_picture,
                                                    contentDescription = "User Profile",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .then(
                                                            if (post.status?.lowercase() == "hidden" && !showHiddenPost) {
                                                                Modifier.blur(10.dp)
                                                            } else {
                                                                Modifier
                                                            }
                                                        )
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.AccountCircle,
                                                    contentDescription = "User Profile",
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .then(
                                                            if (post.status?.lowercase() == "hidden" && !showHiddenPost) {
                                                                Modifier.blur(10.dp)
                                                            } else {
                                                                Modifier
                                                            }
                                                        ),
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = post.username ?: "Anonymous",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = if (post.status?.lowercase() == "hidden" && !showHiddenPost) {
                                                    Modifier.blur(10.dp)
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            Text(
                                                text = formatTime(post.created_at),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray,
                                                modifier = if (post.status?.lowercase() == "hidden" && !showHiddenPost) {
                                                    Modifier.blur(10.dp)
                                                } else {
                                                    Modifier
                                                }
                                            )
                                        }
                                        Box {
                                            IconButton(onClick = { showDropdown = true }) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "More Options",
                                                    tint = MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = showDropdown,
                                                onDismissRequest = { showDropdown = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Edit") },
                                                    onClick = {
                                                        onEditPost(post)
                                                        showDropdown = false
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Delete") },
                                                    onClick = {
                                                        onDeletePost(post)
                                                        showDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (post.status?.lowercase() == "hidden" && !showHiddenPost) {
                                        ExpandableText(
                                            text = post.content ?: "",
                                            enabled = false,
                                            modifier = Modifier.blur(10.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        if (postImages[post.id]?.isNotEmpty() == true) {
                                            if (postImages[post.id]!!.size == 1) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 8.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                ) {
                                                    val painter = rememberAsyncImagePainter(model = postImages[post.id]!!.first())
                                                    Image(
                                                        painter = painter,
                                                        contentDescription = "Post image",
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .aspectRatio(1.91f)
                                                            .blur(10.dp),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(340.dp)
                                                        .padding(top = 8.dp)
                                                ) {
                                                    val pagerState = rememberPagerState(pageCount = { postImages[post.id]!!.size })
                                                    HorizontalPager(
                                                        state = pagerState,
                                                        modifier = Modifier.fillMaxSize()
                                                    ) { page ->
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .clip(RoundedCornerShape(8.dp))
                                                        ) {
                                                            val painter = rememberAsyncImagePainter(model = postImages[post.id]!![page])
                                                            Image(
                                                                painter = painter,
                                                                contentDescription = "Post image $page",
                                                                modifier = Modifier
                                                                    .fillMaxSize()
                                                                    .blur(10.dp),
                                                                contentScale = ContentScale.Crop
                                                            )
                                                        }
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(8.dp)
                                                            .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "${pagerState.currentPage + 1}/${postImages[post.id]!!.size}",
                                                            color = Color.White,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            modifier = Modifier.blur(10.dp)
                                                        )
                                                    }
                                                    Row(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomCenter)
                                                            .padding(bottom = 8.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        postImages[post.id]!!.forEachIndexed { index, _ ->
                                                            val isSelected = index == pagerState.currentPage
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(8.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) Color.White else Color.Transparent)
                                                                    .then(
                                                                        if (!isSelected) Modifier.border(1.dp, Color.White, CircleShape)
                                                                        else Modifier
                                                                    )
                                                                    .blur(10.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        ExpandableText(
                                            text = post.content ?: "",
                                            enabled = true,
                                            modifier = Modifier
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        if (postImages[post.id]?.isNotEmpty() == true) {
                                            if (postImages[post.id]!!.size == 1) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 8.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                ) {
                                                    val painter = rememberAsyncImagePainter(model = postImages[post.id]!!.first())
                                                    Image(
                                                        painter = painter,
                                                        contentDescription = "Post image",
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .aspectRatio(1.91f),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(340.dp)
                                                        .padding(top = 8.dp)
                                                ) {
                                                    val pagerState = rememberPagerState(pageCount = { postImages[post.id]!!.size })
                                                    HorizontalPager(
                                                        state = pagerState,
                                                        modifier = Modifier.fillMaxSize()
                                                    ) { page ->
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .clip(RoundedCornerShape(8.dp))
                                                        ) {
                                                            val painter = rememberAsyncImagePainter(model = postImages[post.id]!![page])
                                                            Image(
                                                                painter = painter,
                                                                contentDescription = "Post image $page",
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentScale = ContentScale.Crop
                                                            )
                                                        }
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(8.dp)
                                                            .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "${pagerState.currentPage + 1}/${postImages[post.id]!!.size}",
                                                            color = Color.White,
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                    Row(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomCenter)
                                                            .padding(bottom = 8.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        postImages[post.id]!!.forEachIndexed { index, _ ->
                                                            val isSelected = index == pagerState.currentPage
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(8.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) Color.White else Color.Transparent)
                                                                    .then(
                                                                        if (!isSelected) Modifier.border(1.dp, Color.White, CircleShape)
                                                                        else Modifier
                                                                    )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (post.status?.lowercase() == "hidden") {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = { showHiddenPost = false },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                                            ) {
                                                Text("Hide Post", color = Color.Black)
                                            }
                                        }
                                    }
                                }

                                if (post.status?.lowercase() == "hidden" && !showHiddenPost) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "This post is hidden",
                                                color = Color.White,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(onClick = { showConfirmationDialog = true }) {
                                                Text("See Post")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (showConfirmationDialog) {
                            AlertDialog(
                                onDismissRequest = { showConfirmationDialog = false },
                                title = { Text("View Hidden Post") },
                                text = { Text("Are you sure you want to view this hidden post?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showHiddenPost = true
                                        showConfirmationDialog = false
                                    }) {
                                        Text("Yes")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showConfirmationDialog = false }) {
                                        Text("No")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableText(
    text: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var isTruncated by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = text,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            onTextLayout = { textLayoutResult ->
                if (!expanded && textLayoutResult.hasVisualOverflow) {
                    isTruncated = true
                }
            }
        )
        if (isTruncated) {
            val toggleText = if (expanded) "See Less" else "See More"
            Text(
                text = toggleText,
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .clickable(enabled = enabled) {
                        if (enabled) {
                            expanded = !expanded
                        }
                    }
                    .padding(top = 2.dp)
            )
        }
    }
}