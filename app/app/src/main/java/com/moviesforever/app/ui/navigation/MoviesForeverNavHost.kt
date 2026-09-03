package com.moviesforever.app.ui.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moviesforever.app.ui.components.BottomTabs
import com.moviesforever.app.ui.components.MoviesBottomBar
import com.moviesforever.app.ui.screen.category.CategoryBrowseScreen
import com.moviesforever.app.ui.screen.celebration.CelebrationScreen
import com.moviesforever.app.ui.screen.detail.MovieDetailScreen
import com.moviesforever.app.ui.screen.downloads.DownloadsScreen
import com.moviesforever.app.ui.screen.home.HomeScreen
import com.moviesforever.app.ui.screen.lock.LockScreen
import com.moviesforever.app.ui.screen.player.PlayerScreen
import com.moviesforever.app.ui.screen.profile.ProfileScreen
import com.moviesforever.app.ui.screen.referral.ReferralScreen
import com.moviesforever.app.ui.screen.search.SearchScreen
import com.moviesforever.app.ui.screen.settings.SettingsScreen
import com.moviesforever.app.ui.screen.splash.SplashScreen
import com.moviesforever.app.ui.viewmodel.AppViewModel
import com.moviesforever.app.ui.viewmodel.LockViewModel
import com.moviesforever.app.ui.theme.Black
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
            startDestination = "splash",
            modifier = Modifier.padding(outerPadding)
        ) {
        composable("splash") {
            SplashScreen(
                onFinished = {
                    // Navigate after showing splash
                    if (uiState.isUnlocked) {
                        navController.navigate("nb_main") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("lock") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("lock") {
            val lockState by lockViewModel.uiState.collectAsState()
            LaunchedEffect(lockState.success) {
                if (lockState.success) {
                    // unlock saved; go to main, and then to celebration if not shown yet
                    val info = uiState.unlockInfo
                    if (info != null && !info.celebrationShown) {
                        navController.navigate("nb_main") {
                            popUpTo("lock") { inclusive = true }
                        }
                        navController.navigate("celebration")
                    } else {
                        navController.navigate("nb_main") {
                            popUpTo("lock") { inclusive = true }
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
                    navController.navigate("nb_main") {
                        popUpTo("lock") { inclusive = true }
                    }
                },
                onRedemptionError = {
                    scope.launch { snackbarHostState.showSnackbar(it) }
                },
                redeeming = lockState.redeeming,
                onRedeemingChange = { }
            )
        }

        composable("nb_main") {
            MainScaffoldWithTabs(
                currentTab = currentTab,
                onTabSelected = { index ->
                    currentTab = index
                },
                uiState = uiState,
                viewModel = viewModel,
                navController = navController,
                snackbarHostState = snackbarHostState,
                scope = scope,
                lockViewModel = lockViewModel
            )
        }

        composable("celebration") {
            CelebrationScreen(
                unlockInfo = uiState.unlockInfo,
                pricing = uiState.pricing,
                onShare = { text ->
                    shareText(context, text)
                },
                onStartWatching = {
                    viewModel.markCelebrationShown()
                    navController.navigate("nb_main") {
                        popUpTo("celebration") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "movie/{movieId}",
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
                    onWatchNow = {
                        val isAllowed = movie.isFree || uiState.isUnlocked
                        if (isAllowed) {
                            navController.navigate(Screen.Player.createRoute(movie.id, trailer = false))
                        } else {
                            navController.navigate("lock")
                        }
                    },
                    onWatchTrailer = {
                        if (!movie.trailerUrl.isNullOrBlank()) {
                            navController.navigate(Screen.Player.createRoute(movie.id, trailer = true))
                        }
                    },
                    onDownload = { },
                    onUnlockClick = { navController.navigate("lock") },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = "player/{movieId}?trailer={trailer}",
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
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        composable(
            route = "category/{categoryId}",
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

        composable("referral") {
            ReferralScreen(
                unlockInfo = uiState.unlockInfo,
                pricing = uiState.pricing,
                onShare = { text -> shareText(context, text) },
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                isUnlocked = uiState.isUnlocked,
                username = uiState.unlockInfo?.username,
                onResetUnlock = { viewModel.resetUnlock() },
                onBack = { navController.popBackStack() }
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
                onUnlockClick = { navController.navigate("lock") },
                onAvatarClick = { navController.navigate("profile") }
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
                downloadedMovies = emptyList(),
                isUnlocked = uiState.isUnlocked,
                onMovieClick = { movie ->
                    navController.navigate(Screen.MovieDetail.createRoute(movie.id))
                },
                onSettings = { navController.navigate("settings") }
            )
            3 -> ProfileScreen(
                unlockInfo = uiState.unlockInfo,
                pricing = uiState.pricing,
                onShareReferral = { text ->
                    shareText(context, text)
                },
                onReferralClick = { navController.navigate("referral") },
                onSettings = { navController.navigate("settings") },
                onUnlockClick = { navController.navigate("lock") }
            )
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