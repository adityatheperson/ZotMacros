package com.zotmacros.data

import com.zotmacros.domain.*
import kotlinx.serialization.json.*
import javax.inject.Inject

private fun JsonElement?.obj(): JsonObject? = this as? JsonObject
private fun JsonElement?.str(): String? = (this as? JsonPrimitive)?.contentOrNull

data class RawPeriod(val id: String, val name: String, val start: String?, val end: String?, val stations: Map<String, List<String>>)

class DiningParser @Inject constructor() {
    fun payload(root: JsonElement): JsonElement {
        val obj = root as? JsonObject ?: error("Invalid API response")
        require((obj["ok"] as? JsonPrimitive)?.booleanOrNull == true) { "The dining service could not return this menu." }
        return obj["data"]?.takeUnless { it is JsonNull } ?: error("Missing API data")
    }
    fun stations(root: JsonElement, hall: String): Map<String, String> {
        val restaurants = payload(root) as? JsonArray ?: error("Invalid restaurants response")
        val restaurant = restaurants.firstOrNull { it.obj()?.get("id").str() == hall }?.obj()
            ?: error("This dining hall is unavailable.")
        return (restaurant["stations"] as? JsonArray).orEmpty().mapNotNull { element ->
            val station = element.obj() ?: return@mapNotNull null
            val id = station["id"].str() ?: return@mapNotNull null
            id to (station["name"].str()?.trim()?.takeIf { it.isNotEmpty() } ?: "Station $id")
        }.toMap()
    }
    fun day(root: JsonElement, hall: String): List<RawPeriod> {
        val data = payload(root).obj() ?: error("Invalid menu response")
        require(data["id"].str() == hall) { "The dining service returned a different hall." }
        val periods = data["periods"].obj() ?: error("Missing meal periods")
        return periods.mapNotNull { (id, value) ->
            val period = value.obj() ?: return@mapNotNull null
            val name = period["name"].str()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val stations = period["stationToDishes"].obj().orEmpty().mapValues { (_, dishes) ->
                (dishes as? JsonArray).orEmpty().mapNotNull { it.str() }.distinct()
            }
            RawPeriod(id, name, period["startTime"].str(), period["endTime"].str(), stations)
        }.sortedWith(compareBy<RawPeriod> { it.start ?: "99:99" }.thenBy { it.name })
    }
    fun dishes(root: JsonElement): List<Dish> = (payload(root) as? JsonArray ?: error("Invalid dishes response")).mapNotNull(::dish)
    fun dish(element: JsonElement): Dish? {
        val value = element.obj() ?: return null
        val id = value["id"].str()?.takeIf { it.isNotBlank() } ?: return null
        val name = value["name"].str()?.takeIf { it.isNotBlank() } ?: return null
        val nutrition = value["nutritionInfo"].obj()
        val numbers = Nutrient.entries.associateWith { key ->
            nutrition?.get(key.apiKey).str()?.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 }
        }
        val serving = listOfNotNull(nutrition?.get("servingSize").str(), nutrition?.get("servingUnit").str())
            .filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Serving size unavailable" }
        val restrictions = value["dietRestriction"].obj()
        val allergens = restrictions.orEmpty().filter { (key, v) -> key.startsWith("contains") && (v as? JsonPrimitive)?.booleanOrNull == true }
            .keys.map { it.removePrefix("contains").replace(Regex("([a-z])([A-Z])"), "$1 $2") }
        return Dish(id, name, serving, Nutrition(numbers), value["description"].str().orEmpty(), value["ingredients"].str(), allergens, restrictions != null)
    }
}
