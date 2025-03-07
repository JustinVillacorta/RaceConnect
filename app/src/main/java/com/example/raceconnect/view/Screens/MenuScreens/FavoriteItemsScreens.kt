package com.example.raceconnect.view.Screens.MenuScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.raceconnect.R
import com.example.raceconnect.viewmodel.MenuViewModel.MenuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteItemsScreen(
    navController: NavController,
    onClose: () -> Unit,
    menuViewModel: MenuViewModel
) {
    // Example data; replace with real items from your MenuViewModel
    val favoriteItems = listOf(
        FavoriteItem(
            name = "Men's NASCAR JH Design Black Raptor Uniform Jacket",
            price = "₱8,510",
            imageRes = R.drawable.raceconnectlogo // Replace with your drawable
        ),
        FavoriteItem(
            name = "24H DU MANS 1959 Poster Men's T-shirt - blue",
            price = "₱1,675",
            imageRes = R.drawable.raceconnectlogo // Replace with your drawable
        )
    )

    // State for the dropdown
    var expanded by remember { mutableStateOf(false) }
    val filterOptions = listOf("All items", "Jackets", "T-shirts")
    var selectedOption by remember { mutableStateOf(filterOptions[0]) }

    Scaffold(
        topBar = {
            // Red top bar with white text and a back arrow
            SmallTopAppBar(
                title = { Text("Favorite Items", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFFC62828), // Red background
                    titleContentColor = Color.White
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 1) Dropdown filter (Exposed Dropdown Menu)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedOption,
                    onValueChange = { /* no-op, read-only */ },
                    label = { Text("All items") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor() // needed for M3 ExposedDropdownMenuBox
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    filterOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedOption = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2) Grid of favorite items
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(favoriteItems.size) { index ->
                    FavoriteItemCard(item = favoriteItems[index])
                }
            }
        }
    }
}

@Composable
fun FavoriteItemCard(item: FavoriteItem) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Item image
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Name
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp)
            )

            // Price with red background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFC62828))
                    .padding(vertical = 4.dp, horizontal = 8.dp),
            ) {
                Text(
                    text = item.price,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Example data class for an item(to be changed)
data class FavoriteItem(
    val name: String,
    val price: String,
    val imageRes: Int
)
