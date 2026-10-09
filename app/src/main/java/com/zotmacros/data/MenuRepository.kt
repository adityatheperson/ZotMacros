package com.zotmacros.data

import com.zotmacros.domain.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepository @Inject constructor(
    private val api: DiningApi, private val parser: DiningParser, private val dao: MenuDao, private val json: Json
) {
    private val mutex = Mutex()
    private var stations: Map<String, Map<String, String>> = emptyMap()
    suspend fun cached(hall: String, date: String): MenuSnapshot? = decode(dao.get(hall, date) ?: dao.latest(hall, date))
    private fun decode(cache: CachedMenu?): MenuSnapshot? = cache?.let { runCatching { json.decodeFromString<MenuSnapshot>(it.payload) }.getOrNull() }
    suspend fun load(hall: String, date: String, force: Boolean = false): MenuResult = mutex.withLock {
        val exact = decode(dao.get(hall, date))
        if (!force && exact != null && System.currentTimeMillis() - exact.fetchedAt < 15 * 60_000) return@withLock MenuResult(exact, cached = true)
        try {
            val stationNames = stations[hall] ?: parser.stations(api.restaurants(), hall).also { stations = stations + (hall to it) }
            val periods = parser.day(api.day(hall, date), hall)
            val ids = periods.flatMap { it.stations.values.flatten() }.distinct()
            val dishes = ids.chunked(20).flatMap { parser.dishes(api.dishes(it.joinToString(","))) }.associateBy { it.id }
            val snapshot = MenuSnapshot(hall, date, System.currentTimeMillis(), periods.map { period ->
                MenuPeriod(period.id, period.name, period.start, period.end, period.stations.flatMap { (stationId, dishIds) ->
                    dishIds.mapNotNull { id -> dishes[id]?.let { MenuFood(it, stationId, stationNames[stationId] ?: "Station $stationId") } }
                })
            }, ids.count { it !in dishes })
            dao.put(CachedMenu(hall, date, snapshot.fetchedAt, json.encodeToString(snapshot)))
            MenuResult(snapshot)
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            val message = when (e) {
                is HttpException -> when (e.code()) {
                    429 -> "The dining service is busy. Wait a moment, then retry."
                    404 -> "No menu is available for this hall and date."
                    else -> "The dining service is unavailable. Please retry."
                }
                is IOException -> "Can’t reach the dining service. Check your connection and retry."
                else -> "We couldn’t read this menu. Please retry."
            }
            MenuResult(exact ?: decode(dao.latest(hall, date)), message, cached = true)
        }
    }
}
