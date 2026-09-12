package com.moviesforever.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.ContactDetails
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactDetailsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ContactDetailsRepository {

    // Live listener (not a one-shot get()) so the support WhatsApp number
    // and the group/channel link update in the app right away when the
    // admin changes them, without a new release.
    override fun observeContactDetails(): Flow<ContactDetails> = callbackFlow {
        val registration = firestore.collection("settings").document("contact")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(ContactDetails())
                    return@addSnapshotListener
                }
                val data = snapshot.data ?: emptyMap<String, Any>()
                trySend(
                    ContactDetails(
                        whatsappNumber = data["whatsappNumber"] as? String ?: "",
                        groupTitle = data["groupTitle"] as? String ?: "",
                        groupLink = data["groupLink"] as? String ?: ""
                    )
                )
            }

        awaitClose { registration.remove() }
    }
}
