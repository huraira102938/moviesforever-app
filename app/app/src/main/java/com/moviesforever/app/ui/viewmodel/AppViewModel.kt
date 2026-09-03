package com.moviesforever.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviesforever.app.data.model.Banner
import com.moviesforever.app.data.model.Category
import com.moviesforever.app.data.model.Genre
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.data.model.PricingSettings
import com.moviesforever.app.data.model.UnlockInfo
import com.moviesforever.app.data.repository.BannersRepository
import com.moviesforever.app.data.repository.CategoriesRepository
import com.moviesforever.app.data.repository.GenresRepository
import com.moviesforever.app.data.repository.MoviesRepository
import com.moviesforever.app.data.repository.PricingRepository
import com.moviesforever.app.data.repository.UnlockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppUiState(
    val unlockInfo: UnlockInfo? = null,
    val pricing: PricingSettings = PricingSettings(),
    val movies: List<Movie> = emptyList(),
    val categories: List<Category> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val banners: List<Banner> = emptyList(),
    val loading: Boolean = true
) {
    val isUnlocked: Boolean get() = unlockInfo != null
}

@HiltViewModel
class AppViewModel @Inject constructor(
    moviesRepository: MoviesRepository,
    categoriesRepository: CategoriesRepository,
    genresRepository: GenresRepository,
    bannersRepository: BannersRepository,
    pricingRepository: PricingRepository,
    unlockRepository: UnlockRepository
) : ViewModel() {

    private val unlockRepositoryRef = unlockRepository

    private data class ContentData(
        val movies: List<Movie>,
        val categories: List<Category>,
        val genres: List<Genre>,
        val banners: List<Banner>
    )

    private val contentFlow = combine(
        moviesRepository.observeMovies(),
        categoriesRepository.observeCategories(),
        genresRepository.observeGenres(),
        bannersRepository.observeBanners()
    ) { movies, categories, genres, banners ->
        ContentData(movies, categories, genres, banners)
    }

    val uiState: StateFlow<AppUiState> = combine(
        unlockRepository.observeUnlockInfo(),
        pricingRepository.observePricing(),
        contentFlow
    ) { unlockInfo, pricing, content ->
        AppUiState(
            unlockInfo = unlockInfo,
            pricing = pricing,
            movies = content.movies,
            categories = content.categories,
            genres = content.genres,
            banners = content.banners,
            loading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppUiState()
    )

    fun markCelebrationShown() {
        viewModelScope.launch {
            unlockRepositoryRef.markCelebrationShown()
        }
    }

    fun resetUnlock() {
        viewModelScope.launch {
            unlockRepositoryRef.resetUnlock()
        }
    }
}
