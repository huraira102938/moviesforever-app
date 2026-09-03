package com.moviesforever.app.ui.screen.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.ui.components.MoviePoster
import com.moviesforever.app.ui.theme.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

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
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("My Downloads", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onSettings) { Text("⚙", color = GoldLight, fontSize = 20.sp) }
        }

        Spacer(Modifier.height(8.dp))

        if (!isUnlocked) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Downloads are available for Free movies. Unlock the app for the full library.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        if (downloadedMovies.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⬇", color = GoldLight, fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("No downloads yet", color = TextMuted, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Download movies to watch offline, even without internet.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(downloadedMovies) { movie ->
                    MoviePoster(movie = movie, onClick = { onMovieClick(movie) })
                }
            }
        }
    }
}
