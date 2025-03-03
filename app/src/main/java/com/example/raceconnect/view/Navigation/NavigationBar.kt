package com.example.raceconnect.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.view.Navigation.BottomNavTab
import com.example.raceconnect.view.ui.theme.Red


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(navController: NavController) {
    TopAppBar(
//        modifier = Modifier
//            .height(50.dp) // Increase height to 72.dp (adjust as needed)
//            .padding(horizontal = 16.dp), // Add horizontal padding for spacing
        title = {
            Text(text = "RaceConnect", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimary)
        },
        actions = {
            IconButton(onClick = { /* Handle search */ }) {
                Icon(
                    painter = painterResource(id = com.example.raceconnect.R.drawable.baseline_search_24),
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Red,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}



@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavTab.NewsFeed,
        BottomNavTab.Marketplace,
        BottomNavTab.Notifications,
        BottomNavTab.Profile
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.White // Background color of the Bottom Nav
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White, // Active icon color
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = Color.DarkGray // Selected item background color
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBottomNavBar() {
    val navController = rememberNavController()
    BottomNavBar(navController = navController)
}