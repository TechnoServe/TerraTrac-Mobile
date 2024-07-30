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

//@Database(entities = [Farm::class, CollectionSite::class], version = 15, exportSchema = true)
//@TypeConverters(BitmapConverter::class, DateConverter::class)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun farmsDAO(): FarmDAO
//
//    companion object {
//        private var INSTANCE: AppDatabase? = null
//
//        fun getInstance(context: Context): AppDatabase {
//            synchronized(this) {
//                var instance = INSTANCE
//
//                if (instance == null) {
//                    instance =
//                        Room
//                            .databaseBuilder(
//                                context.applicationContext,
//                                AppDatabase::class.java,
//                                "farm_collector_database",
//                            ).fallbackToDestructiveMigration()
//                            .build()
//
//                    INSTANCE = instance
//                }
//
//                return instance
//            }
//        }
//    }
//}


@Database(entities = [Farm::class, CollectionSite::class], version = 16, exportSchema = true)
@TypeConverters(BitmapConverter::class, DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun farmsDAO(): FarmDAO

    companion object {
        private var INSTANCE: AppDatabase? = null

        // Define a migration from version 15 to 16
        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new temporary column with NULL support
                db.execSQL("ALTER TABLE Farms ADD COLUMN purchases_tmp REAL")

                // Copy data from the old column to the new column
                db.execSQL("UPDATE Farms SET purchases_tmp = purchases")

                // Drop the old purchases column
                db.execSQL("ALTER TABLE Farms DROP COLUMN purchases")

                // Rename the temporary column to purchases
                db.execSQL("ALTER TABLE Farms RENAME COLUMN purchases_tmp TO purchases")
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
                        .addMigrations(MIGRATION_15_16)
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}
