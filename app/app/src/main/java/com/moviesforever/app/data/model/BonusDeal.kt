package com.moviesforever.app.data.model

/**
 * A limited-time referral bonus campaign, configured by the admin panel in
 * the `bonus-deals` Firestore collection. Example: "Refer 10 people and get
 * an extra PKR 500, valid until 30 Sept."
 *
 * IMPORTANT (matches the admin schema): [createdAt] and [validUntil] MUST be
 * full ISO-8601 instant strings in the exact format produced by JavaScript's
 * `new Date().toISOString()` (e.g. "2026-09-30T23:59:59.999Z"). This is the
 * same format already used for `referral-claims.timestamp`, which lets the
 * app compare them with plain string comparison (no date-parsing library
 * needed) -- see BonusRepositoryImpl.
 *
 * Only one deal should be marked `active = true` at a time; if more than one
 * is active, the app picks the most recently created one that hasn't expired.
 */
data class BonusDeal(
    val id: String = "",
    val tagline: String = "",
    val bonusAmount: Double = 0.0,
    val unlocksRequired: Int = 0,
    val validUntil: String = "",
    val active: Boolean = false,
    val createdAt: String = ""
)
