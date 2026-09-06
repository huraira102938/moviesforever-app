package com.moviesforever.app.ui.screen.lock

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    onRedemptionError: (String) -> Unit,
    redeeming: Boolean,
    onRedeemingChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var showSignIn by remember { mutableStateOf(false) }
    var referralUsername by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .height(440.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LifetimeMiniCard(
                    modifier = Modifier
                        .width(240.dp)
                        .fillMaxHeight(),
                    pricing = pricing
                )
                FreeTrialMiniCard(
                    modifier = Modifier
                        .width(210.dp)
                        .fillMaxHeight(),
                    onBrowseFree = onBrowseFree
                )
            }

            Spacer(Modifier.height(16.dp))

            LifetimeDetails(
                pricing = pricing,
                referralUsername = referralUsername,
                onReferralUsernameChange = { referralUsername = it },
                clipboard = clipboard,
                onSendScreenshot = {
                    com.moviesforever.app.util.Payments.openSupportWhatsApp(context, referralUsername)
                }
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Enjoy every movie for life with just one payment.",
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
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

            // Features / Pros
            MiniBenefitRow("Browse full catalog")
            MiniBenefitRow("Watch free movies & trailers")

            // Limitations / Cons
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
private fun LifetimeMiniCard(modifier: Modifier = Modifier, pricing: PricingSettings) {
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
                onClick = { /* Handle onClick later */ },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LifetimeDetails(
    pricing: PricingSettings,
    referralUsername: String,
    onReferralUsernameChange: (String) -> Unit,
    clipboard: ClipboardManager,
    onSendScreenshot: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(20.dp))
            .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("What you get with Lifetime Access", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            BenefitRow("All current & future movies & shows")
            BenefitRow("No subscription, no renewal, ever")
            BenefitRow("Watch online + encrypted offline downloads")

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = DarkElevated)
            Spacer(Modifier.height(16.dp))

            Text("Send payment to", color = TextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            PaymentDetailRow(label = "JazzCash / EasyPaisa", value = pricing.jazzcashNumber, clipboard = clipboard)
            Spacer(Modifier.height(8.dp))
            PaymentDetailRow(label = "Account title", value = pricing.jazzcashTitle, clipboard = clipboard)

            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = referralUsername,
                onValueChange = onReferralUsernameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Referral username (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = textFieldColors()
            )
            if (pricing.referralPayout > 0) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "We are giving lifetime access to our first 500 members only. Secure your spot before they run out so pay once today and enjoy all current and future movies forever. ",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(16.dp))
            GoldButton(
                text = "Send Screenshot",
                onClick = onSendScreenshot,
                modifier = Modifier.fillMaxWidth(),
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
private fun BenefitRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun PaymentDetailRow(label: String, value: String, clipboard: ClipboardManager) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkElevated, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, color = TextMuted, fontSize = 11.sp)
            Text(value, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        IconButton(onClick = { clipboard.setText(AnnotatedString(value)) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy $label", tint = Gold, modifier = Modifier.size(18.dp))
        }
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