package com.lowerbackstretching.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Stretch(
    val id: String,
    val name: String,
    val bodyParts: List<String>,
    val durationSeconds: Int,
    val difficulty: String,
    val description: String,
    val youtubeId: String,
)

@Serializable
data class Program(
    val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val days: List<ProgramDay>,
)

@Serializable
data class ProgramDay(
    val day: Int,
    val title: String,
    val stretchIds: List<String>,
)
