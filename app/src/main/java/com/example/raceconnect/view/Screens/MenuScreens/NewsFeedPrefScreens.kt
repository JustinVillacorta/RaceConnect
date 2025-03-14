package com.example.raceconnect.view.Screens.MenuScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.raceconnect.R
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedPreference.NewsFeedPreferenceViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedPreference.NewsFeedPreferenceViewModelFactory

data class Brand(val name: String, val iconResId: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedPreferencesScreen(
    navController: NavController,
    onClose: () -> Unit,
    factory: NewsFeedPreferenceViewModelFactory
) {
    val viewModel: NewsFeedPreferenceViewModel = viewModel(factory = factory)
    val selectedBrands by viewModel.selectedBrands.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val brandRed = Color(0xFFC62828)
    val white = Color.White

    val availableBrands = listOf(
        Brand("Formula 1", R.drawable.f1),
        Brand("24H le mans", R.drawable.lemans),
        Brand("Formula drift", R.drawable.formuladrift),
        Brand("WRC", R.drawable.wrc),
        Brand("NASCAR", R.drawable.nascar),
        Brand("GT CUP", R.drawable.gt_championship),
    )

    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isEditable by remember { mutableStateOf(false) } // New state for edit mode

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("News Feed Preferences", color = white) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = white
                        )
                    }
                },
                actions = {
                    Text(
                        text = if (isEditable) "Save" else "Edit",
                        color = white,
                        modifier = Modifier
                            .clickable {
                                if (isEditable) {
                                    viewModel.savePreferences()
                                    isEditable = false
                                } else {
                                    isEditable = true
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = brandRed)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Prioritize Who to See First",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            )
            Text(
                text = "Choose whose posts you don’t want to miss...",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (selectedBrands.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(selectedBrands) { brandName ->
                        val brand = availableBrands.find { it.name == brandName }
                        if (brand != null) {
                            BrandChip(
                                brand = brand,
                                onRemove = if (isEditable) { { viewModel.toggleBrand(brandName) } } else null
                            )
                        }
                    }
                }
            }

            // Show add button and search only when in edit mode
            if (isEditable) {
                if (!showSearchBar) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(brandRed)
                            .clickable { showSearchBar = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add brand",
                            tint = white,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (showSearchBar) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        placeholder = { Text("Search") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search icon",
                                tint = Color.Gray
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close search",
                                tint = Color.Gray,
                                modifier = Modifier.clickable {
                                    searchQuery = ""
                                    showSearchBar = false
                                }
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    val filteredBrands = availableBrands.filter {
                        it.name.lowercase().contains(searchQuery.lowercase()) && it.name !in selectedBrands
                    }
                    if (filteredBrands.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredBrands) { brand ->
                                BrandChip(
                                    brand = brand,
                                    onClick = {
                                        viewModel.toggleBrand(brand.name)
                                        searchQuery = ""
                                        showSearchBar = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Show remove all button only in edit mode
            if (isEditable) {
                Button(
                    onClick = { viewModel.clearPreferences() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = brandRed
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Text(
                        text = "Remove All Preferences",
                        color = brandRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun BrandChip(
    brand: Brand,
    onClick: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = brand.iconResId),
            contentDescription = brand.name,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(4.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = brand.name,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp
        )
        if (onRemove != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove ${brand.name}",
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove.invoke() },
                tint = Color.Gray
            )
        } else if (onClick != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add",
                color = Color(0xFFC62828),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable { onClick.invoke() }
            )
        }
    }
}