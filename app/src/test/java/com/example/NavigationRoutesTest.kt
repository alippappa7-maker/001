package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.AppNavigation
import com.example.ui.navigation.Routes
import com.example.ui.theme.QabasTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class NavigationRoutesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNavigationFromHomeToOrbsAndBack() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            QabasTheme(darkTheme = true) {
                AppNavigation(
                    navController = navController,
                    startDestination = Routes.HOME
                )
            }
        }

        // Verify Home is displayed
        composeTestRule.onNodeWithTag("screen_home").assertIsDisplayed()

        // 1. Studio Screen navigation and back
        composeTestRule.onNodeWithTag("orb_studio").performClick()
        composeTestRule.onNodeWithTag("screen_studio").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_back").performClick()
        composeTestRule.onNodeWithTag("screen_home").assertIsDisplayed()

        // 2. Companion Screen navigation and back
        composeTestRule.onNodeWithTag("orb_companion").performClick()
        composeTestRule.onNodeWithTag("screen_companion").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_back").performClick()
        composeTestRule.onNodeWithTag("screen_home").assertIsDisplayed()

        // 3. Mihrab Screen navigation and back
        composeTestRule.onNodeWithTag("orb_mihrab").performClick()
        composeTestRule.onNodeWithTag("screen_mihrab").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_back").performClick()
        composeTestRule.onNodeWithTag("screen_home").assertIsDisplayed()

        // 4. Knowledge Screen navigation and back
        composeTestRule.onNodeWithTag("orb_knowledge").performClick()
        composeTestRule.onNodeWithTag("screen_knowledge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_back").performClick()
        composeTestRule.onNodeWithTag("screen_home").assertIsDisplayed()

        // 5. Impact Screen navigation and back
        composeTestRule.onNodeWithTag("orb_impact").performClick()
        composeTestRule.onNodeWithTag("screen_impact").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_back").performClick()
        composeTestRule.onNodeWithTag("screen_home").assertIsDisplayed()
    }

    @Test
    fun testNavigationToProfileAndSettings() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            QabasTheme(darkTheme = true) {
                AppNavigation(
                    navController = navController,
                    startDestination = Routes.HOME
                )
            }
        }

        // Navigate to Profile from Home top bar
        composeTestRule.onNodeWithTag("btn_profile").performClick()
        composeTestRule.onNodeWithTag("screen_profile").assertIsDisplayed()

        // Navigate to Settings from Profile
        composeTestRule.onNodeWithTag("btn_profile_settings_icon").performClick()
        composeTestRule.onNodeWithTag("screen_settings").assertIsDisplayed()

        // Pop back to Profile
        composeTestRule.onNodeWithTag("btn_back").performClick()
        composeTestRule.onNodeWithTag("screen_profile").assertIsDisplayed()

        // Pop back to Home
        composeTestRule.onNodeWithTag("btn_back").performClick()
        composeTestRule.onNodeWithTag("screen_home").assertIsDisplayed()
    }

    @Test
    fun testBottomNavigationTabs() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            QabasTheme(darkTheme = true) {
                AppNavigation(
                    navController = navController,
                    startDestination = Routes.HOME
                )
            }
        }

        // Switch to Compass via Bottom Navigation
        composeTestRule.onNodeWithTag("nav_item_compass").performClick()
        composeTestRule.onNodeWithTag("screen_compass").assertIsDisplayed()

        // Switch to Mihrab via Bottom Navigation
        composeTestRule.onNodeWithTag("nav_item_mihrab").performClick()
        composeTestRule.onNodeWithTag("screen_mihrab").assertIsDisplayed()

        // Switch to Journey via Bottom Navigation
        composeTestRule.onNodeWithTag("nav_item_journey").performClick()
        composeTestRule.onNodeWithTag("screen_journey").assertIsDisplayed()

        // Switch back to Home via Bottom Navigation
        composeTestRule.onNodeWithTag("nav_item_home").performClick()
        composeTestRule.onNodeWithTag("screen_home").assertIsDisplayed()
    }
}
