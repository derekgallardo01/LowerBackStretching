package com.lowerbackstretching.data

import android.content.Context
import com.lowerbackstretching.data.model.GlossaryEntry
import com.lowerbackstretching.data.model.Program
import com.lowerbackstretching.data.model.Stretch
import kotlinx.serialization.json.Json

class ContentRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    val stretches: List<Stretch> by lazy { loadList("stretches.json") }
    val programs: List<Program> by lazy { loadList("programs.json") }
    val glossary: List<GlossaryEntry> by lazy { loadList("glossary.json") }

    private val stretchById: Map<String, Stretch> by lazy { stretches.associateBy { it.id } }
    private val programById: Map<String, Program> by lazy { programs.associateBy { it.id } }

    fun stretch(id: String): Stretch? = stretchById[id]
    fun program(id: String): Program? = programById[id]

    fun stretchesFor(program: Program, day: Int): List<Stretch> {
        val d = program.days.firstOrNull { it.day == day } ?: return emptyList()
        return d.stretchIds.mapNotNull { stretchById[it] }
    }

    fun totalDurationSeconds(stretchIds: List<String>): Int =
        stretchIds.sumOf { stretchById[it]?.durationSeconds ?: 0 }

    private inline fun <reified T> loadList(filename: String): List<T> {
        val text = context.assets.open(filename).bufferedReader().use { it.readText() }
        return json.decodeFromString(text)
    }
}
