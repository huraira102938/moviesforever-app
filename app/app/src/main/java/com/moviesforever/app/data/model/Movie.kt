package com.moviesforever.app.data.model

import com.google.firebase.firestore.PropertyName

data class Movie(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val genres: List<String> = emptyList(),
    val year: Int? = null,
    val description: String = "",
    val imdbRating: Double? = null,
    val badge: String? = null,
    val trailerKey: String? = null,
    val trailerUrl: String? = null,
    val videoKey: String = "",
    val videoUrl: String = "",
    val thumbnailKey: String = "",
    val thumbnailUrl: String = "",
    @get:PropertyName("isFree")
    @set:PropertyName("isFree")
    var isFree: Boolean = false,
    val language: String = "",
    val availableDubs: List<String> = emptyList(),
    val sections: List<String> = emptyList(),
    val paused: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)
