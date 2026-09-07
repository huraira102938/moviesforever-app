package com.moviesforever.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.moviesforever.app.ui.navigation.MoviesForeverNavHost
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
    viewModel: com.moviesforever.app.ui.viewmodel.AppViewModel = hiltViewModel()
) {
    MoviesForeverNavHost(viewModel = viewModel)
}
