package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.Banner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BannersRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BannersRepository {

    private val collection = firestore.collection("banners")

    override fun observeBanners(): Flow<List<Banner>> = flow {
        val snapshot = collection.get().await()
        val banners = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Banner::class.java)?.copy(id = doc.id)
        }.sortedBy { it.order }
        emit(banners)
    }
}
