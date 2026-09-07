package com.moviesforever.app.download

import android.app.Notification
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import com.moviesforever.app.R

/**
 * Concrete implementation of Media3's [DownloadService].
 *
 * The manifest previously pointed straight at the abstract
 * `androidx.media3.exoplayer.download.DownloadService`, which cannot be
 * instantiated by the system - so every enqueued download silently went
 * nowhere and the "Download" button appeared to do nothing. This subclass
 * is what actually needs to be registered instead.
 */
@UnstableApi
class MoviesForeverDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    R.string.downloads_channel_name,
    0
) {

    override fun getDownloadManager(): DownloadManager {
        val manager = DownloadUtil.getDownloadManager(this)
        // DownloadUtil.getDownloadManager returns a process-wide singleton, but
        // getDownloadManager() can be called again if the service is recreated -
        // only attach the terminal-state notifier once per process.
        if (!listenerAttached) {
            manager.addListener(TerminalStateNotificationHelper(applicationContext, notificationHelper, FOREGROUND_NOTIFICATION_ID + 1))
            listenerAttached = true
        }
        return manager
    }

    // No Scheduler: downloads only progress while this service is alive (i.e. while
    // the app is in the foreground or the download is actively running), which is
    // sufficient here and avoids needing an extra JobService manifest entry.
    override fun getScheduler(): Scheduler? = null

    override fun getForegroundNotification(downloads: MutableList<Download>, notMetRequirements: Int): Notification {
        return notificationHelper.buildProgressNotification(
            this,
            R.drawable.ic_notification_download,
            null,
            null,
            downloads,
            notMetRequirements
        )
    }

    private val notificationHelper: DownloadNotificationHelper by lazy {
        DownloadNotificationHelper(this, CHANNEL_ID)
    }

    companion object {
        private const val CHANNEL_ID = "moviesforever_downloads"
        private const val FOREGROUND_NOTIFICATION_ID = 2001

        @Volatile
        private var listenerAttached = false
    }
}

/** Posts a one-off notification when a download finishes or fails, since the
 * foreground notification only covers in-progress downloads. */
@OptIn(UnstableApi::class)
private class TerminalStateNotificationHelper(
    private val context: android.content.Context,
    private val notificationHelper: DownloadNotificationHelper,
    private val nextNotificationId: Int
) : DownloadManager.Listener {

    private var notificationId = nextNotificationId

    override fun onDownloadChanged(downloadManager: DownloadManager, download: Download, finalException: Exception?) {
        val notification = when (download.state) {
            Download.STATE_COMPLETED ->
                notificationHelper.buildDownloadCompletedNotification(
                    context,
                    com.moviesforever.app.R.drawable.ic_notification_download,
                    null,
                    download.request.customCacheKey ?: download.request.id
                )
            Download.STATE_FAILED ->
                notificationHelper.buildDownloadFailedNotification(
                    context,
                    com.moviesforever.app.R.drawable.ic_notification_download,
                    null,
                    download.request.customCacheKey ?: download.request.id
                )
            else -> null
        } ?: return

        val manager = context.getSystemService(android.app.NotificationManager::class.java)
        manager?.notify(notificationId++, notification)
    }
}
