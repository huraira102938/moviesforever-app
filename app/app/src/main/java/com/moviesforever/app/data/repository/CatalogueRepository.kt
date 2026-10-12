package com.moviesforever.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.moviesforever.app.data.model.Banner
import com.moviesforever.app.data.model.CatalogueFile
import com.moviesforever.app.data.model.CatalogueState
import com.moviesforever.app.data.model.Shelf
import com.moviesforever.app.data.remote.CatalogueConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Loads the whole catalogue from ONE file (`movies.json`, Cloudflare) and keeps a copy on the phone.
 * The app never reads movies from Firestore.
 *
 * When does the app download?
 *  - The saved copy is shown immediately on launch (no waiting for the network).
 *  - In the background the app then asks the server "has the file changed?" using the saved ETag
 *    (If-None-Match). Unchanged -> a tiny 304 answer, nothing is downloaded. Changed (the admin
 *    pressed "Update JSON") -> the new file is downloaded once, saved on the phone, and shown.
 *  - First launch / no saved copy -> the file is downloaded.
 *  - No internet / slow / broken JSON -> the saved copy keeps being used. Never crashes.
 *
 * Within one app launch each shelf is shuffled with the same [sessionSeed], so the order does not
 * change while the app is open; closing and re-opening it gives a new random order.
 */
@Singleton
class CatalogueRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /** Used for the "did it change?" check when a saved copy exists: it must never hang for long. */
    private val quickClient = client.newBuilder().callTimeout(8, TimeUnit.SECONDS).build()

    private val gson = Gson()

    private val _state = MutableStateFlow(CatalogueState())
    val state: StateFlow<CatalogueState> = _state.asStateFlow()

    private val started = AtomicBoolean(false)

    private companion object {
        const val MAX_BANNERS = 5
    }

    /** New value every time the app process starts -> new random order on every app launch. */
    private val sessionSeed: Long = System.nanoTime()

    private sealed class Fetch {
        class Fresh(val file: CatalogueFile) : Fetch()
        object NotModified : Fetch()
        object Failed : Fetch()
    }

    suspend fun load() {
        if (!started.compareAndSet(false, true)) return
        withContext(Dispatchers.IO) {
            val cached = readCache()
            if (cached != null) publish(cached)

            when (val result = fetch(if (cached != null) quickClient else client, cached != null)) {
                is Fetch.Fresh -> publish(result.file)
                Fetch.NotModified -> Unit
                Fetch.Failed -> if (cached == null) started.set(false) // retry next time Home opens
            }
        }
    }

    /** Groups the file into shelves (one per category, in the admin's category order). */
    private fun publish(file: CatalogueFile) {
        val visible = file.movies.filter { !it.paused }
        val byId = visible.associateBy { it.id }

        val shelves = file.categories
            .sortedBy { it.order }
            .mapNotNull { cat ->
                val list = visible.filter { it.category == cat.id }
                if (list.isEmpty()) null
                else Shelf(
                    categoryId = cat.id,
                    title = cat.name,
                    movies = list.shuffled(Random(sessionSeed + cat.id.hashCode()))
                )
            }

        _state.value = CatalogueState(
            shelves = shelves,
            // All trending movies, in a different scattered order on every app launch.
            trending = file.trending.mapNotNull { byId[it] }.shuffled(Random(sessionSeed + 7919)),
            // Shuffled once per app launch, so Search shows a new scattered order every time the app is opened.
            all = visible.shuffled(Random(sessionSeed + 31)),
            banners = file.banners,
            loaded = true
        )
    }

    /**
     * Asks the server for the file. If [haveSavedCopy] is true the saved ETag is sent, so the
     * server answers 304 (nothing to download) unless a new JSON was uploaded.
     */
    private fun fetch(http: OkHttpClient, haveSavedCopy: Boolean): Fetch {
        val builder = Request.Builder()
            .url(CatalogueConfig.BASE_URL + CatalogueConfig.FILE_NAME)
            .header("Cache-Control", "no-cache")
        if (haveSavedCopy) readEtag()?.let { builder.header("If-None-Match", it) }

        return try {
            http.newCall(builder.build()).execute().use { response ->
                if (response.code == 304) return@use Fetch.NotModified
                if (!response.isSuccessful) return@use Fetch.Failed
                val text = response.body?.string() ?: return@use Fetch.Failed
                val file = parse(text) ?: return@use Fetch.Failed
                // Save the file first; only remember its ETag if the file was really saved.
                writeEtag(if (writeCache(text)) response.header("ETag") else null)
                Fetch.Fresh(file)
            }
        } catch (e: Exception) {
            Fetch.Failed
        }
    }

    @Suppress("USELESS_ELVIS", "UNNECESSARY_SAFE_CALL")
    private fun parse(text: String): CatalogueFile? {
        return try {
            val raw = gson.fromJson(text, CatalogueFile::class.java) ?: return null
            // Gson can leave a list null if the JSON contains an explicit null; guard every list.
            val movies = (raw.movies ?: emptyList()).filter { it.id.isNotBlank() && it.title.isNotBlank() }
            // A file with no movies at all is treated as broken so it can never wipe a good saved copy.
            if (movies.isEmpty()) return null
            raw.copy(
                categories = (raw.categories ?: emptyList()).filter { it.id.isNotBlank() },
                trending = raw.trending ?: emptyList(),
                banners = (raw.banners ?: emptyList())
                    .filter { it.imageUrl?.isNotBlank() == true }
                    .map { it.copy(id = it.id ?: "", imageKey = it.imageKey ?: "", actionType = it.actionType ?: "", actionValue = it.actionValue ?: "") }
                    .sortedBy { it.order }
                    .take(MAX_BANNERS),
                movies = movies
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun dir(): File = File(context.filesDir, "catalogue_cache").apply { mkdirs() }

    private fun cacheFile(): File = File(dir(), CatalogueConfig.FILE_NAME)

    private fun etagFile(): File = File(dir(), CatalogueConfig.FILE_NAME + ".etag")

    private fun readCache(): CatalogueFile? {
        return try {
            val f = cacheFile()
            if (!f.exists()) null else parse(f.readText())
        } catch (e: Exception) {
            null
        }
    }

    /** @return true if the file was saved. */
    private fun writeCache(text: String): Boolean {
        return try {
            val f = cacheFile()
            val tmp = File(f.parentFile, CatalogueConfig.FILE_NAME + ".tmp")
            tmp.writeText(text)
            tmp.renameTo(f)
        } catch (_: Exception) {
            false
        }
    }

    private fun readEtag(): String? {
        return try {
            val f = etagFile()
            if (f.exists()) f.readText().trim().ifEmpty { null } else null
        } catch (_: Exception) {
            null
        }
    }

    /** null (or blank) removes the saved ETag, so the next launch simply downloads again. */
    private fun writeEtag(etag: String?) {
        try {
            val f = etagFile()
            if (etag.isNullOrBlank()) f.delete() else f.writeText(etag)
        } catch (_: Exception) {
        }
    }
}