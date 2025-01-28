package com.example.raceconnect.view.Screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
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

    // Observe the ViewModel's state
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val loggedInUser by viewModel.loggedInUser

    // Handle navigation to NewsFeedActivity on successful login/signup
    loggedInUser?.let { user ->
        context.startActivity(Intent(context, NewsFeedActivity::class.java))
    }

    NavHost(navController, startDestination = "login") {
        // Login screen
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { user ->
                    viewModel.loggedInUser.value = user // Save the user in ViewModel state
                },
                onSignupNavigate = {
                    navController.navigate("signup")
                }
            )
        }

        // Signup screen
        //composable("signup") {
          //  SignupScreen(
            //    onSignupClick = { email, password ->
                 //   if (viewModel.validateSignup(email, password)) {
                        // Simulate signup success (replace with actual logic if needed)
                     //   viewModel.login(email, password)
                  //  } else {
                    //    Toast.makeText(
                       //     context,
                        //    "Invalid signup details. Password must be at least 6 characters long.",
                        //    Toast.LENGTH_SHORT
                       // ).show()
                  //  }
               // },
               // onBackNavigate = {
               //     navController.navigate("login")
              // }
           // )
       // }
    }

    // Display error messages via Toast
    errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
