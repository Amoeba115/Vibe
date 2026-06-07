package me.ayra.music.ui.player

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import me.ayra.music.PlayerState
import me.ayra.music.Track

enum class PlayerSheetState {
    Collapsed,
    Expanded,
}

@Composable
fun AlbumArt(artwork: Uri?, modifier: Modifier, shape: RoundedCornerShape) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (artwork != null) {
            AsyncImage(
                model = artwork,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PlayerSheet(
    sheetState: PlayerSheetState,
    playerState: PlayerState,
    isFavorite: Boolean,
    onSheetStateChange: (PlayerSheetState) -> Unit,
    onSettings: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var lyricsVisible by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = sheetState == PlayerSheetState.Expanded) {
        if (lyricsVisible) {
            lyricsVisible = false
        } else {
            onSheetStateChange(PlayerSheetState.Collapsed)
        }
    }

    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = sheetState,
            transitionSpec = {
                fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                    fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) using SizeTransform(clip = false)
            },
            label = "player-sheet",
        ) { targetState ->
            when (targetState) {
                PlayerSheetState.Collapsed -> Box(Modifier.fillMaxSize()) {
                    CollapsedPlayerContent(
                        playerState = playerState,
                        onExpand = { onSheetStateChange(PlayerSheetState.Expanded) },
                        onPlayPause = onPlayPause,
                        onPrevious = onPrevious,
                        onNext = onNext,
                        animatedVisibilityScope = this@AnimatedContent,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }

                PlayerSheetState.Expanded -> Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(horizontal = 24.dp),
                    ) {
                        ExpandedPlayerContent(
                            playerState = playerState,
                            isFavorite = isFavorite,
                            lyricsVisible = lyricsVisible,
                            onMinimize = { onSheetStateChange(PlayerSheetState.Collapsed) },
                            onSettings = onSettings,
                            onToggleFavorite = onToggleFavorite,
                            onLyrics = { lyricsVisible = true },
                            animatedVisibilityScope = this@AnimatedContent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        )
                        SeekBar(
                            positionMs = playerState.positionMs,
                            durationMs = playerState.durationMs,
                            onSeek = onSeek,
                        )
                        Controls(
                            isPlaying = playerState.isPlaying,
                            shuffle = playerState.shuffle,
                            repeatMode = playerState.repeatMode,
                            hasTrack = playerState.currentTrack != null,
                            onShuffle = onShuffle,
                            onPrevious = onPrevious,
                            onPlayPause = onPlayPause,
                            onNext = onNext,
                            onRepeat = onRepeat,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.CollapsedPlayerContent(
    playerState: PlayerState,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val track = playerState.currentTrack
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .height(68.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onExpand),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SharedCoverArt(
                artwork = track?.albumArtUri,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(14.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
            ) {
                Text(track?.title ?: "No track selected", maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(track?.artist ?: "Choose music to play", maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f), fontSize = 12.sp)
            }
            IconButton(onClick = onPrevious, enabled = track != null) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
            }
            IconButton(onClick = onPlayPause, enabled = track != null) {
                Icon(if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play or pause")
            }
            IconButton(onClick = onNext, enabled = track != null) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next")
            }
            IconButton(onClick = { }, enabled = track != null) {
                Icon(Icons.Default.QueueMusic, contentDescription = "Queue")
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.ExpandedPlayerContent(
    playerState: PlayerState,
    isFavorite: Boolean,
    lyricsVisible: Boolean,
    onMinimize: () -> Unit,
    onSettings: () -> Unit,
    onToggleFavorite: () -> Unit,
    onLyrics: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val track = playerState.currentTrack
    AnimatedContent(
        targetState = lyricsVisible,
        transitionSpec = {
            fadeIn(spring(stiffness = Spring.StiffnessLow)) togetherWith fadeOut() using SizeTransform(clip = false)
        },
        label = "lyrics-switch",
        modifier = modifier,
    ) { showLyrics ->
        if (showLyrics) {
            LyricsContent(
                track = track,
                onMinimize = onMinimize,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onMinimize) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", modifier = Modifier.size(34.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onSettings) { Icon(Icons.Default.MoreVert, contentDescription = "Settings") }
                }
                SharedCoverArt(
                    artwork = track?.albumArtUri,
                    animatedVisibilityScope = animatedVisibilityScope,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clickable(onClick = onLyrics),
                    shape = RoundedCornerShape(34.dp),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(track?.title ?: "No track selected", fontSize = 30.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(track?.artist ?: "Choose music to play", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = { }) { Icon(Icons.Default.QueueMusic, contentDescription = "Queue", modifier = Modifier.size(32.dp)) }
                    IconButton(onClick = onToggleFavorite, enabled = track != null) {
                        Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Favorite", modifier = Modifier.size(34.dp))
                    }
                    IconButton(onClick = { }) { Icon(Icons.Default.Add, contentDescription = "Add to", modifier = Modifier.size(36.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.SharedCoverArt(
    artwork: Uri?,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier,
    shape: RoundedCornerShape,
) {
    AlbumArt(
        artwork = artwork,
        modifier = modifier.sharedElement(
            sharedContentState = rememberSharedContentState(key = "player-cover-art"),
            animatedVisibilityScope = animatedVisibilityScope,
        ),
        shape = shape,
    )
}

@Composable
private fun LyricsContent(track: Track?, onMinimize: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onMinimize) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", modifier = Modifier.size(34.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(track?.title ?: "No track selected", fontSize = 28.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(track?.artist ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                Text("x1", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("No lyric found", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun SeekBar(positionMs: Long, durationMs: Long, onSeek: (Long) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = if (durationMs > 0) positionMs.coerceIn(0L, durationMs).toFloat() else 0f,
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..durationMs.coerceAtLeast(1L).toFloat(),
        )
        Row(Modifier.fillMaxWidth()) {
            Text(formatDuration(positionMs), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text(formatDuration(durationMs), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Controls(
    isPlaying: Boolean,
    shuffle: Boolean,
    repeatMode: Int,
    hasTrack: Boolean,
    onShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 22.dp, bottom = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onShuffle, enabled = hasTrack) {
            Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = if (shuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        }
        IconButton(onClick = onPrevious, enabled = hasTrack) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(38.dp))
        }
        IconButton(onClick = onPlayPause, enabled = hasTrack, modifier = Modifier.size(72.dp)) {
            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play or pause", modifier = Modifier.size(54.dp))
        }
        IconButton(onClick = onNext, enabled = hasTrack) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next", modifier = Modifier.size(38.dp))
        }
        IconButton(onClick = onRepeat, enabled = hasTrack) {
            Icon(Icons.Default.Repeat, contentDescription = "Repeat", tint = if (repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun formatDuration(valueMs: Long): String {
    val totalSeconds = (valueMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
