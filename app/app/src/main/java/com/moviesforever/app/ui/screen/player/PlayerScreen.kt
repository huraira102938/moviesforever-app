package com.moviesforever.app.ui.screen.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionOff
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.moviesforever.app.BuildConfig
import com.moviesforever.app.download.DownloadUtil
import com.moviesforever.app.ui.theme.Black
import com.moviesforever.app.ui.theme.DarkElevated
import com.moviesforever.app.ui.theme.DarkSurface
import com.moviesforever.app.ui.theme.Gold
import com.moviesforever.app.ui.theme.TextPrimary
import java.util.Locale
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

private const val TAG = "PlayerScreen"


fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

data class SubtitleTrackInfo(
    val groupIndex: Int,
    val trackIndex: Int,
    val language: String,
    val label: String
)

data class AudioTrackInfo(
    val groupIndex: Int,
    val trackIndex: Int,
    val language: String,
    val label: String
)

data class ResizeModeOption(
    val mode: Int,
    val label: String
)

@OptIn(UnstableApi::class)
private val resizeModeOptions = listOf(
    ResizeModeOption(AspectRatioFrameLayout.RESIZE_MODE_FIT, "Fit (show full video)"),
    ResizeModeOption(AspectRatioFrameLayout.RESIZE_MODE_ZOOM, "Zoom (crop to fill)"),
    ResizeModeOption(AspectRatioFrameLayout.RESIZE_MODE_FILL, "Stretch to fill"),
    ResizeModeOption(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH, "Fixed width"),
    ResizeModeOption(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT, "Fixed height")
)

/**
 * Maps ExoPlayer's PlaybackException error codes to short, easy Urdu messages.
 * The raw error code + name is still shown underneath so a problem report can say exactly
 * which category the failure fell into.
 */
fun getPlaybackErrorMessage(error: PlaybackException): String {
    return when (error.errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
        PlaybackException.ERROR_CODE_TIMEOUT ->
            "انٹرنیٹ کا مسئلہ ہے۔ انٹرنیٹ چیک کریں اور دوبارہ کوشش کریں۔"
        PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
        PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
        PlaybackException.ERROR_CODE_IO_NO_PERMISSION,
        PlaybackException.ERROR_CODE_IO_UNSPECIFIED ->
            "یہ ویڈیو ابھی نہیں کھل رہی۔ تھوڑی دیر بعد دوبارہ کوشش کریں۔"
        PlaybackException.ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED ->
            "ویڈیو کا لنک محفوظ نہیں ہے۔"
        PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
        PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED,
        PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
        PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED ->
            "یہ ویڈیو فائل خراب ہے یا سپورٹ نہیں ہوتی۔"
        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
        PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED,
        PlaybackException.ERROR_CODE_DECODING_FAILED ->
            "آپ کے فون پر یہ ویڈیو نہیں چل سکتی۔"
        PlaybackException.ERROR_CODE_DRM_UNSPECIFIED,
        PlaybackException.ERROR_CODE_DRM_SCHEME_UNSUPPORTED,
        PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED ->
            "یہ ویڈیو محفوظ ہے اور نہیں چل سکتی۔"
        else ->
            "ویڈیو نہیں چل سکی۔ دوبارہ کوشش کریں۔"
    }
}

/** True for failures that are most likely a bad/lost connection, so trying again can fix them. */
@OptIn(UnstableApi::class)
private fun isNetworkError(error: PlaybackException): Boolean = when (error.errorCode) {
    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
    PlaybackException.ERROR_CODE_TIMEOUT -> true
    // A server answer is only worth retrying when it is a temporary one (5xx, timeout, busy).
    // A 404/403 means the link itself is wrong, and retrying would only keep the viewer waiting.
    PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
        val code = (error.cause as? HttpDataSource.InvalidResponseCodeException)?.responseCode
        code == null || code >= 500 || code == 408 || code == 429
    }
    else -> false
}

// --- Anti "loads forever" settings -----------------------------------------------------------
/** Seconds with NO new video data arriving while buffering, before we restart the connection. */
private const val STALL_RESTART_AFTER_SECONDS = 12
/** Seconds without new data before the viewer is told "internet is slow". */
private const val STALL_NOTICE_AFTER_SECONDS = 5
/** How many times we restart on our own before showing the Retry screen. */
private const val MAX_AUTO_RESTARTS = 3
private const val CONNECT_TIMEOUT_MS = 10_000
private const val READ_TIMEOUT_MS = 10_000

/**
 * Data source for STREAMING. Same cache behaviour as before (anything already downloaded plays
 * from disk), but with shorter timeouts than the downloader uses, so a dead connection fails in
 * about 10 seconds and gets restarted instead of hanging.
 */
@OptIn(UnstableApi::class)
private fun streamingDataSourceFactory(context: Context): DataSource.Factory {
    val http = DefaultHttpDataSource.Factory()
        .setUserAgent("MoviesForever/1.0 (Android)")
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(CONNECT_TIMEOUT_MS)
        .setReadTimeoutMs(READ_TIMEOUT_MS)
    return CacheDataSource.Factory()
        .setCache(DownloadUtil.getDownloadCache(context))
        .setUpstreamDataSourceFactory(http)
        .setCacheWriteDataSinkFactory(null) // read from cache; writing is the downloader's job
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
}

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    videoUrl: String,
    title: String,
    onBack: () -> Unit,
    cacheKey: String? = null
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    var isFullScreenAspect by remember { mutableStateOf(false) }
    var showOverlayControls by remember { mutableStateOf(true) }

    // Subtitle management states
    var subtitlesEnabled by remember { mutableStateOf(true) }
    var availableSubtitles by remember { mutableStateOf<List<SubtitleTrackInfo>>(emptyList()) }
    var selectedSubtitleTrack by remember { mutableStateOf<SubtitleTrackInfo?>(null) }
    var showSubtitleDialog by remember { mutableStateOf(false) }

    // Audio track management states
    var availableAudioTracks by remember { mutableStateOf<List<AudioTrackInfo>>(emptyList()) }
    var selectedAudioTrack by remember { mutableStateOf<AudioTrackInfo?>(null) }
    var showAudioDialog by remember { mutableStateOf(false) }

    // Video resize / aspect-ratio mode state
    var currentResizeMode by remember { mutableStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var showAspectRatioDialog by remember { mutableStateOf(false) }

    // Playback error / diagnostics state
    var playbackError by remember { mutableStateOf<PlaybackException?>(null) }

    // --- Diagnostic state, added to catch the "loads forever" bug ---
    // playerState mirrors ExoPlayer's internal state so a watchdog effect below
    // can react to it (Compose can't observe ExoPlayer's own state directly).
    var playerState by remember { mutableStateOf(Player.STATE_IDLE) }
    var loadStartedAtMs by remember { mutableStateOf(0L) }

    // Stall handling: how long no new data has arrived, how many times we restarted on our own,
    // and whether we gave up (then the Retry screen is shown instead of an endless spinner).
    var stalledSeconds by remember { mutableIntStateOf(0) }
    var autoRestarts by remember { mutableIntStateOf(0) }
    var gaveUp by remember { mutableStateOf(false) }

    // Initialize ExoPlayer
    val exoPlayer = remember(videoUrl, cacheKey) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Preparing player for url=$videoUrl cacheKey=$cacheKey")
        }
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(streamingDataSourceFactory(context))
            // Fewer silent retries (default is 3 with growing waits): fail sooner so we can recover.
            .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(/* minimumLoadableRetryCount = */ 3))
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(videoUrl))
            .apply { cacheKey?.let { setCustomCacheKey(it) } }
            .build()

        // Retain the last 30s of played samples in memory so backward seeks
        // within that window are instant instead of re-triggering a load.
        // Data is already in our disk cache / already fetched — this just
        // delays discarding it from ExoPlayer's in-memory sample buffer.
        val loadControl = DefaultLoadControl.Builder()
            // Start playing sooner (1.5s instead of 2.5s of video), but after a stall wait for a
            // bigger cushion (4s) so it does not stop again right away. Keep up to 60s ahead.
            .setBufferDurationsMs(
                /* minBufferMs = */ 20_000,
                /* maxBufferMs = */ 60_000,
                /* bufferForPlaybackMs = */ 1_500,
                /* bufferForPlaybackAfterRebufferMs = */ 4_000
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBackBuffer(
                /* backBufferDurationMs = */ 30_000,
                /* retainBackBufferFromKeyframe = */ true
            )
            .build()

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            // Duck/pause automatically for phone calls, other apps' audio, etc.,
            // and resume when focus is regained, instead of playing over them.
            .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus = */ true)
            .build()
            .apply {
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
    }

    // Restart loading from where the viewer is, on a fresh connection. This is what un-sticks a
    // video that is "loading and loading": the old connection is dropped and a new one is opened.
    fun restartLoading() {
        val item = exoPlayer.currentMediaItem ?: return
        val position = exoPlayer.currentPosition
        exoPlayer.setMediaItem(item, position)
        exoPlayer.prepare()
    }

    // Internet came back (it was lost while this screen was open): resume by itself if we were
    // stuck or had failed. Only reacts after a real "lost" event, so it never fires at start.
    DisposableEffect(exoPlayer) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        var wasLost = false
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                wasLost = true
            }

            override fun onAvailable(network: Network) {
                if (!wasLost) return
                wasLost = false
                Handler(Looper.getMainLooper()).post {
                    val stuck = exoPlayer.playbackState == Player.STATE_BUFFERING ||
                            exoPlayer.playerError != null
                    if (stuck) {
                        playbackError = null
                        gaveUp = false
                        stalledSeconds = 0
                        restartLoading()
                    }
                }
            }
        }
        try {
            cm?.registerDefaultNetworkCallback(callback)
        } catch (e: Exception) {
            Log.w(TAG, "Could not watch the network", e)
        }
        onDispose {
            try {
                cm?.unregisterNetworkCallback(callback)
            } catch (_: Exception) {
            }
        }
    }

    // Immersive mode: hide the status bar and gesture-nav bar while the player
    // is on screen, restoring them the moment this screen is left. Without
    // this, the notification bar and the bottom gesture pill stay visible the
    // whole time, which is distracting for a full-screen video player. Swiping
    // from an edge still temporarily reveals the bars (BEHAVIOR_SHOW_TRANSIENT_
    // BARS_BY_SWIPE), same as YouTube/Netflix, so the user isn't ever fully
    // locked out of system gestures.
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = activity?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        insetsController?.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            window?.let { WindowCompat.getInsetsController(it, view) }
                ?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Listen for available tracks (Text and Audio) + playback errors
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                val subtitleList = mutableListOf<SubtitleTrackInfo>()
                val audioList = mutableListOf<AudioTrackInfo>()

                for (groupIndex in 0 until tracks.groups.size) {
                    val trackGroup = tracks.groups[groupIndex]
                    if (trackGroup.type == C.TRACK_TYPE_TEXT) {
                        for (trackIndex in 0 until trackGroup.length) {
                            val format = trackGroup.getTrackFormat(trackIndex)
                            val lang = format.language ?: "Unknown"
                            val label = format.label ?: "Track ${subtitleList.size + 1} ($lang)"
                            subtitleList.add(
                                SubtitleTrackInfo(
                                    groupIndex = groupIndex,
                                    trackIndex = trackIndex,
                                    language = lang,
                                    label = label
                                )
                            )
                        }
                    } else if (trackGroup.type == C.TRACK_TYPE_AUDIO) {
                        for (trackIndex in 0 until trackGroup.length) {
                            val format = trackGroup.getTrackFormat(trackIndex)

                            val langCode = format.language?.takeIf { it.isNotBlank() && it != "und" }
                            val languageName = if (langCode != null) {
                                Locale(langCode).displayLanguage.replaceFirstChar { it.uppercase() }
                            } else {
                                "Audio Track ${audioList.size + 1}"
                            }

                            val channels = if (format.channelCount > 0) " (${format.channelCount}ch)" else ""
                            val label = if (langCode != null) {
                                "$languageName$channels"
                            } else {
                                "Track ${audioList.size + 1}$channels"
                            }

                            val trackInfo = AudioTrackInfo(
                                groupIndex = groupIndex,
                                trackIndex = trackIndex,
                                language = langCode ?: "Unknown",
                                label = label
                            )
                            audioList.add(trackInfo)

                            if (trackGroup.isTrackSelected(trackIndex) && selectedAudioTrack == null) {
                                selectedAudioTrack = trackInfo
                            }
                        }
                    }
                }
                availableSubtitles = subtitleList
                availableAudioTracks = audioList
            }

            override fun onPlayerError(error: PlaybackException) {
                // This is the key hook: ExoPlayer stops silently unless we catch this.
                Log.e(
                    TAG,
                    "Playback error for url=$videoUrl code=${error.errorCode} " +
                            "name=${error.errorCodeName} message=${error.message}",
                    error
                )
                // Log the full cause chain too -- often the real reason (e.g. an
                // HttpDataSource.InvalidResponseCodeException with the actual HTTP
                // status) is nested inside error.cause, not in the top-level message.
                var cause: Throwable? = error.cause
                var depth = 0
                while (cause != null && depth < 5) {
                    Log.e(TAG, "  caused by [$depth]: ${cause.javaClass.simpleName}: ${cause.message}")
                    cause = cause.cause
                    depth++
                }
                // A lost/slow connection usually fixes itself: try again on our own a few times
                // before bothering the viewer with an error screen.
                if (isNetworkError(error) && autoRestarts < MAX_AUTO_RESTARTS) {
                    autoRestarts++
                    restartLoading()
                } else {
                    playbackError = error
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                // Clear a stale error once playback actually starts progressing again
                if (playbackState == Player.STATE_READY) {
                    playbackError = null
                    gaveUp = false
                    stalledSeconds = 0
                    autoRestarts = 0
                }
                val name = when (playbackState) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN($playbackState)"
                }
                Log.d(
                    TAG,
                    "onPlaybackStateChanged: $name position=${exoPlayer.currentPosition}ms " +
                            "bufferedPosition=${exoPlayer.bufferedPosition}ms " +
                            "bufferedDuration=${exoPlayer.totalBufferedDuration}ms"
                )
                playerState = playbackState
            }

            // Fires whenever ExoPlayer starts/stops actively loading data from the
            // source - this is the direct signal for "spinner is showing right now".
            // Logging the elapsed duration here is what will prove whether a video is
            // truly stuck (load never ends) vs. just slow (load ends, eventually).
            override fun onIsLoadingChanged(isLoading: Boolean) {
                if (isLoading) {
                    loadStartedAtMs = System.currentTimeMillis()
                    Log.d(TAG, "onIsLoadingChanged: LOAD START url=$videoUrl")
                } else {
                    val elapsed = if (loadStartedAtMs > 0) System.currentTimeMillis() - loadStartedAtMs else -1
                    Log.d(TAG, "onIsLoadingChanged: LOAD END elapsedMs=$elapsed")
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Stall watchdog. While ExoPlayer is BUFFERING it counts the seconds in which NO new video
    // data arrived (bufferedPosition not moving):
    //   - after STALL_NOTICE_AFTER_SECONDS the viewer sees "internet is slow" (so it is not a mystery);
    //   - after STALL_RESTART_AFTER_SECONDS the connection is restarted on its own;
    //   - after MAX_AUTO_RESTARTS restarts it stops and shows the Retry screen.
    // Slow-but-moving data is NOT a stall: the counter resets whenever new data arrives.
    LaunchedEffect(playerState, playbackError, gaveUp) {
        stalledSeconds = 0
        if (playerState != Player.STATE_BUFFERING || playbackError != null || gaveUp) return@LaunchedEffect
        var lastBuffered = exoPlayer.bufferedPosition
        var idle = 0
        while (true) {
            delay(1000)
            val buffered = exoPlayer.bufferedPosition
            if (buffered != lastBuffered) {
                lastBuffered = buffered
                idle = 0
            } else {
                idle++
            }
            stalledSeconds = idle
            if (idle >= STALL_RESTART_AFTER_SECONDS) {
                Log.w(TAG, "STALL: no data for ${idle}s (restart ${autoRestarts + 1}/$MAX_AUTO_RESTARTS) url=$videoUrl")
                if (autoRestarts < MAX_AUTO_RESTARTS) {
                    autoRestarts++
                    idle = 0
                    stalledSeconds = 0
                    lastBuffered = -1L
                    restartLoading()
                } else {
                    gaveUp = true
                    return@LaunchedEffect
                }
            }
        }
    }

    // Auto-hide top overlay controls timer
    LaunchedEffect(showOverlayControls) {
        if (showOverlayControls) {
            delay(4000)
            showOverlayControls = false
        }
    }

    // Handle Orientation Changes
    LaunchedEffect(isFullScreenAspect) {
        if (isFullScreenAspect) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    // Toggle subtitles
    fun toggleSubtitles(enable: Boolean, track: SubtitleTrackInfo? = null) {
        val parameters = exoPlayer.trackSelectionParameters.buildUpon()
        if (!enable) {
            parameters.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            subtitlesEnabled = false
            selectedSubtitleTrack = null
        } else {
            parameters.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
            if (track != null) {
                val trackGroup = exoPlayer.currentTracks.groups[track.groupIndex].mediaTrackGroup
                parameters.setOverrideForType(
                    TrackSelectionOverride(trackGroup, track.trackIndex)
                )
                selectedSubtitleTrack = track
            } else {
                parameters.clearOverridesOfType(C.TRACK_TYPE_TEXT)
            }
            subtitlesEnabled = true
        }
        exoPlayer.trackSelectionParameters = parameters.build()
    }

    // Switch audio track
    fun selectAudioTrack(track: AudioTrackInfo) {
        val trackGroup = exoPlayer.currentTracks.groups[track.groupIndex].mediaTrackGroup
        val parameters = exoPlayer.trackSelectionParameters
            .buildUpon()
            .setOverrideForType(TrackSelectionOverride(trackGroup, track.trackIndex))
            .build()
        exoPlayer.trackSelectionParameters = parameters
        selectedAudioTrack = track
    }

    // Retry playback from scratch after an error
    fun retryPlayback() {
        playbackError = null
        gaveUp = false
        stalledSeconds = 0
        autoRestarts = 0
        restartLoading()
        exoPlayer.playWhenReady = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showOverlayControls = !showOverlayControls
            }
    ) {
        // Native ExoPlayer View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                    resizeMode = currentResizeMode
                    controllerShowTimeoutMs = 3000
                    keepScreenOn = true
                    setOnClickListener {
                        showOverlayControls = !showOverlayControls
                    }
                }
            },
            update = { playerView ->
                // Called on every recomposition where currentResizeMode is a read key;
                // this is what actually lets the user change aspect ratio live.
                if (playerView.resizeMode != currentResizeMode) {
                    playerView.resizeMode = currentResizeMode
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Custom Top Overlay Bar
        AnimatedVisibility(
            visible = showOverlayControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Black.copy(alpha = 0.85f),
                                Color.Transparent
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(38.dp)
                                .background(DarkSurface.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = title,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Aspect Ratio / Resize Mode Selector
                        IconButton(
                            onClick = { showAspectRatioDialog = true },
                            modifier = Modifier
                                .size(38.dp)
                                .background(DarkSurface.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AspectRatio,
                                contentDescription = "Aspect Ratio",
                                tint = if (currentResizeMode != AspectRatioFrameLayout.RESIZE_MODE_FIT) Gold else TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        // Audio Track Selector
                        if (availableAudioTracks.size > 1) {
                            IconButton(
                                onClick = { showAudioDialog = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(DarkSurface.copy(alpha = 0.7f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Audiotrack,
                                    contentDescription = "Audio Language",
                                    tint = Gold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(8.dp))
                        }

                        // Subtitles Toggle Button
                        if (availableSubtitles.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    if (availableSubtitles.size > 1) {
                                        showSubtitleDialog = true
                                    } else {
                                        toggleSubtitles(!subtitlesEnabled)
                                    }
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(DarkSurface.copy(alpha = 0.7f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (subtitlesEnabled) {
                                        Icons.Filled.ClosedCaption
                                    } else {
                                        Icons.Filled.ClosedCaptionOff
                                    },
                                    contentDescription = "Subtitles",
                                    tint = if (subtitlesEnabled) Gold else TextPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(Modifier.width(8.dp))
                        }

                        // Screen Aspect Ratio Toggle Button (fullscreen/portrait orientation, unrelated to resizeMode)
                        IconButton(
                            onClick = { isFullScreenAspect = !isFullScreenAspect },
                            modifier = Modifier
                                .size(38.dp)
                                .background(DarkSurface.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isFullScreenAspect) {
                                    Icons.Filled.FullscreenExit
                                } else {
                                    Icons.Filled.Fullscreen
                                },
                                contentDescription = "Toggle Landscape Mode",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // "Internet is slow" notice: shown while buffering has had no new data for a few seconds,
        // so the viewer knows the app is still trying and has not frozen.
        if (stalledSeconds >= STALL_NOTICE_AFTER_SECONDS && playbackError == null && !gaveUp) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 72.dp, start = 24.dp, end = 24.dp)
                        .background(Black.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "انٹرنیٹ سست ہے۔ ویڈیو لوڈ ہو رہی ہے، تھوڑا انتظار کریں…",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Error / gave-up screen: instead of a spinner that never ends, say what happened in
        // simple Urdu and offer Retry. The raw error code stays visible for problem reports.
        if (playbackError != null || gaveUp) {
            val error = playbackError
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Black.copy(alpha = 0.92f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* swallow taps */ },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ویڈیو نہیں چل رہی",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = if (error != null) getPlaybackErrorMessage(error)
                            else "ویڈیو لوڈ نہیں ہو سکی۔ اپنا انٹرنیٹ چیک کریں اور دوبارہ کوشش کریں۔",
                            color = TextPrimary.copy(alpha = 0.85f),
                            fontSize = 15.sp,
                            lineHeight = 26.sp,
                            textAlign = TextAlign.Center
                        )
                        if (error != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Error code: ${error.errorCode} (${error.errorCodeName})",
                                color = TextPrimary.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                            error.cause?.message?.let { causeMessage ->
                                if (BuildConfig.DEBUG) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = causeMessage,
                                        color = TextPrimary.copy(alpha = 0.4f),
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Row {
                            Button(
                                onClick = { retryPlayback() },
                                colors = ButtonDefaults.buttonColors(containerColor = Gold)
                            ) {
                                Text("دوبارہ کوشش کریں", color = Black)
                            }
                            Spacer(Modifier.width(12.dp))
                            OutlinedButton(onClick = onBack) {
                                Text("واپس جائیں", color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Audio Track Selection Dialog
        if (showAudioDialog) {
            Dialog(onDismissRequest = { showAudioDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .background(DarkSurface, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Audio Language",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(14.dp))

                        availableAudioTracks.forEachIndexed { index, track ->
                            if (index > 0) {
                                HorizontalDivider(color = DarkElevated)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectAudioTrack(track)
                                        showAudioDialog = false
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedAudioTrack == track || (selectedAudioTrack == null && index == 0),
                                    onClick = {
                                        selectAudioTrack(track)
                                        showAudioDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Gold)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(track.label, color = TextPrimary, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        // Subtitle Selection Dialog
        if (showSubtitleDialog) {
            Dialog(onDismissRequest = { showSubtitleDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .background(DarkSurface, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Subtitle Language",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    toggleSubtitles(false)
                                    showSubtitleDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !subtitlesEnabled,
                                onClick = {
                                    toggleSubtitles(false)
                                    showSubtitleDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Gold)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Off", color = TextPrimary, fontSize = 14.sp)
                        }

                        HorizontalDivider(color = DarkElevated)

                        availableSubtitles.forEach { track ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        toggleSubtitles(true, track)
                                        showSubtitleDialog = false
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = subtitlesEnabled && (selectedSubtitleTrack == track || selectedSubtitleTrack == null && track == availableSubtitles.firstOrNull()),
                                    onClick = {
                                        toggleSubtitles(true, track)
                                        showSubtitleDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Gold)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(track.label, color = TextPrimary, fontSize = 14.sp)
                            }
                        }

                    }
                }
            }
        }

        // Aspect Ratio / Resize Mode Selection Dialog
        if (showAspectRatioDialog) {
            Dialog(onDismissRequest = { showAspectRatioDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .background(DarkSurface, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Video Fit",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(14.dp))

                        resizeModeOptions.forEachIndexed { index, option ->
                            if (index > 0) {
                                HorizontalDivider(color = DarkElevated)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentResizeMode = option.mode
                                        showAspectRatioDialog = false
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentResizeMode == option.mode,
                                    onClick = {
                                        currentResizeMode = option.mode
                                        showAspectRatioDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Gold)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(option.label, color = TextPrimary, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}