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
import com.moviesforever.app.data.model.CategoryRules
import com.moviesforever.app.ui.components.UrduErrorDialog
import androidx.navigation.navArgument
import com.moviesforever.app.data.repository.DownloadRequestResult
import com.moviesforever.app.ui.components.MoviesBottomBar
import com.moviesforever.app.ui.screen.category.CategoryBrowseScreen
import com.moviesforever.app.ui.screen.celebration.CelebrationScreen
import com.moviesforever.app.ui.screen.detail.MovieDetailScreen
import com.moviesforever.app.ui.screen.downloads.DownloadsScreen
import com.moviesforever.app.ui.screen.home.HomeScreen
import com.moviesforever.app.ui.viewmodel.CatalogueViewModel
import com.moviesforever.app.data.model.Category
import com.moviesforever.app.ui.screen.notifications.NotificationsScreen
import com.moviesforever.app.ui.screen.paused.PausedScreen
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

    var currentTab by remember { mutableIntStateOf(0) }

    val account = uiState.account
    LaunchedEffect(account?.paused) {
        val isPaused = account?.paused == true
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (isPaused && currentRoute != Screen.Paused.route) {
            navController.navigate(Screen.Paused.route) {
                popUpTo(0) { inclusive = true }
            }
        } else if (!isPaused && currentRoute == Screen.Paused.route) {
            navController.navigate(Screen.Main.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

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
                        val isInstalled = viewModel.installCheckState.value is InstallCheckState.Installed
                        if (!isInstalled) {
                            navController.navigate(Screen.Welcome.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        } else {
                            // Lock screen removed: Go straight to Main
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
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
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.PaymentInstructions.route) {
                val lockState by lockViewModel.uiState.collectAsState()

                LaunchedEffect(lockState.success) {
                    if (lockState.success) {
                        val info = uiState.unlockInfo
                        navController.popBackStack()
                        if (info != null && !info.celebrationShown) {
                            navController.navigate(Screen.Celebration.route)
                        }
                    }
                }

                // Wrong code / username etc.: shown as a dialog in Urdu, not a quick snackbar.
                var redeemErrorMessage by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(lockState.error) {
                    lockState.error?.let {
                        redeemErrorMessage = it
                        lockViewModel.clearError()
                    }
                }
                redeemErrorMessage?.let { message ->
                    UrduErrorDialog(
                        message = message,
                        title = "کوڈ قبول نہیں ہوا",
                        onDismiss = { redeemErrorMessage = null }
                    )
                }

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
                    onRedeemCode = { id, username ->
                        if (id.isBlank() || username.isBlank()) {
                            redeemErrorMessage = "براہ کرم کوڈ اور یوزرنیم دونوں لکھیں۔"
                        } else {
                            lockViewModel.redeem(id, username)
                        }
                    },
                    redeeming = lockState.redeeming,
                    onBack = { navController.popBackStackSafe() }
                )
            }

            composable(Screen.Main.route) {
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

            composable(
                route = "category_browse/{categoryId}",
                arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                val catalogueViewModel: CatalogueViewModel = hiltViewModel()
                val catalogue by catalogueViewModel.state.collectAsState()
                val shelf = catalogue.shelves.find { it.categoryId == categoryId }
                // Some categories also pull in others (Bollywood also shows South), mixed together
                // in a new scattered order each time this screen opens.
                val browseMovies = remember(catalogue.shelves, categoryId) {
                    val ids = listOf(categoryId) + CategoryRules.SHOW_ALL_ALSO_INCLUDES[categoryId].orEmpty()
                    catalogue.shelves
                        .filter { it.categoryId in ids }
                        .flatMap { it.movies }
                        .distinctBy { it.id }
                        .shuffled()
                }

                CategoryBrowseScreen(
                    categoryTitle = shelf?.title.orEmpty(),
                    movies = browseMovies,
                    onMovieClick = { movie ->
                        navController.navigate(Screen.MovieDetail.createRoute(movie.id))
                    },
                    onBack = { navController.popBackStackSafe() }
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
                val catalogueViewModel: CatalogueViewModel = hiltViewModel()
                val catalogue by catalogueViewModel.state.collectAsState()
                // A downloaded movie must still open even if it is no longer in the catalogue.
                val movie = catalogue.all.find { it.id == movieId } ?: uiState.downloadedMovieInfo[movieId]
                if (movie == null) {
                    // Don't leave the screen while movies.json is still loading.
                    if (catalogue.loaded) navController.popBackStack()
                } else {
                    MovieDetailScreen(
                        movie = movie,
                        pricing = uiState.pricing,
                        isUnlocked = uiState.isUnlocked,
                        genres = uiState.genres.associate { it.id to it.name },
                        downloadStatus = uiState.downloadStatuses[movie.id] ?: com.moviesforever.app.data.repository.MovieDownloadStatus.NotDownloaded,
                        onWatchNow = onWatchNow@{
                            if (uiState.account?.paused == true) {
                                navController.navigate(Screen.Paused.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                val isAllowed = movie.isFree || uiState.isUnlocked
                                if (isAllowed) {
                                    navController.navigate(Screen.Player.createRoute(movie.id, trailer = false))
                                } else {
                                    navController.navigate(Screen.PaymentInstructions.route)
                                }
                            }
                        },
                        onWatchTrailer = {
                            if (uiState.account?.paused == true) {
                                navController.navigate(Screen.Paused.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else if (!movie.trailerUrl.isNullOrBlank()) {
                                navController.navigate(Screen.Player.createRoute(movie.id, trailer = true))
                            }
                        },
                        onDownload = {
                            if (uiState.account?.paused == true) {
                                navController.navigate(Screen.Paused.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
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

                val catalogueViewModel: CatalogueViewModel = hiltViewModel()
                val catalogue by catalogueViewModel.state.collectAsState()
                // A downloaded movie must still play even if it is no longer in the catalogue.
                val movie = catalogue.all.find { it.id == movieId } ?: uiState.downloadedMovieInfo[movieId]
                if (movie == null) {
                    if (catalogue.loaded) navController.popBackStack()
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

            composable(Screen.Paused.route) {
                PausedScreen(note = uiState.account?.pauseUserNote.orEmpty())
            }

            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    notifications = uiState.myNotifications,
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
    val catalogueViewModel: CatalogueViewModel = hiltViewModel()
    val catalogue by catalogueViewModel.state.collectAsState()
    val searchCategories = remember(catalogue.shelves) {
        catalogue.shelves.map { Category(id = it.categoryId, name = it.title) }
    }

    // Tabs: 0 -> Home, 1 -> Search, 2 -> Downloads, 3 -> Profile
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
                    pricing = uiState.pricing,
                    isUnlocked = uiState.isUnlocked,
                    onBannerClick = { banner ->
                        handleBannerClick(context, banner) { movieId ->
                            navController.navigate(Screen.MovieDetail.createRoute(movieId))
                        }
                    },
                    onMovieClick = { movie ->
                        navController.navigate(Screen.MovieDetail.createRoute(movie.id))
                    },
                    onShowAllClick = { categoryId ->
                        navController.navigate("category_browse/$categoryId")
                    },
                    onUnlockClick = { navController.navigate(Screen.PaymentInstructions.route) },
                    onAvatarClick = { onTabSelected(3) }
                )
                1 -> SearchScreen(
                    movies = catalogue.all,
                    categories = searchCategories,
                    genres = uiState.genres,
                    onMovieClick = { movie ->
                        navController.navigate(Screen.MovieDetail.createRoute(movie.id))
                    }
                )
                2 -> DownloadsScreen(
                    downloadedMovies = uiState.downloadedMovies,
                    // Built purely from the download system (see AppUiState), not by intersecting
                    // with the catalogue -- in-progress/completed downloads stay visible even if a
                    // movie was removed from movies.json.
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
                    onNotifications = { navController.navigate(Screen.Notifications.route) },
                    notificationCount = uiState.myNotifications.size,
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
