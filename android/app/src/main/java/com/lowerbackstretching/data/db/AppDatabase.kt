package com.lowerbackstretching.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SessionEntity::class, CustomRoutineEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun customRoutineDao(): CustomRoutineDao

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

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "lowerback.db",
            )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}
