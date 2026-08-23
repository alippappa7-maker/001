package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.navigation.NavigationManager
import com.example.ui.navigation.Routes
import com.example.ui.theme.QabasDimens
import com.example.ui.theme.QabasThemeTokens

/**
 * Standard Bottom Navigation bar for QABAS main screens.
 */
@Composable
fun QabasBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val colors = QabasThemeTokens.colors

    NavigationBar(
        containerColor = colors.surface.copy(alpha = 0.95f),
        tonalElevation = 0.dp,
        modifier = Modifier
            .testTag("bottom_navigation_bar")
            .border(
                width = QabasDimens.BorderThin,
                color = colors.surfaceBorder,
                shape = RoundedCornerShape(
                    topStart = QabasDimens.RadiusMedium,
                    topEnd = QabasDimens.RadiusMedium
                )
            )
    ) {
        NavigationBarItem(
            selected = currentRoute == Routes.HOME,
            onClick = { onNavigate(Routes.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.bottom_nav_home)) },
            label = { Text(stringResource(id = R.string.bottom_nav_home), fontSize = 10.sp) },
            modifier = Modifier.testTag("nav_item_home"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colors.gold,
                selectedTextColor = colors.gold,
                indicatorColor = colors.gold.copy(alpha = 0.15f),
                unselectedIconColor = colors.textMuted,
                unselectedTextColor = colors.textMuted
            )
        )
        NavigationBarItem(
            selected = currentRoute == Routes.MIHRAB,
            onClick = { onNavigate(Routes.MIHRAB) },
            icon = { Icon(Icons.Default.Mosque, contentDescription = stringResource(R.string.bottom_nav_mihrab)) },
            label = { Text(stringResource(id = R.string.bottom_nav_mihrab), fontSize = 10.sp) },
            modifier = Modifier.testTag("nav_item_mihrab"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colors.gold,
                selectedTextColor = colors.gold,
                indicatorColor = colors.gold.copy(alpha = 0.15f),
                unselectedIconColor = colors.textMuted,
                unselectedTextColor = colors.textMuted
            )
        )
        NavigationBarItem(
            selected = currentRoute == Routes.COMPASS,
            onClick = { onNavigate(Routes.COMPASS) },
            icon = { Icon(Icons.Default.Explore, contentDescription = stringResource(R.string.bottom_nav_compass)) },
            label = { Text(stringResource(id = R.string.bottom_nav_compass), fontSize = 10.sp) },
            modifier = Modifier.testTag("nav_item_compass"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colors.gold,
                selectedTextColor = colors.gold,
                indicatorColor = colors.gold.copy(alpha = 0.15f),
                unselectedIconColor = colors.textMuted,
                unselectedTextColor = colors.textMuted
            )
        )
        NavigationBarItem(
            selected = currentRoute == Routes.JOURNEY,
            onClick = { onNavigate(Routes.JOURNEY) },
            icon = { Icon(Icons.Default.Route, contentDescription = stringResource(R.string.bottom_nav_journey)) },
            label = { Text(stringResource(id = R.string.bottom_nav_journey), fontSize = 10.sp) },
            modifier = Modifier.testTag("nav_item_journey"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colors.gold,
                selectedTextColor = colors.gold,
                indicatorColor = colors.gold.copy(alpha = 0.15f),
                unselectedIconColor = colors.textMuted,
                unselectedTextColor = colors.textMuted
            )
        )
    }
}
