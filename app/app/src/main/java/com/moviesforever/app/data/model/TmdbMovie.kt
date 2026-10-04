package com.moviesforever.app.data.model

/**
 * TEST FEATURE: one movie record from the TMDB JSON files (uploaded to Cloudflare).
 * Fields match the JSON exactly. `poster` is only the TMDB path (e.g. "/abc.jpg").
 */
data class TmdbMovie(
    val id: Int = 0,
    val title: String = "",
    val year: Int = 0,
    val poster: String = "",
    val rating: Double = 0.0,
    val description: String = "",
    val trailerUrl: String = "",
    val watchUrl: String = ""   // reserved for the legal "where to watch" links (not used yet)
)

/** Category = JSON file. The category is NOT stored inside the JSON items. */
enum class TmdbCategory(val title: String, val fileName: String) {
    BOLLYWOOD("Bollywood", "bollywood.json"),
    HOLLYWOOD("Hollywood", "hollywood.json"),
    SOUTH("South & Tamil", "south.json"),
    PUNJABI("Punjabi", "punjabi.json"),
    OTHERS("Korean", "korean-chinese-others.json"),
    ANIMATION("Animation", "animation.json"),
    ANIME("Anime", "anime.json"),
    TRENDING("Trending", "trending.json")
}

/** Ids of TMDB movies start with this prefix so they never clash with existing movie ids. */
const val TMDB_ID_PREFIX = "tmdb_"

private const val TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w780"

/** Maps a TMDB record onto the app's existing Movie model so the existing UI can show it. */
fun TmdbMovie.toMovie(category: TmdbCategory): Movie = Movie(
    id = "$TMDB_ID_PREFIX$id",
    title = title,
    category = category.title,
    year = year.takeIf { it > 0 },
    description = description,
    imdbRating = rating.takeIf { it > 0.0 },
    trailerUrl = trailerUrl.takeIf { it.isNotBlank() },
    thumbnailUrl = if (poster.isBlank()) "" else "$TMDB_IMAGE_BASE$poster",
    isFree = false
)
