package com.moviesforever.app.ui.screen.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.data.model.ContactDetails
import com.moviesforever.app.data.model.PaymentDetails
import com.moviesforever.app.data.model.PricingSettings
import com.moviesforever.app.ui.components.GoldButton
import com.moviesforever.app.ui.theme.*

private val WhatsAppGreen = Color(0xFF25D366)

/**
 * Payment page, fully in Urdu and kept simple:
 *   price -> 3 steps -> account details (with copy) -> send receipt on WhatsApp -> enter code.
 *
 * The whole page is laid out right-to-left. All text is Urdu; only the price, the account details
 * and the code the person types are shown exactly as stored/typed.
 */
@Composable
fun PaymentInstructionsScreen(
    pricing: PricingSettings,
    paymentDetails: PaymentDetails,
    @Suppress("UNUSED_PARAMETER") contactDetails: ContactDetails, // kept so the NavHost call does not change
    onSendScreenshotWhatsApp: (referralUsername: String) -> Unit,
    onRedeemCode: (String, String) -> Unit,
    redeeming: Boolean,
    onBack: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var codeId by remember { mutableStateOf("") }
    var signInUsername by remember { mutableStateOf("") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Black)
                .statusBarsPadding()
        ) {
            // Top bar: back button (on the right, as in any Urdu app) + title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(DarkSurface, CircleShape)
                        .border(1.dp, DarkElevated, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "واپس",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "ادائیگی کریں",
                    color = TextPrimary,
                    fontSize = 20.sp,
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

                // 1. Price (only if the admin has set one)
                if (pricing.standardPrice > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "قیمت",
                                    color = TextSecondary,
                                    fontSize = 16.sp
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "پورے سال کے لیے",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "${pricing.standardPrice.toInt()} روپے",
                                color = GoldLight,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // 2. Three simple steps
                SectionTitle("ادائیگی کیسے کریں؟")
                Spacer(Modifier.height(10.dp))

                StepRow("۱", "رقم بھیجیں", "نیچے دیے گئے اکاؤنٹ میں رقم بھیجیں۔")
                Spacer(Modifier.height(8.dp))
                StepRow("۲", "رسید بھیجیں", "ادائیگی کی رسید (اسکرین شاٹ) واٹس ایپ پر بھیجیں۔")
                Spacer(Modifier.height(8.dp))
                StepRow("۳", "کوڈ حاصل کریں", "ہماری ٹیم چیک کر کے آپ کو کوڈ اور یوزرنیم بھیج دے گی۔")

                Spacer(Modifier.height(24.dp))

                // 3. Account details, each with a copy button
                SectionTitle("اکاؤنٹ کی تفصیل")
                Spacer(Modifier.height(10.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkElevated, RoundedCornerShape(16.dp))
                ) {
                    Column(Modifier.padding(14.dp)) {
                        DetailItem("بینک کا نام", paymentDetails.bankName, clipboard)
                        Spacer(Modifier.height(8.dp))
                        DetailItem("اکاؤنٹ کا نام", paymentDetails.accountTitle, clipboard)
                        Spacer(Modifier.height(8.dp))
                        DetailItem("اکاؤنٹ نمبر", paymentDetails.accountNumber, clipboard)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // 4. Send receipt: WhatsApp green, so it stands apart from the gold "open account" button below
                Button(
                    onClick = { onSendScreenshotWhatsApp("") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhatsAppGreen,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "رسید واٹس ایپ پر بھیجیں",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(28.dp))

                // 5. Already have the code? Activate here.
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "کوڈ اور یوزرنیم مل گیا؟",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "یہاں لکھیں اور اپنا اکاؤنٹ کھولیں۔",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = codeId,
                            onValueChange = { codeId = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("کوڈ") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = signInUsername,
                            onValueChange = { signInUsername = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("یوزرنیم") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors()
                        )
                        Spacer(Modifier.height(14.dp))

                        GoldButton(
                            text = "اکاؤنٹ کھولیں",
                            onClick = { onRedeemCode(codeId, signInUsername) },
                            loading = redeeming,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = TextPrimary,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun StepRow(number: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(14.dp))
            .border(1.dp, DarkElevated, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Gold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, clipboard: ClipboardManager) {
    val shown = value.ifBlank { "—" }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkElevated, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextMuted, fontSize = 13.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                text = shown,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (value.isNotBlank()) {
            IconButton(
                onClick = { clipboard.setText(AnnotatedString(value)) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = "کاپی کریں",
                    tint = Gold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = DarkSurface,
    unfocusedContainerColor = DarkSurface,
    focusedBorderColor = Gold,
    unfocusedBorderColor = DarkElevated,
    focusedLabelColor = Gold,
    unfocusedLabelColor = TextMuted,
    cursorColor = Gold,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)