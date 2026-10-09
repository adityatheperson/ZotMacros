package com.zotmacros

import com.zotmacros.domain.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class NutritionTest {
    @Test fun decimalServingsKeepPrecision() {
        val n = Nutrition(mapOf(Nutrient.PROTEIN to 20.125, Nutrient.CALORIES to 165.0))
        assertEquals(30.1875, n.scaled(1.5)[Nutrient.PROTEIN]!!, 0.000001)
        assertEquals(247.5, n.scaled(1.5)[Nutrient.CALORIES]!!, 0.000001)
        assertNull(n.scaled(1.5)[Nutrient.SODIUM])
    }
    @Test fun zeroIsKnownButMissingIsUnknown() {
        val totals = aggregate(listOf(Nutrition(mapOf(Nutrient.PROTEIN to 0.0)), Nutrition()))
        assertEquals(0.0, totals.getValue(Nutrient.PROTEIN).value!!, 0.0)
        assertEquals(1, totals.getValue(Nutrient.PROTEIN).missing)
        assertNull(totals.getValue(Nutrient.SODIUM).value)
    }
    @Test fun emptyDiaryIsCompleteZero() {
        val total = aggregate(emptyList()).getValue(Nutrient.CALORIES)
        assertEquals(0.0, total.value!!, 0.0)
        assertEquals(0, total.missing)
    }
    @Test fun goalDoesNotClampNutrition() {
        assertEquals(3000.0, aggregate(listOf(Nutrition(mapOf(Nutrient.CALORIES to 3000.0))))[Nutrient.CALORIES]!!.value!!, 0.0)
    }
    @Test fun invalidServingsAreRejected() {
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, 1001.0).forEach {
            assertThrows(IllegalArgumentException::class.java) { Nutrition().scaled(it) }
        }
    }
    @Test fun losAngelesDateDoesNotFollowUtcMidnight() {
        assertEquals("2026-10-07", campusDate(Instant.parse("2026-10-08T06:59:59Z")).toString())
        assertEquals("2026-10-08", campusDate(Instant.parse("2026-10-08T07:00:00Z")).toString())
        assertEquals("2026-01-01", campusDate(Instant.parse("2026-01-02T07:59:59Z")).toString())
    }
}
