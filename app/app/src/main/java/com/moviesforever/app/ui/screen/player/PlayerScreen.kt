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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.moviesforever.app.ui.theme.Black
import com.moviesforever.app.ui.theme.DarkElevated
import com.moviesforever.app.ui.theme.DarkSurface
import com.moviesforever.app.ui.theme.Gold
import com.moviesforever.app.ui.theme.TextPrimary
import com.moviesforever.app.ui.theme.TextSecondary
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

data class SubtitleTrackInfo(
    val groupIndex: Int,
    val trackIndex: Int,
    val language: String,
    val label: String
)

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

    // Subtitle management states
    var subtitlesEnabled by remember { mutableStateOf(true) }
    var availableSubtitles by remember { mutableStateOf<List<SubtitleTrackInfo>>(emptyList()) }
    var selectedSubtitleTrack by remember { mutableStateOf<SubtitleTrackInfo?>(null) }
    var showSubtitleDialog by remember { mutableStateOf(false) }

    // Initialize ExoPlayer
    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            prepare()
            playWhenReady = true
        }
    }

    // Listen for available subtitle tracks once video metadata is loaded
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                val subtitleList = mutableListOf<SubtitleTrackInfo>()
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
                    }
                }
                availableSubtitles = subtitleList
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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

    // Function to enable/disable or change subtitle tracks
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
                    androidx.media3.common.TrackSelectionOverride(trackGroup, track.trackIndex)
                )
                selectedSubtitleTrack = track
            } else {
                parameters.clearOverridesOfType(C.TRACK_TYPE_TEXT)
            }
            subtitlesEnabled = true
        }
        exoPlayer.trackSelectionParameters = parameters.build()
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

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Subtitles Toggle Button (CC Icon)
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

                        // Screen Aspect Ratio Toggle Button ([ ])
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

        // Subtitle Selection Dialog (if multiple tracks exist)
        if (showSubtitleDialog) {
            Dialog(onDismissRequest = { showSubtitleDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "Subtitle Language",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(14.dp))

                        // Option 1: Off
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

                        // Option 2: Available Subtitle Tracks
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
    }
}