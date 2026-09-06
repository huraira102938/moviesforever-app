package com.moviesforever.app.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
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
        Spacer(Modifier.height(14.dp))

        // Top Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 20.dp)
                    .background(Gold, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(8.dp))
            Text("My Profile", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        // User Avatar Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(DarkSurface, CircleShape)
                    .border(2.dp, Gold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile Avatar",
                    tint = Gold,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = unlockInfo?.username ?: "Free Preview User",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            if (isUnlocked) {
                Surface(
                    color = Gold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold)
                ) {
                    Text(
                        text = "⭐ Lifetime Member",
                        color = Gold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            } else {
                Surface(
                    color = DarkElevated,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Free Access",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        if (isUnlocked) {
            // High-Converting Share & Earn Card
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
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = null,
                                tint = Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("SHARE & EARN", color = Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = if (pricing.referralPayout > 0)
                                "Earn PKR ${pricing.referralPayout.toInt()} for every friend who unlocks!"
                            else
                                "Earn money for every friend who unlocks!",
                            color = Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Share your referral username below — when a friend unlocks with your handle, you get paid.",
                            color = Black.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(Modifier.height(14.dp))

                        // Referral Code Display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
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
                            text = "Share & Earn Now",
                            onClick = {
                                val text = "Watch unlimited movies on MoviesForever! Pay once, unlock forever. Use my referral username: ${unlockInfo?.username}. Earn PKR ${pricing.referralPayout.toInt()} when I refer you! 💰🎬"
                                onShareReferral(text)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // My Referrals Row
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkElevated, RoundedCornerShape(14.dp))
                    .clickable(onClick = onReferralClick)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("My Referrals", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Settings Row
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkElevated, RoundedCornerShape(14.dp))
                    .clickable(onClick = onSettings)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Settings", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        } else {
            // Not Unlocked Option Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(DarkElevated, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Unlock to Start Earning!",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Members earn rewards every time a friend unlocks through their referral username.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Spacer(Modifier.height(16.dp))

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

            Spacer(Modifier.height(10.dp))

            // Settings Row
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkElevated, RoundedCornerShape(14.dp))
                    .clickable(onClick = onSettings)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Settings", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}