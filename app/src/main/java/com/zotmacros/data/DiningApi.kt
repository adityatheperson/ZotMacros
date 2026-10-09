package com.zotmacros.data

import kotlinx.serialization.json.JsonElement
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DiningApi {
    @GET("dining/restaurants") suspend fun restaurants(): JsonElement
    @GET("dining/restaurantToday") suspend fun day(@Query("id") hall: String, @Query("date") date: String): JsonElement
    @GET("dining/dishes/batch") suspend fun dishes(@Query("ids") ids: String): JsonElement
    @GET("dining/dishes/{id}") suspend fun dish(@Path("id") id: String): JsonElement
}
