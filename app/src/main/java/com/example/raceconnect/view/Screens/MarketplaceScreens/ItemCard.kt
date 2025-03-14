package com.example.raceconnect.view.Screens.MarketplaceScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.raceconnect.model.MarketplaceDataClassItem
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModel
import kotlinx.coroutines.launch

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
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick(item.id) },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = displayImage,
                contentDescription = "Marketplace Item Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "₱${item.price}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}