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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.MarketplaceDataClassItem
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModel
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListedItemsScreen(
    navController: NavController,
    userPreferences: UserPreferences,
    onClose: () -> Unit,
    onRefresh: () -> Unit = {} // Optional callback to trigger refresh
) {
    val viewModel: MarketplaceViewModel = viewModel(
        factory = MarketplaceViewModelFactory(userPreferences)
    )

    val userItems by viewModel.userItems.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Trigger fetch of listed items when the screen is first loaded or refreshed
    LaunchedEffect(Unit) {
        if (userItems.isEmpty() && !isRefreshing) {
            viewModel.fetchUserListedItems() // Initial fetch
        }
    }

    // Trigger refresh when onRefresh is called
    LaunchedEffect(onRefresh) {
        onRefresh() // This will be a no-op unless called from outside
        if (!isRefreshing) {
            coroutineScope.launch {
                viewModel.fetchUserListedItems() // Refresh the list
            }
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("My Listed Items", color = Color.White) },
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
                    containerColor = Red,
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
            when {
                isRefreshing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Red
                    )
                }
                userItems.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "You haven't listed any items yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.fetchUserListedItems() // Refresh listed items
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Red)
                        ) {
                            Text("Refresh", color = Color.White)
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(userItems) { item ->
                                ListedItemCard(
                                    item = item,
                                    viewModel = viewModel,
                                    onClick = {
                                        navController.navigate(NavRoutes.MarketplaceItemDetail.createRoute(item.id))
                                    }
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
fun ListedItemCard(
    item: MarketplaceDataClassItem,
    viewModel: MarketplaceViewModel,
    onClick: () -> Unit
) {
    val imagesMap by viewModel.marketplaceImages.collectAsState()
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

            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp),
                maxLines = 2
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Red)
                    .padding(vertical = 4.dp, horizontal = 8.dp)
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