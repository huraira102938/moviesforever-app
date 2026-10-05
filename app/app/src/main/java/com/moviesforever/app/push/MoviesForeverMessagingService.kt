package com.moviesforever.app.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.moviesforever.app.R

/**
 * Receives pushes sent by the Worker.
 *
 * When the app is in the background or closed, Android shows the push itself (using the
 * default icon/channel set in the manifest) and this is NOT called. It is called when the
 * app is in the foreground, where we have to draw the notification ourselves.
 *
 * Topic subscriptions survive token refreshes, so onNewToken needs no work here.
 */
class MoviesForeverMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(R.string.app_name)
        val body = message.notification?.body
            ?: message.data["body"]
            ?: return
        PushNotifier.show(this, title, body)
    }
}
