package com.zotmacros

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun onboardingAndFourTabsAreAccessible() {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodesWithText("Let’s fuel up.").fetchSemanticsNodes().isNotEmpty()
        }
        if (compose.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty()) compose.onNodeWithText("Skip").performClick()
        compose.onNodeWithText("Let’s fuel up.").assertIsDisplayed()
        compose.onNodeWithText("Menu", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Dining menu").assertIsDisplayed()
        compose.onNodeWithText("History", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Your history").assertIsDisplayed()
        compose.onNodeWithText("Settings", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Nutrition goals").assertIsDisplayed()
    }
}
