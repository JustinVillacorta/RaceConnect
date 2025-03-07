package com.example.raceconnect.view.Screens.NewsFeedScreens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepostScreen(
    post: NewsFeedDataClassItem,
    navController: NavController,
    viewModel: NewsFeedViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var repostComment by remember { mutableStateOf("") }
    val brandRed = Color(0xFFC62828) // Your brand's red color

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
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = brandRed
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
            // Optional comment input
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

            // Display the original post using PostCard
            PostCard(
                post = post,
                navController = navController,
                onCommentClick = { /* Disabled in repost screen */ },
                onLikeClick = { /* Disabled in repost screen */ },
                viewModel = viewModel,
                onShowFullScreenImage = { /* Disabled in repost screen */ },
                onShowProfileView = { /* Disabled in repost screen */ },
                onReportClick = { /* No-op implementation */ }, // Added to satisfy the parameter
                onShowRepostScreen = { /* Disabled in repost screen */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Repost button
            Button(
                onClick = {
                    // TODO: Implement repost logic in the ViewModel
                    viewModel.repostPost(post.id, repostComment)
                    Toast.makeText(context, "Reposted successfully!", Toast.LENGTH_SHORT).show()
                    onClose()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandRed)
            ) {
                Text("Repost", color = Color.White)
            }
        }
    }
}