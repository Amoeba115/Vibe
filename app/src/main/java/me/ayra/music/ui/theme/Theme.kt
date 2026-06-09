package me.ayra.music.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import me.ayra.music.util.MusicPreferences

const val THEME_SEED_SYSTEM = MusicPreferences.THEME_SEED_SYSTEM
const val THEME_SEED_NEUTRAL = MusicPreferences.THEME_SEED_NEUTRAL

enum class ThemeMode(
    val label: String,
) {
    Auto("Follow system"),
    Light("Light"),
    Dark("Dark"),
}

class ThemeState(
    initialThemeMode: ThemeMode = ThemeMode.Auto,
    initialThemeColorSeed: String = THEME_SEED_SYSTEM,
    initialAmoledMode: Boolean = false,
    initialColorScheme: ColorScheme = lightColorScheme(),
    private val onThemeModeChange: (ThemeMode) -> Unit = {},
    private val onThemeColorSeedChange: (String) -> Unit = {},
    private val onAmoledModeChange: (Boolean) -> Unit = {},
) {
    val themeMode = MutableStateFlow(initialThemeMode)
    val themeColorSeed = MutableStateFlow(initialThemeColorSeed)
    val amoledMode = MutableStateFlow(initialAmoledMode)
    val colorScheme = MutableStateFlow(initialColorScheme)

    fun setThemeMode(mode: ThemeMode) {
        if (mode == themeMode.value) return
        themeMode.value = mode
        onThemeModeChange(mode)
    }

    fun setThemeColorSeed(seed: String) {
        if (seed == themeColorSeed.value) return
        themeColorSeed.value = seed
        onThemeColorSeedChange(seed)
    }

    fun setAmoledMode(enabled: Boolean) {
        if (enabled == amoledMode.value) return
        amoledMode.value = enabled
        onAmoledModeChange(enabled)
    }
}

val LocalThemeState = staticCompositionLocalOf { ThemeState() }

data class ColorPaletteOption(
    val name: String,
    val seedArgb: Int,
) {
    val hexKey: String = "%08X".format(seedArgb)
}

val presetPalettes =
    listOf(
        ColorPaletteOption("Blue", 0xFF247EE0.toInt()),
        ColorPaletteOption("Green", 0xFF2E7D32.toInt()),
        ColorPaletteOption("Cyan", 0xFF00838F.toInt()),
        ColorPaletteOption("Rose", 0xFFC2185B.toInt()),
        ColorPaletteOption("Amber", 0xFFFF8F00.toInt()),
        ColorPaletteOption("Violet", 0xFF7E57C2.toInt()),
    )

private val DarkColorScheme = darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)
private val LightColorScheme = lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)

@Composable
fun MusicTheme(
    themeMode: ThemeMode = ThemeMode.Auto,
    themeColorSeed: String = THEME_SEED_SYSTEM,
    amoledMode: Boolean = false,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onThemeColorSeedChange: (String) -> Unit = {},
    onAmoledModeChange: (Boolean) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val themeState =
        remember {
            ThemeState(
                initialThemeMode = themeMode,
                initialThemeColorSeed = themeColorSeed,
                initialAmoledMode = amoledMode,
                initialColorScheme = musicColorScheme(context, themeMode, themeColorSeed, amoledMode, systemDark),
                onThemeModeChange = onThemeModeChange,
                onThemeColorSeedChange = onThemeColorSeedChange,
                onAmoledModeChange = onAmoledModeChange,
            )
        }
    val effectiveThemeMode by themeState.themeMode.collectAsState()
    val effectiveThemeColorSeed by themeState.themeColorSeed.collectAsState()
    val effectiveAmoledMode by themeState.amoledMode.collectAsState()
    val colorScheme by themeState.colorScheme.collectAsState()

    LaunchedEffect(themeMode) { themeState.themeMode.value = themeMode }
    LaunchedEffect(themeColorSeed) { themeState.themeColorSeed.value = themeColorSeed }
    LaunchedEffect(amoledMode) { themeState.amoledMode.value = amoledMode }
    LaunchedEffect(context, effectiveThemeMode, effectiveThemeColorSeed, effectiveAmoledMode, systemDark) {
        themeState.colorScheme.value =
            musicColorScheme(context, effectiveThemeMode, effectiveThemeColorSeed, effectiveAmoledMode, systemDark)
    }

    CompositionLocalProvider(LocalThemeState provides themeState) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}

fun musicColorScheme(
    context: android.content.Context,
    themeMode: ThemeMode,
    themeColorSeed: String,
    amoledMode: Boolean,
    systemDarkTheme: Boolean,
): ColorScheme {
    val darkTheme =
        when (themeMode) {
            ThemeMode.Auto -> systemDarkTheme
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
        }
    val amoled = darkTheme && amoledMode
    return when {
        themeColorSeed == THEME_SEED_NEUTRAL -> neutralColorScheme(darkTheme, amoled)
        themeColorSeed != THEME_SEED_SYSTEM -> {
            val seedArgb = themeColorSeed.toLongOrNull(16)?.toInt() ?: Purple40.toArgb()
            colorSchemeFromSeed(seedArgb, darkTheme, amoled)
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context).maybeAmoled(amoled) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme.maybeAmoled(amoled)
        else -> LightColorScheme
    }
}

fun colorSchemeFromSeed(
    seedArgb: Int,
    isDark: Boolean,
    amoled: Boolean = false,
): ColorScheme {
    val primary = seedArgb.tone(if (isDark) 80f else 42f, minSaturation = 0.45f)
    val secondary = seedArgb.shiftHue(32f).tone(if (isDark) 78f else 40f, minSaturation = 0.30f)
    val tertiary = seedArgb.shiftHue(82f).tone(if (isDark) 78f else 42f, minSaturation = 0.34f)
    return seededColorScheme(seedArgb, primary, secondary, tertiary, isDark, amoled)
}

fun neutralColorScheme(
    isDark: Boolean,
    amoled: Boolean = false,
): ColorScheme =
    seededColorScheme(
        seedArgb = 0xFF247EE0.toInt(),
        primary = 0xFF247EE0.toInt().tone(if (isDark) 80f else 42f, minSaturation = 0.25f),
        secondary = 0xFF5F6368.toInt().tone(if (isDark) 78f else 42f),
        tertiary = 0xFF747775.toInt().tone(if (isDark) 78f else 42f),
        isDark = isDark,
        amoled = amoled,
        neutral = true,
    )

private fun seededColorScheme(
    seedArgb: Int,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    isDark: Boolean,
    amoled: Boolean,
    neutral: Boolean = false,
): ColorScheme {
    val surfaceHue = if (neutral) 0f else seedArgb.hue()
    val surfaceSaturation = if (neutral) 0f else 0.18f
    val variantSaturation = if (neutral) 0f else 0.20f
    return (
        if (isDark) {
            darkColorScheme(
                primary = primary,
                onPrimary = primary.onColor(),
                primaryContainer = seedArgb.tone(26f),
                onPrimaryContainer = seedArgb.tone(90f, minSaturation = 0.30f),
                secondary = secondary,
                onSecondary = secondary.onColor(),
                secondaryContainer = secondary.toArgb().tone(25f, minSaturation = 0.20f),
                onSecondaryContainer = secondary.toArgb().tone(90f, minSaturation = 0.20f),
                tertiary = tertiary,
                onTertiary = tertiary.onColor(),
                background = colorFromHsl(surfaceHue, surfaceSaturation, 0.07f),
                onBackground = colorFromHsl(surfaceHue, surfaceSaturation, 0.91f),
                surface = colorFromHsl(surfaceHue, surfaceSaturation, 0.08f),
                onSurface = colorFromHsl(surfaceHue, surfaceSaturation, 0.91f),
                surfaceVariant = colorFromHsl(surfaceHue, variantSaturation, 0.27f),
                onSurfaceVariant = colorFromHsl(surfaceHue, variantSaturation, 0.82f),
                outline = colorFromHsl(surfaceHue, variantSaturation, 0.62f),
                outlineVariant = colorFromHsl(surfaceHue, variantSaturation, 0.33f),
                surfaceContainerLowest = colorFromHsl(surfaceHue, surfaceSaturation, 0.04f),
                surfaceContainerLow = colorFromHsl(surfaceHue, surfaceSaturation, 0.10f),
                surfaceContainer = colorFromHsl(surfaceHue, surfaceSaturation, 0.12f),
                surfaceContainerHigh = colorFromHsl(surfaceHue, surfaceSaturation, 0.16f),
                surfaceContainerHighest = colorFromHsl(surfaceHue, surfaceSaturation, 0.20f),
            )
        } else {
            lightColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = seedArgb.tone(90f, minSaturation = 0.24f),
                onPrimaryContainer = seedArgb.tone(12f, minSaturation = 0.35f),
                secondary = secondary,
                onSecondary = Color.White,
                secondaryContainer = secondary.toArgb().tone(90f, minSaturation = 0.16f),
                onSecondaryContainer = secondary.toArgb().tone(14f, minSaturation = 0.22f),
                tertiary = tertiary,
                onTertiary = Color.White,
                background = colorFromHsl(surfaceHue, surfaceSaturation, 0.985f),
                onBackground = colorFromHsl(surfaceHue, surfaceSaturation, 0.11f),
                surface = colorFromHsl(surfaceHue, surfaceSaturation, 0.985f),
                onSurface = colorFromHsl(surfaceHue, surfaceSaturation, 0.11f),
                surfaceVariant = colorFromHsl(surfaceHue, variantSaturation, 0.90f),
                onSurfaceVariant = colorFromHsl(surfaceHue, variantSaturation, 0.32f),
                outline = colorFromHsl(surfaceHue, variantSaturation, 0.50f),
                outlineVariant = colorFromHsl(surfaceHue, variantSaturation, 0.80f),
                surfaceContainerLowest = Color.White,
                surfaceContainerLow = colorFromHsl(surfaceHue, surfaceSaturation, 0.955f),
                surfaceContainer = colorFromHsl(surfaceHue, surfaceSaturation, 0.935f),
                surfaceContainerHigh = colorFromHsl(surfaceHue, surfaceSaturation, 0.915f),
                surfaceContainerHighest = colorFromHsl(surfaceHue, surfaceSaturation, 0.895f),
            )
        }
    ).maybeAmoled(amoled && isDark)
}

private fun ColorScheme.maybeAmoled(amoled: Boolean): ColorScheme =
    if (amoled) {
        copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = Color(0xFF080808),
            surfaceContainer = Color(0xFF0D0D0F),
            surfaceContainerHigh = Color(0xFF151517),
            surfaceContainerHighest = Color(0xFF1C1C1E),
        )
    } else {
        this
    }

private fun Int.shiftHue(degrees: Float): Int {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this, hsv)
    hsv[0] = (hsv[0] + degrees).mod(360f)
    return android.graphics.Color.HSVToColor(android.graphics.Color.alpha(this), hsv)
}

private fun Int.tone(
    lightness: Float,
    minSaturation: Float = 0.0f,
): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this, hsv)
    val saturation = maxOf(hsv[1], minSaturation).coerceIn(0f, 1f)
    return colorFromHsl(hsv[0], saturation, (lightness / 100f).coerceIn(0f, 1f))
}

private fun Int.hue(): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this, hsv)
    return hsv[0]
}

private fun colorFromHsl(
    hue: Float,
    saturation: Float,
    lightness: Float,
): Color {
    val chroma = (1f - kotlin.math.abs(2f * lightness - 1f)) * saturation
    val h = hue / 60f
    val x = chroma * (1f - kotlin.math.abs(h.mod(2f) - 1f))
    val (r1, g1, b1) =
        when {
            h < 1f -> Triple(chroma, x, 0f)
            h < 2f -> Triple(x, chroma, 0f)
            h < 3f -> Triple(0f, chroma, x)
            h < 4f -> Triple(0f, x, chroma)
            h < 5f -> Triple(x, 0f, chroma)
            else -> Triple(chroma, 0f, x)
        }
    val m = lightness - chroma / 2f
    return Color(
        red = (r1 + m).coerceIn(0f, 1f),
        green = (g1 + m).coerceIn(0f, 1f),
        blue = (b1 + m).coerceIn(0f, 1f),
    )
}

private fun Color.onColor(): Color = if (luminance() > 0.5f) Color.Black else Color.White
