package com.moviesforever.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.moviesforever.app.ui.navigation.MoviesForeverNavHost
import com.moviesforever.app.ui.screen.update.ForceUpdateScreen
import com.moviesforever.app.ui.theme.Black
import com.moviesforever.app.ui.theme.Gold
import com.moviesforever.app.ui.viewmodel.AppUpdateViewModel
import com.moviesforever.app.ui.theme.MoviesForeverTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Required on Android 13+ for the download-progress notification to show.
        // Downloads still work without it, the user just won't see progress in the
        // notification shade - so this is best-effort and not gating anything.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MoviesForeverTheme {
                MoviesForeverApp()
            }
        }
    }
}

@Composable
fun MoviesForeverApp(
    updateViewModel: AppUpdateViewModel = hiltViewModel()
) {
    val gate by updateViewModel.gate.collectAsState()

    when {
        // Waiting for the force-update answer (max ~3 seconds).
        !gate.checked -> Box(
            modifier = Modifier.fillMaxSize().background(Black),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = Gold) }

        // This build is too old: nothing else is created or shown.
        gate.blocked -> ForceUpdateScreen(
            config = gate.config,
            currentVersionCode = gate.currentVersionCode
        )

        else -> AppContent()
    }
}

/** The real app. Only composed when this build is allowed to run, so no data is loaded otherwise. */
@Composable
private fun AppContent(
    viewModel: com.moviesforever.app.ui.viewmodel.AppViewModel = hiltViewModel()
) {
    MoviesForeverNavHost(viewModel = viewModel)
}
