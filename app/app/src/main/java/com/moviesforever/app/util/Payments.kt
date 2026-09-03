package com.moviesforever.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object Payments {
    // WhatsApp number in international format (no +, no spaces).
    // Displays as "+92 0326 4304455" -> wa.me/923264304455
    const val SUPPORT_WHATSAPP = "923264304455"
    const val SUPPORT_DISPLAY = "+92 0326 4304455"

    fun openSupportWhatsApp(context: Context, referralUsername: String? = null) {
        val message = StringBuilder().apply {
            append("Hi MoviesForever! I would like to unlock the lifetime access.")
            val referralPart = referralUsername?.takeIf { it.isNotBlank() }?.let { "@$it" }
            if (referralPart != null) {
                append(" Referral: $referralPart")
            }
        }.toString()
        val uri = Uri.parse("https://wa.me/$SUPPORT_WHATSAPP?text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }
}
