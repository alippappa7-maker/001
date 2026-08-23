package com.example.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Handles navigation safely preventing multiple clicks and duplicated destinations in the backstack.
 */
object NavigationManager {

    /**
     * Safely navigate to a target destination:
     * - Prevents duplicate pushes of the same screen when tapped repeatedly (launchSingleTop = true)
     * - Preserves and restores screen state where applicable
     */
    fun navigateSingleTop(navController: NavController, route: String) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (currentRoute == route) return

        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
        }
    }

    /**
     * Safely navigate for bottom navigation items:
     * - Pops up to the start destination of the graph to avoid building a huge backstack
     * - Restores previous state when reselecting a tab item
     * - Avoids multiple copies of the same destination when re-selecting the same item
     */
    fun navigateBottomTab(navController: NavController, route: String) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (currentRoute == route) return

        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
