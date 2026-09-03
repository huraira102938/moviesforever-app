package com.moviesforever.app.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.data.model.PricingSettings
import com.moviesforever.app.data.model.UnlockInfo
import com.moviesforever.app.ui.components.GoldButton
import com.moviesforever.app.ui.theme.*

@Composable
fun ProfileScreen(
    unlockInfo: UnlockInfo?,
    pricing: PricingSettings,
    onShareReferral: (String) -> Unit,
    onReferralClick: () -> Unit,
    onSettings: () -> Unit,
    onUnlockClick: () -> Unit
) {
    val isUnlocked = unlockInfo != null
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Text("My MoviesForever", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(16.dp))

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Gold, RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 40.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = unlockInfo?.username ?: "Free Preview User",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (isUnlocked) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(Gold.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Lifetime Member", color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        if (isUnlocked) {
            // ------------------ STAGE 2: REFERRAL EMPHASIS ------------------
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(GoldDeep, Gold, GoldLight)),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text("SHARE & EARN", color = Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (pricing.referralPayout > 0)
                                "Earn PKR ${pricing.referralPayout.toInt()} for every friend who unlocks!"
                            else
                                "Earn money for every friend who unlocks!",
                            color = Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Share your username below — when a friend pays with your referral, you get paid.",
                            color = Black.copy(alpha = 0.75f),
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(14.dp))
                        // Referral code box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Black.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "@${unlockInfo?.username ?: ""}",
                                color = Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        GoldButton(
                            text = "Share Now & Earn",
                            onClick = {
                                val text = "Watch unlimited movies on MoviesForever! Pay once, unlock forever. Use my referral username: ${unlockInfo?.username}. Earn PKR ${pricing.referralPayout.toInt()} when I refer you! 💰🎬"
                                onShareReferral(text)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // My referrals count
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onReferralClick)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👥", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Text("My Referrals", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Text("0", color = GoldLight, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Settings
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSettings)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚙", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Text("Settings", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Text("›", color = TextMuted, fontSize = 24.sp)
                }
            }
        } else {
            // Not unlocked - encourage unlock
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔒", fontSize = 40.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Unlock to start earning with referrals!",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Members earn money every time a friend unlocks through their referral.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(14.dp))
                    GoldButton(
                        text = if (pricing.standardPrice > 0)
                            "Unlock for PKR ${pricing.standardPrice.toInt()}"
                        else
                            "Unlock Lifetime Access",
                        onClick = onUnlockClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSettings)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚙", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Text("Settings", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Text("›", color = TextMuted, fontSize = 24.sp)
                }
            }
        }
    }
}
