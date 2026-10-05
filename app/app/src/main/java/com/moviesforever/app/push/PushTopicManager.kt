package com.moviesforever.app.push

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "MF_Push"
private const val PREFS = "push_topics"
private const val KEY_TOPIC = "subscribed_topic"

/**
 * Keeps this device subscribed to the single FCM topic for the user's current group.
 *
 * - Safe to call repeatedly: it does nothing if we're already on the right topic.
 * - Only records success after the subscribe AND the unsubscribes all succeeded, so a
 *   failure (offline, or a device with no Google Play Services such as many TV boxes)
 *   is simply retried the next time the app runs. It never throws.
 */
@Singleton
class PushTopicManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun sync(target: String) {
        val topic = PushTopics.forTarget(target) ?: return
        if (prefs.getString(KEY_TOPIC, null) == topic) return

        val messaging = try {
            FirebaseMessaging.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FCM unavailable on this device; staying on in-app feed only", e)
            return
        }

        val tasks = mutableListOf<Task<Void>>(messaging.subscribeToTopic(topic))
        PushTopics.ALL.filter { it != topic }.forEach { tasks += messaging.unsubscribeFromTopic(it) }

        Tasks.whenAll(tasks).addOnCompleteListener { result ->
            if (result.isSuccessful) {
                prefs.edit().putString(KEY_TOPIC, topic).apply()
                Log.d(TAG, "Subscribed to $topic")
            } else {
                Log.w(TAG, "Topic sync to $topic failed; will retry next launch", result.exception)
            }
        }
    }
}
