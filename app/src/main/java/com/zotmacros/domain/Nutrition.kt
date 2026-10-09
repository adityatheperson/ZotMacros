package com.zotmacros.domain

import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

val CampusZone: ZoneId = ZoneId.of("America/Los_Angeles")
fun campusDate(now: Instant = Instant.now()): LocalDate = now.atZone(CampusZone).toLocalDate()

@Serializable
enum class Nutrient(val label: String, val unit: String, val apiKey: String) {
    CALORIES("Calories", "kcal", "calories"), PROTEIN("Protein", "g", "proteinG"),
    CARBS("Carbs", "g", "totalCarbsG"), FAT("Fats", "g", "totalFatG"),
    SATURATED_FAT("Saturated fat", "g", "saturatedFatG"), TRANS_FAT("Trans fat", "g", "transFatG"),
    FIBER("Fiber", "g", "dietaryFiberG"), SUGAR("Sugar", "g", "sugarsG"),
    SODIUM("Sodium", "mg", "sodiumMg"), CHOLESTEROL("Cholesterol", "mg", "cholesterolMg"),
    IRON("Iron", "mg", "ironMg"), CALCIUM("Calcium", "mg", "calciumMg"),
    VITAMIN_A("Vitamin A", "IU", "vitaminAIU"), VITAMIN_C("Vitamin C", "IU", "vitaminCIU")
}

@Serializable
data class Nutrition(val values: Map<Nutrient, Double?> = emptyMap()) {
    operator fun get(nutrient: Nutrient): Double? = values[nutrient]?.takeIf { it.isFinite() && it >= 0 }
    fun scaled(quantity: Double): Nutrition {
        require(validQuantity(quantity)) { "Enter a serving quantity greater than 0 and at most 1,000." }
        return Nutrition(Nutrient.entries.associateWith { nutrient ->
            this[nutrient]?.times(quantity)?.takeIf { it.isFinite() }
        })
    }
}
fun validQuantity(quantity: Double): Boolean = quantity.isFinite() && quantity > 0 && quantity <= 1000

data class NutrientTotal(val value: Double?, val missing: Int) {
    val isPartial: Boolean get() = missing > 0
}
fun aggregate(items: List<Nutrition>): Map<Nutrient, NutrientTotal> = Nutrient.entries.associateWith { key ->
    val known = items.mapNotNull { it[key] }
    NutrientTotal(if (items.isEmpty()) 0.0 else known.takeIf { it.isNotEmpty() }?.sum(), items.size - known.size)
}
