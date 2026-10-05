package com.moviesforever.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.data.model.TmdbCategory
import com.moviesforever.app.data.model.TmdbMovie
import com.moviesforever.app.data.model.toMovie
import com.moviesforever.app.data.remote.TmdbConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import kotlin.random.Random
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Downloads the 8 category JSON files from Cloudflare, caches them in the app's private storage,
 * and exposes them as Movie lists per category.
 *
 * When does the app download?
 *  - Each saved file remembers the server's ETag. On every app launch the app asks the server
 *    "has this file changed?" (If-None-Match). Unchanged -> tiny 304 answer, nothing downloaded,
 *    the saved copy is used. Changed (a new JSON was uploaded from the admin panel) -> the new
 *    file is downloaded, saved, and shown straight away.
 *  - First launch / no saved copy -> the file is downloaded.
 *  - No internet / slow / bad JSON -> the saved copy is used (or an empty shelf). Never crashes.
 *
 * Session behaviour (one "session" = one app process, from open until the app is closed):
 *  - every category is shuffled ONCE per session with [sessionSeed]
 *  - each category is published ONCE per session (write-once), so going to Search / Detail and
 *    back never re-orders or swaps a shelf
 *  - closing the app and opening it again gives a new random order
 */
@Singleton
class TmdbRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    /** Used when a saved copy exists: the "did it change?" check must never hold Home for long. */
    private val quickClient = client.newBuilder().callTimeout(6, TimeUnit.SECONDS).build()
    private val gson = Gson()

    private val _shelves = MutableStateFlow<Map<TmdbCategory, List<Movie>>>(emptyMap())
    val shelves: StateFlow<Map<TmdbCategory, List<Movie>>> = _shelves.asStateFlow()

    private val started = AtomicBoolean(false)

    /** New value every time the app process starts -> new random order on every app launch. */
    private val sessionSeed: Long = System.nanoTime()

    private sealed class Fetch {
        class Fresh(val movies: List<Movie>) : Fetch()
        object NotModified : Fetch()
        object Failed : Fetch()
    }

    suspend fun load() {
        if (!started.compareAndSet(false, true)) return
        val anyFailed = AtomicBoolean(false)
        withContext(Dispatchers.IO) {
            coroutineScope {
                TmdbCategory.values().map { cat ->
                    async {
                        val cached = readCache(cat)
                        // With a saved copy: ask the server if the file changed (fast 304 when not).
                        // Without one: download it.
                        val result = fetch(cat, if (cached != null) quickClient else client, cached != null)
                        when {
                            result is Fetch.Fresh -> publishOnce(cat, result.movies)
                            cached != null -> publishOnce(cat, cached)
                            else -> anyFailed.set(true)
                        }
                    }
                }.awaitAll()
            }
        }
        if (anyFailed.get()) started.set(false) // allow a retry next time the home screen opens
    }

    /**
     * Write-once per session: shuffles with the session seed and publishes only if this category
     * has not been published yet. Later calls for the same category are ignored.
     */
    private fun publishOnce(cat: TmdbCategory, list: List<Movie>) {
        val shuffled = list.shuffled(Random(sessionSeed + cat.ordinal))
        _shelves.update { current -> if (cat in current) current else current + (cat to shuffled) }
    }

    /**
     * Asks the server for the file. If [haveValidCache] is true the saved ETag is sent, so the
     * server answers 304 (nothing to download) unless a new JSON was uploaded.
     */
    private fun fetch(cat: TmdbCategory, http: OkHttpClient, haveValidCache: Boolean): Fetch {
        val builder = Request.Builder()
            .url(TmdbConfig.BASE_URL + cat.fileName)
            .header("Cache-Control", "no-cache")
        if (haveValidCache) readEtag(cat)?.let { builder.header("If-None-Match", it) }

        return try {
            http.newCall(builder.build()).execute().use { response ->
                if (response.code == 304) return@use Fetch.NotModified
                if (!response.isSuccessful) return@use Fetch.Failed
                val text = response.body?.string() ?: return@use Fetch.Failed
                val movies = parse(text, cat) ?: return@use Fetch.Failed
                // Save the file first; only remember its ETag if the file was really saved.
                writeEtag(cat, if (writeCache(cat, text)) response.header("ETag") else null)
                Fetch.Fresh(movies)
            }
        } catch (e: Exception) {
            Fetch.Failed
        }
    }

    private fun parse(text: String, cat: TmdbCategory): List<Movie>? {
        return try {
            gson.fromJson(text, Array<TmdbMovie>::class.java)
                ?.filter { it.id > 0 && it.title.isNotBlank() }
                ?.map { it.toMovie(cat) }
        } catch (e: Exception) {
            null
        }
    }

    private fun cacheFile(cat: TmdbCategory): File =
        File(File(context.filesDir, "tmdb_cache").apply { mkdirs() }, cat.fileName)

    private fun readCache(cat: TmdbCategory): List<Movie>? {
        return try {
            val f = cacheFile(cat)
            if (!f.exists()) null else parse(f.readText(), cat)
        } catch (e: Exception) {
            null
        }
    }

    /** @return true if the file was saved. */
    private fun writeCache(cat: TmdbCategory, text: String): Boolean {
        return try {
            val f = cacheFile(cat)
            val tmp = File(f.parentFile, cat.fileName + ".tmp")
            tmp.writeText(text)
            tmp.renameTo(f)
        } catch (_: Exception) {
            false
        }
    }

    private fun etagFile(cat: TmdbCategory): File =
        File(File(context.filesDir, "tmdb_cache").apply { mkdirs() }, cat.fileName + ".etag")

    private fun readEtag(cat: TmdbCategory): String? {
        return try {
            val f = etagFile(cat)
            if (f.exists()) f.readText().trim().ifEmpty { null } else null
        } catch (_: Exception) {
            null
        }
    }

    /** null (or blank) removes the saved ETag, so the next launch simply downloads again. */
    private fun writeEtag(cat: TmdbCategory, etag: String?) {
        try {
            val f = etagFile(cat)
            if (etag.isNullOrBlank()) f.delete() else f.writeText(etag)
        } catch (_: Exception) {
        }
    }
}
