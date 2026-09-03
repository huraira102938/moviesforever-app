package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.moviesforever.app.data.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MoviesRepository {

    private val collection = firestore.collection("movies")

    override fun observeMovies(): Flow<List<Movie>> = flow {
        val snapshot = collection.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        val movies = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Movie::class.java)?.copy(id = doc.id)
        }
        emit(movies)
    }

    override suspend fun getMovieById(id: String): Movie? {
        val doc = collection.document(id).get().await()
        return if (doc.exists()) doc.toObject(Movie::class.java)?.copy(id = doc.id) else null
    }
}
