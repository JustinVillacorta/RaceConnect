package com.example.raceconnect.view.Screens.NewsFeedScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.PostComment
import com.example.raceconnect.viewmodel.CommentViewModel
import com.example.raceconnect.viewmodel.CommentViewModelFactory
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CommentSectionScreen(
    postId: Int,
    navController: NavController,
    userPreferences: UserPreferences,
    onShowProfileView: () -> Unit
) {
    val viewModel: CommentViewModel = viewModel(factory = CommentViewModelFactory(userPreferences))
    var commentText by remember { mutableStateOf("") }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    var userId by remember { mutableStateOf(0) }
    var username by remember { mutableStateOf("Unknown") }

    LaunchedEffect(Unit) {
        val user = userPreferences.user.first()
        if (user != null) {
            userId = user.id
            username = user.username
        } else {
            userId = 0
            username = "Guest"
        }
    }

    LaunchedEffect(postId) {
        viewModel.fetchComments(postId)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            CommentInput(
                commentText = commentText,
                onCommentChange = { commentText = it },
                onSendClick = {
                    if (commentText.isNotEmpty()) {
                        if (userId == 0) {
                            viewModel.errorMessage.value = "Please log in to comment."
                            return@CommentInput
                        }
                        val newComment = PostComment(
                            userId = userId,
                            postId = postId,
                            comment = commentText,
                            createdAt = Date(),
                            username = username
                        )
                        viewModel.addComment(newComment)
                        commentText = ""
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when {
                    viewModel.isLoading.value -> {
                        CircularProgressIndicator()
                    }
                    viewModel.errorMessage.value != null -> {
                        Text(
                            text = viewModel.errorMessage.value!!,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    viewModel.comments.isEmpty() -> {
                        Text(
                            text = "No comments yet. Be the first to comment!",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(viewModel.comments) { comment ->
                                CommentItem(
                                    comment = comment,
                                    navController = navController,
                                    onShowProfileView = onShowProfileView
                                )
                                Divider(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    thickness = 0.5.dp,
                                    color = Color.Gray.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentInput(
    commentText: String,
    onCommentChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = onCommentChange,
                placeholder = { Text("Add a comment...") },
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                singleLine = true
            )
            IconButton(
                onClick = onSendClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: PostComment,
    navController: NavController,
    onShowProfileView: () -> Unit
) {
    val timestamp = comment.createdAt?.let {
        val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        formatter.format(it)
    } ?: "Just now"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .clickable { onShowProfileView() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.username?.firstOrNull()?.toString() ?: "?",
                color = Color.White,
                fontSize = 16.sp
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentHeight()
            ) {
                Text(
                    text = comment.username ?: "User ${comment.userId}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = " • $timestamp",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 6.dp),
                    maxLines = 1
                )
            }
            Text(
                text = comment.comment ?: comment.text ?: "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (comment.likes > 0) {
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = comment.icon ?: Icons.Default.Favorite,
                    contentDescription = "Likes",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Gray
                )
                Text(
                    text = " ${comment.likes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}