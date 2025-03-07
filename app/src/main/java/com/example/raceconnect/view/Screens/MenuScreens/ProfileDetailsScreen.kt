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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.raceconnect.R
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
                birthDate = TextFieldValue(it.birthdate ?: "")
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
                val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
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

            Spacer(modifier = Modifier.height(16.dp))

            // Conditionally render editable or read-only fields with labels
            if (isEditMode) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("User Name") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    leadingIcon = { Icon(painterResource(id = R.drawable.baseline_account_circle_24), "User Icon") }
                )

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = TextFieldValue(it.text) },
                    label = { Text("Birth Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { datePickerDialog.show() },
                    leadingIcon = { Icon(painterResource(id = R.drawable.baseline_calendar_month_24), "Calendar Icon") },
                    readOnly = true
                )

                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { contactNumber = it },
                    label = { Text("Contact Number") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    leadingIcon = { Icon(painterResource(id = R.drawable.baseline_phone_24), "Phone Icon") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    leadingIcon = { Icon(painterResource(id = R.drawable.baseline_location_pin_24), "Location Icon") }
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    leadingIcon = { Icon(painterResource(id = R.drawable.baseline_bio_24), "Edit Icon") }
                )
            } else {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "User Name: ",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = profileData?.username ?: "",
                            modifier = Modifier.weight(2f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Birth Date: ",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = profileData?.birthdate ?: "",
                            modifier = Modifier.weight(2f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Contact Number: ",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = profileData?.number ?: "",
                            modifier = Modifier.weight(2f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Address: ",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = profileData?.address ?: "",
                            modifier = Modifier.weight(2f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bio: ",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = profileData?.bio ?: "",
                            modifier = Modifier.weight(2f),
                            style = MaterialTheme.typography.bodyLarge
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
                        menuViewModel.toggleEditMode() // Switch to edit mode
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(50.dp),
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