package com.zotmacros.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zotmacros.data.*
import com.zotmacros.domain.*
import java.text.DecimalFormat

fun number(value: Double?, decimals: Boolean = false): String = value?.let { DecimalFormat(if (decimals) "#,##0.#" else "#,##0").format(it) } ?: "—"
fun nutrientText(value: Double?, nutrient: Nutrient): String = value?.let { "${number(it, nutrient != Nutrient.CALORIES)} ${nutrient.unit}" } ?: "Unavailable"
@Composable fun Muted(text: String, modifier: Modifier = Modifier) = Text(text, modifier, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
@Composable fun Attribution() {
    val uri = LocalUriHandler.current
    TextButton(onClick = { uri.openUri("https://icssc.link/about-anteaterapi") }) {
        Text("Data from Anteater API", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(6.dp)); Icon(Icons.AutoMirrored.Outlined.OpenInNew, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable fun Brand(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Z", color = Gold, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
        Text("ZotMacros", style = MaterialTheme.typography.titleLarge)
    }
}
@Composable fun EmptyState(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.Outlined.Restaurant, null, Modifier.size(36.dp), tint = Gold)
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
@Composable fun CalorieRing(total: NutrientTotal, goal: Double) {
    val amount = total.value ?: 0.0
    val ratio by animateFloatAsState((amount / goal).toFloat().coerceIn(0f, 1f), label = "Calories")
    val track = MaterialTheme.colorScheme.surface
    Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(230.dp).semantics { contentDescription = "${number(total.value)} of ${number(goal)} calories${if (total.isPartial) ", partial total" else ""}" }) {
            val inset = 9.dp.toPx()
            val bounds = androidx.compose.ui.geometry.Size(size.width - inset * 2, size.height - inset * 2)
            val origin = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(track, -90f, 360f, false, origin, bounds, style = Stroke(13.dp.toPx(), cap = StrokeCap.Round))
            if (ratio > 0) drawArc(Gold, -90f, ratio * 360, false, origin, bounds, style = Stroke(13.dp.toPx(), cap = StrokeCap.Round))
        }
        Column(Modifier.width(188.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(number(total.value), style = MaterialTheme.typography.displayMedium)
            Muted("of ${number(goal)} kcal")
            Spacer(Modifier.height(8.dp))
            Text(when {
                total.value == null -> "Calories unavailable"
                total.isPartial -> "Partial total"
                amount > goal -> "${number(amount - goal)} over goal"
                else -> "${number(goal - amount)} remaining"
            }, color = ProteinColor, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    }
}
@Composable fun MacroCards(totals: Map<Nutrient, NutrientTotal>, prefs: UserPreferences) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(Nutrient.PROTEIN to ProteinColor, Nutrient.CARBS to Gold, Nutrient.FAT to FatColor).forEach { (key, color) ->
            val total = totals.getValue(key)
            Surface(Modifier.weight(1f), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(key.label, color = color, style = MaterialTheme.typography.labelLarge)
                    Text(number(total.value, true) + if (total.isPartial && total.value != null) "+" else "", style = MaterialTheme.typography.titleLarge)
                    Muted("/ ${number(prefs.goal(key))} g")
                    LinearProgressIndicator(progress = { ((total.value ?: 0.0) / prefs.goal(key)).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp), color = color, trackColor = BlueBackground)
                }
            }
        }
    }
}
@Composable fun PartialNotice(totals: Map<Nutrient, NutrientTotal>) {
    val missing = totals.filterKeys { it in listOf(Nutrient.CALORIES, Nutrient.PROTEIN, Nutrient.CARBS, Nutrient.FAT) }.values.maxOf { it.missing }
    if (missing > 0) Muted("Partial totals: $missing logged food(s) have missing nutrition. + means the known amount, not a complete total.")
}
@Composable fun FoodLog(entries: List<FoodEntry>, onEdit: (FoodEntry) -> Unit) {
    Meal.entries.forEach { meal ->
        val foods = entries.filter { it.meal == meal }.sortedBy { it.timestamp }
        if (foods.isNotEmpty()) {
            val calories = aggregate(foods.map { it.totals }).getValue(Nutrient.CALORIES)
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                Column {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(meal.label, fontWeight = FontWeight.SemiBold)
                        Text("${number(calories.value)}${if (calories.isPartial) "+" else ""} kcal", fontWeight = FontWeight.SemiBold)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    foods.forEachIndexed { i, entry ->
                        Surface(onClick = { onEdit(entry) }, color = Color.Transparent) {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(entry.name, style = MaterialTheme.typography.titleMedium)
                                    Muted("${number(entry.quantity, true)} serving(s) · ${nutrientText(entry.totals[Nutrient.PROTEIN], Nutrient.PROTEIN)} protein")
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(nutrientText(entry.totals[Nutrient.CALORIES], Nutrient.CALORIES), style = MaterialTheme.typography.labelLarge)
                                Icon(Icons.Outlined.ChevronRight, "Edit ${entry.name}", Modifier.size(20.dp))
                            }
                        }
                        if (i < foods.lastIndex) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}
