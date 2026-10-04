package com.moviesforever.app.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri

/** TEST FEATURE settings. */
object TmdbConfig {
    /**
     * Base URL where the 8 JSON files are uploaded (Cloudflare). Keep the trailing slash.
     * The app requests BASE_URL + "bollywood.json", "hollywood.json", ... etc.
     * >>> CHANGE THIS if your files live somewhere else. <<<
     */
    const val BASE_URL = "https://videos.moviesforever.online/"
}

/** Opens a link in YouTube / the browser (used for trailers). Does nothing if the link is blank. */
fun openExternalLink(context: Context, url: String) {
    if (url.isBlank()) return
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: Exception) {
        // No app can open the link; ignore.
    }
}
