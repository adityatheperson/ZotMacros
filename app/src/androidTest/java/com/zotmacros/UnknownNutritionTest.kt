package com.zotmacros

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zotmacros.domain.*
import com.zotmacros.ui.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@RunWith(AndroidJUnit4::class)
class UnknownNutritionTest {
    @get:Rule val compose = createComposeRule()
    @Test fun unknownNutritionKeepsSaveReachable() {
        var saved: Double? = null
        compose.setContent {
            ZotTheme { FoodSheet(FoodSelection(Dish("1923_101628_A4290_1", "Boom Boom Popcorn Chicken"), "anteatery", "Fire and Ice", "Lunch", Meal.LUNCH), false, {}, { quantity, _ -> saved = quantity }, {}) }
        }
        compose.onNodeWithText("Nutrition unavailable. You can log this food; daily totals will be incomplete.").assertExists()
        compose.onNodeWithText("Add to Today — Unavailable").assertIsDisplayed()
        val instrumentation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
        val screenshot = instrumentation.uiAutomation.takeScreenshot()
        java.io.File(instrumentation.targetContext.filesDir, "unknown-food-sheet.png").outputStream().use {
            screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        screenshot.recycle()
        compose.onNodeWithText("Add to Today — Unavailable").performClick()
        compose.runOnIdle { assertEquals(1.0, saved!!, 0.0) }
    }
}
