package com.moviesforever.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MoviesForeverApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeFirebase()
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
