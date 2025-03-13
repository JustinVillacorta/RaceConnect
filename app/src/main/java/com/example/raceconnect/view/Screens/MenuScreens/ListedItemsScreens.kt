package com.example.raceconnect.view.Screens.MenuScreens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.MarketplaceDataClassItem
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.NewsFeed.ListedItems.ListedItemsViewModel
import com.example.raceconnect.viewmodel.NewsFeed.ListedItems.ListedItemsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListedItemsScreen(
    navController: NavController,
    userPreferences: UserPreferences,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ListedItemsViewModel = viewModel(
        factory = ListedItemsViewModelFactory(userPreferences, context)
    )

    val listedItems by viewModel.listedItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    val filterOptions = listOf("All items", "Available", "Sold")
    var selectedOption by remember { mutableStateOf(filterOptions[0]) }

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
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Red
                    )
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage ?: "An error occurred",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadListedItems() },
                            colors = ButtonDefaults.buttonColors(containerColor = Red)
                        ) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }
                listedItems.isEmpty() -> {
                    Text(
                        text = "You haven't listed any items yet",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
                else -> {
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
                                onValueChange = { /* read-only */ },
                                label = { Text("Filter items") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
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

                        val filteredItems = when (selectedOption) {
                            "Available" -> listedItems.filter { it.status == "Available" }
                            "Sold" -> listedItems.filter { it.status != "Available" }
                            else -> listedItems
                        }

                        if (filteredItems.isEmpty()) {
                            Text(
                                text = "No items match the selected filter",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredItems) { item ->
                                    ListedItemCard(
                                        item = item,
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
}

@Composable
fun ListedItemCard(
    item: MarketplaceDataClassItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            AsyncImage(
                model = item.image_url.takeIf { it.isNotEmpty() } ?: "https://via.placeholder.com/150",
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