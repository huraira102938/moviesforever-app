package com.moviesforever.app.ui.screen.player

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.moviesforever.app.ui.theme.Black
import kotlinx.coroutines.delay

/**
 * Plays a single progressive file via ExoPlayer (range requests supported natively).
 * Uses the raw video URL; when the Cloudflare Worker is deployed, pass the signed URL.
 */
@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun PlayerScreen(
    videoUrl: String,
    title: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Track play state
    LaunchedEffect(Unit) {
        while (true) {
            isPlaying = exoPlayer.isPlaying
            delay(500)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    controllerShowTimeoutMs = 3000
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top bar
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f))
        ) {
            TextButton(onClick = onBack) {
                Text("← Back", color = Color.White)
            }
        }
    }
}
