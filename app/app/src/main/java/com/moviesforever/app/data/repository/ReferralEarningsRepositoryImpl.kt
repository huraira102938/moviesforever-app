package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.ReferralEarnings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReferralEarningsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReferralEarningsRepository {

    // Mirrors admin's UserManagement.tsx totals: sum referrerPendingAmount
    // across transactions where referralUsername == username, split by
    // status ('pending' vs 'paid'). Real-time listener so a payment the
    // admin just verified/marked-paid shows up without a restart.
    override fun observeEarnings(username: String): Flow<ReferralEarnings> {
        if (username.isBlank()) return flowOf(ReferralEarnings())

        return callbackFlow {
            val registration = firestore.collection("transactions")
                .whereEqualTo("referralUsername", username)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        trySend(ReferralEarnings())
                        return@addSnapshotListener
                    }
                    var pending = 0.0
                    var paid = 0.0
                    var totalReceived = 0.0
                    for (doc in snapshot.documents) {
                        val payout = doc.getDouble("referrerPendingAmount") ?: 0.0
                        val received = doc.getDouble("totalReceived") ?: 0.0
                        totalReceived += received
                        when (doc.getString("status")) {
                            "pending" -> pending += payout
                            "paid" -> paid += payout
                        }
                    }
                    trySend(
                        ReferralEarnings(
                            pendingAmount = pending,
                            paidAmount = paid,
                            totalReceived = totalReceived,
                            referralTransactionCount = snapshot.size()
                        )
                    )
                }
            awaitClose { registration.remove() }
        }
    }
}
