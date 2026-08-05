package me.ayra.music.ui.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SnippetFolder
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.ayra.music.AlbumGroup
import me.ayra.music.ArtistGroup
import me.ayra.music.FavoriteType
import me.ayra.music.FolderGroup
import me.ayra.music.LibraryState
import me.ayra.music.PlaylistGroup
import me.ayra.music.R
import me.ayra.music.Track
import me.ayra.music.TrackStats
import me.ayra.music.toAlbumGroups
import me.ayra.music.ui.navigation.MainRoute
import me.ayra.music.ui.navigation.MusicNavigator
import me.ayra.music.ui.player.AlbumArt
import me.ayra.music.ui.player.TrackInfoDialog
import me.ayra.music.ui.player.TrackMetadataEditorDialog
import me.ayra.music.ui.settings.SettingsScreen
import me.ayra.music.util.MusicPreferences
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random

enum class HomeTab(
    @StringRes val labelRes: Int,
) {
    Favorite(R.string.favorite),
    Playlist(R.string.playlist),
    Track(R.string.track),
    Album(R.string.album),
    Artist(R.string.artist),
    Folder(R.string.folder),
}

private enum class TrackSort(
    @StringRes val labelRes: Int,
) {
    Name(R.string.name),
    DateAdded(R.string.date_added),
    Artist(R.string.artist),
}

private enum class AlbumSort(
    @StringRes val labelRes: Int,
) {
    Release(R.string.sort_release),
    Name(R.string.name),
    Artist(R.string.artist),
}

private enum class ArtistSort(
    @StringRes val labelRes: Int,
) {
    Name(R.string.name),
    DateAdded(R.string.date_added),
}

private enum class FolderSort(
    @StringRes val labelRes: Int,
) {
    Name(R.string.name),
    DateAdded(R.string.date_added),
}

private enum class FolderViewMode(
    @StringRes val labelRes: Int,
) {
    Folder(R.string.folder),
    Tree(R.string.folder_view_tree),
}

private enum class PlaylistSort(
    @StringRes val labelRes: Int,
) {
    CustomOrder(R.string.custom_order),
    Name(R.string.name),
    Artist(R.string.artist),
}

private enum class PlaylistListSort(
    @StringRes val labelRes: Int,
) {
    DateAdded(R.string.date_added),
    Name(R.string.name),
    DatePlayed(R.string.sort_date_played),
}

private enum class SelectPickerTab {
    Track,
    Album,
    Artist,
    Folder,
}

private enum class SubParentSort(
    @StringRes val labelRes: Int,
) {
    Name(R.string.name),
    DateAdded(R.string.date_added),
}

private const val SORT_TRACK = "track"
private const val SORT_ALBUM = "album"
private const val SORT_ARTIST = "artist"
private const val SORT_FOLDER = "folder"
private const val FOLDER_VIEW_MODE = "folder_view_mode"
private const val SORT_PLAYLIST_LIST = "playlist_list"
private const val SORT_PLAYLIST_PREFIX = "playlist_"
private const val CUSTOM_PLAYLIST_PREFIX = "custom-"
private const val SORT_SUB_PARENT_PREFIX = "sub_parent_"
private const val PLAYLIST_RECENTLY_ADDED = "recently-added"
private const val PLAYLIST_MOST_PLAYED = "most-played"
private const val PLAYLIST_JUST_PLAYED = "just-played"
private const val PLAYLIST_FAVORITE_TRACK = "favorite-track"
private const val FOLDER_TREE_INTERNAL = "Internal storage"
private const val FOLDER_TREE_MICRO_SD = "Micro SD"
private const val FOLDER_TREE_EXTERNAL = "External storage"
private const val FOLDER_TREE_NAV_DUR = 280
private const val ALBUM_SNAP_EXPAND_THRESHOLD = 0.35f
private const val ALBUM_SNAP_FLING_DELTA_PX = 72
private const val FANCY_TAB_CONTENT_ALPHA = 0.5f
private const val FANCY_TAB_CONTENT_ALPHA_TRANSPARENT = 0.0f
private val HOME_TAB_WIDTH = 104.dp
private val MINI_PLAYER_RESERVED_BOTTOM = 116.dp
private val LocalFancyTabContentBackground = staticCompositionLocalOf { false }
private val FOLDER_TREE_ROOT_ORDER = listOf(FOLDER_TREE_INTERNAL, FOLDER_TREE_MICRO_SD, FOLDER_TREE_EXTERNAL)

private data class AlphabetIndex(
    val letter: Char,
    val position: Int,
)

private data class FolderTreeEntry(
    val root: String,
    val segments: List<String>,
    val folder: FolderGroup,
)

private data class FolderTreeNode(
    val key: String,
    val name: String,
    val subtitle: String,
    val tracks: List<Track>,
)

private fun MainRoute.saveableStateKey(searchStateVersion: Int): String =
    when (this) {
        MainRoute.Home -> "home"
        MainRoute.Search -> "search:$searchStateVersion"
        is MainRoute.SearchTracks -> "search-tracks:$query"
        is MainRoute.SearchArtists -> "search-artists:$query"
        is MainRoute.SearchAlbums -> "search-albums:$query"
        MainRoute.Settings -> "settings"
        is MainRoute.Album -> "album:$id"
        is MainRoute.Artist -> "artist:$name"
        is MainRoute.Folder -> "folder:$path"
        is MainRoute.Playlist -> "playlist:$id"
        is MainRoute.SelectPlaylistTracks -> "select-playlist-tracks:$name"
        is MainRoute.AddTracksToPlaylist -> "add-tracks-to-playlist:$id"
        MainRoute.AddTracksToCurrentQueue -> "add-tracks-to-current-queue"
        is MainRoute.AddToPlaylist -> "add-to-playlist:$trackId"
        is MainRoute.AddToTracks -> "add-to-tracks:${trackIds.joinToString(",")}"
    }

@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen(
    library: LibraryState,
    navigator: MusicNavigator,
    currentQueue: List<Track>,
    currentTrackId: Long?,
    fancyBackgroundEnabled: Boolean = false,
    onRequestPermission: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onToggleFavoriteItem: (String, String) -> Unit,
    onRescan: () -> Unit,
    onHiddenFoldersChanged: (Set<String>) -> Unit,
    onImportPlaylist: () -> Unit,
    onExportPlaylists: () -> Unit,
    onCreatePlaylist: (String, List<Track>) -> Unit,
    onAddTracksToPlaylist: (String, List<Track>) -> Unit,
    onAddTracksToCurrentQueue: (List<Track>) -> Unit,
    onReplaceCurrentQueue: (List<Track>) -> Unit,
    onReplacePlaylistTracks: (String, List<Track>) -> Unit,
    onRenamePlaylist: (String, String) -> Unit,
    onDeletePlaylists: (List<String>) -> Unit,
    onDeleteTracksPermanently: (List<Track>) -> Unit,
    onAddTracksToRoute: (List<Track>) -> Unit,
    onAddTracksToCurrentQueueRoute: () -> Unit,
    onCurrentQueueTracksAdded: () -> Unit,
    onPlaylistEditModeChanged: (Boolean) -> Unit,
    initialTabIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    val context = LocalContext.current
    val currentRoute = navigator.currentRoute
    val detailStateHolder = rememberSaveableStateHolder()
    val homeStateHolder = rememberSaveableStateHolder()
    var searchStateVersion by rememberSaveable { mutableIntStateOf(0) }
    var usePopTransition by remember { mutableStateOf(false) }
    var trackMenuTrack by remember { mutableStateOf<Track?>(null) }
    var deleteMenuTrack by remember { mutableStateOf<Track?>(null) }
    var infoMenuTrack by remember { mutableStateOf<Track?>(null) }
    var metadataMenuTrack by remember { mutableStateOf<Track?>(null) }

    fun navigate(route: MainRoute) {
        usePopTransition = false
        navigator.navigate(route)
    }

    fun back() {
        val leavingRoute = navigator.currentRoute
        usePopTransition = true
        navigator.back()
        if (leavingRoute == MainRoute.Search) {
            searchStateVersion += 1
            detailStateHolder.removeState(leavingRoute.saveableStateKey(searchStateVersion - 1))
        }
    }

    BackHandler(enabled = navigator.canGoBack()) { back() }

    SharedTransitionLayout {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(if (fancyBackgroundEnabled) Color.Transparent else MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = currentRoute == MainRoute.Home,
                enter = fadeIn(animationSpec = tween(290)),
                exit = fadeOut(animationSpec = tween(290)),
                label = "home-host",
            ) {
                CompositionLocalProvider(LocalFancyTabContentBackground provides fancyBackgroundEnabled) {
                    homeStateHolder.SaveableStateProvider("home") {
                        HomeScreen(
                            library = library,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            currentTrackId = currentTrackId,
                            onRequestPermission = onRequestPermission,
                            onSettings = { navigate(MainRoute.Settings) },
                            onTrackClick = onTrackClick,
                            onToggleFavorite = onToggleFavorite,
                            onToggleFavoriteItem = onToggleFavoriteItem,
                            onFolderClick = { navigate(MainRoute.Folder(it.path)) },
                            onAlbumClick = { navigate(MainRoute.Album(it.id)) },
                            onArtistClick = { navigate(MainRoute.Artist(it.name)) },
                            onPlaylistClick = { navigate(MainRoute.Playlist(it.id)) },
                            onCreatePlaylist = { navigate(MainRoute.SelectPlaylistTracks(it)) },
                            onSearch = { navigate(MainRoute.Search) },
                            onReplaceCurrentQueue = onReplaceCurrentQueue,
                            onRenamePlaylist = onRenamePlaylist,
                            onDeletePlaylists = onDeletePlaylists,
                            onDeleteTracksPermanently = onDeleteTracksPermanently,
                            onAddTracksToRoute = onAddTracksToRoute,
                            onPlaylistEditModeChanged = onPlaylistEditModeChanged,
                            onTrackMenu = { trackMenuTrack = it },
                            initialTabIndex = initialTabIndex,
                            onTabSelected = onTabSelected,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = currentRoute,
                transitionSpec = {
                    if (usePopTransition) {
                        modernPopEnter() togetherWith modernPopExit()
                    } else {
                        modernEnter() togetherWith modernExit()
                    }.using(SizeTransform(clip = false))
                },
                label = "detail-nav",
            ) { route ->
                detailStateHolder.SaveableStateProvider(route.saveableStateKey(searchStateVersion)) {
                    CompositionLocalProvider(
                        LocalFancyTabContentBackground provides (fancyBackgroundEnabled && route != MainRoute.Settings),
                    ) {
                        DetailHost(
                            route = route,
                            library = library,
                            currentQueue = currentQueue,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            currentTrackId = currentTrackId,
                            onBack = { back() },
                            onSettings = { navigate(MainRoute.Settings) },
                            onSearch = { navigate(MainRoute.Search) },
                            onTrackClick = onTrackClick,
                            onToggleFavorite = onToggleFavorite,
                            onToggleFavoriteItem = onToggleFavoriteItem,
                            onRescan = onRescan,
                            onHiddenFoldersChanged = onHiddenFoldersChanged,
                            onImportPlaylist = onImportPlaylist,
                            onExportPlaylists = onExportPlaylists,
                            onShowAllTracks = { navigate(MainRoute.SearchTracks(it)) },
                            onShowAllArtists = { navigate(MainRoute.SearchArtists(it)) },
                            onShowAllAlbums = { navigate(MainRoute.SearchAlbums(it)) },
                            onAlbumClick = { navigate(MainRoute.Album(it.id)) },
                            onArtistClick = { navigate(MainRoute.Artist(it.name)) },
                            onAddTracksToPlaylistRoute = { navigate(MainRoute.AddTracksToPlaylist(it)) },
                            onCreatePlaylistRoute = { navigate(MainRoute.SelectPlaylistTracks(it)) },
                            onCreatePlaylist = onCreatePlaylist,
                            onAddTracksToPlaylist = onAddTracksToPlaylist,
                            onAddTracksToCurrentQueue = onAddTracksToCurrentQueue,
                            onReplaceCurrentQueue = onReplaceCurrentQueue,
                            onReplacePlaylistTracks = onReplacePlaylistTracks,
                            onRenamePlaylist = onRenamePlaylist,
                            onDeletePlaylists = onDeletePlaylists,
                            onDeleteTracksPermanently = onDeleteTracksPermanently,
                            onAddTracksToRoute = onAddTracksToRoute,
                            onAddTracksToCurrentQueueRoute = onAddTracksToCurrentQueueRoute,
                            onCurrentQueueTracksAdded = onCurrentQueueTracksAdded,
                            onPlaylistEditModeChanged = onPlaylistEditModeChanged,
                            onTrackMenu = { trackMenuTrack = it },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
            trackMenuTrack?.let { track ->
                TrackActionSheet(
                    track = track,
                    onDismiss = { trackMenuTrack = null },
                    onAdd = {
                        trackMenuTrack = null
                        onAddTracksToRoute(listOf(track))
                    },
                    onDelete = {
                        trackMenuTrack = null
                        deleteMenuTrack = track
                    },
                    onShare = {
                        trackMenuTrack = null
                        shareTracks(context, listOf(track))
                    },
                    onTrackInfo = {
                        trackMenuTrack = null
                        infoMenuTrack = track
                    },
                    onEditMetadata = {
                        trackMenuTrack = null
                        metadataMenuTrack = track
                    },
                    onAlbum = {
                        trackMenuTrack = null
                        library.albumsById[track.albumGroupId]?.let { navigate(MainRoute.Album(it.id)) }
                    },
                    onArtist = {
                        trackMenuTrack = null
                        library.artistsByName[track.artist]?.let { navigate(MainRoute.Artist(it.name)) }
                    },
                )
            }
            deleteMenuTrack?.let { track ->
                ConfirmPermanentDeleteDialog(
                    onConfirm = {
                        deleteMenuTrack = null
                        onDeleteTracksPermanently(listOf(track))
                    },
                    onDismiss = { deleteMenuTrack = null },
                )
            }
            infoMenuTrack?.let { track ->
                TrackInfoDialog(
                    track = track,
                    onDismiss = { infoMenuTrack = null },
                )
            }
            metadataMenuTrack?.let { track ->
                TrackMetadataEditorDialog(
                    track = track,
                    onDismiss = { metadataMenuTrack = null },
                    onSaved = {
                        metadataMenuTrack = null
                        onRescan()
                    },
                )
            }
        }
    }
}

@Composable
private fun DetailHost(
    route: MainRoute,
    library: LibraryState,
    currentQueue: List<Track>,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    currentTrackId: Long?,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onSearch: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onToggleFavoriteItem: (String, String) -> Unit,
    onRescan: () -> Unit,
    onHiddenFoldersChanged: (Set<String>) -> Unit,
    onImportPlaylist: () -> Unit,
    onExportPlaylists: () -> Unit,
    onShowAllTracks: (String) -> Unit,
    onShowAllArtists: (String) -> Unit,
    onShowAllAlbums: (String) -> Unit,
    onAlbumClick: (AlbumGroup) -> Unit,
    onArtistClick: (ArtistGroup) -> Unit,
    onAddTracksToPlaylistRoute: (String) -> Unit,
    onCreatePlaylistRoute: (String) -> Unit,
    onCreatePlaylist: (String, List<Track>) -> Unit,
    onAddTracksToPlaylist: (String, List<Track>) -> Unit,
    onAddTracksToCurrentQueue: (List<Track>) -> Unit,
    onReplaceCurrentQueue: (List<Track>) -> Unit,
    onReplacePlaylistTracks: (String, List<Track>) -> Unit,
    onRenamePlaylist: (String, String) -> Unit,
    onDeletePlaylists: (List<String>) -> Unit,
    onDeleteTracksPermanently: (List<Track>) -> Unit,
    onAddTracksToRoute: (List<Track>) -> Unit,
    onAddTracksToCurrentQueueRoute: () -> Unit,
    onCurrentQueueTracksAdded: () -> Unit,
    onPlaylistEditModeChanged: (Boolean) -> Unit,
    onTrackMenu: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    val smartPlaylists =
        remember(library.tracks, library.favoriteItems, library.trackStats) {
            library.smartPlaylists()
        }
    when (route) {
        MainRoute.Home -> {
            Box(modifier = modifier)
        }

        MainRoute.Search -> {
            SearchScreen(
                library = library,
                onBack = onBack,
                onTrackClick = onTrackClick,
                currentTrackId = currentTrackId,
                onTrackMenu = onTrackMenu,
                onShowAllTracks = onShowAllTracks,
                onShowAllArtists = onShowAllArtists,
                onShowAllAlbums = onShowAllAlbums,
                onArtistClick = onArtistClick,
                onAlbumClick = onAlbumClick,
                modifier = modifier,
            )
        }

        is MainRoute.SearchTracks -> {
            SearchTrackResultsScreen(
                query = route.query,
                library = library,
                onBack = onBack,
                onTrackClick = onTrackClick,
                currentTrackId = currentTrackId,
                onTrackMenu = onTrackMenu,
                modifier = modifier,
            )
        }

        is MainRoute.SearchArtists -> {
            SearchArtistResultsScreen(
                query = route.query,
                library = library,
                onBack = onBack,
                onArtistClick = onArtistClick,
                modifier = modifier,
            )
        }

        is MainRoute.SearchAlbums -> {
            SearchAlbumResultsScreen(
                query = route.query,
                library = library,
                onBack = onBack,
                onAlbumClick = onAlbumClick,
                modifier = modifier,
            )
        }

        MainRoute.Settings -> {
            SettingsScreen(
                onBack = onBack,
                library = library,
                onRescan = onRescan,
                onHiddenFoldersChanged = onHiddenFoldersChanged,
                onImportPlaylist = onImportPlaylist,
                onExportPlaylists = onExportPlaylists,
                modifier = modifier,
            )
        }

        is MainRoute.Album -> {
            val album = library.albumsById[route.id]
            if (album == null) {
                Box(modifier = modifier)
            } else {
                AlbumDetailScreen(
                    album = album,
                    onBack = onBack,
                    onSettings = onSettings,
                    onSearch = onSearch,
                    onTrackClick = onTrackClick,
                    trackStats = library.trackStats,
                    currentTrackId = currentTrackId,
                    onTrackMenu = onTrackMenu,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    isFavorite = library.isFavoriteItem(FavoriteType.Album, album.id.toString()),
                    onToggleFavorite = { onToggleFavoriteItem(FavoriteType.Album, album.id.toString()) },
                    modifier = modifier,
                )
            }
        }

        is MainRoute.Artist -> {
            val artist = library.artistsByName[route.name]
            if (artist == null) {
                Box(modifier = modifier)
            } else {
                ArtistDetailScreen(
                    artist = artist,
                    albums =
                        artist.tracks
                            .toAlbumGroups(),
                    onBack = onBack,
                    onSettings = onSettings,
                    onSearch = onSearch,
                    onTrackClick = onTrackClick,
                    trackStats = library.trackStats,
                    currentTrackId = currentTrackId,
                    onTrackMenu = onTrackMenu,
                    onAlbumClick = onAlbumClick,
                    isFavorite = library.isFavoriteItem(FavoriteType.Artist, artist.name),
                    onToggleFavorite = { onToggleFavoriteItem(FavoriteType.Artist, artist.name) },
                    modifier = modifier,
                )
            }
        }

        is MainRoute.Folder -> {
            val folder = library.foldersByPath[route.path]
            if (folder == null) {
                Box(modifier = modifier)
            } else {
                FolderDetailScreen(
                    folder = folder,
                    favorites = library.favorites,
                    onBack = onBack,
                    onSettings = onSettings,
                    onSearch = onSearch,
                    onTrackClick = onTrackClick,
                    trackStats = library.trackStats,
                    currentTrackId = currentTrackId,
                    onTrackMenu = onTrackMenu,
                    onToggleFavorite = onToggleFavorite,
                    isFavorite = library.isFavoriteItem(FavoriteType.Folder, folder.path),
                    onToggleFolderFavorite = { onToggleFavoriteItem(FavoriteType.Folder, folder.path) },
                    modifier = modifier,
                )
            }
        }

        is MainRoute.Playlist -> {
            val playlist = library.findPlaylist(route.id, smartPlaylists)
            if (playlist == null) {
                Box(modifier = modifier)
            } else {
                PlaylistDetailScreen(
                    playlist = playlist,
                    onBack = onBack,
                    onTrackClick = onTrackClick,
                    trackStats = library.trackStats,
                    currentTrackId = currentTrackId,
                    onTrackMenu = onTrackMenu,
                    isFavorite = library.isFavoriteItem(FavoriteType.Playlist, playlist.id),
                    onToggleFavorite = { onToggleFavoriteItem(FavoriteType.Playlist, playlist.id) },
                    onAddTracks = { onAddTracksToPlaylistRoute(playlist.id) },
                    onEditModeChanged = onPlaylistEditModeChanged,
                    onReplaceCurrentQueue = onReplaceCurrentQueue,
                    onReplacePlaylistTracks = { tracks -> onReplacePlaylistTracks(playlist.id, tracks) },
                    onRenamePlaylist = { name -> onRenamePlaylist(playlist.id, name) },
                    onSettings = onSettings,
                    onAddTracksToRoute = onAddTracksToRoute,
                    modifier = modifier,
                )
            }
        }

        is MainRoute.SelectPlaylistTracks -> {
            SelectTrackScreen(
                title = route.name,
                tracks = library.tracks,
                onBack = onBack,
                onDone = { selected ->
                    onCreatePlaylist(route.name, selected)
                    onBack()
                },
                modifier = modifier,
            )
        }

        is MainRoute.AddTracksToPlaylist -> {
            val playlist = library.findPlaylist(route.id, smartPlaylists)
            SelectTrackScreen(
                title = playlist?.title ?: stringResource(R.string.add_tracks),
                tracks = library.tracks,
                onBack = onBack,
                onDone = { selected ->
                    onAddTracksToPlaylist(route.id, selected)
                    onBack()
                },
                modifier = modifier,
            )
        }

        MainRoute.AddTracksToCurrentQueue -> {
            SelectTrackScreen(
                title = stringResource(R.string.add_tracks),
                tracks = library.tracks,
                onBack = onBack,
                onDone = { selected ->
                    onAddTracksToCurrentQueue(selected)
                    onBack()
                    onCurrentQueueTracksAdded()
                },
                modifier = modifier,
            )
        }

        is MainRoute.AddToPlaylist -> {
            val track = library.tracksById[route.trackId]
            AddToPlaylistScreen(
                tracks = listOfNotNull(track),
                playlists = library.playlists,
                currentQueue = currentQueue,
                isFavorite = track != null && track.id in library.favorites,
                onBack = onBack,
                onCreatePlaylist = { name ->
                    track?.let { onCreatePlaylist(name, listOf(it)) }
                    onBack()
                },
                onAddToCurrent = {
                    track?.let { onAddTracksToCurrentQueue(listOf(it)) }
                    onBack()
                },
                onToggleFavorite = {
                    track?.id?.let(onToggleFavorite)
                    onBack()
                },
                onAdd = { playlist ->
                    track?.let { onAddTracksToPlaylist(playlist.id, listOf(it)) }
                },
                modifier = modifier,
            )
        }

        is MainRoute.AddToTracks -> {
            val selectedTracks = route.trackIds.mapNotNull { id -> library.tracksById[id] }
            AddToPlaylistScreen(
                tracks = selectedTracks,
                playlists = library.playlists,
                currentQueue = currentQueue,
                isFavorite = selectedTracks.size == 1 && selectedTracks.first().id in library.favorites,
                onBack = onBack,
                onCreatePlaylist = { name ->
                    onCreatePlaylist(name, selectedTracks)
                    onBack()
                },
                onAddToCurrent = {
                    onAddTracksToCurrentQueue(selectedTracks)
                    onBack()
                },
                onToggleFavorite = {
                    selectedTracks.singleOrNull()?.id?.let(onToggleFavorite)
                    onBack()
                },
                onAdd = { playlist ->
                    onAddTracksToPlaylist(playlist.id, selectedTracks)
                },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun HomeScreen(
    library: LibraryState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    currentTrackId: Long?,
    onRequestPermission: () -> Unit,
    onSettings: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onToggleFavoriteItem: (String, String) -> Unit,
    onFolderClick: (FolderGroup) -> Unit,
    onAlbumClick: (AlbumGroup) -> Unit,
    onArtistClick: (ArtistGroup) -> Unit,
    onPlaylistClick: (PlaylistGroup) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onSearch: () -> Unit,
    onReplaceCurrentQueue: (List<Track>) -> Unit,
    onRenamePlaylist: (String, String) -> Unit,
    onDeletePlaylists: (List<String>) -> Unit,
    onDeleteTracksPermanently: (List<Track>) -> Unit,
    onAddTracksToRoute: (List<Track>) -> Unit,
    onPlaylistEditModeChanged: (Boolean) -> Unit,
    onTrackMenu: (Track) -> Unit,
    initialTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val tabs = HomeTab.entries
    val restoredTab = initialTabIndex.coerceIn(tabs.indices)
    val pagerState = rememberPagerState(initialPage = restoredTab) { tabs.size }
    val coroutineScope = rememberCoroutineScope()
    val tabListState = rememberLazyListState(initialFirstVisibleItemIndex = restoredTab)
    val density = LocalDensity.current
    val tabWidthPx = with(density) { HOME_TAB_WIDTH.roundToPx() }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var playlistEditMode by rememberSaveable { mutableStateOf(false) }
    var contentEditMode by rememberSaveable { mutableStateOf(false) }
    var selectedPlaylistIds by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var contentEditSelectedCount by rememberSaveable { mutableIntStateOf(0) }
    var contentEditAllSelected by rememberSaveable { mutableStateOf(false) }
    var contentEditSelectAllRequest by rememberSaveable { mutableIntStateOf(0) }
    var contentEditStartRequest by rememberSaveable { mutableIntStateOf(0) }
    var contentEditStartTab by rememberSaveable { mutableStateOf<HomeTab?>(null) }
    val customPlaylists =
        remember(library.playlists) {
            library.playlists.filter { it.id.startsWith(CUSTOM_PLAYLIST_PREFIX) }
        }
    val isPlaylistTab = pagerState.currentPage == HomeTab.Playlist.ordinal

    LaunchedEffect(pagerState.currentPage) {
        onTabSelected(pagerState.currentPage)
    }

    val anyEditMode = playlistEditMode || contentEditMode
    val editSelectedCount = if (playlistEditMode) selectedPlaylistIds.size else contentEditSelectedCount
    val editAllSelected =
        if (playlistEditMode) {
            customPlaylists.isNotEmpty() && selectedPlaylistIds.size == customPlaylists.size
        } else {
            contentEditAllSelected
        }

    LaunchedEffect(anyEditMode) {
        onPlaylistEditModeChanged(anyEditMode)
        if (!playlistEditMode) selectedPlaylistIds = emptyList()
        if (!contentEditMode) {
            contentEditSelectedCount = 0
            contentEditAllSelected = false
        } else {
            contentEditStartTab = null
        }
    }

    LaunchedEffect(isPlaylistTab) {
        if (!isPlaylistTab && playlistEditMode) {
            playlistEditMode = false
        }
    }

    BackHandler(enabled = playlistEditMode) {
        playlistEditMode = false
    }

    LaunchedEffect(pagerState, tabListState, tabWidthPx) {
        snapshotFlow { pagerState.currentPage + pagerState.currentPageOffsetFraction }
            .collect { rawPosition ->
                if (!tabListState.isScrollInProgress) {
                    val tabPosition = rawPosition.coerceIn(0f, tabs.lastIndex.toFloat())
                    val itemIndex = floor(tabPosition).toInt().coerceIn(0, tabs.lastIndex)
                    val itemOffset = ((tabPosition - itemIndex) * tabWidthPx).roundToInt()
                    tabListState.scrollToItem(itemIndex, itemOffset)
                }
            }
    }

    LaunchedEffect(tabListState, pagerState) {
        var lastCenteredTab = restoredTab
        snapshotFlow { tabListState.centeredTabIndex(tabs.size) }
            .collect { centeredTab ->
                if (tabListState.isScrollInProgress && centeredTab != lastCenteredTab) {
                    lastCenteredTab = centeredTab
                    onTabSelected(centeredTab)
                    pagerState.scrollToPage(centeredTab)
                }
            }
    }

    Column(modifier = modifier) {
        AnimatedContent(
            targetState = anyEditMode,
            transitionSpec = { fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(140)) },
            label = "home-top-bar",
        ) { editing ->
            if (editing) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 18.dp, end = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = {
                            if (playlistEditMode) {
                                selectedPlaylistIds =
                                    if (editAllSelected) {
                                        emptyList()
                                    } else {
                                        customPlaylists.map { it.id }
                                    }
                            } else {
                                contentEditSelectAllRequest += 1
                            }
                        },
                    ) {
                        Icon(
                            if (editAllSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                    Text(
                        text =
                            if (editSelectedCount > 0) {
                                stringResource(R.string.selected_count_plain, editSelectedCount)
                            } else {
                                stringResource(if (playlistEditMode) R.string.select_playlist else R.string.select_track)
                            },
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, top = 18.dp, end = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onSearch) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = stringResource(R.string.search),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.menu),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            if (isPlaylistTab) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.edit)) },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        if (customPlaylists.isEmpty()) {
                                            Toast
                                                .makeText(context, R.string.no_custom_playlists_found, Toast.LENGTH_SHORT)
                                                .show()
                                        } else {
                                            playlistEditMode = true
                                        }
                                    },
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.edit)) },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        contentEditStartTab = tabs[pagerState.currentPage]
                                        contentEditStartRequest += 1
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
            }
        }

        CenteredHomeTabs(
            tabs = tabs,
            pagerState = pagerState,
            listState = tabListState,
            enabled = !anyEditMode,
            onTabClick = { index ->
                onTabSelected(index)
                coroutineScope.launch { pagerState.animateScrollToPage(index) }
            },
        )

        when {
            !library.permissionGranted && library.tracks.isEmpty() -> {
                PermissionState(onRequestPermission)
            }

            library.loading -> {
                LoadingState()
            }

            library.error != null -> {
                EmptyPanel(library.error)
            }

            else -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = !anyEditMode,
                ) { page ->
                    val pageTab = tabs[page]
                    val pageEditStartRequest =
                        if (contentEditStartTab == pageTab && pagerState.currentPage == page) {
                            contentEditStartRequest
                        } else {
                            0
                        }
                    when (pageTab) {
                        HomeTab.Favorite -> {
                            FavoriteTab(
                                library = library,
                                onTrackClick = onTrackClick,
                                currentTrackId = currentTrackId,
                                onTrackMenu = onTrackMenu,
                                onArtistClick = onArtistClick,
                                onAlbumClick = onAlbumClick,
                                onFolderClick = onFolderClick,
                                onPlaylistClick = onPlaylistClick,
                                onReplaceCurrentQueue = onReplaceCurrentQueue,
                                onAddTracksToRoute = onAddTracksToRoute,
                                onToggleFavoriteItem = onToggleFavoriteItem,
                                onEditModeChanged = { contentEditMode = it },
                                editStartRequest = pageEditStartRequest,
                                selectAllRequest = contentEditSelectAllRequest,
                                onEditSelectionChanged = { count, allSelected ->
                                    contentEditSelectedCount = count
                                    contentEditAllSelected = allSelected
                                },
                            )
                        }

                        HomeTab.Playlist -> {
                            PlaylistTab(
                                library = library,
                                onPlaylistClick = onPlaylistClick,
                                onCreatePlaylist = onCreatePlaylist,
                                editMode = playlistEditMode,
                                selectedPlaylistIds = selectedPlaylistIds,
                                onTogglePlaylistSelection = { playlistId ->
                                    selectedPlaylistIds =
                                        if (playlistId in selectedPlaylistIds) {
                                            selectedPlaylistIds - playlistId
                                        } else {
                                            selectedPlaylistIds + playlistId
                                        }
                                },
                                onEnterEditMode = { playlistEditMode = true },
                                onExitEditMode = { playlistEditMode = false },
                                onReplaceCurrentQueue = onReplaceCurrentQueue,
                                onRenamePlaylist = onRenamePlaylist,
                                onDeletePlaylists = onDeletePlaylists,
                                onAddTracksToRoute = onAddTracksToRoute,
                            )
                        }

                        HomeTab.Track -> {
                            TrackTab(
                                library = library,
                                onTrackClick = onTrackClick,
                                currentTrackId = currentTrackId,
                                onTrackMenu = onTrackMenu,
                                onReplaceCurrentQueue = onReplaceCurrentQueue,
                                onAddTracksToRoute = onAddTracksToRoute,
                                onDeleteTracksPermanently = onDeleteTracksPermanently,
                                onEditModeChanged = { contentEditMode = it },
                                editStartRequest = pageEditStartRequest,
                                selectAllRequest = contentEditSelectAllRequest,
                                onEditSelectionChanged = { count, allSelected ->
                                    contentEditSelectedCount = count
                                    contentEditAllSelected = allSelected
                                },
                            )
                        }

                        HomeTab.Album -> {
                            AlbumTab(
                                library = library,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                onAlbumClick = onAlbumClick,
                                currentTrackId = currentTrackId,
                                onTrackMenu = onTrackMenu,
                                onReplaceCurrentQueue = onReplaceCurrentQueue,
                                onAddTracksToRoute = onAddTracksToRoute,
                                onDeleteTracksPermanently = onDeleteTracksPermanently,
                                onEditModeChanged = { contentEditMode = it },
                                editStartRequest = pageEditStartRequest,
                                selectAllRequest = contentEditSelectAllRequest,
                                onEditSelectionChanged = { count, allSelected ->
                                    contentEditSelectedCount = count
                                    contentEditAllSelected = allSelected
                                },
                            )
                        }

                        HomeTab.Artist -> {
                            ArtistTab(
                                library = library,
                                onArtistClick = onArtistClick,
                                currentTrackId = currentTrackId,
                                onTrackMenu = onTrackMenu,
                                onReplaceCurrentQueue = onReplaceCurrentQueue,
                                onAddTracksToRoute = onAddTracksToRoute,
                                onDeleteTracksPermanently = onDeleteTracksPermanently,
                                onEditModeChanged = { contentEditMode = it },
                                editStartRequest = pageEditStartRequest,
                                selectAllRequest = contentEditSelectAllRequest,
                                onEditSelectionChanged = { count, allSelected ->
                                    contentEditSelectedCount = count
                                    contentEditAllSelected = allSelected
                                },
                            )
                        }

                        HomeTab.Folder -> {
                            FolderTab(
                                library = library,
                                onFolderClick = onFolderClick,
                                onTrackClick = onTrackClick,
                                currentTrackId = currentTrackId,
                                onTrackMenu = onTrackMenu,
                                onReplaceCurrentQueue = onReplaceCurrentQueue,
                                onAddTracksToRoute = onAddTracksToRoute,
                                onDeleteTracksPermanently = onDeleteTracksPermanently,
                                onEditModeChanged = { contentEditMode = it },
                                editStartRequest = pageEditStartRequest,
                                selectAllRequest = contentEditSelectAllRequest,
                                onEditSelectionChanged = { count, allSelected ->
                                    contentEditSelectedCount = count
                                    contentEditAllSelected = allSelected
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
private fun CenteredHomeTabs(
    tabs: List<HomeTab>,
    pagerState: PagerState,
    listState: LazyListState,
    enabled: Boolean = true,
    onTabClick: (Int) -> Unit,
) {
    val pagerPosition = pagerState.currentPage + pagerState.currentPageOffsetFraction
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val horizontalPadding = ((maxWidth - HOME_TAB_WIDTH) / 2).coerceAtLeast(0.dp)
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            userScrollEnabled = enabled,
        ) {
            itemsIndexed(tabs, key = { _, tab -> tab.name }) { index, tab ->
                val progress = (1f - abs(index - pagerPosition)).coerceIn(0f, 1f)
                val scale = 0.8f + (0.5f * progress)
                val color =
                    lerp(
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        MaterialTheme.colorScheme.primary,
                        progress,
                    )
                Box(
                    modifier =
                        Modifier
                            .width(HOME_TAB_WIDTH)
                            .height(52.dp)
                            .clickable(enabled = enabled) { onTabClick(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(tab.labelRes),
                        color = color,
                        fontSize = 18.sp,
                        fontWeight = if (progress >= 0.5f) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier.graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchScreen(
    library: LibraryState,
    onBack: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    currentTrackId: Long?,
    onTrackMenu: (Track) -> Unit,
    onShowAllTracks: (String) -> Unit,
    onShowAllArtists: (String) -> Unit,
    onShowAllAlbums: (String) -> Unit,
    onArtistClick: (ArtistGroup) -> Unit,
    onAlbumClick: (AlbumGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val normalizedQuery = query.trim()
    val trackResults =
        if (normalizedQuery.isBlank()) {
            emptyList()
        } else {
            library.tracks.filter {
                it.title.contains(normalizedQuery, ignoreCase = true) ||
                    it.artist.contains(normalizedQuery, ignoreCase = true) ||
                    it.album.contains(normalizedQuery, ignoreCase = true)
            }
        }
    val artistResults =
        if (normalizedQuery.isBlank()) {
            emptyList()
        } else {
            library.artists.filter { it.name.contains(normalizedQuery, ignoreCase = true) }
        }
    val albumResults =
        if (normalizedQuery.isBlank()) {
            emptyList()
        } else {
            library.albums.filter {
                it.title.contains(normalizedQuery, ignoreCase = true) ||
                    it.artist.contains(normalizedQuery, ignoreCase = true)
            }
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(appBackgroundContainerColor()),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 18.dp, end = 8.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.primary)
            }
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle =
                    MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    ),
                placeholder = {
                    Text(stringResource(R.string.search), color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
            )
            IconButton(onClick = { if (query.isBlank()) onBack() else query = "" }) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = MaterialTheme.colorScheme.primary)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (normalizedQuery.isBlank()) {
                item { EmptyInline(stringResource(R.string.search_hint)) }
            } else {
                item { SectionTitle("Track (${trackResults.size})") }
                if (trackResults.isNotEmpty()) {
                    item {
                        SearchPanel {
                            trackResults.take(4).forEachIndexed { index, track ->
                                TrackRow(
                                    track = track,
                                    onClick = { onTrackClick(track, trackResults) },
                                    currentTrackId = currentTrackId,
                                    onMenuClick = { onTrackMenu(track) },
                                )
                                if (index != trackResults.take(4).lastIndex) SearchDivider()
                            }
                            if (trackResults.size > 4) {
                                SearchDivider()
                                Text(
                                    stringResource(R.string.show_all),
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable { onShowAllTracks(normalizedQuery) }
                                            .padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                } else {
                    item { SearchEmpty(stringResource(R.string.no_tracks_found)) }
                }

                item { SectionTitle("Artist (${artistResults.size})") }
                if (artistResults.isNotEmpty()) {
                    item {
                        SearchPanel {
                            artistResults.take(4).forEachIndexed { index, artist ->
                                MediaGroupRow(
                                    artwork = artist.tracks.firstOrNull()?.albumArtUri,
                                    title = artist.name,
                                    subtitle = "${artist.albums} albums ${artist.tracks.size} tracks",
                                    onClick = { onArtistClick(artist) },
                                )
                                if (index != artistResults.take(4).lastIndex) SearchDivider()
                            }
                            if (artistResults.size > 4) {
                                SearchDivider()
                                Text(
                                    stringResource(R.string.show_all),
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable { onShowAllArtists(normalizedQuery) }
                                            .padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                } else {
                    item { SearchEmpty(stringResource(R.string.no_artists_found)) }
                }

                item { SectionTitle("Album (${albumResults.size})") }
                if (albumResults.isNotEmpty()) {
                    item {
                        SearchPanel {
                            albumResults.take(4).forEachIndexed { index, album ->
                                MediaGroupRow(
                                    artwork = album.tracks.firstOrNull()?.albumArtUri,
                                    title = album.title,
                                    subtitle = album.artist,
                                    onClick = { onAlbumClick(album) },
                                )
                                if (index != albumResults.take(4).lastIndex) SearchDivider()
                            }
                            if (albumResults.size > 4) {
                                SearchDivider()
                                Text(
                                    stringResource(R.string.show_all),
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable { onShowAllAlbums(normalizedQuery) }
                                            .padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                } else {
                    item { SearchEmpty(stringResource(R.string.no_albums_found)) }
                }
            }
        }
    }
}

@Composable
private fun SearchTrackResultsScreen(
    query: String,
    library: LibraryState,
    onBack: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    currentTrackId: Long?,
    onTrackMenu: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    val normalizedQuery = query.trim()
    val trackResults =
        remember(normalizedQuery, library.tracks) {
            if (normalizedQuery.isBlank()) {
                emptyList()
            } else {
                library.tracks.filter {
                    it.title.contains(normalizedQuery, ignoreCase = true) ||
                        it.artist.contains(normalizedQuery, ignoreCase = true) ||
                        it.album.contains(normalizedQuery, ignoreCase = true)
                }
            }
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(appBackgroundContainerColor()),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 18.dp, end = 12.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.search_results),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "$normalizedQuery | ${trackResults.size}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        RoundedPanelList(listState = rememberLazyListState(), topPadding = 0.dp) {
            item {
                DetailSortHeader(
                    label = stringResource(R.string.tracks_count, trackResults.size),
                    onPlay = {
                        trackResults.firstOrNull()?.let {
                            onTrackClick(it, trackResults)
                        }
                    },
                    onShuffle = {
                        val shuffled = trackResults.smartAntiRepeatShuffle(library.trackStats)
                        shuffled.firstOrNull()?.let {
                            onTrackClick(it, shuffled)
                        }
                    },
                )
            }
            items(trackResults, key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    onClick = { onTrackClick(track, trackResults) },
                    currentTrackId = currentTrackId,
                    onMenuClick = { onTrackMenu(track) },
                    modifier = Modifier.animateItem(),
                )
            }
            if (trackResults.isEmpty()) {
                item { EmptyInline(stringResource(R.string.no_tracks_found)) }
            }
        }
    }
}

@Composable
private fun SearchArtistResultsScreen(
    query: String,
    library: LibraryState,
    onBack: () -> Unit,
    onArtistClick: (ArtistGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    val normalizedQuery = query.trim()
    val artistResults =
        remember(normalizedQuery, library.artists) {
            if (normalizedQuery.isBlank()) {
                emptyList()
            } else {
                library.artists.filter { it.name.contains(normalizedQuery, ignoreCase = true) }
            }
        }

    SearchGroupResultsScreen(
        title = stringResource(R.string.search_results),
        subtitle = "$normalizedQuery | ${artistResults.size}",
        onBack = onBack,
        modifier = modifier,
    ) {
        item { SortHeader(stringResource(R.string.artists_count, artistResults.size)) }
        itemsIndexed(artistResults, key = { _, artist -> artist.name }) { index, artist ->
            MediaGroupRow(
                artwork = artist.tracks.firstOrNull()?.albumArtUri,
                title = artist.name,
                subtitle = "${artist.albums} albums ${artist.tracks.size} tracks",
                onClick = { onArtistClick(artist) },
                modifier = Modifier.animateItem(),
            )
            if (index != artistResults.lastIndex) SearchDivider()
        }
        if (artistResults.isEmpty()) {
            item { EmptyInline(stringResource(R.string.no_artists_found)) }
        }
    }
}

@Composable
private fun SearchAlbumResultsScreen(
    query: String,
    library: LibraryState,
    onBack: () -> Unit,
    onAlbumClick: (AlbumGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    val normalizedQuery = query.trim()
    val albumResults =
        remember(normalizedQuery, library.albums) {
            if (normalizedQuery.isBlank()) {
                emptyList()
            } else {
                library.albums.filter {
                    it.title.contains(normalizedQuery, ignoreCase = true) ||
                        it.artist.contains(normalizedQuery, ignoreCase = true)
                }
            }
        }

    SearchGroupResultsScreen(
        title = stringResource(R.string.search_results),
        subtitle = "$normalizedQuery | ${albumResults.size}",
        onBack = onBack,
        modifier = modifier,
    ) {
        item { SortHeader(stringResource(R.string.albums_count, albumResults.size)) }
        itemsIndexed(albumResults, key = { _, album -> album.id }) { index, album ->
            MediaGroupRow(
                artwork = album.tracks.firstOrNull()?.albumArtUri,
                title = album.title,
                subtitle = album.artist,
                onClick = { onAlbumClick(album) },
                modifier = Modifier.animateItem(),
            )
            if (index != albumResults.lastIndex) SearchDivider()
        }
        if (albumResults.isEmpty()) {
            item { EmptyInline(stringResource(R.string.no_albums_found)) }
        }
    }
}

@Composable
private fun SearchGroupResultsScreen(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(appBackgroundContainerColor()),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 18.dp, end = 12.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        RoundedPanelList(listState = rememberLazyListState(), topPadding = 0.dp, content = content)
    }
}

@Composable
private fun PermissionState(onRequestPermission: () -> Unit) {
    OutlinedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = tabContentContainerColor()),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Default.LibraryMusic,
                contentDescription = null,
                modifier = Modifier.size(38.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(stringResource(R.string.allow_music_access), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.allow_music_access_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onRequestPermission) {
                Text(stringResource(R.string.grant_permission))
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Text(stringResource(R.string.scanning), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun tabContentContainerColor(): Color =
    MaterialTheme.colorScheme.surfaceContainer.copy(
        alpha = if (LocalFancyTabContentBackground.current) FANCY_TAB_CONTENT_ALPHA else 1f,
    )

@Composable
private fun appBackgroundContainerColor(): Color =
    MaterialTheme.colorScheme.background.copy(
        alpha = if (LocalFancyTabContentBackground.current) FANCY_TAB_CONTENT_ALPHA_TRANSPARENT else 1f,
    )

@Composable
private fun rememberHomeLazyListState(): LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

@Composable
private fun rememberHomeLazyGridState(): LazyGridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() }

@Composable
private fun FavoriteTab(
    library: LibraryState,
    onTrackClick: (Track, List<Track>) -> Unit,
    currentTrackId: Long?,
    onTrackMenu: (Track) -> Unit,
    onArtistClick: (ArtistGroup) -> Unit,
    onAlbumClick: (AlbumGroup) -> Unit,
    onFolderClick: (FolderGroup) -> Unit,
    onPlaylistClick: (PlaylistGroup) -> Unit,
    onReplaceCurrentQueue: (List<Track>) -> Unit,
    onAddTracksToRoute: (List<Track>) -> Unit,
    onToggleFavoriteItem: (String, String) -> Unit,
    onEditModeChanged: (Boolean) -> Unit,
    editStartRequest: Int,
    selectAllRequest: Int,
    onEditSelectionChanged: (Int, Boolean) -> Unit,
) {
    val context = LocalContext.current
    val gridState = rememberHomeLazyGridState()
    var editMode by rememberSaveable { mutableStateOf(false) }
    var selectedKeys by rememberSaveable { mutableStateOf(emptyList<String>()) }
    val favoriteCards =
        remember(library.favoriteItems, library.favoriteTracks, library.artists, library.albums, library.folders, library.playlists) {
            library.favoriteCards()
        }
    val selectedCards =
        remember(favoriteCards, selectedKeys) {
            val selected = selectedKeys.toSet()
            favoriteCards.filter { it.selectionKey in selected }
        }
    val selectedTracks = remember(selectedCards) { selectedCards.flatMap { it.tracks }.distinctBy { it.id } }
    val canDeleteFavorites = selectedCards.any { it.type != FavoriteType.Track }
    LaunchedEffect(editMode) {
        onEditModeChanged(editMode)
        if (!editMode) selectedKeys = emptyList()
    }
    LaunchedEffect(editStartRequest) {
        if (editStartRequest > 0 && favoriteCards.isNotEmpty()) editMode = true
    }
    LaunchedEffect(editMode, selectedKeys, favoriteCards) {
        onEditSelectionChanged(
            if (editMode) selectedKeys.size else 0,
            editMode && favoriteCards.isNotEmpty() && selectedKeys.size == favoriteCards.size,
        )
    }
    LaunchedEffect(selectAllRequest) {
        if (editMode && selectAllRequest > 0) {
            selectedKeys =
                if (selectedKeys.size == favoriteCards.size) {
                    emptyList()
                } else {
                    favoriteCards.map { it.selectionKey }
                }
        }
    }
    BackHandler(enabled = editMode) {
        editMode = false
    }
    if (favoriteCards.isEmpty()) {
        EmptyPanel(stringResource(R.string.favorite_empty))
        return
    }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 14.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(tabContentContainerColor()),
            contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = if (editMode) 128.dp else 116.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { SortHeader(stringResource(R.string.sort_favorite_date)) }
            items(favoriteCards, key = { it.selectionKey }) { card ->
                FavoriteGridCard(
                    card = card,
                    selectionVisible = editMode,
                    selected = card.selectionKey in selectedKeys,
                    modifier =
                        Modifier
                            .animateItem()
                            .combinedClickable(
                                onClick = {
                                    if (editMode) {
                                        selectedKeys =
                                            if (card.selectionKey in
                                                selectedKeys
                                            ) {
                                                selectedKeys - card.selectionKey
                                            } else {
                                                selectedKeys + card.selectionKey
                                            }
                                    } else {
                                        when (card.type) {
                                            FavoriteType.Track -> card.playlist?.let(onPlaylistClick)
                                            FavoriteType.Artist -> card.artist?.let(onArtistClick)
                                            FavoriteType.Folder -> card.folder?.let(onFolderClick)
                                            FavoriteType.Album -> card.album?.let(onAlbumClick)
                                            FavoriteType.Playlist -> card.playlist?.let(onPlaylistClick)
                                        }
                                    }
                                },
                                onLongClick = {
                                    editMode = true
                                    selectedKeys = listOf(card.selectionKey)
                                },
                            ),
                )
            }
        }
        AnimatedVisibility(
            visible = editMode,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(animationSpec = tween(220)) { it } + fadeIn(animationSpec = tween(180)),
            exit = slideOutVertically(animationSpec = tween(180)) { it } + fadeOut(animationSpec = tween(140)),
        ) {
            PlaylistEditBottomBar(
                hasSelection = selectedCards.isNotEmpty(),
                deleteEnabled = canDeleteFavorites,
                onPlay = {
                    if (selectedTracks.isNotEmpty()) {
                        onReplaceCurrentQueue(selectedTracks)
                        editMode = false
                    }
                },
                onAdd = {
                    if (selectedTracks.isNotEmpty()) {
                        onAddTracksToRoute(selectedTracks)
                        editMode = false
                    }
                },
                onShare = { shareTracks(context, selectedTracks) },
                onDelete = {
                    selectedCards.filterNot { it.type == FavoriteType.Track }.forEach {
                        onToggleFavoriteItem(it.type, it.key)
                    }
                    editMode = false
                },
            )
        }
    }
}

@Composable
private fun PlaylistTab(
    library: LibraryState,
    onPlaylistClick: (PlaylistGroup) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    editMode: Boolean,
    selectedPlaylistIds: List<String>,
    onTogglePlaylistSelection: (String) -> Unit,
    onEnterEditMode: () -> Unit,
    onExitEditMode: () -> Unit,
    onReplaceCurrentQueue: (List<Track>) -> Unit,
    onRenamePlaylist: (String, String) -> Unit,
    onDeletePlaylists: (List<String>) -> Unit,
    onAddTracksToRoute: (List<Track>) -> Unit,
) {
    val context = LocalContext.current
    val listState = rememberHomeLazyListState()
    var createDialog by rememberSaveable { mutableStateOf(false) }
    var playlistName by rememberSaveable { mutableStateOf("") }
    var renamePlaylist by remember { mutableStateOf<PlaylistGroup?>(null) }
    var renameName by rememberSaveable { mutableStateOf("") }
    val preferences = rememberSortPreferences()
    var listSort by rememberSaveable {
        mutableStateOf(preferences.loadEnumSort(SORT_PLAYLIST_LIST, PlaylistListSort.Name, PlaylistListSort.entries))
    }
    val smartPlaylists = remember(library.tracks, library.favoriteItems, library.trackStats) { library.smartPlaylists() }
    val customPlaylists =
        remember(library.playlists, library.trackStats, listSort) {
            library.playlists
                .filter { playlist -> playlist.id.startsWith(CUSTOM_PLAYLIST_PREFIX) }
                .sortedWith(library.customPlaylistComparator(listSort))
        }
    val selectedPlaylists =
        remember(customPlaylists, selectedPlaylistIds) {
            val selected = selectedPlaylistIds.toSet()
            customPlaylists.filter { it.id in selected }
        }
    val selectedTracks = remember(selectedPlaylists) { selectedPlaylists.flatMap { it.tracks }.distinctBy { it.id } }
    if (library.tracks.isEmpty()) {
        EmptyPanel(stringResource(R.string.playlist_empty))
        return
    }
    renamePlaylist?.let { playlist ->
        AlertDialog(
            onDismissRequest = {
                renamePlaylist = null
                renameName = ""
            },
            title = { Text(stringResource(R.string.rename)) },
            text = {
                OutlinedTextField(
                    value = renameName,
                    onValueChange = { renameName = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.playlist_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    enabled = renameName.isNotBlank(),
                    onClick = {
                        val name = renameName.trim()
                        if (name.isNotEmpty()) {
                            onRenamePlaylist(playlist.id, name)
                            renamePlaylist = null
                            renameName = ""
                        }
                    },
                ) { Text(stringResource(R.string.done)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        renamePlaylist = null
                        renameName = ""
                    },
                ) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
    if (createDialog) {
        AlertDialog(
            onDismissRequest = { createDialog = false },
            title = { Text(stringResource(R.string.create_playlist)) },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.playlist_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = playlistName.trim()
                        if (name.isNotEmpty()) {
                            createDialog = false
                            playlistName = ""
                            onCreatePlaylist(name)
                        }
                    },
                    enabled = playlistName.isNotBlank(),
                ) { Text(stringResource(R.string.next)) }
            },
            dismissButton = {
                TextButton(onClick = { createDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 14.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(tabContentContainerColor()),
            contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = if (editMode) 112.dp else 116.dp),
        ) {
            if (!editMode) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SortHeader(
                            label = stringResource(listSort.labelRes),
                            modifier = Modifier.weight(1f),
                            options = PlaylistListSort.entries.map { stringResource(it.labelRes) },
                            onOptionSelected = { label ->
                                PlaylistListSort.entries.firstOrNull { context.getString(it.labelRes) == label }?.let {
                                    listSort = it
                                    preferences.saveSort(SORT_PLAYLIST_LIST, it.name)
                                }
                            },
                        )
                        IconButton(onClick = { createDialog = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.create_playlist),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(smartPlaylists, key = { it.title }) { playlist ->
                            ArtworkCard(
                                title = playlist.title,
                                subtitle = "${playlist.tracks.size} tracks",
                                artwork = playlist.artwork,
                                modifier =
                                    Modifier
                                        .width(150.dp)
                                        .clickable { onPlaylistClick(playlist) },
                            )
                        }
                    }
                }
            }
            if (customPlaylists.isEmpty()) {
                item { EmptyInline(stringResource(R.string.no_custom_playlists_found)) }
            } else {
                itemsIndexed(customPlaylists, key = { _, playlist -> playlist.id }) { index, playlist ->
                    if (editMode) {
                        EditablePlaylistRowWithDivider(
                            playlist = playlist,
                            selected = playlist.id in selectedPlaylistIds,
                            showDivider = index != customPlaylists.lastIndex,
                            onToggle = { onTogglePlaylistSelection(playlist.id) },
                            modifier = Modifier.animateItem(),
                        )
                    } else {
                        PlaylistRowWithDivider(
                            playlist = playlist,
                            showDivider = index != customPlaylists.lastIndex,
                            onClick = { onPlaylistClick(playlist) },
                            onLongClick = {
                                onTogglePlaylistSelection(playlist.id)
                                onEnterEditMode()
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = editMode,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(animationSpec = tween(220)) { it } + fadeIn(animationSpec = tween(180)),
            exit = slideOutVertically(animationSpec = tween(180)) { it } + fadeOut(animationSpec = tween(140)),
        ) {
            PlaylistEditBottomBar(
                hasSelection = selectedPlaylists.isNotEmpty(),
                showRename = true,
                renameEnabled = selectedPlaylists.size == 1,
                onPlay = {
                    if (selectedTracks.isNotEmpty()) {
                        onReplaceCurrentQueue(selectedTracks)
                        onExitEditMode()
                    }
                },
                onAdd = {
                    if (selectedTracks.isNotEmpty()) {
                        onAddTracksToRoute(selectedTracks)
                        onExitEditMode()
                    }
                },
                onShare = {
                    val intent =
                        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                            type = "audio/*"
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(selectedTracks.map { it.uri }))
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    context.startActivity(Intent.createChooser(intent, null))
                },
                onDelete = {
                    onDeletePlaylists(selectedPlaylistIds)
                    onExitEditMode()
                },
                onRename = {
                    selectedPlaylists.singleOrNull()?.let {
                        renameName = it.title
                        renamePlaylist = it
                    }
                },
            )
        }
    }
}

@Composable
private fun PlaylistDetailScreen(
    playlist: PlaylistGroup,
    onBack: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    trackStats: Map<Long, TrackStats>,
    currentTrackId: Long?,
    onTrackMenu: (Track) -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onAddTracks: () -> Unit,
    onEditModeChanged: (Boolean) -> Unit,
    onReplaceCurrentQueue: (List<Track>) -> Unit,
    onReplacePlaylistTracks: (List<Track>) -> Unit,
    onRenamePlaylist: (String) -> Unit,
    onSettings: () -> Unit,
    onAddTracksToRoute: (List<Track>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val preferences = rememberSortPreferences()
    var menuExpanded by remember { mutableStateOf(false) }
    var renameDialog by rememberSaveable(playlist.id) { mutableStateOf(false) }
    var renameName by rememberSaveable(playlist.id) { mutableStateOf(playlist.title) }
    var editMode by rememberSaveable(playlist.id) { mutableStateOf(false) }
    var showExitEditDialog by rememberSaveable(playlist.id) { mutableStateOf(false) }
    var selectedIds by rememberSaveable(playlist.id) { mutableStateOf(emptyList<Long>()) }
    var editTrackIds by rememberSaveable(playlist.id) { mutableStateOf(playlist.tracks.map { it.id }) }
    val tracksById = remember(playlist.tracks) { playlist.tracks.associateBy { it.id } }
    val editTracks = remember(editTrackIds, tracksById, playlist.tracks) { editTrackIds.mapNotNull { trackId -> tracksById[trackId] } }
    var sort by rememberSaveable(playlist.id) {
        mutableStateOf(preferences.loadEnumSort(SORT_PLAYLIST_PREFIX + playlist.id, PlaylistSort.CustomOrder, PlaylistSort.entries))
    }
    LaunchedEffect(playlist.tracks) {
        if (!editMode) editTrackIds = playlist.tracks.map { it.id }
    }
    LaunchedEffect(editMode) {
        onEditModeChanged(editMode)
        if (!editMode) selectedIds = emptyList()
    }
    val hasPendingPlaylistEdits =
        remember(editMode, playlist.tracks, editTrackIds) {
            editMode && playlist.tracks.map { it.id } != editTrackIds
        }
    val commitEditsAndExit = {
        if (hasPendingPlaylistEdits) onReplacePlaylistTracks(editTracks)
        showExitEditDialog = false
        editMode = false
    }
    val discardEditsAndExit = {
        editTrackIds = playlist.tracks.map { it.id }
        showExitEditDialog = false
        editMode = false
    }
    val requestExitEditMode = {
        if (hasPendingPlaylistEdits) {
            showExitEditDialog = true
        } else {
            editMode = false
        }
    }
    BackHandler(enabled = editMode) {
        requestExitEditMode()
    }
    if (showExitEditDialog) {
        AlertDialog(
            onDismissRequest = { showExitEditDialog = false },
            title = { Text(stringResource(R.string.playlist_edit_unsaved_title)) },
            text = { Text(stringResource(R.string.playlist_edit_unsaved_message)) },
            confirmButton = {
                TextButton(onClick = commitEditsAndExit) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = discardEditsAndExit) { Text(stringResource(R.string.discard)) }
                    TextButton(onClick = { showExitEditDialog = false }) { Text(stringResource(R.string.cancel)) }
                }
            },
        )
    }
    if (renameDialog) {
        AlertDialog(
            onDismissRequest = {
                renameDialog = false
                renameName = playlist.title
            },
            title = { Text(stringResource(R.string.rename)) },
            text = {
                OutlinedTextField(
                    value = renameName,
                    onValueChange = { renameName = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.playlist_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    enabled = renameName.isNotBlank(),
                    onClick = {
                        val name = renameName.trim()
                        if (name.isNotEmpty()) {
                            onRenamePlaylist(name)
                            renameDialog = false
                        }
                    },
                ) { Text(stringResource(R.string.done)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        renameDialog = false
                        renameName = playlist.title
                    },
                ) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
    val tracks =
        remember(playlist.tracks, editTracks, editMode, sort) {
            val source = if (editMode) editTracks else playlist.tracks
            when {
                editMode -> {
                    source
                }

                sort == PlaylistSort.CustomOrder -> {
                    source
                }

                sort == PlaylistSort.Name -> {
                    source.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
                }

                else -> {
                    source.sortedWith(
                        compareBy<Track, String>(String.CASE_INSENSITIVE_ORDER) { it.artist }
                            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title },
                    )
                }
            }
        }
    val selectedSet = remember(selectedIds) { selectedIds.toSet() }
    val selectedTracks = remember(selectedSet, editTracks) { editTracks.filter { it.id in selectedSet } }
    val isCustomPlaylist = playlist.id.startsWith(CUSTOM_PLAYLIST_PREFIX)
    val reorderableLazyListState =
        rememberReorderableLazyListState(listState) { from, to ->
            val headerItems = if (editMode) 1 else 2
            val fromIndex = from.index - headerItems
            val toIndex = to.index - headerItems
            if (fromIndex in editTracks.indices && toIndex in editTracks.indices && fromIndex != toIndex) {
                editTrackIds = editTrackIds.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
            }
        }
    val showPinnedTitle by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 180
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(appBackgroundContainerColor()),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 72.dp, bottom = if (editMode) 104.dp else MINI_PLAYER_RESERVED_BOTTOM),
        ) {
            if (!editMode) {
                item {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 28.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AlbumArt(
                            playlist.artwork,
                            Modifier
                                .fillMaxWidth(0.34f)
                                .aspectRatio(1f),
                            RoundedCornerShape(22.dp),
                        )
                        Text(
                            text = playlist.title,
                            modifier = Modifier.padding(top = 20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 25.sp,
                            lineHeight = 31.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = stringResource(R.string.tracks_count, tracks.size),
                            modifier = Modifier.padding(top = 8.dp, bottom = 22.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                                IconButton(
                                    onClick = {
                                        val shuffledTracks = tracks.smartAntiRepeatShuffle(trackStats)
                                        shuffledTracks.firstOrNull()?.let { onTrackClick(it, shuffledTracks) }
                                    },
                                    enabled = tracks.isNotEmpty(),
                                    modifier = Modifier.size(42.dp),
                                ) {
                                    Icon(Icons.Default.Shuffle, contentDescription = stringResource(R.string.shuffle_play))
                                }
                            }
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                                IconButton(
                                    onClick = { tracks.firstOrNull()?.let { onTrackClick(it, tracks) } },
                                    enabled = tracks.isNotEmpty(),
                                    modifier = Modifier.size(42.dp),
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = stringResource(R.string.play),
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = tabContentContainerColor(),
                ) {
                    Column(
                        modifier = Modifier.padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 8.dp),
                    ) {
                        if (!editMode) {
                            SortHeader(
                                label = stringResource(sort.labelRes),
                                options = PlaylistSort.entries.map { stringResource(it.labelRes) },
                                onOptionSelected = { label ->
                                    PlaylistSort.entries.firstOrNull { context.getString(it.labelRes) == label }?.let {
                                        sort = it
                                        preferences.saveSort(SORT_PLAYLIST_PREFIX + playlist.id, it.name)
                                    }
                                },
                            )
                        }
                        if (tracks.isEmpty()) {
                            EmptyInline(stringResource(R.string.no_tracks_found))
                        }
                    }
                }
            }
            itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                if (editMode) {
                    ReorderableItem(reorderableLazyListState, key = track.id) { isDragging ->
                        Surface(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                            shape =
                                if (index == tracks.lastIndex) {
                                    RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                                } else {
                                    RoundedCornerShape(0.dp)
                                },
                            color = tabContentContainerColor(),
                            tonalElevation = if (isDragging) 6.dp else 0.dp,
                        ) {
                            val reorderScope = this
                            EditablePlaylistTrackRow(
                                track = track,
                                selected = track.id in selectedSet,
                                showDivider = index != tracks.lastIndex,
                                onToggle = {
                                    selectedIds = if (track.id in selectedSet) selectedIds - track.id else selectedIds + track.id
                                },
                                dragHandleModifier = with(reorderScope) { Modifier.draggableHandle() },
                                modifier =
                                    Modifier
                                        .padding(horizontal = 20.dp)
                                        .padding(bottom = if (index == tracks.lastIndex) 14.dp else 0.dp),
                            )
                        }
                    }
                } else {
                    Surface(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .animateItem(),
                        shape =
                            if (index == tracks.lastIndex) {
                                RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                            } else {
                                RoundedCornerShape(0.dp)
                            },
                        color = tabContentContainerColor(),
                    ) {
                        PlaylistTrackRow(
                            track = track,
                            showDivider = index != tracks.lastIndex,
                            onClick = { onTrackClick(track, tracks) },
                            currentTrackId = currentTrackId,
                            onMenuClick = { onTrackMenu(track) },
                            onLongClick =
                                if (isCustomPlaylist) {
                                    {
                                        editTrackIds = playlist.tracks.map { it.id }
                                        selectedIds = listOf(track.id)
                                        editMode = true
                                    }
                                } else {
                                    null
                                },
                            modifier =
                                Modifier
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = if (index == tracks.lastIndex) 14.dp else 0.dp),
                        )
                    }
                }
            }
        }

        Row(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(appBackgroundContainerColor())
                    .padding(start = 8.dp, top = 18.dp, end = 8.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { if (editMode) requestExitEditMode() else onBack() }) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text =
                    if (editMode) {
                        if (selectedIds.isNotEmpty()) {
                            stringResource(R.string.selected_count_plain, selectedIds.size)
                        } else {
                            stringResource(R.string.select_track)
                        }
                    } else {
                        playlist.title
                    },
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .weight(1f)
                        .alpha(if (editMode || showPinnedTitle) 1f else 0f),
            )
            if (editMode) {
                TextButton(
                    onClick = {
                        selectedIds = if (selectedIds.size == editTracks.size) emptyList() else editTracks.map { it.id }
                    },
                ) {
                    val allSelected = selectedIds.size == editTracks.size
                    Icon(
                        if (allSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
                TextButton(onClick = commitEditsAndExit) {
                    Text(stringResource(R.string.done))
                }
            } else {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.favorite_playlist),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (!editMode) {
                IconButton(onClick = onAddTracks) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_tracks),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (!editMode) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.playlist_menu),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit)) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            enabled = isCustomPlaylist,
                            onClick = {
                                menuExpanded = false
                                editMode = true
                                editTrackIds = playlist.tracks.map { it.id }
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.rename)) },
                            enabled = isCustomPlaylist,
                            onClick = {
                                menuExpanded = false
                                renameName = playlist.title
                                renameDialog = true
                            },
                        )
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
        }
        AnimatedVisibility(
            visible = editMode,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(animationSpec = tween(220)) { it } + fadeIn(animationSpec = tween(180)),
            exit = slideOutVertically(animationSpec = tween(180)) { it } + fadeOut(animationSpec = tween(140)),
        ) {
            PlaylistEditBottomBar(
                hasSelection = selectedTracks.isNotEmpty(),
                onPlay = {
                    onReplaceCurrentQueue(selectedTracks)
                    commitEditsAndExit()
                },
                onAdd = {
                    onAddTracksToRoute(selectedTracks)
                    commitEditsAndExit()
                },
                onShare = {
                    val intent =
                        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                            type = "audio/*"
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(selectedTracks.map { it.uri }))
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    context.startActivity(Intent.createChooser(intent, null))
                },
                onDelete = {
                    editTrackIds = editTracks.filterNot { it.id in selectedSet }.map { it.id }
                    selectedIds = emptyList()
                },
            )
        }
    }
}

@Composable
private fun SelectTrackScreen(
    title: String,
    tracks: List<Track>,
    onBack: () -> Unit,
    onDone: (List<Track>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedIds by rememberSaveable { mutableStateOf(emptyList<Long>()) }
    var selectedTab by rememberSaveable { mutableStateOf(SelectPickerTab.Track) }
    var detailRoute by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedIdSet = remember(selectedIds) { selectedIds.toSet() }
    val selectedTracks = remember(selectedIdSet, tracks) { tracks.filter { it.id in selectedIdSet } }
    val albums =
        remember(tracks) {
            tracks
                .toAlbumGroups()
        }
    val artists =
        remember(tracks) {
            tracks
                .groupBy { it.artist.ifBlank { "Unknown artist" } }
                .map { (name, items) -> ArtistGroup(name, items.map { it.albumGroupId }.distinct().size, items) }
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        }
    val folders =
        remember(tracks) {
            tracks
                .groupBy { it.folder.ifBlank { "Unknown folder" } }
                .map { (path, items) -> FolderGroup(path.substringAfterLast('/').ifBlank { path }, path, items) }
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        }
    val detailTracks =
        remember(detailRoute, albums, artists, folders) {
            when {
                detailRoute?.startsWith("album:") == true -> {
                    val albumId = detailRoute?.removePrefix("album:").orEmpty()
                    albums.firstOrNull { it.id == albumId }?.tracks.orEmpty()
                }

                detailRoute?.startsWith("artist:") == true -> {
                    val name = detailRoute?.removePrefix("artist:").orEmpty()
                    artists.firstOrNull { it.name == name }?.tracks.orEmpty()
                }

                detailRoute?.startsWith("folder:") == true -> {
                    val path = detailRoute?.removePrefix("folder:").orEmpty()
                    folders.firstOrNull { it.path == path }?.tracks.orEmpty()
                }

                else -> {
                    emptyList()
                }
            }
        }
    val detailTitle =
        when {
            detailRoute?.startsWith("album:") == true -> {
                val albumId = detailRoute?.removePrefix("album:").orEmpty()
                albums.firstOrNull { it.id == albumId }?.title
            }

            detailRoute?.startsWith("artist:") == true -> {
                val name = detailRoute?.removePrefix("artist:").orEmpty()
                artists.firstOrNull { it.name == name }?.name
            }

            detailRoute?.startsWith("folder:") == true -> {
                val path = detailRoute?.removePrefix("folder:").orEmpty()
                folders.firstOrNull { it.path == path }?.name
            }

            else -> {
                null
            }
        }
    BackHandler(enabled = detailRoute != null) {
        detailRoute = null
    }
    Column(
        modifier = modifier.fillMaxSize().background(appBackgroundContainerColor()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 18.dp, end = 8.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { if (detailRoute != null) detailRoute = null else onBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    detailTitle ?: title,
                    modifier = Modifier.basicMarquee(),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    stringResource(R.string.selected_count_plain, selectedIds.size),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                )
            }
            TextButton(onClick = { onDone(selectedTracks) }, enabled = selectedIds.isNotEmpty()) {
                Text(stringResource(R.string.done))
            }
        }

        if (detailRoute == null) {
            SelectPickerTabs(
                selectedTab = selectedTab,
                onSelected = { selectedTab = it },
            )
        }

        RoundedPanelList(listState = rememberLazyListState(), topPadding = 0.dp) {
            if (detailRoute != null) {
                items(detailTracks, key = { it.id }) { track ->
                    SelectTrackRow(
                        track = track,
                        selected = track.id in selectedIdSet,
                        onClick = {
                            selectedIds = if (track.id in selectedIdSet) selectedIds - track.id else selectedIds + track.id
                        },
                    )
                }
                if (detailTracks.isEmpty()) item { EmptyInline(stringResource(R.string.no_tracks_found)) }
            } else {
                when (selectedTab) {
                    SelectPickerTab.Track -> {
                        items(tracks, key = { it.id }) { track ->
                            SelectTrackRow(
                                track = track,
                                selected = track.id in selectedIdSet,
                                onClick = {
                                    selectedIds = if (track.id in selectedIdSet) selectedIds - track.id else selectedIds + track.id
                                },
                            )
                        }
                        if (tracks.isEmpty()) item { EmptyInline(stringResource(R.string.no_tracks_found)) }
                    }

                    SelectPickerTab.Album -> {
                        items(albums, key = { it.id }) { album ->
                            MediaGroupRow(
                                artwork = album.tracks.firstOrNull()?.albumArtUri,
                                title = album.title,
                                subtitle = "${album.artist} | ${album.tracks.size} tracks",
                                onClick = { detailRoute = "album:${album.id}" },
                                modifier = Modifier.animateItem(),
                            )
                        }
                        if (albums.isEmpty()) item { EmptyInline(stringResource(R.string.no_albums_found)) }
                    }

                    SelectPickerTab.Artist -> {
                        items(artists, key = { it.name }) { artist ->
                            MediaGroupRow(
                                artwork = artist.tracks.firstOrNull()?.albumArtUri,
                                title = artist.name,
                                subtitle = "${artist.albums} albums | ${artist.tracks.size} tracks",
                                onClick = { detailRoute = "artist:${artist.name}" },
                                modifier = Modifier.animateItem(),
                            )
                        }
                        if (artists.isEmpty()) item { EmptyInline(stringResource(R.string.no_artists_found)) }
                    }

                    SelectPickerTab.Folder -> {
                        items(folders, key = { it.path }) { folder ->
                            MediaGroupRow(
                                artwork = folder.tracks.firstOrNull()?.albumArtUri,
                                title = folder.name,
                                subtitle = folder.path,
                                folder = true,
                                onClick = { detailRoute = "folder:${folder.path}" },
                                modifier = Modifier.animateItem(),
                            )
                        }
                        if (folders.isEmpty()) item { EmptyInline(stringResource(R.string.no_folders_found)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectPickerTabs(
    selectedTab: SelectPickerTab,
    onSelected: (SelectPickerTab) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(SelectPickerTab.entries, key = { it.name }) { tab ->
            val selected = selectedTab == tab
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp).clickable { onSelected(tab) },
            ) {
                Text(
                    text = tab.label(),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    fontSize = 15.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun SelectPickerTab.label(): String =
    when (this) {
        SelectPickerTab.Track -> stringResource(R.string.track)
        SelectPickerTab.Album -> stringResource(R.string.album)
        SelectPickerTab.Artist -> stringResource(R.string.artist)
        SelectPickerTab.Folder -> stringResource(R.string.folder)
    }

@Composable
private fun AddToPlaylistScreen(
    tracks: List<Track>,
    playlists: List<PlaylistGroup>,
    currentQueue: List<Track>,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onAddToCurrent: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAdd: (PlaylistGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    var createDialog by rememberSaveable { mutableStateOf(false) }
    var playlistName by rememberSaveable { mutableStateOf("") }
    val primaryTrack = tracks.firstOrNull()
    val targetSubtitle =
        primaryTrack?.title?.takeIf { tracks.size == 1 }
            ?: stringResource(R.string.tracks_count, tracks.size)
    if (createDialog) {
        AlertDialog(
            onDismissRequest = { createDialog = false },
            title = { Text(stringResource(R.string.create_playlist)) },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.playlist_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = playlistName.trim()
                        if (name.isNotEmpty()) {
                            createDialog = false
                            playlistName = ""
                            onCreatePlaylist(name)
                        }
                    },
                    enabled = playlistName.isNotBlank(),
                ) { Text(stringResource(R.string.done)) }
            },
            dismissButton = {
                TextButton(onClick = { createDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
    Column(
        modifier = modifier.fillMaxSize().background(appBackgroundContainerColor()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 18.dp, end = 8.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.add_to),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    primaryTrack?.title ?: stringResource(R.string.no_track_selected),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    maxLines = 1,
                )
            }
        }
        RoundedPanelList(listState = rememberLazyListState(), topPadding = 0.dp) {
            item {
                AddToActionRow(
                    icon = Icons.Default.Add,
                    title = stringResource(R.string.create_new_playlist),
                    subtitle = targetSubtitle,
                    enabled = tracks.isNotEmpty(),
                    showDivider = true,
                    onClick = { createDialog = true },
                )
            }
            item {
                AddToActionRow(
                    icon = Icons.Default.LibraryMusic,
                    title = stringResource(R.string.current_playlist),
                    subtitle =
                        if (currentQueue.isEmpty()) {
                            stringResource(
                                R.string.no_active_playlist,
                            )
                        } else {
                            stringResource(R.string.tracks_count, currentQueue.size)
                        },
                    enabled = tracks.isNotEmpty(),
                    showDivider = true,
                    onClick = onAddToCurrent,
                )
            }
            item {
                AddToActionRow(
                    icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    title = stringResource(R.string.favorite),
                    subtitle =
                        if (isFavorite) {
                            stringResource(
                                R.string.remove_from_favorite_tracks,
                            )
                        } else {
                            stringResource(R.string.add_to_favorite_tracks)
                        },
                    enabled = tracks.size == 1,
                    showDivider = true,
                    onClick = onToggleFavorite,
                )
            }
            val custom =
                playlists.filterNot {
                    it.id in
                        setOf(PLAYLIST_RECENTLY_ADDED, PLAYLIST_MOST_PLAYED, PLAYLIST_JUST_PLAYED, PLAYLIST_FAVORITE_TRACK)
                }
            if (custom.isEmpty()) {
                item { EmptyInline(stringResource(R.string.no_custom_playlists_found)) }
            } else {
                itemsIndexed(custom, key = { _, playlist -> playlist.id }) { index, playlist ->
                    PlaylistRowWithDivider(
                        playlist = playlist,
                        showDivider = index != custom.lastIndex,
                        onClick = { onAdd(playlist) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun AddToActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled, onClick = onClick)
                    .alpha(if (enabled) 1f else 0.5f)
                    .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(13.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 14.dp),
            ) {
                Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 18.sp)
                Text(
                    subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 66.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            )
        }
    }
}

@Composable
private fun SelectTrackRow(
    track: Track,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = selected, onCheckedChange = { onClick() })
        AlbumArt(track.albumArtUri, Modifier.size(48.dp), RoundedCornerShape(12.dp))
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 16.sp)
            Text(
                track.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }
    }
}

private data class FavoriteCardItem(
    val type: String,
    val key: String,
    val title: String,
    val subtitle: String,
    val artwork: Uri?,
    val tracks: List<Track> = emptyList(),
    val artist: ArtistGroup? = null,
    val album: AlbumGroup? = null,
    val folder: FolderGroup? = null,
    val playlist: PlaylistGroup? = null,
    val addedAt: Long,
)

private val FavoriteCardItem.selectionKey: String
    get() = "$type:$key"

private fun LibraryState.favoriteCards(): List<FavoriteCardItem> {
    val cards = mutableListOf<FavoriteCardItem>()
    val trackAddedAt =
        favoriteItems
            .filter { it.type == FavoriteType.Track }
            .maxOfOrNull { it.addedAt }
    if (favoriteTracks.isNotEmpty()) {
        val favoriteTrackPlaylist =
            PlaylistGroup(
                id = PLAYLIST_FAVORITE_TRACK,
                title = "Favorite track",
                tracks = favoriteTracks,
                artwork = favoriteTracks.firstOrNull()?.albumArtUri,
            )
        cards +=
            FavoriteCardItem(
                type = FavoriteType.Track,
                key = "favorite-tracks",
                title = favoriteTrackPlaylist.title,
                subtitle = "${favoriteTrackPlaylist.tracks.size} tracks",
                artwork = favoriteTrackPlaylist.artwork,
                tracks = favoriteTrackPlaylist.tracks,
                playlist = favoriteTrackPlaylist,
                addedAt = trackAddedAt ?: 0L,
            )
    }
    favoriteItems.forEach { favorite ->
        when (favorite.type) {
            FavoriteType.Artist -> {
                artists.firstOrNull { it.name == favorite.key }?.let { artist ->
                    cards +=
                        FavoriteCardItem(
                            type = FavoriteType.Artist,
                            key = favorite.key,
                            title = artist.name,
                            subtitle = "${artist.albums} albums | ${artist.tracks.size} tracks",
                            artwork = artist.tracks.firstOrNull()?.albumArtUri,
                            artist = artist,
                            addedAt = favorite.addedAt,
                        )
                }
            }

            FavoriteType.Folder -> {
                folders.firstOrNull { it.path == favorite.key }?.let { folder ->
                    cards +=
                        FavoriteCardItem(
                            type = FavoriteType.Folder,
                            key = favorite.key,
                            title = folder.name,
                            subtitle = folder.path,
                            artwork = folder.tracks.firstOrNull()?.albumArtUri,
                            folder = folder,
                            addedAt = favorite.addedAt,
                        )
                }
            }

            FavoriteType.Album -> {
                albums
                    .firstOrNull { it.id == favorite.key || it.id.startsWith("${favorite.key}:") }
                    ?.let { album ->
                        cards +=
                            FavoriteCardItem(
                                type = FavoriteType.Album,
                                key = favorite.key,
                                title = album.title,
                                subtitle = "${album.artist} | ${album.tracks.size} tracks",
                                artwork = album.tracks.firstOrNull()?.albumArtUri,
                                album = album,
                                addedAt = favorite.addedAt,
                            )
                    }
            }

            FavoriteType.Playlist -> {
                playlists
                    .firstOrNull { it.id == favorite.key && it.id.startsWith(CUSTOM_PLAYLIST_PREFIX) }
                    ?.let { playlist ->
                        cards +=
                            FavoriteCardItem(
                                type = FavoriteType.Playlist,
                                key = favorite.key,
                                title = playlist.title,
                                subtitle = "${playlist.tracks.size} tracks",
                                artwork = playlist.artwork,
                                tracks = playlist.tracks,
                                playlist = playlist,
                                addedAt = favorite.addedAt,
                            )
                    }
            }
        }
    }
    val distinctCards = cards.distinctBy { it.type to it.key }
    val favoriteTrackCard = distinctCards.firstOrNull { it.type == FavoriteType.Track }
    val otherCards =
        distinctCards
            .filterNot { it.type == FavoriteType.Track }
            .sortedByDescending { it.addedAt }
    return listOfNotNull(favoriteTrackCard) + otherCards
}

private fun LibraryState.smartPlaylists(): List<PlaylistGroup> {
    if (tracks.isEmpty()) return emptyList()
    val tracksById = tracks.associateBy { it.id }
    val latestDateAdded = tracks.maxOfOrNull { it.dateAddedMs } ?: 0L
    val recentlyAdded =
        if (latestDateAdded <= 0L) {
            tracks.sortedByDescending { it.dateAddedMs }.take(50)
        } else {
            val windowStart = latestDateAdded - 7L * 24L * 60L * 60L * 1_000L
            tracks
                .filter { it.dateAddedMs >= windowStart }
                .sortedByDescending { it.dateAddedMs }
        }
    val mostPlayed =
        tracks
            .filter { (trackStats[it.id]?.playCount ?: 0) > 0 }
            .sortedWith(
                compareByDescending<Track> { trackStats[it.id]?.playCount ?: 0 }
                    .thenByDescending { trackStats[it.id]?.lastPlayed ?: 0L }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title },
            ).take(50)
    val justPlayed =
        tracks
            .filter { (trackStats[it.id]?.lastPlayed ?: 0L) > 0L }
            .sortedByDescending { trackStats[it.id]?.lastPlayed ?: 0L }
            .take(50)
    val favoriteTracksByDate =
        favoriteItems
            .filter { it.type == FavoriteType.Track }
            .sortedByDescending { it.addedAt }
            .mapNotNull { favorite -> favorite.key.toLongOrNull()?.let(tracksById::get) }
    return listOf(
        PlaylistGroup(PLAYLIST_RECENTLY_ADDED, "Recently added", recentlyAdded, recentlyAdded.firstOrNull()?.albumArtUri),
        PlaylistGroup(PLAYLIST_MOST_PLAYED, "Most played", mostPlayed, mostPlayed.firstOrNull()?.albumArtUri),
        PlaylistGroup(PLAYLIST_JUST_PLAYED, "Just played", justPlayed, justPlayed.firstOrNull()?.albumArtUri),
        PlaylistGroup(PLAYLIST_FAVORITE_TRACK, "Favorite track", favoriteTracksByDate, favoriteTracksByDate.firstOrNull()?.albumArtUri),
    )
}

private fun LibraryState.findPlaylist(
    id: String,
    smartPlaylists: List<PlaylistGroup>,
): PlaylistGroup? =
    smartPlaylists.firstOrNull { it.id == id }
        ?: playlists.firstOrNull { it.id == id || it.title == id }

private fun LibraryState.customPlaylistComparator(sort: PlaylistListSort): Comparator<PlaylistGroup> =
    when (sort) {
        PlaylistListSort.Name -> {
            compareBy(String.CASE_INSENSITIVE_ORDER) { playlist: PlaylistGroup -> playlist.title }
        }

        PlaylistListSort.DateAdded -> {
            compareByDescending<PlaylistGroup> { it.createdAt }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
        }

        PlaylistListSort.DatePlayed -> {
            compareByDescending<PlaylistGroup> { playlist ->
                playlist.tracks.maxOfOrNull { track -> trackStats[track.id]?.lastPlayed ?: 0L } ?: 0L
            }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
        }
    }

@Composable
private fun TrackTab(
    library: LibraryState,
    onTrackClick: (Track, List<Track>) -> Unit,
    currentTrackId: Long?,
    onTrackMenu: (Track) -> Unit,
    onReplaceCurrentQueue: (List<Track>) -> Unit,
    onAddTracksToRoute: (List<Track>) -> Unit,
    onDeleteTracksPermanently: (List<Track>) -> Unit,
    onEditModeChanged: (Boolean) -> Unit,
    editStartRequest: Int,
    selectAllRequest: Int,
    onEditSelectionChanged: (Int, Boolean) -> Unit,
) {
    val listState = rememberHomeLazyListState()
    val preferences = rememberSortPreferences()
    val context = LocalContext.current
    var editMode by rememberSaveable { mutableStateOf(false) }
    var selectedIds by rememberSaveable { mutableStateOf(emptyList<Long>()) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var sort by rememberSaveable {
        mutableStateOf(preferences.loadEnumSort(SORT_TRACK, TrackSort.Name, TrackSort.entries))
    }
    val tracks = remember(library.tracks, sort) { library.tracks.sortedBy(sort) }
    val selectedTracks = remember(tracks, selectedIds) { tracks.filter { it.id in selectedIds.toSet() } }
    LaunchedEffect(editMode) {
        onEditModeChanged(editMode)
        if (!editMode) selectedIds = emptyList()
    }
    LaunchedEffect(editStartRequest) {
        if (editStartRequest > 0 && tracks.isNotEmpty()) editMode = true
    }
    LaunchedEffect(editMode, selectedIds, tracks) {
        onEditSelectionChanged(
            if (editMode) selectedIds.size else 0,
            editMode && tracks.isNotEmpty() && selectedIds.size == tracks.size,
        )
    }
    LaunchedEffect(selectAllRequest) {
        if (editMode && selectAllRequest > 0) {
            selectedIds =
                if (selectedIds.size == tracks.size) {
                    emptyList()
                } else {
                    tracks.map { it.id }
                }
        }
    }
    BackHandler(enabled = editMode) {
        editMode = false
    }
    if (confirmDelete) {
        ConfirmPermanentDeleteDialog(
            onConfirm = {
                confirmDelete = false
                onDeleteTracksPermanently(selectedTracks)
                editMode = false
            },
            onDismiss = { confirmDelete = false },
        )
    }
    val alphabetIndexes =
        remember(tracks, sort) {
            if (sort == TrackSort.Name) tracks.alphabetIndexes(positionOffset = 1) { it.title } else emptyList()
        }
    Box(modifier = Modifier.fillMaxSize()) {
        IndexedListWithRail(listState = listState, alphabetIndexes = alphabetIndexes) {
            item {
                SortHeader(
                    label = stringResource(sort.labelRes),
                    options = TrackSort.entries.map { stringResource(it.labelRes) },
                    onOptionSelected = { label ->
                        TrackSort.entries.firstOrNull { context.getString(it.labelRes) == label }?.let {
                            sort = it
                            preferences.saveSort(SORT_TRACK, it.name)
                        }
                    },
                )
            }
            items(tracks, key = { it.id }) { track ->
                if (editMode) {
                    SelectableTrackRow(
                        track = track,
                        selected = track.id in selectedIds,
                        onClick = { selectedIds = if (track.id in selectedIds) selectedIds - track.id else selectedIds + track.id },
                        modifier = Modifier.animateItem(),
                    )
                } else {
                    TrackRow(
                        track = track,
                        onClick = { onTrackClick(track, tracks) },
                        currentTrackId = currentTrackId,
                        onMenuClick = { onTrackMenu(track) },
                        onLongClick = {
                            editMode = true
                            selectedIds = listOf(track.id)
                        },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
            if (tracks.isEmpty()) item { EmptyInline(stringResource(R.string.no_tracks_found)) }
        }
        AnimatedVisibility(
            visible = editMode,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(animationSpec = tween(220)) { it } + fadeIn(animationSpec = tween(180)),
            exit = slideOutVertically(animationSpec = tween(180)) { it } + fadeOut(animationSpec = tween(140)),
        ) {
            PlaylistEditBottomBar(
                hasSelection = selectedTracks.isNotEmpty(),
                showShare = false,
                onPlay = {
                    onReplaceCurrentQueue(selectedTracks)
                    editMode = false
                },
                onAdd = {
                    onAddTracksToRoute(selectedTracks)
                    editMode = false
                },
                onShare = {},
                onDelete = { confirmDelete = true },
            )
        }
    }
}

@Composable
private fun AlbumTab(
    library: LibraryState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onAlbumClick: (AlbumGroup) -> Unit,
    currentTrackId: Long?,
    onTrackMenu: (Track) -> Unit,
    onReplaceCurrentQueue: (List<Track>) -> Unit,
    onAddTracksToRoute: (List<Track>) -> Unit,
    onDeleteTracksPermanently: (List<Track>) -> Unit,
    onEditModeChanged: (Boolean) -> Unit,
    editStartRequest: Int,
    selectAllRequest: Int,
    onEditSelectionChanged: (Int, Boolean) -> Unit,
) {
    val gridState = rememberHomeLazyGridState()
    val preferences = rememberSortPreferences()
    val context = LocalContext.current
    var editMode by rememberSaveable { mutableStateOf(false) }
    var selectedIds by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var sort by rememberSaveable {
        mutableStateOf(preferences.loadEnumSort(SORT_ALBUM, AlbumSort.Release, AlbumSort.entries))
    }
    val albums = remember(library.albums, sort) { library.albums.sortedBy(sort) }
    val selectedAlbums = remember(albums, selectedIds) { albums.filter { it.id in selectedIds.toSet() } }
    val selectedTracks = remember(selectedAlbums) { selectedAlbums.flatMap { it.tracks }.distinctBy { it.id } }
    LaunchedEffect(editMode) {
        onEditModeChanged(editMode)
        if (!editMode) selectedIds = emptyList()
    }
    LaunchedEffect(editStartRequest) {
        if (editStartRequest > 0 && albums.isNotEmpty()) editMode = true
    }
    LaunchedEffect(editMode, selectedIds, albums) {
        onEditSelectionChanged(
            if (editMode) selectedIds.size else 0,
            editMode && albums.isNotEmpty() && selectedIds.size == albums.size,
        )
    }
    LaunchedEffect(selectAllRequest) {
        if (editMode && selectAllRequest > 0) {
            selectedIds =
                if (selectedIds.size == albums.size) {
                    emptyList()
                } else {
                    albums.map { it.id }
                }
        }
    }
    BackHandler(enabled = editMode) { editMode = false }
    if (confirmDelete) {
        ConfirmPermanentDeleteDialog(
            onConfirm = {
                confirmDelete = false
                onDeleteTracksPermanently(selectedTracks)
                editMode = false
            },
            onDismiss = { confirmDelete = false },
        )
    }
    Box(modifier = Modifier.fillMaxSize()) {
        RoundedGridPanel(
            state = gridState,
            bottomPadding = if (editMode) 128.dp else MINI_PLAYER_RESERVED_BOTTOM,
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SortHeader(
                    label = stringResource(sort.labelRes),
                    options = AlbumSort.entries.map { stringResource(it.labelRes) },
                    onOptionSelected = { label ->
                        AlbumSort.entries.firstOrNull { context.getString(it.labelRes) == label }?.let {
                            sort = it
                            preferences.saveSort(SORT_ALBUM, it.name)
                        }
                    },
                )
            }
            items(albums, key = { it.id }) { album ->
                ArtworkCard(
                    title = album.title,
                    subtitle = "${album.artist} | ${album.tracks.size} tracks",
                    artwork = album.tracks.firstOrNull()?.albumArtUri,
                    badge = album.fileType,
                    selectionVisible = editMode,
                    selected = editMode && album.id in selectedIds,
                    artworkModifier =
                        with(sharedTransitionScope) {
                            Modifier.sharedElement(
                                rememberSharedContentState(albumSharedKey(album.id)),
                                animatedVisibilityScope,
                            )
                        },
                    modifier =
                        Modifier
                            .animateItem()
                            .combinedClickable(
                                onClick = {
                                    if (editMode) {
                                        selectedIds = if (album.id in selectedIds) selectedIds - album.id else selectedIds + album.id
                                    } else {
                                        onAlbumClick(album)
                                    }
                                },
                                onLongClick = {
                                    editMode = true
                                    selectedIds = listOf(album.id)
                                },
                            ),
                )
            }
            if (albums.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { EmptyInline(stringResource(R.string.no_albums_found)) }
            }
        }
        GroupEditBottomBar(
            visible = editMode,
            selectedTracks = selectedTracks,
            onReplaceCurrentQueue = onReplaceCurrentQueue,
            onAddTracksToRoute = onAddTracksToRoute,
            onDelete = { confirmDelete = true },
            onExit = { editMode = false },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun AlbumDetailScreen(
    album: AlbumGroup,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onSearch: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    trackStats: Map<Long, TrackStats>,
    currentTrackId: Long?,
    onTrackMenu: (Track) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val preferences = rememberSortPreferences()
    var subParentSort by rememberSaveable(album.id) {
        mutableStateOf(preferences.loadEnumSort(SORT_SUB_PARENT_PREFIX + album.id, SubParentSort.Name, SubParentSort.entries))
    }
    val albumTracks = remember(album.tracks) { album.tracks.sortedForAlbumPlayback() }
    val totalDurationMs = albumTracks.sumOf { it.durationMs.coerceAtLeast(0L) }
    val albumTrackGroups = remember(albumTracks, subParentSort) { albumTracks.groupForAlbumDetail(subParentSort) }
    val listState = rememberLazyListState()
    val collapseProgress by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (listState.firstVisibleItemScrollOffset / 400f).coerceIn(0f, 1f)
            }
        }
    }
    LaunchedEffect(listState) {
        var wasScrolling = false
        var previousScrollY = listState.albumDetailScrollY()
        var flingDirection = 0

        snapshotFlow {
            Triple(
                listState.isScrollInProgress,
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset,
            )
        }.collect { (isScrolling, _, _) ->
            val scrollY = listState.albumDetailScrollY()
            if (isScrolling) {
                if (!wasScrolling) {
                    flingDirection = 0
                    previousScrollY = scrollY
                }
                val delta = scrollY - previousScrollY
                if (delta > ALBUM_SNAP_FLING_DELTA_PX) {
                    flingDirection = 1
                } else if (delta < -ALBUM_SNAP_FLING_DELTA_PX) {
                    flingDirection = -1
                }
                previousScrollY = scrollY
                wasScrolling = true
            } else if (wasScrolling) {
                wasScrolling = false
                if (listState.firstVisibleItemIndex == 0) {
                    val targetCollapsed =
                        when {
                            flingDirection > 0 -> true
                            flingDirection < 0 -> false
                            collapseProgress >= ALBUM_SNAP_EXPAND_THRESHOLD -> true
                            else -> false
                        }
                    listState.animateScrollToItem(if (targetCollapsed) 1 else 0)
                }
            }
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(appBackgroundContainerColor()),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 72.dp, bottom = 116.dp),
        ) {
            item {
                AlbumHeader(
                    album = album,
                    albumTracks = albumTracks,
                    totalDurationMs = totalDurationMs,
                    trackStats = trackStats,
                    onTrackClick = onTrackClick,
                    coverModifier =
                        with(sharedTransitionScope) {
                            Modifier.sharedElement(
                                rememberSharedContentState(albumSharedKey(album.id)),
                                animatedVisibilityScope,
                            )
                        },
                )
            }

            item {
                AlbumTrackListHeader(
                    albumTracks = albumTracks,
                    albumTrackGroups = albumTrackGroups,
                    subParentSort = subParentSort,
                    onSubParentSortSelected = {
                        subParentSort = it
                        preferences.saveSort(SORT_SUB_PARENT_PREFIX + album.id, it.name)
                    },
                    trackStats = trackStats,
                    onTrackClick = onTrackClick,
                )
            }
            if (albumTracks.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                        color = tabContentContainerColor(),
                    ) {
                        EmptyInline(stringResource(R.string.no_tracks_found))
                    }
                }
            } else {
                albumTrackGroups.forEachIndexed { groupIndex, group ->
                    if (albumTrackGroups.size > 1) {
                        item(key = "album-group-${group.name}") {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = tabContentContainerColor(),
                            ) {
                                Text(
                                    text = group.name,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                start = 20.dp,
                                                top = if (groupIndex == 0) 10.dp else 22.dp,
                                                end = 20.dp,
                                                bottom = 8.dp,
                                            ),
                                )
                            }
                        }
                    }
                    itemsIndexed(group.tracks, key = { _, track -> "album-${group.name}-${track.id}" }) { index, track ->
                        val isLastTrack = groupIndex == albumTrackGroups.lastIndex && index == group.tracks.lastIndex
                        Surface(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                            shape =
                                if (isLastTrack) {
                                    RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                                } else {
                                    RoundedCornerShape(0.dp)
                                },
                            color = tabContentContainerColor(),
                        ) {
                            Column(
                                modifier =
                                    Modifier
                                        .padding(horizontal = 20.dp)
                                        .padding(bottom = if (isLastTrack) 14.dp else 0.dp),
                            ) {
                                AlbumTrackRow(
                                    index = track.trackNumber.takeIf { it > 0 } ?: index + 1,
                                    track = track,
                                    showDivider = !isLastTrack,
                                    onClick = { onTrackClick(track, albumTracks) },
                                    currentTrackId = currentTrackId,
                                    onMenuClick = { onTrackMenu(track) },
                                )
                            }
                        }
                    }
                }
            }
        }

        AlbumTopBar(
            album = album,
            titleAlpha = collapseProgress,
            isFavorite = isFavorite,
            onBack = onBack,
            onToggleFavorite = onToggleFavorite,
            onSearch = onSearch,
            onSettings = onSettings,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun AlbumTopBar(
    album: AlbumGroup,
    titleAlpha: Float,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(appBackgroundContainerColor())
                .padding(start = 8.dp, top = 18.dp, end = 8.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = album.title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier
                    .weight(1f)
                    .alpha(titleAlpha),
        )
        IconButton(onClick = onToggleFavorite) {
            Icon(
                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(R.string.favorite_album),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        IconButton(onClick = onSearch) {
            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search), tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = onSettings) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.menu), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun AlbumHeader(
    album: AlbumGroup,
    albumTracks: List<Track>,
    totalDurationMs: Long,
    trackStats: Map<Long, TrackStats>,
    onTrackClick: (Track, List<Track>) -> Unit,
    coverModifier: Modifier = Modifier,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AlbumArt(
            albumTracks.firstOrNull()?.albumArtUri,
            coverModifier.size(110.dp),
            RoundedCornerShape(24.dp),
        )
        Text(
            text = album.title,
            modifier = Modifier.padding(top = 24.dp),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 25.sp,
            lineHeight = 33.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = album.artist,
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 17.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(R.string.tracks_duration, albumTracks.size, totalDurationMs.formatDuration()),
            modifier = Modifier.padding(top = 8.dp, bottom = 26.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextButton(
                onClick = {
                    val shuffledTracks = albumTracks.smartAntiRepeatShuffle(trackStats)
                    shuffledTracks.firstOrNull()?.let { onTrackClick(it, shuffledTracks) }
                },
                enabled = albumTracks.isNotEmpty(),
            ) {
                Text(stringResource(R.string.shuffle_play))
            }
            TextButton(
                onClick = { albumTracks.firstOrNull()?.let { onTrackClick(it, albumTracks) } },
                enabled = albumTracks.isNotEmpty(),
            ) {
                Text(stringResource(R.string.play))
            }
        }
    }
}

@Composable
private fun AlbumTrackListHeader(
    albumTracks: List<Track>,
    albumTrackGroups: List<AlbumTrackGroup>,
    subParentSort: SubParentSort,
    onSubParentSortSelected: (SubParentSort) -> Unit,
    trackStats: Map<Long, TrackStats>,
    onTrackClick: (Track, List<Track>) -> Unit,
) {
    val context = LocalContext.current
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = tabContentContainerColor(),
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 8.dp),
        ) {
            AlbumTrackListActions(
                albumTracks = albumTracks,
                trackStats = trackStats,
                onTrackClick = onTrackClick,
            )
            if (albumTrackGroups.size > 1) {
                SortHeader(
                    label = stringResource(subParentSort.labelRes),
                    options = SubParentSort.entries.map { stringResource(it.labelRes) },
                    onOptionSelected = { label ->
                        SubParentSort.entries.firstOrNull { context.getString(it.labelRes) == label }?.let(onSubParentSortSelected)
                    },
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun AlbumTrackListActions(
    albumTracks: List<Track>,
    trackStats: Map<Long, TrackStats>,
    onTrackClick: (Track, List<Track>) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
            IconButton(
                onClick = {
                    val shuffledTracks = albumTracks.smartAntiRepeatShuffle(trackStats)
                    shuffledTracks.firstOrNull()?.let { onTrackClick(it, shuffledTracks) }
                },
                modifier = Modifier.size(42.dp),
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = stringResource(R.string.shuffle))
            }
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 12.dp),
        ) {
            IconButton(
                onClick = { albumTracks.firstOrNull()?.let { onTrackClick(it, albumTracks) } },
                modifier = Modifier.size(42.dp),
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.play),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun ArtistTab(
    library: LibraryState,
    onArtistClick: (ArtistGroup) -> Unit,
    currentTrackId: Long?,
    onTrackMenu: (Track) -> Unit,
    onReplaceCurrentQueue: (List<Track>) -> Unit,
    onAddTracksToRoute: (List<Track>) -> Unit,
    onDeleteTracksPermanently: (List<Track>) -> Unit,
    onEditModeChanged: (Boolean) -> Unit,
    editStartRequest: Int,
    selectAllRequest: Int,
    onEditSelectionChanged: (Int, Boolean) -> Unit,
) {
    val listState = rememberHomeLazyListState()
    val preferences = rememberSortPreferences()
    val context = LocalContext.current
    var editMode by rememberSaveable { mutableStateOf(false) }
    var selectedNames by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var sort by rememberSaveable {
        mutableStateOf(preferences.loadEnumSort(SORT_ARTIST, ArtistSort.Name, ArtistSort.entries))
    }
    val artists = remember(library.artists, sort) { library.artists.sortedBy(sort) }
    val selectedArtists = remember(artists, selectedNames) { artists.filter { it.name in selectedNames.toSet() } }
    val selectedTracks = remember(selectedArtists) { selectedArtists.flatMap { it.tracks }.distinctBy { it.id } }
    LaunchedEffect(editMode) {
        onEditModeChanged(editMode)
        if (!editMode) selectedNames = emptyList()
    }
    LaunchedEffect(editStartRequest) {
        if (editStartRequest > 0 && artists.isNotEmpty()) editMode = true
    }
    LaunchedEffect(editMode, selectedNames, artists) {
        onEditSelectionChanged(
            if (editMode) selectedNames.size else 0,
            editMode && artists.isNotEmpty() && selectedNames.size == artists.size,
        )
    }
    LaunchedEffect(selectAllRequest) {
        if (editMode && selectAllRequest > 0) {
            selectedNames =
                if (selectedNames.size == artists.size) {
                    emptyList()
                } else {
                    artists.map { it.name }
                }
        }
    }
    BackHandler(enabled = editMode) { editMode = false }
    if (confirmDelete) {
        ConfirmPermanentDeleteDialog(
            onConfirm = {
                confirmDelete = false
                onDeleteTracksPermanently(selectedTracks)
                editMode = false
            },
            onDismiss = { confirmDelete = false },
        )
    }
    val alphabetIndexes =
        remember(artists, sort) {
            if (sort == ArtistSort.Name) artists.alphabetIndexes(positionOffset = 1) { it.name } else emptyList()
        }
    Box(modifier = Modifier.fillMaxSize()) {
        IndexedListWithRail(listState = listState, alphabetIndexes = alphabetIndexes) {
            item {
                SortHeader(
                    label = stringResource(sort.labelRes),
                    options = ArtistSort.entries.map { stringResource(it.labelRes) },
                    onOptionSelected = { label ->
                        ArtistSort.entries.firstOrNull { context.getString(it.labelRes) == label }?.let {
                            sort = it
                            preferences.saveSort(SORT_ARTIST, it.name)
                        }
                    },
                )
            }
            items(artists, key = { it.name }) { artist ->
                if (editMode) {
                    SelectableMediaGroupRow(
                        artwork = artist.tracks.firstOrNull()?.albumArtUri,
                        title = artist.name,
                        subtitle = "${artist.albums} albums | ${artist.tracks.size} tracks",
                        selected = artist.name in selectedNames,
                        onClick = {
                            selectedNames =
                                if (artist.name in selectedNames) selectedNames - artist.name else selectedNames + artist.name
                        },
                        modifier = Modifier.animateItem(),
                    )
                } else {
                    MediaGroupRow(
                        artwork = artist.tracks.firstOrNull()?.albumArtUri,
                        title = artist.name,
                        subtitle = "${artist.albums} albums | ${artist.tracks.size} tracks",
                        onClick = { onArtistClick(artist) },
                        onLongClick = {
                            editMode = true
                            selectedNames = listOf(artist.name)
                        },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
            if (artists.isEmpty()) item { EmptyInline(stringResource(R.string.no_artists_found)) }
        }
        GroupEditBottomBar(
            visible = editMode,
            selectedTracks = selectedTracks,
            onReplaceCurrentQueue = onReplaceCurrentQueue,
            onAddTracksToRoute = onAddTracksToRoute,
            onDelete = { confirmDelete = true },
            onExit = { editMode = false },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

private enum class ArtistDetailTab(
    @StringRes val labelRes: Int,
) {
    Track(R.string.track),
    Album(R.string.album),
}

@Composable
private fun ArtistDetailScreen(
    artist: ArtistGroup,
    albums: List<AlbumGroup>,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onSearch: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    trackStats: Map<Long, TrackStats>,
    currentTrackId: Long?,
    onTrackMenu: (Track) -> Unit,
    onAlbumClick: (AlbumGroup) -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = ArtistDetailTab.entries
    val pagerState = rememberPagerState(initialPage = 0) { tabs.size }
    val coroutineScope = rememberCoroutineScope()
    val artistTracks = remember(artist.tracks) { artist.tracks.sortedForArtistPlayback() }
    val artistAlbums =
        remember(albums) {
            albums.sortedWith(
                compareBy<AlbumGroup>({
                    it.releaseYear().takeIf { year ->
                        year > 0
                    } ?: Int.MAX_VALUE
                }, { it.title.lowercase() }),
            )
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(appBackgroundContainerColor()),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 18.dp, end = 8.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = artist.name,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(R.string.favorite_artist),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search), tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.menu), tint = MaterialTheme.colorScheme.primary)
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = pagerState.currentPage == index
                Surface(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .clickable {
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            },
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                ) {
                    Text(
                        text = stringResource(
                            R.string.tab_count,
                            stringResource(tab.labelRes),
                            if (tab == ArtistDetailTab.Track) artistTracks.size else artistAlbums.size,
                        ),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp),
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (tabs[page]) {
                ArtistDetailTab.Track -> {
                    ArtistTrackTab(
                        albums = artistAlbums,
                        tracks = artistTracks,
                        trackStats = trackStats,
                        onTrackClick = onTrackClick,
                        currentTrackId = currentTrackId,
                        onTrackMenu = onTrackMenu,
                    )
                }

                ArtistDetailTab.Album -> {
                    ArtistAlbumTab(
                        albums = artistAlbums,
                        onAlbumClick = onAlbumClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistTrackTab(
    albums: List<AlbumGroup>,
    tracks: List<Track>,
    trackStats: Map<Long, TrackStats>,
    onTrackClick: (Track, List<Track>) -> Unit,
    currentTrackId: Long?,
    onTrackMenu: (Track) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(tabContentContainerColor()),
        contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 116.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                    IconButton(
                        onClick = {
                            val shuffledTracks = tracks.smartAntiRepeatShuffle(trackStats)
                            shuffledTracks.firstOrNull()?.let { onTrackClick(it, shuffledTracks) }
                        },
                        modifier = Modifier.size(42.dp),
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = stringResource(R.string.shuffle))
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 12.dp),
                ) {
                    IconButton(
                        onClick = { tracks.firstOrNull()?.let { onTrackClick(it, tracks) } },
                        modifier = Modifier.size(42.dp),
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.play),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }

        albums.forEach { album ->
            val albumTracks = album.tracks.sortedForAlbumPlayback()
            val albumGroups = albumTracks.groupForAlbumDetail()
            item(key = "artist-album-${album.id}") {
                ArtistAlbumHeader(album)
            }
            albumGroups.forEachIndexed { groupIndex, group ->
                if (albumTracks.any { it.discNumber > 0 } || albumTracks.map { it.folder }.distinct().size > 1) {
                    item(key = "artist-album-${album.id}-group-${group.name}") {
                        Text(
                            text = group.name,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = if (groupIndex == 0) 12.dp else 24.dp, bottom = 8.dp),
                        )
                    }
                }
                items(group.tracks, key = { "artist-track-${album.id}-${it.id}" }) { track ->
                    val index = group.tracks.indexOf(track)
                    val isLastTrack =
                        album == albums.lastOrNull() &&
                            groupIndex == albumGroups.lastIndex &&
                            index == group.tracks.lastIndex
                    AlbumTrackRow(
                        index = track.trackNumber.takeIf { it > 0 } ?: index + 1,
                        track = track,
                        showDivider = !isLastTrack,
                        onClick = { onTrackClick(track, albumTracks) },
                        currentTrackId = currentTrackId,
                        onMenuClick = { onTrackMenu(track) },
                    )
                }
            }
        }

        if (tracks.isEmpty()) item { EmptyInline(stringResource(R.string.no_tracks_found)) }
    }
}

@Composable
private fun ArtistAlbumHeader(album: AlbumGroup) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlbumArt(album.tracks.firstOrNull()?.albumArtUri, Modifier.size(64.dp), RoundedCornerShape(16.dp))
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
        ) {
            Text(
                text = album.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = album.yearLabel(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun ArtistAlbumTab(
    albums: List<AlbumGroup>,
    onAlbumClick: (AlbumGroup) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier =
            Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(tabContentContainerColor()),
        contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 116.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) { SortHeader(stringResource(R.string.sort_release)) }
        items(albums, key = { it.id }) { album ->
            ArtworkCard(
                title = album.title,
                subtitle = album.yearLabel(),
                artwork = album.tracks.firstOrNull()?.albumArtUri,
                badge = album.fileType,
                modifier = Modifier.clickable { onAlbumClick(album) },
            )
        }
        if (albums.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { EmptyInline(stringResource(R.string.no_albums_found)) }
        }
    }
}

@Composable
private fun FolderTab(
    library: LibraryState,
    onFolderClick: (FolderGroup) -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    currentTrackId: Long?,
    onTrackMenu: (Track) -> Unit,
    onReplaceCurrentQueue: (List<Track>) -> Unit,
    onAddTracksToRoute: (List<Track>) -> Unit,
    onDeleteTracksPermanently: (List<Track>) -> Unit,
    onEditModeChanged: (Boolean) -> Unit,
    editStartRequest: Int,
    selectAllRequest: Int,
    onEditSelectionChanged: (Int, Boolean) -> Unit,
) {
    val listState = rememberHomeLazyListState()
    val preferences = rememberSortPreferences()
    var editMode by rememberSaveable { mutableStateOf(false) }
    var selectedPaths by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var sort by rememberSaveable {
        mutableStateOf(preferences.loadEnumSort(SORT_FOLDER, FolderSort.Name, FolderSort.entries))
    }
    var viewMode by rememberSaveable {
        mutableStateOf(preferences.loadEnumSort(FOLDER_VIEW_MODE, FolderViewMode.Folder, FolderViewMode.entries))
    }
    var treePath by rememberSaveable { mutableStateOf(preferences.loadLastFolderTreePath()) }
    val folders = remember(library.folders, sort) { library.folders.sortedBy(sort) }
    val treeEntries = remember(library.folders) { library.folders.toFolderTreeEntries() }
    val selectedFolders = remember(folders, selectedPaths) { folders.filter { it.path in selectedPaths.toSet() } }
    val selectedTracks = remember(selectedFolders) { selectedFolders.flatMap { it.tracks }.distinctBy { it.id } }

    fun updateTreePath(path: List<String>) {
        treePath = path
        preferences.saveLastFolderTreePath(path)
    }

    LaunchedEffect(treeEntries) {
        if (!treeEntries.containsTreePath(treePath)) {
            updateTreePath(emptyList())
        }
    }

    LaunchedEffect(editMode) {
        onEditModeChanged(editMode)
        if (!editMode) selectedPaths = emptyList()
    }
    LaunchedEffect(viewMode) {
        if (viewMode == FolderViewMode.Tree) {
            editMode = false
            selectedPaths = emptyList()
        }
    }
    LaunchedEffect(editStartRequest) {
        if (editStartRequest > 0 && viewMode == FolderViewMode.Folder && folders.isNotEmpty()) editMode = true
    }
    LaunchedEffect(editMode, selectedPaths, folders) {
        onEditSelectionChanged(
            if (editMode) selectedPaths.size else 0,
            editMode && folders.isNotEmpty() && selectedPaths.size == folders.size,
        )
    }
    LaunchedEffect(selectAllRequest) {
        if (editMode && selectAllRequest > 0) {
            selectedPaths =
                if (selectedPaths.size == folders.size) {
                    emptyList()
                } else {
                    folders.map { it.path }
                }
        }
    }
    BackHandler(enabled = editMode) { editMode = false }
    BackHandler(enabled = viewMode == FolderViewMode.Tree && treePath.isNotEmpty() && !editMode) {
        updateTreePath(if (treePath.size <= 2) emptyList() else treePath.dropLast(1))
    }
    if (confirmDelete) {
        ConfirmPermanentDeleteDialog(
            onConfirm = {
                confirmDelete = false
                onDeleteTracksPermanently(selectedTracks)
                editMode = false
            },
            onDismiss = { confirmDelete = false },
        )
    }
    val alphabetIndexes =
        remember(folders, sort) {
            if (sort == FolderSort.Name) folders.alphabetIndexes(positionOffset = 1) { it.name } else emptyList()
        }
    Box(modifier = Modifier.fillMaxSize()) {
        if (viewMode == FolderViewMode.Folder) {
            IndexedListWithRail(listState = listState, alphabetIndexes = alphabetIndexes) {
                item {
                    FolderTabHeader(
                        sort = sort,
                        viewMode = viewMode,
                        onSortSelected = {
                            sort = it
                            preferences.saveSort(SORT_FOLDER, it.name)
                        },
                        onViewModeSelected = {
                            viewMode = it
                            preferences.saveSort(FOLDER_VIEW_MODE, it.name)
                        },
                    )
                }
                items(folders, key = { it.path }) { folder ->
                    if (editMode) {
                        SelectableMediaGroupRow(
                            artwork = folder.tracks.firstOrNull()?.albumArtUri,
                            title = folder.name,
                            subtitle = folder.path,
                            folder = true,
                            selected = folder.path in selectedPaths,
                            onClick = {
                                selectedPaths =
                                    if (folder.path in selectedPaths) selectedPaths - folder.path else selectedPaths + folder.path
                            },
                            modifier = Modifier.animateItem(),
                        )
                    } else {
                        MediaGroupRow(
                            artwork = folder.tracks.firstOrNull()?.albumArtUri,
                            title = folder.name,
                            subtitle = folder.path,
                            folder = true,
                            onClick = { onFolderClick(folder) },
                            onLongClick = {
                                editMode = true
                                selectedPaths = listOf(folder.path)
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
                if (folders.isEmpty()) item { EmptyInline(stringResource(R.string.no_folders_found)) }
            }
        } else {
            FolderTreeContent(
                entries = treeEntries,
                sort = sort,
                viewMode = viewMode,
                treePath = treePath,
                onTreePathChange = ::updateTreePath,
                onSortSelected = {
                    sort = it
                    preferences.saveSort(SORT_FOLDER, it.name)
                },
                onViewModeSelected = {
                    viewMode = it
                    preferences.saveSort(FOLDER_VIEW_MODE, it.name)
                },
                onTrackClick = onTrackClick,
                currentTrackId = currentTrackId,
                onTrackMenu = onTrackMenu,
            )
        }
        if (viewMode == FolderViewMode.Folder) {
            GroupEditBottomBar(
                visible = editMode,
                selectedTracks = selectedTracks,
                onReplaceCurrentQueue = onReplaceCurrentQueue,
                onAddTracksToRoute = onAddTracksToRoute,
                onDelete = { confirmDelete = true },
                onExit = { editMode = false },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun FolderTreeContent(
    entries: List<FolderTreeEntry>,
    sort: FolderSort,
    viewMode: FolderViewMode,
    treePath: List<String>,
    onTreePathChange: (List<String>) -> Unit,
    onSortSelected: (FolderSort) -> Unit,
    onViewModeSelected: (FolderViewMode) -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    currentTrackId: Long?,
    onTrackMenu: (Track) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (treePath.size > 1) {
            FolderTreeBreadcrumb(
                treePath = treePath,
                onTreePathChange = onTreePathChange,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
            )
        }
        AnimatedContent(
            targetState = treePath,
            transitionSpec = {
                val forward = targetState.size > initialState.size
                if (forward) {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(FOLDER_TREE_NAV_DUR),
                    ) + fadeIn(animationSpec = tween(FOLDER_TREE_NAV_DUR)) togetherWith
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(FOLDER_TREE_NAV_DUR),
                        ) + fadeOut(animationSpec = tween(FOLDER_TREE_NAV_DUR))
                } else {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(FOLDER_TREE_NAV_DUR),
                    ) + fadeIn(animationSpec = tween(FOLDER_TREE_NAV_DUR)) togetherWith
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(FOLDER_TREE_NAV_DUR),
                        ) + fadeOut(animationSpec = tween(FOLDER_TREE_NAV_DUR))
                }.using(SizeTransform(clip = false))
            },
            modifier =
                Modifier
                    .fillMaxSize(),
            label = "folder-tree-content",
        ) { currentTreePath ->
            val listState = rememberSaveable(currentTreePath, saver = LazyListState.Saver) { LazyListState() }
            val nodes = remember(entries, sort, currentTreePath) { entries.nodesAt(currentTreePath, sort) }
            val tracks = remember(entries, currentTreePath) { entries.tracksAt(currentTreePath) }
            val showTracks = currentTreePath.isNotEmpty() && tracks.isNotEmpty()

            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(tabContentContainerColor()),
                contentPadding =
                    PaddingValues(
                        start = 12.dp,
                        top = 14.dp,
                        end = 12.dp,
                        bottom = MINI_PLAYER_RESERVED_BOTTOM,
                    ),
            ) {
                item {
                    FolderTabHeader(
                        sort = sort,
                        viewMode = viewMode,
                        onSortSelected = onSortSelected,
                        onViewModeSelected = onViewModeSelected,
                    )
                }
                if (currentTreePath.isEmpty()) {
                    FOLDER_TREE_ROOT_ORDER.forEach { root ->
                        val rootNodes = entries.nodesAt(listOf(root), sort)
                        if (rootNodes.isNotEmpty()) {
                            item(key = "tree-root-title-$root") {
                                Text(
                                    text = root,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp, bottom = 6.dp),
                                )
                            }
                            items(rootNodes, key = { it.key }) { node ->
                                MediaGroupRow(
                                    artwork = node.tracks.firstOrNull()?.albumArtUri,
                                    title = node.name,
                                    subtitle = node.subtitle,
                                    folder = true,
                                    onClick = { onTreePathChange(listOf(root, node.name)) },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                }
                if (currentTreePath.isNotEmpty()) {
                    items(nodes, key = { it.key }) { node ->
                        MediaGroupRow(
                            artwork = node.tracks.firstOrNull()?.albumArtUri,
                            title = node.name,
                            subtitle = node.subtitle,
                            folder = true,
                            onClick = { onTreePathChange(currentTreePath + node.name) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
                if (showTracks) {
                    items(tracks, key = { it.id }) { track ->
                        TrackRow(
                            track = track,
                            onClick = { onTrackClick(track, tracks) },
                            currentTrackId = currentTrackId,
                            onMenuClick = { onTrackMenu(track) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
                if (entries.isEmpty()) {
                    item { EmptyInline(stringResource(R.string.no_folders_found)) }
                } else if (currentTreePath.isNotEmpty() && nodes.isEmpty() && !showTracks) {
                    item { EmptyInline(stringResource(R.string.no_tracks_found)) }
                }
            }
        }
    }
}

@Composable
private fun FolderTabHeader(
    sort: FolderSort,
    viewMode: FolderViewMode,
    onSortSelected: (FolderSort) -> Unit,
    onViewModeSelected: (FolderViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SortHeader(
            label = stringResource(sort.labelRes),
            modifier = Modifier.weight(1f),
            options = FolderSort.entries.map { stringResource(it.labelRes) },
            onOptionSelected = { label ->
                FolderSort.entries.firstOrNull { context.getString(it.labelRes) == label }?.let(onSortSelected)
            },
        )
        ModeHeader(
            label = stringResource(viewMode.labelRes),
            modifier = Modifier.width(102.dp),
            options = FolderViewMode.entries.map { stringResource(it.labelRes) },
            onOptionSelected = { label ->
                FolderViewMode.entries.firstOrNull { context.getString(it.labelRes) == label }?.let(onViewModeSelected)
            },
        )
    }
}

@Composable
private fun FolderTreeBreadcrumb(
    treePath: List<String>,
    onTreePathChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val root = treePath.firstOrNull() ?: return
    val childPath = treePath.drop(1)
    val childPathListState = rememberLazyListState()
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
    val activeColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(treePath) {
        if (childPath.isNotEmpty()) {
            childPathListState.animateScrollToItem(childPath.lastIndex)
        }
    }

    Row(
        modifier = modifier.padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = root,
            color = if (childPath.isEmpty()) activeColor else inactiveColor,
            fontSize = 15.sp,
            fontWeight = if (childPath.isEmpty()) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier
                    .clickable { onTreePathChange(emptyList()) }
                    .padding(vertical = 6.dp),
        )
        if (childPath.isNotEmpty()) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = inactiveColor,
                modifier =
                    Modifier
                        .padding(horizontal = 4.dp)
                        .size(18.dp),
            )
            LazyRow(
                state = childPathListState,
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                itemsIndexed(childPath, key = { index, name -> "$index-$name" }) { index, name ->
                    val isActive = index == childPath.lastIndex
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            color = if (isActive) activeColor else inactiveColor,
                            fontSize = 15.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier =
                                Modifier
                                    .clickable { onTreePathChange(treePath.take(index + 2)) }
                                    .padding(vertical = 6.dp),
                        )
                        if (!isActive) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = inactiveColor,
                                modifier =
                                    Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderDetailScreen(
    folder: FolderGroup,
    favorites: Set<Long>,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onSearch: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    trackStats: Map<Long, TrackStats>,
    currentTrackId: Long?,
    onTrackMenu: (Track) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    isFavorite: Boolean,
    onToggleFolderFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    Column(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(appBackgroundContainerColor())
                    .padding(start = 8.dp, top = 18.dp, end = 8.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = folder.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onToggleFolderFavorite) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(R.string.favorite_folder),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search), tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.menu), tint = MaterialTheme.colorScheme.primary)
            }
        }
        IndexedListWithRail(listState = listState, topPadding = 0.dp) {
            item {
                DetailSortHeader(
                    label = stringResource(R.string.name),
                    onShuffle = {
                        val shuffledTracks = folder.tracks.smartAntiRepeatShuffle(trackStats)
                        shuffledTracks.firstOrNull()?.let { onTrackClick(it, shuffledTracks) }
                    },
                    onPlay = { folder.tracks.firstOrNull()?.let { onTrackClick(it, folder.tracks) } },
                )
            }
            items(folder.tracks, key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    onClick = { onTrackClick(track, folder.tracks) },
                    currentTrackId = currentTrackId,
                    onMenuClick = { onTrackMenu(track) },
                )
            }
            if (folder.tracks.isEmpty()) item { EmptyInline(stringResource(R.string.no_tracks_found)) }
        }
    }
}

@Composable
private fun IndexedListWithRail(
    listState: LazyListState,
    topPadding: Dp = 14.dp,
    alphabetIndexes: List<AlphabetIndex> = emptyList(),
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val showAlphabetRail = alphabetIndexes.isNotEmpty()
    BoxWithConstraints {
        val railTopPadding = topPadding + 16.dp
        val railBottomPadding = MINI_PLAYER_RESERVED_BOTTOM
        val railHeight = (maxHeight - railTopPadding - railBottomPadding).coerceAtLeast(180.dp)
        RoundedPanelList(
            listState = listState,
            topPadding = topPadding,
            showAlphabetRail = showAlphabetRail,
            content = content,
        )
        if (showAlphabetRail) {
            AlphabetRail(
                indexes = alphabetIndexes,
                railHeight = railHeight,
                onIndexSelected = { index ->
                    coroutineScope.launch {
                        listState.scrollToItem(index.position)
                    }
                },
                modifier =
                    Modifier
                        .padding(top = railTopPadding)
                        .align(Alignment.TopEnd),
            )
        }
    }
}

@Composable
private fun RoundedPanelList(
    listState: LazyListState,
    topPadding: Dp = 14.dp,
    showAlphabetRail: Boolean = false,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier =
            Modifier
                .fillMaxSize()
                .padding(top = topPadding)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(tabContentContainerColor()),
        contentPadding =
            PaddingValues(
                start = 12.dp,
                top = 14.dp,
                end = if (showAlphabetRail) 46.dp else 12.dp,
                bottom = MINI_PLAYER_RESERVED_BOTTOM,
            ),
        content = content,
    )
}

@Composable
private fun RoundedGridPanel(
    state: LazyGridState = rememberLazyGridState(),
    bottomPadding: Dp = MINI_PLAYER_RESERVED_BOTTOM,
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        state = state,
        columns = GridCells.Fixed(2),
        modifier =
            Modifier
                .fillMaxSize()
                .padding(top = 14.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(tabContentContainerColor()),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = bottomPadding),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        content = content,
    )
}

@Composable
private fun SortHeader(
    label: String,
    modifier: Modifier = Modifier,
    options: List<String> = emptyList(),
    onOptionSelected: (String) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .clickable(enabled = options.isNotEmpty()) { expanded = true }
                    .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Default.Sort, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (options.isNotEmpty()) {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            fontWeight = if (option == label) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        expanded = false
                        onOptionSelected(option)
                    },
                )
            }
        }
    }
}

@Composable
private fun ModeHeader(
    label: String,
    modifier: Modifier = Modifier,
    options: List<String> = emptyList(),
    onOptionSelected: (String) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .clickable(enabled = options.isNotEmpty()) { expanded = true }
                    .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (options.isNotEmpty()) {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            fontWeight = if (option == label) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        expanded = false
                        onOptionSelected(option)
                    },
                )
            }
        }
    }
}

@Composable
private fun DetailSortHeader(
    label: String,
    onShuffle: () -> Unit,
    onPlay: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Default.Sort, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
        )
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
            IconButton(onClick = onShuffle, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Default.Shuffle, contentDescription = stringResource(R.string.shuffle))
            }
        }
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
            IconButton(onClick = onPlay, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.play))
            }
        }
    }
}

@Composable
private fun AlbumTrackRow(
    index: Int,
    track: Track,
    showDivider: Boolean,
    onClick: () -> Unit,
    currentTrackId: Long? = null,
    onMenuClick: (() -> Unit)? = null,
) {
    val isCurrent = track.id == currentTrackId
    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = index.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(34.dp),
            )
            Text(
                text = track.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 16.dp, end = 10.dp),
            )
            Text(
                text = track.durationMs.formatDuration(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                modifier = Modifier.padding(end = 8.dp),
            )
            IconButton(onClick = { onMenuClick?.invoke() }, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.track_menu))
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 50.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            )
        }
    }
}

private data class AlbumTrackGroup(
    val name: String,
    val tracks: List<Track>,
    val discNumber: Int = 0,
)

private fun List<Track>.sortedForAlbumPlayback(): List<Track> = groupForAlbumDetail().flatMap { it.tracks }

private fun List<Track>.sortedForArtistPlayback(): List<Track> =
    groupBy { it.albumGroupId }
        .values
        .sortedWith(
            compareBy<List<Track>>(
                { tracks -> tracks.mapNotNull { it.year.takeIf { year -> year > 0 } }.minOrNull() ?: Int.MAX_VALUE },
                { tracks ->
                    tracks
                        .firstOrNull()
                        ?.album
                        ?.lowercase()
                        .orEmpty()
                },
            ),
        ).flatMap { it.sortedForAlbumPlayback() }

private fun List<Track>.smartAntiRepeatShuffle(trackStats: Map<Long, TrackStats>): List<Track> {
    val uniqueTracks = distinctBy { it.id }
    if (uniqueTracks.size < 2) return uniqueTracks

    val now = System.currentTimeMillis()
    val random = Random(now xor uniqueTracks.size.toLong())
    val ranked =
        uniqueTracks
            .map { track ->
                val stats = trackStats[track.id]
                val lastPlayed = stats?.lastPlayed ?: 0L
                val playCount = (stats?.playCount ?: 0).coerceAtLeast(0)
                val ageScore =
                    if (lastPlayed <= 0L) {
                        1.0
                    } else {
                        ((now - lastPlayed).coerceAtLeast(0L).toDouble() / SHUFFLE_RECENCY_WINDOW_MS).coerceIn(0.0, 1.0)
                    }
                val playPenalty = (playCount.toDouble() / SHUFFLE_PLAYCOUNT_SOFT_CAP).coerceIn(0.0, 1.0)
                val score = random.nextDouble() + (ageScore * 0.6) - (playPenalty * 0.35)
                track to score
            }.sortedByDescending { it.second }
            .map { it.first }
            .toMutableList()

    val mixed = ArrayList<Track>(ranked.size)
    var lastArtist: String? = null
    var lastAlbumId: Long? = null
    while (ranked.isNotEmpty()) {
        val nextIndex =
            ranked.indexOfFirst { candidate ->
                val artistChanged = !candidate.artist.equals(lastArtist, ignoreCase = true)
                val albumChanged = candidate.albumId != lastAlbumId
                artistChanged && albumChanged
            }.takeIf { it >= 0 } ?: 0
        val selected = ranked.removeAt(nextIndex)
        mixed += selected
        lastArtist = selected.artist
        lastAlbumId = selected.albumId
    }
    return mixed
}

private const val SHUFFLE_RECENCY_WINDOW_MS = 1000L * 60L * 60L * 24L * 14L
private const val SHUFFLE_PLAYCOUNT_SOFT_CAP = 30.0

private fun AlbumGroup.releaseYear(): Int = tracks.mapNotNull { it.year.takeIf { year -> year > 0 } }.minOrNull() ?: 0

private fun AlbumGroup.yearLabel(): String = releaseYear().takeIf { it > 0 }?.toString() ?: "${tracks.size} tracks"

private fun List<Track>.groupForAlbumDetail(sort: SubParentSort = SubParentSort.Name): List<AlbumTrackGroup> {
    if (any { it.discNumber > 0 }) {
        return groupBy { it.discNumber.takeIf { disc -> disc > 0 } ?: 1 }
            .map { (discNumber, tracks) ->
                AlbumTrackGroup(
                    name = "Disc $discNumber",
                    tracks = tracks.sortedWith(albumTrackComparator()),
                    discNumber = discNumber,
                )
            }.sortedBy { it.discNumber }
    }
    return groupBy { it.folder.ifBlank { "Unknown folder" } }
        .map { (folder, tracks) ->
            AlbumTrackGroup(
                name = folder.displayFolderName(),
                tracks = tracks.sortedWith(albumTrackComparator()),
            )
        }.sortedWith { first, second ->
            when (sort) {
                SubParentSort.Name -> {
                    first.name.compareNaturally(second.name)
                }

                SubParentSort.DateAdded -> {
                    (second.tracks.maxOfOrNull { it.dateAddedMs } ?: 0L)
                        .compareTo(first.tracks.maxOfOrNull { it.dateAddedMs } ?: 0L)
                        .takeIf { it != 0 }
                        ?: first.name.compareNaturally(second.name)
                }
            }
        }
}

private fun albumTrackComparator(): Comparator<Track> =
    Comparator { first, second ->
        val firstDisc = first.discNumber.takeIf { it > 0 }
        val secondDisc = second.discNumber.takeIf { it > 0 }
        val firstNumber = first.trackNumber.takeIf { it > 0 }
        val secondNumber = second.trackNumber.takeIf { it > 0 }
        when {
            firstDisc != null && secondDisc != null && firstDisc != secondDisc -> firstDisc.compareTo(secondDisc)
            firstDisc != null && secondDisc == null -> -1
            firstDisc == null && secondDisc != null -> 1
            firstNumber != null && secondNumber != null && firstNumber != secondNumber -> firstNumber.compareTo(secondNumber)
            firstNumber != null && secondNumber == null -> -1
            firstNumber == null && secondNumber != null -> 1
            else -> first.title.compareTo(second.title, ignoreCase = true)
        }
    }

private fun String.displayFolderName(): String = substringAfterLast('/').ifBlank { this }

private fun LazyListState.albumDetailScrollY(): Int = firstVisibleItemIndex * 100_000 + firstVisibleItemScrollOffset

private fun LazyListState.centeredTabIndex(tabCount: Int): Int {
    if (tabCount <= 0) return 0
    val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
    return layoutInfo.visibleItemsInfo
        .minByOrNull { item -> abs((item.offset + item.size / 2) - center) }
        ?.index
        ?.coerceIn(0, tabCount - 1)
        ?: firstVisibleItemIndex.coerceIn(0, tabCount - 1)
}

private fun albumSharedKey(albumId: String): String = "album-art-$albumId"

@Composable
private fun rememberSortPreferences(): MusicPreferences {
    val context = LocalContext.current
    return remember(context) { MusicPreferences(context) }
}

private inline fun <reified T> MusicPreferences.loadEnumSort(
    key: String,
    defaultValue: T,
    entries: List<T>,
): T where T : Enum<T> {
    val saved = loadSort(key, defaultValue.name)
    return entries.firstOrNull { it.name == saved } ?: defaultValue
}

private fun List<Track>.sortedBy(sort: TrackSort): List<Track> =
    when (sort) {
        TrackSort.Name -> {
            sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
        }

        TrackSort.DateAdded -> {
            sortedWith(compareByDescending<Track> { it.dateAddedMs }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.title })
        }

        TrackSort.Artist -> {
            sortedWith(
                compareBy<Track, String>(String.CASE_INSENSITIVE_ORDER) { it.artist }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title },
            )
        }
    }

private fun List<AlbumGroup>.sortedBy(sort: AlbumSort): List<AlbumGroup> =
    when (sort) {
        AlbumSort.Release -> {
            sortedWith(
                compareBy<AlbumGroup>({
                    it.releaseYear().takeIf { year ->
                        year > 0
                    } ?: Int.MAX_VALUE
                }, { it.title.lowercase() }),
            )
        }

        AlbumSort.Name -> {
            sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
        }

        AlbumSort.Artist -> {
            sortedWith(
                compareBy<AlbumGroup, String>(String.CASE_INSENSITIVE_ORDER) { it.artist }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title },
            )
        }
    }

private fun List<ArtistGroup>.sortedBy(sort: ArtistSort): List<ArtistGroup> =
    when (sort) {
        ArtistSort.Name -> {
            sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        }

        ArtistSort.DateAdded -> {
            sortedWith(
                compareByDescending<ArtistGroup> {
                    it.tracks.maxOfOrNull(Track::dateAddedMs) ?: 0L
                }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.name },
            )
        }
    }

private fun List<FolderGroup>.sortedBy(sort: FolderSort): List<FolderGroup> =
    when (sort) {
        FolderSort.Name -> {
            sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        }

        FolderSort.DateAdded -> {
            sortedWith(
                compareByDescending<FolderGroup> {
                    it.tracks.maxOfOrNull(Track::dateAddedMs) ?: 0L
                }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.name },
            )
        }
    }

private fun List<FolderGroup>.toFolderTreeEntries(): List<FolderTreeEntry> = mapNotNull { folder -> folder.toFolderTreeEntry() }

private fun FolderGroup.toFolderTreeEntry(): FolderTreeEntry? {
    val normalizedPath = path.replace('\\', '/').trim()
    if (normalizedPath.isBlank()) return null
    val trimmed = normalizedPath.trim('/')
    val parts = trimmed.split('/').filter { it.isNotBlank() }
    if (parts.isEmpty()) {
        return FolderTreeEntry(
            root = FOLDER_TREE_INTERNAL,
            segments = listOf(name),
            folder = this,
        )
    }

    val lowerPath = normalizedPath.lowercase()
    val root: String
    val segments: List<String>
    when {
        lowerPath.startsWith("/storage/emulated/0") -> {
            root = FOLDER_TREE_INTERNAL
            segments = normalizedPath.removePrefixIgnoreCase("/storage/emulated/0").pathSegmentsOrName(name)
        }

        lowerPath.startsWith("/sdcard") -> {
            root = FOLDER_TREE_INTERNAL
            segments = normalizedPath.removePrefixIgnoreCase("/sdcard").pathSegmentsOrName(name)
        }

        parts.size >= 2 && parts[0].equals("storage", ignoreCase = true) && parts[1].isPortableStorageId() -> {
            root = FOLDER_TREE_MICRO_SD
            segments = parts.drop(2).ifEmpty { listOf(name) }
        }

        parts.firstOrNull()?.equals("storage", ignoreCase = true) == true ||
            parts.firstOrNull()?.equals("mnt", ignoreCase = true) == true -> {
            root = FOLDER_TREE_EXTERNAL
            segments = parts.drop(1).ifEmpty { listOf(name) }
        }

        else -> {
            root = FOLDER_TREE_INTERNAL
            segments = parts
        }
    }

    return FolderTreeEntry(
        root = root,
        segments = segments.filter { it.isNotBlank() },
        folder = this,
    )
}

private fun List<FolderTreeEntry>.nodesAt(
    treePath: List<String>,
    sort: FolderSort,
): List<FolderTreeNode> {
    if (treePath.isEmpty()) {
        return FOLDER_TREE_ROOT_ORDER.mapNotNull { root ->
            val rootEntries = filter { it.root == root }
            if (rootEntries.isEmpty()) {
                null
            } else {
                val tracks = rootEntries.flatMap { it.folder.tracks }.distinctBy { it.id }
                FolderTreeNode(
                    key = root,
                    name = root,
                    subtitle = "${rootEntries.size} folders | ${tracks.size} tracks",
                    tracks = tracks,
                )
            }
        }
    }

    val root = treePath.first()
    val currentSegments = treePath.drop(1)
    val matching =
        filter {
            it.root == root && it.segments.startsWithSegments(currentSegments)
        }
    val childGroups =
        matching
            .filter { it.segments.size > currentSegments.size }
            .groupBy { it.segments[currentSegments.size] }
    return childGroups
        .map { (name, childEntries) ->
            val childSegments = currentSegments + name
            val tracks = childEntries.flatMap { it.folder.tracks }.distinctBy { it.id }
            val directTracks =
                matching
                    .filter { it.segments.equalsSegments(childSegments) }
                    .flatMap { it.folder.tracks }
                    .distinctBy { it.id }
            val childFolderCount =
                matching
                    .filter { it.segments.startsWithSegments(childSegments) && it.segments.size > childSegments.size }
                    .map { it.segments[childSegments.size] }
                    .distinctBy { it.lowercase() }
                    .size
            FolderTreeNode(
                key = (treePath + name).joinToString("/"),
                name = name,
                subtitle = "$childFolderCount folders | ${directTracks.size} tracks",
                tracks = tracks,
            )
        }.sortedTreeNodesBy(sort)
}

private fun List<FolderTreeEntry>.containsTreePath(treePath: List<String>): Boolean {
    if (treePath.isEmpty()) return true
    val root = treePath.first()
    if (root !in FOLDER_TREE_ROOT_ORDER) return false
    val segments = treePath.drop(1)
    if (segments.isEmpty()) return any { it.root == root }
    return any { it.root == root && it.segments.startsWithSegments(segments) }
}

private fun List<FolderTreeEntry>.tracksAt(treePath: List<String>): List<Track> {
    if (treePath.isEmpty()) return emptyList()
    val root = treePath.first()
    val currentSegments = treePath.drop(1)
    return filter {
        it.root == root && it.segments.equalsSegments(currentSegments)
    }.flatMap { it.folder.tracks }
        .distinctBy { it.id }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
}

private fun List<FolderTreeNode>.sortedTreeNodesBy(sort: FolderSort): List<FolderTreeNode> =
    when (sort) {
        FolderSort.Name -> {
            sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        }

        FolderSort.DateAdded -> {
            sortedWith(
                compareByDescending<FolderTreeNode> {
                    it.tracks.maxOfOrNull(Track::dateAddedMs) ?: 0L
                }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.name },
            )
        }
    }

private fun List<String>.startsWithSegments(prefix: List<String>): Boolean =
    size >= prefix.size && prefix.indices.all { index -> this[index].equals(prefix[index], ignoreCase = true) }

private fun List<String>.equalsSegments(other: List<String>): Boolean = size == other.size && startsWithSegments(other)

private fun String.pathSegmentsOrName(fallbackName: String): List<String> =
    trim('/')
        .split('/')
        .filter { it.isNotBlank() }
        .ifEmpty { listOf(fallbackName) }

private fun String.removePrefixIgnoreCase(prefix: String): String =
    if (startsWith(prefix, ignoreCase = true)) substring(prefix.length) else this

private fun String.isPortableStorageId(): Boolean = matches(Regex("[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}"))

private fun <T> List<T>.alphabetIndexes(
    positionOffset: Int,
    label: (T) -> String,
): List<AlphabetIndex> {
    val indexes = mutableListOf<AlphabetIndex>()
    var lastLetter: Char? = null
    forEachIndexed { index, item ->
        val letter = label(item).firstAlphabetLetterOrNull() ?: return@forEachIndexed
        if (letter != lastLetter) {
            indexes += AlphabetIndex(letter = letter, position = index + positionOffset)
            lastLetter = letter
        }
    }
    return indexes
}

private fun String.firstAlphabetLetterOrNull(): Char? =
    trim()
        .firstOrNull { it.isLetter() }
        ?.uppercaseChar()
        ?.takeIf { it in 'A'..'Z' }

private fun String.compareNaturally(other: String): Int {
    val firstParts = Regex("\\d+|\\D+").findAll(lowercase()).map { it.value }.toList()
    val secondParts = Regex("\\d+|\\D+").findAll(other.lowercase()).map { it.value }.toList()
    val maxSize = minOf(firstParts.size, secondParts.size)
    repeat(maxSize) { index ->
        val first = firstParts[index]
        val second = secondParts[index]
        val result =
            if (first.all(Char::isDigit) && second.all(Char::isDigit)) {
                first.toLongOrNull()?.compareTo(second.toLongOrNull() ?: 0L) ?: first.compareTo(second)
            } else {
                first.compareTo(second)
            }
        if (result != 0) return result
    }
    return firstParts.size.compareTo(secondParts.size)
}

@Composable
private fun AnimatedSelectionIconButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleState =
        remember {
            MutableTransitionState(false).apply { targetState = true }
        }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = expandHorizontally(animationSpec = tween(180), expandFrom = Alignment.Start) + fadeIn(animationSpec = tween(160)),
        exit = shrinkHorizontally(animationSpec = tween(140), shrinkTowards = Alignment.Start) + fadeOut(animationSpec = tween(120)),
    ) {
        IconButton(onClick = onClick, modifier = modifier.size(42.dp)) {
            Icon(
                if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun TrackRow(
    track: Track,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    currentTrackId: Long? = null,
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val isCurrent = track.id == currentTrackId
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlbumArt(track.albumArtUri, Modifier.size(48.dp), RoundedCornerShape(12.dp))
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
        ) {
            Text(
                track.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                track.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }
        IconButton(onClick = { onMenuClick?.invoke() }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.track_menu))
        }
    }
}

@Composable
private fun SelectableTrackRow(
    track: Track,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedSelectionIconButton(selected = selected, onClick = onClick)
        AlbumArt(track.albumArtUri, Modifier.size(48.dp), RoundedCornerShape(12.dp))
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
        ) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 16.sp)
            Text(
                track.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }
    }
}

private fun Long.formatDuration(): String {
    val totalSeconds = (coerceAtLeast(0L) / 1_000L).toInt()
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "$minutes:${seconds.toString().padStart(2, '0')}"
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun MediaGroupRow(
    artwork: Uri?,
    title: String,
    subtitle: String,
    folder: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ).padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            AlbumArt(artwork, Modifier.size(56.dp), RoundedCornerShape(13.dp))
            if (folder) {
                Icon(
                    Icons.Outlined.Folder,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Column(modifier = Modifier.padding(start = 14.dp)) {
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 16.sp)
            Text(
                subtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun SelectableMediaGroupRow(
    artwork: Uri?,
    title: String,
    subtitle: String,
    selected: Boolean,
    folder: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedSelectionIconButton(selected = selected, onClick = onClick)
        Box {
            AlbumArt(artwork, Modifier.size(56.dp), RoundedCornerShape(13.dp))
            if (folder) {
                Icon(
                    Icons.Outlined.Folder,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Column(modifier = Modifier.padding(start = 14.dp)) {
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 16.sp)
            Text(
                subtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun PlaylistTrackRow(
    track: Track,
    showDivider: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    currentTrackId: Long? = null,
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val isCurrent = track.id == currentTrackId
    Column(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick,
                    ).padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AlbumArt(track.albumArtUri, Modifier.size(52.dp), RoundedCornerShape(12.dp))
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
            ) {
                Text(
                    track.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 18.sp,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    track.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                )
            }
            IconButton(onClick = { onMenuClick?.invoke() }) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.track_menu))
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 66.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            )
        }
    }
}

@Composable
private fun EditablePlaylistTrackRow(
    track: Track,
    selected: Boolean,
    showDivider: Boolean,
    onToggle: () -> Unit,
    dragHandleModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedSelectionIconButton(selected = selected, onClick = onToggle)
            AlbumArt(track.albumArtUri, Modifier.size(48.dp), RoundedCornerShape(12.dp))
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
            ) {
                Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 16.sp)
                Text(
                    track.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
            }
            Box(
                modifier = dragHandleModifier.size(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.DragIndicator,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 70.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            )
        }
    }
}

@Composable
private fun GroupEditBottomBar(
    visible: Boolean,
    selectedTracks: List<Track>,
    onReplaceCurrentQueue: (List<Track>) -> Unit,
    onAddTracksToRoute: (List<Track>) -> Unit,
    onDelete: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(animationSpec = tween(220)) { it } + fadeIn(animationSpec = tween(180)),
        exit = slideOutVertically(animationSpec = tween(180)) { it } + fadeOut(animationSpec = tween(140)),
    ) {
        PlaylistEditBottomBar(
            hasSelection = selectedTracks.isNotEmpty(),
            showShare = false,
            onPlay = {
                onReplaceCurrentQueue(selectedTracks)
                onExit()
            },
            onAdd = {
                onAddTracksToRoute(selectedTracks)
                onExit()
            },
            onShare = {},
            onDelete = onDelete,
        )
    }
}

@Composable
private fun ConfirmPermanentDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_permanently)) },
        text = { Text(stringResource(R.string.delete_tracks_confirm)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.delete)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

private fun shareTracks(
    context: android.content.Context,
    tracks: List<Track>,
) {
    if (tracks.isEmpty()) return
    val intent =
        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "audio/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(tracks.map { it.uri }))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    context.startActivity(Intent.createChooser(intent, null))
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TrackActionSheet(
    track: Track,
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onTrackInfo: () -> Unit,
    onEditMetadata: () -> Unit,
    onAlbum: () -> Unit,
    onArtist: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 20.dp, end = 20.dp, bottom = 18.dp),
        ) {
            Text(
                track.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            TrackActionRow(Icons.Default.Info, stringResource(R.string.track_info), onTrackInfo)
            TrackActionRow(Icons.Default.Edit, stringResource(R.string.edit_metadata), onEditMetadata)
            TrackActionRow(Icons.Default.Add, stringResource(R.string.add_to), onAdd)
            TrackActionRow(Icons.Default.Delete, stringResource(R.string.delete), onDelete)
            TrackActionRow(Icons.Default.Share, stringResource(R.string.share), onShare)
            TrackActionRow(Icons.Default.LibraryMusic, stringResource(R.string.album), onAlbum)
            TrackActionRow(Icons.Default.MusicNote, stringResource(R.string.artist), onArtist)
        }
    }
}

@Composable
private fun TrackActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 16.sp, modifier = Modifier.padding(start = 18.dp))
    }
}

@Composable
private fun PlaylistEditBottomBar(
    hasSelection: Boolean,
    showShare: Boolean = true,
    showRename: Boolean = false,
    renameEnabled: Boolean = hasSelection,
    deleteEnabled: Boolean = hasSelection,
    onPlay: () -> Unit,
    onAdd: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            EditActionButton(Icons.Default.PlayArrow, stringResource(R.string.play), hasSelection, onPlay)
            EditActionButton(Icons.Default.Add, stringResource(R.string.add), hasSelection, onAdd)
            if (showShare) {
                EditActionButton(Icons.Default.Share, stringResource(R.string.share), hasSelection, onShare)
            }
            EditActionButton(Icons.Default.Delete, stringResource(R.string.delete), deleteEnabled, onDelete)
            if (showRename) {
                EditActionButton(Icons.Default.Edit, stringResource(R.string.rename), renameEnabled, onRename)
            }
        }
    }
}

@Composable
private fun EditActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(onClick = onClick, enabled = enabled) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier =
                    Modifier
                        .size(26.dp),
            )
        }
        Text(
            label,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            fontSize = 14.sp,
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun PlaylistRowWithDivider(
    playlist: PlaylistGroup,
    showDivider: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PlaylistRow(
            playlist = playlist,
            onClick = onClick,
            onLongClick = onLongClick,
        )
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 68.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun PlaylistRow(
    playlist: PlaylistGroup,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlbumArt(playlist.artwork, Modifier.size(56.dp), RoundedCornerShape(13.dp))
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
        ) {
            Text(playlist.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 19.sp)
            Text(
                "${playlist.tracks.size} tracks",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun EditablePlaylistRowWithDivider(
    playlist: PlaylistGroup,
    selected: Boolean,
    showDivider: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedSelectionIconButton(selected = selected, onClick = onToggle)
            AlbumArt(playlist.artwork, Modifier.size(52.dp), RoundedCornerShape(13.dp))
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 14.dp),
            ) {
                Text(playlist.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 18.sp)
                Text(
                    stringResource(R.string.tracks_count, playlist.tracks.size),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 108.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            )
        }
    }
}

@Composable
private fun FavoriteGridCard(
    card: FavoriteCardItem,
    selectionVisible: Boolean = false,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.BottomStart,
        ) {
            AlbumArt(card.artwork, Modifier.fillMaxSize(), RoundedCornerShape(18.dp))
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.28f)),
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = card.type.replaceFirstChar { it.uppercase() },
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    text = card.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = selectionVisible,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                enter = fadeIn(animationSpec = tween(160)) + expandHorizontally(animationSpec = tween(180), expandFrom = Alignment.End),
                exit = fadeOut(animationSpec = tween(120)) + shrinkHorizontally(animationSpec = tween(140), shrinkTowards = Alignment.End),
            ) {
                Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.38f)) {
                    Icon(
                        if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = Color.White,
                        modifier =
                            Modifier
                                .padding(4.dp)
                                .size(22.dp),
                    )
                }
            }
        }
        Text(card.title, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp), fontSize = 16.sp)
        Text(
            card.subtitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun ArtworkCard(
    title: String,
    subtitle: String,
    artwork: Uri?,
    badge: String = "",
    selectionVisible: Boolean = false,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
    artworkModifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Box(
            modifier =
                artworkModifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            AlbumArt(artwork, Modifier.fillMaxSize(), RoundedCornerShape(18.dp))
            if (badge.isNotBlank()) {
                Surface(
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            if (title.contains("Favorite", ignoreCase = true)) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.28f)),
                )
                Text(title, color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center)
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = selectionVisible,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                enter = fadeIn(animationSpec = tween(160)) + expandHorizontally(animationSpec = tween(180), expandFrom = Alignment.End),
                exit = fadeOut(animationSpec = tween(120)) + shrinkHorizontally(animationSpec = tween(140), shrinkTowards = Alignment.End),
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                ) {
                    Icon(
                        if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        modifier =
                            Modifier
                                .padding(4.dp)
                                .size(20.dp),
                    )
                }
            }
        }
        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp), fontSize = 16.sp)
        Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    }
}

@Composable
private fun AlphabetRail(
    indexes: List<AlphabetIndex>,
    railHeight: Dp,
    onIndexSelected: (AlphabetIndex) -> Unit,
    modifier: Modifier = Modifier,
) {
    val letters = remember(indexes) { indexes.map { it.letter }.distinct() }
    val indexByLetter = remember(indexes) { indexes.associateBy { it.letter } }
    val density = LocalDensity.current
    val itemHeightPx = with(density) { (railHeight / 26).toPx() }
    var activeLetter by remember { mutableStateOf<Char?>(null) }
    var activeLetterY by remember { mutableStateOf(0f) }

    fun selectAt(y: Float) {
        val letterIndex = (y / itemHeightPx).toInt().coerceIn(0, 25)
        val letter = ('A'.code + letterIndex).toChar()
        activeLetter = letter
        activeLetterY = (letterIndex + 0.5f) * itemHeightPx
        indexByLetter[letter]?.let(onIndexSelected)
    }

    Box(modifier = modifier.padding(end = 8.dp)) {
        activeLetter?.let { letter ->
            Surface(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset {
                            val popupY = activeLetterY.roundToInt() - 32.dp.roundToPx()
                            IntOffset(x = -58.dp.roundToPx(), y = popupY)
                        }.size(52.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.72f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = letter.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Surface(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .width(24.dp)
                    .height(railHeight)
                    .pointerInput(indexByLetter, railHeight) {
                        detectDragGestures(
                            onDragStart = { offset -> selectAt(offset.y) },
                            onDragEnd = { activeLetter = null },
                            onDragCancel = { activeLetter = null },
                            onDrag = { change, _ ->
                                change.consume()
                                selectAt(change.position.y)
                            },
                        )
                    },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                ('A'..'Z').forEach { letter ->
                    val enabled = letter in letters
                    Text(
                        letter.toString(),
                        fontSize = 10.sp,
                        color =
                            if (enabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f)
                            },
                        fontWeight = if (enabled) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
    )
}

@Composable
private fun SearchPanel(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = tabContentContainerColor(),
        content = {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                content = content,
            )
        },
    )
}

@Composable
private fun SearchDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 60.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
    )
}

@Composable
private fun SearchEmpty(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = tabContentContainerColor(),
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(18.dp),
        )
    }
}

@Composable
private fun EmptyPanel(message: String?) {
    OutlinedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = tabContentContainerColor()),
        border = BorderStroke(0.dp, Color.Transparent),
    ) {
        EmptyInline(message ?: "Nothing found")
    }
}

@Composable
private fun EmptyInline(message: String) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
