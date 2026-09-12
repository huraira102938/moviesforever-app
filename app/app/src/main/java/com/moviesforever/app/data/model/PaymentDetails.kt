package com.moviesforever.app.data.model

/**
 * Bank transfer details shown to users on the checkout screen. Managed from
 * the admin panel (Firestore `settings/payment-details` doc) so they can be
 * changed at any time with no new app build or release required.
 */
data class PaymentDetails(
    val bankName: String = "",
    val accountTitle: String = "",
    val accountNumber: String = ""
)
