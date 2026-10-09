package com.zotmacros

import com.zotmacros.data.DiningParser
import com.zotmacros.domain.Nutrient
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class DiningParserTest {
    private val parser = DiningParser()
    @Test fun malformedNutrientsBecomeUnknownNotZero() {
        val dish = parser.dish(Json.parseToJsonElement("""{"id":"abc","name":"Test","nutritionInfo":{"calories":165,"proteinG":"31","sodiumMg":"30 g","totalFatG":-1,"ironMg":"NaN","calciumMg":0}}"""))!!
        assertEquals(165.0, dish.nutrition[Nutrient.CALORIES]!!, 0.0)
        assertEquals(31.0, dish.nutrition[Nutrient.PROTEIN]!!, 0.0)
        assertNull(dish.nutrition[Nutrient.SODIUM])
        assertNull(dish.nutrition[Nutrient.FAT])
        assertNull(dish.nutrition[Nutrient.IRON])
        assertEquals(0.0, dish.nutrition[Nutrient.CALCIUM]!!, 0.0)
    }
    @Test fun dishWithoutNutritionStillWorks() {
        val dish = parser.dish(Json.parseToJsonElement("""{"id":"abc","name":"Soup","nutritionInfo":null}"""))!!
        assertEquals("Soup", dish.name)
        assertNull(dish.nutrition[Nutrient.CALORIES])
        assertEquals("Serving size unavailable", dish.serving)
    }
    @Test fun badRecordsAreSkipped() {
        assertNull(parser.dish(Json.parseToJsonElement("""{"name":"No ID"}""")))
        assertNull(parser.dish(Json.parseToJsonElement("[]")))
    }
    @Test fun menuKeepsActualPeriodsAndStationIds() {
        val raw = Json.parseToJsonElement(javaClass.getResource("/today.json")!!.readText())
        val day = parser.day(raw, "anteatery")
        assertTrue(day.any { it.name == "Evening Snack" })
        assertTrue(day.any { it.name == "Lunch" && it.stations.containsKey("1932") })
    }
    @Test fun unsuccessfulEnvelopeThrows() {
        assertThrows(IllegalArgumentException::class.java) {
            parser.day(Json.parseToJsonElement("""{"ok":false,"message":"error"}"""), "anteatery")
        }
    }
}
