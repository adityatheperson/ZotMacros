package com.zotmacros.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*

@Composable fun ZotApp(vm: ZotViewModel = hiltViewModel()) {
    val prefs by vm.preferences.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()
    val today by vm.today.collectAsStateWithLifecycle()
    val now by vm.now.collectAsStateWithLifecycle()
    val menu by vm.menu.collectAsStateWithLifecycle()
    val selection by vm.selection.collectAsStateWithLifecycle()
    val saving by vm.saving.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    val lifecycle = LocalLifecycleOwner.current
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) vm.onResume() }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(vm) { vm.events.collect { event -> if (event.haptic) haptic.performHapticFeedback(HapticFeedbackType.LongPress); snackbar.showSnackbar(event.message) } }
    Surface(Modifier.fillMaxSize(), color = BlueBackground) {
        if (prefs == null) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else if (!prefs!!.onboarded) {
            Box { Onboarding(prefs!!, vm::savePreferences); SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter)) }
        } else {
            val nav = rememberNavController()
            val current by nav.currentBackStackEntryAsState()
            val route = current?.destination?.route ?: "Today"
            val destinations = listOf("Today" to Icons.Outlined.Home, "Menu" to Icons.Outlined.Restaurant, "History" to Icons.Outlined.BarChart, "Settings" to Icons.Outlined.Settings)
            fun navigate(name: String) { if (name == route) return; nav.navigate(name) { popUpTo(nav.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } }
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val expanded = maxWidth >= 600.dp
                Row(Modifier.fillMaxSize()) {
                    if (expanded) NavigationRail(containerColor = BlueSurface) {
                        destinations.forEach { (name, icon) -> NavigationRailItem(selected = name == route, onClick = { navigate(name) }, icon = { Icon(icon, name) }, label = { Text(name) }) }
                    }
                    Scaffold(containerColor = BlueBackground, snackbarHost = { SnackbarHost(snackbar) }, bottomBar = {
                        if (!expanded) NavigationBar(Modifier.navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp).clip(RoundedCornerShape(28.dp)), containerColor = BlueSurface, windowInsets = WindowInsets(0, 0, 0, 0)) {
                            destinations.forEach { (name, icon) -> NavigationBarItem(selected = route == name, onClick = { navigate(name) }, icon = { Icon(icon, null) }, label = { Text(name) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Gold, selectedTextColor = Gold, indicatorColor = BlueSurface)) }
                        }
                    }) { padding ->
                        Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                            NavHost(nav, "Today", Modifier.widthIn(max = 720.dp).fillMaxSize(),
                                enterTransition = { fadeIn(tween(ZotMotion.EnterMillis)) },
                                exitTransition = { fadeOut(tween(ZotMotion.ExitMillis)) },
                                popEnterTransition = { fadeIn(tween(ZotMotion.EnterMillis)) },
                                popExitTransition = { fadeOut(tween(ZotMotion.ExitMillis)) }) {
                                composable("Today") { TodayScreen(today, entries, prefs!!, { navigate("Menu") }, vm::edit) }
                                composable("Menu") { MenuScreen(menu, today, now, prefs!!, entries.filter { it.date == today.toString() }.map { it.dishId }.toSet(), vm::selectHall, { vm.refresh(true) }, vm::openFood) }
                                composable("History") { HistoryScreen(today, entries, prefs!!, vm::edit) }
                                composable("Settings") { SettingsScreen(prefs!!, vm::savePreferences, vm::clearHistory) }
                            }
                        }
                    }
                }
            }
        }
        FoodSheetHost(selection, saving, vm::closeFood, vm::saveFood, vm::delete)
    }
}
