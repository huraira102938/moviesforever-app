package com.moviesforever.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviesforever.app.data.model.CatalogueState
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.data.repository.CatalogueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Gives Home, Search, Category and Detail screens access to the movies.json catalogue. */
@HiltViewModel
class CatalogueViewModel @Inject constructor(
    private val repository: CatalogueRepository
) : ViewModel() {

    val state: StateFlow<CatalogueState> = repository.state

    init {
        viewModelScope.launch { repository.load() }
    }

    fun findMovie(id: String): Movie? = state.value.all.find { it.id == id }
}
