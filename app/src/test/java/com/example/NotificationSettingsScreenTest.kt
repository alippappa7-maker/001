package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.ui.screens.NotificationSettingsScreen
import com.example.ui.theme.QabasTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class NotificationSettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNotificationSettingsScreenElementsAndInteractions() {
        var backClicked = false

        composeTestRule.setContent {
            QabasTheme(darkTheme = true) {
                NotificationSettingsScreen(
                    onBack = { backClicked = true }
                )
            }
        }

        // Verify notification screen top components
        composeTestRule.onNodeWithTag("screen_notification_settings").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_master_notifications").assertIsDisplayed()
        composeTestRule.onNodeWithTag("switch_master_notifications").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_back").assertIsDisplayed()

        // Scroll to and verify prayer & sound items
        composeTestRule.onNodeWithTag("card_prayers_notifications").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("toggle_notif_fajr").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("toggle_notif_dhuhr").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_dhikr_notifications").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_sound_vibration").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_send_test_notification").performScrollTo().assertIsDisplayed()

        // Test toggle clicks
        composeTestRule.onNodeWithTag("toggle_notif_fajr").performClick()
        composeTestRule.onNodeWithTag("switch_master_notifications").performClick()

        // Test back button
        composeTestRule.onNodeWithTag("btn_back").performClick()
        assert(backClicked)
    }
}
