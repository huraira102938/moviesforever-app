package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.moviesforever.app.data.model.AppNotification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationsRepository {

    private val collection = firestore.collection("notifications")

    // Only the newest 30 are read, so launch cost stays flat however many notifications the
    // admin has sent over time (it used to re-read every notification ever sent).
    override fun observeNotifications(): Flow<List<AppNotification>> = callbackFlow {
        val registration = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(NOTIFICATION_LIMIT)
            .addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val notifications = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                @Suppress("UNCHECKED_CAST")
                val targets = (data["targets"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                AppNotification(
                    id = doc.id,
                    text = data["text"] as? String ?: "",
                    targets = targets,
                    createdAt = data["createdAt"] as? String ?: ""
                )
            }.sortedByDescending { it.createdAt }
            trySend(notifications)
        }
        awaitClose { registration.remove() }
    }
}

private const val NOTIFICATION_LIMIT = 30L
