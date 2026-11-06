package com.moviesforever.app.data.model

import java.util.Calendar

/** What the viewer picked on first launch. Decides which movies are offered without being asked for. */
enum class ViewerPreference(val title: String, val subtitle: String) {
    ALL("Hindi & English", "Bollywood, South Indian, Punjabi and Hollywood"),
    ENGLISH("English", "Hollywood and international movies");

    val watchesHindi: Boolean get() = this == ALL
}

/** One Home shelf. [id] is what "Show All" opens. */
data class FeedRow(
    val id: String,
    val title: String,
    val movies: List<Movie>
)

/** Home and All Movies for one viewer. Search and the category filter still use the whole catalogue. */
data class HomeFeed(
    val banners: List<Banner> = emptyList(),
    val free: List<Movie> = emptyList(),
    val trending: List<Movie> = emptyList(),
    /** Full shelves, for Show All. */
    val rows: List<FeedRow> = emptyList(),
    /** The same shelves as shown on Home, where no movie appears twice. */
    val homeRows: List<FeedRow> = emptyList(),
    val genreRows: List<FeedRow> = emptyList(),
    val pool: List<Movie> = emptyList()
)

private const val TOP_RATED_FROM = 7.5
private const val MIN_MOVIES_FOR_GENRE_ROW = 6
private const val MOVIES_PER_HOME_ROW = 12
private const val MIN_MOVIES_FOR_HOME_ROW = 4
private const val TRENDING_ON_HOME = 10

/** Earlier shelves keep a movie, later ones skip it. A shelf left too short is dropped from Home. */
private fun uniqueHomeRows(rows: List<FeedRow>, alreadyShown: List<Movie>): List<FeedRow> {
    val seen = alreadyShown.mapTo(HashSet()) { it.id }
    return rows.mapNotNull { row ->
        val movies = row.movies.filter { it.id !in seen }.take(MOVIES_PER_HOME_ROW)
        if (movies.size < MIN_MOVIES_FOR_HOME_ROW) return@mapNotNull null
        movies.mapTo(seen) { it.id }
        row.copy(movies = movies)
    }
}

private fun Movie.isInternational(): Boolean =
    language.equals("English", ignoreCase = true) ||
        availableDubs.any { it.equals("English", ignoreCase = true) }

/**
 * Shelves are built from what a movie is (new, top rated, genre), not where it was made, so Home
 * stays full when Bollywood is left out. Only Hindi viewers also get the category shelves.
 */
fun buildHomeFeed(catalogue: CatalogueState, preference: ViewerPreference): HomeFeed {
    val pool = if (preference.watchesHindi) catalogue.all else catalogue.all.filter { it.isInternational() }
    val poolIds = pool.mapTo(HashSet()) { it.id }

    val lastYear = Calendar.getInstance().get(Calendar.YEAR) - 1
    val newReleases = pool.filter { (it.year ?: 0) >= lastYear }.sortedByDescending { it.year ?: 0 }
    val topRated = pool.filter { (it.imdbRating ?: 0.0) >= TOP_RATED_FROM }
        .sortedByDescending { it.imdbRating ?: 0.0 }

    val categoryRows = if (preference.watchesHindi) {
        catalogue.shelves.map { FeedRow("cat_${it.categoryId}", it.title, it.movies) }
    } else {
        emptyList()
    }
    val genreRows = catalogue.genres
        .map { genre -> FeedRow("genre_${genre.id}", genre.name, pool.filter { genre.id in it.genres }) }
        .filter { it.movies.size >= MIN_MOVIES_FOR_GENRE_ROW }
        .sortedByDescending { it.movies.size }

    val free = pool.filter { it.isFree }
    val trending = catalogue.trending.filter { it.id in poolIds }.take(TRENDING_ON_HOME)
    val rows = buildList {
        if (newReleases.isNotEmpty()) add(FeedRow("new", "New Releases", newReleases))
        addAll(categoryRows)
        if (topRated.isNotEmpty()) add(FeedRow("top", "Top Rated", topRated))
        addAll(genreRows)
    }

    return HomeFeed(
        // A banner that opens a movie this viewer would not be shown goes too.
        banners = catalogue.banners.filter { it.linkedMovieId.isNullOrBlank() || it.linkedMovieId in poolIds },
        free = free,
        trending = trending,
        rows = rows,
        homeRows = uniqueHomeRows(rows, free + trending),
        genreRows = genreRows,
        pool = pool
    )
}
