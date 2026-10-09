package com.zotmacros.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zotmacros.data.UserPreferences
import com.zotmacros.domain.hallName

@Composable fun Onboarding(prefs: UserPreferences, onSave: (UserPreferences) -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var hall by rememberSaveable { mutableStateOf(prefs.hall) }
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Brand(); TextButton(onClick = { onSave(prefs.copy(onboarded = true, hall = hall)) }) { Text("Skip") }
        }
        LinearProgressIndicator(progress = { (step + 1) / 4f }, Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        when(step) {
            0 -> {
                Text("Fuel your day.\nTrack your macros.", style = MaterialTheme.typography.displayMedium)
                Muted("Your dining hall, your nutrition, one simple daily log.")
                Text("Built for life at UCI", style = MaterialTheme.typography.titleLarge)
                Muted("Browse real menus from the Anteatery and Brandywine. Choose your serving, then get on with your day.")
                Muted("No account. Your diary is saved on this device.")
                Button(onClick = { step = 1 }, Modifier.fillMaxWidth().heightIn(min = 54.dp)) { Text("Let’s get started") }
            }
            1 -> {
                Text("Where do you eat?", style = MaterialTheme.typography.headlineLarge)
                Muted("Choose a default. You can switch dining halls any time.")
                listOf("anteatery", "brandywine").forEach { id ->
                    OutlinedCard(onClick = { hall = id }, Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(hall == id, { hall = id }); Text(hallName(id), style = MaterialTheme.typography.titleLarge) }
                    }
                }
                Button(onClick = { step = 2 }, Modifier.fillMaxWidth().heightIn(min = 54.dp)) { Text("Continue") }
            }
            2 -> {
                Text("Set your daily goals", style = MaterialTheme.typography.headlineLarge)
                Muted("These are editable example defaults, not personalized recommendations.")
                GoalFields(prefs, { onSave(it.copy(hall = hall)); step = 3 }, "Use these goals")
            }
            else -> {
                Text("You’re ready to fuel up.", style = MaterialTheme.typography.displayMedium)
                Muted("Find a food. Pick your serving. See your progress.")
                Button(onClick = { onSave(prefs.copy(onboarded = true, hall = hall)) }, Modifier.fillMaxWidth().heightIn(min = 54.dp)) { Text("Go to Today") }
            }
        }
        if (step > 0) TextButton(onClick = { step-- }) { Text("Back") }
    }
}
