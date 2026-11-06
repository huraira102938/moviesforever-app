package com.moviesforever.app.ui.screen.payment

import android.view.WindowManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.data.model.ContactDetails
import com.moviesforever.app.data.model.PaymentDetails
import com.moviesforever.app.data.model.PricingSettings
import com.moviesforever.app.data.remote.Market
import com.moviesforever.app.ui.components.GoldButton
import com.moviesforever.app.ui.screen.player.findActivity
import com.moviesforever.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val WhatsAppGreen = Color(0xFF25D366)

/**
 * Payment page, fully in Roman Urdu and kept simple:
 *   price -> 3 steps -> account details (with copy) -> send receipt on WhatsApp -> enter code.
 *
 * All text is Roman Urdu; only the price, the account details and the code the person types are
 * shown exactly as stored/typed.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PaymentInstructionsScreen(
    pricing: PricingSettings,
    paymentDetails: PaymentDetails,
    @Suppress("UNUSED_PARAMETER") contactDetails: ContactDetails, // kept so the NavHost call does not change
    onSendScreenshotWhatsApp: () -> Unit,
    onRedeemCode: (String, String) -> Unit,
    redeeming: Boolean,
    onBack: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var codeId by remember { mutableStateOf("") }
    var signInUsername by remember { mutableStateOf("") }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val codeCardRequester = remember { BringIntoViewRequester() }

    // Only this page shrinks for the keyboard; elsewhere (Search) the bottom bar must stay down.
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        val previousMode = window?.attributes?.softInputMode
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        onDispose { previousMode?.let { window.setSoftInputMode(it) } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
            .imePadding()
    ) {
        // Top bar: back button + title
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
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Payment",
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
                                text = "Price",
                                color = TextSecondary,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "For one full year",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "${Market.CURRENCY} ${pricing.standardPrice.toInt()}",
                            color = GoldLight,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // 2. Three simple steps
            SectionTitle("How to Pay")
            Spacer(Modifier.height(10.dp))

            StepRow("1", "Transfer the Amount", "Send the payment to the IBAN below.")
            Spacer(Modifier.height(8.dp))
            StepRow("2", "Send the Receipt", "Send a screenshot of your payment on WhatsApp.")
            Spacer(Modifier.height(8.dp))
            StepRow("3", "Get Your Code", "Our team will check it and send you a code and username.")

            Spacer(Modifier.height(24.dp))

            // 3. Account details, each with a copy button
            SectionTitle("Account Details")
            Spacer(Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkElevated, RoundedCornerShape(16.dp))
            ) {
                Column(Modifier.padding(14.dp)) {
                    DetailItem("Bank Name", paymentDetails.bankName, clipboard)
                    Spacer(Modifier.height(8.dp))
                    DetailItem("Account Title", paymentDetails.accountTitle, clipboard)
                    Spacer(Modifier.height(8.dp))
                    DetailItem("IBAN", paymentDetails.accountNumber, clipboard)
                }
            }

            Spacer(Modifier.height(20.dp))

            // 4. Send receipt: WhatsApp green, so it stands apart from the gold "open account" button below
            Button(
                onClick = { onSendScreenshotWhatsApp() },
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
                    text = "Send Receipt on WhatsApp",
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
                    .bringIntoViewRequester(codeCardRequester)
                    .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Got Your Code and Username?",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Enter them here to unlock your account.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    // Keeps both fields and the button above the keyboard once it has finished opening.
                    val showWholeCard = Modifier.onFocusChanged { focus ->
                        if (focus.isFocused) scope.launch {
                            delay(350)
                            codeCardRequester.bringIntoView()
                        }
                    }
                    OutlinedTextField(
                        value = codeId,
                        onValueChange = { codeId = it },
                        modifier = Modifier.fillMaxWidth().then(showWholeCard),
                        label = { Text("Code") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = signInUsername,
                        onValueChange = { signInUsername = it },
                        modifier = Modifier.fillMaxWidth().then(showWholeCard),
                        label = { Text("Username") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            onRedeemCode(codeId, signInUsername)
                        }),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors()
                    )
                    Spacer(Modifier.height(14.dp))

                    GoldButton(
                        text = "Unlock Account",
                        onClick = { onRedeemCode(codeId, signInUsername) },
                        loading = redeeming,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                    contentDescription = "Copy",
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