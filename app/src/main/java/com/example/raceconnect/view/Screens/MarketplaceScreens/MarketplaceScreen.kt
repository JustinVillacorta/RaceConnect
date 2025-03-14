package com.example.raceconnect.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.view.Screens.MarketplaceScreens.MarketplaceItemCard
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModel
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModelFactory
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    userPreferences: UserPreferences,
    navController: NavController,
    onShowCreateListing: () -> Unit,
    onShowItemDetail: (Int) -> Unit,
    viewModel: MarketplaceViewModel = viewModel(factory = MarketplaceViewModelFactory(userPreferences))
) {
    val marketplaceItems by viewModel.marketplaceItems.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        if (marketplaceItems.isEmpty() && !isRefreshing && errorMessage == null) {
            viewModel.refreshMarketplaceItems()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Marketplace",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = { /* Handle search click */ }) {
                        Icon(
                            painter = painterResource(id = com.example.raceconnect.R.drawable.baseline_search_24),
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Red,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }},
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = { viewModel.refreshMarketplaceItems() }
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Button(
                        onClick = onShowCreateListing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text("Create and Sell")
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(marketplaceItems, key = { it.id }) { item ->
                            MarketplaceItemCard(
                                item = item,
                                navController = navController,
                                viewModel = viewModel,
                                onClick = { itemId -> onShowItemDetail(itemId) },
                                onLikeError = { errorMessage ->
                                    coroutineScope.launch {
                                        val snackbarResult = snackbarHostState.showSnackbar(
                                            message = errorMessage,
                                            actionLabel = "Retry",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (snackbarResult == SnackbarResult.ActionPerformed) {
                                            try {
                                                viewModel.toggleLike(item.id)
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar(
                                                    message = "Retry failed: ${e.message}",
                                                    actionLabel = "Dismiss",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMessage ?: "Unknown error",
                    actionLabel = "Dismiss",
                    duration = SnackbarDuration.Short
                )
                viewModel.clearErrorMessage()
            }
        }
    }
}