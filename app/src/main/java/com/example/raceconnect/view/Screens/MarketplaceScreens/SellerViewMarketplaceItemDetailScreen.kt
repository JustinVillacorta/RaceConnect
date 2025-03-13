package com.example.raceconnect.view.Screens.MarketplaceScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModel
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerViewMarketplaceItemDetailScreen(
    itemId: Int,
    navController: NavController,
    viewModel: MarketplaceViewModel,
    onClose: () -> Unit,
    onEdit: (Int) -> Unit = {},
    onDelete: (Int) -> Unit = {}
) {
    // Use userItems instead of items since this is for the seller's own listing
    val userItems by viewModel.userItems.collectAsState()
    val imagesMap by viewModel.marketplaceImages.collectAsState()
    var item by remember { mutableStateOf(userItems.find { it.id == itemId }) }
    var isLoading by remember { mutableStateOf(item == null && itemId != -1) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch item if not found locally
    LaunchedEffect(itemId) {
        if (item == null && itemId != -1) {
            Log.d("SellerView", "Item $itemId not found locally, fetching from API...")
            isLoading = true
            viewModel.fetchItemById(itemId)?.let { fetchedItem ->
                item = fetchedItem
                Log.d("SellerView", "Fetched item: $fetchedItem")
            } ?: run {
                errorMessage = "Failed to load item $itemId"
                Log.e("SellerView", "Failed to fetch item $itemId")
            }
            isLoading = false
        }
    }

    // Fetch images
    LaunchedEffect(itemId) {
        viewModel.getMarketplaceItemImages(itemId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Listing Details", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Red,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp
        val isWideScreen = screenWidthDp > 600

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(
                horizontal = if (isWideScreen) 32.dp else 16.dp,
                vertical = 16.dp
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (item != null) {
                Column {
                    // Image carousel
                    if (imagesMap[itemId]?.isNotEmpty() == true) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isWideScreen) 400.dp else 300.dp)
                        ) {
                            items(imagesMap[itemId]!!) { imageUrl ->
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Item Image",
                                    modifier = Modifier
                                        .width(if (isWideScreen) 400.dp else 300.dp)
                                        .fillMaxHeight()
                                        .padding(end = 8.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    } else {
                        AsyncImage(
                            model = item!!.image_url,
                            contentDescription = "Item Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isWideScreen) 400.dp else 300.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = item!!.title,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Price
                    Text(
                        text = "₱${item!!.price}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status
                    Text(
                        text = "Status: ${item!!.status}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item!!.status == "Available") Color.Green else Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description
                    Text(
                        text = item!!.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        maxLines = if (isWideScreen) 10 else 5,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons (Edit and Delete)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { onEdit(itemId) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .padding(end = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Red)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Listing",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit", color = Color.White)
                            }
                        }

                        Button(
                            onClick = { onDelete(itemId) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .padding(start = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Listing",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}