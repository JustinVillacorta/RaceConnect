package com.example.raceconnect.view.Screens.ProfileScreens

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.raceconnect.R
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.MenuViewModel.MenuViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val BrandRed = Color(0xFFC62828)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(onClose: () -> Unit, menuViewModel: MenuViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity ?: throw IllegalStateException("MyProfileScreen must be used within an Activity context")

    val profileData by menuViewModel.profileData.collectAsState()
    val isLoading by menuViewModel.isLoading.collectAsState()
    val errorMessage by menuViewModel.errorMessage.collectAsState()
    val isEditMode by menuViewModel.isEditMode.collectAsState()

    // State for editable fields
    var username by remember { mutableStateOf(TextFieldValue(profileData?.username ?: "")) }
    var birthDate by remember { mutableStateOf(TextFieldValue(profileData?.birthdate ?: "")) }
    var contactNumber by remember { mutableStateOf(TextFieldValue(profileData?.number ?: "")) }
    var address by remember { mutableStateOf(TextFieldValue(profileData?.address ?: "")) }
    var bio by remember { mutableStateOf(TextFieldValue(profileData?.bio ?: "")) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(profileData) {
        profileData?.let {
            if (!isEditMode) {
                username = TextFieldValue(it.username)
                birthDate = TextFieldValue(it.birthdate?.let { date -> formatDate(date, "yyyy-MM-dd") } ?: "")
                contactNumber = TextFieldValue(it.number ?: "")
                address = TextFieldValue(it.address ?: "")
                bio = TextFieldValue(it.bio ?: "")
            }
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
                menuViewModel.uploadProfileImage(file)
            }
        }
    }

    val calendar = remember { Calendar.getInstance() }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                birthDate = TextFieldValue(sdf.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Personal Details", color = Color.White) },
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
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(120.dp)
            ) {
                Image(
                    painter = profileImageUri?.let { rememberAsyncImagePainter(model = it) }
                        ?: profileData?.profilePicture?.let { rememberAsyncImagePainter(model = it) }
                        ?: painterResource(id = R.drawable.baseline_account_circle_24),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change Profile Picture",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(BrandRed.copy(alpha = 0.7f))
                        .padding(4.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
                            imagePickerLauncher.launch(intent)
                        },
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .heightIn(min = 400.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (isEditMode) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("User Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            leadingIcon = { Icon(Icons.Default.AccountCircle, "User Icon", tint = Red) }
                        )

                        OutlinedTextField(
                            value = birthDate,
                            onValueChange = { birthDate = TextFieldValue(it.text) },
                            label = { Text("Birth Date") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { datePickerDialog.show() },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, "Calendar Icon", tint = Red) },
                            readOnly = true
                        )

                        OutlinedTextField(
                            value = contactNumber,
                            onValueChange = { contactNumber = it },
                            label = { Text("Contact Number") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            leadingIcon = { Icon(Icons.Default.Phone, "Phone Icon", tint = Red) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            leadingIcon = { Icon(Icons.Default.LocationOn, "Location Icon", tint = Red) }
                        )

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            leadingIcon = { Icon(Icons.Default.Edit, "Edit Icon", tint = Red) }
                        )
                    } else {
                        ProfileField(
                            label = "User Name",
                            value = profileData?.username ?: "",
                            icon = Icons.Default.AccountCircle
                        )
                        ProfileField(
                            label = "Birth Date",
                            value = profileData?.birthdate?.let { formatDate(it, "yyyy-MM-dd") } ?: "",
                            icon = Icons.Default.CalendarToday
                        )
                        ProfileField(
                            label = "Contact Number",
                            value = profileData?.number ?: "",
                            icon = Icons.Default.Phone
                        )
                        ProfileField(
                            label = "Address",
                            value = profileData?.address ?: "",
                            icon = Icons.Default.LocationOn
                        )
                        ProfileField(
                            label = "Bio",
                            value = profileData?.bio ?: "",
                            icon = Icons.Default.Edit
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (isEditMode) {
                        menuViewModel.saveProfileChanges(
                            username.text,
                            birthDate.text,
                            contactNumber.text,
                            address.text,
                            bio.text
                        )
                    } else {
                        menuViewModel.toggleEditMode()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp), // Increased to 56.dp for better tap target size
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White)
                else Text(
                    text = if (isEditMode) "Save Changes" else "Edit Profile",
                    color = Color.White
                )
            }

            errorMessage?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

// Reusable ProfileField composable for read-only mode
@Composable
fun ProfileField(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$label Icon",
            modifier = Modifier.padding(end = 12.dp),
            tint = Red
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Helper function to format date
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