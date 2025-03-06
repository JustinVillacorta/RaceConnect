package com.example.raceconnect.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignupScreen(
    onSignupClick: (Context, String, String, String, () -> Unit) -> Unit,
    onBackNavigate: () -> Unit
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordsMatch by remember { mutableStateOf(true) }

    // States for toggling password visibility
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Password validation states
    var hasLowerCase by remember { mutableStateOf(false) }
    var hasUpperCase by remember { mutableStateOf(false) }
    var hasNumber by remember { mutableStateOf(false) }
    var hasMinLength by remember { mutableStateOf(false) }

    // Update validation states when password changes
    LaunchedEffect(password) {
        hasLowerCase = password.any { it.isLowerCase() }
        hasUpperCase = password.any { it.isUpperCase() }
        hasNumber = password.any { it.isDigit() }
        hasMinLength = password.length >= 8
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sign Up", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordsMatch = it == confirmPassword
            },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                passwordsMatch = it == password
            },
            label = { Text("Confirm Password") },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = !passwordsMatch
        )

        if (!passwordsMatch) {
            Text(
                "Passwords do not match",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Password requirements display
        Column(modifier = Modifier.padding(top = 8.dp)) {
            Text("PASSWORD MUST CONTAIN:", style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (hasLowerCase) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (hasLowerCase) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(" At least one lowercase letter")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (hasUpperCase) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (hasUpperCase) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(" At least one uppercase letter")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (hasNumber) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (hasNumber) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(" At least one number")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (hasMinLength) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (hasMinLength) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(" Minimum 8 characters")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onSignupClick(context, username, email, password) {
                    Toast.makeText(context, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
                    onBackNavigate()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = passwordsMatch &&
                    hasLowerCase &&
                    hasUpperCase &&
                    hasNumber &&
                    hasMinLength &&
                    username.isNotBlank() &&
                    email.isNotBlank() &&
                    password.isNotBlank()
        ) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBackNavigate) {
            Text(
                "Already have an account? Login",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}