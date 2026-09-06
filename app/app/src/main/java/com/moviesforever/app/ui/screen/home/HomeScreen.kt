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

        // High-Converting Unlock Banner
        if (!isUnlocked) {
            item {
                ModernUnlockBanner(
                    price = pricing.standardPrice,
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
                    ModernSectionRow(
                        title = SectionLabels.label(section),
                        movies = sectionMovies,
                        onMovieClick = onMovieClick
                    )
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
    }
}
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ModernBannerCarousel(banners: List<Banner>, onClick: (Banner) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Auto-scroll logic: Loops endlessly every 3 seconds
    LaunchedEffect(banners.size) {
        if (banners.size > 1) {
            while (true) {
                delay(3000) // Wait 3 seconds
                val nextPage = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(nextPage)
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
            val banner = banners[page]
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

                    // Gradient overlay for bottom text contrast
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
                        // Badge & Watch Action Callout
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

        // Pager Indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(banners.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) Gold else DarkElevated
                val width = if (pagerState.currentPage == iteration) 18.dp else 6.dp
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
private fun ModernUnlockBanner(price: Double, onClick: () -> Unit) {
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
        Column {
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Unlock Lifetime Pass",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pay once PKR ${price.toInt()} • Watch forever",
                        color = GoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = Gold,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Get Pass",
                            color = Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
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