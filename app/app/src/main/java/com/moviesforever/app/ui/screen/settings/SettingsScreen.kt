package com.moviesforever.app.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.ui.theme.*

@Composable
fun SettingsScreen(
    isUnlocked: Boolean,
    username: String?,
    onResetUnlock: () -> Unit,
    onBack: () -> Unit
) {
    var wifiOnly by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("←", fontSize = 20.sp) }
            Text("Settings", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        // WiFi only download toggle
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("WiFi-only downloads", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "Download movies only when connected to WiFi (recommended)",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = wifiOnly,
                    onCheckedChange = { wifiOnly = it },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Gold,
                        checkedThumbColor = Black
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Account
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Account", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                if (isUnlocked) {
                    Text("Username: @$username", color = TextSecondary, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Status: Lifetime Member",
                        color = GoldLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = onResetUnlock) {
                        Text("Reset unlock on this device", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Text("Status: Free Preview", color = TextSecondary, fontSize = 14.sp)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("About", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("MoviesForever v1.0", color = TextSecondary, fontSize = 14.sp)
                Text(
                    "Pay once, watch forever. Contact us via WhatsApp / JazzCash to unlock.",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}
