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
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TEST FEATURE: downloads the 8 category JSON files from Cloudflare, caches them in the app's
 * private storage, and exposes them as Movie lists per category.
 *  - cached copy is shown immediately, fresh copy replaces it when the download finishes
 *  - no internet / bad JSON -> keeps the cached copy (or an empty shelf). Never crashes.
 */
@Singleton
class TmdbRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    private val _shelves = MutableStateFlow<Map<TmdbCategory, List<Movie>>>(emptyMap())
    val shelves: StateFlow<Map<TmdbCategory, List<Movie>>> = _shelves.asStateFlow()

    private val started = AtomicBoolean(false)

    suspend fun load() {
        if (!started.compareAndSet(false, true)) return
        var anyFailed = false
        withContext(Dispatchers.IO) {
            // 1) cached data first (instant)
            TmdbCategory.values().forEach { cat ->
                readCache(cat)?.let { publish(cat, it) }
            }
            // 2) refresh all files in parallel
            coroutineScope {
                TmdbCategory.values().map { cat ->
                    async {
                        val fresh = try { download(cat) } catch (e: Exception) { null }
                        if (fresh != null) publish(cat, fresh) else anyFailed = true
                    }
                }.awaitAll()
            }
        }
        if (anyFailed) started.set(false) // allow a retry next time the home screen opens
    }

    private fun publish(cat: TmdbCategory, list: List<Movie>) {
        _shelves.update { it + (cat to list) }
    }

    private fun download(cat: TmdbCategory): List<Movie>? {
        val request = Request.Builder()
            .url(TmdbConfig.BASE_URL + cat.fileName)
            .header("Cache-Control", "no-cache")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val text = response.body?.string() ?: return null
            val movies = parse(text, cat) ?: return null
            writeCache(cat, text)
            return movies
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

    private fun writeCache(cat: TmdbCategory, text: String) {
        try {
            val f = cacheFile(cat)
            val tmp = File(f.parentFile, cat.fileName + ".tmp")
            tmp.writeText(text)
            tmp.renameTo(f)
        } catch (_: Exception) {
        }
    }
}
