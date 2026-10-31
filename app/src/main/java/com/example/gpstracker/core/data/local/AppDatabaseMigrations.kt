package com.example.gpstracker.core.data.local

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `recent_searches` (
                `placeId` TEXT NOT NULL,
                `primaryText` TEXT NOT NULL,
                `secondaryText` TEXT,
                `latitude` REAL,
                `longitude` REAL,
                `updatedAtMillis` INTEGER NOT NULL,
                PRIMARY KEY(`placeId`)
            )
            """.trimIndent(),
        )
        connection.execSQL("DROP TABLE IF EXISTS `placeholder`")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `collections` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `createdAtMillis` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `saved_places` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `placeId` TEXT,
                `name` TEXT NOT NULL,
                `address` TEXT,
                `latitude` REAL NOT NULL,
                `longitude` REAL NOT NULL,
                `latKey` INTEGER NOT NULL,
                `lngKey` INTEGER NOT NULL,
                `collectionId` INTEGER,
                `createdAtMillis` INTEGER NOT NULL,
                `iconKind` TEXT NOT NULL,
                FOREIGN KEY(`collectionId`) REFERENCES `collections`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent(),
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_saved_places_placeId` ON `saved_places` (`placeId`)",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_saved_places_collectionId` ON `saved_places` (`collectionId`)",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_saved_places_latKey_lngKey` ON `saved_places` (`latKey`, `lngKey`)",
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `route_history` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `originLabel` TEXT NOT NULL,
                `destLabel` TEXT NOT NULL,
                `destPlaceId` TEXT,
                `destLatitude` REAL NOT NULL,
                `destLongitude` REAL NOT NULL,
                `viaLabel` TEXT,
                `viaPlaceId` TEXT,
                `viaLatitude` REAL,
                `viaLongitude` REAL,
                `plannedDurationSeconds` INTEGER NOT NULL,
                `plannedDistanceMeters` INTEGER NOT NULL,
                `encodedPolyline` TEXT NOT NULL,
                `startedAtMillis` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_route_history_startedAtMillis` ON `route_history` (`startedAtMillis`)",
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE `route_history` ADD COLUMN `hasRouteStats` INTEGER NOT NULL DEFAULT 1",
        )
    }
}

