package com.moviesforever.app.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.ui.theme.*
import kotlinx.coroutines.delay

private const val RESET_UNLOCK_CONFIRM_SECONDS = 10

@Composable
fun SettingsScreen(
    isUnlocked: Boolean,
    username: String?,
    wifiOnlyDownloads: Boolean,
    onWifiOnlyDownloadsChange: (Boolean) -> Unit,
    onResetUnlock: () -> Unit,
    onBack: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp) // Added bottom padding for smooth scrolling end
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
                    checked = wifiOnlyDownloads,
                    onCheckedChange = onWifiOnlyDownloadsChange,
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
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = DarkElevated)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Danger zone",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Your redemption code can only be used once. Resetting here does NOT " +
                                "give you a working code back -- it just deletes your access on " +
                                "this device. You would be locked out permanently unless you " +
                                "purchase a new code.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("Remove access from this device")
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

    if (showResetDialog) {
        ResetUnlockConfirmDialog(
            onDismiss = { showResetDialog = false },
            onConfirm = {
                showResetDialog = false
                onResetUnlock()
            }
        )
    }
}

/**
 * Red danger-style confirmation dialog for resetting the local unlock. The
 * confirm button stays disabled and shows a live countdown for
 * [RESET_UNLOCK_CONFIRM_SECONDS] seconds, so a curious or accidental tap can't
 * immediately trigger it -- the user has to actually wait and read before they
 * can act.
 */
@Composable
private fun ResetUnlockConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var secondsRemaining by remember { mutableIntStateOf(RESET_UNLOCK_CONFIRM_SECONDS) }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining -= 1
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        icon = {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                "Remove access from this device?",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "This is permanent. Your redemption code was a single-use code and is " +
                            "already burned -- it cannot be entered again. Once you remove access " +
                            "here, you will be locked out and see the paywall, with no way to " +
                            "restore it except purchasing a brand new code.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "This does not cancel a subscription (there isn't one -- it was a " +
                            "one-time payment), and there is no refund.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Only continue if you're absolutely sure.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = secondsRemaining <= 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = if (secondsRemaining > 0) {
                        "Remove Access ($secondsRemaining)"
                    } else {
                        "Remove Access Now"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}