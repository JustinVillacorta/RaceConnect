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
    // State variables for form inputs
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedConcern by remember { mutableStateOf<ConcernType?>(null) }
    var postId by remember { mutableStateOf("") }
    var itemId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // Validation checks
    val isUsernameValid = username.isNotBlank()
    val isEmailValid = email.isNotBlank()
    val isDescriptionValid = description.isNotBlank()
    val isPostIdValid = if (selectedConcern == ConcernType.POST_PENALTY) {
        postId.isNotBlank() && postId.toIntOrNull() != null
    } else {
        true
    }
    val isItemIdValid = if (selectedConcern == ConcernType.ITEM_POST_PENALTY) {
        itemId.isNotBlank() && itemId.toIntOrNull() != null
    } else {
        true
    }
    val isFormValid = isUsernameValid && isEmailValid && selectedConcern != null &&
            isDescriptionValid && isPostIdValid && isItemIdValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Appeal") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Account Information
                Text("Section 1: Account Information", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = username,
                    singleLine = true,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isUsernameValid
                )
                if (!isUsernameValid) {
                    Text(
                        "Username is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = email,
                    singleLine = true,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isEmailValid
                )
                if (!isEmailValid) {
                    Text(
                        "Email is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Concern Type Selection
                Text("What is your concern?", style = MaterialTheme.typography.bodyMedium)
                ConcernType.values().forEach { concern ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
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

                // Section 2: Dynamic Fields Based on Concern Type
                when (selectedConcern) {
                    ConcernType.ACCOUNT_PENALTY -> {
                        Text("Section 2: Account Suspension Details", style = MaterialTheme.typography.titleMedium)
                    }
                    ConcernType.POST_PENALTY -> {
                        Text("Section 2: Post Details", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = postId,
                            onValueChange = { postId = it },
                            label = { Text("Post ID") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            isError = !isPostIdValid
                        )
                        if (!isPostIdValid) {
                            Text(
                                "Valid Post ID is required",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    ConcernType.ITEM_POST_PENALTY -> {
                        Text("Section 2: Item Details", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = itemId,
                            onValueChange = { itemId = it },
                            label = { Text("Item ID") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            isError = !isItemIdValid
                        )
                        if (!isItemIdValid) {
                            Text(
                                "Valid Item ID is required",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    null -> {}
                }

                // Description Field (Required for All Types)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description of Appeal") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    isError = !isDescriptionValid
                )
                if (!isDescriptionValid) {
                    Text(
                        "Description is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
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
                Text("Submit Appeal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}