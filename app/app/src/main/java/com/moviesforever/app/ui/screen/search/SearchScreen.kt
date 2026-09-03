package com.moviesforever.app.ui.screen.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

    val genreNames = genres.associate { it.id to it.name }
    val catNames = categories.associate { it.id to it.name }

    // Filter logic
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
        Spacer(Modifier.height(12.dp))
        Text("Search", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search movies…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold,
                unfocusedBorderColor = DarkElevated,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedPlaceholderColor = TextMuted,
                unfocusedPlaceholderColor = TextMuted
            )
        )

        Spacer(Modifier.height(12.dp))

        // Category chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedCategory.isBlank(),
                    onClick = { selectedCategory = "" },
                    label = { Text("All") }
                )
            }
            items(categories.size) { i ->
                val c = categories[i]
                FilterChip(
                    selected = selectedCategory == c.id,
                    onClick = { selectedCategory = if (selectedCategory == c.id) "" else c.id },
                    label = { Text(c.name) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Genre chips
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

        // Free only toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Free only", color = TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = freeOnly,
                onCheckedChange = { freeOnly = it },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Gold,
                    checkedThumbColor = Black
                )
            )
            Spacer(Modifier.weight(1f))
            Text("${filtered.size} movies", color = TextMuted, fontSize = 12.sp)
        }

        Spacer(Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No movies found", color = TextMuted, fontSize = 14.sp)
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
