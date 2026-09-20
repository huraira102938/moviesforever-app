package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.Category
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoriesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CategoriesRepository {

    private val collection = firestore.collection("categories")

    // Live listener -- see MoviesRepositoryImpl for why.
    override fun observeCategories(): Flow<List<Category>> = callbackFlow {
        val registration = collection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val cats = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Category::class.java)?.copy(id = doc.id)
            }.sortedBy { it.order }
            trySend(cats)
        }
        awaitClose { registration.remove() }
    }
}
