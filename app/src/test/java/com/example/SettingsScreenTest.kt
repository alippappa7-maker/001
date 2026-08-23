package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.QabasTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
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
        composeTestRule.onNodeWithTag("switch_dark_mode").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_lang_ar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_lang_en").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_back").assertIsDisplayed()

        // Test interaction
        composeTestRule.onNodeWithTag("btn_lang_en").performClick()
        composeTestRule.onNodeWithTag("btn_lang_ar").performClick()
        composeTestRule.onNodeWithTag("switch_dark_mode").performClick()

        composeTestRule.onNodeWithTag("btn_back").performClick()
        assert(backClicked)
    }
}
