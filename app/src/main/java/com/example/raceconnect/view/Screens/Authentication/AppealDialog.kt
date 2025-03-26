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
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedConcern by remember { mutableStateOf<ConcernType?>(null) }
    var postId by remember { mutableStateOf("") }
    var itemId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

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
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    singleLine = true,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth()
                )

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

                // Section 2: Dynamic based on selection
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
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ConcernType.ITEM_POST_PENALTY -> {
                        Text("Section 2: Item Details", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = itemId,
                            onValueChange = { itemId = it },
                            label = { Text("Item ID") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    null -> {}
                }

                // Description field (common for all types)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description of Appeal") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (username.isNotBlank() && email.isNotBlank() && selectedConcern != null && description.isNotBlank()) {
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
                enabled = username.isNotBlank() && email.isNotBlank() && selectedConcern != null && description.isNotBlank()
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
