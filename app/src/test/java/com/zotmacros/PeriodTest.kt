package com.zotmacros

import com.zotmacros.domain.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime

class PeriodTest {
    private val periods = listOf(MenuPeriod("a","Breakfast","07:00:00","11:00:00"), MenuPeriod("b","Lunch","11:00:00","14:30:00"),MenuPeriod("c","Evening Snack","20:00:00","23:00:00"))
    @Test fun automaticUsesReportedHours() { assertEquals("b", choosePeriod(periods,"Automatic",LocalTime.NOON)) }
    @Test fun savedPreferenceWinsWhenAvailable() { assertEquals("a", choosePeriod(periods,"Breakfast",LocalTime.NOON)) }
    @Test fun absentPreferenceFallsBackToOpenPeriod() { assertEquals("b",choosePeriod(periods,"Dinner",LocalTime.NOON)) }
    @Test fun emptyPeriodsAreAllowed() { assertNull(choosePeriod(emptyList(),"Automatic",LocalTime.NOON)) }
    @Test fun snackMapsToDiaryCategory() { assertEquals(Meal.SNACKS, mealForPeriod("Evening Snack")) }
}
