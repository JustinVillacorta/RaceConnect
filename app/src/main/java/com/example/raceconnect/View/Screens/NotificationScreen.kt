import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raceconnect.Model.Notification
import com.example.raceconnect.R




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen() {
    // Sample notifications
    val notifications = listOf(
        Notification(
            profilePic = R.drawable.baseline_account_circle_24, // Replace with actual drawable
            userName = "Trish Alexa",
            action = "liked your post.",
            timestamp = "2h"
        ),
        Notification(
            profilePic =  R.drawable.baseline_account_circle_24, // Replace with actual drawable
            userName = "Jake Jordan Gyllenhaal",
            action = "liked your report.",
            timestamp = "4h"
        ),
        Notification(
            profilePic =  R.drawable.baseline_account_circle_24, // Replace with actual drawable
            userName = "Mark Zuckerberg",
            action = "reposted the same post.",
            timestamp = "5h"
        ),
        Notification(
            profilePic =  R.drawable.baseline_account_circle_24, // Replace with actual drawable
            userName = "John Cena",
            action = "commented on your post.",
            timestamp = "1d"
        ),
        Notification(
            profilePic =  R.drawable.baseline_account_circle_24, // Replace with actual drawable
            userName = "Earl Parra",
            action = "commented on your post.",
            timestamp = "1d"
        ),
        Notification(
            profilePic = R.drawable.baseline_account_circle_24, // Replace with actual drawable
            userName = "Isaac Yuichi",
            action = "commented on your report.",
            timestamp = "1d"
        ),
        Notification(
            profilePic = R.drawable.baseline_account_circle_24, // Replace with actual drawable
            userName = "Renee Olly",
            action = "liked your post.",
            timestamp = "2d"
        )
    )


    // Notifications List
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp), // Padding below the AppBar
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notifications.size) { index ->
            NotificationItem(notification = notifications[index])
            }
        }
    }


@Composable
fun NotificationItem(notification: Notification) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFEAEA)) // Light pink background
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Picture
        Image(
            painter = painterResource(id = notification.profilePic),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Notification Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.userName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = notification.action,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Timestamp
        Text(
            text = notification.timestamp,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End
        )
    }
}
