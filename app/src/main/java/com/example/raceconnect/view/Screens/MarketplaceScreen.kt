package com.example.raceconnect.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.raceconnect.model.MarkeplaceItem
import com.example.raceconnect.viewmodel.MarketplaceViewModel


// Main navigation host for the Marketplace
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MarketplaceNavHost(viewModel: MarketplaceViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "marketplace") {
        // Main marketplace screen
        composable("marketplace") {
            MarketplaceScreen(
                viewModel = viewModel,
                onCreateListingClick = { navController.navigate("newListing") },
                onItemClick = { item ->
                    navController.navigate("itemDetail/${item.id}")
                }
            )
        }
        // New listing screen for creating and publishing items
        composable("newListing") {
            NewListingScreen(
                onPublish = { newItem ->
                    viewModel.addItem(newItem) // Add the new item to the list
                    navController.popBackStack() // Navigate back to the marketplace
                },
                onCancel = { navController.popBackStack() } // Navigate back if canceled
            )
        }
        // Item detail screen for viewing details of a specific item
        composable("itemDetail/{itemId}") { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")?.toIntOrNull()
            val item = viewModel.items.value.find { it.id == itemId }
            item?.let {
                ItemDetailScreen(item = it, onBack = { navController.popBackStack() })
            }
        }
    }
}

// Marketplace screen displaying "Current's Best" and "More for You" sections
@Composable
fun MarketplaceScreen(
    viewModel: MarketplaceViewModel,
    onItemClick: (MarkeplaceItem) -> Unit,
    onCreateListingClick: () -> Unit
) {
    val items by viewModel.items.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top bar with title and search icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Marketplace",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = { /* TODO: Add search functionality */ }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Create and Sell button
        Button(
            onClick = onCreateListingClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Create and Sell")
        }

        // "Current's Best" section
        Text(
            text = "Current's Best",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(5) { index ->
                Card(
                    modifier = Modifier
                        .width(200.dp)
                        .height(150.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Best $index", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "More for You" section
        Text(
            text = "More for You",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clickable { onItemClick(item) },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Display the title and price
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.price,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
// New listing screen to add a new marketplace item
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewListingScreen(onPublish: (MarkeplaceItem) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Listing") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(onClick = {
                        if (title.isNotEmpty() && price.isNotEmpty()) {
                            onPublish(
                                MarkeplaceItem(
                                    id = (0..1000).random(), // Generate unique ID
                                    title = title,
                                    price = price,
                                    description = description,
                                    images = listOf() // No images for now
                                )
                            )
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
                // Input fields for title, price, and description
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

// Item detail screen to display the selected item's details
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(item: MarkeplaceItem, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                // Display item details
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = item.price,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
}
