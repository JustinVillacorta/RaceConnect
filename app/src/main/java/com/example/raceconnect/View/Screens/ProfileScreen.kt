import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raceconnect.R

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {


        }

        // Profile Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.baseline_account_circle_24), // Replace with actual drawable
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Justin Vilacorta",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Options List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                ProfileOptionItem(
                    iconResId = R.drawable.baseline_account_circle_24,
                    text = "Personal Details",
                    onClick = { /* Handle Personal Details Click */ }
                )
            }
            item {
                ProfileOptionItem(
                    iconResId = R.drawable.baseline_favorite_24,
                    text = "Favorite Items",
                    onClick = { /* Handle Favorite Items Click */ }
                )
            }
            item {
                ProfileOptionItem(
                    iconResId = R.drawable.baseline_home_24,
                    text = "News Feed Preferences",
                    onClick = { /* Handle Preferences Click */ }
                )
            }
            item {
                ProfileOptionItem(
                    iconResId = R.drawable.baseline_settings_24,
                    text = "Reposts",
                    onClick = { /* Handle Reposts Click */ }
                )
            }
            item {
                ProfileOptionItem(
                    iconResId = R.drawable.baseline_settings_24,
                    text = "Settings",
                    onClick = { /* Handle Settings Click */ }
                )
            }
            item {
                ProfileOptionItem(
                    iconResId = R.drawable.baseline_settings_24,
                    text = "FAQ",
                    onClick = { /* Handle FAQ Click */ }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Push the logout button to the bottom

        // Logout Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Handle Logout */ }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout",
                tint = Color.Red
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log out",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProfileOptionItem(iconResId: Int, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
