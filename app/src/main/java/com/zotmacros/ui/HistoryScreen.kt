package com.zotmacros.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zotmacros.data.*
import com.zotmacros.domain.*
import java.time.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun HistoryScreen(today: LocalDate, entries: List<FoodEntry>, prefs: UserPreferences, onEdit: (FoodEntry) -> Unit) {
    var selected by rememberSaveable { mutableStateOf(today.toString()) }
    var showPicker by remember { mutableStateOf(false) }
    val date = LocalDate.parse(selected)
    val foods = entries.filter { it.date == selected }
    val totals = aggregate(foods.map { it.totals })
    val dayRow = rememberLazyListState(initialFirstVisibleItemIndex = 6)
    LaunchedEffect(selected, today) {
        val index = 6 - java.time.temporal.ChronoUnit.DAYS.between(date, today).toInt()
        if (index in 0..6) dayRow.animateScrollToItem(index)
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Text("Your history", style = MaterialTheme.typography.headlineLarge); Muted("A day at a time.") }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { selected = date.minusDays(1).toString() }) { Icon(Icons.Outlined.ChevronLeft, "Previous day") }
            TextButton(onClick = { showPicker = true }) { Icon(Icons.Outlined.CalendarMonth, null); Spacer(Modifier.width(8.dp)); Text(date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))) }
            IconButton(onClick = { selected = date.plusDays(1).toString() }, enabled = date < today) { Icon(Icons.Outlined.ChevronRight, "Next day") }
        } }
        item { LazyRow(state = dayRow, horizontalArrangement = Arrangement.spacedBy(8.dp)) { items((6L downTo 0L).map { today.minusDays(it) }) { day ->
            FilterChip(selected == day.toString(), { selected = day.toString() }, label = { Text(day.format(DateTimeFormatter.ofPattern("EEE d"))) })
        } } }
        if (foods.isEmpty()) item { EmptyState("Nothing logged", "No food entries for this day.") }
        else {
            item { Text("${number(totals.getValue(Nutrient.CALORIES).value)}${if (totals.getValue(Nutrient.CALORIES).isPartial) "+" else ""} kcal", style = MaterialTheme.typography.displayMedium); Muted("Logged on ${date.format(DateTimeFormatter.ofPattern("MMM d"))}") }
            item { MacroCards(totals, prefs); Spacer(Modifier.height(12.dp)); PartialNotice(totals) }
            item { Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { FoodLog(foods, onEdit) } }
        }
        item { Text("Seven-day view", style = MaterialTheme.typography.titleLarge); Muted("Week ending ${date.format(DateTimeFormatter.ofPattern("MMM d"))}") }
        item { TrendChart(entries, date, Nutrient.CALORIES, Gold) }
        item { TrendChart(entries, date, Nutrient.PROTEIN, ProteinColor) }
    }
    if (showPicker) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(), selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate() <= today
        })
        DatePickerDialog(onDismissRequest = { showPicker = false }, confirmButton = { TextButton(onClick = {
            picker.selectedDateMillis?.let { selected = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString() }; showPicker = false
        }) { Text("Select") } }, dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } }) { DatePicker(picker) }
    }
}
@Composable private fun TrendChart(entries: List<FoodEntry>, end: LocalDate, nutrient: Nutrient, color: Color) {
    val days = (6L downTo 0L).map { end.minusDays(it) }
    val data = days.map { day -> entries.filter { it.date == day.toString() }.let { foods -> if (foods.isEmpty()) null else aggregate(foods.map { it.totals })[nutrient] } }
    val max = data.mapNotNull { it?.value }.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    Surface(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("${nutrient.label} · ${nutrient.unit}", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                days.forEachIndexed { i, day ->
                    val total = data[i]
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(number(total?.value) + if (total?.isPartial == true && total.value != null) "+" else "", style = MaterialTheme.typography.labelSmall)
                        Box(Modifier.height(90.dp).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                            if (total?.value != null) Box(Modifier.fillMaxWidth(.6f).height((90 * total.value / max).coerceAtLeast(2.0).dp).background(color, RoundedCornerShape(4.dp)))
                        }
                        Text(day.format(DateTimeFormatter.ofPattern("EE")), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Muted("— no data · + partial nutrition")
        }
    }
}
