package com.moviesforever.app.ui.screen.search

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.data.model.Genre
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.data.model.TMDB_ID_PREFIX
import com.moviesforever.app.ui.components.MoviePoster
import com.moviesforever.app.ui.theme.*

@Composable
fun SearchScreen(
    movies: List<Movie>,
    genres: List<Genre>, // Kept in signature for NavHost compatibility, but unused
    onMovieClick: (Movie) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("") }

    // Filter exclusively for TMDB items (Cloudflare R2 storage excluded)
    val tmdbOnlyMovies = remember(movies) {
        movies.filter { it.id.startsWith(TMDB_ID_PREFIX) }
    }

    val filtered = remember(tmdbOnlyMovies, query, selectedCategory) {
        tmdbOnlyMovies.filter { m ->
            val matchQuery = query.isBlank() || m.title.contains(query, ignoreCase = true)

            // Robust category matching for string variations
            val matchCat = selectedCategory.isBlank() || when (selectedCategory.lowercase()) {
                "south" -> m.category.contains("south", ignoreCase = true) || m.genres.any { it.contains("south", ignoreCase = true) }
                else -> m.category.equals(selectedCategory, ignoreCase = true)
            }

            matchQuery && matchCat && !m.paused
        }
    }

    val randomizedResults = filtered

    val visibleCategories = listOf("Bollywood", "Hollywood", "South", "Punjabi", "Animation", "Anime")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 18.dp)
                    .background(Gold, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(8.dp))
            Text("Search Catalog", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search movies, anime, shows...", color = TextMuted, fontSize = 13.sp) },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear search",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = Gold,
                unfocusedBorderColor = DarkElevated,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = Gold
            )
        )

        Spacer(Modifier.height(12.dp))

        // Category Filter Chips only (Sub-categories/genres completely removed)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                ModernFilterChip(
                    selected = selectedCategory.isBlank(),
                    label = "All",
                    onClick = { selectedCategory = "" }
                )
            }
            items(visibleCategories) { cat ->
                ModernFilterChip(
                    selected = selectedCategory.equals(cat, ignoreCase = true),
                    label = cat,
                    onClick = { selectedCategory = if (selectedCategory.equals(cat, ignoreCase = true)) "" else cat }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (randomizedResults.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Movie,
                        contentDescription = null,
                        tint = DarkElevated,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("No movies found", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text("Try checking your keywords or filters", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(randomizedResults) { movie ->
                    MoviePoster(movie = movie, onClick = { onMovieClick(movie) })
                }
            }
        }
    }
}

@Composable
private fun ModernFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) Gold else DarkSurface)
            .border(1.dp, if (selected) Gold else DarkElevated, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Black else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}