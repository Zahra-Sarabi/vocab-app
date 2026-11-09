package com.vocab.feature.course

import androidx.annotation.StringRes
import com.vocab.core.model.LessonStep

@StringRes
internal fun LessonStep.label(): Int = when (this) {
    LessonStep.LEARN -> R.string.step_learn
    LessonStep.MEANING -> R.string.step_meaning
    LessonStep.MATCH -> R.string.step_match
    LessonStep.GAP -> R.string.step_gap
    LessonStep.LISTEN -> R.string.step_listen
    LessonStep.UNIT_TEST -> R.string.step_unit_test
}
