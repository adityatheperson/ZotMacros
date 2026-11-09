package com.zotmacros.data

import com.zotmacros.domain.MenuPeriod
import kotlinx.serialization.json.*
import java.time.LocalDate
import java.time.LocalTime

/** UCI's published opening-hours syntax; unknown formats fail rather than invent hours. */
fun uciPeriods(schedules: JsonArray, date: String, meals: Map<String, Int>): List<MenuPeriod> {
    val day = LocalDate.parse(date).dayOfWeek.value - 1
    val candidates = schedules.map { it.jsonObject }
    val schedule = candidates.firstOrNull {
        val start = it["start_date"]?.jsonPrimitive?.contentOrNull
        val end = it["end_date"]?.jsonPrimitive?.contentOrNull
        start != null && end != null && date >= start && date <= end
    } ?: candidates.firstOrNull { it["type"]?.jsonPrimitive?.content == "standard" }
    require(schedule != null) { "UCI schedule unavailable" }
    val days = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
    return schedule.getValue("meal_periods").jsonArray.mapNotNull { element ->
        val period = element.jsonObject
        val name = period.getValue("meal_period").jsonPrimitive.content
        var hours: Pair<String, String>? = null
        val raw = period.getValue("opening_hours").jsonPrimitive.content
        for (block in raw.split(';').map { it.trim() }.filter { it.isNotEmpty() }) {
            val parts = block.split(Regex("\\s+"))
            require(parts.size == 2) { "Unrecognized UCI hours" }
            val range = parts[0].split('-')
            require(range.size in 1..2)
            val start = days.indexOf(range.first()); val end = days.indexOf(range.last())
            require(start >= 0 && end >= 0)
            val included = if (start <= end) day in start..end else day >= start || day <= end
            if (included) {
                hours = if (parts[1] == "off") null else {
                    val times = parts[1].split('-')
                    require(times.size == 2)
                    LocalTime.parse(times[0]); LocalTime.parse(times[1])
                    times[0] to times[1]
                }
            }
        }
        hours?.let { (start, end) ->
            val id = meals[name] ?: throw IllegalArgumentException("Unknown UCI meal")
            MenuPeriod("uci:$id", name, start, end)
        }
    }
}
