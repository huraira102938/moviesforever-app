package com.moviesforever.app.ui.screen.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.moviesforever.app.R
import com.moviesforever.app.ui.components.AdminNoteBanner
import com.moviesforever.app.ui.components.GoldButton
import com.moviesforever.app.ui.theme.Black
import com.moviesforever.app.ui.theme.TextMuted
import com.moviesforever.app.ui.theme.TextPrimary

/**
 * Shown exactly once per install, before Lock/Home, while the local
 * "isInstalled" flag is still false. Displays the admin-controlled note
 * (same `settings/pricing`.note used on the pricing card, lock screen, and
 * home screen) and a "Start Browsing" button. Tapping the button is what
 * flips the local flag to true (so this screen never shows again on this
 * device) and increments the Firestore install counter.
 */
@Composable
fun WelcomeScreen(
    note: String,
    onStartBrowsing: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "MoviesForever Logo",
                modifier = Modifier.size(96.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Welcome to MoviesForever",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Your one-time pass to unlimited entertainment.",
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            AdminNoteBanner(note = note, textAlign = TextAlign.Center)

            Spacer(Modifier.height(32.dp))

            GoldButton(
                text = "Start Browsing",
                onClick = onStartBrowsing,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
