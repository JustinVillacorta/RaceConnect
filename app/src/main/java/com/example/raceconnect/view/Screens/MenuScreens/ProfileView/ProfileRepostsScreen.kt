package com.example.raceconnect.view.Screens.MenuScreens.ProfileView

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.raceconnect.R
import com.example.raceconnect.model.ProfileRepostsDataClass
import com.example.raceconnect.model.Repost
import java.text.SimpleDateFormat
import java.util.*



@Composable
fun RepostsSection(
    userReposts: List<Repost>,
    postImages: Map<Int, List<String>>,
    profileOriginalPosts: Map<Int, ProfileRepostsDataClass>,
    profileUsername: String?,
    profileUserId: Int,
    onFetchPostImages: (Int) -> Unit,
    onFetchOriginalPost: (Int) -> Unit
) {
    val myReposts = userReposts.filter { it.userId == profileUserId }

    if (myReposts.isEmpty()) {
        Text(
            text = "No reposts yet",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            items(myReposts.size) { index ->
                val reversedIndex = myReposts.size - 1 - index
                val repost = myReposts[reversedIndex]
                val originalPost = profileOriginalPosts[repost.postId]

                LaunchedEffect(repost.id, repost.postId) {
                    onFetchPostImages(repost.id)
                    onFetchPostImages(repost.postId)
                    if (originalPost == null) {
                        onFetchOriginalPost(repost.postId)
                    }
                }

                Card(
                    shape = RectangleShape,
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(vertical = 2.dp)
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Repost Header (User who reposted)
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
                                    Image(
                                        painter = painterResource(id = R.drawable.baseline_account_circle_24),
                                        contentDescription = "User Profile",
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
                                            text = "${profileUsername ?: "Anonymous"} reposted",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = formatTime(repost.createdAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Repost Comment (if any) with ExpandableText
                            if (!repost.quote.isNullOrEmpty()) {
                                ExpandableText(
                                    text = repost.quote,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Repost Images (if any)
                            if (postImages[repost.id]?.isNotEmpty() == true) {
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(postImages[repost.id]!!.size) { index ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .width(200.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        ) {
                                            val painter = rememberAsyncImagePainter(model = postImages[repost.id]!![index])
                                            Image(
                                                painter = painter,
                                                contentDescription = "Repost image $index",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Original Post
                            if (originalPost != null) {
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // Original Post Header
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
                                                Image(
                                                    painter = painterResource(id = R.drawable.baseline_account_circle_24),
                                                    contentDescription = "Original Post User Profile",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = originalPost.username ?: "Unknown",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black
                                                )
                                                Text(
                                                    text = formatTime(originalPost.created_at),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Original Post Content with ExpandableText
                                        if (!originalPost.content.isNullOrEmpty()) {
                                            ExpandableText(
                                                text = originalPost.content,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }

                                        // Original Post Images (with HorizontalPager)
                                        if (postImages[repost.postId]?.isNotEmpty() == true) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(200.dp)
                                                    .padding(top = 8.dp)
                                            ) {
                                                val pagerState = rememberPagerState(pageCount = { postImages[repost.postId]!!.size })
                                                HorizontalPager(
                                                    state = pagerState,
                                                    modifier = Modifier.fillMaxSize()
                                                ) { page ->
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clip(RoundedCornerShape(8.dp))
                                                    ) {
                                                        val painter = rememberAsyncImagePainter(model = postImages[repost.postId]!![page])
                                                        Image(
                                                            painter = painter,
                                                            contentDescription = "Original post image $page",
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }
                                                }
                                                // Top-right indicator
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(8.dp)
                                                        .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "${pagerState.currentPage + 1}/${postImages[repost.postId]!!.size}",
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                                // Bottom indicators
                                                Row(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomCenter)
                                                        .padding(bottom = 8.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    postImages[repost.postId]!!.forEachIndexed { index, _ ->
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
                                }
                            } else {
                                Text(
                                    text = "Original post unavailable",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}