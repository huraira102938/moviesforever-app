package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.moviesforever.app.data.model.TrendingItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrendingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TrendingRepository {

    // Same collection the admin panel's "Trending" page reads/writes:
    // { movieId: String, order: Int }.
    private val collection = firestore.collection("trending")

    // Live listener -- see MoviesRepositoryImpl for why.
    override fun observeTrendingItems(): Flow<List<TrendingItem>> = callbackFlow {
        val registration = collection
            .orderBy("order", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(TrendingItem::class.java)?.copy(id = doc.id)
                }
                trySend(items)
            }
        awaitClose { registration.remove() }
    }
}
