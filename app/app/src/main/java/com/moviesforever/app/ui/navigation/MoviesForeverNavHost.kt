package com.moviesforever.app.ui.navigation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moviesforever.app.data.repository.DownloadRequestResult
import com.moviesforever.app.ui.components.MoviesBottomBar
import com.moviesforever.app.ui.screen.category.CategoryBrowseScreen
import com.moviesforever.app.ui.screen.celebration.CelebrationScreen
import com.moviesforever.app.ui.screen.detail.MovieDetailScreen
import com.moviesforever.app.ui.screen.downloads.DownloadsScreen
import com.moviesforever.app.ui.screen.home.HomeScreen
import com.moviesforever.app.ui.screen.lock.LockScreen
import com.moviesforever.app.ui.screen.payment.PaymentInstructionsScreen
import com.moviesforever.app.ui.screen.player.PlayerScreen
import com.moviesforever.app.ui.screen.player.findActivity
import com.moviesforever.app.ui.screen.profile.ProfileScreen
import com.moviesforever.app.ui.screen.referral.ReferralScreen
import com.moviesforever.app.ui.screen.search.SearchScreen
import com.moviesforever.app.ui.screen.settings.SettingsScreen
import com.moviesforever.app.ui.screen.splash.SplashScreen
import com.moviesforever.app.ui.screen.welcome.WelcomeScreen
import com.moviesforever.app.ui.theme.Black
import com.moviesforever.app.ui.viewmodel.AppViewModel
import com.moviesforever.app.ui.viewmodel.InstallCheckState
import com.moviesforever.app.ui.viewmodel.LockViewModel
import com.moviesforever.app.ui.viewmodel.UnlockCheckState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MoviesForeverNavHost(
    viewModel: AppViewModel,
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val lockViewModel: LockViewModel = hiltViewModel()

    // Selected tab state
    var currentTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Black,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { outerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(outerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    unlockCheckState = viewModel.unlockCheckState,
                    installCheckState = viewModel.installCheckState,
                    onFinished = {
                        // IMPORTANT: these read the fast, local-only unlock/install
                        // checks (DataStore), not uiState, which also waits on
                        // network calls (movies/categories/pricing via Firestore).
                        // Gating this decision on network content previously caused
                        // premium users to intermittently see the Lock screen
                        // whenever those network calls were slow to resolve.
                        val isInstalled = viewModel.installCheckState.value is InstallCheckState.Installed
                        if (!isInstalled) {
                            // First launch on this device: show the one-time
                            // welcome step before Lock/Main.
                            navController.navigate(Screen.Welcome.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        } else {
                            val isUnlocked = viewModel.unlockCheckState.value is UnlockCheckState.Unlocked
                            if (isUnlocked) {
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.Lock.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        }
                    }
                )
            }

            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    note = uiState.pricing.note,
                    onStartBrowsing = {
                        viewModel.markInstalled()
                        val isUnlocked = viewModel.unlockCheckState.value is UnlockCheckState.Unlocked
                        val destination = if (isUnlocked) Screen.Main.route else Screen.Lock.route
                        navController.navigate(destination) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Lock.route) {
                val lockState by lockViewModel.uiState.collectAsState()
                LaunchedEffect(lockState.success) {
                    if (lockState.success) {
                        val info = uiState.unlockInfo
                        if (info != null && !info.celebrationShown) {
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.Lock.route) { inclusive = true }
                            }
                            navController.navigate(Screen.Celebration.route)
                        } else {
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.Lock.route) { inclusive = true }
                            }
                        }
                    }
                }
                LaunchedEffect(lockState.error) {
                    lockState.error?.let {
                        scope.launch { snackbarHostState.showSnackbar(it) }
                        lockViewModel.clearError()
                    }
                }
                LockScreen(
                    pricing = uiState.pricing,
                    onUnlocked = { id, username ->
                        lockViewModel.redeem(id, username)
                    },
                    onBrowseFree = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Lock.route) { inclusive = true }
                        }
                    },
                    onUnlockClick = {
                        navController.navigate(Screen.PaymentInstructions.route)
                    },
                    onRedemptionError = {
                        scope.launch { snackbarHostState.showSnackbar(it) }
                    },
                    redeeming = lockState.redeeming,
                    onRedeemingChange = { }
                )
            }

            composable(Screen.PaymentInstructions.route) {
                PaymentInstructionsScreen(
                    pricing = uiState.pricing,
                    paymentDetails = uiState.paymentDetails,
                    contactDetails = uiState.contactDetails,
                    onSendScreenshotWhatsApp = { referralUsername ->
                        val supportNumber = uiState.contactDetails.whatsappNumber
                        if (supportNumber.isBlank()) {
                            Toast.makeText(
                                context,
                                "Support WhatsApp number isn't set up yet. Please try again later.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val message = "Hi! I have made the payment for MoviesForever Lifetime Access." +
                                    if (referralUsername.isNotBlank()) " Referral Username: $referralUsername" else ""

                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?phone=$supportNumber&text=${Uri.encode(message)}")
                            }
                            context.startActivity(intent)
                        }
                    },
                    onBack = { navController.popBackStackSafe() }
                )
            }

            composable(Screen.Main.route) {
                // Main is the root of the back stack once past Splash/Lock (that entry
                // was removed via popUpTo(inclusive = true)). So a single system/back
                // press here used to exit the app immediately with no confirmation.
                // This adds the standard "press back again to exit" pattern.
                val activity = context.findActivity()
                var backPressedOnce by remember { mutableStateOf(false) }

                BackHandler(enabled = true) {
                    if (backPressedOnce) {
                        activity?.finish()
                    } else {
                        backPressedOnce = true
                        Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                    }
                }

                LaunchedEffect(backPressedOnce) {
                    if (backPressedOnce) {
                        delay(2000)
                        backPressedOnce = false
                    }
                }

                MainScaffoldWithTabs(
                    currentTab = currentTab,
                    onTabSelected = { index -> currentTab = index },
                    uiState = uiState,
                    viewModel = viewModel,
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    scope = scope,
                    lockViewModel = lockViewModel
                )
            }

            composable(Screen.Celebration.route) {
                CelebrationScreen(
                    unlockInfo = uiState.unlockInfo,
                    pricing = uiState.pricing,
                    appShareLink = uiState.appShareLink,
                    onShare = { text -> shareText(context, text) },
                    onStartWatching = {
                        viewModel.markCelebrationShown()
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Celebration.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.MovieDetail.route,
                arguments = listOf(navArgument("movieId") { type = NavType.StringType })
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
                val movie = uiState.movies.find { it.id == movieId }
                if (movie == null) {
                    navController.popBackStack()
                } else {
                    MovieDetailScreen(
                        movie = movie,
                        pricing = uiState.pricing,
                        isUnlocked = uiState.isUnlocked,
                        genres = uiState.genres.associate { it.id to it.name },
                        downloadStatus = uiState.downloadStatuses[movie.id] ?: com.moviesforever.app.data.repository.MovieDownloadStatus.NotDownloaded,
                        onWatchNow = {
                            val isAllowed = movie.isFree || uiState.isUnlocked
                            if (isAllowed) {
                                navController.navigate(Screen.Player.createRoute(movie.id, trailer = false))
                            } else {
                                navController.navigate(Screen.PaymentInstructions.route)
                            }
                        },
                        onWatchTrailer = {
                            if (!movie.trailerUrl.isNullOrBlank()) {
                                navController.navigate(Screen.Player.createRoute(movie.id, trailer = true))
                            }
                        },
                        onDownload = {
                            viewModel.downloadMovie(movie) { result ->
                                when (result) {
                                    DownloadRequestResult.Started -> {
                                        scope.launch { snackbarHostState.showSnackbar("Download started") }
                                    }
                                    DownloadRequestResult.RequiresUnlock -> {
                                        navController.navigate(Screen.PaymentInstructions.route)
                                    }
                                    DownloadRequestResult.RequiresWifi -> {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Connect to WiFi to download, or turn off WiFi-only downloads in Settings")
                                        }
                                    }
                                }
                            }
                        },
                        onUnlockClick = { navController.navigate(Screen.PaymentInstructions.route) },
                        onBack = { navController.popBackStackSafe() }
                    )
                }
            }

            composable(
                route = Screen.Player.route,
                arguments = listOf(
                    navArgument("movieId") { type = NavType.StringType },
                    navArgument("trailer") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
                val isTrailer = backStackEntry.arguments?.getBoolean("trailer") == true
                val movie = uiState.movies.find { it.id == movieId }
                if (movie == null) {
                    navController.popBackStack()
                } else {
                    val url = if (isTrailer) movie.trailerUrl else movie.videoUrl
                    if (url.isNullOrBlank()) {
                        navController.popBackStack()
                    } else {
                        PlayerScreen(
                            videoUrl = url,
                            title = movie.title,
                            onBack = { navController.popBackStackSafe() },
                            cacheKey = if (isTrailer) null else movie.id
                        )
                    }
                }
            }

            composable(
                route = Screen.CategoryBrowse.route,
                arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId")
                CategoryBrowseScreen(
                    initialCategoryId = categoryId,
                    categories = uiState.categories,
                    genres = uiState.genres,
                    movies = uiState.movies,
                    onMovieClick = { movie ->
                        navController.navigate(Screen.MovieDetail.createRoute(movie.id))
                    }
                )
            }

            composable(Screen.Referral.route) {
                ReferralScreen(
                    unlockInfo = uiState.unlockInfo,
                    pricing = uiState.pricing,
                    account = uiState.account,
                    earnings = uiState.earnings,
                    appShareLink = uiState.appShareLink,
                    onShareApk = { text -> shareText(context, text) },
                    onBack = { navController.popBackStackSafe() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    isUnlocked = uiState.isUnlocked,
                    username = uiState.unlockInfo?.username,
                    wifiOnlyDownloads = uiState.wifiOnlyDownloads,
                    contactDetails = uiState.contactDetails,
                    onWifiOnlyDownloadsChange = { viewModel.setWifiOnlyDownloads(it) },
                    onResetUnlock = { viewModel.resetUnlock() },
                    onBack = { navController.popBackStackSafe() }
                )
            }
        }
    }
}

@Composable
private fun MainScaffoldWithTabs(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    uiState: com.moviesforever.app.ui.viewmodel.AppUiState,
    viewModel: AppViewModel,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    lockViewModel: LockViewModel
) {
    val context = LocalContext.current
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = {
            MoviesBottomBar(currentTab = currentTab, onTabSelected = onTabSelected)
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentTab) {
                0 -> HomeScreen(
                    banners = uiState.banners,
                    movies = uiState.movies,
                    pricing = uiState.pricing,
                    isUnlocked = uiState.isUnlocked,
                    onBannerClick = { banner ->
                        if (banner.clickable && banner.linkedMovieId != null) {
                            navController.navigate(Screen.MovieDetail.createRoute(banner.linkedMovieId))
                        }
                    },
                    onMovieClick = { movie ->
                        navController.navigate(Screen.MovieDetail.createRoute(movie.id))
                    },
                    onUnlockClick = { navController.navigate(Screen.PaymentInstructions.route) },
                    onAvatarClick = { onTabSelected(3) }
                )
                1 -> SearchScreen(
                    movies = uiState.movies,
                    categories = uiState.categories,
                    genres = uiState.genres,
                    onMovieClick = { movie ->
                        navController.navigate(Screen.MovieDetail.createRoute(movie.id))
                    }
                )
                2 -> DownloadsScreen(
                    downloadedMovies = uiState.downloadedMovies,
                    // Built purely from the download system (see AppUiState), not
                    // by intersecting with the live movie catalog -- this is what
                    // keeps in-progress/completed downloads visible even if a
                    // movie has been removed/unpublished remotely.
                    downloadingMovies = uiState.downloadingMovies,
                    downloadStatuses = uiState.downloadStatuses,
                    isUnlocked = uiState.isUnlocked,
                    onMovieClick = { movie ->
                        navController.navigate(Screen.MovieDetail.createRoute(movie.id))
                    },
                    onRemoveDownload = { movieId -> viewModel.removeDownload(movieId) },
                    onSettings = { navController.navigate(Screen.Settings.route) }
                )
                3 -> ProfileScreen(
                    unlockInfo = uiState.unlockInfo,
                    pricing = uiState.pricing,
                    account = uiState.account,
                    earnings = uiState.earnings,
                    appShareLink = uiState.appShareLink,
                    onShareApk = { text -> shareText(context, text) },
                    onReferralClick = { navController.navigate(Screen.Referral.route) },
                    onSettings = { navController.navigate(Screen.Settings.route) },
                    onUnlockClick = { navController.navigate(Screen.PaymentInstructions.route) }
                )
            }
        }
    }
}

private fun shareText(context: android.content.Context, text: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share"))
}
