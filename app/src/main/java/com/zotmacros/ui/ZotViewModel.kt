package com.zotmacros.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zotmacros.data.*
import com.zotmacros.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class FoodSelection(val dish: Dish, val hall: String, val station: String, val period: String, val meal: Meal,
    val quantity: Double = 1.0, val entryId: String? = null, val requestId: String = UUID.randomUUID().toString())
data class MenuUi(val hall: String = "anteatery", val snapshot: MenuSnapshot? = null, val loading: Boolean = false,
    val error: String? = null, val cached: Boolean = false)
data class UiEvent(val message: String, val haptic: Boolean = false)

@HiltViewModel
class ZotViewModel @Inject constructor(
    private val diary: DiaryRepository, private val menus: MenuRepository, private val settings: PreferencesRepository
) : ViewModel() {
    val preferences = settings.preferences.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val entries = diary.entries.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _now = MutableStateFlow(Instant.now())
    val now = _now.asStateFlow()
    private val _today = MutableStateFlow(campusDate())
    val today = _today.asStateFlow()
    private val _menu = MutableStateFlow(MenuUi())
    val menu = _menu.asStateFlow()
    private val _selection = MutableStateFlow<FoodSelection?>(null)
    val selection = _selection.asStateFlow()
    private val _saving = MutableStateFlow(false)
    val saving = _saving.asStateFlow()
    private val eventsChannel = Channel<UiEvent>(Channel.BUFFERED)
    val events = eventsChannel.receiveAsFlow()
    private var menuJob: Job? = null
    private var requestKey: Pair<String, String>? = null
    private var initialized = false
    private var lastAttempt = 0L
    init {
        viewModelScope.launch {
            settings.preferences.collect { prefs ->
                if (!initialized || prefs.hall != _menu.value.hall) {
                    initialized = true
                    selectHall(prefs.hall)
                }
            }
        }
        viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                onResume()
            }
        }
    }
    fun onResume() {
        val instant = Instant.now()
        _now.value = instant
        val now = campusDate(instant)
        if (now != _today.value) {
            _today.value = now
            refresh(force = true)
        } else if (preferences.value?.autoRefresh == true && System.currentTimeMillis() - lastAttempt > 15 * 60_000) refresh()
    }
    fun selectHall(hall: String) {
        if (_menu.value.hall == hall && _menu.value.snapshot != null) return
        menuJob?.cancel()
        _menu.value = MenuUi(hall)
        refresh()
    }
    fun refresh(force: Boolean = false) {
        val hall = _menu.value.hall
        val date = _today.value.toString()
        val key = hall to date
        if (menuJob?.isActive == true && requestKey == key) return
        menuJob?.cancel()
        requestKey = key
        lastAttempt = System.currentTimeMillis()
        menuJob = viewModelScope.launch {
            _menu.update { it.copy(loading = true, error = null) }
            try {
                val cached = menus.cached(hall, date)
                if (cached != null && requestKey == key) _menu.update { it.copy(snapshot = cached, cached = true) }
                val result = menus.load(hall, date, force)
                if (requestKey == key) _menu.value = MenuUi(hall, result.snapshot, false, result.error, result.cached)
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { _menu.update { it.copy(loading = false, error = "Couldn’t load the saved menu. Please retry.") } }
        }
    }
    fun openFood(food: MenuFood, period: String) {
        _selection.value = FoodSelection(food.dish, _menu.value.hall, food.station, period, mealForPeriod(period))
    }
    fun edit(entry: FoodEntry) {
        _selection.value = FoodSelection(Dish(entry.dishId, entry.name, entry.serving, entry.perServing), entry.hall,
            entry.station, entry.period, entry.meal, entry.quantity, entry.id)
    }
    fun closeFood() { if (!_saving.value) _selection.value = null }
    fun saveFood(quantity: Double, meal: Meal) {
        val selection = _selection.value ?: return
        if (_saving.value || !validQuantity(quantity)) return
        _saving.value = true
        viewModelScope.launch {
            try {
                if (selection.entryId == null) diary.add(selection.requestId, selection.dish, selection.hall, selection.station, selection.period, meal, quantity)
                else diary.edit(selection.entryId, quantity, meal)
                _selection.value = null
                eventsChannel.send(UiEvent(if (selection.entryId == null) "Added to Today" else "Entry updated", preferences.value?.haptics == true))
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { eventsChannel.send(UiEvent("Couldn’t save this food. Please try again.")) }
            finally { _saving.value = false }
        }
    }
    fun delete(entryId: String) {
        if (_saving.value) return
        _saving.value = true
        viewModelScope.launch {
            try { diary.delete(entryId); _selection.value = null; eventsChannel.send(UiEvent("Entry deleted")) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { eventsChannel.send(UiEvent("Couldn’t delete this entry. Please retry.")) }
            finally { _saving.value = false }
        }
    }
    fun savePreferences(value: UserPreferences) = viewModelScope.launch {
        try { settings.save(value) }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { eventsChannel.send(UiEvent("Couldn’t save preferences. Please retry.")) }
    }
    fun clearHistory() = viewModelScope.launch {
        try { diary.clear(); eventsChannel.send(UiEvent("Food history cleared")) }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { eventsChannel.send(UiEvent("Couldn’t clear history. Please retry.")) }
    }
}
