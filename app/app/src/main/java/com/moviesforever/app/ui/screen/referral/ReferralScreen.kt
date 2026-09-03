package com.moviesforever.app.ui.screen.referral

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.data.model.PricingSettings
import com.moviesforever.app.data.model.UnlockInfo
import com.moviesforever.app.ui.components.GoldButton
import com.moviesforever.app.ui.theme.*

@Composable
fun ReferralScreen(
    unlockInfo: UnlockInfo?,
    pricing: PricingSettings,
    onShare: (String) -> Unit,
    onBack: () -> Unit
) {
    val username = unlockInfo?.username
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("←", fontSize = 20.sp) }
            Text("My Referrals", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        if (username != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Your referral username", color = TextMuted, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("@$username", color = GoldLight, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    GoldButton(
                        text = "Share via WhatsApp",
                        onClick = {
                            val text = "Watch unlimited movies on MoviesForever! Use my referral: $username — when you pay with it I earn PKR ${pricing.referralPayout.toInt()}. 🎬"
                            onShare(text)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "How it works:\n1. Share your username with friends.\n2. When they pay, they enter your username as the referral.\n3. Admin verifies & pays you the referral payout.",
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        } else {
            Text(
                "Unlock the app to earn from referrals.",
                color = TextSecondary,
                fontSize = 15.sp
            )
        }
    }
}
