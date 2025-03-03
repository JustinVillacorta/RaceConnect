package com.example.raceconnect.view.Screens.Authentication

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.raceconnect.viewmodel.Authentication.AuthenticationViewModel

@Composable
fun ForgotPasswordScreen(viewModel: AuthenticationViewModel, onOtpSent: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Forgot Password", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter your email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                viewModel.requestOtp(email) { success, responseEmail ->
                    isLoading = false
                    message = if (success) "OTP Sent" else "Failed to send OTP"
                    if (success) onOtpSent(responseEmail) // ✅ Pass email forward
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = email.isNotBlank() && !isLoading
        ) {
            Text("Request OTP")
        }

        if (message.isNotEmpty()) {
            Text(message, color = Color.Red, modifier = Modifier.padding(16.dp))
        }

        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}


@Composable
fun OtpVerificationScreen(viewModel: AuthenticationViewModel, email: String, onVerified: () -> Unit) {
    var otp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter OTP", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = otp,
            onValueChange = { otp = it },
            label = { Text("Enter OTP") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                viewModel.verifyOtp(email, otp) { success, responseMessage ->
                    isLoading = false
                    message = responseMessage
                    if (success) onVerified()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = otp.isNotBlank() && !isLoading
        ) {
            Text("Verify OTP")
        }

        if (message.isNotEmpty()) {
            Text(message, color = Color.Red, modifier = Modifier.padding(16.dp))
        }

        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}


@Composable
fun ResetPasswordScreen(viewModel: AuthenticationViewModel, email: String, onResetSuccess: () -> Unit) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var passwordsMatch by remember { mutableStateOf(true) }

    // States for toggling password visibility
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Log.d("ResetPasswordScreen", "📩 Received Email: $email") // ✅ Logs email received in screen

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Reset Password", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field with Show/Hide Button
        OutlinedTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                passwordsMatch = it == confirmPassword
            },
            label = { Text("New Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Confirm Password Field with Show/Hide Button
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                passwordsMatch = it == newPassword
            },
            label = { Text("Confirm Password") },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            isError = !passwordsMatch
        )

        if (!passwordsMatch) {
            Text(
                "Passwords do not match",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                Log.d("ResetPasswordScreen", "📤 Sending Reset Request: email=$email, newPassword=$newPassword, confirmPassword=$confirmPassword")

                viewModel.resetPassword(email, newPassword, confirmPassword) { success, responseMessage ->
                    isLoading = false
                    message = responseMessage

                    if (success) {
                        Log.d("ResetPasswordScreen", "✅ Password reset successful for $email")
                        onResetSuccess()
                    } else {
                        Log.e("ResetPasswordScreen", "❌ Password reset failed: $responseMessage")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = passwordsMatch && newPassword.isNotBlank() && confirmPassword.isNotBlank() && !isLoading
        ) {
            Text("Reset Password")
        }

        if (message.isNotEmpty()) {
            Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
    }
}



