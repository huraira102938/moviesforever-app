package com.moviesforever.app.ui.screen.celebration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.data.model.PricingSettings
import com.moviesforever.app.data.model.UnlockInfo
import com.moviesforever.app.ui.components.GoldButton
import com.moviesforever.app.ui.theme.*

/**
 * Stage 2 emphasis: shown once right after successful unlock.
 * Primarily pushes "Share & Earn".
 */
@Composable
fun CelebrationScreen(
    unlockInfo: UnlockInfo?,
    pricing: PricingSettings,
    onShare: (String) -> Unit,
    onStartWatching: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("🎉", fontSize = 64.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "You're All Set!",
                color = TextPrimary,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Lifetime access unlocked",
                color = GoldLight,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Welcome, @${unlockInfo?.username ?: ""}",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(28.dp))

            // Referral push
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(GoldDeep, Gold, GoldLight)),
                            RoundedCornerShape(18.dp)
                        )
                        .padding(22.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💰", fontSize = 36.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Share & Earn Money!",
                            color = Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (pricing.referralPayout > 0)
                                "Earn PKR ${pricing.referralPayout.toInt()} for every friend who unlocks MoviesForever."
                            else
                                "Earn money for every friend who unlocks MoviesForever.",
                            color = Black.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(14.dp))
                        GoldButton(
                            text = "Share Now & Earn",
                            onClick = {
                                val text = "Watch unlimited movies on MoviesForever! Use my referral @${unlockInfo?.username} — I earn PKR ${pricing.referralPayout.toInt()} when you unlock! 🎬"
                                onShare(text)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onStartWatching) {
                Text("Start Watching →", color = GoldLight, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
