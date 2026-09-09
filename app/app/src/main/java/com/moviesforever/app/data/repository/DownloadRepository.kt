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

    /**
     * Movie metadata captured at the moment each download was started, keyed by
     * movieId. Decoded straight from the persisted Media3 download request -- this
     * is the source of truth for what to display in "My Downloads", and it does
     * NOT depend on the movie still being present in the live catalog (Firestore).
     *
     * This is what fixes the bug where a fully-downloaded, playable-offline movie
     * would vanish from the Downloads screen: previously the UI built its list by
     * intersecting download status with the *current* remote movie catalog fetch,
     * so removing/unpublishing a movie, or any hiccup in that one-shot network
     * call, silently dropped an already-downloaded file from the list even though
     * it was still sitting on disk and playable.
     */
    fun observeDownloadedMovieInfo(): Flow<Map<String, Movie>>

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
