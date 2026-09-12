package com.moviesforever.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private val Context.installDataStore: DataStore<Preferences> by preferencesDataStore(name = "install_store")

// Mirrors the `settings/pricing` and `settings/app` docs the admin panel
// reads/writes today: one small Firestore doc, incremented atomically.
// The admin panel only needs to read `installs/counter`.installCount once it
// adds a UI for it -- this app-side change doesn't require any admin work.
private const val COUNTER_DOC_PATH = "installs/counter"
private const val COUNTER_FIELD = "installCount"

@Singleton
class InstallRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore
) : InstallRepository {

    private object Keys {
        val IS_INSTALLED = booleanPreferencesKey("is_installed")
    }

    override fun observeIsInstalled(): Flow<Boolean> {
        return context.installDataStore.data.map { prefs -> prefs[Keys.IS_INSTALLED] ?: false }
    }

    override suspend fun markInstalled() {
        val alreadyInstalled = context.installDataStore.data.first()[Keys.IS_INSTALLED] ?: false
        if (alreadyInstalled) {
            // Already counted on a previous tap/launch -- never count twice.
            return
        }

        // Flip the local flag first so a slow/failed network call can never
        // cause the button to be pressed twice by the same user. Firestore
        // has offline persistence enabled by default on Android, so this
        // increment is queued and synced automatically even if it's called
        // while offline.
        context.installDataStore.edit { prefs ->
            prefs[Keys.IS_INSTALLED] = true
        }

        try {
            firestore.document(COUNTER_DOC_PATH)
                .set(mapOf(COUNTER_FIELD to FieldValue.increment(1)), SetOptions.merge())
                .await()
        } catch (e: Exception) {
            // Best-effort: the local flag is what drives the UI (welcome
            // screen never shows again), so we don't retry here. Firestore's
            // own offline queue already covers the common "no network right
            // now" case.
        }
    }
}
