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
    // moment the admin verifies a payment -- referralCount, payment details,
    // etc should update in the app without a force-close/reopen.
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
                        paymentMethod = data["paymentMethod"] as? String ?: "",
                        paymentNumber = data["paymentNumber"] as? String ?: "",
                        accountTitle = data["accountTitle"] as? String ?: "",
                        jazzCashNumber = data["jazzCashNumber"] as? String ?: "",
                        jazzCashTitle = data["jazzCashTitle"] as? String ?: "",
                        referralCount = (data["referralCount"] as? Number)?.toInt() ?: 0,
                        paused = data["paused"] as? Boolean ?: false,
                        pauseUserNote = data["pauseUserNote"] as? String ?: ""
                    )
                )
            }

        awaitClose { registration.remove() }
    }
}
