package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.AppConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AppConfigRepository {

    // Doc path mirrors the existing settings/pricing doc used by
    // PricingRepositoryImpl. Live listener -- see MoviesRepositoryImpl for why.
    override fun observeAppConfig(): Flow<AppConfig> = callbackFlow {
        val registration = firestore.document("settings/app")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(AppConfig())
                    return@addSnapshotListener
                }
                val data = snapshot.data ?: emptyMap<String, Any>()
                trySend(
                    AppConfig(
                        apkShareUrl = (data["apkShareUrl"] as? String)?.trim() ?: ""
                    )
                )
            }
        awaitClose { registration.remove() }
    }
}
