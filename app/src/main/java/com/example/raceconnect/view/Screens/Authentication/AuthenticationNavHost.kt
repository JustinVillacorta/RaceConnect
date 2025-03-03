package com.example.raceconnect.view.Screens.Authentication

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.ui.LoginScreen
import com.example.raceconnect.ui.SignupScreen
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.viewmodel.Authentication.AuthenticationViewModel

@Composable
fun AuthenticationNavHost(viewModel: AuthenticationViewModel = viewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val loggedInUser by viewModel.loggedInUser.collectAsState(initial = null)
    val errorMessage by viewModel.errorMessage.collectAsState(initial = null)

    // Removed LaunchedEffect with Intent; AppNavigation will handle the transition

    NavHost(navController, startDestination = NavRoutes.Login.route) {
        composable(NavRoutes.Login.route) {
            LoginScreen(
                onLoginClick = { username, password -> viewModel.validateLogin(username, password) },
                onSignupNavigate = { navController.navigate(NavRoutes.Signup.route) },
                onForgotPasswordNavigate = { navController.navigate(NavRoutes.ForgotPassword.route) }
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

        composable(NavRoutes.ForgotPassword.route) {
            ForgotPasswordScreen(viewModel) { email ->
                navController.navigate(NavRoutes.VerifyOtp.createRoute(email))
            }
        }

        composable(NavRoutes.VerifyOtp.route) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            OtpVerificationScreen(viewModel, email) {
                navController.navigate(NavRoutes.ResetPassword.createRoute(email))
            }
        }

        composable(NavRoutes.ResetPassword.route) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ResetPasswordScreen(viewModel, email) {
                navController.navigate(NavRoutes.Login.route)
            }
        }
    }

    errorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearError() // Clear error to prevent repeated toasts
        }
    }
}