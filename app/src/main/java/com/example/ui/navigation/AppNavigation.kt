package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ui.screens.*
import com.example.ui.screens.studio.*

object Routes {
    const val WELCOME = "welcome"
    const val HOME = "home"
    const val COMPASS = "compass"
    const val MIHRAB = "mihrab"
    const val STUDIO = "studio"
    const val STUDIO_CREATE = "studio_create"
    const val STUDIO_ANALYSIS = "studio_analysis"
    const val STUDIO_PLAN = "studio_plan"
    const val COMPANION = "companion"
    const val KNOWLEDGE = "knowledge"
    const val IMPACT = "impact"
    const val JOURNEY = "journey"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val NOTIFICATIONS = "notifications"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String
) {
    val studioViewModel: StudioViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            ) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
            ) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            fadeIn(
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            ) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            fadeOut(
                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
            ) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
            )
        }
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(navController = navController)
        }

        composable(Routes.COMPASS) {
            CompassScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MIHRAB) {
            MihrabScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.STUDIO) {
            StudioHomeScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                viewModel = studioViewModel
            )
        }
        composable(Routes.STUDIO_CREATE) {
            CreateVideoProjectScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                viewModel = studioViewModel
            )
        }
        composable(Routes.STUDIO_ANALYSIS) {
            IdeaAnalysisScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                viewModel = studioViewModel
            )
        }
        composable(Routes.STUDIO_PLAN) {
            VideoPlanScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                viewModel = studioViewModel
            )
        }

        composable(Routes.COMPANION) {
            CompanionScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.KNOWLEDGE) {
            KnowledgeScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.IMPACT) {
            ImpactScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.JOURNEY) {
            JourneyScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onNavigateToSettings = {
                    NavigationManager.navigateSingleTop(navController, Routes.SETTINGS)
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToNotifications = {
                    NavigationManager.navigateSingleTop(navController, Routes.NOTIFICATIONS)
                }
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
