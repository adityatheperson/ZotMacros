package com.zotmacros

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zotmacros.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Opt-in historical live check against the Anteater API only. */
@RunWith(AndroidJUnit4::class)
class LiveMenuVerificationTest {
    @Test fun octoberEightMenuLoadsFromAnteaterApi() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("verifyLiveMenu") == "true")
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, ZotDatabase::class.java).build()
        try {
            val json = AppModule.json()
            val result = MenuRepository(AppModule.api(json), DiningParser(), db.menus(), json).load("anteatery", "2026-10-08", true)
            assertNull(result.error)
            val snapshot = result.snapshot!!
            assertTrue(snapshot.periods.isNotEmpty())
            val lunch = snapshot.periods.single { it.name == "Lunch" }
            assertTrue(lunch.foods.isNotEmpty())
            assertTrue(lunch.foods.all { it.station.isNotBlank() && it.dish.name.isNotBlank() })
        } finally { db.close() }
    }
}
