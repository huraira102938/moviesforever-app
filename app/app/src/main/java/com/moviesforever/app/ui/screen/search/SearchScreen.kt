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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
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
import com.moviesforever.app.data.model.Category
import com.moviesforever.app.data.model.Genre
import com.moviesforever.app.data.model.Movie
import com.moviesforever.app.ui.components.MoviePoster
import com.moviesforever.app.ui.theme.*

@Composable
fun SearchScreen(
    movies: List<Movie>,
    /** Categories that exist in movies.json (id = Firestore category id), in the admin's order. */
    categories: List<Category>,
    genres: List<Genre>, // Kept in signature for NavHost compatibility, but unused
    onMovieClick: (Movie) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    // Holds a category id ("" = All).
    var selectedCategory by rememberSaveable { mutableStateOf("") }

    val filtered = remember(movies, query, selectedCategory) {
        movies.filter { m ->
            val matchQuery = query.isBlank() || m.title.contains(query, ignoreCase = true)
            val matchCat = selectedCategory.isBlank() || m.category == selectedCategory
            matchQuery && matchCat && !m.paused
        }
    }

    val randomizedResults = filtered

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

        var filterMenuOpen by remember { mutableStateOf(false) }
        val selectedCategoryName = categories.find { it.id == selectedCategory }?.name

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            modifier = Modifier.weight(1f),
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

        Spacer(Modifier.width(8.dp))

        // Filter button: replaces the old row of category chips, which took a lot of space.
        // Gold-filled while a category is chosen, so it is clear a filter is on.
        Box {
            val filterOn = selectedCategory.isNotBlank()
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (filterOn) Gold else DarkSurface)
                    .border(1.dp, if (filterOn) Gold else DarkElevated, RoundedCornerShape(14.dp))
                    .clickable { filterMenuOpen = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "Filter",
                    tint = if (filterOn) Black else Gold,
                    modifier = Modifier.size(26.dp)
                )
            }

            DropdownMenu(
                expanded = filterMenuOpen,
                onDismissRequest = { filterMenuOpen = false },
                modifier = Modifier.background(DarkSurface)
            ) {
                FilterMenuItem(
                    label = "All",
                    selected = selectedCategory.isBlank(),
                    onClick = { selectedCategory = ""; filterMenuOpen = false }
                )
                categories.forEach { cat ->
                    FilterMenuItem(
                        label = cat.name,
                        selected = selectedCategory == cat.id,
                        onClick = { selectedCategory = cat.id; filterMenuOpen = false }
                    )
                }
            }
        }
        }

        // When a filter is on, show it as one small removable tag (instead of a whole chip row).
        if (selectedCategoryName != null) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.15f))
                    .border(1.dp, Gold, CircleShape)
                    .clickable { selectedCategory = "" }
                    .padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedCategoryName, color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "Remove filter",
                    tint = Gold,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

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
private fun FilterMenuItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = label,
                color = if (selected) Gold else TextPrimary,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        trailingIcon = {
            if (selected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
            }
        },
        onClick = onClick
    )
}
