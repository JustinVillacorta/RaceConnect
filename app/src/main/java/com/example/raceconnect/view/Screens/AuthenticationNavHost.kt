package com.example.raceconnect.view.Screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.view.Activities.NewsFeedActivity
import com.example.raceconnect.viewmodel.AuthenticationViewModel
import com.example.raceconnect.ui.LoginScreen
import com.example.raceconnect.ui.SignupScreen
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AuthenticationNavHost(viewModel: AuthenticationViewModel = viewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginClick = { email, password ->
                    if (viewModel.validateLogin(email, password)) {
                        // Navigate to NewsFeedActivity
                        context.startActivity(Intent(context, NewsFeedActivity::class.java))
                    } else {
                        Toast.makeText(
                            context,
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

        composable("signup") {
            SignupScreen(
                onSignupClick = { email, password ->
                    if (viewModel.validateSignup(email, password)) {
                        context.startActivity(Intent(context, NewsFeedActivity::class.java))
                    } else {
                        Toast.makeText(
                            context,
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
