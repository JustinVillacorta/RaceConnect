package com.example.raceconnect.view.Screens.NewsFeedScreens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import com.example.raceconnect.model.Reply
import com.example.raceconnect.viewmodel.CommentViewModel
import com.example.raceconnect.viewmodel.CommentViewModelFactory
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("RememberReturnType")
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
    var selectedCommentId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        Log.d("CommentSectionScreen", "Fetching user data")
        val user = userPreferences.user.first()
        if (user != null) {
            userId = user.id
            username = user.username
            Log.d("CommentSectionScreen", "User loaded: $userId, $username")
        } else {
            userId = 0
            username = "Guest"
            Log.w("CommentSectionScreen", "No user found, using Guest")
        }
    }

    LaunchedEffect(postId) {
        Log.d("CommentSectionScreen", "Fetching comments for post $postId")
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
                            Log.w("CommentSectionScreen", "User not logged in, cannot comment")
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
                        Log.d("CommentSectionScreen", "Adding new comment: $newComment")
                        viewModel.addComment(newComment)
                        commentText = ""
                    } else {
                        Log.w("CommentSectionScreen", "Comment text is empty, ignoring send action")
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
                    .weight(1f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { selectedCommentId = null },
                contentAlignment = Alignment.Center
            ) {
                when {
                    viewModel.isLoading.value -> {
                        Log.d("CommentSectionScreen", "Loading comments...")
                        CircularProgressIndicator()
                    }
                    viewModel.errorMessage.value != null -> {
                        Log.e("CommentSectionScreen", "Error: ${viewModel.errorMessage.value}")
                        Text(
                            text = viewModel.errorMessage.value ?: "Unknown error",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    viewModel.comments.isEmpty() -> {
                        Log.d("CommentSectionScreen", "No comments available")
                        Text(
                            text = "No comments yet. Be the first to comment!",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    else -> {
                        Log.d("CommentSectionScreen", "Displaying ${viewModel.comments.size} comments")
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(
                                items = viewModel.comments,
                                key = { comment -> comment.id ?: Random().nextInt() } // Fallback key if id is null
                            ) { comment ->
                                CommentItem(
                                    comment = comment,
                                    currentUserId = userId,
                                    selectedCommentId = selectedCommentId,
                                    onCommentSelected = { commentId -> selectedCommentId = commentId },
                                    onDeleteComment = { commentId -> viewModel.deleteComment(commentId) },
                                    onUpdateComment = { commentId, newText -> viewModel.updateComment(commentId, newText) },
                                    navController = navController,
                                    onShowProfileView = onShowProfileView,
                                    viewModel = viewModel
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentItem(
    comment: PostComment,
    currentUserId: Int,
    selectedCommentId: Int?,
    onCommentSelected: (Int?) -> Unit,
    onDeleteComment: (Int) -> Unit,
    onUpdateComment: (Int, String) -> Unit,
    navController: NavController,
    onShowProfileView: () -> Unit,
    viewModel: CommentViewModel
) {
    Log.d("CommentItem", "Rendering comment with id: ${comment.id}, userId: ${comment.userId}, comment: ${comment.comment}")

    val timestamp = comment.createdAt?.let {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(it)
    } ?: "Just now"

    val isEditing = remember { mutableStateOf(false) }
    val editedText = remember { mutableStateOf(comment.comment ?: "") }
    val isSelected = selectedCommentId == comment.id
    var showReplies by remember { mutableStateOf(false) }
    var showReplyInput by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        if (comment.userId == currentUserId) {
                            Log.d("CommentItem", "Long-clicked comment ${comment.id}, selected: $isSelected")
                            onCommentSelected(if (isSelected) null else comment.id)
                        } else {
                            Log.d("CommentItem", "Cannot select comment ${comment.id}: userId mismatch")
                        }
                    }
                ),
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
                    verticalAlignment = Alignment.CenterVertically
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

                if (isEditing.value) {
                    TextField(
                        value = editedText.value,
                        onValueChange = { editedText.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        placeholder = { Text("Edit comment") }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            Log.d("CommentItem", "Cancelled editing comment ${comment.id}")
                            isEditing.value = false
                            editedText.value = comment.comment ?: ""
                        }) { Text("Cancel") }
                        TextButton(onClick = {
                            if (comment.id != null) {
                                Log.d("CommentItem", "Saving edited comment ${comment.id}: ${editedText.value}")
                                onUpdateComment(comment.id, editedText.value)
                                isEditing.value = false
                                onCommentSelected(null)
                            }
                        }) { Text("Save") }
                    }
                } else {
                    Text(
                        text = comment.comment ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (isSelected && comment.userId == currentUserId) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                Log.d("CommentItem", "Editing comment ${comment.id}")
                                isEditing.value = true
                            }) { Text("Edit") }
                            TextButton(onClick = {
                                if (comment.id != null) {
                                    Log.d("CommentItem", "Deleting comment ${comment.id}")
                                    onDeleteComment(comment.id)
                                    onCommentSelected(null)
                                }
                            }) { Text("Delete") }
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                IconButton(onClick = {
                    if (comment.id != null) {
                        Log.d("CommentItem", "Toggling like for comment ${comment.id}, current state: ${comment.isLiked}")
                        if (comment.isLiked) viewModel.unlikeComment(comment.id) else viewModel.likeComment(comment.id)
                    }
                }) {
                    Icon(
                        imageVector = if (comment.isLiked) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (comment.isLiked) Color.Red else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "${comment.likes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Row(
            modifier = Modifier.padding(start = 52.dp, top = 4.dp)
        ) {
            TextButton(onClick = {
                Log.d("CommentItem", "Showing reply input for comment ${comment.id}")
                showReplyInput = true
            }) { Text("Reply") }
            TextButton(onClick = {
                Log.d("CommentItem", "Toggling replies for comment ${comment.id}, current state: $showReplies")
                showReplies = !showReplies
            }) { Text(if (showReplies) "Hide replies" else "View replies") }
        }

        if (showReplyInput) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 52.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Write a reply...") }
                )
                IconButton(onClick = {
                    if (replyText.isNotEmpty() && comment.id != null) {
                        Log.d("CommentItem", "Sending reply for comment ${comment.id}: $replyText")
                        viewModel.addReply(comment.id, replyText)
                        replyText = ""
                        showReplyInput = false
                        showReplies = true
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Send reply")
                }
            }
        }

        if (showReplies) {
            LaunchedEffect(showReplies) {
                if (showReplies) {
                    Log.d("CommentItem", "Checking replies for comment ${comment.id}, current replies: ${comment.replies}")
                    val replies = comment.replies ?: emptyList()
                    if (replies.isEmpty() && viewModel.isLoadingRepliesMap[comment.id] != true) {
                        if (comment.id != null) {
                            Log.d("CommentItem", "Fetching replies for comment ${comment.id}")
                            viewModel.fetchReplies(comment.id)
                        } else {
                            Log.e("CommentItem", "Cannot fetch replies: comment.id is null")
                        }
                    }
                }
            }

            val replies = comment.replies ?: emptyList()
            val isLoadingReplies = viewModel.isLoadingRepliesMap[comment.id] ?: false
            Log.d("CommentItem", "Rendering replies for comment ${comment.id}: $replies, isLoading: $isLoadingReplies")
            Column(modifier = Modifier.padding(start = 16.dp)) {
                if (isLoadingReplies) {
                    Log.d("CommentItem", "Loading replies for comment ${comment.id}")
                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                } else if (replies.isNotEmpty()) {
                    Log.d("CommentItem", "Displaying ${replies.size} replies for comment ${comment.id}")
                    replies.forEach { reply ->
                        ReplyItem(
                            reply = reply,
                            currentUserId = currentUserId,
                            viewModel = viewModel
                        )
                    }
                    if (viewModel.canLoadMoreReplies[comment.id] == true && !isLoadingReplies) {
                        TextButton(
                            onClick = {
                                if (comment.id != null) {
                                    Log.d("CommentItem", "Loading more replies for comment ${comment.id}")
                                    viewModel.fetchReplies(comment.id, loadMore = true)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text("Load More Replies")
                        }
                    }
                } else {
                    Log.d("CommentItem", "No replies to display for comment ${comment.id}")
                    Text("No replies yet.", modifier = Modifier.padding(8.dp), color = Color.Gray)
                }
            }
        }
    }
}@Composable
fun ReplyItem(
    reply: Reply,
    currentUserId: Int,
    viewModel: CommentViewModel
) {
    Log.d("ReplyItem", "Rendering reply with id: ${reply.id}, userId: ${reply.userId}, text: ${reply.text}")

    val timestamp = reply.createdAt?.let {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(it)
    } ?: "Just now"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = reply.username?.firstOrNull()?.toString() ?: "?",
                color = Color.White,
                fontSize = 14.sp
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = reply.username ?: "User ${reply.userId}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = " • $timestamp",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Text(
                text = reply.text ?: "",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = if (reply.isLiked) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Like",
                tint = if (reply.isLiked) Color.Red else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${reply.likes}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}