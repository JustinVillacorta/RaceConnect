package com.example.raceconnect.view.Screens.MarketplaceScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceItemDetailScreen(
    itemId: Int,
    navController: NavController,
    viewModel: MarketplaceViewModel,
    onClose: () -> Unit,
    onClickChat: (Int) -> Unit = { navController.navigate(NavRoutes.ChatSeller.createRoute(it)) },
    onLikeError: (String) -> Unit
) {
    val marketplaceItems by viewModel.marketplaceItems.collectAsState()
    val imagesMap by viewModel.marketplaceImages.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val item = marketplaceItems.find { it.id == itemId }
    val itemImages = imagesMap[itemId] ?: emptyList()
    val liked = isLiked[itemId] ?: false
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(itemId) {
        viewModel.getMarketplaceItemImages(itemId)
        viewModel.fetchLikeStatus(itemId)
    }

    if (item == null) {
        Text("Item not found", modifier = Modifier.padding(16.dp))
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp
        val isWideScreen = screenWidthDp > 600

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(
                    horizontal = if (isWideScreen) 32.dp else 16.dp,
                    vertical = 16.dp
                )
        ) {
            if (itemImages.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isWideScreen) 400.dp else 300.dp)
                ) {
                    items(itemImages) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Marketplace Item Detail Image",
                            modifier = Modifier
                                .width(if (isWideScreen) 400.dp else 300.dp)
                                .fillMaxHeight()
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {
                AsyncImage(
                    model = item.image_url?.takeIf { it.isNotEmpty() } ?: "https://via.placeholder.com/150",
                    contentDescription = "Marketplace Item Detail Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isWideScreen) 400.dp else 300.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
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
                color = MaterialTheme.colorScheme.primary,
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
                    onClick = {
                        coroutineScope.launch {
                            try {
                                viewModel.toggleLike(itemId)
                            } catch (e: Exception) {
                                onLikeError("Failed to toggle favorite: ${e.message}")
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(56.dp)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (liked) MaterialTheme.colorScheme.secondary else Color(0xFFB71C1C),
                        contentColor = if (liked) MaterialTheme.colorScheme.onSecondary else Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (liked) "Remove from\nFavorites" else "Add to\nFavorites",
                            fontSize = 14.sp,
                            maxLines = 2, // Allow two lines
                            textAlign = TextAlign.Center // Center the text
                        )
                    }
                }

                Button(
                    onClick = { onClickChat(itemId) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(start = 8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Chat Seller",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Chat Seller", fontSize = 14.sp)
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