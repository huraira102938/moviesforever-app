package com.moviesforever.app.data.model

/**
 * WhatsApp contact details shown to users. Managed from the admin panel
 * (Firestore `settings/contact` doc): [whatsappNumber] is where users send
 * their payment screenshot, [groupTitle]/[groupLink] are the WhatsApp
 * group/channel invite shown on the Settings screen for every user, free or
 * paid.
 */
data class ContactDetails(
    val whatsappNumber: String = "",
    val groupTitle: String = "",
    val groupLink: String = ""
)
