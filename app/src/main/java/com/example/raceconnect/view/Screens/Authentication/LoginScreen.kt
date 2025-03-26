package com.example.raceconnect.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raceconnect.R
import com.example.raceconnect.view.Screens.Authentication.AppealDialog
import com.example.raceconnect.view.Screens.Authentication.ForgotPasswordDialog
import com.example.raceconnect.view.Screens.Authentication.OtpVerificationDialog
import com.example.raceconnect.view.Screens.Authentication.ResetPasswordDialog
import com.example.raceconnect.view.ui.theme.fontFamily
import com.example.raceconnect.viewmodel.Authentication.AuthenticationViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthenticationViewModel,
    onLoginClick: (String, String) -> Unit = { username: String, password: String ->
        viewModel.validateLogin(username, password)
    },
    onSignupNavigate: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val errorMessage by viewModel.ErrorMessage.collectAsState()

    // Check for 401 (Unauthorized) error
    val isAuthError = errorMessage?.contains("401") == true ||
            errorMessage?.contains("Unauthorized", ignoreCase = true) == true

    // Check for 400 (Bad Request) error
    val isBadRequestError = errorMessage?.contains("400") == true ||
            errorMessage?.contains("Bad Request", ignoreCase = true) == true

    // Dialog states
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showOtpDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showAppealDialog by remember { mutableStateOf(false) }
    var tempEmail by remember { mutableStateOf("") }

    val appealSubmitted by viewModel.appealSubmitted.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Red header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(color = Color(0xFFC62828))
            ) {
                Text(
                    text = "Log In",
                    fontFamily = fontFamily, color = Color.White, fontSize = 30.sp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(16.dp)
                )
            }

            // Main card
            Card(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 120.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ready, Set, Connect!",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(16.dp)
                    )

                    // Username TextField with conditional red border for 401
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFFC62828)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = isAuthError || isBadRequestError, // Red border for 401 or 400
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            errorBorderColor = Color.Red
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password TextField with conditional red border for 401
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFC62828)
                            )
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color(0xFFC62828)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = isAuthError || isBadRequestError, // Red border for 401 or 400
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            errorBorderColor = Color.Red
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Remember me and Forgot Password row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it }
                            )
                            Text(text = "Remember me", color = Color.Black)
                        }
                        TextButton(
                            onClick = { showForgotPasswordDialog = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Forgot Password?", color = Color(0xFFC62828))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Display error message, overriding for 401 and 400
                    errorMessage?.let {
                        Text(
                            text = when {
                                isAuthError -> "Incorrect username or password"
                                isBadRequestError -> "Enter your username and password"
                                else -> it
                            },
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // Log in button
                    Button(
                        onClick = { onLoginClick(username, password) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC62828),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Log in")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign up link
                    Row {
                        Text(text = "Don't have an account? ", color = Color.Black)
                        Text(
                            text = "Sign Up",
                            color = Color(0xFFC62828),
                            modifier = Modifier.clickable { onSignupNavigate() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Need to submit an appeal?",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable { showAppealDialog = true }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Footer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "RaceConnect Logo",
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Welcome to RaceConnect! Login to manage your account\n" +
                                    "and access exclusive features.",
                            color = Color(0xFFC62828),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Dialogs (unchanged)
    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            viewModel = viewModel,
            onDismiss = { showForgotPasswordDialog = false },
            onOtpSent = { email ->
                showForgotPasswordDialog = false
                tempEmail = email
                showOtpDialog = true
            }
        )
    }

    if (showOtpDialog) {
        OtpVerificationDialog(
            viewModel = viewModel,
            email = tempEmail,
            onDismiss = { showOtpDialog = false },
            onVerified = {
                showOtpDialog = false
                showResetDialog = true
            }
        )
    }

    if (showResetDialog) {
        ResetPasswordDialog(
            viewModel = viewModel,
            email = tempEmail,
            onDismiss = { showResetDialog = false },
            onResetSuccess = { showResetDialog = false }
        )
    }

    if (showAppealDialog) {
        AppealDialog(
            onDismiss = { showAppealDialog = false },
            onSubmit = { appealRequest -> viewModel.submitAppeal(appealRequest) }
        )
    }

    if (appealSubmitted) {
        LaunchedEffect(Unit) {
            delay(3000)
            viewModel._appealSubmitted.value = false
        }
        Text(
            text = "Appeal submitted successfully!",
            color = Color.Green,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
