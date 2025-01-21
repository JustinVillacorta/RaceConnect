package com.example.raceconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.Controller.validateLogin
import com.example.raceconnect.Controller.validateSignup
import com.example.raceconnect.ui.LoginScreen
import com.example.raceconnect.ui.SignupScreen

class AuthenticationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuthenticationNavHost()
        }
    }
}

@Composable
fun AuthenticationNavHost() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "login") {
        // Login Screen
        composable("login") {
            LoginScreen(
                onLoginClick = { email, password ->
                    // Validate login
                    if (validateLogin(email, password)) {
                        // If valid, navigate to NewsFeedActivity
                        val intent = Intent(navController.context, NewsFeedActivity::class.java)
                        startActivity(navController.context, intent, null)
                    } else {
                        // Show an error message if validation fails
                        Toast.makeText(
                            navController.context,
                            "Invalid login credentials. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onSignupNavigate = {
                    navController.navigate("signup")
                }
            )
        }

        // Signup Screen
        composable("signup") {
            SignupScreen(
                onSignupClick = { email, password ->
                    // Validate signup
                    if (validateSignup(email, password)) {
                        // If valid, navigate to NewsFeedActivity
                        val intent = Intent(navController.context, NewsFeedActivity::class.java)
                        startActivity(navController.context, intent, null)
                    } else {
                        // Show an error message if validation fails
                        Toast.makeText(
                            navController.context,
                            "Invalid signup details. Password must be at least 6 characters long.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onBackNavigate = {
                    navController.navigate("login")
                }
            )
        }
    }
}
