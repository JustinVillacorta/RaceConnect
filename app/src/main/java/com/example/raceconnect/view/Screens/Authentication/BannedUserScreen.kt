package com.example.raceconnect.view.Screens.Authentication

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BannedUserScreen(
    suspensionEndDate: String?,
    onLogoutClick: () -> Unit
) {
    val isPermanentBan = suspensionEndDate == null
    val currentDateTime = LocalDateTime.now()

    var remainingTime by remember { mutableStateOf("") }

    // Calculate remaining time if not permanent ban
    LaunchedEffect(suspensionEndDate) {
        if (!isPermanentBan && suspensionEndDate != null) {
            val formatter = DateTimeFormatter.ISO_DATE_TIME
            val endDate = LocalDateTime.parse(suspensionEndDate, formatter)

            if (currentDateTime.isBefore(endDate)) {
                val days = ChronoUnit.DAYS.between(currentDateTime, endDate)
                val hours = ChronoUnit.HOURS.between(currentDateTime, endDate) % 24
                remainingTime = when {
                    days > 0 -> "$days day${if (days > 1) "s" else ""}"
                    hours > 0 -> "$hours hour${if (hours > 1) "s" else ""}"
                    else -> "less than an hour"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = "Warning Icon",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Account Suspended",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isPermanentBan) {
                "Your account has been permanently suspended due to violation of our community guidelines."
            } else {
                "Your account has been temporarily suspended due to violation of our community guidelines.\n\nTime remaining: $remainingTime"
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Log Out")
        }
    }
}