package com.moviesforever.app.data.repository

import com.moviesforever.app.data.model.Movie
import kotlinx.coroutines.flow.Flow

/** Status of a single movie's offline download, as shown in the UI. */
sealed class MovieDownloadStatus {
    data object NotDownloaded : MovieDownloadStatus()
    data class Downloading(val percent: Int) : MovieDownloadStatus()
    data object Completed : MovieDownloadStatus()
    data object Failed : MovieDownloadStatus()
}

/** Result of tapping the "Download Offline" button, so the UI can react
 * (show a paywall, tell the user to connect to WiFi, etc). */
sealed class DownloadRequestResult {
    data object Started : DownloadRequestResult()
    data object RequiresUnlock : DownloadRequestResult()
    data object RequiresWifi : DownloadRequestResult()
}

interface DownloadRepository {

    fun observeDownloadStatuses(): Flow<Map<String, MovieDownloadStatus>>

    fun observeDownloadedMovieInfo(): Flow<Map<String, Movie>>

    fun observeWifiOnly(): Flow<Boolean>

    suspend fun setWifiOnly(enabled: Boolean)

    suspend fun requestDownload(movie: Movie, isUnlocked: Boolean): DownloadRequestResult

    fun removeDownload(movieId: String)

    fun clearAllDownloads()
}