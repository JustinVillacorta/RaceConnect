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
import androidx.compose.material.icons.filled.Close
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
import android.net.Uri
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModel
import android.util.Log
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMarketplaceItemScreen(
    itemId: Int,
    navController: NavController,
    viewModel: MarketplaceViewModel,
    onClose: () -> Unit
) {
    val userItems by viewModel.userItems.collectAsState()
    val imagesMap by viewModel.marketplaceImages.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    var item by remember { mutableStateOf(userItems.find { it.id == itemId }) }
    var isLoading by remember { mutableStateOf(item == null && itemId != -1) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var saveTriggered by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf(item?.title ?: "") }
    var price by remember { mutableStateOf(item?.price ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "Formula 1") }
    var description by remember { mutableStateOf(item?.description ?: "") }
    var listingStatus by remember { mutableStateOf(item?.listing_status ?: "Available") }

    val listingStatusOptions = listOf("Available", "Sold", "Reserved")
    val categories = listOf(
        "Formula 1", "24 Hours of Lemans", "World Rally Championship",
        "NASCAR", "Formula Drift", "GT Championship"
    )

    var newImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var imagesToDelete by remember { mutableStateOf<List<Int>>(emptyList()) }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { newImageUris = newImageUris + listOf(it) }
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

    LaunchedEffect(saveTriggered, updateStatus) {
        if (saveTriggered && updateStatus == null) {
            item?.let {
                val updatedItem = it.copy(
                    title = title,
                    price = price,
                    category = category,
                    description = description,
                    listing_status = listingStatus
                )
                viewModel.updateItem(itemId, updatedItem, newImageUris, context, imagesToDelete)
                Log.d("EditMarketplaceItem", "Update triggered for item $itemId")
            }
        } else if (saveTriggered && updateStatus == true) {
            Log.d("EditMarketplaceItem", "Update successful, navigating to MarketplaceItemDetail")
            navController.navigate(NavRoutes.MarketplaceItemDetail.createRoute(itemId)) {
                popUpTo(NavRoutes.MarketplaceItemDetail.route) { inclusive = true }
                launchSingleTop = true
            }
            saveTriggered = false
            viewModel.resetUpdateStatus()
        } else if (saveTriggered && updateStatus == false) {
            errorMessage = viewModel.errorMessage.value ?: "Update failed"
            Log.e("EditMarketplaceItem", "Update failed: $errorMessage")
            saveTriggered = false
            viewModel.resetUpdateStatus()
        }
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
                    Column {
                        if (imagesMap[itemId]?.isNotEmpty() == true || newImageUris.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isWideScreen) 400.dp else 300.dp)
                                    .padding(bottom = 16.dp)
                            ) {
                                imagesMap[itemId]?.let { images ->
                                    items(images.filter { imageUrl ->
                                        val imageId = runBlocking {
                                            val allImages = RetrofitInstance.api.getMarketplaceItemImages(itemId).body()
                                            allImages?.find { it.image_url == imageUrl }?.id
                                        }
                                        imageId != null && !imagesToDelete.contains(imageId)
                                    }) { imageUrl ->
                                        Box {
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
                                            val imageId = runBlocking {
                                                val allImages = RetrofitInstance.api.getMarketplaceItemImages(itemId).body()
                                                allImages?.find { it.image_url == imageUrl }?.id
                                            }
                                            if (imageId != null) {
                                                IconButton(
                                                    onClick = {
                                                        imagesToDelete = imagesToDelete + imageId
                                                        Log.d("EditMarketplaceItem", "Marked image $imageId for deletion")
                                                    },
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remove Image",
                                                        tint = Color.Red
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                items(newImageUris) { imageUri ->
                                    Box {
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
                                        IconButton(
                                            onClick = {
                                                newImageUris = newImageUris.filter { it != imageUri }
                                                Log.d("EditMarketplaceItem", "Removed new image URI: $imageUri")
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove New Image",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (!item?.image_url.isNullOrEmpty()) {
                            Box {
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
                        }

                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
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

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (₱)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

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

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp)
                            .padding(bottom = 4.dp),
                        maxLines = 3
                    )

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

                    Button(
                        onClick = { saveTriggered = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(top = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Red)
                    ) {
                        Text("Save Changes", color = Color.White)
                    }
                }
            }
        }
    }
}