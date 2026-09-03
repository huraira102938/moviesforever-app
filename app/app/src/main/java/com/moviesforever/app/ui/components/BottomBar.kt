package com.moviesforever.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moviesforever.app.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.background

data class BottomTab(
    val label: String,
    val icon: ImageVector
)

object BottomTabs {
    val Home = BottomTab("Home", Icons.Filled.Home)
    val Search = BottomTab("Search", Icons.Filled.Search)
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
        modifier = Modifier.height(60.dp)
    ) {
        BottomTabs.all.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = currentTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    val isSelected = currentTab == index
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (isSelected) Gold else TextMuted
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        color = if (currentTab == index) Gold else TextMuted,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Gold.copy(alpha = 0.15f),
                    selectedIconColor = Gold,
                    selectedTextColor = Gold,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}
