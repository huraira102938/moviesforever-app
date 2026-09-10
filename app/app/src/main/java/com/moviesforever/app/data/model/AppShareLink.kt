package com.moviesforever.app.data.model

/**
 * One entry from the `app-sharing` Firestore collection, managed from the
 * admin panel's "App Sharing" page. Field names match the admin's `AppLink`
 * interface exactly: `title`, `apkUrl`, `version` (optional), `createdAt`.
 */
data class AppShareLink(
    val id: String = "",
    val title: String = "",
    val apkUrl: String = "",
    val version: String = "",
    val createdAt: String = ""
)

/**
 * Builds the WhatsApp share text. Deliberately just three things, nothing
 * else added by the app: the admin-configured title (which the admin writes
 * to already include the pitch + "use my referral code" instruction), the
 * user's own referral username, and the admin-configured download link.
 */
fun AppShareLink.buildShareMessage(username: String?): String {
    return listOfNotNull(
        title.takeIf { it.isNotBlank() },
        username?.takeIf { it.isNotBlank() },
        apkUrl.takeIf { it.isNotBlank() }
    ).joinToString("\n")
}
