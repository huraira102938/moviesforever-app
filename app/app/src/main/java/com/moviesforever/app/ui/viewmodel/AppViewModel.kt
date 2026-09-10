package com.moviesforever.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviesforever.app.data.model.AppShareLink
import com.moviesforever.app.data.model.Banner
import com.moviesforever.app.data.model.Category
import com.moviesforever.app.data.model.Genre
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.data.model.PricingSettings
import com.moviesforever.app.data.model.ReferralEarnings
import com.moviesforever.app.data.model.UnlockInfo
import com.moviesforever.app.data.model.UserAccount
import com.moviesforever.app.data.repository.AccountRepository
import com.moviesforever.app.data.repository.AppShareRepository
import com.moviesforever.app.data.repository.BannersRepository
import com.moviesforever.app.data.repository.CategoriesRepository
import com.moviesforever.app.data.repository.DownloadRepository
import com.moviesforever.app.data.repository.DownloadRequestResult
import com.moviesforever.app.data.repository.GenresRepository
import com.moviesforever.app.data.repository.MovieDownloadStatus
import com.moviesforever.app.data.repository.MoviesRepository
import com.moviesforever.app.data.repository.PricingRepository
import com.moviesforever.app.data.repository.ReferralEarningsRepository
import com.moviesforever.app.data.repository.UnlockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MF_Download"

data class AppUiState(
    val unlockInfo: UnlockInfo? = null,
    val pricing: PricingSettings = PricingSettings(),
    val movies: List<Movie> = emptyList(),
    val categories: List<Category> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val banners: List<Banner> = emptyList(),
    val downloadStatuses: Map<String, MovieDownloadStatus> = emptyMap(),
    val downloadedMovieInfo: Map<String, Movie> = emptyMap(),
    val wifiOnlyDownloads: Boolean = true,
    val loading: Boolean = true,
    val account: UserAccount? = null,
    val earnings: ReferralEarnings = ReferralEarnings(),
    val appShareLink: AppShareLink? = null
) {
    val isUnlocked: Boolean get() = unlockInfo != null

    val downloadedMovies: List<Movie> get() = downloadStatuses.entries
        .filter { it.value is MovieDownloadStatus.Completed }
        .mapNotNull { downloadedMovieInfo[it.key] }

    val downloadingMovies: List<Movie> get() = downloadStatuses.entries
        .filter { it.value is MovieDownloadStatus.Downloading }
        .mapNotNull { downloadedMovieInfo[it.key] }
}

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
    accountRepository: AccountRepository,
    referralEarningsRepository: ReferralEarningsRepository,
    appShareRepository: AppShareRepository,
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val unlockRepositoryRef = unlockRepository

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

    private val downloadedMovieInfoState: StateFlow<Map<String, Movie>> =
        downloadRepository.observeDownloadedMovieInfo().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    private val downloadsCombinedState: StateFlow<Pair<Map<String, MovieDownloadStatus>, Map<String, Movie>>> =
        combine(downloadStatusesState, downloadedMovieInfoState) { statuses, info ->
            val completedIds = statuses.filterValues { it is MovieDownloadStatus.Completed }.keys
            val missingFromInfo = completedIds - info.keys
            if (missingFromInfo.isNotEmpty()) {
                Log.w(TAG, "[ViewModel] MISMATCH: completed in downloadStatuses but MISSING from downloadedMovieInfo: $missingFromInfo")
            }
            Log.d(TAG, "[ViewModel] combine: statuses=$statuses infoKeys=${info.keys}")
            statuses to info
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap<String, MovieDownloadStatus>() to emptyMap()
            )

    private val wifiOnlyState: StateFlow<Boolean> = downloadRepository.observeWifiOnly()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    private val accountState: StateFlow<UserAccount?> = unlockCheckState
        .flatMapLatest { check ->
            val info = (check as? UnlockCheckState.Unlocked)?.info
            if (info != null) accountRepository.observeAccount(info.id) else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val earningsState: StateFlow<ReferralEarnings> = unlockCheckState
        .flatMapLatest { check ->
            val info = (check as? UnlockCheckState.Unlocked)?.info
            if (info != null) referralEarningsRepository.observeEarnings(info.username) else flowOf(ReferralEarnings())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReferralEarnings()
        )

    private val appShareLinkState: StateFlow<AppShareLink?> = appShareRepository.observeCurrentShareLink()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val baseUiState: StateFlow<AppUiState> = combine(
        unlockCheckState,
        pricingState,
        contentDataState,
        downloadsCombinedState,
        wifiOnlyState
    ) { unlockCheck, pricing, content, downloadsCombined, wifiOnly ->
        val (downloadStatuses, downloadedMovieInfo) = downloadsCombined
        AppUiState(
            unlockInfo = (unlockCheck as? UnlockCheckState.Unlocked)?.info,
            pricing = pricing,
            movies = content.movies,
            categories = content.categories,
            genres = content.genres,
            banners = content.banners,
            downloadStatuses = downloadStatuses,
            downloadedMovieInfo = downloadedMovieInfo,
            wifiOnlyDownloads = wifiOnly,
            loading = unlockCheck is UnlockCheckState.Loading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppUiState()
    )

    val uiState: StateFlow<AppUiState> = combine(
        baseUiState,
        accountState,
        earningsState,
        appShareLinkState
    ) { base, account, earnings, appShareLink ->
        base.copy(
            account = account,
            earnings = earnings,
            appShareLink = appShareLink
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