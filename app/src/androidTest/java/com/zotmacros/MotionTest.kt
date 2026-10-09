package com.zotmacros

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.zotmacros.domain.*
import com.zotmacros.ui.*
import org.junit.Rule
import org.junit.Test

class MotionTest {
    @get:Rule val compose = createComposeRule()
    @Test fun sheetFinishesExitAndCanReopen() {
        val food = FoodSelection(Dish("motion-test", "Motion test food"), "anteatery", "Station", "Lunch", Meal.LUNCH)
        val selection = mutableStateOf<FoodSelection?>(food)
        compose.setContent { ZotTheme { FoodSheetHost(selection.value, false, { selection.value = null }, { _, _ -> selection.value = null }, {}) } }
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithContentDescription("Close food details").performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Motion test food").assertExists()
        compose.mainClock.advanceTimeBy(1500)
        compose.onNodeWithText("Motion test food").assertDoesNotExist()
        compose.runOnIdle { selection.value = food.copy(requestId = "reopen") }
        compose.mainClock.autoAdvance = true
        compose.onNodeWithText("Add to Today — Unavailable").assertIsDisplayed().performClick()
        compose.onNodeWithText("Motion test food").assertDoesNotExist()
    }
}
