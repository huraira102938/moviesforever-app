package com.moviesforever.app.data.repository

import com.moviesforever.app.data.model.Movie

interface MoviesRepository {
    fun observeMovies(): kotlinx.coroutines.flow.Flow<List<Movie>>
    suspend fun getMovieById(id: String): Movie?
}
