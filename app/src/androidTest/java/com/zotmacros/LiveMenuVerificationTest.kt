package com.zotmacros

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zotmacros.data.*
import com.zotmacros.domain.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Opt-in historical live check; ordinary device tests do not depend on UCI availability. */
@RunWith(AndroidJUnit4::class)
class LiveMenuVerificationTest {
    @Test fun octoberEightLunchMatchesCapturedSource() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("verifyLiveMenu") == "true")
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, ZotDatabase::class.java).build()
        try {
            val json = AppModule.json()
            val base = MenuRepository(AppModule.api(json), DiningParser(), db.menus(), json).load("anteatery", "2026-10-08", true).snapshot!!
            val result = MenuRepository(AppModule.api(json), DiningParser(), db.menus(), json, SupplementSource(json)).load("anteatery", "2026-10-08", true)
            assertNull(result.error)
            val lunch = result.snapshot!!.periods.single { it.name == "Lunch" }
            assertEquals(46, lunch.foods.size)
            val originalLunch = base.periods.single { it.name == "Lunch" }
            lunch.foods.forEach { food -> originalLunch.foods.firstOrNull { it.dish.id == food.dish.id && it.stationId == food.stationId }?.let { assertEquals(it.dish.nutrition, food.dish.nutrition) } }
            assertEquals(46, lunch.foods.distinctBy { it.stationId to it.dish.id }.size)
            assertEquals(mapOf("1935" to 12, "1953" to 1, "1932" to 3, "1941" to 3, "1929" to 1, "1947" to 17, "1926" to 2, "1944" to 2, "1938" to 5), lunch.foods.groupingBy { it.stationId }.eachCount())
            val chicken = lunch.foods.single { it.dish.name == "Boom Boom Popcorn Chicken" }
            assertEquals("Fire and Ice", chicken.station)
            assertNull(chicken.dish.nutrition[Nutrient.CALORIES])
            val diary = DiaryRepository(db.diary())
            assertTrue(diary.add("live-chicken", chicken.dish, "anteatery", chicken.station, "Lunch", Meal.LUNCH, 1.0))
            assertNull(db.diary().find("live-chicken")!!.totals[Nutrient.CALORIES])
        } finally { db.close() }
    }
}
