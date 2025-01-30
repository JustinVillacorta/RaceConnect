package com.example.raceconnect.view.Screens

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

    // Observe login state
    val loggedInUser by viewModel.loggedInUser
    val errorMessage by viewModel.errorMessage

    // Redirect to NewsFeedActivity on successful login
    LaunchedEffect(loggedInUser) {
        Log.d("AuthenticationNavHost", "LaunchedEffect triggered with loggedInUser: $loggedInUser")
        loggedInUser?.let { user ->
            Log.d("AuthenticationNavHost", "Navigating to NewsFeedActivity with user: ${user.username}")
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
                onSignupClick = { email, password ->
                    Toast.makeText(context, "Signup not implemented. Please use login.", Toast.LENGTH_SHORT).show()
                },
                onBackNavigate = {
                    navController.navigate("login")
                }
            )
        }
    }

    errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
