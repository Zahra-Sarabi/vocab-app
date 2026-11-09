package com.vocab.core.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vocab.core.designsystem.theme.ClayShadow
import com.vocab.core.designsystem.theme.Dimens
import com.vocab.core.designsystem.theme.VocabTheme
import com.vocab.core.designsystem.theme.clay
import com.vocab.core.model.Illustration
import com.vocab.core.model.TopicColor

/** A puffy clay container. Defaults to a white card. */
@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    color: Color = VocabTheme.colors.surface,
    radius: Dp = Dimens.radiusLg,
    shadow: ClayShadow = VocabTheme.shadows.card,
    padding: Dp = Dimens.space5,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clay(color, radius, shadow)
            .clip(RoundedCornerShape(radius))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(Dimens.space3),
        content = content,
    )
}

enum class ClayButtonStyle { PRIMARY, SECONDARY, GO, GHOST }

/** The chunky clay button: it sits on a ledge and sinks 3dp while pressed. */
@Composable
fun ClayButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ClayButtonStyle = ClayButtonStyle.PRIMARY,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    val colors = VocabTheme.colors
    val shadows = VocabTheme.shadows
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val (fill, content, shadow) = when {
        !enabled -> Triple(colors.surfaceSunk, colors.inkMuted, shadows.sunk)
        style == ClayButtonStyle.PRIMARY -> Triple(colors.primaryStrong, colors.onPrimary, if (pressed) shadows.buttonPressed else shadows.button)
        style == ClayButtonStyle.GO -> Triple(colors.go, colors.onGo, if (pressed) shadows.buttonPressed else shadows.buttonGo)
        style == ClayButtonStyle.SECONDARY -> Triple(colors.surface, colors.ink, if (pressed) shadows.sunk else shadows.buttonSoft)
        else -> Triple(Color.Transparent, colors.primaryText, ClayShadow(emptyList()))
    }
    Row(
        modifier = modifier
            .padding(bottom = 4.dp)
            .offset(y = if (pressed && enabled && style != ClayButtonStyle.GHOST) 3.dp else 0.dp)
            .heightIn(min = Dimens.buttonHeight)
            .clay(fill, Dimens.radiusMd, shadow)
            .clip(RoundedCornerShape(Dimens.radiusMd))
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = Dimens.space5),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) Icon(leadingIcon, null, tint = content, modifier = Modifier.size(20.dp).padding(end = 0.dp))
        if (leadingIcon != null) Box(Modifier.width(Dimens.space2))
        Text(text, style = VocabTheme.type.labelFa.copy(fontSize = VocabTheme.type.bodyFa.fontSize), color = content, maxLines = 1)
        if (trailingIcon != null) Box(Modifier.width(Dimens.space2))
        if (trailingIcon != null) Icon(trailingIcon, null, tint = content, modifier = Modifier.size(20.dp))
    }
}

/** A round clay button holding one icon, e.g. pronunciation or close. */
@Composable
fun ClayIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = VocabTheme.colors.surface,
    tint: Color = VocabTheme.colors.ink,
    size: Dp = 44.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clay(container, size / 2, VocabTheme.shadows.raised)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.55f))
    }
}

/** A small puffy tag. Put a 16–20dp icon or [Art] in [leading]. */
@Composable
fun ClayPill(
    text: String,
    background: Color,
    content: Color,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .clay(background, Dimens.radiusFull, VocabTheme.shadows.pill)
            .padding(start = if (leading != null) 6.dp else Dimens.space3, end = Dimens.space3, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.space1),
    ) {
        leading?.invoke()
        Text(text, style = VocabTheme.type.captionFa.copy(fontWeight = androidx.compose.ui.text.font.FontWeight(800)), color = content)
    }
}

/** A sunken track with a puffy fill. [progress] is 0..1. */
@Composable
fun ClayProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    fill: Color = VocabTheme.colors.primary,
    track: Color = VocabTheme.colors.surfaceSunk,
    height: Dp = 12.dp,
    sunk: Boolean = true,
) {
    val value = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clay(track, height / 2, if (sunk) VocabTheme.shadows.sunk else ClayShadow(emptyList()))
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..1f) },
    ) {
        if (value > 0f) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value)
                    .clay(fill, height / 2, VocabTheme.shadows.pill),
            )
        }
    }
}

/** A square clay tile with an icon in [chip] colors, used for state marks and topic icons. */
@Composable
fun ClayThumb(
    illustration: Illustration,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    greyed: Boolean = false,
) {
    Box(
        modifier = modifier
            .size(size)
            .clay(if (greyed) VocabTheme.colors.surface else color, Dimens.radiusSm, VocabTheme.shadows.raised),
        contentAlignment = Alignment.Center,
    ) {
        Art(illustration, size * 0.7f, greyed = greyed)
    }
}

/** A square pastel tile with a 3D picture and a label: one topic. */
@Composable
fun TopicTile(
    title: String,
    illustration: Illustration,
    color: TopicColor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clay(VocabTheme.colors.topic(color), Dimens.radiusMd, VocabTheme.shadows.card)
            .clip(RoundedCornerShape(Dimens.radiusMd))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(Dimens.space2),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Art(illustration, 46.dp)
        Text(title, style = VocabTheme.type.labelFa, color = VocabTheme.colors.chipInk, textAlign = TextAlign.Center, maxLines = 1)
    }
}

enum class OptionState { REST, SELECTED, CORRECT, WRONG }

/** A full-width answer row with a lettered circle; correct and wrong reuse the selected build. */
@Composable
fun AnswerOption(
    letter: String?,
    text: String,
    state: OptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    letterColor: Color = VocabTheme.colors.mint,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = VocabTheme.colors
    val (fill, border, badge) = when (state) {
        OptionState.REST -> Triple(colors.surface, Color.Transparent, letterColor)
        OptionState.SELECTED -> Triple(colors.primarySoft, colors.primaryStrong, colors.primaryStrong)
        OptionState.CORRECT -> Triple(colors.correctSoft, colors.correct, colors.correct)
        OptionState.WRONG -> Triple(colors.wrongSoft, colors.wrong, colors.wrong)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .heightIn(min = 52.dp)
            .clay(fill, Dimens.radiusMd, VocabTheme.shadows.buttonSoft)
            .then(if (border != Color.Transparent) Modifier.border(2.dp, border, RoundedCornerShape(Dimens.radiusMd)) else Modifier)
            .clip(RoundedCornerShape(Dimens.radiusMd))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Dimens.space3, vertical = Dimens.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.space3),
    ) {
        if (letter != null) {
            Box(
                Modifier.size(32.dp).clay(badge, 16.dp, VocabTheme.shadows.raised),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    letter,
                    style = VocabTheme.type.labelFa.copy(fontFamily = com.vocab.core.designsystem.theme.Nunito),
                    color = if (state == OptionState.REST) colors.chipInk else colors.surface,
                )
            }
        }
        Text(
            text,
            modifier = Modifier.weight(1f),
            style = if (state == OptionState.REST) VocabTheme.type.bodyFa else VocabTheme.type.bodyFa.copy(fontWeight = androidx.compose.ui.text.font.FontWeight(700)),
            color = colors.ink,
        )
        trailing?.invoke(this)
    }
}

/** Earned stars in `star`, the rest in `line`. */
@Composable
fun StarsRow(stars: Int, modifier: Modifier = Modifier, max: Int = 3, size: Dp = 18.dp) {
    Row(modifier.semantics { contentDescription = "$stars از $max ستاره" }) {
        repeat(max) { index ->
            Icon(
                Icons.Rounded.Star,
                contentDescription = null,
                tint = if (index < stars) VocabTheme.colors.star else VocabTheme.colors.line,
                modifier = Modifier.size(size),
            )
        }
    }
}

/** A section title with an optional action on the opposite side. */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = VocabTheme.type.titleFa.copy(fontSize = VocabTheme.type.bodyFa.fontSize), color = VocabTheme.colors.ink, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            Text(
                action,
                style = VocabTheme.type.labelFa,
                color = VocabTheme.colors.primaryText,
                modifier = Modifier.clickable(role = Role.Button, onClick = onAction).padding(Dimens.space1),
            )
        }
    }
}

/** English text inside Persian layouts. */
@Composable
fun EnText(text: String, modifier: Modifier = Modifier, style: androidx.compose.ui.text.TextStyle = VocabTheme.type.wordMd, color: Color = VocabTheme.colors.ink) {
    Text(text, modifier = modifier, style = style, color = color)
}

/** Box with clay chrome, for custom layouts that still look like clay. */
@Composable
fun ClayBox(
    modifier: Modifier = Modifier,
    color: Color = VocabTheme.colors.surface,
    radius: Dp = Dimens.radiusMd,
    shadow: ClayShadow = VocabTheme.shadows.raised,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.clay(color, radius, shadow), contentAlignment = contentAlignment, content = content)
}

/** Keeps a column of text at a readable width on tablets. */
fun Modifier.readableWidth() = this.widthIn(max = 560.dp)
