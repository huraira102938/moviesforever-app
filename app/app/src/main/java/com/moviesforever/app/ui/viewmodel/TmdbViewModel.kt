package com.moviesforever.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.data.model.TmdbCategory
import com.moviesforever.app.data.repository.TmdbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Home only shows movies with a rating above this value. */
const val HOME_MIN_RATING = 7.0

/** TEST FEATURE: gives the home screen and details screen access to the TMDB JSON data. */
@HiltViewModel
class TmdbViewModel @Inject constructor(
    private val repository: TmdbRepository
) : ViewModel() {

    /** Every movie of every category (already shuffled once for this session). */
    val shelves: StateFlow<Map<TmdbCategory, List<Movie>>> = repository.shelves

    /**
     * What the HOME screen shows: only movies rated strictly above [HOME_MIN_RATING].
     * Filtering keeps the session's shuffled order, so the shelves stay stable.
     */
    val homeShelves: StateFlow<Map<TmdbCategory, List<Movie>>> = repository.shelves
        .map { all ->
            all.mapValues { (_, list) -> list.filter { (it.imdbRating ?: 0.0) > HOME_MIN_RATING } }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    init {
        viewModelScope.launch { repository.load() }
    }

    fun findMovie(id: String): Movie? =
        shelves.value.values.firstNotNullOfOrNull { list -> list.find { it.id == id } }
}
