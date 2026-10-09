package com.zotmacros
import com.zotmacros.data.*
import com.zotmacros.domain.*
import org.junit.Assert.*
import org.junit.Test
class SupplementTest {
    @Test fun rawLunchRetainsAllPlacementsAndUnknownChicken() {
        val raw = kotlinx.serialization.json.Json.parseToJsonElement(javaClass.getResource("/uci-lunch-2026-10-08.json")!!.readText())
        val foods = UciMenuParser().foods(raw, "2026-10-08", mapOf("1926" to "Fire and Ice"))
        assertEquals(46, foods.size)
        assertEquals(2, foods.count { it.name == "Jasmine Rice" })
        assertEquals("Fire and Ice", foods.single { it.name == "Boom Boom Popcorn Chicken" }.station)
        assertEquals(46, foods.distinctBy { it.stationId to it.id }.size)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsWrongDate() {
        val raw = kotlinx.serialization.json.Json.parseToJsonElement(javaClass.getResource("/uci-lunch-2026-10-08.json")!!.readText())
        UciMenuParser().foods(raw, "2020-01-01", emptyMap())
    }

    private val existing = MenuPeriod("l", "Lunch", foods=listOf(MenuFood(Dish("variant", "Rice", nutrition=Nutrition(mapOf(Nutrient.CALORIES to 123.0))), "home", "Home")))
    @Test fun configurableParentDoesNotBorrowVariantNutrition() {
        val merged = mergeSupplement(existing, listOf(SupplementFood("parent", "Rice", "home", "Home", listOf("parent", "variant"))))
        assertEquals("parent", merged.foods.single().dish.id)
        assertNull(merged.foods.single().dish.nutrition[Nutrient.CALORIES])
    }
    @Test fun excludesFoodsNoLongerPublishedForThisMeal() {
        val stale = existing.copy(foods=existing.foods + MenuFood(Dish("removed", "Old menu item"), "home", "Home"))
        val merged = mergeSupplement(stale, listOf(SupplementFood("variant", "Rice", "home", "Home", listOf("variant"))))
        assertEquals(existing.foods, merged.foods)
    }
    @Test fun preservesNutritionAndAddsOnlyMissingStationPlacements() {
        val entries = listOf(SupplementFood("variant", "Rice", "home", "Home", listOf("variant")), SupplementFood("parent", "Rice", "fire", "Fire and Ice", listOf("parent", "variant")), SupplementFood("chicken", "Boom Boom Popcorn Chicken", "fire", "Fire and Ice", listOf("chicken")))
        val merged = mergeSupplement(existing, entries + entries)
        assertEquals(3, merged.foods.size)
        assertEquals(existing.foods.first(), merged.foods.first())
        assertNull(merged.foods.last().dish.nutrition[Nutrient.CALORIES])
        val total = aggregate(merged.foods.map { it.dish.nutrition })[Nutrient.CALORIES]!!
        assertEquals(123.0, total.value!!, 0.0)
        assertEquals(2, total.missing)
    }
}
