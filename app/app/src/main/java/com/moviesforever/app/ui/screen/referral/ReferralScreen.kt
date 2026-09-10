package com.moviesforever.app.ui.screen.referral

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.moviesforever.app.data.model.AppShareLink
import com.moviesforever.app.data.model.PricingSettings
import com.moviesforever.app.data.model.ReferralEarnings
import com.moviesforever.app.data.model.UnlockInfo
import com.moviesforever.app.data.model.UserAccount
import com.moviesforever.app.data.model.buildShareMessage
import com.moviesforever.app.ui.components.GoldButton
import com.moviesforever.app.ui.theme.*

@Composable
fun ReferralScreen(
    unlockInfo: UnlockInfo?,
    pricing: PricingSettings,
    account: UserAccount?,
    earnings: ReferralEarnings,
    appShareLink: AppShareLink?,
    onShareApk: (String) -> Unit,
    onBack: () -> Unit
) {
    val username = unlockInfo?.username
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("@$username", color = GoldLight, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(username))
                                Toast.makeText(context, "Username copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy username",
                                tint = GoldLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    GoldButton(
                        text = "Share App & Earn Money",
                        enabled = appShareLink != null && appShareLink.apkUrl.isNotBlank(),
                        onClick = {
                            appShareLink?.let { link ->
                                onShareApk(link.buildShareMessage(username))
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
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
                            value = "PKR ${earnings.pendingAmount.toInt()}",
                            valueColor = Warning,
                            modifier = Modifier.weight(1f)
                        )
                        StatBlock(
                            label = "Already sent",
                            value = "PKR ${earnings.paidAmount.toInt()}",
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

            Spacer(Modifier.height(20.dp))

            // Payout details
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
                    DetailRow(
                        label = "${account?.paymentMethodLabel ?: "Payment"} Number",
                        value = account?.effectivePaymentNumber?.takeIf { it.isNotBlank() } ?: "—"
                    )
                    Spacer(Modifier.height(10.dp))
                    DetailRow(
                        label = "Account Title",
                        value = account?.effectiveAccountTitle?.takeIf { it.isNotBlank() } ?: "—"
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "If your payment number or title is wrong, or you want to change it, please contact admin.",
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
