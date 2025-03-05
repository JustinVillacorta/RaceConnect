package com.example.raceconnect.view.Screens.NewsFeedScreens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter



@Composable
fun AddPostSection(
    navController: NavController,
    onAddPostClick: () -> Unit,
    onShowProfileView: () -> Unit // New callback to show ProfileViewScreen
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onAddPostClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(48.dp) // Adjusted size to account for padding
                .padding(4.dp)
                .clickable { onShowProfileView() } // Use the new callback
        )
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(viewModel: NewsFeedViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    var postText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCategory by remember { mutableStateOf("Formula 1") } // Default category
    var selectedPrivacy by remember { mutableStateOf("Public") } // Default privacy (UI-only)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Categories for the dropdown (using enum display names)
    val categories = listOf("Formula 1", "24 Hours of Le Mans", "World Rally Championship")
    // Privacy options for the dropdown (UI-only: Public and Friends Only)
    val privacyOptions = listOf("Public", "Friends Only")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // Use theme background color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Enable scrolling for the entire content
                .padding(horizontal = 8.dp) // Reduced horizontal padding
        ) {
            // TopBar integrated into the scrollable content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Create Post",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = {
                        if (postText.isNotEmpty()) {
                            viewModel.addPost(context, postText, selectedImageUri, selectedCategory, selectedPrivacy)
                            onClose()
                        }
                    },
                    enabled = postText.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), // Use theme primary color
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Post", color = MaterialTheme.colorScheme.onPrimary) // Use theme onPrimary color
                }
            }

            // Profile Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(36.dp) // Slightly smaller for a more compact look
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) // Use theme surface variant with transparency
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Anonymous",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                )
            }

            // Dropdowns for Categories and Privacy (Smaller and more rounded, no explicit colors)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center, // Center the dropdowns
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Dropdown
                var categoryExpanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .width(100.dp) // Smaller width for a compact look
                        .height(32.dp) // Smaller height for a compact look
                        .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp)) // Use theme primary color, more rounded
                        .clickable { categoryExpanded = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedCategory,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimary),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown Arrow",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp) // Smaller icon for compact look
                        )
                    }
                }
                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface) // Use theme surface color
                        .width(100.dp)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        )
                    }
                }

                // Privacy Dropdown
                var privacyExpanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .width(100.dp) // Smaller width for a compact look
                        .height(32.dp) // Smaller height for a compact look
                        .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp)) // Use theme primary color, more rounded
                        .clickable { privacyExpanded = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedPrivacy,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimary),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown Arrow",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp) // Smaller icon for compact look
                        )
                    }
                }
                DropdownMenu(
                    expanded = privacyExpanded,
                    onDismissRequest = { privacyExpanded = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface) // Use theme surface color
                        .width(100.dp)
                ) {
                    privacyOptions.forEach { privacy ->
                        DropdownMenuItem(
                            text = { Text(privacy, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                selectedPrivacy = privacy
                                privacyExpanded = false
                            },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        )
                    }
                }
            }

            // TextField without outline, auto-adjusting height
            TextField(
                value = postText,
                onValueChange = { postText = it },
                placeholder = { Text("What's on your mind?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight() // Auto-adjust height based on content
                    .background(Color.Transparent), // No background
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent, // Remove underline
                    unfocusedIndicatorColor = Color.Transparent, // Remove underline
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent, // Ensure no container color
                    unfocusedContainerColor = Color.Transparent // Ensure no container color
                )
            )

            // Fixed-size Image Preview
            if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // Fixed height like Facebook
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop // Crop to fit like Facebook
                )
            }

            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                contentPadding = PaddingValues(12.dp)
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = "Pick Image",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Photo", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Add bottom padding to prevent content from being cut off
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}