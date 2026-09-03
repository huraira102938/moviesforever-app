package com.moviesforever.app.ui.screen.lock

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.R
import com.moviesforever.app.data.model.PricingSettings
import com.moviesforever.app.ui.components.GoldButton
import com.moviesforever.app.ui.components.GoldOutlinedButton
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
    var id by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "MoviesForever Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "MoviesForever",
            color = GoldLight,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Unlimited Movies & Shows",
            color = TextSecondary,
            fontSize = 16.sp
        )
        Text(
            text = "One-Time Payment · Lifetime Access",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(24.dp))

        // Price hero card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(GoldDeep, Gold, GoldLight)),
                    RoundedCornerShape(16.dp)
                )
                .padding(20.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column {
                Text(
                    text = "LIFETIME OFFER",
                    color = Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (pricing.standardPrice > 0) "PKR ${pricing.standardPrice.toInt()}" else "PKR 0",
                    color = Black,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Pay once, unlock everything — forever.",
                    color = Black.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Benefits
        BenefitRow("✓", "All current & future movies & shows")
        BenefitRow("✓", "No subscription, no renewal, ever")
        BenefitRow("✓", "Watch online + encrypted offline downloads")
        BenefitRow("✓", "Watch on any device with your code")

        Spacer(Modifier.height(20.dp))

        // Conversion copy (Roman Urdu + English)
        Text(
            text = "اپنی زندگی بھر کے لیے تمام موویز پانے کا موقع – صرف ایک بار ادائیگی کریں!",
            color = GoldLight,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Enjoy every movie for life with just one payment. Unlock and start watching now!",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        // How it works
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "How to unlock",
                    color = GoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(10.dp))
                StepNumber(1, "Pay via JazzCash to the number we share with you")
                StepNumber(2, "Send your payment screenshot + name on WhatsApp")
                StepNumber(3, "Receive your Code + Username and enter them below")
            }
        }

        Spacer(Modifier.height(20.dp))

        // Redeem fields
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Already purchased?",
                    color = GoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Redemption Code ID") },
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
                Spacer(Modifier.height(16.dp))

                GoldButton(
                    text = "Unlock Forever",
                    onClick = {
                        if (id.isBlank() || username.isBlank()) {
                            onRedemptionError("Please enter both your Code ID and Username.")
                        } else {
                            onUnlocked(id, username)
                        }
                    },
                    loading = redeeming,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Pay to unlock
        GoldButton(
            text = "Pay to Unlock",
            onClick = {
                com.moviesforever.app.util.Payments.openSupportWhatsApp(context)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Referral hint
        GoldOutlinedButton(
            text = "Have a referral? Pay with a referral & save",
            onClick = {
                com.moviesforever.app.util.Payments.openSupportWhatsApp(context)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Browse free
        TextButton(onClick = onBrowseFree) {
            Text(
                "Just browsing — explore free content →",
                color = TextMuted,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun BenefitRow(icon: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, color = GoldLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(10.dp))
        Text(text, color = TextSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun StepNumber(number: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(Gold, RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("$number", color = Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        Text(text, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
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
