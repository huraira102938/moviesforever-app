package com.moviesforever.app.data.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.AppUpdateConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Live view of `settings/appUpdate` (the admin panel's Force Update page).
 *
 * The last value seen is saved on the phone. If the phone is offline (or Firestore fails) the
 * saved value is used, so a blocked build cannot get past the block just by switching off the
 * internet. If nothing was ever saved, the app is allowed to run.
 */
@Singleton
class AppUpdateRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) {
    private val prefs get() = context.getSharedPreferences("app_update_cache", Context.MODE_PRIVATE)

    fun observe(): Flow<AppUpdateConfig> = callbackFlow {
        val registration = firestore.collection("settings").document("appUpdate")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(readCache())
                    return@addSnapshotListener
                }
                if (!snapshot.exists()) {
                    // Doc deleted / never created = no force update.
                    val none = AppUpdateConfig()
                    writeCache(none)
                    trySend(none)
                    return@addSnapshotListener
                }
                val d = snapshot.data ?: emptyMap<String, Any>()
                val config = AppUpdateConfig(
                    enabled = d["enabled"] as? Boolean ?: false,
                    minVersionCode = (d["minVersionCode"] as? Number)?.toLong() ?: 0L,
                    title = d["title"] as? String ?: "",
                    message = d["message"] as? String ?: "",
                    contactNumber = d["contactNumber"] as? String ?: "",
                    downloadUrl = d["downloadUrl"] as? String ?: ""
                )
                writeCache(config)
                trySend(config)
            }
        awaitClose { registration.remove() }
    }

    fun readCache(): AppUpdateConfig = try {
        val p = prefs
        AppUpdateConfig(
            enabled = p.getBoolean("enabled", false),
            minVersionCode = p.getLong("minVersionCode", 0L),
            title = p.getString("title", "") ?: "",
            message = p.getString("message", "") ?: "",
            contactNumber = p.getString("contactNumber", "") ?: "",
            downloadUrl = p.getString("downloadUrl", "") ?: ""
        )
    } catch (_: Exception) {
        AppUpdateConfig()
    }

    private fun writeCache(c: AppUpdateConfig) {
        try {
            prefs.edit()
                .putBoolean("enabled", c.enabled)
                .putLong("minVersionCode", c.minVersionCode)
                .putString("title", c.title)
                .putString("message", c.message)
                .putString("contactNumber", c.contactNumber)
                .putString("downloadUrl", c.downloadUrl)
                .apply()
        } catch (_: Exception) {
            // best effort
        }
    }
}
