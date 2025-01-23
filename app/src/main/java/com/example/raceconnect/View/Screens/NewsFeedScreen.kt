import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.raceconnect.Model.Post
import com.example.raceconnect.R


@Composable
fun NewsFeedScreen() {
    val posts = remember {
        mutableStateListOf(
            Post("Post 1", listOf(R.drawable.img, R.drawable.img, R.drawable.img), 10, 5),
            Post("Post 2", listOf(R.drawable.img, R.drawable.img), 20, 8),
            Post("Post 3", listOf(R.drawable.img, R.drawable.img, R.drawable.img, R.drawable.img    ), 15, 2)
        )
    }

    Scaffold(
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                items(posts.size) { index ->
                    PostCard(post = posts[index], onLike = {
                        posts[index] = posts[index].copy(likeCount = posts[index].likeCount + 1)
                    }, onComment = {
                        // Handle comment action here
                    }, onShare = {
                        // Handle share action here
                    })
                }
            }
        }
    )
}

@Composable
fun PostCard(post: Post, onLike: () -> Unit, onComment: () -> Unit, onShare: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            // Post Title
            Text(
                text = post.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Images in LazyRow
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(post.images.size) { imageIndex ->
                    Image(
                        painter = painterResource(id = post.images[imageIndex]),
                        contentDescription = "Post Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Like, Comment, Share Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLike) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = Color.Red
                    )
                }
                Text(text = "${post.likeCount} Likes")

                IconButton(onClick = onComment) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_mode_comment_24),
                        contentDescription = "Comment",
                        tint = Color.Gray
                    )
                }
                Text(text = "${post.commentCount} Comments")

                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.Gray
                    )
                }
                Text(text = "Share")
            }
        }
    }
}


