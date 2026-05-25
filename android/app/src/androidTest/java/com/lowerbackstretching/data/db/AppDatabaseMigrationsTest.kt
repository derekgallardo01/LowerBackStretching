package com.lowerbackstretching.data.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migration smoke tests. Each test seeds a fresh SQLite database with
 * an older schema, executes the relevant `AppDatabase.MIGRATION_X_Y`
 * SQL, then asserts the new shape (column added, row default, table
 * created). Doesn't depend on Room schema-export — we lay down the
 * starting schema by hand so the test is fully self-contained.
 *
 * Catches: typos in the migration SQL, missing DEFAULTs that would
 * break existing rows, columns referenced by entity classes but never
 * created. Doesn't catch full Room-schema equivalence — adopt
 * MigrationTestHelper + exportSchema if/when richer checks are needed.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationsTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val dbName = "migration-test.db"
    private lateinit var helper: SupportSQLiteOpenHelper

    @Before fun deleteOldDb() {
        context.deleteDatabase(dbName)
    }

    @After fun close() {
        if (::helper.isInitialized) helper.close()
        context.deleteDatabase(dbName)
    }

    /** Open a fresh database with [setup] establishing the starting schema. */
    private fun openWithSchema(setup: (SupportSQLiteDatabase) -> Unit): SupportSQLiteDatabase {
        val factory = FrameworkSQLiteOpenHelperFactory()
        helper = factory.create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        setup(db)
                    }
                    override fun onUpgrade(
                        db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int,
                    ) = Unit
                })
                .build()
        )
        return helper.writableDatabase
    }

    @Test fun migration_2_to_3_adds_type_column_defaulting_to_program() {
        val db = openWithSchema { v2 ->
            v2.execSQL(
                """
                CREATE TABLE sessions (
                  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                  programId TEXT NOT NULL,
                  dayNumber INTEGER NOT NULL,
                  completedAtEpochDay INTEGER NOT NULL,
                  completedAtEpochMillis INTEGER NOT NULL,
                  durationSeconds INTEGER NOT NULL
                )
                """.trimIndent()
            )
            v2.execSQL(
                "INSERT INTO sessions(programId, dayNumber, completedAtEpochDay, " +
                    "completedAtEpochMillis, durationSeconds) " +
                    "VALUES ('seven-day', 3, 19000, 1640995200000, 180)"
            )
        }

        AppDatabase.MIGRATION_2_3.migrate(db)

        db.query("SELECT type FROM sessions").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("program")
        }
    }

    @Test fun migration_3_to_4_adds_displayOrder_and_deletedAt_to_custom_routines() {
        val db = openWithSchema { v3 ->
            v3.execSQL(
                """
                CREATE TABLE custom_routines (
                  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                  name TEXT NOT NULL,
                  stretchIdsCsv TEXT NOT NULL,
                  createdAtEpochMillis INTEGER NOT NULL
                )
                """.trimIndent()
            )
            v3.execSQL(
                "INSERT INTO custom_routines(name, stretchIdsCsv, createdAtEpochMillis) " +
                    "VALUES ('Morning', 'cat-cow,child-pose', 1640995200000)"
            )
        }

        AppDatabase.MIGRATION_3_4.migrate(db)

        db.query("SELECT displayOrder, deletedAtEpochMillis FROM custom_routines").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getInt(0)).isEqualTo(0)
            assertThat(cursor.isNull(1)).isTrue()
        }
    }

    @Test fun migration_4_to_5_creates_program_progress_table() {
        val db = openWithSchema { /* no program_progress yet */ }

        AppDatabase.MIGRATION_4_5.migrate(db)

        // Should be able to insert + select without error.
        db.execSQL(
            "INSERT INTO program_progress(programId, currentDay, updatedAtEpochMillis) " +
                "VALUES ('seven-day', 4, 1640995200000)"
        )
        db.query("SELECT currentDay FROM program_progress WHERE programId = 'seven-day'").use { c ->
            assertThat(c.moveToFirst()).isTrue()
            assertThat(c.getInt(0)).isEqualTo(4)
        }
    }

    @Test fun migration_5_to_6_creates_flexibility_tests_table_with_nullable_metrics() {
        val db = openWithSchema { /* no flexibility_tests yet */ }

        AppDatabase.MIGRATION_5_6.migrate(db)

        db.execSQL(
            "INSERT INTO flexibility_tests(recordedAtEpochMillis, sitAndReachCm) " +
                "VALUES (1640995200000, 12.5)"
        )
        db.query(
            "SELECT recordedAtEpochMillis, sitAndReachCm, toeTouchCm, shoulderReachCm " +
                "FROM flexibility_tests"
        ).use { c ->
            assertThat(c.moveToFirst()).isTrue()
            assertThat(c.getLong(0)).isEqualTo(1640995200000L)
            assertThat(c.getFloat(1)).isEqualTo(12.5f)
            assertThat(c.isNull(2)).isTrue()
            assertThat(c.isNull(3)).isTrue()
        }
    }

    @Test fun migration_6_to_7_creates_pain_logs_table_with_indices() {
        val db = openWithSchema { /* no pain_logs yet */ }

        AppDatabase.MIGRATION_6_7.migrate(db)

        // Round-trip insert covering both PRE (sessionId NULL) and POST rows.
        db.execSQL(
            "INSERT INTO pain_logs(recordedAtEpochMillis, painLevel, bodyLocationTag, " +
                "context, sessionId) " +
                "VALUES (1640995200000, 6, 'lower-back', 'PRE_SESSION', NULL)"
        )
        db.execSQL(
            "INSERT INTO pain_logs(recordedAtEpochMillis, painLevel, bodyLocationTag, " +
                "context, sessionId) " +
                "VALUES (1640995260000, 3, NULL, 'POST_SESSION', 99)"
        )
        db.query(
            "SELECT painLevel, bodyLocationTag, context, sessionId FROM pain_logs " +
                "ORDER BY recordedAtEpochMillis ASC"
        ).use { c ->
            assertThat(c.moveToFirst()).isTrue()
            assertThat(c.getInt(0)).isEqualTo(6)
            assertThat(c.getString(1)).isEqualTo("lower-back")
            assertThat(c.getString(2)).isEqualTo("PRE_SESSION")
            assertThat(c.isNull(3)).isTrue()
            assertThat(c.moveToNext()).isTrue()
            assertThat(c.getInt(0)).isEqualTo(3)
            assertThat(c.isNull(1)).isTrue()
            assertThat(c.getString(2)).isEqualTo("POST_SESSION")
            assertThat(c.getLong(3)).isEqualTo(99L)
        }

        // Indices must be present so the hot-path queries don't full-scan.
        db.query(
            "SELECT name FROM sqlite_master WHERE type = 'index' AND tbl_name = 'pain_logs'"
        ).use { c ->
            val indexNames = mutableListOf<String>()
            while (c.moveToNext()) indexNames += c.getString(0)
            assertThat(indexNames).contains("index_pain_logs_recordedAtEpochMillis")
            assertThat(indexNames).contains("index_pain_logs_sessionId")
        }
    }
}
