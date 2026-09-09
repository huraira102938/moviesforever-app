package com.moviesforever.app.ui.screen.referral

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.data.model.BonusStatus
import com.moviesforever.app.data.model.PricingSettings
import com.moviesforever.app.data.model.UnlockInfo
import com.moviesforever.app.data.model.UserAccount
import com.moviesforever.app.ui.components.GoldButton
import com.moviesforever.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun ReferralScreen(
    unlockInfo: UnlockInfo?,
    pricing: PricingSettings,
    account: UserAccount?,
    bonusStatus: BonusStatus?,
    apkShareUrl: String,
    onShare: (String) -> Unit,
    onShareApk: (String) -> Unit,
    onBack: () -> Unit
) {
    val username = unlockInfo?.username
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("←", fontSize = 20.sp) }
            Text("My Referrals", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        if (username != null) {
            // Referral username + share card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkElevated, RoundedCornerShape(16.dp))
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
                    if (apkShareUrl.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                val text = "Download MoviesForever and watch unlimited movies! Get the app here: $apkShareUrl\n\nWhen you unlock, use my referral username \"$username\" so I earn a reward too. 🎬💰"
                                onShareApk(text)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                            border = BorderStroke(1.dp, Gold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Share App (APK Link)")
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // How your friend gets you credit
            InfoCard(
                icon = Icons.Filled.Info,
                title = "Tell your friend this",
                body = "When your friend sends their payment screenshot to admin, remind them to also send your username \"$username\" as their referral — that's the only way you get credited for it."
            )

            Spacer(Modifier.height(16.dp))

            // General referrals section
            SectionHeader(title = "General Referrals")
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkElevated, RoundedCornerShape(16.dp))
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatBlock(
                            label = "Referrals done",
                            value = "${account?.referralCount ?: 0}",
                            modifier = Modifier.weight(1f)
                        )
                        StatBlock(
                            label = "Pending",
                            value = "PKR ${(account?.generalPendingAmount ?: 0.0).toInt()}",
                            valueColor = Warning,
                            modifier = Modifier.weight(1f)
                        )
                        StatBlock(
                            label = "Already sent",
                            value = "PKR ${(account?.generalPaidAmount ?: 0.0).toInt()}",
                            valueColor = Success,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = DarkElevated)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Referral payments usually take 2–3 days to process. If you don't receive your payment after that, please message admin.",
                        color = TextSecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // Bonus deal section
            if (bonusStatus != null) {
                Spacer(Modifier.height(20.dp))
                SectionHeader(title = "Bonus Deal")
                Spacer(Modifier.height(8.dp))
                BonusDealCard(bonusStatus = bonusStatus)
            }

            Spacer(Modifier.height(20.dp))

            // JazzCash payout details
            SectionHeader(title = "Your Payout Details")
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkElevated, RoundedCornerShape(16.dp))
            ) {
                Column(Modifier.padding(18.dp)) {
                    DetailRow(label = "JazzCash Number", value = account?.jazzCashNumber?.takeIf { it.isNotBlank() } ?: "—")
                    Spacer(Modifier.height(10.dp))
                    DetailRow(label = "Account Title", value = account?.jazzCashTitle?.takeIf { it.isNotBlank() } ?: "—")
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "If your JazzCash number or title is wrong, or you want to change it, please contact admin.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "How it works:\n1. Share your username (or the app link above) with friends.\n2. When they pay, they enter your username as the referral.\n3. Admin verifies & pays you the referral payout.",
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

@Composable
private fun SectionHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 16.dp)
                .background(Gold, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimary
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(label, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 13.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun InfoCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, body: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Info.copy(alpha = 0.10f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Info.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
    ) {
        Row(Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = Info, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(body, color = TextSecondary, fontSize = 12.5.sp, lineHeight = 17.sp)
            }
        }
    }
}

private val isoDisplayFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
private val prettyDateFormat = SimpleDateFormat("d MMM yyyy", Locale.US)

private fun formatValidUntil(iso: String): String = try {
    isoDisplayFormat.parse(iso)?.let { prettyDateFormat.format(it) } ?: iso
} catch (e: Exception) {
    iso
}

@Composable
private fun BonusDealCard(bonusStatus: BonusStatus) {
    val deal = bonusStatus.deal
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
                .padding(18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = Black, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("LIMITED-TIME BONUS", color = Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = deal.tagline.ifBlank { "Refer more friends and unlock an extra bonus!" },
                    color = Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 21.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Valid until ${formatValidUntil(deal.validUntil)}",
                    color = Black.copy(alpha = 0.75f),
                    fontSize = 11.5.sp
                )
                Spacer(Modifier.height(14.dp))

                when {
                    bonusStatus.alreadyPaidOut -> {
                        BonusStateBanner(
                            containerColor = Success.copy(alpha = 0.18f),
                            icon = Icons.Filled.CheckCircle,
                            iconTint = Success,
                            text = "Bonus of PKR ${deal.bonusAmount.toInt()} has already been transferred to your bank account. 🎉"
                        )
                    }
                    bonusStatus.isTargetReached -> {
                        BonusStateBanner(
                            containerColor = Black.copy(alpha = 0.85f),
                            icon = Icons.Filled.CheckCircle,
                            iconTint = GoldLight,
                            text = "🎉 Congratulations! You've successfully unlocked the bonus of PKR ${deal.bonusAmount.toInt()}. Message admin for withdrawal to your bank account."
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "${bonusStatus.unlocksAchieved} of ${deal.unlocksRequired} unlocks",
                                    color = Black,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${bonusStatus.unlocksRemaining} more ${if (bonusStatus.unlocksRemaining == 1) "person" else "people"} need to unlock using your username to get an extra PKR ${deal.bonusAmount.toInt()}.",
                                    color = Black.copy(alpha = 0.8f),
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BonusStateBanner(
    containerColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = TextPrimary, fontSize = 12.5.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
    }
}
