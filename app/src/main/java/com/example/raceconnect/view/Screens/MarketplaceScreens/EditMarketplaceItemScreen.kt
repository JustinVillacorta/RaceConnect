package com.example.raceconnect.view.Screens.MarketplaceScreens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import android.net.Uri // Added import for Uri
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModel
import android.util.Log
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMarketplaceItemScreen(
    itemId: Int, // Ensure itemId is Int
    navController: NavController,
    viewModel: MarketplaceViewModel,
    onClose: () -> Unit
) {
    val userItems by viewModel.userItems.collectAsState()
    val imagesMap by viewModel.marketplaceImages.collectAsState()
    var item by remember { mutableStateOf(userItems.find { it.id == itemId }) }
    var isLoading by remember { mutableStateOf(item == null && itemId != -1) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Editable fields with initial values from the item
    var title by remember { mutableStateOf(item?.title ?: "") }
    var price by remember { mutableStateOf(item?.price ?: "") } // String to match MarketplaceDataClassItem
    var category by remember { mutableStateOf(item?.category ?: "Formula 1") }
    var description by remember { mutableStateOf(item?.description ?: "") }
    var listingStatus by remember { mutableStateOf(item?.listing_status ?: "Available") }

    // Status and Category options
    val listingStatusOptions = listOf("Available", "Sold", "Reserved")
    val categories = listOf(
        "Formula 1",
        "24 Hours of Lemans",
        "World Rally Championship",
        "NASCAR",
        "Formula Drift",
        "GT Championship"
    )

    // State for new images (URIs of images picked by the user)
    var newImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) } // Explicitly typed

    // Image picker launcher
    val context = LocalContext.current // Moved outside onClick
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            newImageUris = newImageUris + listOf(it) // Add Uri to list
        }
    }

    LaunchedEffect(itemId) {
        if (item == null && itemId != -1) {
            Log.d("EditMarketplaceItem", "Item $itemId not found locally, fetching from API...")
            isLoading = true
            viewModel.fetchItemById(itemId)?.let { fetchedItem ->
                item = fetchedItem
                title = fetchedItem.title ?: ""
                price = fetchedItem.price ?: ""
                category = fetchedItem.category ?: "Formula 1"
                description = fetchedItem.description ?: ""
                listingStatus = fetchedItem.listing_status ?: "Available"
                Log.d("EditMarketplaceItem", "Fetched item: $fetchedItem")
            } ?: run {
                errorMessage = "Failed to load item $itemId"
                Log.e("EditMarketplaceItem", "Failed to fetch item $itemId")
            }
            isLoading = false
        }
        viewModel.getMarketplaceItemImages(itemId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Listing", fontWeight = FontWeight.Bold, color = Color.White) },
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = if (isWideScreen) 32.dp else 16.dp)
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp)
                ) {
                    // Image section
                    Column {
                        // Display existing images
                        if (imagesMap[itemId]?.isNotEmpty() == true || newImageUris.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isWideScreen) 400.dp else 300.dp)
                                    .padding(bottom = 16.dp)
                            ) {
                                // Existing images from the server
                                imagesMap[itemId]?.let { images ->
                                    items(images) { imageUrl ->
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
                                // New images picked by the user
                                items(newImageUris) { imageUri ->
                                    AsyncImage(
                                        model = imageUri,
                                        contentDescription = "New Item Image",
                                        modifier = Modifier
                                            .width(if (isWideScreen) 400.dp else 300.dp)
                                            .fillMaxHeight()
                                            .padding(end = 8.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        } else if (!item?.image_url.isNullOrEmpty()) {
                            AsyncImage(
                                model = item!!.image_url,
                                contentDescription = "Item Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isWideScreen) 400.dp else 300.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(bottom = 16.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Add Image Button
                        Button(
                            onClick = {
                                imagePickerLauncher.launch("image/*")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Red)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Image",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Image", color = Color.White)
                            }
                        }
                    }

                    // Editable Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    // Editable Price
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (₱)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    // Category Dropdown
                    var categoryExpanded by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded }
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                categories.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            category = option
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Editable Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp)
                            .padding(bottom = 8.dp),
                        maxLines = 5
                    )

                    // Listing Status Dropdown
                    var listingStatusExpanded by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = listingStatusExpanded,
                            onExpandedChange = { listingStatusExpanded = !listingStatusExpanded }
                        ) {
                            OutlinedTextField(
                                value = listingStatus,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Listing Status") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = listingStatusExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = listingStatusExpanded,
                                onDismissRequest = { listingStatusExpanded = false }
                            ) {
                                listingStatusOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            listingStatus = option
                                            listingStatusExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Save Button
                    Button(
                        onClick = {
                            item?.let {
                                val updatedItem = it.copy(
                                    title = title,
                                    price = price,
                                    category = category,
                                    description = description,
                                    listing_status = listingStatus
                                )
                                viewModel.updateItem(itemId, updatedItem, newImageUris, context)
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Red)
                    ) {
                        Text("Save Changes", color = Color.White)
                    }
                }
            }
        }
    }
}