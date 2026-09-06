package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.PricingSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PricingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PricingRepository {

    override fun observePricing(): Flow<PricingSettings> = flow {
        val defaults = PricingSettings()
        emit(defaults)
        try {
            val doc = firestore.document("settings/pricing").get().await()
            if (doc.exists()) {
                val data = doc.data ?: return@flow
                val defaultForMissingFields = PricingSettings()
                emit(
                    PricingSettings(
                        standardPrice = (data["standardPrice"] as? Number)?.toDouble() ?: 0.0,
                        referralPrice = (data["referralPrice"] as? Number)?.toDouble() ?: 0.0,
                        referralPayout = (data["referralPayout"] as? Number)?.toDouble() ?: 0.0,
                        jazzcashNumber = (data["jazzcashNumber"] as? String)?.takeIf { it.isNotBlank() }
                            ?: defaultForMissingFields.jazzcashNumber,
                        jazzcashTitle = (data["jazzcashTitle"] as? String)?.takeIf { it.isNotBlank() }
                            ?: defaultForMissingFields.jazzcashTitle
                    )
                )
            }
        } catch (e: Exception) {
            // keep defaults on error
        }
    }
}
