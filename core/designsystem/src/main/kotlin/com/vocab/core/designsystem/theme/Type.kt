package com.vocab.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import com.vocab.core.designsystem.R

@OptIn(ExperimentalTextApi::class)
private fun variable(res: Int, weight: Int) = Font(
    resId = res,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

/** Persian interface text. */
val Vazirmatn = FontFamily(
    variable(R.font.vazirmatn, 400),
    variable(R.font.vazirmatn, 500),
    variable(R.font.vazirmatn, 700),
    variable(R.font.vazirmatn, 800),
)

/** English words being learned, and big numbers. */
val Nunito = FontFamily(
    variable(R.font.nunito, 600),
    variable(R.font.nunito, 800),
    variable(R.font.nunito, 900),
)

@Immutable
data class VocabTypography(
    val displayFa: TextStyle,
    val titleFa: TextStyle,
    val bodyFa: TextStyle,
    val labelFa: TextStyle,
    val captionFa: TextStyle,
    val wordXl: TextStyle,
    val wordMd: TextStyle,
    val exampleEn: TextStyle,
    val number: TextStyle,
)

private val fa = TextStyle(fontFamily = Vazirmatn, textDirection = TextDirection.ContentOrRtl)
private val en = TextStyle(fontFamily = Nunito, textDirection = TextDirection.Ltr)

val DefaultTypography = VocabTypography(
    displayFa = fa.copy(fontSize = 24.sp, lineHeight = 36.sp, fontWeight = FontWeight(800)),
    titleFa = fa.copy(fontSize = 18.sp, lineHeight = 28.sp, fontWeight = FontWeight(800)),
    bodyFa = fa.copy(fontSize = 16.sp, lineHeight = 28.sp, fontWeight = FontWeight(400)),
    labelFa = fa.copy(fontSize = 14.sp, lineHeight = 22.sp, fontWeight = FontWeight(700)),
    captionFa = fa.copy(fontSize = 12.sp, lineHeight = 20.sp, fontWeight = FontWeight(500)),
    wordXl = en.copy(fontSize = 36.sp, lineHeight = 44.sp, fontWeight = FontWeight(900)),
    wordMd = en.copy(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight(800)),
    exampleEn = en.copy(fontSize = 16.sp, lineHeight = 26.sp, fontWeight = FontWeight(600), fontStyle = FontStyle.Italic),
    number = en.copy(fontSize = 28.sp, lineHeight = 32.sp, fontWeight = FontWeight(900)),
)

val LocalVocabTypography = staticCompositionLocalOf { DefaultTypography }
