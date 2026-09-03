package com.moviesforever.app.data.model

data class Banner(
    val id: String = "",
    val imageKey: String = "",
    val imageUrl: String = "",
    val clickable: Boolean = false,
    val linkedMovieId: String? = null,
    val order: Int = 0
)
