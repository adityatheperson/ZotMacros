package com.zotmacros.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zotmacros.data.UserPreferences
import com.zotmacros.domain.hallName

@Composable fun GoalFields(prefs: UserPreferences, onSave: (UserPreferences) -> Unit, button: String = "Save goals") {
    var calories by rememberSaveable(prefs.calories) { mutableStateOf(prefs.calories.toInt().toString()) }
    var protein by rememberSaveable(prefs.protein) { mutableStateOf(prefs.protein.toString()) }
    var carbs by rememberSaveable(prefs.carbs) { mutableStateOf(prefs.carbs.toString()) }
    var fat by rememberSaveable(prefs.fat) { mutableStateOf(prefs.fat.toString()) }
    val values = listOf(calories, protein, carbs, fat).map { it.replace(',', '.').toDoubleOrNull() }
    val valid = values.all { it != null && it.isFinite() && it > 0 && it <= 100_000 }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(Triple("Calories (kcal)", calories, { s: String -> calories = s }), Triple("Protein (g)", protein, { s: String -> protein = s }), Triple("Carbs (g)", carbs, { s: String -> carbs = s }), Triple("Fats (g)", fat, { s: String -> fat = s })).forEach { (label, value, update) ->
            OutlinedTextField(value, update, Modifier.fillMaxWidth(), label = { Text(label) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
                isError = value.replace(',', '.').toDoubleOrNull()?.let { !it.isFinite() || it <= 0 || it > 100_000 } ?: true)
        }
        if (!valid) Muted("Enter a positive number up to 100,000 for each goal.")
        Button(onClick = { onSave(prefs.copy(calories = values[0]!!, protein = values[1]!!, carbs = values[2]!!, fat = values[3]!!, exampleGoals = false)) }, enabled = valid, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(button) }
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable fun SettingsScreen(prefs: UserPreferences, onSave: (UserPreferences) -> Unit, onClear: () -> Unit) {
    var clear by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { Text("Settings", style = MaterialTheme.typography.headlineLarge); Muted("Make it your daily routine.") }
        item { Text("Nutrition goals", style = MaterialTheme.typography.titleLarge); if (prefs.exampleGoals) Muted("These are example defaults, not personalized recommendations. Replace them with your own goals.") }
        item { GoalFields(prefs, { onSave(it); saved = true }); if (saved) Muted("Your goals apply to the daily progress display.") }
        item { Text("Dining preferences", style = MaterialTheme.typography.titleLarge) }
        item {
            Muted("Preferred dining hall")
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf("anteatery", "brandywine").forEachIndexed { i, hall -> SegmentedButton(selected = prefs.hall == hall, onClick = { onSave(prefs.copy(hall = hall)) }, shape = SegmentedButtonDefaults.itemShape(i, 2), label = { Text(hallName(hall)) }) }
            }
        }
        item { Muted("Default meal period"); FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Automatic", "Breakfast", "Lunch", "Dinner").forEach { meal -> FilterChip(prefs.meal == meal, { onSave(prefs.copy(meal = meal)) }, label = { Text(meal) }) }
        }; Muted("Falls back to an available period when your choice isn’t offered.") }
        item { ToggleRow("Auto-refresh menus", "Refresh while the app is open, every 15 minutes", prefs.autoRefresh) { onSave(prefs.copy(autoRefresh = it)) } }
        item { ToggleRow("Haptic feedback", "A subtle confirmation when food is saved", prefs.haptics) { onSave(prefs.copy(haptics = it)) } }
        item { HorizontalDivider(); TextButton(onClick = { clear = true }) { Text("Clear local food history") } }
        item { Text("About ZotMacros", style = MaterialTheme.typography.titleLarge); Muted("Fuel your day. Track your macros.\nVersion 1.0.2 · Built for UCI students\nYour food diary stays on this device. No account required."); Attribution(); UciAttribution(); Muted("Independent student app. Not an official UC Irvine application. Dining information may change; confirm ingredients and allergens with dining staff.") }
    }
    if (clear) AlertDialog(onDismissRequest = { clear = false }, title = { Text("Clear all food history?") }, text = { Text("This permanently deletes every saved food entry on this device. Your goals and preferences will stay.") },
        confirmButton = { TextButton(onClick = { clear = false; onClear() }) { Text("Clear history") } }, dismissButton = { TextButton(onClick = { clear = false }) { Text("Cancel") } })
}
@Composable private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); Muted(subtitle) }
        Spacer(Modifier.width(12.dp)); Switch(checked, onChange)
    }
}
