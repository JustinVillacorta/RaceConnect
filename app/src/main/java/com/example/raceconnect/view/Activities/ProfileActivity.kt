package com.example.raceconnect.view.Activities

import ProfileScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.ui.LoginScreen
import com.example.raceconnect.view.Screens.MainScreen


class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            NavHost(navController, startDestination = "profile") {

                // ✅ Profile Screen with Logout Redirection
                composable("profile") {
                    ProfileScreen(
                        onLogoutSuccess = {
                            navController.navigate("login") {
                                popUpTo("profile") { inclusive = true } // ✅ Clears profile from backstack
                            }
                        }
                    )
                }

                // ✅ Login Screen (Ensure it's reachable)
                composable("login") {
                    LoginScreen(
                        onLoginClick = { username, password ->
                            // Handle login logic
                        },
                        onSignupNavigate = { navController.navigate("signup") },
                        onForgotPasswordNavigate = { navController.navigate("forgot_password") }
                    )
                }
            }
        }
    }
}


