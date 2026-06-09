package me.ayra.music.ui.home

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import me.ayra.music.ui.navigation.MainRoute
import me.ayra.music.ui.navigation.MusicNavigator
import me.ayra.music.ui.player.AlbumArt
import me.ayra.music.ui.settings.SettingsScreen
import me.ayra.music.util.MusicPreferences
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

enum class HomeTab(
    val label: String,
) {
    Favorite("Favorite"),
    Playlist("Playlist"),
    Track("Track"),
    Album("Album"),
    Artist("Artist"),
    Folder("Folder"),
}

private enum class TrackSort(
    val label: String,
) {
    Name("Name"),
    DateAdded("Date added"),
    Artist("Artist"),
}

private enum class AlbumSort(
    val label: String,
) {
    Release("Release"),
    Name("Name"),
    Artist("Artist"),
}

private enum class ArtistSort(
    val label: String,
) {
    Name("Name"),
    DateAdded("Date added"),
}

private enum class FolderSort(
    val label: String,
) {
    Name("Name"),
    DateAdded("Date added"),
}

private enum class PlaylistSort(
    val label: String,
) {
    CustomOrder("Custom order"),
    Name("Name"),
    Artist("Artist"),
}

private enum class PlaylistListSort(
    val label: String,
) {
    DateAdded("Date added"),
    Name("Name"),
    DatePlayed("Date played"),
}

private enum class SelectPickerTab {
    Track,
    Album,
    Artist,
    Folder,
}

private enum class SubParentSort(
    val label: String,
) {
    Name("Name"),
    DateAdded("Date added"),
}

private const val SORT_TRACK = "track"
private const val SORT_ALBUM = "album"
private const val SORT_ARTIST = "artist"
private const val SORT_FOLDER = "folder"
private const val SORT_PLAYLIST_LIST = "playlist_list"
private const val SORT_PLAYLIST_PREFIX = "playlist_"
private const val CUSTOM_PLAYLIST_PREFIX = "custom-"
private const val SORT_SUB_PARENT_PREFIX = "sub_parent_"
private const val PLAYLIST_RECENTLY_ADDED = "recently-added"
private const val PLAYLIST_MOST_PLAYED = "most-played"
private const val PLAYLIST_JUST_PLAYED = "just-played"
private const val PLAYLIST_FAVORITE_TRACK = "favorite-track"
private const val ALBUM_SNAP_EXPAND_THRESHOLD = 0.35f
private const val ALBUM_SNAP_FLING_DELTA_PX = 72
private val HOME_TAB_WIDTH = 104.dp
private val MINI_PLAYER_RESERVED_BOTTOM = 116.dp

private data class AlphabetIndex(
    val letter: Char,
    val position: Int,
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
        is MainRoute.AddToPlaylist -> "add-to-playlist:$trackId"
    }

@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen(
    library: LibraryState,
    navigator: MusicNavigator,
    currentQueue: List<Track>,
    onRequestPermission: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onToggleFavoriteItem: (String, String) -> Unit,
    onRescan: () -> Unit,
    onHiddenFoldersChanged: (Set<String>) -> Unit,
    onCreatePlaylist: (String, List<Track>) -> Unit,
    onAddTracksToPlaylist: (String, List<Track>) -> Unit,
    onAddTracksToCurrentQueue: (List<Track>) -> Unit,
    initialTabIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    val currentRoute = navigator.currentRoute
    val detailStateHolder = rememberSaveableStateHolder()
    var searchStateVersion by rememberSaveable { mutableIntStateOf(0) }
    var usePopTransition by remember { mutableStateOf(false) }

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
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            AnimatedVisibility(
                visible = currentRoute == MainRoute.Home,
                enter = fadeIn(animationSpec = tween(290)),
                exit = fadeOut(animationSpec = tween(290)),
                label = "home-host",
            ) {
                HomeScreen(
                    library = library,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
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
                    initialTabIndex = initialTabIndex,
                    onTabSelected = onTabSelected,
                    modifier = Modifier.fillMaxSize(),
                )
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
                    DetailHost(
                        route = route,
                        library = library,
                        currentQueue = currentQueue,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                        onBack = { back() },
                        onSettings = { navigate(MainRoute.Settings) },
                        onSearch = { navigate(MainRoute.Search) },
                        onTrackClick = onTrackClick,
                        onToggleFavorite = onToggleFavorite,
                        onToggleFavoriteItem = onToggleFavoriteItem,
                        onRescan = onRescan,
                        onHiddenFoldersChanged = onHiddenFoldersChanged,
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
                        modifier = Modifier.fillMaxSize(),
                    )
                }
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
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onSearch: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onToggleFavoriteItem: (String, String) -> Unit,
    onRescan: () -> Unit,
    onHiddenFoldersChanged: (Set<String>) -> Unit,
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
    modifier: Modifier = Modifier,
) {
    when (route) {
        MainRoute.Home -> {
            Box(modifier = modifier)
        }

        MainRoute.Search -> {
            SearchScreen(
                library = library,
                onBack = onBack,
                onTrackClick = onTrackClick,
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
                modifier = modifier,
            )
        }

        is MainRoute.Album -> {
            val album = library.albums.firstOrNull { it.id == route.id }
            if (album == null) {
                Box(modifier = modifier)
            } else {
                AlbumDetailScreen(
                    album = album,
                    onBack = onBack,
                    onSettings = onSettings,
                    onSearch = onSearch,
                    onTrackClick = onTrackClick,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    isFavorite = library.isFavoriteItem(FavoriteType.Album, album.id.toString()),
                    onToggleFavorite = { onToggleFavoriteItem(FavoriteType.Album, album.id.toString()) },
                    modifier = modifier,
                )
            }
        }

        is MainRoute.Artist -> {
            val artist = library.artists.firstOrNull { it.name == route.name }
            if (artist == null) {
                Box(modifier = modifier)
            } else {
                ArtistDetailScreen(
                    artist = artist,
                    albums = library.albums.filter { albumGroup -> albumGroup.tracks.any { it.artist == artist.name } },
                    onBack = onBack,
                    onSettings = onSettings,
                    onSearch = onSearch,
                    onTrackClick = onTrackClick,
                    onAlbumClick = onAlbumClick,
                    isFavorite = library.isFavoriteItem(FavoriteType.Artist, artist.name),
                    onToggleFavorite = { onToggleFavoriteItem(FavoriteType.Artist, artist.name) },
                    modifier = modifier,
                )
            }
        }

        is MainRoute.Folder -> {
            val folder = library.folders.firstOrNull { it.path == route.path }
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
                    onToggleFavorite = onToggleFavorite,
                    isFavorite = library.isFavoriteItem(FavoriteType.Folder, folder.path),
                    onToggleFolderFavorite = { onToggleFavoriteItem(FavoriteType.Folder, folder.path) },
                    modifier = modifier,
                )
            }
        }

        is MainRoute.Playlist -> {
            val playlist = library.findPlaylist(route.id)
            if (playlist == null) {
                Box(modifier = modifier)
            } else {
                PlaylistDetailScreen(
                    playlist = playlist,
                    onBack = onBack,
                    onTrackClick = onTrackClick,
                    isFavorite = library.isFavoriteItem(FavoriteType.Playlist, playlist.id),
                    onToggleFavorite = { onToggleFavoriteItem(FavoriteType.Playlist, playlist.id) },
                    onAddTracks = { onAddTracksToPlaylistRoute(playlist.id) },
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
            val playlist = library.findPlaylist(route.id)
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

        is MainRoute.AddToPlaylist -> {
            val track = library.tracks.firstOrNull { it.id == route.trackId }
            AddToPlaylistScreen(
                track = track,
                playlists = library.playlists,
                currentQueue = currentQueue,
                isFavorite = track?.id in library.favorites,
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
                    onBack()
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
    initialTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = HomeTab.entries
    val restoredTab = initialTabIndex.coerceIn(tabs.indices)
    val pagerState = rememberPagerState(initialPage = restoredTab) { tabs.size }
    val coroutineScope = rememberCoroutineScope()
    val tabListState = rememberLazyListState(initialFirstVisibleItemIndex = restoredTab)
    val density = LocalDensity.current
    val tabWidthPx = with(density) { HOME_TAB_WIDTH.roundToPx() }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        onTabSelected(pagerState.currentPage)
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
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 18.dp, end = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Music",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search), tint = MaterialTheme.colorScheme.primary)
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

        CenteredHomeTabs(
            tabs = tabs,
            pagerState = pagerState,
            listState = tabListState,
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
                ) { page ->
                    when (tabs[page]) {
                        HomeTab.Favorite -> {
                            FavoriteTab(
                                library = library,
                                onTrackClick = onTrackClick,
                                onArtistClick = onArtistClick,
                                onAlbumClick = onAlbumClick,
                                onFolderClick = onFolderClick,
                                onPlaylistClick = onPlaylistClick,
                            )
                        }

                        HomeTab.Playlist -> {
                            PlaylistTab(
                                library = library,
                                onPlaylistClick = onPlaylistClick,
                                onCreatePlaylist = onCreatePlaylist,
                            )
                        }

                        HomeTab.Track -> {
                            TrackTab(library, onTrackClick)
                        }

                        HomeTab.Album -> {
                            AlbumTab(
                                library = library,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                onAlbumClick = onAlbumClick,
                            )
                        }

                        HomeTab.Artist -> {
                            ArtistTab(library, onArtistClick)
                        }

                        HomeTab.Folder -> {
                            FolderTab(library, onFolderClick)
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
    onTabClick: (Int) -> Unit,
) {
    val pagerPosition = pagerState.currentPage + pagerState.currentPageOffsetFraction
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val horizontalPadding = ((maxWidth - HOME_TAB_WIDTH) / 2).coerceAtLeast(0.dp)
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
        ) {
            itemsIndexed(tabs, key = { _, tab -> tab.label }) { index, tab ->
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
                            .clickable { onTabClick(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tab.label,
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
                .background(MaterialTheme.colorScheme.background),
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
                .background(MaterialTheme.colorScheme.background),
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
                    label = "${trackResults.size} tracks",
                    onPlay = {
                        trackResults.firstOrNull()?.let {
                            onTrackClick(it, trackResults)
                        }
                    },
                    onShuffle = {
                        val shuffled = trackResults.shuffled()
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
        item { SortHeader("${artistResults.size} artists") }
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
        item { SortHeader("${albumResults.size} albums") }
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
                .background(MaterialTheme.colorScheme.background),
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
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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
private fun FavoriteTab(
    library: LibraryState,
    onTrackClick: (Track, List<Track>) -> Unit,
    onArtistClick: (ArtistGroup) -> Unit,
    onAlbumClick: (AlbumGroup) -> Unit,
    onFolderClick: (FolderGroup) -> Unit,
    onPlaylistClick: (PlaylistGroup) -> Unit,
) {
    val favoriteCards =
        remember(library.favoriteItems, library.favoriteTracks, library.artists, library.albums, library.folders, library.playlists) {
            library.favoriteCards()
        }
    if (favoriteCards.isEmpty()) {
        EmptyPanel(stringResource(R.string.favorite_empty))
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier =
            Modifier
                .fillMaxSize()
                .padding(top = 14.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
        contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 116.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) { SortHeader("Favorite date") }
        items(favoriteCards, key = { "${it.type}:${it.key}" }) { card ->
            FavoriteGridCard(
                card = card,
                modifier =
                    Modifier
                        .animateItem()
                        .clickable {
                            when (card.type) {
                                FavoriteType.Track -> card.tracks.firstOrNull()?.let { onTrackClick(it, card.tracks) }
                                FavoriteType.Artist -> card.artist?.let(onArtistClick)
                                FavoriteType.Folder -> card.folder?.let(onFolderClick)
                                FavoriteType.Album -> card.album?.let(onAlbumClick)
                                FavoriteType.Playlist -> card.playlist?.let(onPlaylistClick)
                            }
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
) {
    var createDialog by rememberSaveable { mutableStateOf(false) }
    var playlistName by rememberSaveable { mutableStateOf("") }
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
    if (library.tracks.isEmpty()) {
        EmptyPanel(stringResource(R.string.playlist_empty))
        return
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
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(top = 14.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
        contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 116.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SortHeader(
                    label = listSort.label,
                    modifier = Modifier.weight(1f),
                    options = PlaylistListSort.entries.map { it.label },
                    onOptionSelected = { label ->
                        PlaylistListSort.entries.firstOrNull { it.label == label }?.let {
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
        if (customPlaylists.isEmpty()) {
            item { EmptyInline(stringResource(R.string.no_custom_playlists_found)) }
        } else {
            itemsIndexed(customPlaylists, key = { _, playlist -> playlist.id }) { index, playlist ->
                PlaylistRowWithDivider(
                    playlist = playlist,
                    showDivider = index != customPlaylists.lastIndex,
                    onClick = { onPlaylistClick(playlist) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun PlaylistDetailScreen(
    playlist: PlaylistGroup,
    onBack: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onAddTracks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val preferences = rememberSortPreferences()
    var sort by rememberSaveable(playlist.id) {
        mutableStateOf(preferences.loadEnumSort(SORT_PLAYLIST_PREFIX + playlist.id, PlaylistSort.CustomOrder, PlaylistSort.entries))
    }
    val tracks =
        remember(playlist.tracks, sort) {
            when (sort) {
                PlaylistSort.CustomOrder -> {
                    playlist.tracks
                }

                PlaylistSort.Name -> {
                    playlist.tracks.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
                }

                PlaylistSort.Artist -> {
                    playlist.tracks.sortedWith(
                        compareBy<Track, String>(String.CASE_INSENSITIVE_ORDER) { it.artist }
                            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title },
                    )
                }
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
                .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 72.dp, bottom = MINI_PLAYER_RESERVED_BOTTOM),
        ) {
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
                }
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Column(
                        modifier = Modifier.padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 8.dp),
                    ) {
                        SortHeader(
                            label = sort.label,
                            options = PlaylistSort.entries.map { it.label },
                            onOptionSelected = { label ->
                                PlaylistSort.entries.firstOrNull { it.label == label }?.let {
                                    sort = it
                                    preferences.saveSort(SORT_PLAYLIST_PREFIX + playlist.id, it.name)
                                }
                            },
                        )
                        if (tracks.isEmpty()) {
                            EmptyInline(stringResource(R.string.no_tracks_found))
                        }
                    }
                }
            }
            itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
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
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    PlaylistTrackRow(
                        track = track,
                        showDivider = index != tracks.lastIndex,
                        onClick = { onTrackClick(track, tracks) },
                        modifier =
                            Modifier
                                .padding(horizontal = 20.dp)
                                .padding(bottom = if (index == tracks.lastIndex) 14.dp else 0.dp),
                    )
                }
            }
        }

        Row(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 8.dp, top = 18.dp, end = 8.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = playlist.title,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .weight(1f)
                        .alpha(if (showPinnedTitle) 1f else 0f),
            )
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(R.string.favorite_playlist),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onAddTracks) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_tracks), tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.playlist_menu),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
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
    val albums = remember(tracks) {
        tracks
            .groupBy { it.albumId }
            .map { (_, items) -> AlbumGroup(items.first().albumId, items.first().album, items.first().artist, items) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
    }
    val artists = remember(tracks) {
        tracks
            .groupBy { it.artist.ifBlank { "Unknown artist" } }
            .map { (name, items) -> ArtistGroup(name, items.map { it.albumId }.distinct().size, items) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    }
    val folders = remember(tracks) {
        tracks
            .groupBy { it.folder.ifBlank { "Unknown folder" } }
            .map { (path, items) -> FolderGroup(path.substringAfterLast('/').ifBlank { path }, path, items) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    }
    val detailTracks =
        remember(detailRoute, albums, artists, folders) {
            when {
                detailRoute?.startsWith("album:") == true -> {
                    val albumId = detailRoute?.removePrefix("album:")?.toLongOrNull()
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
                val albumId = detailRoute?.removePrefix("album:")?.toLongOrNull()
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
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 18.dp, end = 8.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { if (detailRoute != null) detailRoute = null else onBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(detailTitle ?: title, color = MaterialTheme.colorScheme.primary, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1)
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
                modifier = Modifier.clickable { onSelected(tab) },
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
    track: Track?,
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
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
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
                    track?.title ?: stringResource(R.string.no_track_selected),
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
                    subtitle = track?.title ?: stringResource(R.string.no_track_selected),
                    enabled = track != null,
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
                    enabled = track != null,
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
                    enabled = track != null,
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

private fun LibraryState.favoriteCards(): List<FavoriteCardItem> {
    val cards = mutableListOf<FavoriteCardItem>()
    val trackAddedAt =
        favoriteItems
            .filter { it.type == FavoriteType.Track }
            .maxOfOrNull { it.addedAt }
    if (favoriteTracks.isNotEmpty()) {
        cards +=
            FavoriteCardItem(
                type = FavoriteType.Track,
                key = "favorite-tracks",
                title = "Favorite track",
                subtitle = "${favoriteTracks.size} tracks",
                artwork = favoriteTracks.firstOrNull()?.albumArtUri,
                tracks = favoriteTracks,
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
                favorite.key
                    .toLongOrNull()
                    ?.let { albumId -> albums.firstOrNull { it.id == albumId } }
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
            .mapNotNull { favorite -> favorite.key.toLongOrNull()?.let { trackId -> tracks.firstOrNull { it.id == trackId } } }
    return listOf(
        PlaylistGroup(PLAYLIST_RECENTLY_ADDED, "Recently added", recentlyAdded, recentlyAdded.firstOrNull()?.albumArtUri),
        PlaylistGroup(PLAYLIST_MOST_PLAYED, "Most played", mostPlayed, mostPlayed.firstOrNull()?.albumArtUri),
        PlaylistGroup(PLAYLIST_JUST_PLAYED, "Just played", justPlayed, justPlayed.firstOrNull()?.albumArtUri),
        PlaylistGroup(PLAYLIST_FAVORITE_TRACK, "Favorite track", favoriteTracksByDate, favoriteTracksByDate.firstOrNull()?.albumArtUri),
    )
}

private fun LibraryState.findPlaylist(id: String): PlaylistGroup? =
    smartPlaylists().firstOrNull { it.id == id }
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
) {
    val listState = rememberLazyListState()
    val preferences = rememberSortPreferences()
    var sort by rememberSaveable {
        mutableStateOf(preferences.loadEnumSort(SORT_TRACK, TrackSort.Name, TrackSort.entries))
    }
    val tracks = remember(library.tracks, sort) { library.tracks.sortedBy(sort) }
    val alphabetIndexes =
        remember(tracks, sort) {
            if (sort == TrackSort.Name) tracks.alphabetIndexes(positionOffset = 1) { it.title } else emptyList()
        }
    IndexedListWithRail(listState = listState, alphabetIndexes = alphabetIndexes) {
        item {
            SortHeader(
                label = sort.label,
                options = TrackSort.entries.map { it.label },
                onOptionSelected = { label ->
                    TrackSort.entries.firstOrNull { it.label == label }?.let {
                        sort = it
                        preferences.saveSort(SORT_TRACK, it.name)
                    }
                },
            )
        }
        items(tracks, key = { it.id }) { track ->
            TrackRow(
                track = track,
                onClick = { onTrackClick(track, tracks) },
                modifier = Modifier.animateItem(),
            )
        }
        if (tracks.isEmpty()) item { EmptyInline(stringResource(R.string.no_tracks_found)) }
    }
}

@Composable
private fun AlbumTab(
    library: LibraryState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onAlbumClick: (AlbumGroup) -> Unit,
) {
    val preferences = rememberSortPreferences()
    var sort by rememberSaveable {
        mutableStateOf(preferences.loadEnumSort(SORT_ALBUM, AlbumSort.Release, AlbumSort.entries))
    }
    val albums = remember(library.albums, sort) { library.albums.sortedBy(sort) }
    RoundedGridPanel {
        item(span = { GridItemSpan(maxLineSpan) }) {
            SortHeader(
                label = sort.label,
                options = AlbumSort.entries.map { it.label },
                onOptionSelected = { label ->
                    AlbumSort.entries.firstOrNull { it.label == label }?.let {
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
                        .clickable { onAlbumClick(album) },
            )
        }
        if (albums.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { EmptyInline(stringResource(R.string.no_albums_found)) }
        }
    }
}

@Composable
private fun AlbumDetailScreen(
    album: AlbumGroup,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onSearch: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
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
                .background(MaterialTheme.colorScheme.background),
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
                    onTrackClick = onTrackClick,
                )
            }
            if (albumTracks.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
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
                                color = MaterialTheme.colorScheme.surfaceContainer,
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
                            color = MaterialTheme.colorScheme.surfaceContainer,
                        ) {
                            Column(
                                modifier =
                                    Modifier
                                        .padding(horizontal = 20.dp)
                                        .padding(bottom = if (isLastTrack) 14.dp else 0.dp),
                            ) {
                                AlbumTrackRow(
                                    index = index + 1,
                                    track = track,
                                    showDivider = !isLastTrack,
                                    onClick = { onTrackClick(track, albumTracks) },
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
                .background(MaterialTheme.colorScheme.background)
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
            text = "${albumTracks.size} tracks | ${totalDurationMs.formatDuration()}",
            modifier = Modifier.padding(top = 8.dp, bottom = 26.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
        )
    }
}

@Composable
private fun AlbumTrackListHeader(
    albumTracks: List<Track>,
    albumTrackGroups: List<AlbumTrackGroup>,
    subParentSort: SubParentSort,
    onSubParentSortSelected: (SubParentSort) -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 8.dp),
        ) {
            AlbumTrackListActions(
                albumTracks = albumTracks,
                onTrackClick = onTrackClick,
            )
            if (albumTrackGroups.size > 1) {
                SortHeader(
                    label = subParentSort.label,
                    options = SubParentSort.entries.map { it.label },
                    onOptionSelected = { label ->
                        SubParentSort.entries.firstOrNull { it.label == label }?.let(onSubParentSortSelected)
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
    onTrackClick: (Track, List<Track>) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
            IconButton(
                onClick = {
                    val shuffledTracks = albumTracks.shuffled()
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
) {
    val listState = rememberLazyListState()
    val preferences = rememberSortPreferences()
    var sort by rememberSaveable {
        mutableStateOf(preferences.loadEnumSort(SORT_ARTIST, ArtistSort.Name, ArtistSort.entries))
    }
    val artists = remember(library.artists, sort) { library.artists.sortedBy(sort) }
    val alphabetIndexes =
        remember(artists, sort) {
            if (sort == ArtistSort.Name) artists.alphabetIndexes(positionOffset = 1) { it.name } else emptyList()
        }
    IndexedListWithRail(listState = listState, alphabetIndexes = alphabetIndexes) {
        item {
            SortHeader(
                label = sort.label,
                options = ArtistSort.entries.map { it.label },
                onOptionSelected = { label ->
                    ArtistSort.entries.firstOrNull { it.label == label }?.let {
                        sort = it
                        preferences.saveSort(SORT_ARTIST, it.name)
                    }
                },
            )
        }
        items(artists, key = { it.name }) { artist ->
            MediaGroupRow(
                artwork = artist.tracks.firstOrNull()?.albumArtUri,
                title = artist.name,
                subtitle = "${artist.albums} albums | ${artist.tracks.size} tracks",
                onClick = { onArtistClick(artist) },
                modifier = Modifier.animateItem(),
            )
        }
        if (artists.isEmpty()) item { EmptyInline(stringResource(R.string.no_artists_found)) }
    }
}

private enum class ArtistDetailTab(
    val label: String,
) {
    Track("Track"),
    Album("Album"),
}

@Composable
private fun ArtistDetailScreen(
    artist: ArtistGroup,
    albums: List<AlbumGroup>,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onSearch: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
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
                .background(MaterialTheme.colorScheme.background),
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
                        text = "${tab.label} (${if (tab == ArtistDetailTab.Track) artistTracks.size else artistAlbums.size})",
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
                        onTrackClick = onTrackClick,
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
    onTrackClick: (Track, List<Track>) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
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
                            val shuffledTracks = tracks.shuffled()
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
                        index = index + 1,
                        track = track,
                        showDivider = !isLastTrack,
                        onClick = { onTrackClick(track, albumTracks) },
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
                .background(MaterialTheme.colorScheme.surfaceContainer),
        contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 116.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) { SortHeader("Release") }
        items(albums, key = { it.id }) { album ->
            ArtworkCard(
                title = album.title,
                subtitle = album.yearLabel(),
                artwork = album.tracks.firstOrNull()?.albumArtUri,
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
) {
    val listState = rememberLazyListState()
    val preferences = rememberSortPreferences()
    var sort by rememberSaveable {
        mutableStateOf(preferences.loadEnumSort(SORT_FOLDER, FolderSort.Name, FolderSort.entries))
    }
    val folders = remember(library.folders, sort) { library.folders.sortedBy(sort) }
    val alphabetIndexes =
        remember(folders, sort) {
            if (sort == FolderSort.Name) folders.alphabetIndexes(positionOffset = 1) { it.name } else emptyList()
        }
    IndexedListWithRail(listState = listState, alphabetIndexes = alphabetIndexes) {
        item {
            SortHeader(
                label = sort.label,
                options = FolderSort.entries.map { it.label },
                onOptionSelected = { label ->
                    FolderSort.entries.firstOrNull { it.label == label }?.let {
                        sort = it
                        preferences.saveSort(SORT_FOLDER, it.name)
                    }
                },
            )
        }
        items(folders, key = { it.path }) { folder ->
            MediaGroupRow(
                artwork = folder.tracks.firstOrNull()?.albumArtUri,
                title = folder.name,
                subtitle = folder.path,
                folder = true,
                onClick = { onFolderClick(folder) },
                modifier = Modifier.animateItem(),
            )
        }
        if (folders.isEmpty()) item { EmptyInline("No folders found") }
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
                    .background(MaterialTheme.colorScheme.background)
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
                    label = "Name",
                    onShuffle = { folder.tracks.firstOrNull()?.let { onTrackClick(it, folder.tracks) } },
                    onPlay = { folder.tracks.firstOrNull()?.let { onTrackClick(it, folder.tracks) } },
                )
            }
            items(folder.tracks, key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    onClick = { onTrackClick(track, folder.tracks) },
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
                .background(MaterialTheme.colorScheme.surfaceContainer),
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
private fun RoundedGridPanel(content: LazyGridScope.() -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier =
            Modifier
                .fillMaxSize()
                .padding(top = 14.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 116.dp),
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
) {
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
            IconButton(onClick = { }, modifier = Modifier.size(42.dp)) {
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
    groupBy { it.albumId }
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

private fun albumSharedKey(albumId: Long): String = "album-art-$albumId"

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
private fun TrackRow(
    track: Track,
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
        IconButton(onClick = { }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.track_menu))
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
private fun MediaGroupRow(
    artwork: Uri?,
    title: String,
    subtitle: String,
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
private fun PlaylistTrackRow(
    track: Track,
    showDivider: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TrackRow(
            track = track,
            onClick = onClick,
        )
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 66.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            )
        }
    }
}

@Composable
private fun PlaylistRowWithDivider(
    playlist: PlaylistGroup,
    showDivider: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PlaylistRow(
            playlist = playlist,
            onClick = onClick,
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
private fun PlaylistRow(
    playlist: PlaylistGroup,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
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
private fun FavoriteGridCard(
    card: FavoriteCardItem,
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
            if (title.contains("Favorite", ignoreCase = true)) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.28f)),
                )
                Text(title, color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center)
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
        color = MaterialTheme.colorScheme.surfaceContainer,
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
        color = MaterialTheme.colorScheme.surfaceContainer,
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
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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
