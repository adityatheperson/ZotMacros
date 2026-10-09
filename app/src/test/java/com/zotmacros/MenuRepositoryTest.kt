package com.zotmacros

import com.zotmacros.data.*
import com.zotmacros.domain.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class MenuRepositoryTest {
    class MemoryMenuDao : MenuDao {
        private val rows = mutableMapOf<Pair<String,String>, CachedMenu>()
        override suspend fun get(hall: String, date: String) = rows[hall to date]
        override suspend fun latest(hall: String, date: String) = rows.values.filter { it.hall == hall && it.date <= date }.maxByOrNull { it.date }
        override suspend fun put(menu: CachedMenu) { rows[menu.hall to menu.date] = menu }
    }
    class TestApi : DiningApi {
        var offline = false
        var calls = 0
        var requestedDate = ""
        override suspend fun restaurants(): JsonElement {
            if(offline) throw IOException()
            return Json.parseToJsonElement("""{"ok":true,"data":[{"id":"anteatery","stations":[{"id":"s","name":"Real station"}]}]}""")
        }
        override suspend fun day(hall: String, date: String): JsonElement {
            calls++; requestedDate = date
            if(offline) throw IOException()
            return Json.parseToJsonElement("""{"ok":true,"data":{"id":"anteatery","periods":{"l":{"name":"Lunch","stationToDishes":{"s":["d","d"]}},"d":{"name":"Dinner","stationToDishes":{"s":["d"]}}}}}""")
        }
        override suspend fun dishes(ids: String): JsonElement {
            assertEquals("d", ids)
            return Json.parseToJsonElement("""{"ok":true,"data":[{"id":"d","name":"Actual food","nutritionInfo":{"calories":123}}]}""")
        }
        override suspend fun dish(id: String): JsonElement = error("Batch should be used")
    }
    @Test fun loadsRealStructureDeduplicatesAndCaches() = runTest {
        val api = TestApi(); val repo = MenuRepository(api, DiningParser(), MemoryMenuDao(), Json)
        val first = repo.load("anteatery", "2026-10-07")
        assertNull(first.error)
        assertEquals("2026-10-07", api.requestedDate)
        assertEquals("Real station", first.snapshot!!.periods.first().foods.first().station)
        assertEquals(1, first.snapshot!!.periods.first().foods.size)
        assertEquals(123.0, first.snapshot!!.periods.first().foods.first().dish.nutrition[Nutrient.CALORIES]!!, 0.0)
        assertTrue(repo.load("anteatery", "2026-10-07").cached)
        assertEquals(1, api.calls)
    }
    @Test fun offlineFallbackKeepsOriginalDateAndError() = runTest {
        val api = TestApi(); val repo = MenuRepository(api, DiningParser(), MemoryMenuDao(), Json)
        repo.load("anteatery", "2026-10-07")
        api.offline = true
        val result = repo.load("anteatery", "2026-10-08", true)
        assertEquals("2026-10-07", result.snapshot!!.date)
        assertNotNull(result.error)
        assertTrue(result.cached)
    }
}
