package com.example.raceconnect.view.Screens.MarketplaceScreens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModel
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMarketplaceItemScreen(
    userPreferences: UserPreferences,
    onClose: () -> Unit,
    viewModel: MarketplaceViewModel = viewModel(factory = MarketplaceViewModelFactory(userPreferences))
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val currentUserId by viewModel.currentUserId.collectAsState()
    val user by userPreferences.user.collectAsState(initial = null)

    // Define categories for the dropdown
    val categories = listOf("Formula 1", "24 Hours of Lemans", "World Rally Championship", "NASCAR", "Formula Drift", "GT Championship")

    // Image picker launcher for multiple images
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri>? ->
        if (uris != null) {
            // Append new images to the existing list, ensuring no duplicates
            selectedImageUris = (selectedImageUris + uris).distinct()
        }
    }

    // Get the profile picture URL from UserPreferences (assuming it's stored there)
    val profilePictureUrl = user?.profilePicture ?: "https://via.placeholder.com/150" // Fallback if null
    val isFormComplete = title.isNotEmpty() &&
            price.isNotEmpty() &&
            description.isNotEmpty() &&
            category.isNotEmpty() &&
            selectedImageUris.isNotEmpty() &&
            currentUserId != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Listing", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.addMarketplaceItemWithImages(
                                context = context,
                                title = title,
                                price = price,
                                description = description,
                                category = category,
                                imageUris = selectedImageUris
                            )
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        enabled = isFormComplete // Enable only when all fields are complete
                    ) {
                        Text("Publish", color = Color.White)
                    }
                }
            )
        },
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    // Seller Profile Section (Using saved profile picture)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(profilePictureUrl),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = user?.username ?: "User",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Add Photos Section
                    OutlinedTextField(
                        value = "Photos: ${selectedImageUris.size}/10",
                        onValueChange = {},
                        label = { Text("Add photos") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { launcher.launch("image/*") }
                            .padding(bottom = 8.dp),
                        enabled = false,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Add Photo",
                                modifier = Modifier.clickable { launcher.launch("image/*") },
                                tint = Red
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Image Previews (using LazyRow for horizontal scrolling)
                    if (selectedImageUris.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 0.dp)
                        ) {
                            items(selectedImageUris) { uri ->
                                Box(
                                    modifier = Modifier
                                        .width(150.dp) // Increased width
                                        .aspectRatio(4f / 3f) // Set 4:3 aspect ratio
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(uri),
                                        contentDescription = "Selected Image",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = {
                                            // Remove the image from the list
                                            selectedImageUris = selectedImageUris.filter { it != uri }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            .zIndex(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove Image",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Title Field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.Gray,  // Set to same as unfocused to remove highlight
                            unfocusedBorderColor = Color.Gray, // Default border color
                            focusedLabelColor = Color.Black,    // Label color when focused
                            unfocusedLabelColor = Color.Black   // Same as focused to disable highlight
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Price Field
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.Gray,  // Set to same as unfocused to remove highlight
                            unfocusedBorderColor = Color.Gray, // Default border color
                            focusedLabelColor = Color.Black,    // Label color when focused
                            unfocusedLabelColor = Color.Black   // Same as focused to disable highlight
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Category Dropdown
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color.Gray,  // Set to same as unfocused to remove highlight
                                unfocusedBorderColor = Color.Gray, // Default border color
                                focusedLabelColor = Color.Black,    // Label color when focused
                                unfocusedLabelColor = Color.Black   // Same as focused to disable highlight
                            ),
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { categoryOption ->
                                DropdownMenuItem(
                                    text = { Text(categoryOption) },
                                    onClick = {
                                        category = categoryOption
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Description Field
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .heightIn(min = 100.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.Gray,  // Set to same as unfocused to remove highlight
                            unfocusedBorderColor = Color.Gray, // Default border color
                            focusedLabelColor = Color.Black,    // Label color when focused
                            unfocusedLabelColor = Color.Black   // Same as focused to disable highlight
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    )
}