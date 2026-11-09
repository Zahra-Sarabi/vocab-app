package com.vocab.core.data.content

import kotlinx.serialization.Serializable

/** `assets/content/course.json`: the levels and the unit files of each, in order. */
@Serializable
data class CourseDto(val version: Int, val levels: List<LevelDto>)

@Serializable
data class LevelDto(val id: String, val order: Int, val title: String, val titleFa: String, val units: List<String> = emptyList())

/** One unit file under `assets/content/`. See docs/CONTENT_FORMAT.md. */
@Serializable
data class UnitDto(
    val id: String,
    val order: Int,
    val title: String,
    val titleFa: String,
    val illustration: String,
    val color: String,
    val descriptionFa: String = "",
    val referenceRows: List<String> = emptyList(),
    val words: List<WordDto>,
    val gapQuestions: List<GapQuestionDto> = emptyList(),
)

@Serializable
data class WordDto(
    val id: String,
    val text: String,
    val meaningFa: String,
    val partOfSpeech: String? = null,
    val phonetic: String? = null,
    val example: String,
    val tipFa: String? = null,
    val illustration: String? = null,
    val wordFamily: List<String> = emptyList(),
    val collocations: List<String> = emptyList(),
)

@Serializable
data class GapQuestionDto(val id: String, val sentence: String, val answer: String, val hintFa: String? = null)
