package com.moviesforever.app.ui.navigation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.moviesforever.app.data.model.Banner

/**
 * Runs what the admin chose for a banner: open the movie, a website, a WhatsApp chat or a
 * WhatsApp channel. [onOpenMovie] is called only for movie banners.
 */
fun handleBannerClick(context: Context, banner: Banner, onOpenMovie: (String) -> Unit) {
    when (banner.resolvedType()) {
        "movie" -> banner.linkedMovieId?.takeIf { it.isNotBlank() }?.let(onOpenMovie)

        "link", "channel" -> {
            val raw = banner.actionValue.trim()
            if (raw.isNotEmpty()) {
                val url = if (raw.startsWith("http://", true) || raw.startsWith("https://", true)) raw else "https://$raw"
                openExternal(context, url)
            }
        }

        "whatsapp" -> {
            val digits = banner.actionValue.filter { it.isDigit() }
            if (digits.isNotEmpty()) openExternal(context, "https://wa.me/$digits")
        }
    }
}

private fun openExternal(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "لنک نہیں کھل سکا", Toast.LENGTH_SHORT).show()
    }
}
