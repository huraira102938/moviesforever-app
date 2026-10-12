package com.moviesforever.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

data class BottomTab(
    val label: String,
    val icon: ImageVector
)

object BottomTabs {
    val Home = BottomTab("Home", Icons.Filled.Home)
    // "Library" icon: a stack of videos. Reads as "all the movies are here" even to someone who cannot read.
    val Search = BottomTab("All Movies", Icons.Filled.VideoLibrary)
    val Downloads = BottomTab("Downloads", Icons.Filled.Download)
    val Profile = BottomTab("Profile", Icons.Filled.Person)
    val all = listOf(Home, Search, Downloads, Profile)
}

/**
 * Golden bottom navigation bar (Netflix-style, tabs at bottom).
 */
@Composable
fun MoviesBottomBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = BlackSurface,
        contentColor = TextSecondary,
        modifier = Modifier.height(66.dp)
    ) {
        BottomTabs.all.forEachIndexed { index, tab ->
            // Tab 1 is the movie library / search. It is drawn a little bigger, inside a circle, so
            // it stands out as "everything is in here". Gold only while it is selected.
            val isLibrary = index == 1
            val isSelected = currentTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    if (isLibrary) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                // Selected: gold. Not selected: a quiet light-grey disc (no gold), so the
                                // black icon on top can still be seen on the dark bar.
                                .background(if (isSelected) Gold else TextSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = Black, // black whether selected or not
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (isSelected) Gold else TextMuted
                        )
                    }
                },
                label = {
                    Text(
                        text = tab.label,
                        // Same as the other tabs: gold only when selected, never bold.
                        color = if (isSelected) Gold else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = if (isLibrary) Color.Transparent else Gold.copy(alpha = 0.15f),
                    selectedIconColor = Gold,
                    selectedTextColor = Gold,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}