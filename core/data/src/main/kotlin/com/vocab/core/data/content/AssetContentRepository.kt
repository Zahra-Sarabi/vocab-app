package com.vocab.core.data.content

import android.content.Context
import com.vocab.core.domain.repository.ContentRepository
import com.vocab.core.model.GapQuestion
import com.vocab.core.model.Illustration
import com.vocab.core.model.Level
import com.vocab.core.model.TopicColor
import com.vocab.core.model.UnitSummary
import com.vocab.core.model.VocabUnit
import com.vocab.core.model.Word
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** Loads the course from `assets/content` once and keeps it in memory. */
@Singleton
class AssetContentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : ContentRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var cache: Course? = null

    override suspend fun levels(): List<Level> = course().levels

    override suspend fun unit(unitId: String): VocabUnit =
        course().units[unitId] ?: error("Unknown unit $unitId")

    override suspend fun words(wordIds: Collection<String>): Map<String, Word> {
        val all = course().words
        return wordIds.mapNotNull { id -> all[id]?.let { id to it } }.toMap()
    }

    private suspend fun course(): Course = mutex.withLock {
        cache ?: withContext(Dispatchers.IO) { load() }.also { cache = it }
    }

    private fun load(): Course {
        val course = json.decodeFromString<CourseDto>(readAsset("$ROOT/course.json"))
        val units = mutableMapOf<String, VocabUnit>()
        val levels = course.levels.map { level ->
            val levelUnits = level.units.map { path ->
                json.decodeFromString<UnitDto>(readAsset("$ROOT/$path")).toModel(level.id)
            }
            levelUnits.forEach { units[it.id] = it }
            Level(level.id, level.order, level.title, level.titleFa, levelUnits.map { it.summary })
        }
        return Course(levels, units, units.values.flatMap { it.words }.associateBy { it.id })
    }

    private fun readAsset(path: String) = context.assets.open(path).bufferedReader().use { it.readText() }

    private fun UnitDto.toModel(levelId: String): VocabUnit {
        val summary = UnitSummary(
            id = id,
            levelId = levelId,
            order = order,
            title = title,
            titleFa = titleFa,
            illustration = Illustration.fromKey(illustration) ?: Illustration.OPEN_BOOK,
            color = TopicColor.entries.firstOrNull { it.name.equals(color, ignoreCase = true) } ?: TopicColor.PERIWINKLE,
            wordCount = words.size,
        )
        return VocabUnit(
            summary = summary,
            descriptionFa = descriptionFa,
            words = words.map { w ->
                Word(
                    w.id, id, w.text, w.meaningFa, w.partOfSpeech, w.phonetic, w.example, w.tipFa,
                    Illustration.fromKey(w.illustration), w.wordFamily, w.collocations,
                )
            },
            gapQuestions = gapQuestions.map { GapQuestion(it.id, it.sentence, it.answer, it.hintFa) },
            referenceRows = referenceRows,
        )
    }

    private data class Course(val levels: List<Level>, val units: Map<String, VocabUnit>, val words: Map<String, Word>)

    private companion object {
        const val ROOT = "content"
    }
}
