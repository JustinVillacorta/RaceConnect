package com.example.raceconnect.view.Screens.MenuScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.raceconnect.R
import com.example.raceconnect.viewmodel.ProfileDetails.MenuViewModel.MenuViewModel

// Placeholder data class for a brand
data class Brand(val name: String, val iconResId: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedPreferencesScreen(navController: NavController, onClose: () -> Unit, menuViewModel: MenuViewModel, ) {

    val brandRed = Color(0xFFC62828) // Your brand's red color
    val white = Color.White

    // Sample list of available brands (you can fetch this from a ViewModel or API)
    val availableBrands = listOf(
        Brand("Formula 1", R.drawable.f1),
        Brand("24H le mans", R.drawable.lemans),
        Brand("Formula drift", R.drawable.formuladrift),
        Brand("WRC", R.drawable.wrc),
        Brand("NASCAR", R.drawable.nascar),
        Brand("GT CUP", R.drawable.gt_championship),
    )

    // State for selected brands (can be moved to ViewModel)
    val selectedBrands = remember { mutableStateListOf<Brand>() }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

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
                        text = "Save",
                        color = white,
                        modifier = Modifier
                            .clickable {
                                // TODO: Save the selected brands (e.g., to ViewModel or DataStore)
                                onClose()
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = brandRed
                )
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
            // Title and description
            Text(
                text = "Prioritize Who to See First",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            )
            Text(
                text = "Choose whose posts you don’t want to miss. Posts from other users and drivers will appear lower in your News Feed.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Display selected brands
            if (selectedBrands.isNotEmpty()) {
                LazyVerticalGrid (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    columns = GridCells.Fixed(2)
                ) {
                    items(selectedBrands) { brand ->
                        BrandChip(
                            brand = brand,
                            onRemove = { selectedBrands.remove(brand) }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Add button
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

            // Search bar for adding new brands
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
                            modifier = Modifier
                                .clickable {
                                    searchQuery = ""
                                    showSearchBar = false
                                }
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                // Display filtered brands
                val filteredBrands = availableBrands.filter {
                    it.name.lowercase().contains(searchQuery.lowercase()) && it !in selectedBrands
                }
                if (filteredBrands.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement =  Arrangement.spacedBy(8.dp),

                    ) {
                        items(filteredBrands) { brand ->
                            BrandChip(
                                brand = brand,
                                onClick = {
                                    selectedBrands.add(brand)
                                    searchQuery = ""
                                    showSearchBar = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Remove From Preferences button
            Button(
                onClick = {
                    selectedBrands.clear()
                    // TODO: Reset preferences (e.g., update ViewModel or DataStore)
                },
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
                    text = "Remove From Preferences",
                    color = brandRed,
                    style = MaterialTheme.typography.bodyMedium
                )
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
        } else {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add",
                color = Color(0xFFC62828),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable { onClick?.invoke() }
            )
        }
    }
}


