package com.moviesforever.app.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    banners: List<Banner>,
    movies: List<Movie>,
    trendingMovies: List<Movie>,
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
            .background(Black),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "MoviesForever",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onAvatarClick,
                    modifier = Modifier
                        .size(38.dp)
                        .background(DarkSurface, CircleShape)
                        .border(1.dp, DarkElevated, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = Gold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Hero Banner Carousel
        if (banners.isNotEmpty()) {
            item {
                ModernBannerCarousel(banners = banners, onClick = onBannerClick)
                Spacer(Modifier.height(16.dp))
            }
        }

        // Single admin-controlled offer banner (note text comes entirely from
        // the admin panel; only the "Limited Time Offer" label and "Get Pass"
        // button are app-owned chrome around it).
        if (!isUnlocked) {
            item {
                ModernOfferBanner(
                    note = pricing.note,
                    onClick = onUnlockClick
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        // Curated Shelves
        SectionLabels.orderedSections.forEach { section ->
            val sectionMovies = movies.filter { it.sections.contains(section) && !it.paused }
            if (sectionMovies.isNotEmpty()) {
                item {
                    if (section == SectionLabels.ALL_TIME_HIT) {
                        // All-time Hit is ordered newest-year-first, oldest
                        // last. The year itself is never shown to the user --
                        // it only drives the internal ordering.
                        ModernSectionRow(
                            title = SectionLabels.label(section),
                            movies = sectionMovies.sortedByDescending { it.year ?: Int.MIN_VALUE },
                            onMovieClick = onMovieClick
                        )
                    } else {
                        ModernSectionRow(
                            title = SectionLabels.label(section),
                            movies = sectionMovies,
                            onMovieClick = onMovieClick
                        )
                    }
                }
            }
        }

        // Free Shelf
        val freeMovies = movies.filter { it.isFree && !it.paused }
        if (freeMovies.isNotEmpty()) {
            item {
                ModernSectionRow(
                    title = "Free to Watch",
                    movies = freeMovies,
                    onMovieClick = onMovieClick
                )
            }
        }

        // Trending Now -- curated by the admin panel's dedicated "Trending"
        // page (its own movieId + order list, not a section tag). Shown at
        // the very bottom of the home feed as a static grid, not a row.
        if (trendingMovies.isNotEmpty()) {
            item {
                TrendingSection(
                    movies = trendingMovies,
                    onMovieClick = onMovieClick
                )
            }
        }
    }
}
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ModernBannerCarousel(banners: List<Banner>, onClick: (Banner) -> Unit) {
    val actualCount = banners.size
    val isLooping = actualCount > 1

    // Small, fixed virtual count — plenty for looping, cheap to measure.
    // e.g. with 4 banners this gives 100 "laps" before it resets.
    val virtualPageCount = if (isLooping) actualCount * 25 else actualCount

    val startPage = if (isLooping) {
        // Start in the middle lap, aligned to a real banner index (page 0)
        val midLap = (virtualPageCount / actualCount) / 2
        midLap * actualCount
    } else 0

    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { virtualPageCount }
    )

    val currentRealIndex = pagerState.currentPage % actualCount

    LaunchedEffect(actualCount) {
        if (!isLooping) return@LaunchedEffect
        while (true) {
            delay(3000)
            val next = pagerState.currentPage + 1
            if (next < virtualPageCount - 1) {
                pagerState.animateScrollToPage(next)
            } else {
                // Near the end of our virtual range: jump back to an
                // aligned page instantly (same real banner, invisible to user),
                // then continue looping forward from there.
                pagerState.scrollToPage(startPage)
            }
        }
    }

    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) { page ->
            val banner = banners[page % actualCount]
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onClick(banner) },
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = banner.imageUrl,
                        contentDescription = "Banner Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Black.copy(alpha = 0.85f)
                                    ),
                                    startY = 80f
                                )
                            )
                    )

                    if (!banner.clickable) {

                    } else {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Gold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Gold)
                            ) {
                                Text(
                                    text = "🔥 Featured",
                                    color = Gold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Gold, CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Watch",
                                    color = Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(actualCount) { iteration ->
                val color = if (currentRealIndex == iteration) Gold else DarkElevated
                val width = if (currentRealIndex == iteration) 18.dp else 6.dp
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(width = width, height = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ModernOfferBanner(note: String, onClick: () -> Unit) {
    val trimmedNote = note.trim()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        DarkSurface,
                        Color(0xFF231B0C)
                    )
                )
            )
            .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {

        // Fully admin-controlled copy (settings/pricing.note). The app
            // never hardcodes offer text -- only this label and the button
            // around it are app-owned chrome.
            if (trimmedNote.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = trimmedNote,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(12.dp))

        }
    }


@Composable
private fun TrendingSection(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 16.dp)
                        .background(Gold, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "🔥 Trending Now",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${movies.size} Movies",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        // Plain grid, stacked in a Column -- intentionally NOT a
        // LazyRow/horizontal scroller.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            movies.chunked(3).forEach { rowMovies ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowMovies.forEach { movie ->
                        TrendingGridTile(
                            movie = movie,
                            onClick = { onMovieClick(movie) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Keep tile width consistent when the last row has
                    // fewer than 3 items.
                    repeat(3 - rowMovies.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendingGridTile(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurface)
        ) {
            AsyncImage(
                model = movie.thumbnailUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (movie.isFree) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(Gold.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "FREE",
                        color = Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            movie.badge?.let { badge ->
                Text(
                    text = badge,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = movie.title,
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ModernSectionRow(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 16.dp)
                        .background(Gold, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${movies.size} Movies",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(movies) { movie ->
                MoviePoster(movie = movie, onClick = { onMovieClick(movie) })
            }
        }
    }
}