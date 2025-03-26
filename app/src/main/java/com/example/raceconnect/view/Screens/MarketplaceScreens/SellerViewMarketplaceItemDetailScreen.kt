package com.example.raceconnect.view.Screens.MarketplaceScreens

import android.util.Log
import androidx.compose.foundation.background
import com.example.raceconnect.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.view.ui.theme.fontFamily
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerViewMarketplaceItemDetailScreen(
    itemId: Int,
    navController: NavController,
    viewModel: MarketplaceViewModel,
    onClose: () -> Unit,
    onRefreshListedItems: () -> Unit = {}
) {
    val userItems by viewModel.userItems.collectAsState()
    val imagesMap by viewModel.marketplaceImages.collectAsState()
    val item = userItems.find { it.id == itemId }
    var isLoading by remember { mutableStateOf(item == null && itemId != -1) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var triggerDelete by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(itemId) {
        Log.d("SellerView", "Composing with itemId: $itemId, current item: $item")
        if (item == null && itemId != -1) {
            Log.d("SellerView", "Item $itemId not found locally, fetching from API...")
            isLoading = true
            viewModel.fetchItemById(itemId)?.let { fetchedItem ->
                Log.d("SellerView", "Fetched item: $fetchedItem")
            } ?: run {
                errorMessage = "Failed to load item $itemId"
                Log.e("SellerView", "Failed to fetch item $itemId")
            }
            viewModel.fetchUserListedItems()
            isLoading = false
        }
        if (imagesMap[itemId]?.isEmpty() != false) {
            viewModel.getMarketplaceItemImages(itemId)
        }
        Log.d("SellerView", "userItems after fetch: ${userItems.map { it.id to it.title }}")
        Log.d("SellerView", "imagesMap after fetch: ${imagesMap[itemId]}")
    }

    LaunchedEffect(imagesMap[itemId]) {
        imagesMap[itemId]?.forEach { imageUrl ->
            scope.launch {
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .build()
                context.imageLoader.enqueue(request)
            }
        }
    }

    LaunchedEffect(triggerDelete) {
        if (triggerDelete) {
            viewModel._errorMessage.collect { error ->
                if (error == null) {
                    onClose()
                    onRefreshListedItems()
                } else {
                    errorMessage = error
                }
                triggerDelete = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${item?.title ?: "Loading..."} details",
                        fontFamily = fontFamily, color = Color.White, fontSize = 24.sp
                    )
                },
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
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
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
                .padding(
                    horizontal = if (isWideScreen) 32.dp else 16.dp,
                    vertical = 16.dp
                )
        ) {
            when {
                isLoading && item == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                item != null -> {
                    Column {
                        val images = imagesMap[itemId]
                        when {
                            images?.isNotEmpty() == true -> {
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isWideScreen) 400.dp else 300.dp)
                                ) {
                                    items(images) { imageUrl ->
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = "Item Image",
                                            modifier = Modifier
                                                .width(if (isWideScreen) 400.dp else 300.dp)
                                                .fillMaxHeight()
                                                .padding(end = 8.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop,
                                            placeholder = painterResource(id = R.drawable.baseline_image_24),
                                            error = painterResource(id = R.drawable.baseline_error_24)
                                        )
                                    }
                                }
                            }
                            !item.image_url.isNullOrEmpty() -> {
                                AsyncImage(
                                    model = item.image_url,
                                    contentDescription = "Item Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isWideScreen) 400.dp else 300.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(id = R.drawable.baseline_image_24),
                                    error = painterResource(id = R.drawable.baseline_error_24)
                                )
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isWideScreen) 400.dp else 300.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "₱${item.price}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Red,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            maxLines = if (isWideScreen) 10 else 5,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { navController.navigate(NavRoutes.EditMarketplaceItem.createRoute(itemId)) },
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
                                onClick = { showDeleteDialog = true },
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

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Confirm Delete") },
                    text = { Text("Are you sure you want to delete this item? This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteItem(itemId, context)
                                triggerDelete = true
                                showDeleteDialog = false
                            }
                        ) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDeleteDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 8.dp)
                )
            }
            viewModel._errorMessage.collectAsState().value?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 8.dp)
                )
            }
        }
    }
}