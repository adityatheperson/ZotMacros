package com.zotmacros

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zotmacros.data.*
import com.zotmacros.domain.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiaryPersistenceTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private lateinit var db: ZotDatabase
    @Before fun setup() { context.deleteDatabase("test-diary.db"); db = open() }
    private fun open() = Room.databaseBuilder(context, ZotDatabase::class.java, "test-diary.db").build()
    @After fun close() { db.close(); context.deleteDatabase("test-diary.db") }
    @Test fun unknownChickenPersistsAndKeepsTotalsIncomplete() = runBlocking {
        val repository = DiaryRepository(db.diary())
        val dish = Dish("1923_101628_A4290_1", "Boom Boom Popcorn Chicken")
        assertTrue(repository.add("unknown-chicken", dish, "anteatery", "Fire and Ice", "Lunch", Meal.LUNCH, 1.5))
        db.close(); db = open()
        val reopened = DiaryRepository(db.diary())
        reopened.edit("unknown-chicken", 2.0, Meal.LUNCH)
        val entry = db.diary().find("unknown-chicken")!!
        assertEquals("Fire and Ice", entry.station)
        assertNull(entry.totals[Nutrient.CALORIES])
        val total = aggregate(listOf(entry.totals, Nutrition(mapOf(Nutrient.CALORIES to 100.0))))[Nutrient.CALORIES]!!
        assertEquals(100.0, total.value!!, 0.0)
        assertEquals(1, total.missing)
    }
    @Test fun addReopenEditAndDeletePreserveNutritionSnapshot() = runBlocking {
        val dish = Dish("test-dish", "Test food", "100 g", Nutrition(mapOf(Nutrient.CALORIES to 165.0, Nutrient.PROTEIN to 31.0)))
        val repository = DiaryRepository(db.diary())
        assertTrue(repository.add("request-1", dish, "anteatery", "Station", "Lunch", Meal.LUNCH, 1.5))
        assertFalse(repository.add("request-1", dish, "anteatery", "Station", "Lunch", Meal.LUNCH, 1.5))
        assertTrue(repository.add("request-2", dish, "anteatery", "Station", "Lunch", Meal.LUNCH, 1.0))
        assertEquals(2, repository.entries.first().size)
        db.close(); db = open()
        val reopened = DiaryRepository(db.diary())
        assertEquals(247.5, db.diary().find("request-1")!!.totals[Nutrient.CALORIES]!!, 0.001)
        reopened.edit("request-1", 2.0, Meal.DINNER)
        val edited = db.diary().find("request-1")!!
        assertEquals(62.0, edited.totals[Nutrient.PROTEIN]!!, 0.001)
        assertNull(edited.totals[Nutrient.SODIUM])
        assertEquals(31.0, edited.perServing[Nutrient.PROTEIN]!!, 0.001)
        assertEquals(Meal.DINNER, edited.meal)
        reopened.delete("request-1")
        assertEquals(1, reopened.entries.first().size)
        reopened.clear(); assertTrue(reopened.entries.first().isEmpty())
    }
}
