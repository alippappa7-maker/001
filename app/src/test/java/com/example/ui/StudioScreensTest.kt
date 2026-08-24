package com.example.ui

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.studio.VideoIdea
import com.example.ui.screens.studio.*
import com.example.ui.theme.QabasTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class StudioScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: StudioViewModel

    @Before
    fun setUp() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val fakeRepo = FakeStudioRepository()
        viewModel = StudioViewModel(application, fakeRepo)
    }

    @Test
    fun testStudioHomeScreenRendersAndInteracts() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            QabasTheme(darkTheme = true) {
                StudioHomeScreen(
                    navController = navController,
                    onBack = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithTag("screen_studio").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_create_video_hero").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_filter_all").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_filter_processing").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_filter_completed").assertIsDisplayed()
    }

    @Test
    fun testCreateVideoProjectScreenRendersAndInputs() {
        viewModel.createNewProject()

        composeTestRule.setContent {
            val navController = rememberNavController()
            QabasTheme(darkTheme = true) {
                CreateVideoProjectScreen(
                    navController = navController,
                    onBack = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithTag("screen_studio_create").assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_idea_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_idea_text").performTextInput("فكرة فيديو جديدة عن السكينة والذكر")
        composeTestRule.onNodeWithTag("btn_analyze_idea").assertIsDisplayed()
    }

    @Test
    fun testIdeaAnalysisScreenRenders() {
        viewModel.createNewProject()
        viewModel.updateIdea(VideoIdea(ideaText = "فيديو هادف عن الرضا بالقضاء والقدر وطمأنينة النفس"))
        viewModel.analyzeIdea()

        composeTestRule.setContent {
            val navController = rememberNavController()
            QabasTheme(darkTheme = true) {
                IdeaAnalysisScreen(
                    navController = navController,
                    onBack = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithTag("screen_studio_analysis").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_create_video_plan").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_edit_idea").assertIsDisplayed()
    }

    @Test
    fun testVideoPlanScreenRendersAndAddScene() {
        viewModel.createNewProject()
        viewModel.updateIdea(VideoIdea(ideaText = "فيديو تعريفي بأركان الإسلام"))
        viewModel.analyzeIdea()
        viewModel.generatePlan()

        composeTestRule.setContent {
            val navController = rememberNavController()
            QabasTheme(darkTheme = true) {
                VideoPlanScreen(
                    navController = navController,
                    onBack = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithTag("screen_studio_plan").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_save_project").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_start_creation").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_add_scene").assertIsDisplayed()
    }
}

