package com.example.raceconnect.ui

import MarketplaceViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.raceconnect.model.MarketplaceDataClassItem
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState


// Marketplace screen displaying "Current's Best" and "More for You" sections
@Composable
fun MarketplaceScreen(viewModel: MarketplaceViewModel = viewModel()) {
    val items by viewModel.items.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showCreateListing by remember { mutableStateOf(false) }

    if (showCreateListing) {
        CreateMarketplaceItemScreen(
            onClose = { showCreateListing = false },
            onPost = { title, price, description, category, imageUrl ->
                viewModel.addMarketplaceItem(
                    title = title,
                    price = price,
                    description = description,
                    category = category,
                    imageUrl = imageUrl,
                    sellerId = 10 // Replace with dynamic seller ID
                )
                showCreateListing = false
            }
        )
    } else {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { viewModel.refreshMarketplaceItems() }
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Marketplace", style = MaterialTheme.typography.headlineMedium)
                    IconButton(onClick = { showCreateListing = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
                    }
                }

                // Marketplace Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items, key = { it.id }) { item ->
                        MarketplaceItemCard(item = item)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMarketplaceItemScreen(
    onClose: () -> Unit,
    onPost: (title: String, price: String, description: String, category: String, imageUrl: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Listing") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(onClick = {
                        if (title.isNotEmpty() && price.isNotEmpty()) {
                            onPost(title, price, description, category, imageUrl)
                        }
                    }) {
                        Text("Publish")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                //OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") })
            }
        }
    )
}


@Composable
fun MarketplaceItemCard(item: MarketplaceDataClassItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { /* Navigate to item details */ },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = item.title, style = MaterialTheme.typography.bodyMedium)
            Text(text = "₱${item.price}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
