package com.example.raceconnect.view.Screens.NewsFeedScreens

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
            import androidx.compose.ui.res.painterResource
            import androidx.compose.ui.text.font.FontWeight
            import androidx.compose.ui.unit.dp
            import androidx.navigation.NavController
            import com.example.raceconnect.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileViewScreen(navController: NavController) {
    Scaffold(
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                ProfileHeaderSection()
                Spacer(modifier = Modifier.height(16.dp))
                ProfileTabsWithContent()
            }
        }
    )
}

@Composable
fun ProfileHeaderSection() {
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
            text = "Junnifer Lawrence",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "I'm just a woman",
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

//@Composable
//fun ProfileViewScreen() {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(text = "Profile View", style = MaterialTheme.typography.headlineLarge)
//    }
//}