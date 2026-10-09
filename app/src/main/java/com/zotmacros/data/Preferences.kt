package com.zotmacros.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.zotmacros.domain.Nutrient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.preferences by preferencesDataStore("zot_preferences")
data class UserPreferences(
    val onboarded: Boolean = false, val calories: Double = 2400.0, val protein: Double = 160.0,
    val carbs: Double = 250.0, val fat: Double = 75.0, val hall: String = "anteatery",
    val meal: String = "Automatic", val autoRefresh: Boolean = true, val haptics: Boolean = true,
    val exampleGoals: Boolean = true
) {
    fun goal(n: Nutrient): Double = when(n) {
        Nutrient.CALORIES -> calories; Nutrient.PROTEIN -> protein; Nutrient.CARBS -> carbs; Nutrient.FAT -> fat; else -> 0.0
    }
}
@Singleton
class PreferencesRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private object Keys {
        val onboarded = booleanPreferencesKey("onboarded")
        val calories = doublePreferencesKey("calories")
        val protein = doublePreferencesKey("protein")
        val carbs = doublePreferencesKey("carbs")
        val fat = doublePreferencesKey("fat")
        val hall = stringPreferencesKey("hall")
        val meal = stringPreferencesKey("meal")
        val refresh = booleanPreferencesKey("auto_refresh")
        val haptics = booleanPreferencesKey("haptics")
        val example = booleanPreferencesKey("example_goals")
    }
    val preferences = context.preferences.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { p ->
        UserPreferences(p[Keys.onboarded] ?: false, p[Keys.calories] ?: 2400.0, p[Keys.protein] ?: 160.0,
            p[Keys.carbs] ?: 250.0, p[Keys.fat] ?: 75.0, p[Keys.hall] ?: "anteatery", p[Keys.meal] ?: "Automatic",
            p[Keys.refresh] ?: true, p[Keys.haptics] ?: true, p[Keys.example] ?: true)
    }
    suspend fun save(value: UserPreferences) {
        require(listOf(value.calories, value.protein, value.carbs, value.fat).all { it.isFinite() && it > 0 && it <= 100_000 })
        context.preferences.edit {
            it[Keys.onboarded] = value.onboarded; it[Keys.calories] = value.calories; it[Keys.protein] = value.protein
            it[Keys.carbs] = value.carbs; it[Keys.fat] = value.fat; it[Keys.hall] = value.hall
            it[Keys.meal] = value.meal; it[Keys.refresh] = value.autoRefresh; it[Keys.haptics] = value.haptics
            it[Keys.example] = value.exampleGoals
        }
    }
}
