package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.PricingSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.moviesforever.app.data.remote.Market
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PricingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PricingRepository {

    // Live listener -- see MoviesRepositoryImpl for why.
    override fun observePricing(): Flow<PricingSettings> = callbackFlow {
        val registration = firestore.document("settings/${Market.PRICING_DOC}")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(PricingSettings())
                    return@addSnapshotListener
                }
                val data = snapshot.data ?: emptyMap<String, Any>()
                trySend(
                    PricingSettings(
                        standardPrice = (data["standardPrice"] as? Number)?.toDouble() ?: 0.0,
                        referralPrice = (data["referralPrice"] as? Number)?.toDouble() ?: 0.0,
                        referralPayout = (data["referralPayout"] as? Number)?.toDouble() ?: 0.0,
                        note = (data["note"] as? String)?.trim() ?: ""
                    )
                )
            }
        awaitClose { registration.remove() }
    }
}
