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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.data.model.Category
import com.moviesforever.app.data.model.Genre
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.ui.components.MoviePoster
import com.moviesforever.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    movies: List<Movie>,
    categories: List<Category>,
    genres: List<Genre>,
    onMovieClick: (Movie) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var selectedGenres by rememberSaveable { mutableStateOf(setOf<String>()) }
    var freeOnly by rememberSaveable { mutableStateOf(false) }

    val filtered = movies.filter { m ->
        val matchQuery = query.isBlank() || m.title.contains(query, ignoreCase = true)
        val matchCat = selectedCategory.isBlank() || m.category == selectedCategory
        val matchGenre = selectedGenres.isEmpty() || m.genres.any { selectedGenres.contains(it) }
        val matchFree = !freeOnly || m.isFree
        matchQuery && matchCat && matchGenre && matchFree && !m.paused
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Compact Top Bar with Integrated "Free Only" Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 18.dp)
                        .background(Gold, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text("Search", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            // Compact Free Only Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Free only", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.width(6.dp))
                Switch(
                    checked = freeOnly,
                    onCheckedChange = { freeOnly = it },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Gold,
                        checkedThumbColor = Black,
                        uncheckedTrackColor = DarkElevated,
                        uncheckedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Search Bar with leading search icon and trailing clear button
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search movies, shows...", color = TextMuted, fontSize = 13.sp) },
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

        Spacer(Modifier.height(8.dp))

        // Category Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                ModernFilterChip(
                    selected = selectedCategory.isBlank(),
                    label = "All",
                    onClick = { selectedCategory = "" }
                )
            }
            items(categories) { cat ->
                ModernFilterChip(
                    selected = selectedCategory == cat.id,
                    label = cat.name,
                    onClick = { selectedCategory = if (selectedCategory == cat.id) "" else cat.id }
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // Genre Filter Chips
        if (genres.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(genres) { g ->
                    ModernFilterChip(
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
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(4.dp))

        if (filtered.isEmpty()) {
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
                    Text("Try adjusting your filters or search keywords", color = TextMuted, fontSize = 12.sp)
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
                items(filtered) { movie ->
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
            .border(
                1.dp,
                if (selected) Gold else DarkElevated,
                CircleShape
            )
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