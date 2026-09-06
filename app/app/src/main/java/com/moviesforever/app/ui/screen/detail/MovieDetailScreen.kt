package com.moviesforever.app.ui.screen.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
        // Hero Image Header with Gradient Overlay & Back Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        ) {
            AsyncImage(
                model = movie.thumbnailUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Deep Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Black.copy(alpha = 0.5f),
                                Color.Transparent,
                                Black.copy(alpha = 0.8f),
                                Black
                            ),
                            startY = 0f
                        )
                    )
            )

            // Floating Top Back Action Button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(40.dp)
                    .background(DarkSurface.copy(alpha = 0.7f), CircleShape)
                    .border(1.dp, DarkElevated, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Optional Floating Badge Overlay
            movie.badge?.let { badge ->
                Surface(
                    color = Gold,
                    shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 12.dp, topEnd = 12.dp, bottomEnd = 0.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = badge,
                        color = Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Detailed Content Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-20).dp)
                .padding(horizontal = 16.dp)
        ) {
            // Movie Title
            Text(
                text = movie.title,
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )

            Spacer(Modifier.height(10.dp))

            // Metadata Row: IMDb Rating, Release Year, Language
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                movie.imdbRating?.let { rating ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(DarkSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = Gold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "$rating",
                            color = GoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                movie.year?.let { year ->
                    Text(text = "$year", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                if (movie.language.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(TextMuted, CircleShape)
                    )
                    Text(text = movie.language, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                if (movie.isFree) {
                    Surface(
                        color = Gold,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "FREE",
                            color = Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Genre Chips Row
            if (movie.genres.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    movie.genres.forEach { genreId ->
                        genres[genreId]?.let { name ->
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(DarkSurface)
                                    .border(1.dp, DarkElevated, CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(text = name, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Action Buttons / Conversion Section
            // Action Buttons / Conversion Section
            if (canFullPlay) {
                GoldButton(
                    text = "Watch Now",
                    onClick = onWatchNow,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                GoldOutlinedButton(
                    text = "Download Offline",
                    onClick = onDownload,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                if (hasTrailer) {
                    OutlinedButton(
                        onClick = onWatchTrailer,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkElevated)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Watch Trailer", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(14.dp))
                }

                // Redesigned English Lifetime Access Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "LIMITED TIME OFFER",
                                    color = Error,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "First 500 users only",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = if (pricing.standardPrice > 0)
                                "Unlock Everything for PKR ${pricing.standardPrice.toInt()}"
                            else
                                "Unlock Lifetime Access",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "Pay once and enjoy unlimited movies and fast downloads forever. No monthly subscriptions, ever.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
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

            // Movie Description
            if (movie.description.isNotBlank()) {
                Text(
                    text = "Overview",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = movie.description,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(14.dp))
            }

            // Available Dubs Information
            if (movie.availableDubs.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Available Dubs: ",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = movie.availableDubs.joinToString(", "),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}