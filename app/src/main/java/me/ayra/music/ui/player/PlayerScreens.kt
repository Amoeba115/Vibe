package me.ayra.music.ui.player

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.google.android.material.color.utilities.TonalPalette
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ayra.music.AudioInfo
import me.ayra.music.BuildConfig
import me.ayra.music.PlayerState
import me.ayra.music.R
import me.ayra.music.Track
import me.ayra.music.lyrics.LrclibLyricsProvider
import me.ayra.music.lyrics.LyricsRenderer
import me.ayra.music.lyrics.LyricsSearchResult
import me.ayra.music.util.MusicPreferences
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.util.Collections
import java.util.LinkedHashMap
import kotlin.math.roundToInt
import kotlin.math.roundToLong

data class PlayerSheetState(
    val progress: Float,
)

private enum class PlayerSheetAnchor {
    Expanded,
    Collapsed,
}

private enum class PlayerUpperContent {
    Cover,
    Lyrics,
    Playlist,
}

private enum class PlayerQueueSort {
    Custom,
    DateAdded,
    Name,
    Artist,
}

private data class MiniPlayerAccent(
    val primary: Color,
    val container: Color,
    val onContainer: Color,
    val fullscreen: Color,
    val onFullscreen: Color,
)

private data class ChannelOutputOption(
    val value: String,
    val labelRes: Int,
)

private val playerChannelOutputOptions =
    listOf(
        ChannelOutputOption("Auto", R.string.channel_output_auto),
        ChannelOutputOption("AllChannels", R.string.channel_output_all),
        ChannelOutputOption("Channel1", R.string.channel_output_1),
        ChannelOutputOption("Channel2", R.string.channel_output_2),
        ChannelOutputOption("Channel3", R.string.channel_output_3),
        ChannelOutputOption("Channel4", R.string.channel_output_4),
        ChannelOutputOption("Stereo12", R.string.channel_output_stereo12),
        ChannelOutputOption("Stereo34", R.string.channel_output_stereo34),
    )

private val artworkSeedColorCache =
    Collections.synchronizedMap(
        object : LinkedHashMap<String, Int?>(40, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Int?>): Boolean = size > 40
        },
    )

@Composable
fun AlbumArt(
    artwork: Uri?,
    modifier: Modifier,
    shape: RoundedCornerShape,
    contentScale: ContentScale = ContentScale.Crop,
) {
    Box(
        modifier =
            modifier
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (artwork != null) {
            SubcomposeAsyncImage(
                model = artwork,
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier
                                .size(30.dp)
                                .padding(12.dp),
                    )
                },
                error = {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier
                                .size(30.dp)
                                .padding(12.dp),
                    )
                },
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlayerSheet(
    playerState: PlayerState,
    isFavorite: Boolean,
    expandRequest: Int,
    playlistExpandRequest: Int,
    onExpandRequestConsumed: () -> Unit = {},
    onPlaylistExpandRequestConsumed: () -> Unit = {},
    onSettings: () -> Unit,
    onAddTo: (Track) -> Unit,
    onDeleteTrack: (Track) -> Unit,
    onShareTrack: (Track) -> Unit,
    onAlbum: (Track) -> Unit,
    onArtist: (Track) -> Unit,
    onChannelOutput: (Track) -> Unit,
    onAddTracksToCurrentQueueRoute: () -> Unit,
    onReplaceCurrentQueue: (List<Track>) -> Unit,
    onToggleFavorite: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onQueueTrackClick: (Track) -> Unit,
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val preferences = remember(context) { MusicPreferences(context) }
    var miniPlayerStyle by remember { mutableStateOf(preferences.loadMiniPlayerStyle()) }
    DisposableEffect(preferences) {
        val listener =
            android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                miniPlayerStyle = preferences.loadMiniPlayerStyle()
            }
        preferences.registerSettingsListener(listener)
        onDispose { preferences.unregisterSettingsListener(listener) }
    }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val miniPlayerHeight = 68.dp
    var upperContent by rememberSaveable { mutableStateOf(PlayerUpperContent.Cover) }
    val draggableState =
        remember {
            AnchoredDraggableState<PlayerSheetAnchor>(
                initialValue = PlayerSheetAnchor.Collapsed,
            )
        }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val navigationBarBottom = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
        val floatingMiniPlayer = miniPlayerStyle != MusicPreferences.MINI_PLAYER_STYLE_FILLED
        val collapsedOffsetPx =
            with(density) {
                (maxHeight - miniPlayerHeight - navigationBarBottom).toPx().coerceAtLeast(0f)
            }
        val miniHorizontalPadding = if (floatingMiniPlayer) 10.dp else 0.dp
        val maxCoverSize = maxWidth - 48.dp

        SideEffect {
            draggableState.updateAnchors(
                DraggableAnchors {
                    PlayerSheetAnchor.Expanded at 0f
                    PlayerSheetAnchor.Collapsed at collapsedOffsetPx
                },
            )
        }

        LaunchedEffect(expandRequest, collapsedOffsetPx) {
            if (expandRequest > 0) {
                upperContent = PlayerUpperContent.Cover
                draggableState.animateTo(PlayerSheetAnchor.Expanded)
                onExpandRequestConsumed()
            }
        }

        LaunchedEffect(playlistExpandRequest, collapsedOffsetPx) {
            if (playlistExpandRequest > 0) {
                upperContent = PlayerUpperContent.Playlist
                draggableState.animateTo(PlayerSheetAnchor.Expanded)
                onPlaylistExpandRequestConsumed()
            }
        }

        PredictiveBackHandler(enabled = draggableState.currentValue == PlayerSheetAnchor.Expanded) { backProgress ->
            if (upperContent != PlayerUpperContent.Cover) {
                backProgress.collect { }
                upperContent = PlayerUpperContent.Cover
            } else {
                try {
                    backProgress.collect { event ->
                        val targetOffset = collapsedOffsetPx * event.progress.coerceIn(0f, 1f)
                        val currentOffset = draggableState.offset.takeIf { !it.isNaN() } ?: 0f
                        draggableState.dispatchRawDelta(targetOffset - currentOffset)
                    }
                    draggableState.animateTo(PlayerSheetAnchor.Collapsed)
                } catch (_: CancellationException) {
                    draggableState.animateTo(PlayerSheetAnchor.Expanded)
                }
            }
        }

        val offsetPx = draggableState.offset.takeIf { !it.isNaN() } ?: collapsedOffsetPx
        val progress = (1f - (offsetPx / collapsedOffsetPx.coerceAtLeast(1f))).coerceIn(0f, 1f)
        val sheetState = PlayerSheetState(progress)
        val topCornerRadius = lerp(24.dp, 12.dp, progress)
        val bottomCollapsedRadius = if (floatingMiniPlayer) 24.dp else 0.dp
        val bottomCornerRadius = lerp(bottomCollapsedRadius, 12.dp, progress)
        val horizontalPadding = lerp(miniHorizontalPadding, 0.dp, progress)
        val sheetHeight = lerp(miniPlayerHeight, maxHeight, progress)

        PlayerSurface(
            sheetState = sheetState,
            playerState = playerState,
            isFavorite = isFavorite,
            upperContent = upperContent,
            miniPlayerHeight = miniPlayerHeight,
            maxCoverSize = maxCoverSize,
            onExpand = { coroutineScope.launch { draggableState.animateTo(PlayerSheetAnchor.Expanded) } },
            onMinimize = {
                if (upperContent != PlayerUpperContent.Cover) {
                    upperContent = PlayerUpperContent.Cover
                } else {
                    coroutineScope.launch { draggableState.animateTo(PlayerSheetAnchor.Collapsed) }
                }
            },
            onExpandPlaylist = {
                coroutineScope.launch {
                    upperContent = PlayerUpperContent.Playlist
                    draggableState.animateTo(PlayerSheetAnchor.Expanded)
                }
            },
            onSettings = {
                coroutineScope.launch {
                    upperContent = PlayerUpperContent.Cover
                    draggableState.animateTo(PlayerSheetAnchor.Collapsed)
                    onSettings()
                }
            },
            onAddTo = { track ->
                coroutineScope.launch {
                    upperContent = PlayerUpperContent.Cover
                    draggableState.animateTo(PlayerSheetAnchor.Collapsed)
                    onAddTo(track)
                }
            },
            onDeleteTrack = onDeleteTrack,
            onShareTrack = onShareTrack,
            onAlbum = { track ->
                coroutineScope.launch {
                    upperContent = PlayerUpperContent.Cover
                    draggableState.animateTo(PlayerSheetAnchor.Collapsed)
                    onAlbum(track)
                }
            },
            onArtist = { track ->
                coroutineScope.launch {
                    upperContent = PlayerUpperContent.Cover
                    draggableState.animateTo(PlayerSheetAnchor.Collapsed)
                    onArtist(track)
                }
            },
            onChannelOutput = { track ->
                coroutineScope.launch {
                    upperContent = PlayerUpperContent.Cover
                    draggableState.animateTo(PlayerSheetAnchor.Collapsed)
                    onChannelOutput(track)
                }
            },
            onAddTracksToCurrentQueueRoute = {
                coroutineScope.launch {
                    upperContent = PlayerUpperContent.Cover
                    draggableState.animateTo(PlayerSheetAnchor.Collapsed)
                    onAddTracksToCurrentQueueRoute()
                }
            },
            onReplaceCurrentQueue = onReplaceCurrentQueue,
            onToggleFavorite = onToggleFavorite,
            onLyrics = { upperContent = PlayerUpperContent.Lyrics },
            onExitUpperContent = { upperContent = PlayerUpperContent.Cover },
            onSeek = onSeek,
            onShuffle = onShuffle,
            onQueueTrackClick = onQueueTrackClick,
            onPrevious = onPrevious,
            onPlayPause = onPlayPause,
            onNext = onNext,
            onRepeat = onRepeat,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(sheetHeight)
                    .padding(horizontal = horizontalPadding)
                    .offsetY(offsetPx)
                    .anchoredDraggable(
                        state = draggableState,
                        orientation = Orientation.Vertical,
                        enabled = upperContent == PlayerUpperContent.Cover,
                        flingBehavior =
                            AnchoredDraggableDefaults.flingBehavior(
                                state = draggableState,
                                positionalThreshold = { distance -> distance * 0.45f },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            ),
                    ),
            shape =
                RoundedCornerShape(
                    topStart = topCornerRadius,
                    topEnd = topCornerRadius,
                    bottomStart = bottomCornerRadius,
                    bottomEnd = bottomCornerRadius,
                ),
        )
    }
}

private fun Modifier.offsetY(offsetPx: Float): Modifier =
    this.then(
        Modifier
            .padding(top = 0.dp)
            .then(
                Modifier.offset { IntOffset(0, offsetPx.roundToInt()) },
            ),
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PlayerSurface(
    sheetState: PlayerSheetState,
    playerState: PlayerState,
    isFavorite: Boolean,
    upperContent: PlayerUpperContent,
    miniPlayerHeight: Dp,
    maxCoverSize: Dp,
    onExpand: () -> Unit,
    onMinimize: () -> Unit,
    onExpandPlaylist: () -> Unit,
    onSettings: () -> Unit,
    onAddTo: (Track) -> Unit,
    onDeleteTrack: (Track) -> Unit,
    onShareTrack: (Track) -> Unit,
    onAlbum: (Track) -> Unit,
    onArtist: (Track) -> Unit,
    onChannelOutput: (Track) -> Unit,
    onAddTracksToCurrentQueueRoute: () -> Unit,
    onReplaceCurrentQueue: (List<Track>) -> Unit,
    onToggleFavorite: () -> Unit,
    onLyrics: () -> Unit,
    onExitUpperContent: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onQueueTrackClick: (Track) -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit,
    modifier: Modifier,
    shape: RoundedCornerShape,
) {
    val context = LocalContext.current
    val preferences = remember(context) { MusicPreferences(context) }
    var disableAlbumDynamicColor by remember { mutableStateOf(preferences.loadDisableAlbumDynamicColor()) }
    var fancyPlayerEnabled by remember { mutableStateOf(preferences.loadFancyPlayerEnabled()) }
    val darkTheme = isSystemInDarkTheme()
    val progress = sheetState.progress
    val collapsedVisible = 1f - progress
    val expandedVisible = progress
    val collapsedInteractive = collapsedVisible >= 0.5f
    val expandedInteractive = expandedVisible >= 0.5f
    val track = playerState.currentTrack
    val artwork = track?.albumArtUri
    var artworkSeedColor by remember { mutableStateOf<Int?>(null) }
    val defaultMiniAccent =
        MiniPlayerAccent(
            primary = MaterialTheme.colorScheme.primary,
            container = MaterialTheme.colorScheme.primaryContainer,
            onContainer = MaterialTheme.colorScheme.onPrimaryContainer,
            fullscreen = MaterialTheme.colorScheme.background,
            onFullscreen = MaterialTheme.colorScheme.onSurface,
        )
    val miniAccent =
        artworkSeedColor?.takeUnless { disableAlbumDynamicColor }?.let { seed ->
            miniPlayerAccentFromSeed(seed, darkTheme)
        } ?: defaultMiniAccent
    val animatedMiniContainer by animateColorAsState(
        targetValue = miniAccent.container,
        animationSpec = tween(durationMillis = 450),
        label = "mini-container-accent",
    )
    val animatedMiniOnContainer by animateColorAsState(
        targetValue = miniAccent.onContainer,
        animationSpec = tween(durationMillis = 450),
        label = "mini-on-container-accent",
    )
    val animatedPrimary by animateColorAsState(
        targetValue = miniAccent.primary,
        animationSpec = tween(durationMillis = 450),
        label = "player-primary-accent",
    )
    val animatedFullscreen by animateColorAsState(
        targetValue = miniAccent.fullscreen,
        animationSpec = tween(durationMillis = 450),
        label = "fullscreen-accent",
    )
    val animatedOnFullscreen by animateColorAsState(
        targetValue = miniAccent.onFullscreen,
        animationSpec = tween(durationMillis = 450),
        label = "fullscreen-on-accent",
    )
    val surfaceColor =
        lerp(
            animatedMiniContainer,
            animatedFullscreen,
            progress,
        )
    val contentColor =
        lerp(
            animatedMiniOnContainer,
            animatedOnFullscreen,
            progress,
        )
    val animatedMiniAccent =
        miniAccent.copy(
            primary = animatedPrimary,
            container = animatedMiniContainer,
            onContainer = animatedMiniOnContainer,
            fullscreen = animatedFullscreen,
            onFullscreen = animatedOnFullscreen,
        )

    LaunchedEffect(track?.id, artwork, track?.uri, disableAlbumDynamicColor) {
        artworkSeedColor =
            if (disableAlbumDynamicColor) {
                null
            } else {
                track?.let { loadTrackSeedColor(context, it.albumArtUri, it.uri) }
            }
    }

    DisposableEffect(preferences) {
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                disableAlbumDynamicColor = preferences.loadDisableAlbumDynamicColor()
                fancyPlayerEnabled = preferences.loadFancyPlayerEnabled()
            }
        preferences.registerSettingsListener(listener)
        onDispose { preferences.unregisterSettingsListener(listener) }
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = surfaceColor,
        contentColor = contentColor,
        tonalElevation = 4.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (fancyPlayerEnabled && track?.albumId != 0L && artwork != null) {
                FancyPlayerBackground(
                    artwork = artwork,
                    tint = animatedFullscreen,
                    alpha = progress,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            if (progress < 0.5f) {
                ExpandedPlayerContent(
                    playerState = playerState,
                    isFavorite = isFavorite,
                    upperContent = upperContent,
                    alpha = expandedVisible,
                    enabled = expandedInteractive,
                    progress = progress,
                    maxCoverSize = maxCoverSize,
                    onMinimize = onMinimize,
                    onSettings = onSettings,
                    onAddTo = onAddTo,
                    onDeleteTrack = onDeleteTrack,
                    onShareTrack = onShareTrack,
                    onAlbum = onAlbum,
                    onArtist = onArtist,
                    onChannelOutput = onChannelOutput,
                    onAddTracksToCurrentQueueRoute = onAddTracksToCurrentQueueRoute,
                    onReplaceCurrentQueue = onReplaceCurrentQueue,
                    onToggleFavorite = onToggleFavorite,
                    onLyrics = onLyrics,
                    onPlaylist = onExpandPlaylist,
                    onExitUpperContent = onExitUpperContent,
                    onSeek = onSeek,
                    onShuffle = onShuffle,
                    onQueueTrackClick = onQueueTrackClick,
                    onPrevious = onPrevious,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onRepeat = onRepeat,
                    seekAccent = animatedMiniAccent.primary,
                    modifier = Modifier.fillMaxSize(),
                )
                CollapsedPlayerContent(
                    playerState = playerState,
                    alpha = collapsedVisible,
                    enabled = collapsedInteractive,
                    accent = animatedMiniAccent,
                    miniPlayerHeight = miniPlayerHeight,
                    onExpand = onExpand,
                    onPlayPause = onPlayPause,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onPlaylist = onExpandPlaylist,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            } else {
                CollapsedPlayerContent(
                    playerState = playerState,
                    alpha = collapsedVisible,
                    enabled = collapsedInteractive,
                    accent = animatedMiniAccent,
                    miniPlayerHeight = miniPlayerHeight,
                    onExpand = onExpand,
                    onPlayPause = onPlayPause,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onPlaylist = onExpandPlaylist,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
                ExpandedPlayerContent(
                    playerState = playerState,
                    isFavorite = isFavorite,
                    upperContent = upperContent,
                    alpha = expandedVisible,
                    enabled = expandedInteractive,
                    progress = progress,
                    maxCoverSize = maxCoverSize,
                    onMinimize = onMinimize,
                    onSettings = onSettings,
                    onAddTo = onAddTo,
                    onDeleteTrack = onDeleteTrack,
                    onShareTrack = onShareTrack,
                    onAlbum = onAlbum,
                    onArtist = onArtist,
                    onChannelOutput = onChannelOutput,
                    onAddTracksToCurrentQueueRoute = onAddTracksToCurrentQueueRoute,
                    onReplaceCurrentQueue = onReplaceCurrentQueue,
                    onToggleFavorite = onToggleFavorite,
                    onLyrics = onLyrics,
                    onPlaylist = onExpandPlaylist,
                    onExitUpperContent = onExitUpperContent,
                    onSeek = onSeek,
                    onShuffle = onShuffle,
                    onQueueTrackClick = onQueueTrackClick,
                    onPrevious = onPrevious,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onRepeat = onRepeat,
                    seekAccent = animatedMiniAccent.primary,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            AnchoredCoverArt(
                artwork = playerState.currentTrack?.albumArtUri,
                progress = progress,
                visible = upperContent == PlayerUpperContent.Cover,
                miniPlayerHeight = miniPlayerHeight,
                maxCoverSize = maxCoverSize,
                onExpand = onExpand,
                onLyrics = onLyrics,
                modifier = Modifier.align(Alignment.TopStart),
            )
        }
    }
}

@Composable
private fun FancyPlayerBackground(
    artwork: Uri,
    tint: Color,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    val visibleAlpha = alpha.coerceIn(0f, 1f)
    Box(modifier = modifier.alpha(visibleAlpha)) {
        AsyncImage(
            model = artwork,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxSize()
                    .blur(36.dp)
                    .alpha(0.22f),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CollapsedPlayerContent(
    playerState: PlayerState,
    alpha: Float,
    enabled: Boolean,
    accent: MiniPlayerAccent,
    miniPlayerHeight: Dp,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = playerState.currentTrack
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(miniPlayerHeight)
                .clickable(enabled = enabled, onClick = onExpand)
                .padding(start = 64.dp, end = 8.dp)
                .alpha(alpha),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f),
        ) {
            Text(
                track?.title ?: "No track selected",
                modifier = Modifier.basicMarquee(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Text(
                track?.artist ?: "Choose music to play",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = accent.onContainer.copy(alpha = 0.75f),
                fontSize = 11.sp,
            )
        }
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.size(40.dp),
            enabled = enabled && track != null,
        ) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(20.dp))
        }
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(40.dp),
            enabled = enabled && track != null,
        ) {
            Icon(
                if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play or pause",
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(
            onClick = onNext,
            modifier = Modifier.size(40.dp),
            enabled = enabled && track != null,
        ) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next", modifier = Modifier.size(20.dp))
        }
        IconButton(
            onClick = onPlaylist,
            modifier = Modifier.size(40.dp),
            enabled = enabled && track != null,
        ) {
            Icon(Icons.Default.QueueMusic, contentDescription = "Queue", modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun AnchoredCoverArt(
    artwork: Uri?,
    progress: Float,
    visible: Boolean,
    miniPlayerHeight: Dp,
    maxCoverSize: Dp,
    onExpand: () -> Unit,
    onLyrics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible) return

    val size = lerp(46.dp, maxCoverSize, progress)
    val radius = lerp(14.dp, 34.dp, progress)
    val startX = lerp(12.dp, 24.dp, progress)
    val miniTop = (miniPlayerHeight - 46.dp) / 2
    val expandedTop = 24.dp + 18.dp + 48.dp + 28.dp
    val top = lerp(miniTop, expandedTop, progress)
    val anchoredToFullscreen = progress >= 0.999f
    val contentScale = if (anchoredToFullscreen) ContentScale.Fit else ContentScale.Crop
    val coverClick = if (progress < 0.5f) onExpand else onLyrics
    val coverClickEnabled = progress < 0.5f || progress > 0.85f
    val coverInteractionSource = remember { MutableInteractionSource() }

    AlbumArt(
        artwork = artwork,
        contentScale = contentScale,
        modifier =
            modifier
                .padding(start = startX, top = top)
                .size(size)
                .clickable(
                    interactionSource = coverInteractionSource,
                    indication = null,
                    enabled = coverClickEnabled,
                    onClick = coverClick,
                ),
        shape = RoundedCornerShape(radius),
    )
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ExpandedPlayerContent(
    playerState: PlayerState,
    isFavorite: Boolean,
    upperContent: PlayerUpperContent,
    alpha: Float,
    enabled: Boolean,
    progress: Float,
    maxCoverSize: Dp,
    onMinimize: () -> Unit,
    onSettings: () -> Unit,
    onAddTo: (Track) -> Unit,
    onDeleteTrack: (Track) -> Unit,
    onShareTrack: (Track) -> Unit,
    onAlbum: (Track) -> Unit,
    onArtist: (Track) -> Unit,
    onChannelOutput: (Track) -> Unit,
    onAddTracksToCurrentQueueRoute: () -> Unit,
    onReplaceCurrentQueue: (List<Track>) -> Unit,
    onToggleFavorite: () -> Unit,
    onLyrics: () -> Unit,
    onPlaylist: () -> Unit,
    onExitUpperContent: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onQueueTrackClick: (Track) -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit,
    seekAccent: Color,
    modifier: Modifier = Modifier,
) {
    val track = playerState.currentTrack
    val context = LocalContext.current
    val preferences = remember(context) { MusicPreferences(context) }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var channelDialog by rememberSaveable { mutableStateOf(false) }
    var channelOutput by rememberSaveable { mutableStateOf(preferences.loadVgmChannelOutput()) }
    val isVgmstreamTrack = track?.isVgmstreamTrack() == true

    if (confirmDelete && track != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_permanently)) },
            text = { Text(stringResource(R.string.delete_tracks_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDeleteTrack(track)
                    },
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
    if (channelDialog) {
        PlayerChannelOutputDialog(
            selectedValue = channelOutput,
            onDismiss = { channelDialog = false },
            onSelect = {
                channelOutput = it
                preferences.saveVgmChannelOutput(it)
                channelDialog = false
            },
        )
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp)
                .alpha(alpha),
    ) {
        AnimatedContent(
            targetState = upperContent,
            transitionSpec = {
                fadeIn(spring(stiffness = Spring.StiffnessLow)) togetherWith fadeOut() using SizeTransform(clip = false)
            },
            label = "player-upper-content",
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        ) { content ->
            when (content) {
                PlayerUpperContent.Lyrics -> {
                    LyricsContent(
                        playerState = playerState,
                        enabled = enabled,
                        accent = seekAccent,
                        onSeek = onSeek,
                        onExit = onExitUpperContent,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                PlayerUpperContent.Playlist -> {
                    PlaylistContent(
                        playerState = playerState,
                        enabled = enabled,
                        onExit = onExitUpperContent,
                        onQueueTrackClick = onQueueTrackClick,
                        onAddTracksToCurrentQueueRoute = onAddTracksToCurrentQueueRoute,
                        onReplaceCurrentQueue = onReplaceCurrentQueue,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                PlayerUpperContent.Cover -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 18.dp, bottom = 28.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = onMinimize, enabled = enabled) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", modifier = Modifier.size(34.dp))
                            }
                            Spacer(Modifier.weight(1f))
                            Box {
                                IconButton(
                                    onClick = { menuExpanded = true },
                                    enabled = enabled,
                                ) { Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.menu)) }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.delete)) },
                                        enabled = track != null,
                                        onClick = {
                                            menuExpanded = false
                                            confirmDelete = true
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.share)) },
                                        enabled = track != null,
                                        onClick = {
                                            menuExpanded = false
                                            track?.let(onShareTrack)
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.album)) },
                                        enabled = track != null,
                                        onClick = {
                                            menuExpanded = false
                                            track?.let(onAlbum)
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.artist)) },
                                        enabled = track != null,
                                        onClick = {
                                            menuExpanded = false
                                            track?.let(onArtist)
                                        },
                                    )
                                    if (isVgmstreamTrack) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.channel_output)) },
                                            onClick = {
                                                menuExpanded = false
                                                channelDialog = true
                                            },
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.settings)) },
                                        onClick = {
                                            menuExpanded = false
                                            onSettings()
                                        },
                                    )
                                }
                            }
                        }
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(maxCoverSize),
                            contentAlignment = Alignment.TopCenter,
                        ) {}
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                track?.title ?: "No track selected",
                                modifier = Modifier.basicMarquee(),
                                fontSize = 24.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                track?.artist ?: "Choose music to play",
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            track?.audioInfo?.formatAudioInfo()?.let { info ->
                                Text(
                                    info,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            IconButton(onClick = onPlaylist, enabled = enabled && playerState.queue.isNotEmpty()) {
                                Icon(Icons.Default.QueueMusic, contentDescription = "Queue", modifier = Modifier.size(32.dp))
                            }
                            IconButton(onClick = onToggleFavorite, enabled = enabled && track != null) {
                                Icon(
                                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    modifier = Modifier.size(34.dp),
                                )
                            }
                            IconButton(
                                onClick = { track?.let(onAddTo) },
                                enabled = enabled && track != null,
                            ) { Icon(Icons.Default.Add, contentDescription = "Add to", modifier = Modifier.size(36.dp)) }
                        }
                    }
                }
            }
        }
        SeekBar(
            positionMs = playerState.positionMs,
            durationMs = playerState.durationMs,
            enabled = enabled,
            accent = seekAccent,
            onSeek = onSeek,
        )
        Controls(
            isPlaying = playerState.isPlaying,
            shuffle = playerState.shuffle,
            repeatMode = playerState.repeatMode,
            hasTrack = track != null,
            enabled = enabled,
            accent = seekAccent,
            onShuffle = onShuffle,
            onPrevious = onPrevious,
            onPlayPause = onPlayPause,
            onNext = onNext,
            onRepeat = onRepeat,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LyricsContent(
    playerState: PlayerState,
    enabled: Boolean,
    accent: Color,
    onSeek: (Long) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = playerState.currentTrack
    val coroutineScope = rememberCoroutineScope()
    val lyricsProvider = remember { LrclibLyricsProvider() }
    var selectedLyrics by remember(track?.id) { mutableStateOf(playerState.lyrics) }
    var showSearchSheet by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable(track?.id) { mutableStateOf(track?.title.orEmpty()) }
    var searchLoading by rememberSaveable { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf(emptyList<LyricsSearchResult>()) }
    val visibleLyrics = selectedLyrics ?: playerState.lyrics

    LaunchedEffect(playerState.lyrics, track?.id) {
        if (selectedLyrics == null) {
            selectedLyrics = playerState.lyrics
        }
    }

    fun searchLyrics(query: String) {
        if (query.isBlank()) {
            searchResults = emptyList()
            return
        }
        searchLoading = true
        coroutineScope.launch {
            searchResults = lyricsProvider.searchResults(query)
            searchLoading = false
        }
    }

    if (showSearchSheet) {
        LyricsSearchSheet(
            query = searchQuery,
            loading = searchLoading,
            results = searchResults,
            onQueryChange = {
                searchQuery = it
            },
            onDismiss = { showSearchSheet = false },
            onResultClick = {
                selectedLyrics = it.lyrics
                showSearchSheet = false
            },
            onSearch = { searchLyrics(searchQuery) },
        )
    }

    Column(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onExit, enabled = enabled) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Show cover", modifier = Modifier.size(34.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    track?.title ?: "No track selected",
                    modifier = Modifier.basicMarquee(),
                    fontSize = 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    track?.artist ?: "",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            FilledIconButton(
                onClick = {
                    showSearchSheet = true
                    searchQuery = track?.title.orEmpty()
                    searchLyrics(searchQuery)
                },
                shape = CircleShape,
            )
            {
                Icon(Icons.Default.ManageSearch, null)
            }
        }
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 28.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                playerState.lyricsLoading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator()

                        Text(
                            text = stringResource(R.string.loading_lyric),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                visibleLyrics != null -> {
                    LyricsRenderer(
                        lyrics = visibleLyrics,
                        positionMs = playerState.positionMs,
                        accent = accent,
                        onTimestampClick = onSeek,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    Text(stringResource(R.string.no_lyric_found), fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricsSearchSheet(
    query: String,
    loading: Boolean,
    results: List<LyricsSearchResult>,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onResultClick: (LyricsSearchResult) -> Unit,
    onSearch: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column {
                Text(
                    text = stringResource(R.string.search_lyric),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = stringResource(R.string.search_lyric_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    placeholder = {
                        Text("Track / Artist")
                    },
                    keyboardOptions =
                        KeyboardOptions(
                            imeAction = ImeAction.Search,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onSearch = { onSearch() },
                        ),
                )

                FilledIconButton(
                    onClick = { onSearch() },
                ) {
                    Icon(Icons.Default.Search, null)
                }
            }
            when {
                loading -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            CircularProgressIndicator()

                            Text(
                                text = stringResource(R.string.loading_lyric),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                results.isEmpty() -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                Icons.Default.LibraryMusic,
                                null,
                                modifier = Modifier.size(64.dp),
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                stringResource(R.string.no_lyric_found),
                                style = MaterialTheme.typography.titleMedium,
                            )

                            Text(
                                stringResource(R.string.no_lyric_found_description),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            results,
                            key = { "${it.id}-${it.title}-${it.artist}" },
                        ) { result ->
                            LyricsSearchResultRow(
                                result = result,
                                onClick = {
                                    onResultClick(result)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricsSearchResultRow(
    result: LyricsSearchResult,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(52.dp),
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text =
                        result.title.ifBlank {
                            stringResource(R.string.track)
                        },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = result.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (result.album.isNotBlank()) {
                    Text(
                        text = result.album,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (result.lyrics.lines.any { it.startMs != null }) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = {
                            Text("Synced")
                        },
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        text = formatDuration(result.durationMs),
                        modifier =
                            Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 4.dp,
                            ),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
private fun PlaylistContent(
    playerState: PlayerState,
    enabled: Boolean,
    onExit: () -> Unit,
    onQueueTrackClick: (Track) -> Unit,
    onAddTracksToCurrentQueueRoute: () -> Unit,
    onReplaceCurrentQueue: (List<Track>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val currentTrackId = playerState.currentTrack?.id
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var sortMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var editMode by rememberSaveable { mutableStateOf(false) }
    var selectedTrackIds by rememberSaveable { mutableStateOf(emptyList<Long>()) }
    var sortMode by rememberSaveable { mutableStateOf(PlayerQueueSort.Custom) }
    val sortedQueue =
        remember(playerState.queue, sortMode) {
            when (sortMode) {
                PlayerQueueSort.Custom -> {
                    playerState.queue
                }

                PlayerQueueSort.DateAdded -> {
                    playerState.queue.sortedByDescending { it.dateAddedMs }
                }

                PlayerQueueSort.Name -> {
                    playerState.queue.sortedBy { it.title.lowercase() }
                }

                PlayerQueueSort.Artist -> {
                    playerState.queue.sortedWith(
                        compareBy<Track> { it.artist.lowercase() }.thenBy { it.title.lowercase() },
                    )
                }
            }
        }
    val activeIndex = sortedQueue.indexOfFirst { it.id == currentTrackId }
    val activeCount = if (activeIndex >= 0) activeIndex + 1 else 0
    val allSelected = sortedQueue.isNotEmpty() && selectedTrackIds.size == sortedQueue.size
    val reorderableLazyListState =
        rememberReorderableLazyListState(listState) { from, to ->
            val fromIndex = from.index
            val toIndex = to.index
            if (editMode && fromIndex in sortedQueue.indices && toIndex in sortedQueue.indices && fromIndex != toIndex) {
                val reorderedQueue =
                    sortedQueue.toMutableList().apply {
                        add(toIndex, removeAt(fromIndex))
                    }
                sortMode = PlayerQueueSort.Custom
                onReplaceCurrentQueue(reorderedQueue)
            }
        }

    fun toggleTrack(track: Track) {
        selectedTrackIds =
            if (track.id in selectedTrackIds) {
                selectedTrackIds - track.id
            } else {
                selectedTrackIds + track.id
            }
    }

    fun enterEditMode(track: Track? = null) {
        editMode = true
        if (track != null && selectedTrackIds.isEmpty()) {
            selectedTrackIds = listOf(track.id)
        }
    }

    fun exitEditMode() {
        editMode = false
        selectedTrackIds = emptyList()
    }

    BackHandler(enabled = editMode) {
        exitEditMode()
    }

    LaunchedEffect(currentTrackId, sortedQueue) {
        val activeIndex = sortedQueue.indexOfFirst { it.id == currentTrackId }
        if (activeIndex >= 0) {
            listState.scrollToItem(activeIndex)
            withFrameNanos { }
            val layoutInfo = listState.layoutInfo
            val activeItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == activeIndex }
            if (activeItem != null) {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val itemCenter = activeItem.offset + activeItem.size / 2
                listState.scrollBy((itemCenter - viewportCenter).toFloat())
            }
        }
    }

    LaunchedEffect(sortedQueue) {
        val visibleIds = sortedQueue.map { it.id }.toSet()
        selectedTrackIds = selectedTrackIds.filter { it in visibleIds }
        if (editMode && sortedQueue.isEmpty()) {
            exitEditMode()
        }
    }

    Column(modifier = modifier) {
        AnimatedContent(
            targetState = editMode,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
            label = "playlist-edit-top-bar",
        ) { editing ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (editing) {
                    IconButton(
                        onClick = {
                            selectedTrackIds =
                                if (allSelected) {
                                    emptyList()
                                } else {
                                    sortedQueue.map { it.id }
                                }
                        },
                        enabled = enabled && sortedQueue.isNotEmpty(),
                    ) {
                        Icon(
                            if (allSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = stringResource(if (allSelected) R.string.unselect_all else R.string.select_all),
                        )
                    }
                    Text(
                        text =
                            if (selectedTrackIds.isEmpty()) {
                                stringResource(R.string.select_track)
                            } else {
                                stringResource(R.string.selected_count_plain, selectedTrackIds.size)
                            },
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                    IconButton(
                        onClick = {
                            val selected = selectedTrackIds.toSet()
                            onReplaceCurrentQueue(playerState.queue.filterNot { it.id in selected })
                            exitEditMode()
                        },
                        enabled = enabled && selectedTrackIds.isNotEmpty(),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                    }
                } else {
                    IconButton(onClick = onExit, enabled = enabled) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Show player", modifier = Modifier.size(34.dp))
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Playlist", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text(
                            stringResource(R.string.tracks_count, playerState.queue.size),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }, enabled = enabled) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.playlist_menu))
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.add)) },
                                enabled = true,
                                onClick = {
                                    menuExpanded = false
                                    onAddTracksToCurrentQueueRoute()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.edit)) },
                                enabled = sortedQueue.isNotEmpty(),
                                onClick = {
                                    menuExpanded = false
                                    enterEditMode()
                                },
                            )
                        }
                    }
                }
            }
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                TextButton(
                    onClick = { sortMenuExpanded = true },
                    enabled = enabled && !editMode,
                ) {
                    Icon(
                        Icons.Default.Sort,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                    Text(playerQueueSortLabel(sortMode))
                }
                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false },
                ) {
                    PlayerQueueSort.entries.forEach { sort ->
                        DropdownMenuItem(
                            text = { Text(playerQueueSortLabel(sort)) },
                            onClick = {
                                sortMode = sort
                                sortMenuExpanded = false
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "$activeCount/${sortedQueue.size}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
            )
        }
        if (sortedQueue.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("No playlist", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 22.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding =
                    androidx.compose.foundation.layout
                        .PaddingValues(bottom = 28.dp),
            ) {
                itemsIndexed(sortedQueue, key = { _, track -> track.id }) { index, track ->
                    val selected = track.id == playerState.currentTrack?.id
                    if (editMode) {
                        ReorderableItem(reorderableLazyListState, key = track.id) { isDragging ->
                            val reorderScope = this
                            Surface(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .animateItem(),
                                shape = RoundedCornerShape(0.dp),
                                color = Color.Transparent,
                                tonalElevation = if (isDragging) 6.dp else 0.dp,
                            ) {
                                QueueTrackRow(
                                    index = index + 1,
                                    track = track,
                                    selected = selected,
                                    editMode = true,
                                    checked = track.id in selectedTrackIds,
                                    showDivider = index != sortedQueue.lastIndex,
                                    enabled = enabled,
                                    dragHandleModifier = with(reorderScope) { Modifier.draggableHandle() },
                                    onClick = { toggleTrack(track) },
                                    onLongClick = { },
                                )
                            }
                        }
                    } else {
                        QueueTrackRow(
                            index = index + 1,
                            track = track,
                            selected = selected,
                            editMode = false,
                            checked = false,
                            showDivider = index != sortedQueue.lastIndex,
                            enabled = enabled,
                            dragHandleModifier = Modifier,
                            onClick = { onQueueTrackClick(track) },
                            onLongClick = { enterEditMode(track) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun playerQueueSortLabel(sort: PlayerQueueSort): String =
    when (sort) {
        PlayerQueueSort.Custom -> stringResource(R.string.custom_order)
        PlayerQueueSort.DateAdded -> stringResource(R.string.date_added)
        PlayerQueueSort.Name -> stringResource(R.string.name)
        PlayerQueueSort.Artist -> stringResource(R.string.artist)
    }

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
private fun QueueTrackRow(
    index: Int,
    track: Track,
    selected: Boolean,
    editMode: Boolean,
    checked: Boolean,
    showDivider: Boolean,
    enabled: Boolean,
    dragHandleModifier: Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        enabled = enabled,
                        onClick = onClick,
                        onLongClick = onLongClick,
                    ).padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(width = 34.dp, height = 48.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                AnimatedContent(
                    targetState = editMode,
                    transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
                    label = "queue-row-selection",
                ) { editing ->
                    if (editing) {
                        Icon(
                            if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = index.toString(),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
            AlbumArt(track.albumArtUri, Modifier.size(48.dp), RoundedCornerShape(12.dp))
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
            ) {
                Text(
                    track.title,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 17.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
                Text(
                    track.artist,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp,
                )
            }
            AnimatedContent(
                targetState = editMode,
                transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
                label = "queue-row-trailing",
            ) { editing ->
                if (editing) {
                    Box(
                        modifier =
                            dragHandleModifier
                                .padding(start = 8.dp)
                                .size(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.DragIndicator,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Text(
                        formatDuration(track.durationMs),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 58.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            )
        }
    }
}

@Composable
private fun SeekBar(
    positionMs: Long,
    durationMs: Long,
    enabled: Boolean,
    accent: Color,
    onSeek: (Long) -> Unit,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val density = LocalDensity.current
    var dragging by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(positionMs.coerceAtLeast(0L).toFloat()) }
    val safeDuration = durationMs.coerceAtLeast(0L)
    val displayPosition = if (dragging) sliderValue.roundToLong() else positionMs

    LaunchedEffect(positionMs, safeDuration, dragging) {
        if (!dragging) {
            sliderValue = positionMs.coerceIn(0L, safeDuration.coerceAtLeast(1L)).toFloat()
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .pointerInput(enabled, safeDuration) {
                        if (!enabled || safeDuration <= 0L) return@pointerInput
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            dragging = true

                            fun updateValue(x: Float) {
                                val width = size.width.toFloat().coerceAtLeast(1f)
                                sliderValue =
                                    ((x.coerceIn(0f, width) / width) * safeDuration).coerceIn(
                                        0f,
                                        safeDuration.toFloat(),
                                    )
                            }

                            updateValue(down.position.x)
                            drag(down.id) { change ->
                                updateValue(change.position.x)
                                change.consume()
                            }
                            dragging = false
                            onSeek(sliderValue.roundToLong().coerceIn(0L, safeDuration))
                        }
                    },
        )
        {
            val trackHeight = with(density) { 4.dp.toPx() }
            val thumbRadius = with(density) { 5.dp.toPx() }
            val centerY = size.height / 2f
            val progress =
                if (safeDuration > 0L) {
                    (sliderValue / safeDuration.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
            val thumbX = size.width * progress
            val activeColor = if (enabled && safeDuration > 0L) accent else onSurface.copy(alpha = 0.35f)
            val inactiveColor = if (enabled && safeDuration > 0L) accent.copy(alpha = 0.18f) else onSurface.copy(alpha = 0.15f)

            drawLine(
                color = onSurface.copy(alpha = 0.15f),
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = inactiveColor,
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = activeColor,
                start = Offset(0f, centerY),
                end = Offset(thumbX, centerY),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round,
            )
            drawCircle(
                color = activeColor,
                radius = thumbRadius,
                center = Offset(thumbX, centerY),
            )
        }
        Row(Modifier.fillMaxWidth()) {
            Text(formatDuration(displayPosition), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    enabled: Boolean,
    accent: Color,
    onShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 22.dp, bottom = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        AccentOptionButton(
            active = shuffle,
            enabled = enabled && hasTrack,
            accent = accent,
            onClick = onShuffle,
        ) {
            Icon(Icons.Default.Shuffle, contentDescription = "Shuffle")
        }
        IconButton(onClick = onPrevious, enabled = enabled && hasTrack) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(38.dp))
        }
        IconButton(onClick = onPlayPause, enabled = enabled && hasTrack, modifier = Modifier.size(72.dp)) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play or pause",
                modifier = Modifier.size(54.dp),
            )
        }
        IconButton(onClick = onNext, enabled = enabled && hasTrack) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next", modifier = Modifier.size(38.dp))
        }
        AccentOptionButton(
            active = repeatMode != Player.REPEAT_MODE_OFF,
            enabled = enabled && hasTrack,
            accent = accent,
            onClick = onRepeat,
        ) {
            Icon(
                if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                contentDescription =
                    when (repeatMode) {
                        Player.REPEAT_MODE_ONE -> "Repeat one"
                        Player.REPEAT_MODE_ALL -> "Repeat all"
                        else -> "Repeat off"
                    },
            )
        }
    }
}

@Composable
private fun AccentOptionButton(
    active: Boolean,
    enabled: Boolean,
    accent: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val contentColor =
        when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            active -> accent
            else -> MaterialTheme.colorScheme.onSurface
        }
    IconButton(onClick = onClick, enabled = enabled) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides contentColor,
            content = content,
        )
    }
}

private fun formatDuration(valueMs: Long): String {
    val totalSeconds = (valueMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private fun AudioInfo.formatAudioInfo(): String? {
    val parts =
        listOfNotNull(
            codec,
            bitDepth?.let { "$it-bit" },
            sampleRate?.let { rate ->
                if (rate >= 1000) {
                    val khz = rate / 1000f
                    if (rate % 1000 == 0) {
                        "${rate / 1000} kHz"
                    } else {
                        "%.1f kHz".format(khz)
                    }
                } else {
                    "$rate Hz"
                }
            },
            bitrate?.let { "${it / 1000} kbps" },
            channels?.let { if (it > 2) "$it channels" else "" },
        ).filter { it.isNotBlank() }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" • ")
}

private fun Track.isVgmstreamTrack(): Boolean = BuildConfig.IS_VGM_BUILD && uri.scheme.equals("file", ignoreCase = true)

@Composable
private fun PlayerChannelOutputDialog(
    selectedValue: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.channel_output)) },
        text = {
            Column {
                playerChannelOutputOptions.forEach { option ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelect(option.value) }
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val selected = option.value == selectedValue
                        Icon(
                            if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = stringResource(option.labelRes),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
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

private suspend fun loadTrackSeedColor(
    context: Context,
    artwork: Uri,
    trackUri: Uri,
): Int? {
    val cacheKey = "$artwork|$trackUri"
    if (artworkSeedColorCache.containsKey(cacheKey)) {
        return artworkSeedColorCache[cacheKey]
    }

    val seedColor =
        withContext(Dispatchers.IO) {
            loadSeedColorWithCoil(context, artwork)
                ?: loadSeedColorWithCoil(context, trackUri)
                ?: loadEmbeddedSeedColor(context, trackUri)
        }

    artworkSeedColorCache[cacheKey] = seedColor
    return seedColor
}

private suspend fun loadSeedColorWithCoil(
    context: Context,
    uri: Uri,
): Int? {
    return runCatching {
        val request =
            ImageRequest
                .Builder(context)
                .data(uri)
                .size(192)
                .build()
        val result = ImageLoader(context).execute(request) as? SuccessResult
        val image = result?.image ?: return@runCatching null
        val bitmap = image.toBitmap(image.width.coerceAtLeast(1), image.height.coerceAtLeast(1))
        bitmap.toPaletteSeedColor()
    }.getOrNull()
}

private fun loadEmbeddedSeedColor(
    context: Context,
    trackUri: Uri,
): Int? {
    return runCatching {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, trackUri)
            val bytes = retriever.embeddedPicture ?: return@runCatching null
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@runCatching null
            bitmap.toPaletteSeedColor()
        } finally {
            retriever.release()
        }
    }.getOrNull()
}

private fun android.graphics.Bitmap.toPaletteSeedColor(): Int? {
    val palette =
        Palette
            .from(this)
            .maximumColorCount(24)
            .generate()
    return palette.vibrantSwatch?.rgb
        ?: palette.lightVibrantSwatch?.rgb
        ?: palette.darkVibrantSwatch?.rgb
        ?: palette.mutedSwatch?.rgb
        ?: palette.dominantSwatch?.rgb
}

private fun miniPlayerAccentFromSeed(
    seedColor: Int,
    darkTheme: Boolean,
): MiniPlayerAccent {
    val palette = TonalPalette.fromInt(seedColor)
    return if (darkTheme) {
        MiniPlayerAccent(
            primary = Color(palette.tone(70)),
            container = Color(palette.tone(35)),
            onContainer = Color(palette.tone(90)),
            fullscreen = Color(palette.tone(10)),
            onFullscreen = Color(palette.tone(90)),
        )
    } else {
        MiniPlayerAccent(
            primary = Color(palette.tone(40)),
            container = Color(palette.tone(85)),
            onContainer = Color(palette.tone(10)),
            fullscreen = Color(palette.tone(96)),
            onFullscreen = Color(palette.tone(10)),
        )
    }
}
