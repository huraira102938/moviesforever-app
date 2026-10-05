package com.moviesforever.app.push

/**
 * FCM topics, one per audience group. Every device is subscribed to exactly ONE of
 * these at a time, matching AppUiState.myNotificationTarget ("free" / "paid" / "paused").
 *
 * The Cloudflare Worker sends to a topic (or a condition combining topics) so a single
 * FCM request reaches every device in the group -- no token lists, no per-user sends.
 *
 * Keep these strings in sync with TOPIC_PREFIX in worker/src/index.js.
 */
object PushTopics {
    const val FREE = "mf_free"
    const val PAID = "mf_paid"
    const val PAUSED = "mf_paused"

    val ALL = listOf(FREE, PAID, PAUSED)

    fun forTarget(target: String): String? = when (target) {
        "free" -> FREE
        "paid" -> PAID
        "paused" -> PAUSED
        else -> null
    }
}
