package com.example.raceconnect.view.Screens.ProfileScreens

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.raceconnect.R
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.ProfileDetails.ProfileDetailsViewModel.ProfileDetailsViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val BrandRed = Color(0xFFC62828)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    onClose: () -> Unit,
    profileDetailsViewModel: ProfileDetailsViewModel,
    userPreferences: UserPreferences
) {
    val context = LocalContext.current
    val activity = context as? Activity ?: throw IllegalStateException("MyProfileScreen must be used within an Activity context")

    val profileData by profileDetailsViewModel.profileData.collectAsState()
    val isLoading by profileDetailsViewModel.isLoading.collectAsState()
    val errorMessage by profileDetailsViewModel.errorMessage.collectAsState()
    val isEditMode by profileDetailsViewModel.isEditMode.collectAsState()

    var username by remember { mutableStateOf(TextFieldValue(profileData?.username ?: "")) }
    var birthDate by remember { mutableStateOf(TextFieldValue(profileData?.birthdate ?: "")) }
    var contactNumber by remember { mutableStateOf(TextFieldValue(profileData?.number ?: "")) }
    var address by remember { mutableStateOf(TextFieldValue(profileData?.address ?: "")) }
    var bio by remember { mutableStateOf(TextFieldValue(profileData?.bio ?: "")) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(profileData) {
        profileData?.let {
            if (!isEditMode) {
                username = TextFieldValue(it.username)
                birthDate = TextFieldValue(it.birthdate?.let { date -> formatDate(date, "yyyy-MM-dd") } ?: "")
                contactNumber = TextFieldValue(it.number ?: "")
                address = TextFieldValue(it.address ?: "")
                bio = TextFieldValue(it.bio ?: "")
            }
            Log.d("MyProfileScreen", "Profile data updated: profilePicture=${it.profilePicture}")
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                profileImageUri = uri
                val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                profileDetailsViewModel.uploadProfileImage(file)
            }
        }
    }

    // Initialize the DatePickerDialog with the current date or the user's birthdate
    val calendar = remember { Calendar.getInstance() }
    profileData?.birthdate?.let { dateStr ->
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr)
            date?.let { calendar.time = it }
        } catch (e: Exception) {
            Log.e("MyProfileScreen", "Error parsing birthdate: ${e.message}")
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            birthDate = TextFieldValue(sdf.format(calendar.time))
            showDatePicker = false
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Show DatePicker when triggered
    LaunchedEffect(showDatePicker) {
        if (showDatePicker) {
            datePickerDialog.show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Personal Details", color = Color.White, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = BrandRed)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image Section
            Box(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .clickable(enabled = isEditMode) {
                        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
                        imagePickerLauncher.launch(intent)
                    }
            ) {
                val imagePainter = profileImageUri?.let { uri ->
                    Log.d("MyProfileScreen", "Loading temporary profile picture from URI: $uri")
                    rememberAsyncImagePainter(
                        model = uri,
                        onError = { error ->
                            Log.e("MyProfileScreen", "Error loading temporary image: ${error.result.throwable.message}")
                        }
                    )
                } ?: profileData?.profilePicture?.let { url ->
                    Log.d("MyProfileScreen", "Loading profile picture from URL: $url")
                    rememberAsyncImagePainter(
                        model = url,
                        onError = { error ->
                            Log.e("MyProfileScreen", "Error loading profile picture: ${error.result.throwable.message}")
                        }
                    )
                } ?: painterResource(id = R.drawable.baseline_account_circle_24)

                Image(
                    painter = imagePainter,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                if (isEditMode) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change Profile Picture",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(BrandRed.copy(alpha = 0.8f))
                            .padding(6.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content based on edit mode
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (!isEditMode) { // View-only mode
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .weight(1f, fill = false)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            ProfileField(
                                label = "User Name",
                                value = profileData?.username ?: "Not set",
                                icon = Icons.Default.AccountCircle
                            )
                            ProfileField(
                                label = "Birth Date",
                                value = profileData?.birthdate?.let { formatDate(it, "yyyy-MM-dd") } ?: "0000-00-00",
                                icon = Icons.Default.CalendarToday
                            )
                            ProfileField(
                                label = "Contact Number",
                                value = profileData?.number ?: "Not set",
                                icon = Icons.Default.Phone
                            )
                            ProfileField(
                                label = "Address",
                                value = profileData?.address ?: "Not set",
                                icon = Icons.Default.LocationOn
                            )
                            ProfileField(
                                label = "Bio",
                                value = profileData?.bio ?: "Not set",
                                icon = Icons.Default.Edit
                            )
                        }
                    }
                } else { // Edit mode
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .weight(1f, fill = false)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it.copy(text = it.text.trim()) },
                                label = { Text("User Name", fontSize = 16.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                leadingIcon = { Icon(Icons.Default.AccountCircle, "User Icon", tint = Red) },
                                isError = username.text.isBlank(),
                                supportingText = {
                                    if (username.text.isBlank()) {
                                        Text("Username cannot be empty", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            )

                            OutlinedTextField(
                                value = birthDate,
                                onValueChange = { /* No direct editing allowed */ },
                                label = { Text("Birth Date", fontSize = 16.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .clickable(enabled = isEditMode) {
                                        showDatePicker = true
                                    },
                                leadingIcon = { Icon(Icons.Default.CalendarToday, "Calendar Icon", tint = Red) },
                                readOnly = true,
                                enabled = false, // Prevents keyboard from appearing
                                isError = birthDate.text.isBlank(),
                                supportingText = {
                                    if (birthDate.text.isBlank()) {
                                        Text("Birth date is required", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            )

                            OutlinedTextField(
                                value = contactNumber,
                                onValueChange = { contactNumber = it.copy(text = it.text.filter { char -> char.isDigit() }) },
                                label = { Text("Contact Number", fontSize = 16.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                leadingIcon = { Icon(Icons.Default.Phone, "Phone Icon", tint = Red) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                isError = contactNumber.text.length !in 10..15,
                                supportingText = {
                                    if (contactNumber.text.length !in 10..15) {
                                        Text("Enter a valid phone number (10-15 digits)", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            )

                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Address", fontSize = 16.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                leadingIcon = { Icon(Icons.Default.LocationOn, "Location Icon", tint = Red) }
                            )

                            OutlinedTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                label = { Text("Bio", fontSize = 16.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                leadingIcon = { Icon(Icons.Default.Edit, "Edit Icon", tint = Red) },
                                maxLines = 3
                            )
                        }
                    }
                }
            }

            // Action Button (always visible at the bottom, text changes based on mode)
            Button(
                onClick = {
                    if (isEditMode) {
                        if (username.text.isNotBlank() && birthDate.text.isNotBlank() && contactNumber.text.length in 10..15) {
                            profileDetailsViewModel.saveProfileChanges(
                                username.text,
                                birthDate.text,
                                contactNumber.text,
                                address.text,
                                bio.text,
                                userPreferences
                            )
                            profileDetailsViewModel.toggleEditMode() // Switch back to view mode
                            onClose()
                        } else {
                            profileDetailsViewModel.setErrorMessage("Please fill all required fields with valid data")
                        }
                    } else {
                        profileDetailsViewModel.toggleEditMode() // Enter edit mode
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed, disabledContainerColor = BrandRed.copy(alpha = 0.5f)),
                enabled = !isLoading && (!isEditMode || (username.text.isNotBlank() && birthDate.text.isNotBlank() && contactNumber.text.length in 10..15))
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text(
                    text = if (isEditMode) "Save Profile" else "Edit Profile",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            // Error Message
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileField(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$label Icon",
            modifier = Modifier
                .padding(end = 16.dp)
                .size(24.dp),
            tint = Red
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
        }
    }
}

fun formatDate(dateStr: String, format: String): String {
    return try {
        val originalFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val targetFormat = SimpleDateFormat(format, Locale.getDefault())
        val date = originalFormat.parse(dateStr)
        date?.let { targetFormat.format(it) } ?: dateStr
    } catch (e: Exception) {
        dateStr
    }
}