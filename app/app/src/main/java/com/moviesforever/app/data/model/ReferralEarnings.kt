package com.moviesforever.app.data.model

/**
 * A referrer's pending/paid totals, computed the exact same way the admin
 * panel's User Management page computes them: by summing `referrerPendingAmount`
 * across `transactions` docs where `referralUsername == this user's username`,
 * split by `status` ('pending' vs 'paid'). There is no running total stored on
 * the user doc -- it's always derived from the transaction records, so the
 * app derives it the same way instead of trusting a stale field.
 */
data class ReferralEarnings(
    val pendingAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val totalReceived: Double = 0.0,
    val referralTransactionCount: Int = 0
)
