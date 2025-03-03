package com.example.raceconnect.view.Screens.Authentication

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
import com.example.raceconnect.view.Navigation.NewsFeedActivity
import com.example.raceconnect.viewmodel.Authentication.AuthenticationViewModel
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
                },
                onForgotPasswordNavigate = {
                    navController.navigate("forgot_password")
                }
            )
        }

        composable("signup") {
            SignupScreen(
                onSignupClick = { ctx, username, email, password, onSignupSuccess ->
                    Log.d("AuthenticationNavHost", "Signup clicked with username: $username, email: $email")

                    viewModel.signUp(ctx, username, email, password) { message ->
                        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
                        if (message.contains("success", ignoreCase = true)) { // ✅ Check for success message
                            onSignupSuccess() // ✅ Navigate to login after success
                        }
                    }
                },
                onBackNavigate = {
                    navController.navigate("login")
                }
            )
        }




    // ✅ Forgot Password Screen (Request OTP)
        composable("forgot_password") {
            ForgotPasswordScreen(viewModel) { email ->
                navController.navigate("verify_otp/$email") // ✅ Pass email
            }
        }

        composable("verify_otp/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            OtpVerificationScreen(viewModel, email) {
                navController.navigate("reset_password/$email") // ✅ Pass email forward
            }
        }

        composable("reset_password/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ResetPasswordScreen(viewModel, email) {
                navController.navigate("login") // ✅ Redirect after reset
            }
        }

        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}



