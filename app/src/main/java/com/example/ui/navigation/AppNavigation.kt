package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
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
    const val STUDIO_STATUS = "studio_status"
    const val STUDIO_PREVIEW = "studio_preview"
    const val STUDIO_EDITOR = "studio_editor"
    const val STUDIO_STYLE_REFERENCE = "studio_style_reference"
    const val STUDIO_GRAPH = "studio_graph"
    const val COMPANION = "companion"
    const val KNOWLEDGE = "knowledge"
    const val IMPACT = "impact"
    const val JOURNEY = "journey"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val NOTIFICATIONS = "notifications"
    const val ADMIN = "admin"
}


@Composable
private fun rememberStudioViewModel(navController: NavController): StudioViewModel {
    // نحدّد scope الـ ViewModel إلى مدخل الرسم البياني الأب المشترك (STUDIO_GRAPH)
    // بدل مدخل الوجهة نفسها، حتى تتشارك كل شاشات الاستوديو نسخة ViewModel
    // واحدة (وإلا فُقد currentProject وحالة التصدير عند التنقل بين الشاشات).
    val parentEntry = remember(navController) {
        navController.getBackStackEntry(Routes.STUDIO_GRAPH)
    }
    val application = LocalContext.current.applicationContext as android.app.Application

    // StudioViewModel's constructor has extra parameters with default values
    // (repository, generationService, resourceProvider, renderService), so Kotlin
    // does NOT generate a plain single-Application constructor. The default
    // AndroidViewModelFactory relies on reflection to find that constructor and
    // fails with NoSuchMethodException, crashing the app. We provide an explicit
    // factory instead so the defaults are applied normally in Kotlin code.
    return remember(parentEntry, application) {
        ViewModelProvider(
            parentEntry,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return StudioViewModel(application) as T
                }
            }
        ).get(StudioViewModel::class.java)
    }
}

@Composable
private fun rememberKnowledgeViewModel(): com.example.ui.screens.knowledge.KnowledgeViewModel {
    val owner = checkNotNull(LocalViewModelStoreOwner.current) {
        "A ViewModelStoreOwner is required for KnowledgeViewModel"
    }
    val application = LocalContext.current.applicationContext as android.app.Application
    // KnowledgeViewModel لها معامل repository افتراضي؛ الاعتماد على الانعكاس
    // الافتراضي عبر AndroidViewModelFactory يفشل بـ NoSuchMethodException.
    return remember(owner, application) {
        ViewModelProvider(
            owner,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return com.example.ui.screens.knowledge.KnowledgeViewModel(application) as T
                }
            }
        ).get(com.example.ui.screens.knowledge.KnowledgeViewModel::class.java)
    }
}

@Composable
private fun rememberImpactViewModel(): com.example.ui.screens.impact.ImpactViewModel {
    val owner = checkNotNull(LocalViewModelStoreOwner.current) {
        "A ViewModelStoreOwner is required for ImpactViewModel"
    }
    val application = LocalContext.current.applicationContext as android.app.Application
    // ImpactViewModel لها معامل repository افتراضي؛ الاعتماد على الانعكاس
    // الافتراضي عبر AndroidViewModelFactory يفشل بـ NoSuchMethodException.
    return remember(owner, application) {
        ViewModelProvider(
            owner,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return com.example.ui.screens.impact.ImpactViewModel(application) as T
                }
            }
        ).get(com.example.ui.screens.impact.ImpactViewModel::class.java)
    }
}

@Composable
private fun rememberAuthViewModel(): com.example.ui.AuthViewModel {
    val owner = checkNotNull(LocalViewModelStoreOwner.current) {
        "A ViewModelStoreOwner is required for AuthViewModel"
    }
    val application = LocalContext.current.applicationContext as android.app.Application
    // AuthViewModel لها معاملات repositories افتراضية؛ الاعتماد على الانعكاس
    // الافتراضي عبر AndroidViewModelFactory يفشل بـ NoSuchMethodException.
    return remember(owner, application) {
        ViewModelProvider(
            owner,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return com.example.ui.AuthViewModel(application) as T
                }
            }
        ).get(com.example.ui.AuthViewModel::class.java)
    }
}

@Composable
private fun rememberCompanionViewModel(): com.example.ui.CompanionViewModel {
    val owner = checkNotNull(LocalViewModelStoreOwner.current) {
        "A ViewModelStoreOwner is required for CompanionViewModel"
    }
    val application = LocalContext.current.applicationContext as android.app.Application
    // CompanionViewModel لها معامل repository افتراضي؛ الاعتماد على الانعكاس
    // الافتراضي عبر AndroidViewModelFactory يفشل بـ NoSuchMethodException.
    return remember(owner, application) {
        ViewModelProvider(
            owner,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return com.example.ui.CompanionViewModel(application) as T
                }
            }
        ).get(com.example.ui.CompanionViewModel::class.java)
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String
) {
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

        navigation(startDestination = Routes.STUDIO, route = Routes.STUDIO_GRAPH) {
            composable(Routes.STUDIO) {
                val studioViewModel = rememberStudioViewModel(navController)
                StudioHomeScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    viewModel = studioViewModel
                )
            }
            composable(Routes.STUDIO_CREATE) {
                val studioViewModel = rememberStudioViewModel(navController)
                CreateVideoProjectScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    viewModel = studioViewModel
                )
            }
            composable(Routes.STUDIO_ANALYSIS) {
                val studioViewModel = rememberStudioViewModel(navController)
                IdeaAnalysisScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    viewModel = studioViewModel
                )
            }
            composable(Routes.STUDIO_PLAN) {
                val studioViewModel = rememberStudioViewModel(navController)
                VideoPlanScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    viewModel = studioViewModel
                )
            }
            composable(Routes.STUDIO_STATUS) {
                val studioViewModel = rememberStudioViewModel(navController)
                GenerationStatusScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    viewModel = studioViewModel
                )
            }
            composable(Routes.STUDIO_PREVIEW) {
                val studioViewModel = rememberStudioViewModel(navController)
                VideoPreviewScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    viewModel = studioViewModel
                )
            }
            composable(Routes.STUDIO_EDITOR) {
                val studioViewModel = rememberStudioViewModel(navController)
                VideoEditorScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    viewModel = studioViewModel
                )
            }

            composable(Routes.STUDIO_STYLE_REFERENCE) {
                val studioViewModel = rememberStudioViewModel(navController)
                com.example.ui.screens.studio.StyleReferenceScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    viewModel = studioViewModel
                )
            }
        }


        composable(Routes.COMPANION) {
            CompanionScreen(
                onBack = { navController.popBackStack() },
                companionViewModel = rememberCompanionViewModel()
            )
        }

        composable(Routes.KNOWLEDGE) {
            val knowledgeViewModel = rememberKnowledgeViewModel()
            com.example.ui.screens.knowledge.KnowledgeHomeScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                viewModel = knowledgeViewModel
            )
        }
        
        composable("knowledge_article/{articleId}") { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: return@composable
            val knowledgeViewModel = rememberKnowledgeViewModel()
            com.example.ui.screens.knowledge.KnowledgeArticleScreen(
                articleId = articleId,
                navController = navController,
                onBack = { navController.popBackStack() },
                viewModel = knowledgeViewModel
            )
        }

        composable(Routes.IMPACT) {
            val impactViewModel = rememberImpactViewModel()
            com.example.ui.screens.impact.ImpactHomeScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                viewModel = impactViewModel
            )
        }
        
        composable("impact_detail/{initiativeId}") { backStackEntry ->
            val initiativeId = backStackEntry.arguments?.getString("initiativeId") ?: return@composable
            val impactViewModel = rememberImpactViewModel()
            com.example.ui.screens.impact.ImpactDetailScreen(
                initiativeId = initiativeId,
                navController = navController,
                onBack = { navController.popBackStack() },
                viewModel = impactViewModel
            )
        }

        composable(Routes.JOURNEY) {
            JourneyScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) {
            val authViewModel = rememberAuthViewModel()
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onNavigateToSettings = {
                    NavigationManager.navigateSingleTop(navController, Routes.SETTINGS)
                },
                onNavigateToAdmin = {
                    val user = authViewModel.uiState.value.user
                    if (user != null && (user.role == com.example.domain.model.UserRole.DEVELOPER || user.role == com.example.domain.model.UserRole.SUPER_ADMIN)) {
                        NavigationManager.navigateSingleTop(navController, Routes.ADMIN)
                    }
                },
                authViewModel = authViewModel
            )
        }
        
        composable(Routes.ADMIN) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val authViewModel = rememberAuthViewModel()
            val user = authViewModel.uiState.value.user
            
            if (user != null && (user.role == com.example.domain.model.UserRole.DEVELOPER || user.role == com.example.domain.model.UserRole.SUPER_ADMIN)) {
                val adminRepo = remember { com.example.data.repository.AdminRepositoryImpl(context) }
                val settingsRepo = remember { com.example.data.local.SettingsRepository(context) }
                val adminViewModel: com.example.ui.screens.admin.DeveloperDashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return com.example.ui.screens.admin.DeveloperDashboardViewModel(adminRepo, settingsRepo, user.uid, user.role) as T
                        }
                    }
                )
                com.example.ui.screens.admin.DeveloperDashboardScreen(
                    viewModel = adminViewModel,
                    onBack = { navController.popBackStack() }
                )
            } else {
                androidx.compose.material3.Text("Access Denied", modifier = Modifier.fillMaxSize().padding(16.dp))
            }
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
