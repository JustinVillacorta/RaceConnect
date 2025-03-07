package com.example.raceconnect.view.Screens.MenuScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.raceconnect.R
import com.example.raceconnect.viewmodel.MenuViewModel.MenuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListedItemsScreen(
    navController: NavController,
    onClose: () -> Unit,
    menuViewModel: MenuViewModel
) {
    // Example data; replace this with real data from your MenuViewModel
    val listedItems = listOf(
        ListedItem(
            name = "Vintage Racer Jacket",
            price = "₱8,510",
            imageRes = R.drawable.raceconnectlogo // Replace with your drawable
        ),
        ListedItem(
            name = "Retro Race T-Shirt",
            price = "₱1,675",
            imageRes = R.drawable.raceconnectlogo // Replace with your drawable
        ),
        // Add more items as needed
    )

    // State for the dropdown filter
    var expanded by remember { mutableStateOf(false) }
    val filterOptions = listOf("All items", "Jackets", "T-shirts")
    var selectedOption by remember { mutableStateOf(filterOptions[0]) }

    Scaffold(
        topBar = {
            // Red top bar with title and a back arrow
            SmallTopAppBar(
                title = { Text("Listed Items", color = Color.White) },
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
                    containerColor = Color(0xFFC62828),
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
            // Exposed Dropdown Menu for filtering items
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedOption,
                    onValueChange = { /* read-only */ },
                    label = { Text("Filter items") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor() // Required for the M3 ExposedDropdownMenuBox
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

            // LazyVerticalGrid for a two-column layout
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(listedItems) { item ->
                    ListedItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun ListedItemCard(item: ListedItem) {
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

            // Item name
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Price with a red background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFC62828))
                    .padding(vertical = 4.dp, horizontal = 8.dp)
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

// Data class for a listed item
data class ListedItem(
    val name: String,
    val price: String,
    val imageRes: Int
)


