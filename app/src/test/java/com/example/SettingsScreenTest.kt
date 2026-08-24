package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.QabasTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSettingsScreenElementsAndToggle() {
        var backClicked = false

        composeTestRule.setContent {
            QabasTheme(darkTheme = true) {
                SettingsScreen(
                    onBack = { backClicked = true }
                )
            }
        }

        // Verify settings screen and its core components exist
        composeTestRule.onNodeWithTag("screen_settings").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_settings_notifications").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_back").assertIsDisplayed()

        // Scroll to and verify elements
        composeTestRule.onNodeWithTag("switch_dark_mode").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_lang_ar").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_lang_en").performScrollTo().assertIsDisplayed()

        // Test interaction
        composeTestRule.onNodeWithTag("btn_lang_en").performClick()
        composeTestRule.onNodeWithTag("btn_lang_ar").performClick()
        composeTestRule.onNodeWithTag("switch_dark_mode").performClick()

        composeTestRule.onNodeWithTag("btn_back").performClick()
        assert(backClicked)
    }
}
