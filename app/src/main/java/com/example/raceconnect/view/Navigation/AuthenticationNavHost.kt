package com.example.raceconnect.view.Navigation

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.ui.LoginScreen
import com.example.raceconnect.ui.SignupScreen
import com.example.raceconnect.viewmodel.Authentication.AuthenticationViewModel

@Composable
fun AuthenticationNavHost(viewModel: AuthenticationViewModel = viewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val loggedInUser by viewModel.loggedInUser.collectAsState(initial = null)
    val errorMessage by viewModel.errorMessage.collectAsState(initial = null)

    NavHost(navController, startDestination = NavRoutes.Login.route) {
        composable(NavRoutes.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                onLoginClick = { username, password ->
                    viewModel.validateLogin(username, password)
                },
                onSignupNavigate = { navController.navigate(NavRoutes.Signup.route) }
            )
        }

        composable(NavRoutes.Signup.route) {
            SignupScreen(
                onSignupClick = { ctx, username, email, password, onSignupSuccess ->
                    viewModel.signUp(ctx, username, email, password) { message ->
                        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
                        if (message.contains("success", ignoreCase = true)) onSignupSuccess()
                    }
                },
                onBackNavigate = { navController.navigateUp() }
            )
        }
    }

    errorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearError() // Clear error to prevent repeated toasts
        }
    }
}
