package com.vocab.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.vocab.core.model.ThemeMode

/** Corner radii and spacing from the design system. */
object Dimens {
    val radiusSm = 12.dp
    val radiusMd = 20.dp
    val radiusLg = 28.dp
    val radiusFull = 999.dp
    val space1 = 4.dp
    val space2 = 8.dp
    val space3 = 12.dp
    val space4 = 16.dp
    val space5 = 20.dp
    val space6 = 24.dp
    val space8 = 32.dp
    val tapMin = 48.dp
    val buttonHeight = 48.dp
}

val LocalClayShadows = staticCompositionLocalOf { LightClayShadows }

/** Access point for the design system inside composables, like `MaterialTheme`. */
object VocabTheme {
    val colors: VocabColors
        @Composable @ReadOnlyComposable get() = LocalVocabColors.current
    val type: VocabTypography
        @Composable @ReadOnlyComposable get() = LocalVocabTypography.current
    val shadows: ClayShadows
        @Composable @ReadOnlyComposable get() = LocalClayShadows.current
}

/** Wraps the app: the Lavender palette, clay shadows, Persian type and right-to-left layout. */
@Composable
fun VocabTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors = if (dark) DarkVocabColors else LightVocabColors
    val material = if (dark) {
        darkColorScheme(primary = colors.primaryStrong, onPrimary = colors.onPrimary, background = colors.surfaceAlt, surface = colors.surface, onSurface = colors.ink, onBackground = colors.ink)
    } else {
        lightColorScheme(primary = colors.primaryStrong, onPrimary = colors.onPrimary, background = colors.surfaceAlt, surface = colors.surface, onSurface = colors.ink, onBackground = colors.ink)
    }
    CompositionLocalProvider(
        LocalVocabColors provides colors,
        LocalVocabTypography provides DefaultTypography,
        LocalClayShadows provides if (dark) DarkClayShadows else LightClayShadows,
        LocalLayoutDirection provides LayoutDirection.Rtl,
    ) {
        MaterialTheme(colorScheme = material, content = content)
    }
}
