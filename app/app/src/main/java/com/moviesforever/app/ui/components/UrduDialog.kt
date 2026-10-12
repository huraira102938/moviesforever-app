package com.moviesforever.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.moviesforever.app.ui.theme.DarkSurface
import com.moviesforever.app.ui.theme.Error
import com.moviesforever.app.ui.theme.Gold
import com.moviesforever.app.ui.theme.TextPrimary
import com.moviesforever.app.ui.theme.TextSecondary

/**
 * A simple error dialog in Urdu (right-to-left): red icon, short title, one message, one button.
 * Use it for mistakes the person must notice and fix, such as a wrong code.
 */
@Composable
fun UrduErrorDialog(
    message: String,
    onDismiss: () -> Unit,
    title: String = "غلطی",
    buttonText: String = "ٹھیک ہے"
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = DarkSurface,
            icon = {
                Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = Error
                )
            },
            title = {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = message,
                    color = TextSecondary,
                    fontSize = 16.sp,
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = buttonText,
                        color = Gold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}
