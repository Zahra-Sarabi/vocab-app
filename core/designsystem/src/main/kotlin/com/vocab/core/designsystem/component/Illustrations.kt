package com.vocab.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.vocab.core.designsystem.R
import com.vocab.core.model.Illustration

/** The bundled Fluent Emoji 3D picture for a key. */
@DrawableRes
fun Illustration.drawableRes(): Int = when (this) {
    Illustration.ANIMALS -> R.drawable.illus_animals
    Illustration.BACKUP -> R.drawable.illus_backup
    Illustration.BELL -> R.drawable.illus_bell
    Illustration.BOOKS -> R.drawable.illus_books
    Illustration.BRAIN -> R.drawable.illus_brain
    Illustration.BULLSEYE -> R.drawable.illus_bullseye
    Illustration.CALENDAR -> R.drawable.illus_calendar
    Illustration.CHART -> R.drawable.illus_chart_increasing
    Illustration.CHECK -> R.drawable.illus_check
    Illustration.CLOTHES -> R.drawable.illus_clothes
    Illustration.CROSS -> R.drawable.illus_cross
    Illustration.FAMILY -> R.drawable.illus_family
    Illustration.FIRE -> R.drawable.illus_fire
    Illustration.FOOD -> R.drawable.illus_food
    Illustration.GRADUATION_CAP -> R.drawable.illus_graduation_cap
    Illustration.HEADPHONE -> R.drawable.illus_headphone
    Illustration.HOME -> R.drawable.illus_home
    Illustration.JOBS -> R.drawable.illus_jobs
    Illustration.LIGHT_BULB -> R.drawable.illus_light_bulb
    Illustration.LOCKED -> R.drawable.illus_locked
    Illustration.OPEN_BOOK -> R.drawable.illus_open_book
    Illustration.PARTY -> R.drawable.illus_party_popper
    Illustration.SPARKLES -> R.drawable.illus_sparkles
    Illustration.SPEAKER -> R.drawable.illus_speaker
    Illustration.SPORT -> R.drawable.illus_sport
    Illustration.STAR -> R.drawable.illus_star
    Illustration.STUDENT -> R.drawable.illus_student
    Illustration.TRAVEL -> R.drawable.illus_travel
    Illustration.TROPHY -> R.drawable.illus_trophy
    Illustration.WEATHER -> R.drawable.illus_weather
}

private val greyscale = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

/** A decorative 3D picture. Pass [contentDescription] only when it carries meaning. */
@Composable
fun Art(
    illustration: Illustration,
    size: Dp,
    modifier: Modifier = Modifier,
    greyed: Boolean = false,
    contentDescription: String? = null,
) {
    Image(
        painter = painterResource(illustration.drawableRes()),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        colorFilter = if (greyed) greyscale else null,
        alpha = if (greyed) 0.55f else 1f,
    )
}
