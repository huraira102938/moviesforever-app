package com.moviesforever.app.data.model

/**
 * Mirrors a doc in the admin panel's `notifications` collection, written
 * from the admin's Notifications page (`{ id, text, targets, createdAt }`).
 * The app shows these as an in-app feed, and the Worker also pushes each one via
 * FCM topics (`pushStatus` tracks that; the app ignores it). `targets`
 * holds a subset of "free" / "paid" / "paused", matching the same three
 * groups the admin panel targets when sending.
 */
data class AppNotification(
    val id: String = "",
    val text: String = "",
    val targets: List<String> = emptyList(),
    val createdAt: String = ""
)
