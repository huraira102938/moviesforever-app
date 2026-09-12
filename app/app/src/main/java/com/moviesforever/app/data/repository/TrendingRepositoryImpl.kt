package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.moviesforever.app.data.model.TrendingItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrendingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TrendingRepository {

    // Same collection the admin panel's "Trending" page reads/writes:
    // { movieId: String, order: Int }.
    private val collection = firestore.collection("trending")

    override fun observeTrendingItems(): Flow<List<TrendingItem>> = flow {
        val snapshot = collection.orderBy("order", Query.Direction.ASCENDING).get().await()
        val items = snapshot.documents.mapNotNull { doc ->
            doc.toObject(TrendingItem::class.java)?.copy(id = doc.id)
        }
        emit(items)
    }
}
