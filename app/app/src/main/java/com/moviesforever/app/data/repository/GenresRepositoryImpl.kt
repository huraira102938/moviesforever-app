package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.Genre
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenresRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : GenresRepository {

    private val collection = firestore.collection("genres")

    // Live listener -- see MoviesRepositoryImpl for why.
    override fun observeGenres(): Flow<List<Genre>> = callbackFlow {
        val registration = collection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val genres = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Genre::class.java)?.copy(id = doc.id)
            }.sortedBy { it.name }
            trySend(genres)
        }
        awaitClose { registration.remove() }
    }
}
