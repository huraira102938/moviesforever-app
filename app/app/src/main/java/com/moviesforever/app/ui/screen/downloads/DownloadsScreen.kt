package com.moviesforever.app.ui.screen.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.ui.theme.*

@Composable
fun DownloadsScreen(
    downloadedMovies: List<Movie>,
    isUnlocked: Boolean,
    onMovieClick: (Movie) -> Unit,
    onSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(14.dp))

        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 20.dp)
                        .background(Gold, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "My Downloads",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onSettings,
                modifier = Modifier
                    .size(38.dp)
                    .background(DarkSurface, CircleShape)
                    .border(1.dp, DarkElevated, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = Gold,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Access Status Banner
        if (!isUnlocked) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = Error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Downloads are enabled for Free movies. Unlock Lifetime Access to download the full library.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Downloads List or Empty State
        if (downloadedMovies.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(DarkSurface, CircleShape)
                            .border(1.dp, DarkElevated, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FileDownload,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "No downloads yet",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Download movies to watch offline anytime, even without an internet connection.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(downloadedMovies) { movie ->
                    DownloadedMovieCard(
                        movie = movie,
                        onClick = { onMovieClick(movie) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadedMovieCard(
    movie: Movie,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkElevated, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = movie.thumbnailUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(70.dp)
                    .height(95.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = movie.title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    movie.year?.let { year ->
                        Text(
                            text = "$year",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    if (movie.language.isNotBlank()) {
                        Text(
                            text = movie.language,
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DownloadDone,
                        contentDescription = "Downloaded",
                        tint = Gold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Downloaded • Encrypted",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Gold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}