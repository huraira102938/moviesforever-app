package com.moviesforever.app.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.google.gson.Gson
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

private const val TAG = "MF_Download"

@Singleton
@UnstableApi
class DownloadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DownloadRepository {

    private object Keys {
        val WIFI_ONLY = booleanPreferencesKey("wifi_only_downloads")
    }

    private val gson = Gson()

    override fun observeWifiOnly(): Flow<Boolean> {
        return context.downloadSettingsStore.data.map { prefs ->
            prefs[Keys.WIFI_ONLY] ?: true
        }
    }

    override suspend fun setWifiOnly(enabled: Boolean) {
        context.downloadSettingsStore.edit { prefs ->
            prefs[Keys.WIFI_ONLY] = enabled
        }
    }

    /**
     * Builds the full, current set of every known download, in every state.
     *
     * IMPORTANT: DownloadManager.getCurrentDownloads() explicitly EXCLUDES
     * completed and failed downloads (this is documented Media3 behaviour,
     * not a bug on Google's end) - using it alone caused a download to
     * disappear from the UI the instant it finished, even though the file
     * was still on disk and playable offline. downloadIndex.getDownloads()
     * is the persisted record of every download regardless of state, so we
     * use it as the base and overlay currentDownloads on top purely to get
     * live percentage ticks for anything still actively downloading.
     */
    private fun allDownloadsById(downloadManager: androidx.media3.exoplayer.offline.DownloadManager): Map<String, Download> {
        val all = mutableMapOf<String, Download>()
        downloadManager.downloadIndex.getDownloads().use { cursor ->
            while (cursor.moveToNext()) {
                all[cursor.download.request.id] = cursor.download
            }
        }
        downloadManager.currentDownloads.forEach { download ->
            all[download.request.id] = download
        }
        return all
    }

    override fun observeDownloadStatuses(): Flow<Map<String, MovieDownloadStatus>> = callbackFlow {
        val downloadManager = DownloadUtil.getDownloadManager(context)

        fun snapshot(): Map<String, MovieDownloadStatus> =
            allDownloadsById(downloadManager).mapValues { (_, download) -> download.toStatus() }

        trySend(snapshot())

        val listener = object : androidx.media3.exoplayer.offline.DownloadManager.Listener {
            override fun onInitialized(downloadManager: androidx.media3.exoplayer.offline.DownloadManager) {
                trySend(snapshot())
            }

            override fun onDownloadChanged(
                downloadManager: androidx.media3.exoplayer.offline.DownloadManager,
                download: Download,
                finalException: Exception?
            ) {
                if (download.state == Download.STATE_COMPLETED || download.state == Download.STATE_FAILED) {
                    Log.d(TAG, "onDownloadChanged: id=${download.request.id} -> ${stateName(download.state)} finalException=$finalException")
                }
                trySend(snapshot())
            }

            override fun onDownloadRemoved(
                downloadManager: androidx.media3.exoplayer.offline.DownloadManager,
                download: Download
            ) {
                Log.w(TAG, "onDownloadRemoved: id=${download.request.id} lastState=${stateName(download.state)}")
                trySend(snapshot())
            }
        }
        downloadManager.addListener(listener)

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

    override fun observeDownloadedMovieInfo(): Flow<Map<String, Movie>> = callbackFlow {
        val downloadManager = DownloadUtil.getDownloadManager(context)

        fun snapshot(): Map<String, Movie> {
            val downloads = allDownloadsById(downloadManager)
            val decoded = downloads.mapNotNull { (id, download) ->
                decodeMovie(id, download.request.data)?.let { movie -> id to movie }
            }.toMap()
            if (decoded.size != downloads.size) {
                Log.w(TAG, "observeDownloadedMovieInfo: ${downloads.size - decoded.size} of ${downloads.size} downloads failed to decode and would have been hidden")
            }
            return decoded
        }

        trySend(snapshot())

        val listener = object : androidx.media3.exoplayer.offline.DownloadManager.Listener {
            override fun onInitialized(downloadManager: androidx.media3.exoplayer.offline.DownloadManager) {
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

        awaitClose {
            downloadManager.removeListener(listener)
        }
    }

    private fun decodeMovie(downloadId: String, data: ByteArray): Movie? {
        if (data.isEmpty()) {
            Log.w(TAG, "decodeMovie: id=$downloadId has EMPTY data blob (nothing to decode)")
            return null
        }

        return try {
            gson.fromJson(String(data), Movie::class.java)
        } catch (e: Exception) {
            Log.w(TAG, "decodeMovie: id=$downloadId FAILED to decode (${data.size} bytes) -- falling back to raw title", e)
            val rawTitle = String(data).trim().ifBlank { "Downloaded video" }
            runCatching { Movie(id = downloadId, title = rawTitle) }.getOrNull()
        }
    }

    override suspend fun requestDownload(movie: Movie, isUnlocked: Boolean): DownloadRequestResult {
        val allowedByPlan = movie.isFree || isUnlocked
        if (!allowedByPlan) {
            return DownloadRequestResult.RequiresUnlock
        }

        val wifiOnlyRightNow = observeWifiOnly().first()

        if (wifiOnlyRightNow && !isOnWifi()) {
            return DownloadRequestResult.RequiresWifi
        }

        if (movie.videoUrl.isBlank()) {
            return DownloadRequestResult.RequiresUnlock
        }

        val request = DownloadRequest.Builder(movie.id, Uri.parse(movie.videoUrl))
            .setCustomCacheKey(movie.id)
            .setData(gson.toJson(movie).toByteArray())
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

    override fun clearAllDownloads() {
        val downloadManager = DownloadUtil.getDownloadManager(context)
        allDownloadsById(downloadManager).keys.forEach { id ->
            DownloadService.sendRemoveDownload(
                context,
                MoviesForeverDownloadService::class.java,
                id,
                /* foreground = */ false
            )
        }
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

    private fun stateName(state: Int): String = when (state) {
        Download.STATE_QUEUED -> "QUEUED"
        Download.STATE_STOPPED -> "STOPPED"
        Download.STATE_DOWNLOADING -> "DOWNLOADING"
        Download.STATE_COMPLETED -> "COMPLETED"
        Download.STATE_FAILED -> "FAILED"
        Download.STATE_REMOVING -> "REMOVING"
        Download.STATE_RESTARTING -> "RESTARTING"
        else -> "UNKNOWN($state)"
    }
}