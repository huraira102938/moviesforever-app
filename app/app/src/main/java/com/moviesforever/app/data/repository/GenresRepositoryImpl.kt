package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.Genre
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenresRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : GenresRepository {

    private val collection = firestore.collection("genres")

    override fun observeGenres(): Flow<List<Genre>> = flow {
        val snapshot = collection.get().await()
        val genres = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Genre::class.java)?.copy(id = doc.id)
        }.sortedBy { it.name }
        emit(genres)
    }
}
