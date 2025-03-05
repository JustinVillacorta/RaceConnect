package com.example.raceconnect.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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



@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavTab.NewsFeed,
        BottomNavTab.Friends,
        BottomNavTab.Marketplace,
        BottomNavTab.Notifications,
        BottomNavTab.Profile
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp) // Standard Material Design height for bottom navigation
            .padding(horizontal = 8.dp), // Minimal horizontal padding for responsiveness
        containerColor = Color.White // Background color of the Bottom Nav
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp), // Standard icon size for compact design
                        tint = if (currentRoute == item.route) Color(0xFF8B0000) else MaterialTheme.colorScheme.onSurface // Red for selected, gray for unselected
                    )
                },
                // Removed label to match icon-only design in screenshot
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF8B0000), // Red for selected icon
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface, // Gray for unselected icons
                    selectedTextColor = Color(0xFF8B0000), // Not used since labels are removed
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface // Not used since labels are removed
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