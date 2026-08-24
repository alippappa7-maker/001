package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.CompassScreen
import com.example.ui.screens.CompanionScreen
import com.example.ui.screens.ImpactScreen
import com.example.ui.screens.JourneyScreen
import com.example.ui.screens.KnowledgeScreen
import com.example.ui.screens.MihrabScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.studio.StudioHomeScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.QabasTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("QABAS", appName)
    }

    @Test
    fun `test welcome and screens render in light and dark modes`() {
        composeTestRule.setContent {
            QabasTheme(darkTheme = false) {
                WelcomeScreen(onNavigateToHome = {})
            }
        }
        composeTestRule.onNodeWithTag("screen_welcome").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_welcome_start").assertIsDisplayed()
    }

    @Test
    fun `test compass and mihrab render properly`() {
        composeTestRule.setContent {
            QabasTheme(darkTheme = true) {
                CompassScreen(onBack = {})
            }
        }
        composeTestRule.onNodeWithTag("screen_compass").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_compass_permission").assertIsDisplayed()
    }

    @Test
    fun `test all auxiliary screens render properly`() {
        composeTestRule.setContent {
            QabasTheme(darkTheme = true) {
                MihrabScreen(onBack = {})
            }
        }
        composeTestRule.onNodeWithTag("screen_mihrab").assertIsDisplayed()
    }
}
