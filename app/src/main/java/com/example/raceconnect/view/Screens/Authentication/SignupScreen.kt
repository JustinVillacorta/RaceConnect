package com.example.raceconnect.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raceconnect.R


@Composable
fun SignupScreen(
    onSignupClick: (Context, String, String, String, () -> Unit) -> Unit,
    onBackNavigate: () -> Unit
) {
    val context = LocalContext.current

    // Form fields
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Password match
    var passwordsMatch by remember { mutableStateOf(true) }

    // Toggles for password visibility
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

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 1) Red header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(color = Color(0xFFC62828))
        ) {
            // Back arrow (top-left)
            IconButton(
                onClick = onBackNavigate,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // "Sign Up" text (center)
            Text(
                text = "Sign Up",
                style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 2) Main card (rounded top corners) that overlaps the header
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
                // "Ready, Set, Connect!" in bold red
                Text(
                    text = "Ready, Set, Connect!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFC62828)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Username field with red icon
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email field with red icon
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color(0xFFC62828)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password field with red lock + toggle
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
                                imageVector = if (passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFFC62828)
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFC62828)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Confirm Password field with red lock + toggle
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
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFFC62828)
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFC62828)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !passwordsMatch
                )

                // If passwords don't match, show error text
                if (!passwordsMatch) {
                    Text(
                        "Passwords do not match",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Password requirements
                Column(
                    modifier = Modifier
                        .fillMaxWidth()  // Fill the full width of the parent
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.Start  // Align child items to the left
                ) {
                    Text(
                        text = "PASSWORD MUST CONTAIN:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
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

                // Sign Up button (red)
                Button(
                    onClick = {
                        onSignupClick(context, username, email, password) {
                            Toast.makeText(
                                context,
                                "Account Created Successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onBackNavigate()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC62828),
                        contentColor = Color.White
                    ),
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

                Spacer(modifier = Modifier.weight(1f))

            }
        }
    }
}


        @Preview(showBackground = true, widthDp = 360, heightDp = 800)
        @Composable
        fun PreviewSignupScreen() {
            SignupScreen(
                onSignupClick = { _, _, _, _, _ -> },
                onBackNavigate = {}
            )
        }
