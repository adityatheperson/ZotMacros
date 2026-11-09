package com.zotmacros.data

import com.zotmacros.domain.*
import kotlinx.serialization.Serializable

@Serializable data class SupplementFood(val id: String, val name: String, val stationId: String, val station: String, val aliases: List<String>)
/** UCI determines current placement; matching Anteater nutrition remains unchanged. */
fun mergeSupplement(period: MenuPeriod, foods: List<SupplementFood>): MenuPeriod {
    val merged = foods.distinctBy { it.stationId to it.id }.map { food ->
        require(food.id.isNotBlank() && food.name.isNotBlank() && food.stationId.isNotBlank() && food.station.isNotBlank())
        period.foods.firstOrNull {
            it.stationId == food.stationId && (it.dish.id == food.id || it.dish.id == "${food.id}:station:${food.stationId}")
        } ?: MenuFood(Dish(food.id, food.name), food.stationId, food.station)
    }
    return period.copy(foods = merged)
}
