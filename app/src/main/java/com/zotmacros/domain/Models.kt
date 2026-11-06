package com.zotmacros.domain

import kotlinx.serialization.Serializable
import java.time.LocalTime

@Serializable
data class Dish(
    val id: String, val name: String, val serving: String = "Serving size unavailable",
    val nutrition: Nutrition = Nutrition(), val description: String = "", val ingredients: String? = null,
    val allergens: List<String> = emptyList(), val allergensReported: Boolean = false
)
@Serializable
data class MenuFood(val dish: Dish, val stationId: String, val station: String)
@Serializable
data class MenuPeriod(val id: String, val name: String, val start: String? = null, val end: String? = null, val foods: List<MenuFood> = emptyList())
@Serializable
data class MenuSnapshot(val hall: String, val date: String, val fetchedAt: Long, val periods: List<MenuPeriod>, val unavailableDishes: Int = 0)

data class MenuResult(val snapshot: MenuSnapshot?, val error: String? = null, val cached: Boolean = false)
@Serializable
enum class Meal(val label: String) { BREAKFAST("Breakfast"), LUNCH("Lunch"), DINNER("Dinner"), SNACKS("Snacks") }
fun mealForPeriod(name: String): Meal = when {
    name.contains("breakfast", true) -> Meal.BREAKFAST
    name.contains("lunch", true) || name.contains("brunch", true) -> Meal.LUNCH
    name.contains("dinner", true) -> Meal.DINNER
    else -> Meal.SNACKS
}
fun hallName(id: String): String = if (id == "brandywine") "Brandywine" else "Anteatery"
/** Foods served all day also appear under their meal periods upstream; keep them only in All Day. */
fun dedupeAllDayOverlaps(periods: List<MenuPeriod>): List<MenuPeriod> {
    val allDay = periods.filter { it.name.equals("All Day", true) }.flatMap { it.foods }.map { it.stationId to it.dish.id }.toSet()
    if (allDay.isEmpty()) return periods
    val meals = setOf("lunch", "dinner", "brunch")
    return periods.map { period ->
        if (period.name.lowercase() in meals) period.copy(foods = period.foods.filter { (it.stationId to it.dish.id) !in allDay })
        else period
    }
}
fun choosePeriod(periods: List<MenuPeriod>, preference: String, time: LocalTime): String? {
    if (preference != "Automatic") periods.firstOrNull { it.name.equals(preference, true) }?.let { return it.id }
    return periods.firstOrNull {
        val start = runCatching { LocalTime.parse(it.start) }.getOrNull()
        val end = runCatching { LocalTime.parse(it.end) }.getOrNull()
        start != null && end != null && !time.isBefore(start) && time.isBefore(end)
    }?.id ?: periods.firstOrNull()?.id
}
