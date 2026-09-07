package com.moviesforever.app.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.download.DownloadUtil
import com.moviesforever.app.download.MoviesForeverDownloadService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.downloadSettingsStore: DataStore<Preferences> by preferencesDataStore(name = "download_settings")

@Singleton
@UnstableApi
class DownloadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DownloadRepository {

    private object Keys {
        val WIFI_ONLY = booleanPreferencesKey("wifi_only_downloads")
    }

    override fun observeWifiOnly(): Flow<Boolean> {
        return context.downloadSettingsStore.data.map { prefs ->
            // Default ON: safer default so a free/trial user doesn't accidentally burn
            // mobile data the first time they hit "Download Offline".
            prefs[Keys.WIFI_ONLY] ?: true
        }
    }

    override suspend fun setWifiOnly(enabled: Boolean) {
        context.downloadSettingsStore.edit { prefs ->
            prefs[Keys.WIFI_ONLY] = enabled
        }
    }

    override fun observeDownloadStatuses(): Flow<Map<String, MovieDownloadStatus>> = callbackFlow {
        val downloadManager = DownloadUtil.getDownloadManager(context)

        // Read from the live in-memory list (currentDownloads), not the on-disk
        // index - the index is only flushed periodically, which is why the old
        // implementation looked "stuck" until the app was restarted.
        fun snapshot(): Map<String, MovieDownloadStatus> {
            return downloadManager.currentDownloads.associate { download ->
                download.request.id to download.toStatus()
            }
        }

        trySend(snapshot())

        val listener = object : androidx.media3.exoplayer.offline.DownloadManager.Listener {
            override fun onInitialized(downloadManager: androidx.media3.exoplayer.offline.DownloadManager) {
                // Fires once the manager has finished loading persisted downloads
                // from disk on startup - without this, a cold app start can briefly
                // show an empty/stale list even though downloads exist.
                trySend(snapshot())
            }

            override fun onDownloadChanged(
                downloadManager: androidx.media3.exoplayer.offline.DownloadManager,
                download: Download,
                finalException: Exception?
            ) {
                trySend(snapshot())
            }

            override fun onDownloadRemoved(
                downloadManager: androidx.media3.exoplayer.offline.DownloadManager,
                download: Download
            ) {
                trySend(snapshot())
            }
        }
        downloadManager.addListener(listener)

        // IMPORTANT: DownloadManager only notifies listeners on discrete state
        // changes (queued -> downloading -> completed/failed) - it does NOT call
        // onDownloadChanged on every progress tick. Without this poll, the
        // percentage only ever refreshes once per state transition, which is
        // what caused the "stuck until app restart" behaviour. Media3's own
        // notification progress bar works the same way internally (it polls
        // getCurrentDownloads() on a timer rather than waiting for callbacks).
        val pollingJob = launch {
            while (isActive) {
                delay(1000)
                val hasActiveDownload = downloadManager.currentDownloads.any {
                    it.state == Download.STATE_DOWNLOADING
                }
                if (hasActiveDownload) {
                    trySend(snapshot())
                }
            }
        }

        awaitClose {
            downloadManager.removeListener(listener)
            pollingJob.cancel()
        }
    }

    override suspend fun requestDownload(movie: Movie, isUnlocked: Boolean): DownloadRequestResult {
        // Rule 1: free-trial users can only download movies the admin marked as free.
        val allowedByPlan = movie.isFree || isUnlocked
        if (!allowedByPlan) {
            return DownloadRequestResult.RequiresUnlock
        }

        // Rule 2: respect the WiFi-only preference at the moment of the tap.
        val wifiOnlyRightNow = observeWifiOnly().first()

        if (wifiOnlyRightNow && !isOnWifi()) {
            return DownloadRequestResult.RequiresWifi
        }

        if (movie.videoUrl.isBlank()) {
            return DownloadRequestResult.RequiresUnlock // no playable source; nothing sensible to download
        }

        val request = DownloadRequest.Builder(movie.id, Uri.parse(movie.videoUrl))
            .setCustomCacheKey(movie.id)
            .setData(movie.title.toByteArray())
            .build()

        DownloadService.sendAddDownload(
            context,
            MoviesForeverDownloadService::class.java,
            request,
            /* foreground = */ false
        )

        return DownloadRequestResult.Started
    }

    override fun removeDownload(movieId: String) {
        DownloadService.sendRemoveDownload(
            context,
            MoviesForeverDownloadService::class.java,
            movieId,
            /* foreground = */ false
        )
    }

    private fun isOnWifi(): Boolean {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    private fun Download.toStatus(): MovieDownloadStatus = when (state) {
        Download.STATE_COMPLETED -> MovieDownloadStatus.Completed
        Download.STATE_FAILED -> MovieDownloadStatus.Failed
        Download.STATE_REMOVING -> MovieDownloadStatus.NotDownloaded
        else -> MovieDownloadStatus.Downloading(percentDownloaded.coerceIn(0f, 100f).toInt())
    }
}