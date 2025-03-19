package com.example.raceconnect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.view.Navigation.BottomNavTab
import com.example.raceconnect.view.Navigation.NavRoutes

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavTab.NewsFeed,
        BottomNavTab.Friends,
        BottomNavTab.Marketplace,
        BottomNavTab.Notifications,
        BottomNavTab.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 2.dp),
        containerColor = Color.White
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp),
                            tint = if (currentRoute == item.route) Color(0xFF8B0000) else MaterialTheme.colorScheme.onSurface
                        )
                        // Add the underline for the selected item
                        if (currentRoute == item.route) {
                            Spacer(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(1.dp)
                                    .background(Color(0xFF8B0000))
                            )
                        }
                    }
                },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(NavRoutes.NewsFeed.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF8B0000),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedTextColor = Color(0xFF8B0000),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
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