package com.example.raceconnect.view.Screens.Authentication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.raceconnect.model.AppealRequest
import com.example.raceconnect.model.ConcernType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppealDialog(
    onDismiss: () -> Unit,
    onSubmit: (AppealRequest) -> Unit
) {
    // Form input states
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedConcern by remember { mutableStateOf<ConcernType?>(null) }
    var postId by remember { mutableStateOf("") }
    var itemId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // Validation logic
    val isUsernameValid = username.isNotBlank()
    val isEmailValid = email.isNotBlank()
    val isDescriptionValid = description.isNotBlank()
    val isPostIdValid = if (selectedConcern == ConcernType.POST_PENALTY) {
        postId.isNotBlank() && postId.toIntOrNull() != null
    } else true
    val isItemIdValid = if (selectedConcern == ConcernType.ITEM_POST_PENALTY) {
        itemId.isNotBlank() && itemId.toIntOrNull() != null
    } else true
    val isFormValid = isUsernameValid && isEmailValid && selectedConcern != null &&
            isDescriptionValid && isPostIdValid && isItemIdValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Appeal") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp) // Fixed, smaller height
                    .verticalScroll(rememberScrollState()) // Enables scrolling
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp) // Reduced spacing
            ) {
                // Account Information Section
                Text(text = "Account Information", style = MaterialTheme.typography.titleSmall)

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isUsernameValid
                )
                if (!isUsernameValid) {
                    Text("Required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isEmailValid
                )
                if (!isEmailValid) {
                    Text("Required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                // Concern Type Selection
                Text(text = "Concern Type", style = MaterialTheme.typography.bodyMedium)
                ConcernType.values().forEach { concern ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = selectedConcern == concern,
                            onClick = { selectedConcern = concern }
                        )
                        Text(
                            text = when (concern) {
                                ConcernType.ACCOUNT_PENALTY -> "Account Penalty"
                                ConcernType.POST_PENALTY -> "Post Penalty"
                                ConcernType.ITEM_POST_PENALTY -> "Item Post Penalty"
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // Dynamic Fields Based on Concern Type
                when (selectedConcern) {
                    ConcernType.ACCOUNT_PENALTY -> {
                        Text(text = "Account Suspension Details", style = MaterialTheme.typography.titleSmall)
                    }
                    ConcernType.POST_PENALTY -> {
                        Text(text = "Post Details", style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(
                            value = postId,
                            onValueChange = { postId = it },
                            label = { Text("Post ID") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            isError = !isPostIdValid
                        )
                        if (!isPostIdValid) {
                            Text("Valid ID required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    ConcernType.ITEM_POST_PENALTY -> {
                        Text(text = "Item Details", style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(
                            value = itemId,
                            onValueChange = { itemId = it },
                            label = { Text("Item ID") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            isError = !isItemIdValid
                        )
                        if (!isItemIdValid) {
                            Text("Valid ID required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    null -> {}
                }

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp), // Fixed height for the description field
                    isError = !isDescriptionValid
                )
                if (!isDescriptionValid) {
                    Text("Required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid) {
                        onSubmit(
                            AppealRequest(
                                username = username,
                                email = email,
                                concernType = selectedConcern!!,
                                postId = if (selectedConcern == ConcernType.POST_PENALTY) postId.toIntOrNull() else null,
                                itemId = if (selectedConcern == ConcernType.ITEM_POST_PENALTY) itemId.toIntOrNull() else null,
                                description = description
                            )
                        )
                        onDismiss()
                    }
                },
                enabled = isFormValid
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
