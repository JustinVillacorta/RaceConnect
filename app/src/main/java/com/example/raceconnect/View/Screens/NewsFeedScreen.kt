package com.example.raceconnect.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class) // Needed for Material3 components like Scaffold
@Composable
fun NewsFeedScreen() {
    Scaffold(
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                Text(
                    text = "Welcome to the News Feed",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    )
}
