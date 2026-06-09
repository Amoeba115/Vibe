package me.ayra.music.ui.settings

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ayra.music.BuildConfig
import me.ayra.music.ui.theme.LocalThemeState
import me.ayra.music.ui.theme.THEME_SEED_NEUTRAL
import me.ayra.music.ui.theme.THEME_SEED_SYSTEM
import me.ayra.music.ui.theme.ThemeMode
import me.ayra.music.ui.theme.colorSchemeFromSeed
import me.ayra.music.ui.theme.musicColorScheme
import me.ayra.music.ui.theme.neutralColorScheme
import me.ayra.music.ui.theme.presetPalettes
import me.ayra.music.util.MusicPreferences
import kotlin.math.roundToInt

private enum class SettingsCategory(
    val title: String,
    val depth: Int = 1,
) {
    LookAndFeel("Look and feel"),
    Player("Player"),
    Library("Library"),
    Vgmstream("vgmstream"),
    About("About"),
}

private data class OptionItem(
    val value: String,
    val label: String,
)

private val loopModeOptions =
    listOf(
        OptionItem(MusicPreferences.VGM_LOOP_FOLLOW_APP, "Follow app"),
        OptionItem("Normal", "Normal"),
        OptionItem("Forever", "Loop forever"),
        OptionItem("IgnoreLoop", "Ignore loop points"),
    )

private val channelOutputOptions =
    listOf(
        OptionItem("Auto", "Auto"),
        OptionItem("AllChannels", "All channels"),
        OptionItem("Channel1", "Channel 1"),
        OptionItem("Channel2", "Channel 2"),
        OptionItem("Channel3", "Channel 3"),
        OptionItem("Channel4", "Channel 4"),
        OptionItem("Stereo12", "Stereo 1/2"),
        OptionItem("Stereo34", "Stereo 3/4"),
    )

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCategory by rememberSaveable { mutableStateOf<SettingsCategory?>(null) }
    val title = selectedCategory?.title ?: "Settings"

    BackHandler {
        if (selectedCategory == null) onBack() else selectedCategory = null
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        AnimatedContent(
            targetState = selectedCategory,
            transitionSpec = {
                val forward = (targetState?.depth ?: 0) > (initialState?.depth ?: 0)
                val direction =
                    if (forward) AnimatedContentTransitionScope.SlideDirection.Left else AnimatedContentTransitionScope.SlideDirection.Right
                (
                    slideIntoContainer(direction, animationSpec = tween(220)) + fadeIn(animationSpec = tween(180))
                ).togetherWith(
                    slideOutOfContainer(direction, animationSpec = tween(220)) + fadeOut(animationSpec = tween(160)),
                ).using(SizeTransform(clip = false))
            },
            label = "settings-category",
        ) { category ->
            when (category) {
                null -> SettingsCategoryList(onCategorySelected = { selectedCategory = it })
                SettingsCategory.LookAndFeel -> LookAndFeelSettings()
                SettingsCategory.Player -> PlayerSettings()
                SettingsCategory.Library -> LibrarySettings()
                SettingsCategory.Vgmstream -> VgmstreamSettings()
                SettingsCategory.About -> AboutSettings()
            }
        }

        SettingsHeader(
            title = title,
            onBack = {
                if (selectedCategory == null) onBack() else selectedCategory = null
            },
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun SettingsCategoryList(onCategorySelected: (SettingsCategory) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 92.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            SettingsSectionTitle("Appearance")
            SettingsGroup {
                SettingsNavigationRow(
                    title = "Look and feel",
                    subtitle = "Theme, color palette, and preview",
                    icon = Icons.Rounded.Palette,
                    onClick = { onCategorySelected(SettingsCategory.LookAndFeel) },
                )
            }
        }
        item {
            SettingsSectionTitle("Music")
            SettingsGroup {
                SettingsNavigationRow(
                    title = "Player",
                    subtitle = "Speed and crossfade",
                    icon = Icons.Rounded.PlayCircle,
                    onClick = { onCategorySelected(SettingsCategory.Player) },
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = "Library",
                    subtitle = "Scanner and cached local library",
                    icon = Icons.Rounded.LibraryMusic,
                    onClick = { onCategorySelected(SettingsCategory.Library) },
                )
                if (BuildConfig.IS_VGM_BUILD) {
                    SettingsDivider()
                    SettingsNavigationRow(
                        title = "vgmstream",
                        subtitle = "Loop, fade, subsong, and channel output",
                        icon = Icons.Rounded.Settings,
                        onClick = { onCategorySelected(SettingsCategory.Vgmstream) },
                    )
                }
            }
        }
        item {
            SettingsSectionTitle("Info")
            SettingsGroup {
                SettingsNavigationRow(
                    title = "About",
                    subtitle = "Version and project information",
                    icon = Icons.Rounded.Info,
                    onClick = { onCategorySelected(SettingsCategory.About) },
                )
            }
        }
    }
}

@Composable
private fun LookAndFeelSettings() {
    val context = LocalContext.current
    val themeState = LocalThemeState.current
    val themeMode by themeState.themeMode.collectAsState()
    val themeColorSeed by themeState.themeColorSeed.collectAsState()
    val amoledMode by themeState.amoledMode.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDark =
        when (themeMode) {
            ThemeMode.Auto -> systemDark
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
        }
    val amoled = amoledMode && isDark
    val wallpaperPalettes =
        remember(context, isDark) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val dynamicScheme = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                listOf(dynamicScheme.primary, dynamicScheme.secondary, dynamicScheme.tertiary)
                    .mapIndexed { index, color ->
                        me.ayra.music.ui.theme.ColorPaletteOption(
                            name = listOf("Primary", "Secondary", "Tertiary")[index],
                            seedArgb = color.toArgb(),
                        )
                    }.distinctBy { it.hexKey }
            } else {
                emptyList()
            }
        }
    val previewScheme =
        remember(context, themeMode, themeColorSeed, amoledMode, systemDark) {
            musicColorScheme(context, themeMode, themeColorSeed, amoledMode, systemDark)
        }
    var selectedTab by rememberSaveable(themeColorSeed) {
        mutableIntStateOf(if (presetPalettes.any { it.hexKey == themeColorSeed }) 1 else 0)
    }
    val followSystem = themeMode == ThemeMode.Auto
    val darkMode = themeMode == ThemeMode.Dark || (followSystem && systemDark)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(top = 92.dp)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(14.dp))
        MusicPhonePreview(
            colorScheme = previewScheme,
            modifier =
                Modifier
                    .fillMaxWidth(0.55f)
                    .aspectRatio(0.52f),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Preview updates with your selected palette.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
        Spacer(Modifier.height(16.dp))
        if (selectedTab == 0) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                item("system") {
                    SystemColorOption(
                        isSelected = themeColorSeed == THEME_SEED_SYSTEM,
                        onClick = { themeState.setThemeColorSeed(THEME_SEED_SYSTEM) },
                    )
                }
                items(wallpaperPalettes, key = { it.hexKey }) { palette ->
                    ColorCircleItem(
                        scheme = colorSchemeFromSeed(palette.seedArgb, isDark, amoled),
                        isSelected = themeColorSeed == palette.hexKey,
                        onClick = { themeState.setThemeColorSeed(palette.hexKey) },
                    )
                }
                item("neutral") {
                    ColorCircleItem(
                        scheme = neutralColorScheme(isDark, amoled),
                        isSelected = themeColorSeed == THEME_SEED_NEUTRAL,
                        onClick = { themeState.setThemeColorSeed(THEME_SEED_NEUTRAL) },
                    )
                }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(presetPalettes, key = { it.hexKey }) { palette ->
                    ColorCircleItem(
                        scheme = colorSchemeFromSeed(palette.seedArgb, isDark, amoled),
                        isSelected = themeColorSeed == palette.hexKey,
                        onClick = { themeState.setThemeColorSeed(palette.hexKey) },
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PillTab(selected = selectedTab == 0, text = "Material You", onClick = { selectedTab = 0 })
                PillTab(selected = selectedTab == 1, text = "Colors", onClick = { selectedTab = 1 })
            }
        }
        Spacer(Modifier.height(24.dp))
        SettingsGroup(modifier = Modifier.padding(horizontal = 20.dp)) {
            SettingsSwitchRow(
                title = "Follow system theme",
                subtitle = "Use the Android light or dark setting",
                checked = followSystem,
                onCheckedChange = { checked ->
                    themeState.setThemeMode(
                        if (checked) ThemeMode.Auto else if (systemDark) ThemeMode.Dark else ThemeMode.Light,
                    )
                },
            )
            SettingsDivider()
            SettingsSwitchRow(
                title = "Use dark theme",
                subtitle = "Force dark colors when system following is off",
                checked = darkMode,
                enabled = !followSystem,
                onCheckedChange = { checked -> themeState.setThemeMode(if (checked) ThemeMode.Dark else ThemeMode.Light) },
            )
            SettingsDivider()
            SettingsSwitchRow(
                title = "Use AMOLED mode",
                subtitle = "Use black background and surfaces in dark theme",
                checked = amoledMode,
                onCheckedChange = themeState::setAmoledMode,
            )
        }
        Spacer(Modifier.height(116.dp))
    }
}

@Composable
private fun PlayerSettings() {
    val preferences = rememberPreferences()
    var speed by rememberSaveable { mutableFloatStateOf(preferences.loadPlaybackSpeed()) }
    var crossfadeSeconds by rememberSaveable { mutableIntStateOf(preferences.loadCrossfadeSeconds()) }

    SettingsPage {
        SettingsGroup {
            SliderSettingRow(
                title = "Play speed",
                subtitle = "Playback speed for Media3 playback",
                valueLabel = "%.1fx".format(speed),
                value = speed,
                valueRange = 0.5f..2.0f,
                steps = 14,
                onValueChange = {
                    speed = (it * 10f).roundToInt() / 10f
                    preferences.savePlaybackSpeed(speed)
                },
            )
            SettingsDivider()
            SliderSettingRow(
                title = "Crossfade each track",
                subtitle = "Saved for playback transition behavior",
                valueLabel = if (crossfadeSeconds == 0) "Disabled" else "${crossfadeSeconds}s",
                value = crossfadeSeconds.toFloat(),
                valueRange = 0f..5f,
                steps = 4,
                onValueChange = {
                    crossfadeSeconds = it.roundToInt().coerceIn(0, 5)
                    preferences.saveCrossfadeSeconds(crossfadeSeconds)
                },
            )
        }
    }
}

@Composable
private fun VgmstreamSettings() {
    val preferences = rememberPreferences()
    var loopMode by rememberSaveable { mutableStateOf(preferences.loadVgmLoopMode()) }
    var loopCountText by rememberSaveable { mutableStateOf(preferences.loadVgmLoopCount().toString()) }
    var fadeLength by rememberSaveable { mutableIntStateOf(preferences.loadVgmFadeLengthSeconds()) }
    var fadeDelay by rememberSaveable { mutableIntStateOf(preferences.loadVgmFadeDelaySeconds()) }
    var disableSubsongs by rememberSaveable { mutableStateOf(preferences.loadVgmDisableSubsongs()) }
    var downmix by rememberSaveable { mutableStateOf(preferences.loadVgmDownmixEnabled()) }
    var downmixChannels by rememberSaveable { mutableIntStateOf(preferences.loadVgmDownmixChannels()) }
    var channelOutput by rememberSaveable { mutableStateOf(preferences.loadVgmChannelOutput()) }
    var loopDialog by rememberSaveable { mutableStateOf(false) }
    var channelDialog by rememberSaveable { mutableStateOf(false) }

    if (loopDialog) {
        SingleChoiceDialog(
            title = "Loop mode",
            options = loopModeOptions,
            selectedValue = loopMode,
            onDismiss = { loopDialog = false },
            onSelect = {
                loopMode = it
                preferences.saveVgmLoopMode(it)
                loopDialog = false
            },
        )
    }
    if (channelDialog) {
        SingleChoiceDialog(
            title = "Channel output",
            options = channelOutputOptions,
            selectedValue = channelOutput,
            onDismiss = { channelDialog = false },
            onSelect = {
                channelOutput = it
                preferences.saveVgmChannelOutput(it)
                channelDialog = false
            },
        )
    }

    SettingsPage {
        SettingsGroup {
            SettingsValueRow(
                title = "Loop mode",
                subtitle = "Loop behavior for vgmstream formats",
                value = loopModeOptions.firstOrNull { it.value == loopMode }?.label ?: "Normal",
                onClick = { loopDialog = true },
            )
            SettingsDivider()
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                Text("Loop count", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                Text("Default is 2.0, maximum is 99.0", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = loopCountText,
                    onValueChange = { value ->
                        loopCountText = value
                        value.toFloatOrNull()?.let { preferences.saveVgmLoopCount(it.coerceIn(0f, 99f)) }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            SettingsDivider()
            SliderSettingRow(
                title = "Fade length",
                subtitle = "Fade-out duration after loop limit",
                valueLabel = if (fadeLength == 0) "Disabled" else "${fadeLength}s",
                value = fadeLength.toFloat(),
                valueRange = 0f..30f,
                steps = 29,
                onValueChange = {
                    fadeLength = it.roundToInt().coerceIn(0, 30)
                    preferences.saveVgmFadeLengthSeconds(fadeLength)
                },
            )
            SettingsDivider()
            SliderSettingRow(
                title = "Fade delay",
                subtitle = "Delay before fade-out starts",
                valueLabel = if (fadeDelay == 0) "Disabled" else "${fadeDelay}s",
                value = fadeDelay.toFloat(),
                valueRange = 0f..30f,
                steps = 29,
                onValueChange = {
                    fadeDelay = it.roundToInt().coerceIn(0, 30)
                    preferences.saveVgmFadeDelaySeconds(fadeDelay)
                },
            )
            SettingsDivider()
            SettingsSwitchRow(
                title = "Disable subsongs",
                subtitle = "Ignore detected subsong entries",
                checked = disableSubsongs,
                onCheckedChange = {
                    disableSubsongs = it
                    preferences.saveVgmDisableSubsongs(it)
                },
            )
            SettingsDivider()
            SettingsSwitchRow(
                title = "Downmix",
                subtitle = "Mix output to a fixed channel count",
                checked = downmix,
                onCheckedChange = {
                    downmix = it
                    preferences.saveVgmDownmixEnabled(it)
                },
            )
            if (downmix) {
                SettingsDivider()
                SliderSettingRow(
                    title = "Downmix channel",
                    subtitle = "Output channel count",
                    valueLabel = downmixChannels.toString(),
                    value = downmixChannels.toFloat(),
                    valueRange = 1f..6f,
                    steps = 4,
                    onValueChange = {
                        downmixChannels = it.roundToInt().coerceIn(1, 6)
                        preferences.saveVgmDownmixChannels(downmixChannels)
                    },
                )
            }
            SettingsDivider()
            SettingsValueRow(
                title = "Channel output",
                subtitle = "Select a specific source channel layout",
                value = channelOutputOptions.firstOrNull { it.value == channelOutput }?.label ?: "Auto",
                onClick = { channelDialog = true },
            )
        }
    }
}

@Composable
private fun LibrarySettings() {
    SettingsPage {
        SettingsGroup {
            SettingsValueRow("Scanner", "Local device audio from MediaStore and enabled file scanners", "Enabled")
            SettingsDivider()
            SettingsValueRow("Cache", "Room cache with background refresh", "Enabled")
        }
    }
}

@Composable
private fun AboutSettings() {
    SettingsPage {
        SettingsGroup {
            SettingsValueRow("App", "Material You local music player", "Music")
            SettingsDivider()
            SettingsValueRow("Version", "Current installed build", BuildConfig.VERSION_NAME)
        }
    }
}

@Composable
private fun SettingsPage(content: @Composable () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 92.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { content() }
    }
}

@Composable
private fun SettingsHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(start = 8.dp, top = 18.dp, end = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        content = content,
    )
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(modifier = Modifier.size(42.dp), shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onCheckedChange(!checked) }
                .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.48f),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.48f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun SettingsValueRow(
    title: String,
    subtitle: String,
    value: String,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            value,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(start = 14.dp),
        )
    }
}

@Composable
private fun SliderSettingRow(
    title: String,
    subtitle: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            Text(valueLabel, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        }
        Spacer(Modifier.height(10.dp))
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange, steps = steps)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 18.dp),
    )
}

@Composable
private fun SingleChoiceDialog(
    title: String,
    options: List<OptionItem>,
    selectedValue: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelect(option.value) }
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            option.label,
                            color = if (option.value == selectedValue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (option.value == selectedValue) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun SystemColorOption(
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .then(if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Outlined.Check else Icons.Outlined.Palette,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun ColorCircleItem(
    scheme: ColorScheme,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(if (isSelected) 3.dp else 0.dp, if (isSelected) scheme.primary else Color.Transparent, CircleShape)
                .padding(if (isSelected) 3.dp else 0.dp)
                .clickable(onClick = onClick),
    ) {
        Column(Modifier.matchParentSize()) {
            Row(Modifier.weight(1f)) {
                Box(Modifier.weight(1f).fillMaxHeight().background(scheme.primary))
                Box(Modifier.weight(1f).fillMaxHeight().background(scheme.secondary))
            }
            Row(Modifier.weight(1f)) {
                Box(Modifier.weight(1f).fillMaxHeight().background(scheme.tertiary))
                Box(Modifier.weight(1f).fillMaxHeight().background(scheme.primaryContainer))
            }
        }
        if (isSelected) {
            Box(
                modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun MusicPhonePreview(
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier,
) {
    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Music", modifier = Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Surface(modifier = Modifier.size(26.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    listOf("Favorite", "Playlist", "Track").forEachIndexed { index, label ->
                        Text(
                            label,
                            fontSize = if (index == 0) 10.sp else 8.sp,
                            fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(4) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer))
                            Column(Modifier.padding(start = 8.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.fillMaxWidth(0.7f).height(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                                Box(Modifier.fillMaxWidth(0.45f).height(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant))
                            }
                        }
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(12.dp).height(42.dp),
                    shape = RoundedCornerShape(21.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("No track selected", modifier = Modifier.padding(start = 8.dp), color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PillTab(
    selected: Boolean,
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clip(CircleShape).clickable(onClick = onClick),
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun rememberPreferences(): MusicPreferences {
    val context = LocalContext.current
    return remember(context) { MusicPreferences(context) }
}
