package com.moviesforever.app.ui.screen.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.moviesforever.app.R
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.data.model.PricingSettings
import com.moviesforever.app.ui.components.GoldButton
import com.moviesforever.app.ui.components.GoldOutlinedButton
import com.moviesforever.app.ui.theme.*

@Composable
fun MovieDetailScreen(
    movie: Movie,
    pricing: PricingSettings,
    isUnlocked: Boolean,
    genres: Map<String, String>,
    onWatchNow: () -> Unit,
    onWatchTrailer: () -> Unit,
    onDownload: () -> Unit,
    onUnlockClick: () -> Unit,
    onBack: () -> Unit
) {
    val canFullPlay = movie.isFree || isUnlocked
    val hasTrailer = !movie.trailerUrl.isNullOrBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("←") }
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(36.dp)
            )
        }

        // Hero
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = movie.thumbnailUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.5f to Color.Transparent,
                            1f to Black
                        )
                    )
            )
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            // Title + badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                movie.badge?.let {
                    Text(it, fontSize = 24.sp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = movie.title,
                    color = TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            // Metadata row
            Row(verticalAlignment = Alignment.CenterVertically) {
                movie.imdbRating?.let {
                    Text("⭐ $it", color = GoldLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(12.dp))
                }
                movie.year?.let {
                    Text("$it", color = TextSecondary, fontSize = 14.sp)
                    Spacer(Modifier.width(12.dp))
                }
                if (movie.language.isNotBlank()) {
                    Text(movie.language, color = TextSecondary, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Genre chips
            if (movie.genres.isNotEmpty()) {
                Row {
                    movie.genres.forEach { genreId ->
                        genres[genreId]?.let { name ->
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .background(DarkSurface, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(name, color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Free tag
            if (movie.isFree) {
                Box(
                    modifier = Modifier
                        .background(Gold, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("FREE MOVIE", color = Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(Modifier.height(12.dp))
            }

            // Action buttons
            if (canFullPlay) {
                GoldButton(
                    text = "Watch Now",
                    onClick = onWatchNow,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!movie.isFree) {
                    Spacer(Modifier.height(10.dp))
                    GoldOutlinedButton(
                        text = "Download",
                        onClick = onDownload,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // Locked path
                if (hasTrailer) {
                    GoldButton(
                        text = "Watch Trailer",
                        onClick = onWatchTrailer,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(16.dp))
                // Conversion box (Roman Urdu + English)
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = if (pricing.standardPrice > 0)
                                "پوری لائف کے لیے صرف PKR ${pricing.standardPrice.toInt()} میں تمام موویز لاک کریں!"
                            else
                                "پوری لائف کے لیے تمام موویز لاک کریں!",
                            color = GoldLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (pricing.standardPrice > 0)
                                "Unlock this movie and the entire library forever for just PKR ${pricing.standardPrice.toInt()} (one-time)."
                            else
                                "This is a paid movie. Unlock the entire library with a one-time lifetime payment to watch.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(14.dp))
                        GoldButton(
                            text = "Unlock Lifetime Access",
                            onClick = onUnlockClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Description
            if (movie.description.isNotBlank()) {
                Text(
                    text = movie.description,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(8.dp))
            }

            // Dubs
            if (movie.availableDubs.isNotEmpty()) {
                Text(
                    text = "Available dubs: ${movie.availableDubs.joinToString(", ")}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
