package com.zotmacros.data

import kotlinx.serialization.json.*

class UciMenuParser {
    fun data(root: JsonElement): JsonObject {
        val obj = root.jsonObject
        require((obj["errors"] as? JsonArray).isNullOrEmpty()) { "Incomplete UCI response" }
        return obj["data"] as? JsonObject ?: throw IllegalArgumentException("Missing UCI data")
    }
    fun foods(root: JsonElement, date: String, stations: Map<String, String>): List<SupplementFood> {
        val menu = data(root)["getLocationRecipes"]?.jsonObject ?: throw IllegalArgumentException("Missing menu")
        val mapping = menu["locationRecipesMap"]
        if (mapping == JsonNull && menu["products"] == JsonNull) return emptyList()
        val day = mapping?.jsonObject?.get("dateSkuMap")?.jsonArray?.singleOrNull { it.jsonObject["date"]?.jsonPrimitive?.content == date }
        require(day != null) { "Menu date unavailable" }
        val products = menu.getValue("products").jsonObject.getValue("items").jsonArray.associate { it.jsonObject.getValue("sku").jsonPrimitive.content to it.jsonObject }
        val foods = mutableListOf<SupplementFood>()
        for (element in day.jsonObject.getValue("stations").jsonArray) {
            val station = element.jsonObject
            val stationId = station.getValue("id").jsonPrimitive.content
            val skus = station.getValue("skus").jsonObject
            val simple = skus.getValue("simple").jsonArray.map { it.jsonPrimitive.content }.distinct()
            val entries = simple.map { it to listOf(it) } + skus.getValue("configurable").jsonArray.mapNotNull {
                val parent = it.jsonObject
                val id = parent.getValue("sku").jsonPrimitive.content
                val variants = parent.getValue("variants").jsonArray.map { v -> v.jsonPrimitive.content }
                if (variants.any { v -> v in simple }) null else id to (listOf(id) + variants)
            }
            for ((id, aliases) in entries) {
                val product = products[id] ?: throw IllegalArgumentException("Missing food details")
                val name = product.getValue("name").jsonPrimitive.content
                require(name.isNotBlank())
                val attributes = product.getValue("attributes").jsonArray.associate { it.jsonObject.getValue("name").jsonPrimitive.content to it.jsonObject["value"] }
                val hidden = (attributes["is_hide_from_web_menu"] as? JsonPrimitive)?.content
                if (hidden in listOf("1", "yes")) continue
                foods.add(SupplementFood(id, name, stationId, stations[stationId] ?: "Station $stationId", aliases))
            }
        }
        return foods.distinctBy { it.stationId to it.id }
    }
}
