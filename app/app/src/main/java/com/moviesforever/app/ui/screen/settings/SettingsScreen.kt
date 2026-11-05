package com.moviesforever.app.ui.screen.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.data.model.ContactDetails
import com.moviesforever.app.ui.theme.*
import kotlinx.coroutines.delay

private const val RESET_UNLOCK_CONFIRM_SECONDS = 10

@Composable
fun SettingsScreen(
    isUnlocked: Boolean,
    username: String?,
    wifiOnlyDownloads: Boolean,
    contactDetails: ContactDetails = ContactDetails(),
    onWifiOnlyDownloadsChange: (Boolean) -> Unit,
    onResetUnlock: () -> Unit,
    onBack: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

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
                    Text("WiFi-only Downloads", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "Movies sirf WiFi par download hon (default off: mobile data par bhi download hota hai)",
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
                        "Status: One Year Member",
                        color = GoldLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = DarkElevated)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Danger Zone",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Aap ka code sirf aik baar use ho sakta hai. Yahan reset karne se code wapas NAHI " +
                                "milta -- sirf is device se aap ka access khatam ho jata hai. " +
                                "Phir app lock ho jayegi, jab tak aap " +
                                "naya code nahi khareedte.",
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
                        Text("Remove Access From This Device")
                    }
                } else {
                    Text("Status: Free", color = TextSecondary, fontSize = 14.sp)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // WhatsApp group/channel -- shown to everyone, free trial or paid.
        if (contactDetails.groupLink.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(contactDetails.groupLink))
                                )
                            } catch (e: Exception) {
                                Toast.makeText(context, "Link open nahi ho saka", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            contactDetails.groupTitle.ifBlank { "WhatsApp group / channel join karein" },
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            contactDetails.groupLink,
                            color = Gold,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

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
                    "Aik baar payment karein, poora saal dekhein. Unlock ke liye WhatsApp / JazzCash par contact karein.",
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
                "Is device se access remove karein?",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Yeh permanent hai. Aap ka code sirf aik baar use hota hai aur " +
                            "already use ho chuka hai -- dobara nahi lag sakta. Access remove karne ke baad " +
                            "app lock ho jayegi aur payment page nazar aayega. Wapas lane ka " +
                            "sirf aik tareeqa hai: naya code khareedna.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Yeh koi subscription cancel nahi karta (subscription hai hi nahi -- yeh " +
                            "aik baar ki payment thi), aur refund nahi hoga.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Sirf tab aage barhein agar aap ko poora yaqeen hai.",
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