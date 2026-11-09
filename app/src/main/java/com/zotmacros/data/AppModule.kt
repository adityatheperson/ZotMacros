package com.zotmacros.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun supplement(json: Json): SupplementSource = SupplementSource(json)
    @Provides @Singleton fun json(): Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    @Provides @Singleton fun db(@ApplicationContext context: Context): ZotDatabase =
        Room.databaseBuilder(context, ZotDatabase::class.java, "zotmacros.db").build()
    @Provides fun diary(db: ZotDatabase): DiaryDao = db.diary()
    @Provides fun menus(db: ZotDatabase): MenuDao = db.menus()
    @Provides @Singleton fun api(json: Json): DiningApi {
        val client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS).addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().header("User-Agent", "ZotMacros-Android/1.0").build())
            }.build()
        return Retrofit.Builder().baseUrl("https://anteaterapi.com/v2/rest/").client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build().create(DiningApi::class.java)
    }
}
