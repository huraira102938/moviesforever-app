package com.moviesforever.app.data.model

data class PricingSettings(
    val standardPrice: Double = 0.0,
    val referralPrice: Double = 0.0,
    val referralPayout: Double = 0.0,
    // Free-text note fully controlled by the admin panel's Pricing Settings
    // page (`settings/pricing`.note). The app never hardcodes offer/promo
    // copy -- it just renders whatever the admin has written here, wherever
    // this note is meant to show (pricing card, lock screen, home screen).
    val note: String = ""
)
