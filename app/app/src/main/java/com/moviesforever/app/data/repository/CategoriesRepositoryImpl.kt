package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoriesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CategoriesRepository {

    private val collection = firestore.collection("categories")

    override fun observeCategories(): Flow<List<Category>> = flow {
        val snapshot = collection.get().await()
        val cats = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Category::class.java)?.copy(id = doc.id)
        }.sortedBy { it.order }
        emit(cats)
    }
}
