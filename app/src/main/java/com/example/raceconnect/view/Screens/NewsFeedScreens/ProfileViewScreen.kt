package com.example.raceconnect.view.Screens.NewsFeedScreens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.R
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.users
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileViewScreen(navController: NavController, context: Context) {
    val userPreferences = remember { UserPreferences(context) }
    val user by userPreferences.user.collectAsState(initial = null)

    Scaffold(
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                ProfileHeaderSection(user)
                Spacer(modifier = Modifier.height(16.dp))
                ProfileTabsWithContent()
            }
        }
    )
}

@Composable
fun ProfileHeaderSection(user: users?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) {
            Image(
                painter = painterResource(id = R.drawable.baseline_account_circle_24),
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user?.username ?: "Guest User",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = user?.email ?: "No email available",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
fun ProfileTabsWithContent() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Posts", "Photos", "Videos")

    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.White,
        contentColor = Color.Red
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                text = { Text(title) }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    when (selectedTabIndex) {
        0 -> PostsSection()
        1 -> PhotosSection()
        2 -> VideosSection()
    }
}

@Composable
fun PostsSection() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(5) {
            PostItem(
                username = "Junnifer Lawrence",
                content = "Life is as simple as it is with my car.",
                timestamp = "1hr",
                imageRes = R.drawable.baseline_ondemand_video_24
            )
        }
    }
}

@Composable
fun PostItem(username: String, content: String, timestamp: String, imageRes: Int) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_account_circle_24),
                        contentDescription = "User Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = username, fontWeight = FontWeight.Bold)
                    Text(text = timestamp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = content)
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Post Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                ReactionIcon(Icons.Default.ThumbUp, "100")
                ReactionIcon(Icons.Default.ChatBubble, "1")
                ReactionIcon(Icons.Default.Share, "1")
            }
        }
    }
}

@Composable
fun ReactionIcon(icon: ImageVector, count: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = count, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun PhotosSection() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Photos Section")
    }
}

@Composable
fun VideosSection() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Videos Section")
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewProfileViewScreen() {
    ProfileViewScreen(navController = rememberNavController(), context = LocalContext.current)
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileHeaderSection() {
    ProfileHeaderSection(
        user = users(id = 1, username = "John Doe", email = "johndoe@example.com")
    )
}


@Preview(showBackground = true)
@Composable
fun PreviewProfileTabsWithContent() {
    ProfileTabsWithContent()
}

@Preview(showBackground = true)
@Composable
fun PreviewPostsSection() {
    PostsSection()
}

@Preview(showBackground = true)
@Composable
fun PreviewPostItem() {
    PostItem(
        username = "Jane Doe",
        content = "Hello world! Enjoying my day.",
        timestamp = "2h ago",
        imageRes = R.drawable.baseline_ondemand_video_24
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewVideosSection() {
    VideosSection()
}
