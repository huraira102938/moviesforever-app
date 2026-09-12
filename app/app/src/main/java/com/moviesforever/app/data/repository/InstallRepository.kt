package com.moviesforever.app.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Tracks whether this device has been through the one-time "Start Browsing"
 * welcome step.
 *
 * The flag is stored locally (see [InstallRepositoryImpl]) so it starts out
 * false on every fresh install, flips to true the first time the user taps
 * "Start Browsing", and stays true forever after that -- the only way it
 * goes back to false is the user clearing the app's storage from Android
 * Settings, or uninstalling/reinstalling the app. There is deliberately no
 * "reset" function here (unlike UnlockRepository), because nothing in the
 * app is supposed to ever flip this back to false.
 */
interface InstallRepository {
    fun observeIsInstalled(): Flow<Boolean>

    /**
     * Marks this device as installed (persisted locally, permanently) and,
     * the first time this is called, increments the global install counter
     * in Firestore by 1. Safe to call more than once -- it only counts once.
     */
    suspend fun markInstalled()
}
