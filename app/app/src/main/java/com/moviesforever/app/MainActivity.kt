package com.moviesforever.app

import android.Manifest
import android.content.Intent
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.moviesforever.app.data.repository.CatalogueRepository
import com.moviesforever.app.push.PushNotifier
import com.moviesforever.app.ui.navigation.MoviesForeverNavHost
import com.moviesforever.app.ui.screen.update.ForceUpdateScreen
import com.moviesforever.app.ui.theme.Black
import com.moviesforever.app.ui.theme.Gold
import com.moviesforever.app.ui.viewmodel.AppUpdateViewModel
import com.moviesforever.app.ui.theme.MoviesForeverTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var catalogueRepository: CatalogueRepository

    /** True from a tapped push until the Notifications screen has been opened. */
    private var openNotifications by mutableStateOf(false)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Back closes the app but keeps the process, so a fresh open must ask for a new order.
        if (savedInstanceState == null) {
            catalogueRepository.reshuffle()
            handlePushIntent(intent)
        }

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
                MoviesForeverApp(
                    openNotifications = openNotifications,
                    onNotificationsOpened = { openNotifications = false }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePushIntent(intent)
    }

    /** Pushes carry notificationId, both Android's own (app closed) and ours (app open). */
    private fun handlePushIntent(intent: Intent) {
        val fromRecents = intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0
        if (intent.hasExtra(PushNotifier.EXTRA_NOTIFICATION_ID) && !fromRecents) {
            intent.removeExtra(PushNotifier.EXTRA_NOTIFICATION_ID)
            openNotifications = true
        }
    }
}

@Composable
fun MoviesForeverApp(
    openNotifications: Boolean,
    onNotificationsOpened: () -> Unit,
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

        else -> AppContent(openNotifications, onNotificationsOpened)
    }
}

/** The real app. Only composed when this build is allowed to run, so no data is loaded otherwise. */
@Composable
private fun AppContent(
    openNotifications: Boolean,
    onNotificationsOpened: () -> Unit,
    viewModel: com.moviesforever.app.ui.viewmodel.AppViewModel = hiltViewModel()
) {
    MoviesForeverNavHost(
        viewModel = viewModel,
        openNotifications = openNotifications,
        onNotificationsOpened = onNotificationsOpened
    )
}
