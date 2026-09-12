package com.moviesforever.app.ui.screen.paused

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.ui.theme.Black
import com.moviesforever.app.ui.theme.DarkSurface
import com.moviesforever.app.ui.theme.Gold
import com.moviesforever.app.ui.theme.TextMuted
import com.moviesforever.app.ui.theme.TextPrimary

/**
 * Shown, full-screen and exclusively, whenever the admin has paused this
 * user's account (`users/{id}.paused == true`, set from the admin panel's
 * User Management page). While this is on screen the user has no way to
 * reach any other part of the app -- no bottom bar, no back-to-content
 * action, nothing to tap. The only content is whatever free-text note the
 * admin wrote for this user (`pauseUserNote`); the app never invents its
 * own copy here, matching how [com.moviesforever.app.ui.components.AdminNoteBanner]
 * treats the pricing note.
 *
 * The back button is intentionally swallowed rather than left to pop the
 * nav stack: the caller (NavHost) already clears the back stack down to
 * this screen when pausing, so an unhandled back press here would exit
 * the activity, which is fine, but we swallow it anyway so a stray
 * leftover back-stack entry can never be used to sneak back into content.
 */
@Composable
fun PausedScreen(note: String) {
    BackHandler(enabled = true) { /* no-op: nothing to navigate back to */ }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .background(DarkSurface, CircleShape)
                    .border(1.dp, Gold.copy(alpha = 0.35f), CircleShape)
                    .padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PauseCircleFilled,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(48.dp)
                )
            }

            Text(
                text = "Streaming Paused",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 20.dp)
            )

            val trimmedNote = note.trim()
            if (trimmedNote.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .background(DarkSurface, RoundedCornerShape(14.dp))
                        .border(1.dp, Gold.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = trimmedNote,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Text(
                    text = "Your account's streaming access has been paused. Please contact support for more details.",
                    color = TextMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
