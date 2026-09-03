package com.moviesforever.app.data.repository

import com.moviesforever.app.data.model.Genre

interface GenresRepository {
    fun observeGenres(): kotlinx.coroutines.flow.Flow<List<Genre>>
}
