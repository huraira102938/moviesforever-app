package com.moviesforever.app.data.model

/**
 * A home-screen banner. What happens when it is tapped is set in the admin panel:
 *   actionType = "movie"    -> open the movie  (linkedMovieId)
 *   actionType = "link"     -> open a website  (actionValue = URL)
 *   actionType = "whatsapp" -> open a WhatsApp chat (actionValue = number with country code)
 *   actionType = "channel"  -> open a WhatsApp channel (actionValue = channel link)
 *   actionType = "" / "none" -> not tappable
 *
 * Banners saved before these fields existed have no actionType: [resolvedType] treats
 * "clickable + linkedMovieId" as a movie banner, so old banners keep working.
 */
data class Banner(
    val id: String = "",
    val imageKey: String = "",
    val imageUrl: String = "",
    val clickable: Boolean = false,
    val linkedMovieId: String? = null,
    val order: Int = 0,
    val actionType: String = "",
    val actionValue: String = ""
) {
    fun resolvedType(): String = when {
        actionType.isNotBlank() -> actionType
        clickable && !linkedMovieId.isNullOrBlank() -> "movie"
        else -> "none"
    }

    fun isTappable(): Boolean = when (resolvedType()) {
        "movie" -> !linkedMovieId.isNullOrBlank()
        "link", "whatsapp", "channel" -> actionValue.isNotBlank()
        else -> false
    }
}
