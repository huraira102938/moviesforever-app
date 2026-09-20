package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.AppShareLink
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppShareRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AppShareRepository {

    // The admin panel supports multiple app-sharing links; it always shows
    // (and expects) the newest one to be treated as current, sorted by
    // createdAt descending. createdAt is a fixed-format ISO-8601 string
    // (new Date().toISOString()), so plain string comparison is safe.
    override fun observeCurrentShareLink(): Flow<AppShareLink?> = callbackFlow {
        val registration = firestore.collection("app-sharing")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val docs = snapshot.documents
                    .mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        AppShareLink(
                            id = doc.id,
                            title = data["title"] as? String ?: "",
                            apkUrl = data["apkUrl"] as? String ?: "",
                            version = data["version"] as? String ?: "",
                            createdAt = data["createdAt"] as? String ?: ""
                        )
                    }
                // Prefer the newest doc that still has a drive link so the
                // in-app share keeps working even when a newer APK-only entry
                // (used for the landing page download) is added above it.
                val withLink = docs.filter { it.apkUrl.isNotBlank() }
                val newest = docs.maxByOrNull { it.createdAt }
                val current = newest?.takeIf { it.apkUrl.isNotBlank() } ?: withLink.maxByOrNull { it.createdAt }
                trySend(current)
            }
        awaitClose { registration.remove() }
    }
}
