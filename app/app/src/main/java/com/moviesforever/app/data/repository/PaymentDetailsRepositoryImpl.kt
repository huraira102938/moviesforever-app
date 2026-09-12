package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.PaymentDetails
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentDetailsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PaymentDetailsRepository {

    // Live listener (not a one-shot get()) so admin changes to the bank
    // details show up in the app right away, without a new release.
    override fun observePaymentDetails(): Flow<PaymentDetails> = callbackFlow {
        val registration = firestore.collection("settings").document("payment-details")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(PaymentDetails())
                    return@addSnapshotListener
                }
                val data = snapshot.data ?: emptyMap<String, Any>()
                trySend(
                    PaymentDetails(
                        bankName = data["bankName"] as? String ?: "",
                        accountTitle = data["accountTitle"] as? String ?: "",
                        accountNumber = data["accountNumber"] as? String ?: ""
                    )
                )
            }

        awaitClose { registration.remove() }
    }
}
