package com.moviesforever.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.data.model.TmdbCategory
import com.moviesforever.app.data.repository.TmdbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** TEST FEATURE: gives the home screen and details screen access to the TMDB JSON data. */
@HiltViewModel
class TmdbViewModel @Inject constructor(
    private val repository: TmdbRepository
) : ViewModel() {

    val shelves: StateFlow<Map<TmdbCategory, List<Movie>>> = repository.shelves

    init {
        viewModelScope.launch { repository.load() }
    }

    fun findMovie(id: String): Movie? =
        shelves.value.values.firstNotNullOfOrNull { list -> list.find { it.id == id } }
}
