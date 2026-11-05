package com.moviesforever.app.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionOff
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.ui.theme.Black
import com.moviesforever.app.ui.theme.DarkElevated
import com.moviesforever.app.ui.theme.Gold
import com.moviesforever.app.ui.theme.TextPrimary
import com.moviesforever.app.ui.theme.TextSecondary

const val TV_SEEK_STEP_MS = 10_000L

/** Player controls for the TV remote: left/right moves between buttons, OK presses the button. */
@Composable
fun TvPlayerControls(
    title: String,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    showAudio: Boolean,
    showSubtitles: Boolean,
    subtitlesOn: Boolean,
    videoFitChanged: Boolean,
    dialogOpen: Boolean,
    onPlayPause: () -> Unit,
    onSeekBy: (Long) -> Unit,
    onVideoFit: () -> Unit,
    onAudio: () -> Unit,
    onSubtitles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playFocus = remember { FocusRequester() }

    // Start on Play/Pause, and come back to it after a picker closes.
    LaunchedEffect(dialogOpen) {
        if (!dialogOpen) playFocus.requestFocus()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Black.copy(alpha = 0.85f), Color.Transparent)))
                .padding(horizontal = 48.dp, vertical = 27.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Black.copy(alpha = 0.9f))))
                .padding(horizontal = 48.dp, vertical = 27.dp)
        ) {
            TvPlayerProgress(positionMs = positionMs, durationMs = durationMs)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Top
            ) {
                TvIconButton(
                    icon = Icons.Filled.Replay10,
                    label = "-10 sec",
                    onClick = { onSeekBy(-TV_SEEK_STEP_MS) }
                )
                TvIconButton(
                    icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    label = if (isPlaying) "Pause" else "Play",
                    onClick = onPlayPause,
                    focusRequester = playFocus
                )
                TvIconButton(
                    icon = Icons.Filled.Forward10,
                    label = "+10 sec",
                    onClick = { onSeekBy(TV_SEEK_STEP_MS) }
                )
                Spacer(Modifier.width(24.dp))
                if (showAudio) {
                    TvIconButton(
                        icon = Icons.Filled.Audiotrack,
                        label = "Audio",
                        onClick = onAudio,
                        tint = Gold
                    )
                }
                if (showSubtitles) {
                    TvIconButton(
                        icon = if (subtitlesOn) Icons.Filled.ClosedCaption else Icons.Filled.ClosedCaptionOff,
                        label = "Subtitles",
                        onClick = onSubtitles,
                        tint = if (subtitlesOn) Gold else TextPrimary
                    )
                }
                TvIconButton(
                    icon = Icons.Filled.AspectRatio,
                    label = "Video Fit",
                    onClick = onVideoFit,
                    tint = if (videoFitChanged) Gold else TextPrimary
                )
            }
        }
    }
}

/** Time + progress line. Also shown on its own for a moment when seeking with the controls hidden. */
@Composable
fun TvPlayerProgress(positionMs: Long, durationMs: Long, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(formatPlaybackTime(positionMs), color = TextPrimary, fontSize = 14.sp)
        Spacer(Modifier.width(12.dp))
        LinearProgressIndicator(
            progress = { if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f },
            color = Gold,
            trackColor = DarkElevated,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        Text(formatPlaybackTime(durationMs), color = TextSecondary, fontSize = 14.sp)
    }
}
