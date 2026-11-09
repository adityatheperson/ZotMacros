package com.zotmacros

import com.zotmacros.data.*
import com.zotmacros.domain.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class UciSourceTest {
    private val json = Json { ignoreUnknownKeys = true }
    @Test fun sendsExactHallDateAndReturnedMealId() = runTest {
        val api = object : UciDiningApi {
            override suspend fun query(query: String, variables: String): JsonElement {
                val args = json.parseToJsonElement(variables).jsonObject
                assertEquals("the-anteatery", args.getValue("locationUrlKey").jsonPrimitive.content)
                if ("Commerce_mealPeriods" in query) return json.parseToJsonElement("""{"data":{"Commerce_mealPeriods":[{"id":25,"name":"Lunch"}],"getLocation":{"aemAttributes":{"hoursOfOperation":{"schedule":[{"type":"standard","meal_periods":[{"meal_period":"Lunch","opening_hours":"Mo-Fr 11:00-14:30"}]}]}},"commerceAttributes":{"children":[{"id":1926,"name":"Fire and Ice"}]}}}}""")
                assertEquals("2026-10-08", args.getValue("date").jsonPrimitive.content)
                assertEquals(25, args.getValue("mealPeriod").jsonPrimitive.int)
                return json.parseToJsonElement("""{"data":{"getLocationRecipes":{"locationRecipesMap":{"dateSkuMap":[{"date":"2026-10-08","stations":[{"id":1926,"skus":{"simple":[],"configurable":[{"sku":"chicken","variants":["bulk","small"]}]}}]}]},"products":{"items":[{"sku":"chicken","name":"Boom Boom Popcorn Chicken","attributes":[]}]}}}}""")
            }
        }
        val result = SupplementSource(json, suppliedApi=api).complete(MenuSnapshot("anteatery", "2026-10-08", 0, emptyList()))
        assertTrue(result.supplementVerified)
        assertEquals("Fire and Ice", result.periods.single().foods.single().station)
        assertNull(result.periods.single().foods.single().dish.nutrition[Nutrient.CALORIES])
    }
    @Test fun failedUciRefreshKeepsExactDateCacheAndWarns() = runTest {
        val dao = MenuRepositoryTest.MemoryMenuDao()
        val baseApi = MenuRepositoryTest.TestApi()
        val original = MenuRepository(baseApi, DiningParser(), dao, json).load("anteatery", "2026-10-08").snapshot!!
        val failing = object : UciDiningApi {
            override suspend fun query(query: String, variables: String): JsonElement = throw java.io.IOException("offline")
        }
        val result = MenuRepository(baseApi, DiningParser(), dao, json, SupplementSource(json, suppliedApi=failing)).load("anteatery", "2026-10-08", true)
        assertEquals(original, result.snapshot)
        assertNotNull(result.error)
        assertTrue(result.cached)
    }
    @Test(expected=kotlinx.coroutines.CancellationException::class) fun cancellationEscapesSupplementAndRepository() = runTest {
        val cancelling = object : UciDiningApi {
            override suspend fun query(query: String, variables: String): JsonElement = throw kotlinx.coroutines.CancellationException()
        }
        MenuRepository(MenuRepositoryTest.TestApi(), DiningParser(), MenuRepositoryTest.MemoryMenuDao(), json, SupplementSource(json, suppliedApi=cancelling)).load("anteatery", "2026-10-08", true)
    }
    @Test(expected=IllegalArgumentException::class) fun rejectsGraphqlPartialFailure() {
        UciMenuParser().foods(json.parseToJsonElement("""{"errors":[{"message":"failed"}],"data":{}}"""),"2026-10-08", emptyMap())
    }
}
