package com.moviesforever.app.ui.screen.lock

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.moviesforever.app.R
import com.moviesforever.app.data.model.PricingSettings
import com.moviesforever.app.ui.components.GoldButton
import com.moviesforever.app.ui.theme.*

@Composable
fun LockScreen(
    pricing: PricingSettings,
    onUnlocked: (String, String) -> Unit,
    onBrowseFree: () -> Unit,
    onUnlockClick: () -> Unit = {},
    onRedemptionError: (String) -> Unit,
    redeeming: Boolean,
    onRedeemingChange: (Boolean) -> Unit
) {
    var showSignIn by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        // Top Header Row with Logo and Sign In
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("MoviesForever", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = { showSignIn = true }) {
                Text("Sign In", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Choose how you want to watch",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))

            // Plan Cards Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .height(430.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LifetimeMiniCard(
                    modifier = Modifier
                        .width(240.dp)
                        .fillMaxHeight(),
                    pricing = pricing,
                    onUnlockClick = onUnlockClick
                )
                FreeTrialMiniCard(
                    modifier = Modifier
                        .width(210.dp)
                        .fillMaxHeight(),
                    onBrowseFree = onBrowseFree
                )
            }

            Spacer(Modifier.height(24.dp))

            // --- RICH BOTTOM CONTENT ---
            Text(
                text = "Why Choose MoviesForever?",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            // Feature Grid Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureTile(
                    icon = Icons.Filled.Bolt,
                    title = "Instant Unlock",
                    subtitle = "WhatsApp activation",
                    modifier = Modifier.weight(1f)
                )
                FeatureTile(
                    icon = Icons.Filled.Hd,
                    title = "Ultra HD / 4K",
                    subtitle = "High video quality",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Feature Grid Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureTile(
                    icon = Icons.Filled.Verified,
                    title = "Safe Transfer",
                    subtitle = "Verified account",
                    modifier = Modifier.weight(1f)
                )
                FeatureTile(
                    icon = Icons.Filled.CheckCircle,
                    title = "No Ads Ever",
                    subtitle = "Zero interruptions",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Reassurance Banner
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Gold, CircleShape)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Enjoy lifetime access with 24/7 WhatsApp verification support.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }

    if (showSignIn) {
        SignInDialog(
            redeeming = redeeming,
            onDismiss = { showSignIn = false },
            onUnlock = { id, username ->
                if (id.isBlank() || username.isBlank()) {
                    onRedemptionError("Please enter both your Code ID and Username.")
                } else {
                    onUnlocked(id, username)
                }
            }
        )
    }
}

@Composable
private fun FeatureTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkElevated),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(DarkElevated, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FreeTrialMiniCard(modifier: Modifier = Modifier, onBrowseFree: () -> Unit) {
    Box(
        modifier = modifier
            .background(DarkSurface, RoundedCornerShape(18.dp))
            .border(1.dp, DarkElevated, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Free Trial", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("No payment needed", color = TextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(10.dp))

            MiniBenefitRow("Browse full catalog")
            MiniBenefitRow("Watch free movies & trailers")

            MiniConRow("Limited movies")
            MiniConRow("Slow bandwidth")
            MiniConRow("No downloads")

            Spacer(Modifier.weight(1f))
            OutlinedButton(
                onClick = onBrowseFree,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 10.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkElevated)
            ) {
                Text("Browse Free", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun LifetimeMiniCard(
    modifier: Modifier = Modifier,
    pricing: PricingSettings,
    onUnlockClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(DarkSurface, RoundedCornerShape(18.dp))
            .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LimitedBadge()
            Spacer(Modifier.height(8.dp))
            Text(
                "Lifetime Access",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (pricing.standardPrice > 0) "PKR ${pricing.standardPrice.toInt()}" else "Price coming soon",
                color = GoldLight,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text("one-time", color = TextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(10.dp))
            MiniBenefitRow("All movies & shows, for life")
            MiniBenefitRow("All upcoming movies & shows")
            MiniBenefitRow("No subscription, ever")
            MiniBenefitRow("Unlimited downloads")
            MiniBenefitRow("Faster streaming speed")

            Spacer(Modifier.weight(1f))
            GoldButton(
                text = "Unlock Now",
                onClick = onUnlockClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LimitedBadge() {
    Row(
        modifier = Modifier
            .background(Error.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = Error, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(3.dp))
        Text("Limited", color = Error, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MiniBenefitRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Gold, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun MiniConRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(text, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun SignInDialog(
    redeeming: Boolean,
    onDismiss: () -> Unit,
    onUnlock: (String, String) -> Unit
) {
    var id by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(20.dp))
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Sign in with your code", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Already purchased? Enter the details we sent you.", color = TextMuted, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Code ID") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = textFieldColors()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Username") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = textFieldColors()
                )
                Spacer(Modifier.height(18.dp))

                GoldButton(
                    text = "Unlock Forever",
                    onClick = { onUnlock(id, username) },
                    loading = redeeming,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel", color = TextMuted)
                }
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Gold,
    unfocusedBorderColor = DarkElevated,
    focusedLabelColor = Gold,
    unfocusedLabelColor = TextMuted,
    cursorColor = Gold,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)