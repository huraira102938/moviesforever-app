package com.moviesforever.app.download

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import java.io.File
import java.util.concurrent.Executors

private const val TAG = "MF_Download"

/** Decodes DownloadManager.Listener#onRequirementsStateChanged's notMetRequirements
 * bitmask into readable text, since a raw int here is useless for debugging.
 * If this ever logs anything other than "(none)" while a download is stuck,
 * THAT is the root cause - Media3 has silently paused the download and is
 * just waiting, with no error and no UI indication. */
@OptIn(UnstableApi::class)
private fun decodeRequirements(notMetRequirements: Int): String {
    if (notMetRequirements == 0) return "(none - requirements satisfied)"
    val flags = mutableListOf<String>()
    if (notMetRequirements and androidx.media3.exoplayer.scheduler.Requirements.NETWORK != 0) flags += "NETWORK"
    if (notMetRequirements and androidx.media3.exoplayer.scheduler.Requirements.NETWORK_UNMETERED != 0) flags += "NETWORK_UNMETERED"
    if (notMetRequirements and androidx.media3.exoplayer.scheduler.Requirements.DEVICE_IDLE != 0) flags += "DEVICE_IDLE"
    if (notMetRequirements and androidx.media3.exoplayer.scheduler.Requirements.DEVICE_CHARGING != 0) flags += "DEVICE_CHARGING"
    if (notMetRequirements and androidx.media3.exoplayer.scheduler.Requirements.DEVICE_STORAGE_NOT_LOW != 0) flags += "DEVICE_STORAGE_NOT_LOW"
    return flags.joinToString(", ")
}

@OptIn(UnstableApi::class)
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

/**
 * Provides a single, app-wide instance of everything Media3 needs to download and
 * play back offline media: the on-disk [Cache], the [DownloadManager] that drives
 * downloads, and a [CacheDataSource.Factory] used both by the downloader and by the
 * player so that anything already downloaded is played straight from disk instead
 * of being re-streamed.
 *
 * This mirrors the pattern used by [UnlockRepositoryImpl] for DataStore: a single
 * lazily-created singleton, built off the application context so it survives
 * configuration changes and is shared by the DownloadService, the repository and
 * the player screen.
 */
@UnstableApi
object DownloadUtil {

    private const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"

    @Volatile
    private var downloadManager: DownloadManager? = null

    @Volatile
    private var downloadCache: Cache? = null

    @Volatile
    private var databaseProvider: StandaloneDatabaseProvider? = null

    // Explicit timeouts: the default DefaultHttpDataSource timeout (8s) only
    // fires if NO data arrives at all within the window. A connection that's
    // trickling a few bytes every few seconds resets that timer on every read
    // and can "load" indefinitely without ever throwing - which matches the
    // "loading for thousand minutes" symptom exactly. These are intentionally
    // generous (15s) so a genuinely slow-but-working connection isn't killed,
    // but a truly stalled one now fails with a catchable exception instead of
    // hanging forever.
    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 15_000

    private val httpDataSourceFactory: DataSource.Factory by lazy {
        DefaultHttpDataSource.Factory()
            .setUserAgent("MoviesForever/1.0 (Android)")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(CONNECT_TIMEOUT_MS)
            .setReadTimeoutMs(READ_TIMEOUT_MS)
    }

    @Synchronized
    private fun getDatabaseProvider(context: Context): StandaloneDatabaseProvider {
        return databaseProvider ?: StandaloneDatabaseProvider(context.applicationContext).also {
            databaseProvider = it
        }
    }

    @Synchronized
    fun getDownloadCache(context: Context): Cache {
        return downloadCache ?: run {
            val downloadContentDirectory = File(context.getExternalFilesDir(null) ?: context.filesDir, DOWNLOAD_CONTENT_DIRECTORY)
            // NoOpCacheEvictor: downloaded files are only ever removed by an explicit
            // user action (via DownloadRepository.removeDownload), never automatically
            // evicted to make room, so a completed download can't silently disappear.
            SimpleCache(downloadContentDirectory, NoOpCacheEvictor(), getDatabaseProvider(context)).also {
                downloadCache = it
            }
        }
    }

    /** Data source factory used by both the downloader and the player, so anything
     * already downloaded is read from disk instead of being re-fetched over the network. */
    fun getCacheDataSourceFactory(context: Context): CacheDataSource.Factory {
        return CacheDataSource.Factory()
            .setCache(getDownloadCache(context))
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setCacheWriteDataSinkFactory(null) // read from cache; writing is handled by the downloader
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    fun getDefaultDataSourceFactory(context: Context): DataSource.Factory {
        return DefaultDataSource.Factory(context, httpDataSourceFactory)
    }

    @Synchronized
    fun getDownloadManager(context: Context): DownloadManager {
        return downloadManager ?: run {
            val manager = DownloadManager(
                context,
                getDatabaseProvider(context),
                getDownloadCache(context),
                httpDataSourceFactory,
                Executors.newFixedThreadPool(2)
            ).apply {
                maxParallelDownloads = 2
            }
            attachDiagnosticLogger(manager)
            downloadManager = manager
            manager
        }
    }

    /** Verbose logging attached once, at the source, so it captures everything
     * regardless of which screen/repository is observing. This is the log to
     * watch while a download is stuck: it logs EVERY state change (not just
     * completed/failed), the running percent/bytes, and - the key one -
     * onRequirementsStateChanged, which fires silently whenever Media3 pauses
     * a download because it thinks a requirement (default: network) isn't met. */
    private fun attachDiagnosticLogger(manager: DownloadManager) {
        Log.d(TAG, "DownloadManager created. Default requirements=${manager.requirements}, notMetRequirements=${decodeRequirements(manager.notMetRequirements)}")
        manager.addListener(object : DownloadManager.Listener {
            override fun onInitialized(downloadManager: DownloadManager) {
                Log.d(TAG, "onInitialized: ${downloadManager.currentDownloads.size} active downloads restored from disk")
            }

            override fun onDownloadChanged(downloadManager: DownloadManager, download: Download, finalException: Exception?) {
                Log.d(
                    TAG,
                    "onDownloadChanged: id=${download.request.id} state=${stateName(download.state)} " +
                            "percent=${download.percentDownloaded} bytes=${download.bytesDownloaded}/${download.contentLength} " +
                            "notMetRequirements=${decodeRequirements(downloadManager.notMetRequirements)} " +
                            "finalException=$finalException"
                )
                finalException?.let { Log.e(TAG, "  -> exception detail for id=${download.request.id}", it) }
            }

            override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
                Log.w(TAG, "onDownloadRemoved: id=${download.request.id} lastState=${stateName(download.state)}")
            }

            override fun onRequirementsStateChanged(
                downloadManager: DownloadManager,
                requirements: androidx.media3.exoplayer.scheduler.Requirements,
                notMetRequirements: Int
            ) {
                // THIS is the log line to look for while a download sits at a fixed
                // percent doing nothing. If notMetRequirements != "(none)" here, Media3
                // has paused every download until that requirement is satisfied again -
                // most likely a network-connectivity flap Android detected that you didn't notice.
                Log.w(TAG, "onRequirementsStateChanged: notMetRequirements=${decodeRequirements(notMetRequirements)} (raw=$notMetRequirements)")
            }

            override fun onDownloadsPausedChanged(downloadManager: DownloadManager, downloadsPaused: Boolean) {
                Log.w(TAG, "onDownloadsPausedChanged: downloadsPaused=$downloadsPaused")
            }

            override fun onIdle(downloadManager: DownloadManager) {
                Log.d(TAG, "onIdle: DownloadManager has nothing left to do right now")
            }
        })
    }
}