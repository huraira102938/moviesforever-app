package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.UserAccount
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AccountRepository {

    // Live listener (not a one-shot get()) because this data changes the
    // moment the admin verifies a payment or settles a payout -- the
    // Profile/Referral screens should reflect that without the user having
    // to force-close and reopen the app.
    override fun observeAccount(userId: String): Flow<UserAccount?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val registration = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val data = snapshot.data ?: emptyMap<String, Any>()
                trySend(
                    UserAccount(
                        id = snapshot.id,
                        username = data["username"] as? String ?: "",
                        realName = data["realName"] as? String ?: "",
                        phoneNumber = data["phoneNumber"] as? String ?: "",
                        jazzCashNumber = data["jazzCashNumber"] as? String ?: "",
                        jazzCashTitle = data["jazzCashTitle"] as? String ?: "",
                        referralCount = (data["referralCount"] as? Number)?.toInt() ?: 0,
                        generalPendingAmount = (data["generalPendingAmount"] as? Number)?.toDouble() ?: 0.0,
                        generalPaidAmount = (data["generalPaidAmount"] as? Number)?.toDouble() ?: 0.0,
                        bonusPaidDealIds = (data["bonusPaidDealIds"] as? List<*>)
                            ?.mapNotNull { it as? String } ?: emptyList()
                    )
                )
            }

        awaitClose { registration.remove() }
    }
}
