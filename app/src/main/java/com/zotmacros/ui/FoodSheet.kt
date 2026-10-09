package com.zotmacros.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.zotmacros.domain.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable fun FoodSheet(selection: FoodSelection, saving: Boolean, onDismiss: () -> Unit, onSave: (Double, Meal) -> Unit, onDelete: (String) -> Unit) {
    var quantity by rememberSaveable(selection.requestId) { mutableStateOf(selection.quantity.toString()) }
    var meal by rememberSaveable(selection.requestId) { mutableStateOf(selection.meal) }
    var confirmDelete by remember { mutableStateOf(false) }
    val amount = quantity.replace(',', '.').toDoubleOrNull()
    val valid = amount != null && validQuantity(amount)
    val totals = if (valid) selection.dish.nutrition.scaled(amount!!) else Nutrition()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = BlueSurface) {
        val sheetView = LocalView.current
        SideEffect {
            val window = generateSequence(sheetView.parent) { it.parent }
                .filterIsInstance<DialogWindowProvider>().firstOrNull()?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, sheetView).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
        Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 20.dp).navigationBarsPadding()) {
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(selection.dish.name, style = MaterialTheme.typography.headlineMedium)
                        Muted("${hallName(selection.hall)} · ${selection.station} · ${selection.period}")
                    }
                    IconButton(onClick = onDismiss, enabled = !saving) { Icon(Icons.Outlined.Close, "Close food details") }
                }
                Muted("1 serving = ${selection.dish.serving}")
                if (Nutrient.entries.all { selection.dish.nutrition[it] == null }) Muted("Nutrition unavailable. You can log this food; daily totals will be incomplete.")
                Text("${number(totals[Nutrient.CALORIES])} kcal", style = MaterialTheme.typography.displayMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(Nutrient.PROTEIN to ProteinColor, Nutrient.CARBS to Gold, Nutrient.FAT to FatColor).forEach { (key, color) ->
                        Column(Modifier.weight(1f)) { Text(if (totals[key] == null) "—" else nutrientText(totals[key], key), color = color, style = MaterialTheme.typography.titleLarge); Text(key.label, color = color) }
                    }
                }
                Text("Serving quantity", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0.5, 1.0, 1.5, 2.0).forEach { value -> FilterChip(selected = amount == value, onClick = { quantity = value.toString() }, label = { Text("${number(value, true)}×") }, enabled = !saving) }
                    FilterChip(selected = amount !in listOf(0.5, 1.0, 1.5, 2.0), onClick = { quantity = "" }, label = { Text("Custom") }, enabled = !saving)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { quantity = ((amount ?: 1.0) - 0.5).coerceAtLeast(0.1).toString() }, enabled = !saving) { Icon(Icons.Outlined.Remove, "Decrease servings") }
                    OutlinedTextField(quantity, { quantity = it }, Modifier.weight(1f), label = { Text("Servings") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = !valid, enabled = !saving,
                        supportingText = if (!valid) ({ Text("Enter more than 0, up to 1,000") }) else null)
                    IconButton(onClick = { quantity = ((amount ?: 0.0) + 0.5).coerceAtMost(1000.0).toString() }, enabled = !saving) { Icon(Icons.Outlined.Add, "Increase servings") }
                }
                Text("Log to", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Meal.entries.forEach { choice -> FilterChip(meal == choice, { meal = choice }, label = { Text(choice.label) }, enabled = !saving) }
                }
                Text("Nutrition facts", style = MaterialTheme.typography.titleLarge)
                Nutrient.entries.drop(4).forEach { key ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Muted(key.label); Text(nutrientText(totals[key], key)) }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                if (selection.entryId == null) {
                    Text("Ingredients & allergens", style = MaterialTheme.typography.titleMedium)
                    Muted(selection.dish.ingredients?.takeIf { it.isNotBlank() } ?: "Ingredients unavailable")
                    Muted(if (selection.dish.allergens.isNotEmpty()) "Contains: ${selection.dish.allergens.joinToString()}" else "No allergen information reported. Check with dining staff.")
                    Attribution(); UciAttribution()
                } else {
                    Muted("Using the original nutrition saved with this entry.")
                    TextButton(onClick = { confirmDelete = true }, enabled = !saving) { Icon(Icons.Outlined.DeleteOutline, null); Text("Delete entry") }
                }
            }
            Button(onClick = { if (valid) onSave(amount!!, meal) }, enabled = valid && !saving, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).heightIn(min = 54.dp)) {
                if (saving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text(if (selection.entryId != null) "Save changes" else "Add to Today — ${nutrientText(totals[Nutrient.CALORIES], Nutrient.CALORIES)}")
            }
        }
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("Delete this entry?") }, text = { Text("${selection.dish.name} will be removed from your log.") },
        confirmButton = { TextButton(onClick = { confirmDelete = false; selection.entryId?.let(onDelete) }) { Text("Delete") } }, dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } })
}
