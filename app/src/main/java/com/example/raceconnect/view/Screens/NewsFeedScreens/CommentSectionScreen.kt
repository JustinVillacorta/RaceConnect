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

    // Fetch userId and username from userPreferences.user Flow
    var userId by remember { mutableStateOf(0) }
    var username by remember { mutableStateOf("Unknown") }

    LaunchedEffect(Unit) {
        val user = userPreferences.user.first()
        if (user != null) {
            userId = user.id
            username = user.username
        } else {
            // Handle case where user is not logged in
            // For now, use defaults; ideally, redirect to login screen
            userId = 0
            username = "Guest"
        }
    }

    LaunchedEffect(postId) {
        viewModel.fetchComments(postId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (viewModel.isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))
        }

        viewModel.errorMessage.value?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
        } ?: run {
            if (viewModel.comments.isEmpty() && !viewModel.isLoading.value) {
                Text(
                    text = "No comments available.",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(viewModel.comments) { comment ->
                CommentItem(
                    comment = comment,
                    navController = navController,
                    onShowProfileView = onShowProfileView
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Add a comment...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                if (commentText.isNotEmpty()) {
                    if (userId == 0) {
                        // Optionally redirect to login screen if user is not logged in
                        // For now, show a message
                        viewModel.errorMessage.value = "Please log in to comment."
                        return@IconButton
                    }
                    val newComment = PostComment(
                        userId = userId, // Use dynamic userId
                        postId = postId,
                        comment = commentText,
                        createdAt = Date(),
                        username = username // Use dynamic username
                    )
                    viewModel.addComment(newComment)
                    commentText = ""
                }
            }) {
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Picture Placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .clickable { onShowProfileView() }
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.username ?: "User ${comment.userId}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(
                text = comment.comment ?: comment.text ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Optional: Display likes if you extend the backend to support it
        if (comment.likes > 0) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = comment.icon ?: Icons.Default.Favorite,
                    contentDescription = "Reaction Icon",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Gray
                )
                Text(
                    text = comment.likes.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}