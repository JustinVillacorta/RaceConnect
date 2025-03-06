package com.example.raceconnect.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raceconnect.R // Replace with your own R import if needed

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onSignupNavigate: () -> Unit,
    onForgotPasswordNavigate: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Main column fills the screen
    Column(modifier = Modifier.fillMaxSize()) {

        // 1) Red header with "Log In" centered
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 1) Red header at the top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)    // Adjust as needed
                    .background(color = Color(0xFFC62828)) // Use your brand color
            ) {
                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(16.dp)
                )
            }

            // 2) Main card that sits below the red header
            Card(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 120.dp) // So it overlaps the red header
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 2.1) "Ready, Set, Connect!" text
                    Text(
                        text = "Ready, Set, Connect!",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFC62828),  // Make this red or brand color
                        modifier = Modifier
                            .padding(16.dp)
                    )

                    // Email TextField with red icon
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

                    // Password TextField with red icon + toggle
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
                        visualTransformation = if (isPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color(0xFFC62828)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row with "Remember me" + "Forgot Password?" in red
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

                        // "Forgot Password?" in red
                        TextButton(
                            onClick = onForgotPasswordNavigate,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Forgot Password?", color = Color(0xFFC62828))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Log in button in red
                    Button(
                        onClick = { onLoginClick(email, password) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC62828),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Log in")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // "Don't have an account? Sign Up" - only "Sign Up" is red and clickable
                    Row {
                        Text(text = "Don't have an account? ", color = Color.Black)
                        Text(
                            text = "Sign Up",
                            color = Color(0xFFC62828),
                            modifier = Modifier.clickable {
                                onSignupNavigate()
                            }
                        )
                    }

                    // Push the footer (logo + text) to the bottom
                    Spacer(modifier = Modifier.weight(1f))

                    // 3) Footer: RaceConnect logo + text
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo), // Replace with your logo resource
                            contentDescription = "RaceConnect Logo",
                            modifier = Modifier.size(100.dp)  // Adjust size as needed
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Welcome to RaceConnect! Login to manage your account\n" +
                                    "and access exclusive features.",
                            color = Color(0xFFC62828), // Red text; change if you prefer another color
                            fontSize = 12.sp,
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun PreviewLoginScreen() {
    LoginScreen(
        onLoginClick = { email, password ->
            // Handle login preview
        },
        onSignupNavigate = {
            // Handle sign-up preview
        },
        onForgotPasswordNavigate = {
            // Handle forgot password preview
        }
    )
}