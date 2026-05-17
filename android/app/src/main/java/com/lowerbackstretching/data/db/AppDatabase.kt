package com.lowerbackstretching.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SessionEntity::class, CustomRoutineEntity::class, ProgramProgressEntity::class],
    version = 5,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun customRoutineDao(): CustomRoutineDao
    abstract fun programProgressDao(): ProgramProgressDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        /** v2 → v3: add `type` column to sessions, default "program". */
        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE sessions ADD COLUMN type TEXT NOT NULL DEFAULT 'program'"
                )
            }
        }

        /** v3 → v4: add displayOrder + deletedAtEpochMillis to custom_routines. */
        internal val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE custom_routines ADD COLUMN displayOrder INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE custom_routines ADD COLUMN deletedAtEpochMillis INTEGER DEFAULT NULL"
                )
            }
        }

        /** v4 → v5: create program_progress table for per-program day bookmarks. */
        internal val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS program_progress (" +
                        "programId TEXT NOT NULL PRIMARY KEY, " +
                        "currentDay INTEGER NOT NULL, " +
                        "updatedAtEpochMillis INTEGER NOT NULL)"
                )
            }
        }

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "lowerback.db",
            )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}
