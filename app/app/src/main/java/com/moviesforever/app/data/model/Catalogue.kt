package com.moviesforever.app.data.model

/**
 * movies.json — the whole catalogue in ONE file on Cloudflare.
 *
 * It is written by the admin panel ("Update JSON" button) from the Firestore `movies`,
 * `categories` and `trending` collections, and read by the app. The app never reads the
 * movies from Firestore.
 *
 * [movies] use exactly the same fields as the Firestore `movies` documents ([Movie]):
 * `category` is the Firestore category id, `genres` are Firestore genre ids.
 */
data class CatalogueFile(
    val version: Int = 1,
    /** ISO time of the last admin update. The admin panel uses it to find new/changed movies. */
    val generatedAt: String = "",
    val categories: List<CatalogueCategory> = emptyList(),
    /** Movie ids in the order the admin panel's Trending page shows them. */
    val trending: List<String> = emptyList(),
    val movies: List<Movie> = emptyList()
)

data class CatalogueCategory(
    val id: String = "",
    val name: String = "",
    val order: Int = 0
)

/** One Home-screen shelf: all (non-paused) movies of one category. */
data class Shelf(
    val categoryId: String,
    val title: String,
    val movies: List<Movie>
)

/** Everything the UI needs from movies.json, already grouped. */
data class CatalogueState(
    val shelves: List<Shelf> = emptyList(),
    val trending: List<Movie> = emptyList(),
    val all: List<Movie> = emptyList(),
    /** True once movies.json (saved copy or fresh download) has been read at least once. */
    val loaded: Boolean = false
)