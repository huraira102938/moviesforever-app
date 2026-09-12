package com.moviesforever.app.data.model

/**
 * Mirrors a doc in the admin panel's `notifications` collection, written
 * from the admin's Notifications page (`{ id, text, targets, createdAt }`).
 * This is display-only in the app -- there is no push/system notification
 * delivery here, just an in-app feed the user can open and read. `targets`
 * holds a subset of "free" / "paid" / "paused", matching the same three
 * groups the admin panel targets when sending.
 */
data class AppNotification(
    val id: String = "",
    val text: String = "",
    val targets: List<String> = emptyList(),
    val createdAt: String = ""
)
