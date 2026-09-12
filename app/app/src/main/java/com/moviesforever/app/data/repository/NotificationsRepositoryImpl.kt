package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.AppNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationsRepository {

    private val collection = firestore.collection("notifications")

    override fun observeNotifications(): Flow<List<AppNotification>> = flow {
        val snapshot = collection.get().await()
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
        emit(notifications)
    }
}
