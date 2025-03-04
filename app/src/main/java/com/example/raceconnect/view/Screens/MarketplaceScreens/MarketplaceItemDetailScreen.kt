package com.example.raceconnect.view.Screens.MarketplaceScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.raceconnect.model.MarketplaceDataClassItem

@Composable
fun MarketplaceItemDetailScreen(itemId: Int, navController: NavController) {
    // Get screen width to adjust layout dynamically
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isWideScreen = screenWidthDp > 600 // Threshold for tablets or large phones

    // Simulate fetching item data based on itemId (replace with your actual data source)
    val item = MarketplaceDataClassItem(
        id = itemId, // Let backend assign or use the provided ID
        seller_id = 0,
        title = "McLaren 2024 Team Polo", // Hardcoded title or fetched from data source
        description = "The Men's Official McLaren F1 Team Polo Shirt features striking McLaren-colored tape along the shoulders and waist - a subtle yet powerful nod to your favorite team. The classic Polo Shirt features a sleek pointed collar and concealed placket fastening for a smart look. Mesh panelling at the back and underarm ventilation ensure a fresh feel for maximum comfort during tense races.",
        price = "3705", // Price in Philippine Pesos as String
        category = "Formula 1",
        image_url = "https://example.com/mclaren_polo.jpg", // Replace with actual image URL
        favorite_count = 0,
        status = "Available",
        report = "None",
        reported_at = null,
        previous_status = null,
        listing_status = "Available",
        created_at = "", // Backend will set this
        updated_at = "" // Backend will set this
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = if (isWideScreen) 32.dp else 16.dp, // Larger padding on wide screens
                vertical = 16.dp
            )
    ) {
        // Item Image (maintain aspect ratio and scale responsively)
        AsyncImage(
            model = item.image_url,
            contentDescription = "Marketplace Item Detail Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isWideScreen) 400.dp else 300.dp) // Adjust height for larger screens
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp)) // Consistent spacing as in the image

        // Item Title (centered, matching the image)
        Text(
            text = item.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp)) // Consistent spacing as in the image

        // Item Price (centered, matching the image)
        Text(
            text = "₱${item.price}", // Format price with Philippine Peso symbol
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp)) // Consistent spacing as in the image

        // Item Description (left-aligned, matching the image, with responsive max lines)
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            maxLines = if (isWideScreen) 10 else 5, // Fewer lines on smaller screens
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(16.dp)) // Consistent spacing as in the image

        // Add to Favorites and Chat Seller Buttons (centered, matching the image, responsive arrangement)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly // Ensure even spacing for uniformity
        ) {
            // Use a fixed width or minimum width for both buttons to enforce equal sizing
            Button(
                onClick = { /* Handle add to favorites */ },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f) // Distribute space evenly
                    .height(50.dp) // Fixed height for consistency
                    .padding(end = 8.dp) // Consistent padding between buttons
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center // Center content within button
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Add to Favorites",
                        modifier = Modifier.size(24.dp) // Fix icon size for consistency
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to Favorites")
                }
            }

            Button(
                onClick = { /* Handle chat seller */ },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .weight(1f) // Distribute space evenly
                    .height(50.dp) // Fixed height for consistency
                    .padding(start = 8.dp) // Consistent padding between buttons
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center // Center content within button
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Chat Seller",
                        modifier = Modifier.size(24.dp) // Fix icon size for consistency
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat Seller")
                }
            }
        }
    }
}

// Preview function for MarketplaceItemDetailScreen in portrait mode (small phone)
@Preview(showBackground = true, name = "Marketplace Item Detail - Portrait (Small Phone)")
@Composable
fun MarketplaceItemDetailScreenPreviewPortrait() {
    MaterialTheme {
        MarketplaceItemDetailScreen(
            itemId = 1,
            navController = rememberNavController()
        )
    }
}

// Preview function for MarketplaceItemDetailScreen in landscape mode (tablet/large phone)
@Preview(showBackground = true, name = "Marketplace Item Detail - Landscape (Tablet)", device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Composable
fun MarketplaceItemDetailScreenPreviewLandscape() {
    MaterialTheme {
        MarketplaceItemDetailScreen(
            itemId = 1,
            navController = rememberNavController()
        )
    }
}