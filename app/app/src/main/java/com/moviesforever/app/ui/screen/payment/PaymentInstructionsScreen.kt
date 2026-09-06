package com.moviesforever.app.ui.screen.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.data.model.PricingSettings
import com.moviesforever.app.ui.components.GoldButton
import com.moviesforever.app.ui.theme.*

@Composable
fun PaymentInstructionsScreen(
    pricing: PricingSettings,
    onSendScreenshotWhatsApp: (referralUsername: String) -> Unit,
    onBack: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var referralUsername by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .background(DarkSurface, CircleShape)
                    .border(1.dp, DarkElevated, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Lifetime Pass Checkout",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Limited Time Header Callout
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Error.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = null,
                            tint = Error,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LIMITED OFFER • First 500 Users Only",
                            color = Error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (pricing.standardPrice > 0)
                                "PKR ${pricing.standardPrice.toInt()} • One-Time Payment"
                            else
                                "One-Time Payment",
                            color = GoldLight,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Section 1: Membership Benefits
            Text(
                text = "What You Get",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            BenefitRow("Full access to all current & future movies/shows")
            BenefitRow("Encrypted offline downloads for watching anywhere")
            BenefitRow("Fast streaming servers with zero ads")
            BenefitRow("No monthly renewal or hidden fees, ever")

            Spacer(Modifier.height(24.dp))

            // Section 2: How It Works (Step-by-Step)
            Text(
                text = "3 Easy Steps to Unlock",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            StepCard(
                stepNumber = "1",
                title = "Send Payment",
                description = "Transfer PKR ${pricing.standardPrice.toInt()} to the bank account details provided below."
            )

            Spacer(Modifier.height(10.dp))

            StepCard(
                stepNumber = "2",
                title = "Share Screenshot",
                description = "Click 'Send Screenshot on WhatsApp' below and attach your payment receipt to +92 03264304455."
            )

            Spacer(Modifier.height(10.dp))

            StepCard(
                stepNumber = "3",
                title = "Get Account Credentials",
                description = "Once verified, our team will send your unique Code ID & Username to unlock your account forever."
            )

            Spacer(Modifier.height(24.dp))

            // Section 3: Official Bank Details Box
            Text(
                text = "Payment Transfer Details",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkElevated, RoundedCornerShape(18.dp))
            ) {
                Column(Modifier.padding(16.dp)) {
                    PaymentDetailItem(
                        label = "Bank Name",
                        value = "Faysal Bank",
                        clipboard = clipboard
                    )
                    Spacer(Modifier.height(10.dp))
                    PaymentDetailItem(
                        label = "Account Title",
                        value = "ABU HURAIRA",
                        clipboard = clipboard
                    )
                    Spacer(Modifier.height(10.dp))
                    PaymentDetailItem(
                        label = "IBAN Number",
                        value = "PK92FAYS3291301000005223",
                        clipboard = clipboard
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Section 4: Optional Referral Username Field
            OutlinedTextField(
                value = referralUsername,
                onValueChange = { referralUsername = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Referral Username (Optional)") },
                placeholder = { Text("e.g. john123", color = TextMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = DarkElevated,
                    focusedLabelColor = Gold,
                    unfocusedLabelColor = TextMuted,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Gold
                )
            )
            if (pricing.referralPayout > 0) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "If someone referred you, enter their username so they receive PKR ${pricing.referralPayout.toInt()} once verified.",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            // Action Call to Action Button
            GoldButton(
                text = "Send Screenshot on WhatsApp",
                onClick = { onSendScreenshotWhatsApp(referralUsername) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LockOpen,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Verification typically takes less than 5 minutes",
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BenefitRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun StepCard(stepNumber: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(14.dp))
            .border(1.dp, DarkElevated, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Gold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun PaymentDetailItem(label: String, value: String, clipboard: ClipboardManager) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkElevated, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        IconButton(
            onClick = { clipboard.setText(AnnotatedString(value)) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = "Copy $label",
                tint = Gold,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}