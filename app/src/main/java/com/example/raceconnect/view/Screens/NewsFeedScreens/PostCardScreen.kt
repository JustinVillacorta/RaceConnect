package com.example.raceconnect.view.Screens.NewsFeedScreens

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

@Composable
fun PostCard(post: NewsFeedDataClassItem, navController: NavController, onCommentClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Profile Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .clickable {
                            navController.navigate("profile")
                        }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Anonymous",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Just now",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Post Content
            Text(
                text = post.content ?: "No content available.",
                style = MaterialTheme.typography.bodyMedium
            )

            // Image Display (if available)
            if (!post.img_url.isNullOrEmpty()) {
                AndroidView(factory = { context ->
                    ImageView(context).apply {
                        Picasso.get()
                            .load(post.img_url)
                            .into(this, object : Callback {
                                override fun onSuccess() {}
                                override fun onError(e: Exception?) {
                                    e?.printStackTrace()
                                }
                            })
                    }
                }, modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reaction Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReactionIcon(icon = Icons.Default.Favorite)
                Spacer(modifier = Modifier.width(12.dp))
                ReactionIcon(icon = Icons.Default.ChatBubble, onClick = onCommentClick)
                Spacer(modifier = Modifier.width(12.dp))
                ReactionIcon(icon = Icons.Default.Repeat)
            }
        }
    }
}


@Composable
fun ReactionIcon(icon: ImageVector, onClick: (() -> Unit)? = null) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
            .size(20.dp)
            .clickable { onClick?.invoke() },
        tint = Color.Gray
    )
}