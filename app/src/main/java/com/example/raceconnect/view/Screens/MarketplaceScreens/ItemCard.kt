package com.example.raceconnect.view.Screens.MarketplaceScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.raceconnect.model.MarketplaceDataClassItem
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModel

@Composable
fun MarketplaceItemCard(
    item: MarketplaceDataClassItem,
    navController: NavController,
    viewModel: MarketplaceViewModel,
    onClick: (Int) -> Unit = {},
    onLikeError: (String) -> Unit
) {
    val imagesMap by viewModel.marketplaceImages.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val itemImages = imagesMap[item.id] ?: emptyList()
    val displayImage = itemImages.firstOrNull() ?: item.image_url?.takeIf { it.isNotEmpty() } ?: "https://via.placeholder.com/150"
    val liked by remember { derivedStateOf { isLiked[item.id] ?: false } }

    // State to toggle visibility of hidden items
    var showHiddenItem by remember(item.id, item.status) { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val isHidden = item.status?.lowercase() == "hidden"

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !isHidden || showHiddenItem, // Disable clicking when hidden unless shown
                onClick = { onClick(item.id) }
            )
    ) {
        Box {
            Column {
                AsyncImage(
                    model = displayImage,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .then(
                            if (isHidden && !showHiddenItem) Modifier.blur(10.dp) else Modifier
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 8.dp)
                        .then(
                            if (isHidden && !showHiddenItem) Modifier.blur(10.dp) else Modifier
                        ),
                    maxLines = 2
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFC62828))
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Text(
                        text = "₱${item.price}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = if (isHidden && !showHiddenItem) Modifier.blur(10.dp) else Modifier
                    )
                }
            }

            // Overlay for hidden items
            if (isHidden && !showHiddenItem) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "This item is hidden",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showConfirmationDialog = true }) {
                            Text("See Item")
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for revealing hidden item
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("View Hidden Item") },
            text = { Text("Are you sure you want to view this hidden item?") },
            confirmButton = {
                TextButton(onClick = {
                    showHiddenItem = true
                    showConfirmationDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}