package com.moviesforever.app.ui.screen.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.moviesforever.app.R
import com.moviesforever.app.ui.theme.Black
import com.moviesforever.app.ui.viewmodel.InstallCheckState
import com.moviesforever.app.ui.viewmodel.UnlockCheckState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * [unlockCheckState] and [installCheckState] are fast, local-only (DataStore)
 * signals from AppViewModel -- NOT the full network-backed AppUiState. This
 * screen waits for both to leave their `Loading` state before finishing, so
 * the routing decision (Welcome vs Lock vs Main) never gets made against
 * stale default state (see AppViewModel.UnlockCheckState doc for the bug this
 * fixes: premium users intermittently seeing the lock screen because the
 * routing decision used to depend on slow network calls).
 *
 * A short minimum display time is kept purely for branding/UX polish, run in
 * parallel with (not blocking) the real checks.
 */
@Composable
fun SplashScreen(
    unlockCheckState: Flow<UnlockCheckState>,
    installCheckState: Flow<InstallCheckState>,
    onFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        // Run the minimum branding delay and the real status waits
        // concurrently, so a slow local check (rare, disk is fast) doesn't add
        // to the fixed 800ms, and a fast check doesn't skip the branding delay.
        coroutineScope {
            val minDisplay = launch { delay(800) }
            val waitForUnlockCheck = launch {
                unlockCheckState.filter { it != UnlockCheckState.Loading }.first()
            }
            val waitForInstallCheck = launch {
                installCheckState.filter { it != InstallCheckState.Loading }.first()
            }
            minDisplay.join()
            waitForUnlockCheck.join()
            waitForInstallCheck.join()
        }
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "MoviesForever Logo",
                modifier = Modifier
                    .size(160.dp)
                    .alpha(1f)
            )
        }
    }
}