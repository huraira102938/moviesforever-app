package com.moviesforever.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.ui.theme.DarkSurface
import com.moviesforever.app.ui.theme.TextPrimary
import com.moviesforever.app.ui.theme.TextSecondary

/**
 * Urdu text: laid out right-to-left and aligned to the right, even though the rest of the app is
 * left-to-right. Use it for every Urdu sentence so punctuation and alignment look correct.
 */
@Composable
fun UrduText(
    text: String,
    color: Color,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            // Urdu script needs more line spacing than Latin text to stay readable.
            lineHeight = fontSize * 1.75f,
            textAlign = TextAlign.Start, // "Start" is the RIGHT edge in right-to-left
            modifier = modifier
        )
    }
}

/**
 * A highlighted card with an Urdu title and an Urdu paragraph. [accent] colours the border, icon
 * and title so a person who cannot read can still tell what kind of message it is
 * (for example green = free, red = payment needed).
 *
 * Keep the text free of prices and numbers: those change and are shown elsewhere in the app.
 */
@Composable
fun UrduNote(
    title: String,
    body: String,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    UrduText(
                        text = title,
                        color = accent,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                UrduText(
                    text = body,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** Convenience for a short Urdu line in the app's main text colour. */
@Composable
fun UrduLine(text: String, modifier: Modifier = Modifier, fontSize: TextUnit = 13.sp) {
    UrduText(text = text, color = TextPrimary, fontSize = fontSize, modifier = modifier)
}
