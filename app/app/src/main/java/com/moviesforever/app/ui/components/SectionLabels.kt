package com.moviesforever.app.ui.components

object SectionLabels {
    const val RECENTLY_ADDED = "recently-added"
    const val HOT = "hot"
    const val ALL_TIME_HIT = "all-time-hit"
    const val HIT_OF_THIS_YEAR = "hit-of-this-year"

    private val labels = mapOf(
        RECENTLY_ADDED to "Recently Added",
        HOT to "Hot",
        ALL_TIME_HIT to "All-time Hit",
        HIT_OF_THIS_YEAR to "Hit of This Year"
    )

    // Fixed display order for curated shelves
    val orderedSections = listOf(RECENTLY_ADDED, HOT, ALL_TIME_HIT, HIT_OF_THIS_YEAR)

    fun label(section: String): String = labels[section] ?: section
}
