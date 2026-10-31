package com.example.gpstracker.core.data.local

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RouteHistoryDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: RouteHistoryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .setDriver(AndroidSQLiteDriver())
            .build()
        dao = db.routeHistoryDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertQueryDelete() = runTest {
        val id = dao.insert(
            RouteHistoryEntity(
                originLabel = "Here",
                destLabel = "Mosque",
                destPlaceId = "p",
                destLatitude = 33.7,
                destLongitude = 73.0,
                viaLabel = "Blue Area",
                viaPlaceId = "v",
                viaLatitude = 33.71,
                viaLongitude = 73.05,
                plannedDurationSeconds = 600,
                plannedDistanceMeters = 2000,
                encodedPolyline = "_p~iF~ps|U",
                startedAtMillis = 10L,
            ),
        )
        assertEquals("Mosque", dao.getById(id)?.destLabel)
        assertEquals(1, dao.observeAll().first().size)
        dao.deleteById(id)
        assertNull(dao.getById(id))
    }
}
