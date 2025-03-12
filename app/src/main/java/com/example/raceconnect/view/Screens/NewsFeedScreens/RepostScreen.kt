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
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Report
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.model.Repost
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepostScreen(
    post: NewsFeedDataClassItem,
    navController: NavController,
    viewModel: NewsFeedViewModel,
    onClose: () -> Unit,
    userPreferences: UserPreferences // Added to pass to PostCard
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
                    containerColor = Red // Use theme Red color
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicTextField(
                value = repostComment,
                onValueChange = { repostComment = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                ),
                decorationBox = { innerTextField ->
                    if (repostComment.isEmpty()) {
                        Text(
                            text = "Add a comment to your repost (optional)",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Use the updated PostCard composable
            PostCard(
                post = post,
                navController = navController,
                onCommentClick = { /* Disabled in repost screen */ },
                onLikeClick = { _ -> /* Disabled in repost screen */ },
                viewModel = viewModel,
                onShowFullScreenImage = { /* Disabled in repost screen */ },
                userPreferences = userPreferences,
                onReportClick = { /* No-op implementation */ },
                onShowRepostScreen = { /* Disabled in repost screen */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.repostPost(post.id, repostComment)
                    Toast.makeText(context, "Reposted successfully!", Toast.LENGTH_SHORT).show()
                    onClose()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red) // Use theme Red color
            ) {
                Text("Repost", color = Color.White)
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
    onReportClick: () -> Unit,
    onShowRepostScreen: (NewsFeedDataClassItem) -> Unit
) {
    val user by userPreferences.user.collectAsState(initial = null)
    Log.d("RepostCard", "Rendering repost ID: ${repost.id}, OriginalPostId: ${repost.original_post_id}, OriginalPostAvailable: ${originalPost != null}")

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
                                val destination = if (user?.id == repost.user_id) {
                                    NavRoutes.ProfileView.createRoute(repost.user_id)
                                } else {
                                    NavRoutes.ProfileView.createRoute(repost.user_id)
                                }
                                navController.navigate(destination)
                                Log.d("RepostCard", "Navigating to profile for user ID: ${repost.user_id}")
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
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = formatDateTime(repost.created_at) ?: "Just now",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
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
                            .background(Color.White)
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
                            onShowRepostScreen = onShowRepostScreen
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
                    ReactionIcon(icon = Icons.Default.Repeat, onClick = { onShowRepostScreen(repost) })
                }
            }

            Icon(
                imageVector = Icons.Default.Report,
                contentDescription = "Report repost",
                modifier = Modifier
                    .size(34.dp)
                    .clickable { onReportClick() }
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                tint = Color.Gray
            )
        }
    }
}
// Helper function to format dates
@Composable
fun formatDateTime(dateTime: String?): String? {
    if (dateTime.isNullOrEmpty()) return null
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM d, yyyy 'at' hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(dateTime)
        date?.let { outputFormat.format(it) }
    } catch (e: Exception) {
        dateTime // Fallback to original string if parsing fails
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PreviewRepostCard() {
//    val dummyRepost = NewsFeedDataClassItem(
//        id = 2,
//        user_id = 2,
//        title = "Repost Title",
//        content = "This is my comment on the repost!",
//        isLiked = false,
//        like_count = 5,
//        username = "Reposter",
//        created_at = "2 minutes ago"
//    )
//
//    val dummyOriginalPost = NewsFeedDataClassItem(
//        id = 1,
//        user_id = 1,
//        content = "This is the original post content.",
//        isLiked = false,
//        like_count = 10,
//        title = "Repost Title",
//        username = "OriginalPoster",
//        created_at = "1 hour ago",
//        images = listOf("https://via.placeholder.com/300")
//    )
//
//    val mockViewModel = NewsFeedViewModel(UserPreferences(LocalContext.current))
//    LaunchedEffect(Unit) {
//        mockViewModel.getPostImages(dummyOriginalPost.id)
//    }
//
////    RepostCard(
////        repost = dummyRepost,
////        originalPost = dummyOriginalPost,
////        navController = NavController(LocalContext.current),
////        onCommentClick = {},
////        onLikeClick = {},
////        viewModel = mockViewModel,
////        onShowFullScreenImage = {},
////        userPreferences = UserPreferences(LocalContext.current),
////        onReportClick = {},
////        onShowRepostScreen = {}
////
//    )
//}