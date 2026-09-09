package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.AppConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AppConfigRepository {

    // Doc path mirrors the existing settings/pricing doc used by
    // PricingRepositoryImpl.
    override fun observeAppConfig(): Flow<AppConfig> = flow {
        emit(AppConfig())
        try {
            val doc = firestore.document("settings/app").get().await()
            if (doc.exists()) {
                val data = doc.data ?: return@flow
                emit(
                    AppConfig(
                        apkShareUrl = (data["apkShareUrl"] as? String)?.trim() ?: ""
                    )
                )
            }
        } catch (e: Exception) {
            // keep default (empty) on error
        }
    }
}
