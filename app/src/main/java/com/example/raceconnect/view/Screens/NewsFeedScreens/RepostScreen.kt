package com.example.raceconnect.view.Screens.NewsFeedScreens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepostScreen(
    post: NewsFeedDataClassItem,
    navController: NavController,
    viewModel: NewsFeedViewModel,
    onClose: () -> Unit,
    userPreferences: UserPreferences,
    onShowPostCardProfile: (Int) -> Unit // Add this parameter
) {
    val context = LocalContext.current
    var repostComment by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Repost", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { onClose() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Red
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // TextField with Outline
                BasicTextField(
                    value = repostComment,
                    onValueChange = { repostComment = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (repostComment.isEmpty()) {
                                Text(
                                    text = "Add a comment to your repost (optional)",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable PostCard
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    PostCard(
                        post = post,
                        navController = navController,
                        onCommentClick = { /* Disabled */ },
                        onLikeClick = { _ -> /* Disabled */ },
                        viewModel = viewModel,
                        onShowFullScreenImage = { /* Disabled */ },
                        userPreferences = userPreferences,
                        onReportClick = { postId, reason, otherText ->
                            viewModel.reportPost(postId, reason, otherText, onSuccess = {
                                Log.d("RepostScreen", "Post reported successfully")
                            }, onFailure = { error ->
                                Log.e("RepostScreen", "Failed to report post: $error")
                            })
                        },
                        onShowRepostScreen = { /* Disabled */ },
                        onUserActionClick = { userId, action, otherText ->
                            when (action) {
                                "Report User" -> viewModel.reportUser(userId, action, otherText)
                                else -> Log.d("RepostScreen", "Unhandled action: $action")
                            }
                        },
                        onShowPostCardProfile = onShowPostCardProfile // Pass the callback
                    )
                }
            }

            // Repost Button
            Button(
                onClick = {
                    viewModel.repostPost(post.id, repostComment)
                    Toast.makeText(context, "Reposted successfully!", Toast.LENGTH_SHORT).show()
                    onClose()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Repost", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun RepostCard(
    repost: NewsFeedDataClassItem,
    originalPost: NewsFeedDataClassItem?,
    navController: NavController,
    onCommentClick: () -> Unit,
    onLikeClick: (Boolean) -> Unit,
    viewModel: NewsFeedViewModel,
    onShowFullScreenImage: (String) -> Unit,
    userPreferences: UserPreferences,
    onReportClick: (Int, String, String?) -> Unit,
    onShowRepostScreen: (NewsFeedDataClassItem) -> Unit,
    onUserActionClick: (Int, String, String?) -> Unit,
    onShowRepostCardProfile: (Int) -> Unit // New callback
) {
    val user by userPreferences.user.collectAsState(initial = null)
    val context = LocalContext.current
    var showReportDialog by remember { mutableStateOf(false) }
    var showUserDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
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
                                onShowRepostCardProfile(repost.user_id) // Use overlay callback
                            }
                    ) {
                        AsyncImage(
                            model = "https://via.placeholder.com/40",
                            contentDescription = "Reposter Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Repost Icon",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${repost.username ?: "Anonymous"} reposted",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = formatTime(repost.created_at),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!repost.content.isNullOrEmpty()) {
                    Text(
                        text = repost.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        modifier = Modifier
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                originalPost?.let { original ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                    ) {
                        PostCard(
                            post = original,
                            navController = navController,
                            onCommentClick = onCommentClick,
                            onLikeClick = onLikeClick,
                            viewModel = viewModel,
                            onShowFullScreenImage = onShowFullScreenImage,
                            userPreferences = userPreferences,
                            onReportClick = onReportClick,
                            onShowRepostScreen = onShowRepostScreen,
                            onUserActionClick = onUserActionClick,
                            onShowPostCardProfile = onShowRepostCardProfile // Pass same callback for nested PostCard
                        )
                    }
                } ?: run {
                    Text(
                        text = "Original post unavailable",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(8.dp)
                    )
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
                        isLiked = repost.isLiked,
                        onClick = { onLikeClick(!repost.isLiked) }
                    )
                    Text(
                        text = "${repost.like_count}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                    )
                    ReactionIcon(icon = Icons.Default.ChatBubble, onClick = onCommentClick)
                    Spacer(modifier = Modifier.width(8.dp))
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
                        text = { Text("Report Repost") },
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
        var selectedOption by remember { mutableStateOf("") }
        var otherText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(text = "Report Repost") },
            text = {
                Column {
                    Text(text = "Please select a reason for reporting this repost:")
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
                    color = if (selectedOption.isNotEmpty() && (selectedOption != "Others" || otherText.isNotEmpty()))
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable {
                            if (selectedOption.isNotEmpty()) {
                                if (selectedOption == "Others" && otherText.isEmpty()) {
                                    Toast.makeText(context, "Please specify the reason", Toast.LENGTH_SHORT).show()
                                } else {
                                    onReportClick(repost.id, selectedOption, if (selectedOption == "Others") otherText else null)
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
        var selectedOption by remember { mutableStateOf("") }
        var otherText by remember { mutableStateOf("") }

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
                    color = if (selectedOption.isNotEmpty() && (selectedOption != "Others" || otherText.isNotEmpty()))
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable {
                            if (selectedOption.isNotEmpty()) {
                                if (selectedOption == "Others" && otherText.isEmpty()) {
                                    Toast.makeText(context, "Please specify the action", Toast.LENGTH_SHORT).show()
                                } else {
                                    onUserActionClick(repost.user_id, selectedOption, if (selectedOption == "Others") otherText else null)
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