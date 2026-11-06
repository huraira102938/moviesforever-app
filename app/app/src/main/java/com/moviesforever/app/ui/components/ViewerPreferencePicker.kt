package com.moviesforever.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.data.model.ViewerPreference
import com.moviesforever.app.ui.theme.DarkElevated
import com.moviesforever.app.ui.theme.DarkSurface
import com.moviesforever.app.ui.theme.Gold
import com.moviesforever.app.ui.theme.TextPrimary
import com.moviesforever.app.ui.theme.TextSecondary
import com.moviesforever.app.ui.tv.tvFocusHighlight

/** The three first-launch choices, also used to change the answer later from Profile. */
@Composable
fun ViewerPreferenceOptions(
    selected: ViewerPreference?,
    onSelect: (ViewerPreference) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ViewerPreference.entries.forEach { option ->
            val isSelected = option == selected
            val shape = RoundedCornerShape(14.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .tvFocusHighlight(shape, focusedScale = 1.02f)
                    .clip(shape)
                    .background(if (isSelected) Gold.copy(alpha = 0.15f) else DarkSurface)
                    .border(2.dp, if (isSelected) Gold else DarkElevated, shape)
                    .clickable { onSelect(option) }
                    .padding(16.dp)
            ) {
                Text(option.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(option.subtitle, color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ViewerPreferenceDialog(
    selected: ViewerPreference?,
    onSelect: (ViewerPreference) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("What do you like to watch?", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = { ViewerPreferenceOptions(selected = selected, onSelect = onSelect) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = Gold) }
        }
    )
}
