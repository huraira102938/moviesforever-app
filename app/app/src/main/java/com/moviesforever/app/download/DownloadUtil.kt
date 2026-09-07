package com.moviesforever.app.download

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import java.io.File
import java.util.concurrent.Executors

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

    private val httpDataSourceFactory: DataSource.Factory by lazy {
        DefaultHttpDataSource.Factory()
            .setUserAgent("MoviesForever/1.0 (Android)")
            .setAllowCrossProtocolRedirects(true)
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
            downloadManager = manager
            manager
        }
    }
}
