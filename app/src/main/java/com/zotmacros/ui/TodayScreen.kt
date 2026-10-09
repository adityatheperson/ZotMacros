package com.zotmacros.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zotmacros.data.*
import com.zotmacros.domain.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable fun TodayScreen(date: LocalDate, entries: List<FoodEntry>, prefs: UserPreferences, onAdd: () -> Unit, onEdit: (FoodEntry) -> Unit) {
    val foods = entries.filter { it.date == date.toString() }
    val totals = aggregate(foods.map { it.totals })
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Brand(); Muted(date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))) } }
        item { Text("Let’s fuel up.", style = MaterialTheme.typography.headlineLarge) }
        item { CalorieRing(totals.getValue(Nutrient.CALORIES), prefs.calories) }
        item { MacroCards(totals, prefs) }
        item { PartialNotice(totals); if (prefs.exampleGoals) Muted("Example goals · Personalize them in Settings") }
        item { Button(onClick = onAdd, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(8.dp)); Text("Add food") } }
        item { Text("Today’s food", style = MaterialTheme.typography.titleLarge) }
        if (foods.isEmpty()) item { EmptyState("A fresh start", "Find a meal from your dining hall to start today’s log.") }
        else item { Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { FoodLog(foods, onEdit) } }
        item { Text("Daily nutrition", style = MaterialTheme.typography.titleLarge) }
        item { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Nutrient.entries.filter { it !in listOf(Nutrient.CALORIES, Nutrient.PROTEIN, Nutrient.CARBS, Nutrient.FAT) }.forEach { n ->
                val total = totals.getValue(n)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Muted(n.label)
                    Text(if (total.value == null) "Unavailable" else "${number(total.value, true)}${if (total.isPartial) "+" else ""} ${n.unit}")
                }
            }
        } }
    }
}
