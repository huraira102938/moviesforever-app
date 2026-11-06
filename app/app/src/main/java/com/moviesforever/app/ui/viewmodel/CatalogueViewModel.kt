package com.moviesforever.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviesforever.app.data.model.CatalogueState
import com.moviesforever.app.data.model.HomeFeed
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.data.model.ViewerPreference
import com.moviesforever.app.data.model.buildHomeFeed
import com.moviesforever.app.data.repository.CatalogueRepository
import com.moviesforever.app.data.repository.ViewerPreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Gives Home, Search, Category and Detail screens access to the movies.json catalogue. */
@HiltViewModel
class CatalogueViewModel @Inject constructor(
    private val repository: CatalogueRepository,
    private val viewerPreferences: ViewerPreferenceRepository
) : ViewModel() {

    val state: StateFlow<CatalogueState> = repository.state

    val preference: StateFlow<ViewerPreference?> = viewerPreferences.preference

    /** Starts filled in, so coming back to a screen never flashes an empty list. */
    val feed: StateFlow<HomeFeed> = combine(repository.state, viewerPreferences.preference) { catalogue, preference ->
        buildHomeFeed(catalogue, preference ?: ViewerPreference.ALL)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        buildHomeFeed(repository.state.value, viewerPreferences.preference.value ?: ViewerPreference.ALL)
    )

    fun setPreference(preference: ViewerPreference) {
        viewModelScope.launch { viewerPreferences.save(preference) }
    }

    init {
        viewModelScope.launch { repository.load() }
    }

    fun findMovie(id: String): Movie? = state.value.all.find { it.id == id }
}
