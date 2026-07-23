package me.ayra.music.ui.settings

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
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
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ayra.music.FolderGroup
import me.ayra.music.LibraryState
import me.ayra.music.R
import me.ayra.music.ui.player.AlbumArt
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

private const val ABOUT_TELEGRAM_URL = "https://t.me/AyraHikari"
private const val ABOUT_GITHUB_URL = "https://github.com/AyraHikari"
private const val ABOUT_AMOEBA_GITHUB_URL = "https://github.com/Amoeba115"

private enum class SettingsCategory(
    @StringRes val titleRes: Int,
    val depth: Int = 1,
) {
    LookAndFeel(R.string.look_and_feel),
    Player(R.string.player),
    Library(R.string.library),
    Playlist(R.string.playlist),
    HideFolders(R.string.hide_folder, depth = 2),
}

private data class OptionItem(
    val value: String,
    @StringRes val labelRes: Int,
)

private val miniPlayerStyleOptions =
    listOf(
        OptionItem(MusicPreferences.MINI_PLAYER_STYLE_FLOATING, R.string.mini_player_style_floating),
        // OptionItem(MusicPreferences.MINI_PLAYER_STYLE_FILLED, R.string.mini_player_style_filled), // TODO: rework the style
    )

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    library: LibraryState,
    onRescan: () -> Unit,
    onHiddenFoldersChanged: (Set<String>) -> Unit,
    onImportPlaylist: () -> Unit,
    onExportPlaylists: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCategory by rememberSaveable { mutableStateOf<SettingsCategory?>(null) }
    val title = selectedCategory?.titleRes?.let { stringResource(it) } ?: stringResource(R.string.settings)

    fun backInSettings() {
        selectedCategory =
            when (selectedCategory) {
                null -> {
                    onBack()
                    null
                }

                SettingsCategory.HideFolders -> {
                    SettingsCategory.Library
                }

                else -> {
                    null
                }
            }
    }

    BackHandler {
        backInSettings()
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
                null -> {
                    SettingsCategoryList(onCategorySelected = { selectedCategory = it })
                }

                SettingsCategory.LookAndFeel -> {
                    LookAndFeelSettings()
                }

                SettingsCategory.Player -> {
                    PlayerSettings()
                }

                SettingsCategory.Library -> {
                    LibrarySettings(
                        library = library,
                        onScan = onRescan,
                        onHideFolders = { selectedCategory = SettingsCategory.HideFolders },
                    )
                }

                SettingsCategory.Playlist -> {
                    PlaylistSettings(
                        importing = library.importingPlaylist,
                        importMessage = library.playlistImportMessage,
                        onImport = onImportPlaylist,
                        onExport = onExportPlaylists,
                    )
                }

                SettingsCategory.HideFolders -> {
                    HideFolderSettings(
                        folders = library.allFolders,
                        hiddenFolders = library.hiddenFolders,
                        onHiddenFoldersChanged = onHiddenFoldersChanged,
                    )
                }

            }
        }

        SettingsHeader(
            title = title,
            onBack = { backInSettings() },
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
            AboutCard()
        }
        item {
            SettingsSectionTitle(stringResource(R.string.appearance))
            SettingsGroup {
                SettingsNavigationRow(
                    title = stringResource(R.string.look_and_feel),
                    subtitle = stringResource(R.string.look_and_feel_subtitle),
                    icon = Icons.Rounded.Palette,
                    onClick = { onCategorySelected(SettingsCategory.LookAndFeel) },
                )
            }
        }
        item {
            SettingsSectionTitle(stringResource(R.string.music))
            SettingsGroup {
                SettingsNavigationRow(
                    title = stringResource(R.string.player),
                    subtitle = stringResource(R.string.player_subtitle),
                    icon = Icons.Rounded.PlayCircle,
                    onClick = { onCategorySelected(SettingsCategory.Player) },
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = stringResource(R.string.library),
                    subtitle = stringResource(R.string.library_subtitle),
                    icon = Icons.Rounded.LibraryMusic,
                    onClick = { onCategorySelected(SettingsCategory.Library) },
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = stringResource(R.string.playlist),
                    subtitle = stringResource(R.string.playlist_settings_subtitle),
                    icon = Icons.Rounded.MusicNote,
                    onClick = { onCategorySelected(SettingsCategory.Playlist) },
                )
            }
        }
    }
}

@Composable
private fun AboutCard() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val appName = stringResource(R.string.app_name)
    val versionName =
        remember(context) {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager
                        .getPackageInfo(
                            context.packageName,
                            android.content.pm.PackageManager.PackageInfoFlags
                                .of(0),
                        ).versionName
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName
                }
            }.getOrNull().orEmpty()
        }
    val colorScheme = MaterialTheme.colorScheme
    val cardBrush =
        Brush.horizontalGradient(
            listOf(
                colorScheme.surfaceContainerHighest,
                colorScheme.primaryContainer.copy(alpha = 0.82f),
            ),
        )
    val onCard = colorScheme.onPrimaryContainer

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(cardBrush)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                Icons.Rounded.Info,
                contentDescription = null,
                tint = onCard,
                modifier = Modifier.size(20.dp),
            )
            if (versionName.isNotBlank()) {
                Text(
                    text = "v$versionName",
                    color = colorScheme.onTertiaryContainer,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(colorScheme.tertiaryContainer.copy(alpha = 0.78f))
                            .padding(horizontal = 22.dp, vertical = 10.dp),
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = appName,
                color = onCard,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = stringResource(R.string.about_developer),
                color = onCard.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AboutLinkButton(
                label = stringResource(R.string.telegram),
                icon = { Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null) },
                onClick = { uriHandler.openUri(ABOUT_TELEGRAM_URL) },
                modifier = Modifier.weight(1f),
            )
            AboutLinkButton(
                label = stringResource(R.string.github),
                icon = { Icon(painterResource(R.drawable.ic_github), contentDescription = null) },
                onClick = { uriHandler.openUri(ABOUT_GITHUB_URL) },
                modifier = Modifier.weight(1f),
            )
        }
        AboutLinkButton(
            label = stringResource(R.string.github_amoeba115),
            icon = { Icon(painterResource(R.drawable.ic_github), contentDescription = null) },
            onClick = { uriHandler.openUri(ABOUT_AMOEBA_GITHUB_URL) },
            modifier = Modifier.fillMaxWidth(),
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.about_changes_title),
                color = onCard,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = stringResource(R.string.about_change_vgm_removed),
                color = onCard.copy(alpha = 0.84f),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(R.string.about_change_large_library),
                color = onCard.copy(alpha = 0.84f),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(R.string.about_change_playlist_import),
                color = onCard.copy(alpha = 0.84f),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(R.string.about_change_track_info_metadata),
                color = onCard.copy(alpha = 0.84f),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun AboutLinkButton(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.76f))
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onTertiaryContainer) {
            icon()
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun LookAndFeelSettings() {
    val context = LocalContext.current
    val preferences = rememberPreferences()
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
    var disableAlbumDynamicColor by rememberSaveable { mutableStateOf(preferences.loadDisableAlbumDynamicColor()) }
    var miniPlayerStyle by rememberSaveable { mutableStateOf(preferences.loadMiniPlayerStyle()) }
    var miniPlayerStyleDialog by rememberSaveable { mutableStateOf(false) }
    var fancyBackground by rememberSaveable { mutableStateOf(preferences.loadFancyBackgroundEnabled()) }
    var fancyPlayer by rememberSaveable { mutableStateOf(preferences.loadFancyPlayerEnabled()) }

    if (miniPlayerStyleDialog) {
        SingleChoiceDialog(
            title = stringResource(R.string.mini_player_style),
            options = miniPlayerStyleOptions,
            selectedValue = miniPlayerStyle,
            onDismiss = { miniPlayerStyleDialog = false },
            onSelect = {
                miniPlayerStyle = it
                preferences.saveMiniPlayerStyle(it)
                miniPlayerStyleDialog = false
            },
        )
    }

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
            miniPlayerStyle = miniPlayerStyle,
            modifier =
                Modifier
                    .fillMaxWidth(0.55f)
                    .aspectRatio(0.52f),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.preview_palette_description),
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
                PillTab(selected = selectedTab == 0, text = stringResource(R.string.material_you), onClick = { selectedTab = 0 })
                PillTab(selected = selectedTab == 1, text = stringResource(R.string.colors), onClick = { selectedTab = 1 })
            }
        }
        Spacer(Modifier.height(24.dp))
        SettingsSectionTitle(
            title = stringResource(R.string.appearance),
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        SettingsGroup(modifier = Modifier.padding(horizontal = 20.dp)) {
            SettingsSwitchRow(
                title = stringResource(R.string.follow_system_theme),
                subtitle = stringResource(R.string.follow_system_theme_subtitle),
                checked = followSystem,
                onCheckedChange = { checked ->
                    themeState.setThemeMode(
                        if (checked) {
                            ThemeMode.Auto
                        } else if (systemDark) {
                            ThemeMode.Dark
                        } else {
                            ThemeMode.Light
                        },
                    )
                },
            )
            SettingsDivider()
            SettingsSwitchRow(
                title = stringResource(R.string.use_dark_theme),
                subtitle = stringResource(R.string.use_dark_theme_subtitle),
                checked = darkMode,
                enabled = !followSystem,
                onCheckedChange = { checked -> themeState.setThemeMode(if (checked) ThemeMode.Dark else ThemeMode.Light) },
            )
            SettingsDivider()
            SettingsSwitchRow(
                title = stringResource(R.string.use_amoled_mode),
                subtitle = stringResource(R.string.use_amoled_mode_subtitle),
                checked = amoledMode,
                onCheckedChange = themeState::setAmoledMode,
            )
            SettingsDivider()
            SettingsSwitchRow(
                title = stringResource(R.string.disable_album_dynamic_color),
                subtitle = stringResource(R.string.disable_album_dynamic_color_subtitle),
                checked = disableAlbumDynamicColor,
                onCheckedChange = {
                    disableAlbumDynamicColor = it
                    preferences.saveDisableAlbumDynamicColor(it)
                },
            )
        }
        Spacer(Modifier.height(18.dp))
        SettingsSectionTitle(
            title = stringResource(R.string.style),
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        SettingsGroup(modifier = Modifier.padding(horizontal = 20.dp)) {
            SettingsSwitchRow(
                title = stringResource(R.string.fancy_player),
                subtitle = stringResource(R.string.fancy_player_subtitle),
                checked = fancyPlayer,
                onCheckedChange = {
                    fancyPlayer = it
                    preferences.saveFancyPlayerEnabled(it)
                },
            )
            SettingsDivider()
            SettingsSwitchRow(
                title = stringResource(R.string.fancy_background),
                subtitle = stringResource(R.string.fancy_background_subtitle),
                checked = fancyBackground,
                onCheckedChange = {
                    fancyBackground = it
                    preferences.saveFancyBackgroundEnabled(it)
                },
            )
            SettingsDivider()
            SettingsValueRow(
                title = stringResource(R.string.mini_player_style),
                subtitle = stringResource(R.string.mini_player_style_subtitle),
                value =
                    miniPlayerStyleOptions.firstOrNull { it.value == miniPlayerStyle }?.let { stringResource(it.labelRes) }
                        ?: stringResource(R.string.mini_player_style_floating),
                onClick = { miniPlayerStyleDialog = true },
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
    var stopOnTaskRemoved by rememberSaveable { mutableStateOf(preferences.loadStopOnTaskRemoved()) }
    var pauseWhenVolumeZero by rememberSaveable { mutableStateOf(preferences.loadPauseWhenVolumeZero()) }

    SettingsPage {
        SettingsGroup {
            SettingsSwitchRow(
                title = stringResource(R.string.stop_on_recent_close),
                subtitle = stringResource(R.string.stop_on_recent_close_subtitle),
                checked = stopOnTaskRemoved,
                onCheckedChange = {
                    stopOnTaskRemoved = it
                    preferences.saveStopOnTaskRemoved(it)
                },
            )
            SettingsDivider()
            SettingsSwitchRow(
                title = stringResource(R.string.pause_when_volume_zero),
                subtitle = stringResource(R.string.pause_when_volume_zero_subtitle),
                checked = pauseWhenVolumeZero,
                onCheckedChange = {
                    pauseWhenVolumeZero = it
                    preferences.savePauseWhenVolumeZero(it)
                },
            )
            SettingsDivider()
            SliderSettingRow(
                title = stringResource(R.string.play_speed),
                subtitle = stringResource(R.string.play_speed_subtitle),
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
                title = stringResource(R.string.crossfade_each_track),
                subtitle = stringResource(R.string.crossfade_each_track_subtitle),
                valueLabel = if (crossfadeSeconds == 0) stringResource(R.string.disabled) else "${crossfadeSeconds}s",
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
private fun PlaylistSettings(
    importing: Boolean,
    importMessage: String?,
    onImport: () -> Unit,
    onExport: () -> Unit,
) {
    SettingsPage {
        SettingsSectionTitle(stringResource(R.string.playlist_layout))
        SettingsGroup {
            SettingsNavigationRow(
                title = stringResource(R.string.import_playlist),
                subtitle = if (importing) stringResource(R.string.import_playlist_in_progress) else stringResource(R.string.import_playlist_subtitle),
                icon = Icons.Rounded.LibraryMusic,
                enabled = !importing,
                onClick = onImport,
            )
            SettingsDivider()
            SettingsNavigationRow(
                title = stringResource(R.string.export_playlist),
                subtitle = stringResource(R.string.export_playlist_subtitle),
                icon = Icons.Rounded.MusicNote,
                enabled = !importing,
                onClick = onExport,
            )
            if (importing || importMessage != null) {
                SettingsDivider()
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (importing) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    Text(
                        text = importMessage ?: stringResource(R.string.import_playlist_in_progress),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun LibrarySettings(
    library: LibraryState,
    onScan: () -> Unit,
    onHideFolders: () -> Unit,
) {
    SettingsPage {
        SettingsGroup {
            ScanSettingsRow(
                scanning = library.scanning,
                trackCount = if (library.scanning) library.scannedTrackCount else library.tracks.size,
                onClick = onScan,
            )
            SettingsDivider()
            SettingsValueRow(
                title = stringResource(R.string.hide_folder),
                subtitle = stringResource(R.string.select_folder),
                value = stringResource(R.string.hidden_count, library.hiddenFolders.size),
                onClick = onHideFolders,
            )
        }
    }
}

@Composable
private fun HideFolderSettings(
    folders: List<FolderGroup>,
    hiddenFolders: Set<String>,
    onHiddenFoldersChanged: (Set<String>) -> Unit,
) {
    SettingsPage {
        Text(
            text = stringResource(R.string.selected_count, hiddenFolders.size),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(18.dp),
        )
        SettingsGroup {
            if (folders.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_folders_found),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(18.dp),
                )
            } else {
                folders.forEachIndexed { index, folder ->
                    HideFolderRow(
                        folder = folder,
                        selected = folder.path in hiddenFolders,
                        onClick = {
                            val updated =
                                if (folder.path in hiddenFolders) {
                                    hiddenFolders - folder.path
                                } else {
                                    hiddenFolders + folder.path
                                }
                            onHiddenFoldersChanged(updated)
                        },
                    )
                    if (index != folders.lastIndex) {
                        SettingsDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanSettingsRow(
    scanning: Boolean,
    trackCount: Int,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = !scanning, onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.scanner),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (scanning) stringResource(R.string.scanning) else stringResource(R.string.scan_now),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = trackCount.toString(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }
        if (scanning) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun HideFolderRow(
    folder: FolderGroup,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .padding(end = 12.dp)
                    .size(24.dp),
        )
        AlbumArt(
            folder.tracks.firstOrNull()?.albumArtUri,
            Modifier.size(48.dp),
            RoundedCornerShape(12.dp),
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
        ) {
            Text(
                text = folder.name,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = folder.path,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = folder.tracks.size.toString(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
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
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.primary,
            )
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
private fun SettingsSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        modifier = modifier.padding(start = 4.dp, bottom = 8.dp),
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
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(modifier = Modifier.size(42.dp), shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = if (enabled) 1f else 0.48f),
                )
            }
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
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
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.48f),
        )
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
            Text(
                title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
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
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                valueLabel,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
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
                        Icon(
                            if (option.value == selectedValue) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = null,
                            tint =
                                if (option.value == selectedValue) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            stringResource(option.labelRes),
                            color =
                                if (option.value ==
                                    selectedValue
                                ) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            fontWeight = if (option.value == selectedValue) FontWeight.SemiBold else FontWeight.Normal,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .padding(start = 14.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
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
    miniPlayerStyle: String,
    modifier: Modifier = Modifier,
) {
    MaterialTheme(colorScheme = colorScheme) {
        val floatingMiniPlayer = miniPlayerStyle != MusicPreferences.MINI_PLAYER_STYLE_FILLED
        val miniPlayerPadding = if (floatingMiniPlayer) 12.dp else 0.dp
        val miniPlayerShape =
            if (floatingMiniPlayer) {
                RoundedCornerShape(21.dp)
            } else {
                RoundedCornerShape(topStart = 21.dp, topEnd = 21.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
            }
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
                    Text(
                        stringResource(R.string.app_name),
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Surface(modifier = Modifier.size(26.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    listOf(
                        stringResource(R.string.favorite),
                        stringResource(R.string.playlist),
                        stringResource(R.string.track),
                    ).forEachIndexed { index, label ->
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
                                Box(
                                    Modifier
                                        .fillMaxWidth(
                                            0.7f,
                                        ).height(5.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onSurface),
                                )
                                Box(
                                    Modifier
                                        .fillMaxWidth(
                                            0.45f,
                                        ).height(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onSurfaceVariant),
                                )
                            }
                        }
                    }
                }
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(miniPlayerPadding)
                            .height(42.dp),
                    shape = miniPlayerShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            stringResource(R.string.no_track_selected),
                            modifier = Modifier.padding(start = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 11.sp,
                        )
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
