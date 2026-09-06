package com.moviesforever.app.ui.screen.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.data.model.Category
import com.moviesforever.app.data.model.Genre
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.ui.components.MoviePoster
import com.moviesforever.app.ui.theme.*

@Composable
fun CategoryBrowseScreen(
    initialCategoryId: String?,
    categories: List<Category>,
    genres: List<Genre>,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf(initialCategoryId ?: categories.firstOrNull()?.id ?: "") }
    var selectedGenres by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(initialCategoryId) {
        if (initialCategoryId != null) {
            selectedCategoryId = initialCategoryId
            selectedGenres = emptySet()
        }
    }

    val catMovies = movies.filter { it.category == selectedCategoryId && !it.paused }
    val filtered = catMovies.filter { m ->
        selectedGenres.isEmpty() || m.genres.any { selectedGenres.contains(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(14.dp))

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
                    text = categories.find { it.id == selectedCategoryId }?.name ?: "Browse",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${filtered.size} Movies",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        // Category Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                CategoryChip(
                    selected = selectedCategoryId == cat.id,
                    label = cat.name,
                    onClick = {
                        selectedCategoryId = cat.id
                        selectedGenres = emptySet()
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Genre Filter Chips
        if (genres.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(genres) { g ->
                    CategoryChip(
                        selected = selectedGenres.contains(g.id),
                        label = g.name,
                        onClick = {
                            selectedGenres = if (selectedGenres.contains(g.id)) {
                                selectedGenres - g.id
                            } else {
                                selectedGenres + g.id
                            }
                        }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(6.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Category,
                        contentDescription = null,
                        tint = DarkElevated,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No movies in this category yet",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Check back soon for new additions",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { movie ->
                    MoviePoster(movie = movie, onClick = { onMovieClick(movie) })
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) Gold else DarkSurface)
            .border(
                1.dp,
                if (selected) Gold else DarkElevated,
                CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Black else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}