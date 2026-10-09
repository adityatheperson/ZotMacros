package com.zotmacros.data

import com.zotmacros.domain.MenuSnapshot
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface UciDiningApi {
    @GET("graphql") suspend fun query(@Query("query") query: String, @Query("variables") variables: String): JsonElement
}

/** Direct public UCI menu access. No hosted proxy, credentials, or diary uploads. */
class SupplementSource(json: Json, val enabled: Boolean = true, suppliedApi: UciDiningApi? = null) {
    private val parser = UciMenuParser()
    private val api = suppliedApi ?: Retrofit.Builder()
        .baseUrl("https://api.elevate-dxp.com/api/mesh/c087f756-cc72-4649-a36f-3a41b700c519/")
        .client(OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).callTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain -> chain.proceed(chain.request().newBuilder()
                .header("store", "ch_uci_en").header("magento-store-code", "ch_uci")
                .header("magento-website-code", "ch_uci").header("magento-store-view-code", "ch_uci_en")
                .header("x-api-key", "ElevateAPIProd") // Public storefront identifier, not an account secret.
                .header("Origin", "https://uci.mydininghub.com").header("Referer", "https://uci.mydininghub.com/").build()) }.build())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build().create(UciDiningApi::class.java)
    suspend fun complete(snapshot: MenuSnapshot): MenuSnapshot {
        require(snapshot.hall in listOf("anteatery", "brandywine"))
        val location = if (snapshot.hall == "anteatery") "the-anteatery" else "brandywine"
        val metadata = parser.data(api.query(locationQuery, buildJsonObject { put("locationUrlKey", location) }.toString()))
        val stations = metadata.getValue("getLocation").jsonObject.getValue("commerceAttributes").jsonObject.getValue("children").jsonArray.associate {
            it.jsonObject.getValue("id").jsonPrimitive.content to it.jsonObject.getValue("name").jsonPrimitive.content
        }
        val meals = metadata.getValue("Commerce_mealPeriods").jsonArray.associate {
            it.jsonObject.getValue("name").jsonPrimitive.content to it.jsonObject.getValue("id").jsonPrimitive.int
        }
        // Sequential calls bound upstream load. The repository caches the completed hall/date.
        val schedules = metadata.getValue("getLocation").jsonObject.getValue("aemAttributes").jsonObject
            .getValue("hoursOfOperation").jsonObject.getValue("schedule").jsonArray
        val discovered = uciPeriods(schedules, snapshot.date, meals)
        val requestedPeriods = (snapshot.periods + discovered.filter { fresh -> snapshot.periods.none { it.name == fresh.name } })
            .sortedWith(compareBy({ it.start ?: "99:99" }, { it.name }))
        val periods = requestedPeriods.map { period ->
            val mealId = meals[period.name] ?: throw IllegalArgumentException("Unknown UCI meal period")
            val variables = buildJsonObject {
                put("locationUrlKey", location); put("date", snapshot.date); put("mealPeriod", mealId); put("viewType", "WEEKLY")
            }
            val foods = parser.foods(api.query(menuQuery, variables.toString()), snapshot.date, stations)
            require(foods.all { it.stationId in stations }) { "Unknown UCI station" }
            mergeSupplement(period, foods)
        }
        return snapshot.copy(periods = periods, supplementVerified = true)
    }
    companion object {
        val locationQuery = """query(${ '$' }locationUrlKey:String!){getLocation(campusUrlKey:"campus",locationUrlKey:${ '$' }locationUrlKey){commerceAttributes{children{id name}} aemAttributes{hoursOfOperation{schedule}}} Commerce_mealPeriods {id name}}"""
        val menuQuery = """query(${ '$' }locationUrlKey:String!,${ '$' }date:String!,${ '$' }mealPeriod:Int,${ '$' }viewType:Commerce_MenuViewType!){getLocationRecipes(campusUrlKey:"campus",locationUrlKey:${ '$' }locationUrlKey,date:${ '$' }date,mealPeriod:${ '$' }mealPeriod,viewType:${ '$' }viewType){locationRecipesMap{dateSkuMap{date stations{id skus{simple configurable{sku variants}}}}} products{items{sku name attributes{name value}}}}}"""
    }
}
