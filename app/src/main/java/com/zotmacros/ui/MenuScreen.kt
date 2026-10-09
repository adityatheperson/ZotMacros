package com.zotmacros.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zotmacros.data.UserPreferences
import com.zotmacros.domain.*
import java.time.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun MenuScreen(state: MenuUi, today: LocalDate, now: Instant, prefs: UserPreferences, loggedIds: Set<String>, onHall: (String) -> Unit, onRefresh: () -> Unit, onFood: (MenuFood, String) -> Unit) {
    var query by rememberSaveable(state.hall) { mutableStateOf("") }
    var selectedPeriod by rememberSaveable(state.hall, state.snapshot?.date) { mutableStateOf<String?>(null) }
    val periods = state.snapshot?.periods.orEmpty()
    val period = periods.firstOrNull { it.id == selectedPeriod }
        ?: periods.firstOrNull { it.id == choosePeriod(periods, prefs.meal, now.atZone(CampusZone).toLocalTime()) }
    var selectedStation by rememberSaveable(state.hall, state.snapshot?.date, period?.id) { mutableStateOf<String?>(null) }
    val stations = period?.foods.orEmpty().distinctBy { it.stationId }
    val effectiveStation = selectedStation?.takeIf { id -> stations.any { it.stationId == id } }
    val foods = period?.foods.orEmpty().filter { (effectiveStation == null || it.stationId == effectiveStation) && it.dish.name.contains(query, true) }
    PullToRefreshBox(isRefreshing = state.loading, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Dining menu", style = MaterialTheme.typography.headlineLarge); Muted("Today, ${today.format(DateTimeFormatter.ofPattern("MMM d"))}") }
            item { SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf("anteatery", "brandywine").forEachIndexed { index, hall ->
                    SegmentedButton(selected = state.hall == hall, onClick = { onHall(hall) }, shape = SegmentedButtonDefaults.itemShape(index, 2), label = { Text(hallName(hall)) })
                }
            } }
            if (periods.isNotEmpty()) item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(periods, key = { it.id }) { p -> FilterChip(selected = period?.id == p.id, onClick = { selectedPeriod = p.id }, label = { Text(p.name) }) }
                }
            }
            item { OutlinedTextField(query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                placeholder = { Text("Search this menu") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, shape = RoundedCornerShape(16.dp)) }
            item { Attribution(); UciAttribution() }
            if (stations.isNotEmpty()) item { LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(effectiveStation == null, { selectedStation = null }, label = { Text("All stations") }) }
                items(stations, key = { it.stationId }) { s -> FilterChip(effectiveStation == s.stationId, { selectedStation = s.stationId }, label = { Text(s.station) }) }
            } }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.snapshot?.let { snapshot ->
                        val historical = snapshot.date != today.toString()
                        val old = now.toEpochMilli() - snapshot.fetchedAt > 60 * 60_000
                        val time = Instant.ofEpochMilli(snapshot.fetchedAt).atZone(CampusZone).format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))
                        Muted(if (historical) "Saved menu for ${snapshot.date} · Not confirmed for today" else if (old || state.error != null) "Saved menu · May be out of date" else "${if (state.cached) "Cached · " else ""}Updated $time")
                        if (historical || old || state.error != null) Muted("Last retrieved $time")
                        if (snapshot.unavailableDishes > 0) Muted("${snapshot.unavailableDishes} dish details are unavailable. Refresh to try again.")
                    }
                    if (state.error != null) {
                        Text(state.error, color = MaterialTheme.colorScheme.onSurface)
                        OutlinedButton(onClick = onRefresh, enabled = !state.loading) { Text("Retry") }
                    }
                    if (state.loading && state.snapshot == null) { LinearProgressIndicator(Modifier.fillMaxWidth()); Muted("Loading your dining hall…") }
                    if (period?.start != null && period.end != null) Muted("${period.name} · ${period.start.take(5)}–${period.end.take(5)}")
                }
            }
            if (foods.isEmpty() && !state.loading) item {
                EmptyState(if (query.isNotBlank()) "No matching foods" else "No foods to show", if (query.isNotBlank()) "Try another food name or station." else "This meal’s menu may not be published yet. Pull down to refresh.")
            }
            foods.groupBy { it.stationId }.forEach { (_, group) ->
                item { Text(group.first().station, style = MaterialTheme.typography.titleLarge) }
                items(group, key = { "${it.stationId}:${it.dish.id}" }) { food ->
                    Surface(onClick = { onFood(food, period?.name.orEmpty()) }, color = androidx.compose.ui.graphics.Color.Transparent) {
                        Column {
                            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(food.dish.name, style = MaterialTheme.typography.titleMedium)
                                    Muted(food.dish.serving)
                                    if (Nutrient.entries.all { food.dish.nutrition[it] == null }) Muted("Nutrition unavailable")
                                    Text(nutrientText(food.dish.nutrition[Nutrient.CALORIES], Nutrient.CALORIES), style = MaterialTheme.typography.labelLarge)
                                    Text("P ${number(food.dish.nutrition[Nutrient.PROTEIN], true)} g  ·  C ${number(food.dish.nutrition[Nutrient.CARBS], true)} g  ·  F ${number(food.dish.nutrition[Nutrient.FAT], true)} g", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                    if (food.dish.id in loggedIds) Text("✓ Logged today", color = Gold, style = MaterialTheme.typography.labelMedium)
                                }
                                IconButton(onClick = { onFood(food, period?.name.orEmpty()) }) { Icon(Icons.Outlined.AddCircleOutline, "Add ${food.dish.name}", tint = Gold) }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}
