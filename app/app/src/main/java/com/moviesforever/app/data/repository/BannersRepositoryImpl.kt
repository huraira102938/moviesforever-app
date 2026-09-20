package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.Banner
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BannersRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BannersRepository {

    private val collection = firestore.collection("banners")

    // Live listener -- see MoviesRepositoryImpl for why.
    override fun observeBanners(): Flow<List<Banner>> = callbackFlow {
        val registration = collection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val banners = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Banner::class.java)?.copy(id = doc.id)
            }.sortedBy { it.order }
            trySend(banners)
        }
        awaitClose { registration.remove() }
    }
}
