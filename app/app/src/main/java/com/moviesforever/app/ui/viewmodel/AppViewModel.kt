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
import com.moviesforever.app.data.repository.DownloadRepository
import com.moviesforever.app.data.repository.DownloadRequestResult
import com.moviesforever.app.data.repository.GenresRepository
import com.moviesforever.app.data.repository.MovieDownloadStatus
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
    val downloadStatuses: Map<String, MovieDownloadStatus> = emptyMap(),
    val wifiOnlyDownloads: Boolean = true,
    val loading: Boolean = true
) {
    val isUnlocked: Boolean get() = unlockInfo != null
    val downloadedMovies: List<Movie> get() = movies.filter { downloadStatuses[it.id] is MovieDownloadStatus.Completed }
}

@HiltViewModel
class AppViewModel @Inject constructor(
    moviesRepository: MoviesRepository,
    categoriesRepository: CategoriesRepository,
    genresRepository: GenresRepository,
    bannersRepository: BannersRepository,
    pricingRepository: PricingRepository,
    unlockRepository: UnlockRepository,
    private val downloadRepository: DownloadRepository
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
        contentFlow,
        downloadRepository.observeDownloadStatuses(),
        downloadRepository.observeWifiOnly()
    ) { unlockInfo, pricing, content, downloadStatuses, wifiOnly ->
        AppUiState(
            unlockInfo = unlockInfo,
            pricing = pricing,
            movies = content.movies,
            categories = content.categories,
            genres = content.genres,
            banners = content.banners,
            downloadStatuses = downloadStatuses,
            wifiOnlyDownloads = wifiOnly,
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

    /**
     * Tries to download [movie] for offline playback.
     * Callers get a [DownloadRequestResult] so they can show the right feedback:
     * a paywall for locked+paid movies, or a "connect to WiFi" prompt when the
     * WiFi-only setting is on and the device is on mobile data.
     */
    fun downloadMovie(movie: Movie, onResult: (DownloadRequestResult) -> Unit) {
        viewModelScope.launch {
            val result = downloadRepository.requestDownload(movie, uiState.value.isUnlocked)
            onResult(result)
        }
    }

    fun removeDownload(movieId: String) {
        downloadRepository.removeDownload(movieId)
    }

    fun setWifiOnlyDownloads(enabled: Boolean) {
        viewModelScope.launch {
            downloadRepository.setWifiOnly(enabled)
        }
    }
}
