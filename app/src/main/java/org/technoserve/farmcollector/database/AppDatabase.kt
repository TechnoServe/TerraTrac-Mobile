package org.technoserve.farmcollector.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.technoserve.farmcollector.database.converters.BitmapConverter
import org.technoserve.farmcollector.database.converters.DateConverter

@Database(entities = [Farm::class, CollectionSite::class], version = 19, exportSchema = true)
@TypeConverters(BitmapConverter::class, DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun farmsDAO(): FarmDAO

    companion object {
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_12_16 = object : Migration(12, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Step 1: Create a new temporary table with the updated schema
                db.execSQL("""
            CREATE TABLE new_Farms (
                siteId           INTEGER NOT NULL,
                remote_id        BLOB    NOT NULL,
                farmerPhoto      TEXT    NOT NULL,
                farmerName       TEXT    NOT NULL,
                memberId         TEXT    NOT NULL,
                village          TEXT    NOT NULL,
                district         TEXT    NOT NULL,
                purchases        REAL,
                size             REAL    NOT NULL,
                latitude         TEXT    NOT NULL,
                longitude        TEXT    NOT NULL,
                coordinates      TEXT,
                synced           INTEGER NOT NULL DEFAULT 0,
                scheduledForSync INTEGER NOT NULL DEFAULT 0,
                createdAt        INTEGER NOT NULL,
                updatedAt        INTEGER NOT NULL,
                needsUpdate      INTEGER NOT NULL DEFAULT 0,
                id               INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                FOREIGN KEY (siteId)
                REFERENCES CollectionSites (siteId) ON UPDATE NO ACTION
                                                    ON DELETE CASCADE
            )
        """.trimIndent())

                // Step 2: Copy data from the old table to the new table, setting needsUpdate to 0
                db.execSQL("""
            INSERT INTO new_Farms (
                siteId, remote_id, farmerPhoto, farmerName, memberId,
                village, district, purchases, size, latitude, longitude,
                coordinates, synced, scheduledForSync, createdAt, updatedAt, needsUpdate, id
            )
            SELECT
                siteId, remote_id, farmerPhoto, farmerName, memberId,
                village, district, purchases, size, latitude, longitude,
                coordinates, synced, scheduledForSync, createdAt, updatedAt, 0 AS needsUpdate, id
            FROM Farms
        """.trimIndent())

                // Step 3: Drop the old table
                db.execSQL("DROP TABLE Farms")

                // Step 4: Rename the new table to the original table name
                db.execSQL("ALTER TABLE new_Farms RENAME TO Farms")
            }
        }

        // Define a migration from version 15 to 16
        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Step 1: Create a new temporary table with the updated schema
                db.execSQL("""
            CREATE TABLE new_Farms (
                siteId           INTEGER NOT NULL,
                remote_id        BLOB    NOT NULL,
                farmerPhoto      TEXT    NOT NULL,
                farmerName       TEXT    NOT NULL,
                memberId         TEXT    NOT NULL,
                village          TEXT    NOT NULL,
                district         TEXT    NOT NULL,
                purchases        REAL,
                size             REAL    NOT NULL,
                latitude         TEXT    NOT NULL,
                longitude        TEXT    NOT NULL,
                coordinates      TEXT,
                synced           INTEGER NOT NULL DEFAULT 0,
                scheduledForSync INTEGER NOT NULL DEFAULT 0,
                createdAt        INTEGER NOT NULL,
                updatedAt        INTEGER NOT NULL,
                needsUpdate      INTEGER NOT NULL DEFAULT 0,
                id               INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                FOREIGN KEY (siteId)
                REFERENCES CollectionSites (siteId) ON UPDATE NO ACTION
                                                    ON DELETE CASCADE
            )
        """.trimIndent())

                // Step 2: Copy data from the old table to the new table, setting needsUpdate to 0
                db.execSQL("""
            INSERT INTO new_Farms (
                siteId, remote_id, farmerPhoto, farmerName, memberId,
                village, district, purchases, size, latitude, longitude,
                coordinates, synced, scheduledForSync, createdAt, updatedAt, needsUpdate, id
            )
            SELECT
                siteId, remote_id, farmerPhoto, farmerName, memberId,
                village, district, purchases, size, latitude, longitude,
                coordinates, synced, scheduledForSync, createdAt, updatedAt,0 AS needsUpdate, id
            FROM Farms
        """.trimIndent())

                // Step 3: Drop the old table
                db.execSQL("DROP TABLE Farms")

                // Step 4: Rename the new table to the original table name
                db.execSQL("ALTER TABLE new_Farms RENAME TO Farms")
            }
        }

        // Define a migration from version 16 to 17
        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Step 1: Create a new temporary table with the updated schema
                db.execSQL("""
            CREATE TABLE new_Farms (
                siteId           INTEGER NOT NULL,
                remote_id        BLOB    NOT NULL,
                farmerPhoto      TEXT    NOT NULL,
                farmerName       TEXT    NOT NULL,
                memberId         TEXT    NOT NULL,
                village          TEXT    NOT NULL,
                district         TEXT    NOT NULL,
                purchases        REAL,
                size             REAL    NOT NULL,
                latitude         TEXT    NOT NULL,
                longitude        TEXT    NOT NULL,
                coordinates      TEXT,
                synced           INTEGER NOT NULL DEFAULT 0,
                scheduledForSync INTEGER NOT NULL DEFAULT 0,
                createdAt        INTEGER NOT NULL,
                updatedAt        INTEGER NOT NULL,
                needsUpdate      INTEGER NOT NULL DEFAULT 0,
                id               INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                FOREIGN KEY (siteId)
                REFERENCES CollectionSites (siteId) ON UPDATE NO ACTION
                                                    ON DELETE CASCADE
            )
        """.trimIndent())

                // Step 2: Copy data from the old table to the new table, setting needsUpdate to 0
                db.execSQL("""
            INSERT INTO new_Farms (
                siteId, remote_id, farmerPhoto, farmerName, memberId,
                village, district, purchases, size, latitude, longitude,
                coordinates, synced, scheduledForSync, createdAt, updatedAt, needsUpdate, id
            )
            SELECT
                siteId, remote_id, farmerPhoto, farmerName, memberId,
                village, district, purchases, size, latitude, longitude,
                coordinates, 0 AS synced, 0 AS scheduledForSync, createdAt, updatedAt,0 AS needsUpdate, id
            FROM Farms
        """.trimIndent())

                // Step 3: Drop the old table
                db.execSQL("DROP TABLE Farms")

                // Step 4: Rename the new table to the original table name
                db.execSQL("ALTER TABLE new_Farms RENAME TO Farms")
            }
        }

        // Define a migration from version 17 to 18
        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Step 1: Create a new temporary table with the updated schema
                db.execSQL("""
            CREATE TABLE new_Farms (
                siteId           INTEGER NOT NULL,
                remote_id        BLOB    NOT NULL,
                farmerPhoto      TEXT    NOT NULL,
                farmerName       TEXT    NOT NULL,
                memberId         TEXT    NOT NULL,
                village          TEXT    NOT NULL,
                district         TEXT    NOT NULL,
                purchases        REAL,
                size             REAL    NOT NULL,
                latitude         TEXT    NOT NULL,
                longitude        TEXT    NOT NULL,
                coordinates      TEXT,
                synced           INTEGER NOT NULL DEFAULT 0,
                scheduledForSync INTEGER NOT NULL DEFAULT 0,
                createdAt        INTEGER NOT NULL,
                updatedAt        INTEGER NOT NULL,
                needsUpdate      INTEGER NOT NULL DEFAULT 0,
                id               INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                FOREIGN KEY (siteId)
                REFERENCES CollectionSites (siteId) ON UPDATE NO ACTION
                                                    ON DELETE CASCADE
            )
        """.trimIndent())

                // Step 2: Copy data from the old table to the new table, setting needsUpdate to 0
                db.execSQL("""
            INSERT INTO new_Farms (
                siteId, remote_id, farmerPhoto, farmerName, memberId,
                village, district, purchases, size, latitude, longitude,
                coordinates, synced, scheduledForSync, createdAt, updatedAt, needsUpdate, id
            )
            SELECT
                siteId, remote_id, farmerPhoto, farmerName, memberId,
                village, district, purchases, size, latitude, longitude,
                coordinates, 0 AS synced, 0 AS scheduledForSync, createdAt, updatedAt,0 AS needsUpdate, id
            FROM Farms
        """.trimIndent())

                // Step 3: Drop the old table
                db.execSQL("DROP TABLE Farms")

                // Step 4: Rename the new table to the original table name
                db.execSQL("ALTER TABLE new_Farms RENAME TO Farms")
            }
        }

        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
            CREATE TABLE new_Farms (
                siteId           INTEGER NOT NULL,
                remote_id        BLOB    NOT NULL,
                farmerPhoto      TEXT    NOT NULL,
                farmerName       TEXT    NOT NULL,
                memberId         TEXT    NOT NULL,
                village          TEXT    NOT NULL,
                district         TEXT    NOT NULL,
                purchases        REAL,
                size             REAL    NOT NULL,
                latitude         TEXT    NOT NULL,
                longitude        TEXT    NOT NULL,
                coordinates      TEXT,
                synced           INTEGER NOT NULL DEFAULT 0,
                scheduledForSync INTEGER NOT NULL DEFAULT 0,
                createdAt        INTEGER NOT NULL,
                updatedAt        INTEGER NOT NULL,
                needsUpdate      INTEGER NOT NULL DEFAULT 0,
                id               INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                FOREIGN KEY (siteId)
                REFERENCES CollectionSites (siteId) ON UPDATE NO ACTION
                                                    ON DELETE CASCADE
            )
        """.trimIndent())

                db.execSQL("""
            INSERT INTO new_Farms (
                siteId, remote_id, farmerPhoto, farmerName, memberId,
                village, district, purchases, size, latitude, longitude,
                coordinates, synced, scheduledForSync, createdAt, updatedAt, needsUpdate, id
            )
            SELECT
                siteId, remote_id, farmerPhoto, farmerName, memberId,
                village, district, purchases, size, latitude, longitude,
                coordinates, 0 AS synced, 0 AS scheduledForSync, createdAt, updatedAt, 0 AS needsUpdate, id
            FROM Farms
        """.trimIndent())

                db.execSQL("DROP TABLE Farms")
                db.execSQL("ALTER TABLE new_Farms RENAME TO Farms")
            }
        }


        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "farm_collector_database"
                    )
                        .addMigrations(MIGRATION_12_16,MIGRATION_15_16,MIGRATION_16_17,MIGRATION_17_18,MIGRATION_18_19)
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}
