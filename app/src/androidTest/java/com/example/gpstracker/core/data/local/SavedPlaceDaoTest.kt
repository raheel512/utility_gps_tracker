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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SavedPlaceDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: SavedPlaceDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .setDriver(AndroidSQLiteDriver())
            .build()
        dao = db.savedPlaceDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertQueryDelete() = runTest {
        val id = dao.insert(
            SavedPlaceEntity(
                placeId = "p1",
                name = "Home",
                address = "Islamabad",
                latitude = 33.7,
                longitude = 73.0,
                latKey = coordinateKey(33.7),
                lngKey = coordinateKey(73.0),
                collectionId = null,
                createdAtMillis = 1L,
                iconKind = SavedPlaceEntity.IconKind.HOME,
            ),
        )
        assertEquals("Home", dao.getById(id)?.name)
        assertEquals(1, dao.observeAll().first().size)
        dao.deleteById(id)
        assertNull(dao.getById(id))
    }

    @Test
    fun findByPlaceIdAndCoordinates() = runTest {
        dao.insert(
            SavedPlaceEntity(
                placeId = "abc",
                name = "Cafe",
                address = null,
                latitude = 33.68441,
                longitude = 73.04791,
                latKey = coordinateKey(33.68441),
                lngKey = coordinateKey(73.04791),
                collectionId = null,
                createdAtMillis = 1L,
            ),
        )
        assertNotNull(dao.findByPlaceId("abc"))
        assertNotNull(
            dao.findByCoordinateKeys(
                coordinateKey(33.68442),
                coordinateKey(73.04792),
            ),
        )
    }
}
