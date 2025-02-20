package com.example.raceconnect.view.Screens

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    // Observe login state and error message using collectAsState
    val loggedInUser by viewModel.loggedInUser.collectAsState(initial = null)
    val errorMessage by viewModel.errorMessage.collectAsState(initial = null)

    // Redirect to NewsFeedActivity on successful login
    LaunchedEffect(loggedInUser) {
        Log.d("AuthenticationNavHost", "LaunchedEffect triggered with loggedInUser: $loggedInUser")
        loggedInUser?.let { user ->
            Log.d(
                "AuthenticationNavHost",
                "Navigating to NewsFeedActivity with user: ${user.username}"
            )
            val intent = Intent(context, NewsFeedActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }
    }

    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginClick = { username, password ->
                    Log.d("AuthenticationNavHost", "Login clicked with username: $username")
                    viewModel.validateLogin(username, password)
                },
                onSignupNavigate = {
                    navController.navigate("signup")
                }
            )
        }

        composable("signup") {
            SignupScreen(
                onSignupClick = { username, email, password ->
                    Log.d(
                        "AuthenticationNavHost",
                        "Signup clicked with username: $username, email: $email"
                    )
                    viewModel.signUp(username, email, password)
                },
                onBackNavigate = {
                    navController.navigate("login")
                }
            )
        }

        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
