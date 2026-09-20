package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.moviesforever.app.data.model.Movie
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MoviesRepository {

    private val collection = firestore.collection("movies")

    // Live listener (not a one-shot get()) so this doesn't re-read the
    // entire movies collection every time something re-subscribes to it
    // (e.g. navigating between screens). Firestore keeps this connection
    // open and only bills for documents that actually changed after the
    // first snapshot -- see AppViewModel's use of SharingStarted.Eagerly
    // for why this listener, once started, stays attached for the whole
    // app session instead of being torn down and re-created.
    override fun observeMovies(): Flow<List<Movie>> = callbackFlow {
        val registration = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val movies = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Movie::class.java)?.copy(id = doc.id)
                }
                trySend(movies)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getMovieById(id: String): Movie? {
        val doc = collection.document(id).get().await()
        return if (doc.exists()) doc.toObject(Movie::class.java)?.copy(id = doc.id) else null
    }
}

