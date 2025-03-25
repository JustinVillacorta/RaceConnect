package com.example.raceconnect.view.Screens.MarketplaceScreens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun MarketplaceItemDetailScreen(
    itemId: Int,
    navController: NavController,
    userPreferences: UserPreferences,
    onLikeError: (String) -> Unit,
    onMessageSent: (String) -> Unit
) {
    val viewModel: MarketplaceViewModel = viewModel(
        factory = MarketplaceViewModelFactory(userPreferences)
    )
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val user by userPreferences.user.collectAsState(initial = null)
    val currentUserId = user?.id

    val item = remember { mutableStateOf<MarketplaceDataClassItem?>(null) }
    LaunchedEffect(itemId) {
        item.value = viewModel.fetchItemById(itemId)
    }

    val itemImages by viewModel.marketplaceImages.collectAsState()
    val imagesForItem = itemImages[itemId] ?: listOf(item.value?.image_url ?: "")

    val liked by viewModel.isLiked.collectAsState()
    val isLiked = liked[itemId] ?: false

    val errorMessage by viewModel.errorMessage.collectAsState()
    val messageSentStatus by viewModel.messageSentStatus.collectAsState()

    val conversationState by viewModel.conversationExists.collectAsState()
    val (conversationExists, conversationId) = conversationState ?: Pair(false, null)

    LaunchedEffect(item.value, currentUserId) {
        if (item.value != null && currentUserId != null) {
            viewModel.checkConversationExists(
                buyerId = currentUserId,
                sellerId = item.value!!.seller_id,
                productId = itemId
            )
        }
    }

    LaunchedEffect(messageSentStatus) {
        if (messageSentStatus == "Message sent successfully" && viewModel.lastConversationId.value != null) {
            navController.navigate(
                NavRoutes.ChatSeller.createRoute(
                    itemTitle = item.value?.title ?: "",
                    itemId = itemId,
                    itemImage = imagesForItem.firstOrNull() ?: item.value?.image_url ?: "",
                    conversationId = viewModel.lastConversationId.value!!,
                    sellerId = item.value?.seller_id ?: 0
                )
            )
            viewModel.clearMessageSentStatus()
        }
    }

    val isWideScreen = LocalConfiguration.current.screenWidthDp > 600

    // State for reporting
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("") }
    var otherText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // State for hidden item visibility
    var showHiddenItem by remember(itemId, item.value?.status) { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val isHidden = item.value?.status?.lowercase() == "hidden"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${item.value?.title ?: "Loading..."} details",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showReportDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Report,
                            contentDescription = "Report item",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Red
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        if (item.value == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isSystemInDarkTheme()) Black else Color.White)
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .then(
                                if (isHidden && !showHiddenItem) Modifier.blur(10.dp) else Modifier
                            ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(imagesForItem) { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Item image",
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .width(200.dp)
                                    .background(LightGray),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Text(
                        text = item.value!!.title,
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) Color.White else Black
                        ),
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .then(
                                if (isHidden && !showHiddenItem) Modifier.blur(10.dp) else Modifier
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .then(
                                if (isHidden && !showHiddenItem) Modifier.blur(10.dp) else Modifier
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Price: ₱${item.value!!.price}",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSystemInDarkTheme()) Color.White else Black
                            )
                        )
                        Text(
                            text = item.value!!.listing_status,
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = when (item.value!!.listing_status) {
                                    "Available" -> Color.Green
                                    "Sold" -> Color.Red
                                    else -> if (isSystemInDarkTheme()) LightGray else DarkGray
                                }
                            )
                        )
                    }

                    Text(
                        text = "Category: ${item.value!!.category}",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = if (isSystemInDarkTheme()) LightGray else DarkGray
                        ),
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .then(
                                if (isHidden && !showHiddenItem) Modifier.blur(10.dp) else Modifier
                            )
                    )

                    Text(
                        text = item.value!!.description,
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = if (isSystemInDarkTheme()) Color.White else Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .then(
                                if (isHidden && !showHiddenItem) Modifier.blur(10.dp) else Modifier
                            ),
                        maxLines = if (isWideScreen) 10 else 5,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (conversationExists && conversationId != null) {
                        Button(
                            onClick = {
                                if (currentUserId != null) {
                                    navController.navigate(
                                        NavRoutes.ChatSeller.createRoute(
                                            itemTitle = item.value!!.title,
                                            itemId = itemId,
                                            itemImage = imagesForItem.firstOrNull() ?: item.value!!.image_url ?: "",
                                            conversationId = conversationId!!,
                                            sellerId = item.value!!.seller_id
                                        )
                                    )
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Please log in to view chats",
                                            actionLabel = "Dismiss",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                                .then(
                                    if (isHidden && !showHiddenItem) Modifier.blur(10.dp) else Modifier
                                ),
                            enabled = !isHidden || showHiddenItem,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Red,
                                contentColor = Color.White
                            )
                        ) {
                            Text("See Chat")
                        }
                    } else {
                        Surface(
                            color = Color(0xFFF0F0F0),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                                .then(
                                    if (isHidden && !showHiddenItem) Modifier.blur(10.dp) else Modifier
                                )
                        ) {
                            MessageInputArea(
                                defaultMessage = "Hi, is this still available?",
                                onSendMessage = { message ->
                                    if (currentUserId != null) {
                                        coroutineScope.launch {
                                            viewModel.sendMessage(
                                                buyerId = currentUserId,
                                                sellerId = item.value!!.seller_id,
                                                productId = itemId,
                                                message = message
                                            )
                                            onMessageSent("Message sent")
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Please log in to send a message",
                                                actionLabel = "Dismiss",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .then(
                                if (isHidden && !showHiddenItem) Modifier.blur(10.dp) else Modifier
                            ),
                        enabled = !isHidden || showHiddenItem,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLiked) MaterialTheme.colorScheme.secondary else Color(0xFFB71C1C),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorite",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLiked) "Remove from\nFavorites" else "Add to\nFavorites",
                                fontSize = 14.sp,
                                maxLines = 2,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .then(
                                    if (isHidden && !showHiddenItem) Modifier.blur(10.dp) else Modifier
                                )
                        )
                    }

                    messageSentStatus?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .then(
                                    if (isHidden && !showHiddenItem) Modifier.blur(10.dp) else Modifier
                                )
                        )
                    }

                    // Add "Hide Item" button when hidden item is shown
                    if (isHidden && showHiddenItem) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showHiddenItem = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Hide Item", color = Color.Black)
                        }
                    }
                }

                // Overlay for hidden items
                if (isHidden && !showHiddenItem) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "This item is hidden",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { showConfirmationDialog = true }) {
                                Text("See Item")
                            }
                        }
                    }
                }
            }
        }
    }

    // Report Dialog (unchanged)
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(text = "Report Item") },
            text = {
                Column {
                    Text(text = "Please select a reason for reporting this item:")
                    Spacer(modifier = Modifier.height(16.dp))

                    val reportOptions = listOf("Not related", "Nudity", "Inappropriate", "Others")
                    reportOptions.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedReason = reason },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason }
                            )
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    if (selectedReason == "Others") {
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = otherText,
                            onValueChange = { otherText = it },
                            label = { Text("Please specify") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Text(
                    text = "Confirm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedReason.isNotEmpty() && (selectedReason != "Others" || otherText.isNotEmpty()))
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable {
                            if (selectedReason.isNotEmpty()) {
                                if (selectedReason == "Others" && otherText.isEmpty()) {
                                    Toast.makeText(context, "Please specify the reason", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.reportMarketplaceItem(
                                        marketplaceItemId = itemId,
                                        reason = selectedReason,
                                        otherText = if (selectedReason == "Others") otherText else null,
                                        onSuccess = {
                                            Toast.makeText(context, "Item reported successfully", Toast.LENGTH_SHORT).show()
                                            showReportDialog = false
                                        },
                                        onFailure = { error ->
                                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                        .padding(8.dp)
                )
            },
            dismissButton = {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clickable { showReportDialog = false }
                        .padding(8.dp)
                )
            }
        )
    }

    // Confirmation dialog for revealing hidden item
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("View Hidden Item") },
            text = { Text("Are you sure you want to view this hidden item?") },
            confirmButton = {
                TextButton(onClick = {
                    showHiddenItem = true
                    showConfirmationDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun MessageInputArea(defaultMessage: String, onSendMessage: (String) -> Unit) {
    var message by remember { mutableStateOf(defaultMessage) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "Chat",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Send seller a message",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                placeholder = { Text("Hi, is this still available?", color = Color.Gray) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = {
                    if (message.isNotBlank()) {
                        onSendMessage(message)
                        message = ""
                    }
                },
                shape = CircleShape,
                containerColor = if (message.isNotBlank()) Color(0xFFB71C1C) else Color.Gray,
                contentColor = Color.White,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send"
                )
            }
        }
    }
}