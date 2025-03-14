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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    Card(
        shape = RoundedCornerShape(8.dp), // Match FavoriteItemCard shape
        elevation = CardDefaults.cardElevation(4.dp), // Match elevation
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item.id) }
    ) {
        Column {
            AsyncImage(
                model = displayImage,
                contentDescription = item.title, // Match contentDescription
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp) // Match height
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)) // Match clipping
            )

            Spacer(modifier = Modifier.height(8.dp)) // Match spacing

            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, // Match font weight
                modifier = Modifier
                    .padding(horizontal = 8.dp) // Match padding
                    .padding(bottom = 8.dp),
                maxLines = 2 // Match maxLines
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFC62828)) // Match red background
                    .padding(vertical = 4.dp, horizontal = 8.dp), // Match padding
            ) {
                Text(
                    text = "₱${item.price}",
                    color = Color.White, // Match text color
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold // Match font weight
                )
            }
        }
    }
}