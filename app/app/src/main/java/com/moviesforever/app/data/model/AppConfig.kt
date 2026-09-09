package com.moviesforever.app.data.model

/**
 * Misc app-wide settings managed from the admin panel, sourced from the
 * `settings/app` Firestore document (sibling to the existing
 * `settings/pricing` doc used by [PricingSettings]).
 */
data class AppConfig(
    val apkShareUrl: String = ""
)
