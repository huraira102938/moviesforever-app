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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.semantics.Role
import com.moviesforever.app.ui.tv.tvFocusHighlight

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

/** Four equal tabs on one line: same icon size, same label position, gold only on the open tab. */
@Composable
fun MoviesBottomBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(BlackSurface)) {
        HorizontalDivider(color = DarkElevated)
        Row(modifier = Modifier.fillMaxWidth().height(64.dp)) {
            BottomTabs.all.forEachIndexed { index, tab ->
                val isSelected = currentTab == index
                val tint = if (isSelected) Gold else TextMuted
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .tvFocusHighlight(RoundedCornerShape(12.dp), focusedScale = 1f)
                        .selectable(selected = isSelected, role = Role.Tab, onClick = { onTabSelected(index) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 56.dp, height = 30.dp)
                            .background(
                                if (isSelected) Gold.copy(alpha = 0.15f) else Color.Transparent,
                                RoundedCornerShape(15.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = tab.label,
                        color = tint,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
