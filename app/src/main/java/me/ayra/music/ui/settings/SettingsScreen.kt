package me.ayra.music.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ayra.music.BuildConfig

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
                    if (forward) {
                        AnimatedContentTransitionScope.SlideDirection.Left
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Right
                    }
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
                    SettingsPage {
                        SettingsGroup {
                            SettingsValueRow("Dynamic color", "Uses your Material You system palette", "System")
                            SettingsDivider()
                            SettingsValueRow("Background", "Use Material You background colors", "Enabled")
                        }
                    }
                }

                SettingsCategory.Player -> {
                    SettingsPage {
                        SettingsGroup {
                            SettingsValueRow("Playback core", "Media3 session and notification playback", "Media3")
                            SettingsDivider()
                            SettingsValueRow("Queue restore", "Restore last active playlist on launch", "Enabled")
                        }
                    }
                }

                SettingsCategory.Library -> {
                    SettingsPage {
                        SettingsGroup {
                            SettingsValueRow("Scanner", "Local device audio from MediaStore", "Enabled")
                            SettingsDivider()
                            SettingsValueRow("Cache", "Room cache with background refresh", "Enabled")
                        }
                    }
                }

                SettingsCategory.Vgmstream -> {
                    SettingsPage {
                        SettingsGroup {
                            SettingsValueRow(
                                "File scan",
                                "Adds vgmstream formats from storage",
                                if (BuildConfig.IS_VGM_BUILD) "Enabled" else "Unavailable",
                            )
                            SettingsDivider()
                            SettingsValueRow("Playback", "Uses vgmstream-media3 adapter for game audio", "Hybrid")
                            SettingsDivider()
                            SettingsValueRow("Loop mode", "Normal loop behavior for supported formats", "Default")
                        }
                    }
                }

                SettingsCategory.About -> {
                    SettingsPage {
                        SettingsGroup {
                            SettingsValueRow("App", "Material You local music player", "Music")
                            SettingsDivider()
                            SettingsValueRow("Version", "Current installed build", "1.0")
                        }
                    }
                }
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
                    subtitle = "Dynamic color and background behavior",
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
                    subtitle = "Playback, queue, and notification behavior",
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
                        subtitle = "Game audio scan and playback settings",
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
private fun SettingsGroup(content: @Composable () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        content()
    }
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
        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
        ) {
            Text(
                title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsValueRow(
    title: String,
    subtitle: String,
    value: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
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
private fun SettingsDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 18.dp),
    )
}
