package com.moviesforever.app.data.model

/**
 * Full account record for the signed-in user, sourced from the `users/{id}`
 * Firestore document (the same doc the admin panel's PaymentVerify /
 * UserManagement pages write to). This is intentionally separate from
 * [UnlockInfo] (which is the fast, local-only DataStore signal used purely
 * for unlock/routing checks) -- this model backs the richer Profile and
 * Referral screens and is allowed to depend on network/Firestore.
 */
data class UserAccount(
    val id: String = "",
    val username: String = "",
    val realName: String = "",
    val phoneNumber: String = "",
    val jazzCashNumber: String = "",
    val jazzCashTitle: String = "",
    // Total referrals credited to this user (kept on the user doc by the
    // admin panel today).
    val referralCount: Int = 0,
    // General (non-bonus) referral payouts, tracked separately from the
    // bonus-deal payouts below since they follow their own admin workflow.
    val generalPendingAmount: Double = 0.0,
    val generalPaidAmount: Double = 0.0,
    // IDs of bonus-deals (see [BonusDeal]) this user has already been paid
    // out for. Presence of a deal's id here means "settled" -- the app uses
    // this to stop showing the congratulations box for that deal.
    val bonusPaidDealIds: List<String> = emptyList()
)
