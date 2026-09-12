package com.moviesforever.app.data.model

/**
 * Full account record for the signed-in user, sourced from the `users/{id}`
 * Firestore document (the same doc the admin panel's PaymentVerify /
 * UserManagement pages write to). Separate from [UnlockInfo] (the fast,
 * local-only DataStore signal used purely for unlock/routing checks) --
 * this backs the richer Profile and Referral screens and depends on network.
 *
 * The admin supports two payout methods (JazzCash or EasyPaisa), stored
 * generically as [paymentMethod]/[paymentNumber]/[accountTitle]. [jazzCashNumber]
 * /[jazzCashTitle] are also still written by the admin (only populated when
 * paymentMethod == "jazzcash") -- kept here as a fallback for older records.
 *
 * NOTE: pending/paid referral payout amounts are NOT stored on this doc --
 * the admin computes them on the fly from the `transactions` collection, so
 * the app does the same. See [ReferralEarnings] / ReferralEarningsRepository.
 */
data class UserAccount(
    val id: String = "",
    val username: String = "",
    val realName: String = "",
    val phoneNumber: String = "",
    val paymentMethod: String = "",
    val paymentNumber: String = "",
    val accountTitle: String = "",
    val jazzCashNumber: String = "",
    val jazzCashTitle: String = "",
    val referralCount: Int = 0,
    /**
     * Set by the admin panel's User Management "Pause" action (`users/{id}.paused`).
     * While true, the app must block all streaming/playback for this user and show
     * only the [pauseUserNote] message -- see PausedScreen.
     */
    val paused: Boolean = false,
    /** User-facing note the admin wrote when pausing (`users/{id}.pauseUserNote`). */
    val pauseUserNote: String = ""
) {
    /** Display-friendly payout number, preferring the generic field. */
    val effectivePaymentNumber: String
        get() = paymentNumber.ifBlank { jazzCashNumber }

    /** Display-friendly payout account title, preferring the generic field. */
    val effectiveAccountTitle: String
        get() = accountTitle.ifBlank { jazzCashTitle }

    /** "JazzCash" / "EasyPaisa" for display, falling back sensibly. */
    val paymentMethodLabel: String
        get() = when {
            paymentMethod.equals("jazzcash", ignoreCase = true) -> "JazzCash"
            paymentMethod.equals("easypaisa", ignoreCase = true) -> "EasyPaisa"
            jazzCashNumber.isNotBlank() -> "JazzCash"
            else -> "Payment"
        }
}
