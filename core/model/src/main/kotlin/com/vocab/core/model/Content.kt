package com.vocab.core.model

/** A CEFR-style level such as Elementary. Units belong to exactly one level. */
data class Level(
    val id: String,
    val order: Int,
    val title: String,
    val titleFa: String,
    val units: List<UnitSummary>,
)

/** What the path screen needs about a unit without loading all of its words. */
data class UnitSummary(
    val id: String,
    val levelId: String,
    val order: Int,
    val title: String,
    val titleFa: String,
    val illustration: Illustration,
    val color: TopicColor,
    val wordCount: Int,
)

/** A unit with everything needed to teach and test it. */
data class VocabUnit(
    val summary: UnitSummary,
    val descriptionFa: String,
    val words: List<Word>,
    val gapQuestions: List<GapQuestion>,
    /** Optional reference shown beside gap questions, e.g. a family tree, one row per line. */
    val referenceRows: List<String>,
) {
    val id: String get() = summary.id
}

data class Word(
    val id: String,
    val unitId: String,
    val text: String,
    val meaningFa: String,
    val partOfSpeech: String?,
    val phonetic: String?,
    val example: String,
    val tipFa: String?,
    val illustration: Illustration?,
    val wordFamily: List<String>,
    val collocations: List<String>,
)

/** A hand-written fill-in-the-blank question. `sentence` contains exactly one `___`. */
data class GapQuestion(
    val id: String,
    val sentence: String,
    val answer: String,
    val hintFa: String?,
)

enum class TopicColor { PERIWINKLE, MINT, BUTTER, PINK, SKY, LILAC }

/** Keys of the bundled 3D illustrations. Content files refer to them by [key]. */
enum class Illustration(val key: String) {
    ANIMALS("animals"), BACKUP("backup"), BELL("bell"), BOOKS("books"), BRAIN("brain"),
    BULLSEYE("bullseye"), CALENDAR("calendar"), CHART("chart-increasing"), CHECK("check"),
    CLOTHES("clothes"), CROSS("cross"), FAMILY("family"), FIRE("fire"), FOOD("food"),
    GRADUATION_CAP("graduation-cap"), HEADPHONE("headphone"), HOME("home"), JOBS("jobs"),
    LIGHT_BULB("light-bulb"), LOCKED("locked"), OPEN_BOOK("open-book"), PARTY("party-popper"),
    SPARKLES("sparkles"), SPEAKER("speaker"), SPORT("sport"), STAR("star"), STUDENT("student"),
    TRAVEL("travel"), TROPHY("trophy"), WEATHER("weather");

    companion object {
        fun fromKey(key: String?): Illustration? = entries.firstOrNull { it.key == key }
    }
}
