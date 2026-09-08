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
import kotlinx.coroutines.flow.map
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

/**
 * Result of the LOCAL unlock check only (DataStore on disk). This is intentionally
 * kept separate from [AppUiState], which also waits on network calls (movies,
 * categories, pricing, etc via Firestore). Gating navigation on the combined
 * state caused an intermittent bug: on a cold start where Firestore's network
 * calls were slow to resolve, [AppUiState] would still be sitting at its default
 * (unlockInfo = null) when the splash screen made its routing decision, so
 * premium/lifetime users would incorrectly get sent to the Lock screen even
 * though their unlock info was already saved locally. Splash screen routing
 * must only depend on this fast, local-only signal.
 */
sealed class UnlockCheckState {
    data object Loading : UnlockCheckState()
    data object Locked : UnlockCheckState()
    data class Unlocked(val info: UnlockInfo) : UnlockCheckState()
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

    /**
     * Fast, network-independent unlock signal for routing decisions (splash screen).
     * Backed only by local DataStore, so it resolves quickly and reliably regardless
     * of Firestore/network latency. See [UnlockCheckState] for why this exists
     * separately from [uiState].
     */
    val unlockCheckState: StateFlow<UnlockCheckState> = unlockRepository.observeUnlockInfo()
        .map { info -> if (info != null) UnlockCheckState.Unlocked(info) else UnlockCheckState.Locked }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UnlockCheckState.Loading
        )

    private data class ContentData(
        val movies: List<Movie>,
        val categories: List<Category>,
        val genres: List<Genre>,
        val banners: List<Banner>
    )

    // Each of these is turned into its own hot StateFlow (via stateIn) BEFORE the
    // final combine below. This matters: combine() only emits once every source
    // has emitted at least once. If the network-backed sources (movies,
    // categories, genres, banners, pricing) were combined directly as cold flows,
    // the WHOLE uiState -- including unlockInfo, which is actually fast and local
    // -- would sit at its default (unlockInfo = null) until those network calls
    // finished. That caused a second instance of the same class of bug as the
    // splash screen: HomeScreen would flash its "unlock lifetime pass" banner for
    // ~1 second for premium users, right after the (correct) splash decision,
    // because uiState.isUnlocked hadn't caught up yet.
    //
    // By pre-warming each source into its own StateFlow with an immediate cached
    // initial value, the outer combine() can emit right away: content starts as
    // empty lists (normal loading state, not a false "you're not premium" claim)
    // while unlockCheckState -- which resolves in milliseconds -- is reflected
    // correctly from the first emission.
    private val contentDataState: StateFlow<ContentData> = combine(
        moviesRepository.observeMovies(),
        categoriesRepository.observeCategories(),
        genresRepository.observeGenres(),
        bannersRepository.observeBanners()
    ) { movies, categories, genres, banners ->
        ContentData(movies, categories, genres, banners)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ContentData(emptyList(), emptyList(), emptyList(), emptyList())
    )

    private val pricingState: StateFlow<PricingSettings> = pricingRepository.observePricing()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PricingSettings()
        )

    private val downloadStatusesState: StateFlow<Map<String, MovieDownloadStatus>> =
        downloadRepository.observeDownloadStatuses().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    private val wifiOnlyState: StateFlow<Boolean> = downloadRepository.observeWifiOnly()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val uiState: StateFlow<AppUiState> = combine(
        unlockCheckState,
        pricingState,
        contentDataState,
        downloadStatusesState,
        wifiOnlyState
    ) { unlockCheck, pricing, content, downloadStatuses, wifiOnly ->
        AppUiState(
            unlockInfo = (unlockCheck as? UnlockCheckState.Unlocked)?.info,
            pricing = pricing,
            movies = content.movies,
            categories = content.categories,
            genres = content.genres,
            banners = content.banners,
            downloadStatuses = downloadStatuses,
            wifiOnlyDownloads = wifiOnly,
            loading = unlockCheck is UnlockCheckState.Loading
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