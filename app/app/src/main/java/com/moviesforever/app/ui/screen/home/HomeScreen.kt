package com.moviesforever.app.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import com.moviesforever.app.data.model.FeedRow
import com.moviesforever.app.data.model.HomeFeed
import com.moviesforever.app.ui.tv.tvFocusHighlight
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.data.model.PricingSettings
import com.moviesforever.app.ui.theme.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.moviesforever.app.ui.components.MoviePoster
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    feed: HomeFeed,
    pricing: PricingSettings,
    isUnlocked: Boolean,
    onBannerClick: (Banner) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onShowAllClick: (String) -> Unit,
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

        // Hero Banner Carousel (tap action comes from the admin panel: movie / website / WhatsApp / channel)
        if (feed.banners.isNotEmpty()) {
            item {
                ModernBannerCarousel(banners = feed.banners, onBannerClick = onBannerClick)
                Spacer(Modifier.height(16.dp))
            }
        }

        // FREE section: shown first, only to people who have NOT paid. Once premium, it disappears.
        if (!isUnlocked && feed.free.isNotEmpty()) {
            item(key = "free_section") {
                FreeSection(movies = feed.free, onMovieClick = onMovieClick)
                Spacer(Modifier.height(12.dp))
            }
        }

        if (!isUnlocked && pricing.note.isNotBlank()) {
            item {
                ModernOfferBanner(
                    note = pricing.note,
                    onClick = onUnlockClick
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        if (feed.trending.isNotEmpty()) {
            item(key = "shelf_trending") {
                TrendingSection(
                    movies = feed.trending,
                    onMovieClick = onMovieClick
                )
            }
        }

        if (feed.genreRows.isNotEmpty()) {
            item(key = "genre_pills") {
                GenrePills(rows = feed.genreRows, onClick = { onShowAllClick(it.id) })
            }
        }

        items(feed.homeRows, key = { it.id }) { row ->
            ModernSectionRow(
                title = row.title,
                movies = row.movies,
                onMovieClick = onMovieClick,
                onShowAllClick = { onShowAllClick(row.id) }
            )
        }
    }
}

@Composable
private fun GenrePills(rows: List<FeedRow>, onClick: (FeedRow) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(rows, key = { it.id }) { row ->
            Text(
                text = row.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .tvFocusHighlight(RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkElevated, RoundedCornerShape(20.dp))
                    .clickable { onClick(row) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ModernBannerCarousel(banners: List<Banner>, onBannerClick: (Banner) -> Unit) {
    val actualCount = banners.size
    val isLooping = actualCount > 1
    val virtualPageCount = if (isLooping) actualCount * 25 else actualCount
    val startPage = if (isLooping) {
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
                    .clickable(enabled = banner.isTappable()) { onBannerClick(banner) },
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
        if (trimmedNote.isNotEmpty()) {
            Text(
                text = trimmedNote,
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String? = null) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 16.dp)
                .background(Gold, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(text = title, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        subtitle?.let {
            Spacer(Modifier.width(8.dp))
            Text(text = it, color = TextMuted, fontSize = 13.sp)
        }
    }
}

/** A numbered Top 10 row: each big number sits fully beside its poster. */
@Composable
private fun TrendingSection(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        SectionTitle("Top 10 This Week")
        Spacer(Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            itemsIndexed(movies, key = { _, movie -> movie.id }) { index, movie ->
                TrendingTile(rank = index + 1, movie = movie, onClick = { onMovieClick(movie) })
            }
        }
    }
}

@Composable
private fun TrendingTile(rank: Int, movie: Movie, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .tvFocusHighlight(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "$rank",
            color = Gold,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 72.sp,
            maxLines = 1,
            softWrap = false
        )
        Spacer(Modifier.width(4.dp))
        AsyncImage(
            model = movie.thumbnailUrl,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(120.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurface)
        )
    }
}

/** Free movies sit in a normal shelf; the Free tag on each poster says the rest. */
@Composable
private fun FreeSection(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        SectionTitle("Watch Free", subtitle = "No payment needed")
        Spacer(Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(movies, key = { it.id }) { movie ->
                MoviePoster(movie = movie, onClick = { onMovieClick(movie) })
            }
        }
    }
}

@Composable
private fun ModernSectionRow(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    onShowAllClick: () -> Unit
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

            TextButton(onClick = onShowAllClick) {
                Text(
                    text = "Show All",
                    color = Gold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(movies, key = { it.id }) { movie ->
                MoviePoster(movie = movie, onClick = { onMovieClick(movie) })
            }
        }
    }
}