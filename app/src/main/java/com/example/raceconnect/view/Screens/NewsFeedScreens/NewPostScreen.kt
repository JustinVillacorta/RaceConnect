package com.example.raceconnect.view.Screens.NewsFeedScreens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel

@Composable
fun AddPostSection(
    navController: NavController,
    onAddPostClick: () -> Unit,
    onShowProfileView: () -> Unit
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val user by userPreferences.user.collectAsState(initial = null)

    val profilePictureUri = user?.profilePicture?.let {
        try {
            Uri.parse(it)
        } catch (e: Exception) {
            null
        }
    }

    // Wrap the entire section in a Card to create the container effect
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp) // Padding inside the card
        ) {
            // Original AddPostSection content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable(onClick = onAddPostClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (profilePictureUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = profilePictureUri,
                            error = painterResource(id = android.R.drawable.ic_menu_gallery),
                            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                        ),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .clickable { onShowProfileView() },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .clickable { onShowProfileView() }
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFF8F8F8))
                        .border(
                            width = 1.dp,
                            color = Color.LightGray,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable(onClick = onAddPostClick),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "What's new today?",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(viewModel: NewsFeedViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    var postText by remember { mutableStateOf("") }
    var postTitle by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("Formula 1") }
    var selectedPrivacy by remember { mutableStateOf("Public") }

    // Use GetMultipleContents to select multiple images
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri>? ->
        selectedImageUris = uris ?: emptyList()
    }

    val userPreferences = remember { UserPreferences(context) }
    val user by userPreferences.user.collectAsState(initial = null)

    val categories = listOf("Formula 1", "24 Hours of Lemans", "World Rally Championship", "NASCAR", "Formula Drift", "GT Championship")
    val privacyOptions = listOf("Public", "Friends Only", "Only me")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // TopBar (unchanged)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Create Post",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = {
                        if (postText.isNotEmpty() && postTitle.isNotEmpty()) {
                            viewModel.addPost(context, postText, postTitle, selectedImageUris, selectedCategory, selectedPrivacy)
                            onClose()
                        }
                    },
                    enabled = postText.isNotEmpty() && postTitle.isNotEmpty(),
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Post")
                }
            }
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            )

            // Content below with padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Profile Section (unchanged)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    val profilePictureUri = user?.profilePicture?.let { Uri.parse(it) }
                    if (profilePictureUri != null) {
                        val painter = rememberAsyncImagePainter(model = profilePictureUri)
                        Image(
                            painter = painter,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = user?.username ?: "Anonymous",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Title TextField (unchanged)
                TextField(
                    value = postTitle,
                    onValueChange = { postTitle = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                // Dropdowns (unchanged)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    var categoryExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category", style = MaterialTheme.typography.labelMedium) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .height(48.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier = Modifier
                                .width(IntrinsicSize.Min)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        selectedCategory = category
                                        categoryExpanded = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    var privacyExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = privacyExpanded,
                        onExpandedChange = { privacyExpanded = !privacyExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = selectedPrivacy,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Privacy", style = MaterialTheme.typography.labelMedium) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = privacyExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .height(48.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = privacyExpanded,
                            onDismissRequest = { privacyExpanded = false },
                            modifier = Modifier
                                .width(IntrinsicSize.Min)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        ) {
                            privacyOptions.forEach { privacy ->
                                DropdownMenuItem(
                                    text = { Text(privacy, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        selectedPrivacy = privacy
                                        privacyExpanded = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // TextField
                OutlinedTextField(
                    value = postText,
                    onValueChange = { postText = it },
                    placeholder = { Text("What's on your mind?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color.Transparent, shape = RoundedCornerShape(8.dp)),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    )
                )
                Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                        .padding(vertical = 2.dp)
                )

                // Image Preview for Multiple Images
                if (selectedImageUris.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        items(selectedImageUris) { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Selected Image",
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(end = 8.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Add Photo Button
                OutlinedButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Pick Images")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Photos")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}