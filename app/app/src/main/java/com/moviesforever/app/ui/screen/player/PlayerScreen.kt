package com.moviesforever.app.ui.screen.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.moviesforever.app.ui.theme.Black
import com.moviesforever.app.ui.theme.DarkSurface
import com.moviesforever.app.ui.theme.TextPrimary
import kotlinx.coroutines.delay

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

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    videoUrl: String,
    title: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    var isFullScreenAspect by remember { mutableStateOf(false) }
    var showOverlayControls by remember { mutableStateOf(true) }

    // Initialize ExoPlayer
    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            prepare()
            playWhenReady = true
        }
    }

    // Auto-hide top overlay controls timer
    LaunchedEffect(showOverlayControls) {
        if (showOverlayControls) {
            delay(4000)
            showOverlayControls = false
        }
    }

    // Handle Orientation Changes when Fullscreen button is clicked
    LaunchedEffect(isFullScreenAspect) {
        if (isFullScreenAspect) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
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

                    // Hide next and previous video buttons
                    setShowNextButton(false)
                    setShowPreviousButton(false)

                    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    controllerShowTimeoutMs = 3000
                    setOnClickListener {
                        showOverlayControls = !showOverlayControls
                    }
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

                    // Screen Aspect Ratio Toggle Button
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
}