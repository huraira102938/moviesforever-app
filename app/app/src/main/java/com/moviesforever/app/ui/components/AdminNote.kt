package com.moviesforever.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.moviesforever.app.ui.theme.DarkSurface
import com.moviesforever.app.ui.theme.Gold
import com.moviesforever.app.ui.theme.TextPrimary

/**
 * Renders whatever free-text note the admin has configured in the admin
 * panel's Pricing Settings page (`settings/pricing`.note), verbatim. The app
 * never hardcodes offer/promo copy -- content, emoji, formatting, and
 * whether there even is a note at all is entirely up to the admin.
 *
 * Renders nothing if [note] is blank, so screens don't show an empty box
 * while pricing is still loading or if the admin hasn't set a note.
 */
@Composable
fun AdminNoteBanner(
    note: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    val trimmed = note.trim()
    if (trimmed.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(14.dp))
            .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(
            text = trimmed,
            color = TextPrimary,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Medium,
            textAlign = textAlign
        )
    }
}
