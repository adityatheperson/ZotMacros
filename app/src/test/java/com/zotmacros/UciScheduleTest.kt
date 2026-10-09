package com.zotmacros
import com.zotmacros.data.*
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
class UciScheduleTest {
    private fun schedules(value:String) = Json.parseToJsonElement(value).jsonArray
    @Test fun discoversLunchWithoutAnteaterAndHonorsClosedDays() {
        val schedule = schedules("""[{"type":"standard","meal_periods":[{"meal_period":"Lunch","opening_hours":"Mo-Fr 11:00-14:30"},{"meal_period":"Brunch","opening_hours":"Sa-Su 11:00-16:30"}]}]""")
        assertEquals(listOf("Lunch"), uciPeriods(schedule,"2026-10-08", mapOf("Lunch" to 25,"Brunch" to 13)).map { it.name })
        assertEquals(listOf("Brunch"), uciPeriods(schedule,"2026-10-10", mapOf("Lunch" to 25,"Brunch" to 13)).map { it.name })
    }
    @Test fun laterHoursOverrideOffForTheSameDay() {
        val schedule = schedules("""[{"type":"standard","meal_periods":[{"meal_period":"Breakfast","opening_hours":"Mo off; Mo 07:00-09:00"}]}]""")
        assertEquals("07:00", uciPeriods(schedule,"2026-10-12",mapOf("Breakfast" to 10)).single().start)
    }
    @Test fun specialClosureOverridesStandard() {
        val schedule = schedules("""[{"type":"standard","meal_periods":[{"meal_period":"Lunch","opening_hours":"Mo-Su 11:00-14:30"}]},{"type":"special","start_date":"2026-10-08","end_date":"2026-10-08","meal_periods":[{"meal_period":"Lunch","opening_hours":"Mo-Su off"}]}]""")
        assertTrue(uciPeriods(schedule,"2026-10-08",mapOf("Lunch" to 25)).isEmpty())
    }
}
