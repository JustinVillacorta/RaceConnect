package com.example.raceconnect.ui

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.R
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.view.ui.theme.fontFamily
import com.example.raceconnect.viewmodel.Authentication.AuthenticationViewModel


@Composable
fun TermsOfServiceDialog(
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    var isChecked by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    var canProceed by remember { mutableStateOf(false) }

    val uriHandler = LocalUriHandler.current

    val fullText = """
        Terms and Conditions for RaceConnect

        Welcome to RaceConnect! By creating an account and using our services, you agree to comply with and be bound by these Terms of Service. If you do not agree to these terms, please do not use RaceConnect.

        1. Acceptance of Terms
        By accessing and using RaceConnect, you agree to be bound by these Terms of Service.

        2. Account Registration
        •	You must provide accurate and complete information when creating an account.
        •	You are responsible for maintaining the confidentiality of your account credentials.
        •	RaceConnect is not responsible for any unauthorized use of your account.

        3. User Conduct
        When using RaceConnect, you agree not to:
            •	Violate any applicable laws or regulations.
            •	Engage in any fraudulent, abusive, or harmful activity.
            •	Interfere with or disrupt RaceConnect services or servers.
            •	Attempt to gain unauthorized access to any part of our system.
            •	Post any content that is illegal, harmful, or infringes on the rights of others.

        4. Data Privacy
        We value your privacy and are committed to protecting your personal information. While we are in the process of finalizing our comprehensive Privacy Policy, please be assured that:
            •   We collect only the data necessary to provide and improve our services (e.g., email addresses or usage data).
            •   Your data is used solely for these purposes and is not shared with third parties without your consent, except as required by law.
            •   We implement appropriate security measures to protect your information.
            •   You have the right to access, correct, or delete your data by contacting us at raceconnect.team@gmail.com.

        5. Content
        Users are responsible for all content they post on RaceConnect.

        6. Termination of Account
        RaceConnect reserves the right to suspend or terminate your account at any time if you violate these terms or engage in harmful behavior.

        7. Changes to Terms
        We may update these Terms from time to time. Continued use of RaceConnect after changes are posted constitutes acceptance of the new terms.

        8. Limitation of Liability
        To the fullest extent permitted by law, RaceConnect shall not be liable for any direct, indirect, incidental, or consequential damages resulting from your use of the platform.

        9. Governing Law
        These terms are governed by applicable laws.

        10. Contact information
        If you have any questions about these Terms, please contact us at raceconnect.team@gmail.com
    """.trimIndent()

    val email = "raceconnect.team@gmail.com"
    val startIndex = fullText.indexOf(email)
    val endIndex = if (startIndex != -1) startIndex + email.length else -1

    val termsText = buildAnnotatedString {
        append(fullText)
        if (startIndex != -1) {
            addStyle(
                style = SpanStyle(
                    color = Color.Blue,
                    textDecoration = TextDecoration.Underline
                ),
                start = startIndex,
                end = endIndex
            )
            addStringAnnotation(
                tag = "URL",
                annotation = "mailto:$email",
                start = startIndex,
                end = endIndex
            )
        }
    }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value to scrollState.maxValue }
            .collect { (value, maxValue) ->
                if (maxValue == 0 || value == maxValue) {
                    canProceed = true
                }
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Terms of Service",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                        .background(Color.LightGray.copy(alpha = 0.1f))
                        .verticalScroll(scrollState)
                        .padding(horizontal = 4.dp)
                ) {
                    ClickableText(
                        text = termsText,
                        style = MaterialTheme.typography.bodyMedium,
                        onClick = { offset ->
                            termsText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    uriHandler.openUri(annotation.item)
                                }
                        }
                    )
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { isChecked = it }
                    )
                    Text(
                        text = "By clicking \"Sign Up,\" you acknowledge that you have read, understood, and agreed to these Terms of Service.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAccept,
                    enabled = isChecked && canProceed
                ) {
                    Text("Continue")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    navController: NavController,
    viewModel: AuthenticationViewModel = viewModel(), // Inject ViewModel here
    onBackNavigate: () -> Unit
) {
    val context = LocalContext.current

    // Form fields
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Email validation state
    var isEmailValid by remember { mutableStateOf(true) }

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

    // Update validation states when password or email changes
    LaunchedEffect(password) {
        hasLowerCase = password.any { it.isLowerCase() }
        hasUpperCase = password.any { it.isUpperCase() }
        hasNumber = password.any { it.isDigit() }
        hasMinLength = password.length >= 8
    }
    LaunchedEffect(email) {
        isEmailValid = email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    var showTosDialog by remember { mutableStateOf(false) }
    var tosAccepted by remember { mutableStateOf(false) }

    // Observe ViewModel states
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.ErrorMessage.collectAsState()

    // Scroll state for vertical scrolling
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Red header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(color = Red)
        ) {
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
            Text(
                text = "Sign Up",
                fontFamily = fontFamily, color = Color.White, fontSize = 30.sp,
                modifier = Modifier.align(Alignment.Center)
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
                    .verticalScroll(scrollState) // Add vertical scrolling here
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ready, Set, Connect!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFC62828)
                )
                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = if (it.length <= 8) it else it.take(8)
                        },
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
                        isError = username.length == 8,
                        supportingText = {
                            if (username.length == 8) {
                                Text(
                                    text = "Maximum of 8 characters reached",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = if (username.length == 8) Color.Red else Color.Gray,
                            unfocusedBorderColor = if (username.length == 8) Color.Red else Color.Gray,
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.Black
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Email field with validation
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Gray,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black
                    ),
                    isError = !isEmailValid,
                    supportingText = {
                        if (!isEmailValid && email.isNotEmpty()) {
                            Text(
                                text = "Please enter a valid email address",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Password field
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
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Gray,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Confirm Password field
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
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Gray,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black
                    ),
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
                Spacer(modifier = Modifier.height(16.dp))

                // Password requirements
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.Start
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

                // Sign Up button
                Button(
                    onClick = {
                        if (!tosAccepted) {
                            showTosDialog = true
                        } else {
                            viewModel.signUp(
                                context = context,
                                username = username,
                                email = email,
                                password = password,
                                onToast = { message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    if (message == "Account Created Successfully!") {
                                        username = ""
                                        email = ""
                                        password = ""
                                        confirmPassword = ""
                                        navController.popBackStack()
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = !isLoading &&
                            username.isNotEmpty() &&
                            email.isNotEmpty() && isEmailValid &&
                            password.isNotEmpty() &&
                            confirmPassword.isNotEmpty() &&
                            passwordsMatch &&
                            hasLowerCase &&
                            hasUpperCase &&
                            hasNumber &&
                            hasMinLength,
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Sign Up")
                    }
                }

                // Show error message if present
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp)) // Add some padding at the bottom
            }
        }
    }

    // Show TOS Dialog
    if (showTosDialog) {
        TermsOfServiceDialog(
            onDismiss = { showTosDialog = false },
            onAccept = {
                tosAccepted = true
                showTosDialog = false
                viewModel.signUp(
                    context = context,
                    username = username,
                    email = email,
                    password = password,
                    onToast = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        if (message == "Account Created Successfully!") {
                            username = ""
                            email = ""
                            password = ""
                            confirmPassword = ""
                            navController.popBackStack()
                        }
                    }
                )
            }
        )
    }
}
