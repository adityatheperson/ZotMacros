package com.zotmacros.data

import com.zotmacros.domain.*
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepository @Inject constructor(private val dao: DiaryDao) {
    val entries = dao.observeAll()
    suspend fun add(id: String, dish: Dish, hall: String, station: String, period: String, meal: Meal, quantity: Double, date: LocalDate = campusDate()): Boolean {
        val totals = dish.nutrition.scaled(quantity)
        return dao.insert(FoodEntry(id, date.toString(), System.currentTimeMillis(), dish.id, dish.name, hall, station, period, meal, quantity, dish.serving, dish.nutrition, totals)) != -1L
    }
    suspend fun edit(id: String, quantity: Double, meal: Meal) {
        val entry = dao.find(id) ?: error("This entry was already removed.")
        dao.update(entry.copy(quantity = quantity, meal = meal, totals = entry.perServing.scaled(quantity)))
    }
    suspend fun delete(id: String) = dao.delete(id)
    suspend fun clear() = dao.clear()
}
