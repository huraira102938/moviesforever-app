package com.moviesforever.app.ui.screen.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.data.model.Category
import com.moviesforever.app.data.model.Genre
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.ui.components.MoviePoster
import com.moviesforever.app.ui.theme.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

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

    // If initial category changes (nav arg), update
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
        Spacer(Modifier.height(12.dp))
        Text(
            text = categories.find { it.id == selectedCategoryId }?.name ?: "Browse",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        // Category chip row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategoryId == cat.id,
                    onClick = {
                        selectedCategoryId = cat.id
                        selectedGenres = emptySet()
                    },
                    label = { Text(cat.name) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Genre filter chips within this category
        if (genres.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(genres.size) { i ->
                    val g = genres[i]
                    FilterChip(
                        selected = selectedGenres.contains(g.id),
                        onClick = {
                            selectedGenres = if (selectedGenres.contains(g.id)) {
                                selectedGenres - g.id
                            } else {
                                selectedGenres + g.id
                            }
                        },
                        label = { Text(g.name) }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No movies in this category yet",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { movie ->
                    MoviePoster(movie = movie, onClick = { onMovieClick(movie) })
                }
            }
        }
    }
}
