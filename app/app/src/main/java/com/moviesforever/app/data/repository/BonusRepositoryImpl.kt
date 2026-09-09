package com.moviesforever.app.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.model.BonusDeal
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BonusRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BonusRepository {

    // Matches the exact format JavaScript's `new Date().toISOString()`
    // produces (e.g. "2026-09-30T23:59:59.999Z"), which is what the admin
    // panel writes for both bonus-deals.validUntil/createdAt and
    // referral-claims.timestamp. Only used to check "has this deal expired
    // yet?" against the device clock -- the deal-vs-claim window comparison
    // below uses plain string comparison instead, since both sides are
    // already in this same sortable format.
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun parseIsoMillis(iso: String): Long? = try {
        isoFormat.parse(iso)?.time
    } catch (e: Exception) {
        null
    }

    private fun toBonusDeal(doc: DocumentSnapshot): BonusDeal? {
        val data = doc.data ?: return null
        return BonusDeal(
            id = doc.id,
            tagline = data["tagline"] as? String ?: "",
            bonusAmount = (data["bonusAmount"] as? Number)?.toDouble() ?: 0.0,
            unlocksRequired = (data["unlocksRequired"] as? Number)?.toInt() ?: 0,
            validUntil = data["validUntil"] as? String ?: "",
            active = data["active"] as? Boolean ?: false,
            createdAt = data["createdAt"] as? String ?: ""
        )
    }

    /** The newest active, not-yet-expired deal, or null if none qualifies. */
    private fun observeCurrentDeal(): Flow<BonusDeal?> = callbackFlow {
        val registration = firestore.collection("bonus-deals")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val now = System.currentTimeMillis()
                val deal = snapshot.documents
                    .mapNotNull { toBonusDeal(it) }
                    .filter { deal ->
                        // Fail open on a malformed/missing validUntil so a
                        // typo on the admin side doesn't just silently hide
                        // the whole deal -- it'll just look like it never
                        // expires until fixed.
                        val expiryMillis = parseIsoMillis(deal.validUntil)
                        expiryMillis == null || expiryMillis >= now
                    }
                    .maxByOrNull { it.createdAt }
                trySend(deal)
            }
        awaitClose { registration.remove() }
    }

    /** Count of this user's referral-claims that fall inside [deal]'s window. */
    private fun observeProgress(username: String, deal: BonusDeal): Flow<Int> = callbackFlow {
        val registration = firestore.collection("referral-claims")
            .whereEqualTo("referrerUsername", username)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(0)
                    return@addSnapshotListener
                }
                val count = snapshot.documents.count { doc ->
                    val timestamp = doc.getString("timestamp")
                    // Plain string comparison is safe here: both this
                    // timestamp and deal.createdAt/validUntil are the same
                    // fixed-width ISO-8601 format, so lexical order matches
                    // chronological order.
                    !timestamp.isNullOrBlank() &&
                        timestamp >= deal.createdAt &&
                        timestamp <= deal.validUntil
                }
                trySend(count)
            }
        awaitClose { registration.remove() }
    }

    override fun observeActiveDealProgress(username: String): Flow<Pair<BonusDeal, Int>?> {
        if (username.isBlank()) return flowOf(null)
        return observeCurrentDeal()
            .distinctUntilChanged()
            .flatMapLatest { deal ->
                if (deal == null) {
                    flowOf(null)
                } else {
                    observeProgress(username, deal).map { achieved -> deal to achieved }
                }
            }
    }
}
