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
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.data.model.PricingSettings
import com.moviesforever.app.ui.theme.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.moviesforever.app.ui.components.MoviePoster
import com.moviesforever.app.ui.components.UrduText
import com.moviesforever.app.ui.viewmodel.CatalogueViewModel
import kotlinx.coroutines.delay

/**
 * Categories that are NOT shown as shelves on the Home screen (Punjabi, South).
 * Their movies stay in the catalogue: Search and the category chips still show them.
 */
private val HIDDEN_HOME_CATEGORY_IDS = setOf(
    "hpyasXGJKaBR9g0RWH3P", // Punjabi
    "ZeRQutD2FN0BS9qTyeTP"  // South
)

@Composable
fun HomeScreen(
    banners: List<Banner>,
    pricing: PricingSettings,
    isUnlocked: Boolean,
    onBannerClick: (Banner) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onShowAllClick: (String) -> Unit,
    onUnlockClick: () -> Unit,
    onAvatarClick: () -> Unit,
    catalogueViewModel: CatalogueViewModel = hiltViewModel()
) {
    // Shelves come from movies.json (one per category, in the admin's category order),
    // shuffled once per app launch (stable until the app is closed).
    val catalogue by catalogueViewModel.state.collectAsState()

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

        // Hero Banner Carousel (Unclickable)
        if (banners.isNotEmpty()) {
            item {
                ModernBannerCarousel(banners = banners)
                Spacer(Modifier.height(16.dp))
            }
        }

        // FREE section: shown first, only to people who have NOT paid. Once premium, it disappears.
        val freeMovies = catalogue.all.filter { it.isFree }
        if (!isUnlocked && freeMovies.isNotEmpty()) {
            item(key = "free_section") {
                FreeSection(movies = freeMovies, onMovieClick = onMovieClick)
                Spacer(Modifier.height(12.dp))
            }
        }

        if (!isUnlocked) {
            item {
                ModernOfferBanner(
                    note = pricing.note,
                    onClick = onUnlockClick
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        // One shelf per category: shuffled preview (max 5 items) and a "Show All" button
        catalogue.shelves.filter { it.categoryId !in HIDDEN_HOME_CATEGORY_IDS }.forEach { shelf ->
            item(key = "shelf_${shelf.categoryId}") {
                ModernSectionRow(
                    title = shelf.title,
                    movies = shelf.movies.take(5),
                    onMovieClick = onMovieClick,
                    onShowAllClick = { onShowAllClick(shelf.categoryId) }
                )
            }
        }

        if (catalogue.trending.isNotEmpty()) {
            item(key = "shelf_trending") {
                TrendingSection(
                    movies = catalogue.trending,
                    onMovieClick = onMovieClick
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ModernBannerCarousel(banners: List<Banner>) {
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
                modifier = Modifier.fillMaxSize(),
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
        }

        Spacer(Modifier.height(12.dp))

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
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = movie.title,
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

/**
 * Green "FREE" shelf. Green background + green border + a big FREE tag + an Urdu line, so even a
 * person who cannot read English understands: these movies are free, everything else is paid.
 */
@Composable
private fun FreeSection(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Success.copy(alpha = 0.10f))
            .border(1.5.dp, Success, RoundedCornerShape(18.dp))
            .padding(vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Success, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "FREE",
                    color = Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Free to Watch",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            UrduText(
                text = "مفت",
                color = Success,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(6.dp))

        UrduText(
            text = "یہ فلمیں بالکل مفت ہیں۔ باقی تمام فلمیں دیکھنے کے لیے ادائیگی کرنا ضروری ہے۔",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        )

        Spacer(Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
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