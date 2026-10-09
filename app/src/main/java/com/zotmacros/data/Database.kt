package com.zotmacros.data

import androidx.room.*
import com.zotmacros.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

@Entity(tableName = "food_entries", indices = [Index("date")])
data class FoodEntry(
    @PrimaryKey val id: String,
    val date: String,
    val timestamp: Long,
    val dishId: String,
    val name: String,
    val hall: String,
    val station: String,
    val period: String,
    val meal: Meal,
    val quantity: Double,
    val serving: String,
    val perServing: Nutrition,
    val totals: Nutrition
)
@Entity(tableName = "menu_cache", primaryKeys = ["hall", "date"])
data class CachedMenu(val hall: String, val date: String, val fetchedAt: Long, val payload: String)

class Converters {
    @TypeConverter fun nutritionToJson(value: Nutrition): String = Json.encodeToString(value)
    @TypeConverter fun jsonToNutrition(value: String): Nutrition = Json.decodeFromString(value)
    @TypeConverter fun mealToString(value: Meal): String = value.name
    @TypeConverter fun stringToMeal(value: String): Meal = Meal.valueOf(value)
}
@Dao
interface DiaryDao {
    @Query("SELECT * FROM food_entries ORDER BY date DESC, timestamp ASC") fun observeAll(): Flow<List<FoodEntry>>
    @Query("SELECT * FROM food_entries WHERE id = :id") suspend fun find(id: String): FoodEntry?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(entry: FoodEntry): Long
    @Update suspend fun update(entry: FoodEntry)
    @Query("DELETE FROM food_entries WHERE id = :id") suspend fun delete(id: String)
    @Query("DELETE FROM food_entries") suspend fun clear()
}
@Dao
interface MenuDao {
    @Query("SELECT * FROM menu_cache WHERE hall = :hall AND date = :date LIMIT 1") suspend fun get(hall: String, date: String): CachedMenu?
    @Query("SELECT * FROM menu_cache WHERE hall = :hall AND date <= :date ORDER BY date DESC LIMIT 1") suspend fun latest(hall: String, date: String): CachedMenu?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun put(menu: CachedMenu)
}
@Database(entities = [FoodEntry::class, CachedMenu::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class ZotDatabase : RoomDatabase() {
    abstract fun diary(): DiaryDao
    abstract fun menus(): MenuDao
}
