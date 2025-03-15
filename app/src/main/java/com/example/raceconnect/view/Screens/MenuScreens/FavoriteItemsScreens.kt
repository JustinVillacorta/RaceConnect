package com.example.raceconnect.view.Screens.MenuScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.MarketplaceDataClassItem
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteItemsScreen(
    navController: NavController,
    userPreferences: UserPreferences,
    onClose: () -> Unit,
    onShowItemDetail: (Int) -> Unit,
    viewModel: MarketplaceViewModel // Use the provided viewModel
) {
    val userItems by viewModel.userItems.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val imagesMap by viewModel.marketplaceImages.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var expanded by remember { mutableStateOf(false) }
    val filterOptions = listOf("All items", "Jackets", "T-shirts", "Formula Drift", "NASCAR")
    var selectedOption by remember { mutableStateOf(filterOptions[0]) }

    val filteredItems = if (selectedOption == "All items") {
        userItems
    } else {
        userItems.filter { it.category == selectedOption }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            viewModel.fetchUserMarketplaceItems() // Ensure data is fetched on screen load
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Favorite Items", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFFC62828),
                    titleContentColor = Color.White
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedOption,
                        onValueChange = { /* no-op, read-only */ },
                        label = { Text("Filter by category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded
                            )
                        },
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        filterOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedOption = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    isRefreshing -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = Red
                        )
                    }
                    userItems.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "You haven't liked any items yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.fetchUserMarketplaceItems()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Red)
                            ) {
                                Text("Refresh", color = Color.White)
                            }
                        }
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredItems) { item ->
                                FavoriteItemCard(
                                    item = item,
                                    imagesMap = imagesMap,
                                    onClick = { onShowItemDetail(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteItemCard(
    item: MarketplaceDataClassItem,
    imagesMap: Map<Int, List<String>>,
    onClick: () -> Unit
) {
    val itemImages = imagesMap[item.id] ?: emptyList()
    val displayImage = itemImages.firstOrNull() ?: item.image_url?.takeIf { it.isNotEmpty() } ?: "https://via.placeholder.com/150"

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            // Item image
            AsyncImage(
                model = displayImage,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Name
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp),
                maxLines = 2
            )

            // Price with red background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFC62828))
                    .padding(vertical = 4.dp, horizontal = 8.dp),
            ) {
                Text(
                    text = "₱${item.price}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}