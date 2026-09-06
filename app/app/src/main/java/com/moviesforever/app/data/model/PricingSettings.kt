package com.moviesforever.app.data.model

data class PricingSettings(
    val standardPrice: Double = 0.0,
    val referralPrice: Double = 0.0,
    val referralPayout: Double = 0.0,
    // Where users should send payment. Defaults to the real details until
    // the admin panel gets a UI to manage this in Firestore - once that
    // field exists there, it will override these defaults automatically.
    val jazzcashNumber: String = "03164304455",
    val jazzcashTitle: String = "Abu Huraira"
)
