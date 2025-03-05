package com.example.raceconnect.ui

import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.view.Screens.MarketplaceScreens.CreateMarketplaceItemScreen
import com.example.raceconnect.view.Screens.MarketplaceScreens.MarketplaceItemCard
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModelFactory
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

// Marketplace screen displaying "Current's Best" and "More for You" sections
@Composable
fun MarketplaceScreen(
    userPreferences: UserPreferences,
    navController: NavController,
    onShowCreateListing: () -> Unit,
    onShowItemDetail: (Int) -> Unit, // Callback to show MarketplaceItemDetailScreen
    viewModel: MarketplaceViewModel = viewModel(factory = MarketplaceViewModelFactory(userPreferences))
) {
    val items by viewModel.items.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { viewModel.refreshMarketplaceItems() }
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // "Create and Sell" Button
                Button(
                    onClick = onShowCreateListing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text("Create and Sell")
                }

                // Grid of Marketplace Items
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items, key = { it.id }) { item ->
                        MarketplaceItemCard(
                            item = item,
                            navController = navController,
                            onClick = { onShowItemDetail(item.id) } // Trigger callback
                        )
                    }
                }
            }
        }
    }
}