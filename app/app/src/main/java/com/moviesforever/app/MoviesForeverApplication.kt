package com.moviesforever.app

import android.app.Application
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadService
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.moviesforever.app.download.MoviesForeverDownloadService
import com.moviesforever.app.push.PushNotifier
import dagger.hilt.android.HiltAndroidApp

private const val TAG = "MF_Download"

@UnstableApi
@HiltAndroidApp
class MoviesForeverApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeFirebase()
        // Channel must exist before a background push arrives, or Android uses a generic one.
        PushNotifier.ensureChannel(this)

        // THE MISSING FIX: nothing else in this app ever calls DownloadService.start().
        // Without this, a killed/stalled download only resumes whenever the OS randomly
        // decides to restart the service (Media3 uses START_STICKY) - which explains the
        // "stuck for 15-20 min then suddenly moves" behaviour. This call is safe even if
        // there's nothing to resume; it just re-attaches to any QUEUED/DOWNLOADING entries.
        Log.d(TAG, "Application.onCreate: calling DownloadService.start() to resume any pending downloads")
        try {
            DownloadService.start(this, MoviesForeverDownloadService::class.java)
        } catch (e: IllegalStateException) {
            // Thrown if this somehow runs while the app is fully backgrounded on API 26+
            // without a foreground download already active - fall back to a non-throwing start.
            Log.w(TAG, "DownloadService.start() rejected, falling back to startForegroundService path", e)
            DownloadService.startForeground(this, MoviesForeverDownloadService::class.java)
        }
    }

    private fun initializeFirebase() {
        if (FirebaseApp.getApps(this).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyDupSvfIg0tV4GLOxq7i4hgK5NUpkELgpA")
                .setApplicationId("1:1089292070173:android:94d9d3ba49200992a545f4")
                .setProjectId("moviesforever-da21d")
                .setStorageBucket("moviesforever-da21d.firebasestorage.app")
                .setGcmSenderId("1089292070173")
                .build()
            FirebaseApp.initializeApp(this, options)
        }
    }
}