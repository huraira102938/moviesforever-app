package com.moviesforever.app.data.model

/**
 * Mirrors a document in the admin panel's dedicated `trending` Firestore
 * collection (Trending page). This is intentionally separate from a movie's
 * `sections` tags -- the admin curates this exact list and order from the
 * "Trending" screen, and the app renders it verbatim as the "Trending Now"
 * shelf on the home screen.
 */
data class TrendingItem(
    val id: String = "",
    val movieId: String = "",
    val order: Int = 0
)
