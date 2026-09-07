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

    /** Map of movieId -> current download status, updated live as downloads progress. */
    fun observeDownloadStatuses(): Flow<Map<String, MovieDownloadStatus>>

    /** Whether downloads should be restricted to WiFi. Defaults to true (safer for user's data plan). */
    fun observeWifiOnly(): Flow<Boolean>

    suspend fun setWifiOnly(enabled: Boolean)

    /**
     * Attempts to start a download for [movie].
     *
     * Enforces both access rules server-admins expect:
     *  - Free-trial (locked) users may only download movies flagged `isFree` in the admin panel.
     *  - Unlocked (lifetime/premium) users may download anything.
     * and the WiFi-only preference, checked against the network at the moment of the tap.
     */
    suspend fun requestDownload(movie: Movie, isUnlocked: Boolean): DownloadRequestResult

    fun removeDownload(movieId: String)
}
