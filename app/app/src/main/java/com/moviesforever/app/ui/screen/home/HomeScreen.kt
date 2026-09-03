package com.moviesforever.app.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.moviesforever.app.R
import com.moviesforever.app.data.model.Banner
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.data.model.PricingSettings
import com.moviesforever.app.ui.components.MoviePoster
import com.moviesforever.app.ui.components.SectionLabels
import com.moviesforever.app.ui.theme.*

/**
 * Home screen: unlock strip (if not unlocked) + banner carousel + curated shelves.
 */
@Composable
fun HomeScreen(
    banners: List<Banner>,
    movies: List<Movie>,
    pricing: PricingSettings,
    isUnlocked: Boolean,
    onBannerClick: (Banner) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onUnlockClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        // Top bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.weight(1f))
                // Profile avatar
                CircularIconButton(onClick = onAvatarClick)
            }
        }

        // Unlock banner strip - only if not unlocked
        if (!isUnlocked) {
            item {
                UnlockStrip(
                    price = pricing.standardPrice,
                    onClick = onUnlockClick
                )
            }
        }

        // Banner carousel
        if (banners.isNotEmpty()) {
            item { BannerCarousel(banners = banners, onClick = onBannerClick) }
        }

        // Curated shelves
        SectionLabels.orderedSections.forEach { section ->
            val sectionMovies = movies
                .filter { it.sections.contains(section) && !it.paused }
            if (sectionMovies.isNotEmpty()) {
                item {
                    SectionRow(
                        title = SectionLabels.label(section),
                        movies = sectionMovies,
                        onMovieClick = onMovieClick
                    )
                }
            }
        }

        // Free shelf - only if at least one free movie
        val freeMovies = movies.filter { it.isFree && !it.paused }
        if (freeMovies.isNotEmpty()) {
            item {
                SectionRow(
                    title = "Free",
                    movies = freeMovies,
                    onMovieClick = onMovieClick
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun CircularIconButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(Gold, RoundedCornerShape(20.dp))
            .padding(0.dp),
        contentAlignment = Alignment.Center
    ) {
        TextButton(onClick = onClick) {
            Text("👤", fontSize = 20.sp)
        }
    }
}

@Composable
private fun UnlockStrip(price: Double, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(
                Brush.horizontalGradient(listOf(GoldDeep, Gold)),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Unlock Everything for PKR ${price.toInt()}",
                color = Black,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Text("→", color = Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun BannerCarousel(banners: List<Banner>, onClick: (Banner) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { banners.size })
    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) { page ->
            val banner = banners[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
                    .clickable { onClick(banner) }
            ) {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = "Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (!banner.clickable) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Scrim),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Upcoming",
                            color = GoldLight,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SectionRow(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    Column(Modifier.padding(vertical = 10.dp)) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(movies) { movie ->
                MoviePoster(movie = movie, onClick = { onMovieClick(movie) })
            }
        }
    }
}

