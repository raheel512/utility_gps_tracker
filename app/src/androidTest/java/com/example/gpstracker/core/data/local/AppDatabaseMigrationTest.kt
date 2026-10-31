package com.example.gpstracker.core.data.local

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val dbFile = instrumentation.targetContext.getDatabasePath(TEST_DB)

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = instrumentation,
        file = dbFile,
        driver = AndroidSQLiteDriver(),
        databaseClass = AppDatabase::class,
    )

    @Test
    fun migrate2To3_preservesRecentSearchesAndCreatesSavedTables() = runBlocking {
        instrumentation.targetContext.deleteDatabase(TEST_DB)

        val connection = helper.createDatabase(2)
        connection.execSQL(
            """
            INSERT INTO recent_searches
            (placeId, primaryText, secondaryText, latitude, longitude, updatedAtMillis)
            VALUES ('pid', 'Cafe', 'Street', 1.0, 2.0, 10)
            """.trimIndent(),
        )
        connection.close()

        val migrated = helper.runMigrationsAndValidate(
            version = 3,
            migrations = listOf(MIGRATION_2_3),
        )
        migrated.prepare("SELECT primaryText FROM recent_searches WHERE placeId = ?").use { stmt ->
            stmt.bindText(1, "pid")
            check(stmt.step())
            check(stmt.getText(0) == "Cafe")
        }
        migrated.prepare(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='saved_places'",
        ).use { stmt ->
            check(stmt.step())
        }
        migrated.prepare(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='collections'",
        ).use { stmt ->
            check(stmt.step())
        }
        migrated.close()
    }

    @Test
    fun migrate3To4_preservesSavedPlacesAndCreatesRouteHistory() = runBlocking {
        instrumentation.targetContext.deleteDatabase(TEST_DB)

        val connection = helper.createDatabase(3)
        connection.execSQL(
            """
            INSERT INTO recent_searches
            (placeId, primaryText, secondaryText, latitude, longitude, updatedAtMillis)
            VALUES ('pid', 'Cafe', 'Street', 1.0, 2.0, 10)
            """.trimIndent(),
        )
        connection.close()

        val migrated = helper.runMigrationsAndValidate(
            version = 4,
            migrations = listOf(MIGRATION_3_4),
        )
        migrated.prepare("SELECT primaryText FROM recent_searches WHERE placeId = ?").use { stmt ->
            stmt.bindText(1, "pid")
            check(stmt.step())
            check(stmt.getText(0) == "Cafe")
        }
        migrated.prepare(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='route_history'",
        ).use { stmt ->
            check(stmt.step())
        }
        migrated.close()
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
