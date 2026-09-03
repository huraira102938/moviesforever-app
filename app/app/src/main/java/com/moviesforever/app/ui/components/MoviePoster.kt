package com.moviesforever.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.ui.theme.DarkSurface
import com.moviesforever.app.ui.theme.Gold
import com.moviesforever.app.ui.theme.TextMuted

@Composable
fun MoviePoster(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Int = 120
) {
    Column(
        modifier = modifier
            .width(width.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((width * 1.5).dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurface),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = movie.thumbnailUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height((width * 1.5).dp)
            )
            // Free tag
            if (movie.isFree) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(Gold.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "FREE",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            // Badge emoji top-right
            movie.badge?.let { badge ->
                Text(
                    text = badge,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                )
            }
        }
        Text(
            text = movie.title,
            color = TextMuted,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
