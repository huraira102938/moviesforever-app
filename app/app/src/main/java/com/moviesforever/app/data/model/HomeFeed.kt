package com.moviesforever.app.data.model

import java.util.Calendar

/** One Home shelf. [id] is what "Show All" opens. */
data class FeedRow(
    val id: String,
    val title: String,
    val movies: List<Movie>
)

data class HomeFeed(
    val banners: List<Banner> = emptyList(),
    val free: List<Movie> = emptyList(),
    val trending: List<Movie> = emptyList(),
    /** Full shelves, for Show All. */
    val rows: List<FeedRow> = emptyList(),
    /** The same shelves as shown on Home, where no movie appears twice. */
    val homeRows: List<FeedRow> = emptyList(),
    val genreRows: List<FeedRow> = emptyList()
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

fun buildHomeFeed(catalogue: CatalogueState, genres: List<Genre>): HomeFeed {
    val all = catalogue.all
    val lastYear = Calendar.getInstance().get(Calendar.YEAR) - 1
    val newReleases = all.filter { (it.year ?: 0) >= lastYear }.sortedByDescending { it.year ?: 0 }
    val topRated = all.filter { (it.imdbRating ?: 0.0) >= TOP_RATED_FROM }
        .sortedByDescending { it.imdbRating ?: 0.0 }
    val categoryRows = catalogue.shelves.map { FeedRow("cat_${it.categoryId}", it.title, it.movies) }
    val genreRows = genres
        .map { genre -> FeedRow("genre_${genre.id}", genre.name, all.filter { genre.id in it.genres }) }
        .filter { it.movies.size >= MIN_MOVIES_FOR_GENRE_ROW }
        .sortedByDescending { it.movies.size }

    val free = all.filter { it.isFree }
    val trending = catalogue.trending.take(TRENDING_ON_HOME)
    val rows = buildList {
        if (newReleases.isNotEmpty()) add(FeedRow("new", "New Releases", newReleases))
        addAll(categoryRows)
        if (topRated.isNotEmpty()) add(FeedRow("top", "Top Rated", topRated))
        addAll(genreRows)
    }

    return HomeFeed(
        banners = catalogue.banners,
        free = free,
        trending = trending,
        rows = rows,
        homeRows = uniqueHomeRows(rows, free + trending),
        genreRows = genreRows
    )
}
