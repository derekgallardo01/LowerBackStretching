package com.lowerbackstretching.data.db

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CustomRoutineDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: CustomRoutineDao

    @Before
    fun setup() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java).build()
        dao = db.customRoutineDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_returns_generated_id_and_round_trips() = runBlocking {
        val id = dao.insert(routine(name = "Morning", csv = "cat-cow,child-pose", created = 1000L))
        assertThat(id).isGreaterThan(0L)
        val read = dao.byId(id)
        assertThat(read?.name).isEqualTo("Morning")
        assertThat(read?.stretchIds).containsExactly("cat-cow", "child-pose").inOrder()
    }

    @Test
    fun all_returns_newest_first() = runBlocking {
        dao.insert(routine(name = "Old", csv = "", created = 100L))
        dao.insert(routine(name = "Mid", csv = "", created = 200L))
        dao.insert(routine(name = "New", csv = "", created = 300L))
        val list = dao.all().first()
        assertThat(list.map { it.name }).containsExactly("New", "Mid", "Old").inOrder()
    }

    @Test
    fun update_replaces_existing_row() = runBlocking {
        val id = dao.insert(routine(name = "Old", csv = "a", created = 100L))
        val existing = dao.byId(id)!!
        dao.update(existing.copy(name = "New", stretchIdsCsv = "b,c"))
        val read = dao.byId(id)!!
        assertThat(read.name).isEqualTo("New")
        assertThat(read.stretchIds).containsExactly("b", "c").inOrder()
    }

    @Test
    fun delete_removes_row() = runBlocking {
        val id = dao.insert(routine(name = "x", csv = "a", created = 1L))
        dao.delete(dao.byId(id)!!)
        assertThat(dao.byId(id)).isNull()
        assertThat(dao.all().first()).isEmpty()
    }

    @Test
    fun byId_returns_null_for_unknown() = runBlocking {
        assertThat(dao.byId(99_999L)).isNull()
    }

    private fun routine(name: String, csv: String, created: Long) = CustomRoutineEntity(
        name = name, stretchIdsCsv = csv, createdAtEpochMillis = created,
    )
}
