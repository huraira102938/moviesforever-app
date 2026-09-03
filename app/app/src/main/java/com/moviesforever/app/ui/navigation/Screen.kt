package com.moviesforever.app.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Lock : Screen("lock")
    data object Main : Screen("main")
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Downloads : Screen("downloads")
    data object Profile : Screen("profile")
    data object MovieDetail : Screen("movie/{movieId}") {
        fun createRoute(movieId: String) = "movie/$movieId"
    }
    data object CategoryBrowse : Screen("category/{categoryId}") {
        fun createRoute(categoryId: String) = "category/$categoryId"
    }
    data object Player : Screen("player/{movieId}?trailer={trailer}") {
        fun createRoute(movieId: String, trailer: Boolean = false) =
            "player/$movieId?trailer=$trailer"
    }
    data object Referral : Screen("referral")
    data object Celebration : Screen("celebration")
    data object Settings : Screen("settings")
}
