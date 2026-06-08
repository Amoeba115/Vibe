package me.ayra.music.ui.home

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.ayra.music.AlbumGroup
import me.ayra.music.ArtistGroup
import me.ayra.music.FolderGroup
import me.ayra.music.LibraryState
import me.ayra.music.Track
import me.ayra.music.ui.player.AlbumArt

enum class HomeTab(val label: String) {
    Favorite("Favorite"),
    Playlist("Playlist"),
    Track("Track"),
    Album("Album"),
    Artist("Artist"),
    Folder("Folder"),
}

private const val ROUTE_HOME = "home"
private const val ROUTE_SEARCH = "search"
private const val ROUTE_FOLDER_PREFIX = "folder:"
private const val ROUTE_ALBUM_PREFIX = "album:"
private const val ROUTE_ARTIST_PREFIX = "artist:"

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    library: LibraryState,
    onRequestPermission: () -> Unit,
    onSettings: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    initialTabIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    var route by rememberSaveable { mutableStateOf(ROUTE_HOME) }
    BackHandler(enabled = route != ROUTE_HOME) {
        route = ROUTE_HOME
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        AnimatedContent(
            targetState = route,
            transitionSpec = {
                if (targetState != ROUTE_HOME) {
                    modernEnter() togetherWith modernExit()
                } else {
                    modernPopEnter() togetherWith modernPopExit()
                }.using(SizeTransform(clip = false))
            },
            label = "content-nav",
        ) { currentRoute ->
            when (currentRoute) {
                ROUTE_HOME -> HomeScreen(
                    library = library,
                    onRequestPermission = onRequestPermission,
                    onSettings = onSettings,
                    onTrackClick = onTrackClick,
                    onToggleFavorite = onToggleFavorite,
                    onFolderClick = { route = ROUTE_FOLDER_PREFIX + it.path },
                    onAlbumClick = { route = ROUTE_ALBUM_PREFIX + it.id },
                    onArtistClick = { route = ROUTE_ARTIST_PREFIX + it.name },
                    onSearch = { route = ROUTE_SEARCH },
                    initialTabIndex = initialTabIndex,
                    onTabSelected = onTabSelected,
                    modifier = Modifier.fillMaxSize(),
                )

                ROUTE_SEARCH -> SearchScreen(
                    library = library,
                    onBack = { route = ROUTE_HOME },
                    onTrackClick = onTrackClick,
                    modifier = Modifier.fillMaxSize(),
                )

                else -> {
                    val albumId = currentRoute.takeIf { it.startsWith(ROUTE_ALBUM_PREFIX) }
                        ?.removePrefix(ROUTE_ALBUM_PREFIX)
                        ?.toLongOrNull()
                    val album = albumId?.let { id -> library.albums.firstOrNull { it.id == id } }
                    val folderPath = currentRoute.takeIf { it.startsWith(ROUTE_FOLDER_PREFIX) }
                        ?.removePrefix(ROUTE_FOLDER_PREFIX)
                    val folder = folderPath?.let { path -> library.folders.firstOrNull { it.path == path } }
                    val artistName = currentRoute.takeIf { it.startsWith(ROUTE_ARTIST_PREFIX) }
                        ?.removePrefix(ROUTE_ARTIST_PREFIX)
                    val artist = artistName?.let { name -> library.artists.firstOrNull { it.name == name } }
                    when {
                        album != null -> AlbumDetailScreen(
                            album = album,
                            onBack = { route = ROUTE_HOME },
                            onSettings = onSettings,
                            onSearch = { route = ROUTE_SEARCH },
                            onTrackClick = onTrackClick,
                            modifier = Modifier.fillMaxSize(),
                        )

                        artist != null -> ArtistDetailScreen(
                            artist = artist,
                            albums = library.albums
                                .filter { albumGroup -> albumGroup.tracks.any { it.artist == artist.name } },
                            onBack = { route = ROUTE_HOME },
                            onSettings = onSettings,
                            onSearch = { route = ROUTE_SEARCH },
                            onTrackClick = onTrackClick,
                            onAlbumClick = { route = ROUTE_ALBUM_PREFIX + it.id },
                            modifier = Modifier.fillMaxSize(),
                        )

                        folder != null -> {
                        FolderDetailScreen(
                            folder = folder,
                            favorites = library.favorites,
                            onBack = { route = ROUTE_HOME },
                            onSettings = onSettings,
                            onSearch = { route = ROUTE_SEARCH },
                            onTrackClick = onTrackClick,
                            onToggleFavorite = onToggleFavorite,
                            modifier = Modifier.fillMaxSize(),
                        )
                        }

                        else -> {
                        HomeScreen(
                            library = library,
                            onRequestPermission = onRequestPermission,
                            onSettings = onSettings,
                            onTrackClick = onTrackClick,
                            onToggleFavorite = onToggleFavorite,
                            onFolderClick = { route = ROUTE_FOLDER_PREFIX + it.path },
                            onAlbumClick = { route = ROUTE_ALBUM_PREFIX + it.id },
                            onArtistClick = { route = ROUTE_ARTIST_PREFIX + it.name },
                            onSearch = { route = ROUTE_SEARCH },
                            initialTabIndex = initialTabIndex,
                            onTabSelected = onTabSelected,
                            modifier = Modifier.fillMaxSize(),
                        )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    library: LibraryState,
    onRequestPermission: () -> Unit,
    onSettings: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onFolderClick: (FolderGroup) -> Unit,
    onAlbumClick: (AlbumGroup) -> Unit,
    onArtistClick: (ArtistGroup) -> Unit,
    onSearch: () -> Unit,
    initialTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = HomeTab.entries
    val restoredTab = initialTabIndex.coerceIn(tabs.indices)
    val pagerState = rememberPagerState(initialPage = restoredTab) { tabs.size }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        onTabSelected(pagerState.currentPage)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
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
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val centeredTabEdge = (maxWidth / 2 - 44.dp).coerceAtLeast(0.dp)
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = centeredTabEdge,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {},
                modifier = Modifier.fillMaxWidth(),
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            onTabSelected(index)
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = {
                            Text(
                                text = tab.label,
                                fontSize = if (pagerState.currentPage == index) 20.sp else 16.sp,
                                fontWeight = if (pagerState.currentPage == index) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        },
                    )
                }
            }
        }

        when {
            !library.permissionGranted && library.tracks.isEmpty() -> PermissionState(onRequestPermission)
            library.loading -> LoadingState()
            library.error != null -> EmptyPanel(library.error)
            else -> HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (tabs[page]) {
                    HomeTab.Favorite -> FavoriteTab(library, onTrackClick)
                    HomeTab.Playlist -> PlaylistTab(library, onTrackClick)
                    HomeTab.Track -> TrackTab(library, onTrackClick)
                    HomeTab.Album -> AlbumTab(library, onAlbumClick)
                    HomeTab.Artist -> ArtistTab(library, onArtistClick)
                    HomeTab.Folder -> FolderTab(library, onFolderClick)
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
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val normalizedQuery = query.trim()
    val trackResults = if (normalizedQuery.isBlank()) {
        emptyList()
    } else {
        library.tracks.filter {
            it.title.contains(normalizedQuery, ignoreCase = true) ||
                it.artist.contains(normalizedQuery, ignoreCase = true) ||
                it.album.contains(normalizedQuery, ignoreCase = true)
        }
    }
    val artistResults = if (normalizedQuery.isBlank()) {
        emptyList()
    } else {
        library.artists.filter { it.name.contains(normalizedQuery, ignoreCase = true) }
    }
    val albumResults = if (normalizedQuery.isBlank()) {
        emptyList()
    } else {
        library.albums.filter {
            it.title.contains(normalizedQuery, ignoreCase = true) ||
                it.artist.contains(normalizedQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 18.dp, end = 8.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
            }
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                ),
                placeholder = {
                    Text("Search", color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
            IconButton(onClick = { if (query.isBlank()) onBack() else query = "" }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.primary)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (normalizedQuery.isBlank()) {
                item { EmptyInline("Search tracks, artists, and albums") }
            } else {
                item { SectionTitle("Track (${trackResults.size})") }
                if (trackResults.isNotEmpty()) {
                    item {
                        SearchPanel {
                            trackResults.take(4).forEach { track ->
                                TrackRow(
                                    track = track,
                                    onClick = { onTrackClick(track, trackResults) },
                                )
                            }
                            if (trackResults.size > 4) {
                                Text(
                                    "Show all",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                } else {
                    item { SearchEmpty("No tracks found") }
                }

                item { SectionTitle("Artist (${artistResults.size})") }
                if (artistResults.isNotEmpty()) {
                    items(artistResults.take(3), key = { it.name }) { artist ->
                        SearchPanel {
                            MediaGroupRow(
                                artwork = artist.tracks.firstOrNull()?.albumArtUri,
                                title = artist.name,
                                subtitle = "${artist.albums} albums ${artist.tracks.size} tracks",
                                onClick = { artist.tracks.firstOrNull()?.let { onTrackClick(it, artist.tracks) } },
                            )
                        }
                    }
                } else {
                    item { SearchEmpty("No artists found") }
                }

                item { SectionTitle("Album (${albumResults.size})") }
                if (albumResults.isNotEmpty()) {
                    item {
                        SearchPanel {
                            albumResults.take(4).forEach { album ->
                                MediaGroupRow(
                                    artwork = album.tracks.firstOrNull()?.albumArtUri,
                                    title = album.title,
                                    subtitle = album.artist,
                                    onClick = { album.tracks.firstOrNull()?.let { onTrackClick(it, album.tracks) } },
                                )
                            }
                        }
                    }
                } else {
                    item { SearchEmpty("No albums found") }
                }
            }
        }
    }
}

@Composable
private fun PermissionState(onRequestPermission: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Default.LibraryMusic, contentDescription = null, modifier = Modifier.size(38.dp), tint = MaterialTheme.colorScheme.primary)
            Text("Allow music access", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Music needs audio permission to load tracks from your device.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onRequestPermission) {
                Text("Grant permission")
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Text("Scanning local music", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FavoriteTab(library: LibraryState, onTrackClick: (Track, List<Track>) -> Unit) {
    val tracks = library.favoriteTracks
    if (tracks.isEmpty()) {
        EmptyPanel("Favorite tracks will appear here.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 116.dp),
    ) {
        item {
            ArtworkCard(
                title = "Favorite tracks",
                subtitle = "${tracks.size} tracks",
                artwork = tracks.firstOrNull()?.albumArtUri,
                modifier = Modifier
                    .fillMaxWidth(0.52f)
                    .clickable { onTrackClick(tracks.first(), tracks) },
            )
        }
    }
}

@Composable
private fun PlaylistTab(library: LibraryState, onTrackClick: (Track, List<Track>) -> Unit) {
    val playlists = library.playlists
    if (library.tracks.isEmpty()) {
        EmptyPanel("Create playlists and they will appear here.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 116.dp),
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(playlists) { playlist ->
                    ArtworkCard(
                        title = playlist.title,
                        subtitle = "${playlist.tracks.size} tracks",
                        artwork = playlist.artwork,
                        modifier = Modifier
                            .width(156.dp)
                            .clickable { playlist.tracks.firstOrNull()?.let { onTrackClick(it, playlist.tracks) } },
                    )
                }
            }
        }
        item {
            Text(
                "Create playlists, and playlists will appear here.",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}

@Composable
private fun TrackTab(
    library: LibraryState,
    onTrackClick: (Track, List<Track>) -> Unit,
) {
    val listState = rememberLazyListState()
    IndexedListWithRail(listState = listState) {
        item { SortHeader("Name") }
        items(library.tracks, key = { it.id }) { track ->
            TrackRow(
                track = track,
                onClick = { onTrackClick(track, library.tracks) },
            )
        }
        if (library.tracks.isEmpty()) item { EmptyInline("No tracks found") }
    }
}

@Composable
private fun AlbumTab(library: LibraryState, onAlbumClick: (AlbumGroup) -> Unit) {
    RoundedGridPanel {
        item(span = { GridItemSpan(maxLineSpan) }) { SortHeader("Release") }
        items(library.albums, key = { it.id }) { album ->
            ArtworkCard(
                title = album.title,
                subtitle = "${album.artist} | ${album.tracks.size} tracks",
                artwork = album.tracks.firstOrNull()?.albumArtUri,
                modifier = Modifier.clickable { onAlbumClick(album) },
            )
        }
        if (library.albums.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { EmptyInline("No albums found") }
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
    modifier: Modifier = Modifier,
) {
    val albumTracks = remember(album.tracks) { album.tracks.sortedForAlbumPlayback() }
    val totalDurationMs = albumTracks.sumOf { it.durationMs.coerceAtLeast(0L) }
    val albumTrackGroups = remember(albumTracks) { albumTracks.groupForAlbumDetail() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showPinnedTitle by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 210
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) return@LaunchedEffect
        if (listState.firstVisibleItemIndex == 0) {
            val offset = listState.firstVisibleItemScrollOffset
            when {
                offset in 80..260 -> coroutineScope.launch { listState.animateScrollToItem(1) }
                offset in 1..79 -> coroutineScope.launch { listState.animateScrollToItem(0) }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 72.dp, bottom = 116.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AlbumArt(
                        albumTracks.firstOrNull()?.albumArtUri,
                        Modifier
                            .fillMaxWidth(0.46f)
                            .aspectRatio(1f),
                        RoundedCornerShape(24.dp),
                    )
                    Text(
                        text = album.title,
                        modifier = Modifier.padding(top = 24.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 27.sp,
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

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Column(
                        modifier = Modifier.padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 18.dp),
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
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(Icons.Default.Shuffle, contentDescription = "Shuffle")
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 12.dp),
                            ) {
                                IconButton(
                                    onClick = { albumTracks.firstOrNull()?.let { onTrackClick(it, albumTracks) } },
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                        albumTrackGroups.forEachIndexed { groupIndex, group ->
                            if (albumTrackGroups.size > 1) {
                                Text(
                                    text = group.name,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = if (groupIndex == 0) 18.dp else 26.dp, bottom = 8.dp),
                                )
                            }
                            group.tracks.forEachIndexed { index, track ->
                                val isLastTrack = groupIndex == albumTrackGroups.lastIndex && index == group.tracks.lastIndex
                                AlbumTrackRow(
                                    index = index + 1,
                                    track = track,
                                    showDivider = !isLastTrack,
                                    onClick = { onTrackClick(track, albumTracks) },
                                )
                            }
                        }
                        if (albumTracks.isEmpty()) EmptyInline("No tracks found")
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.96f))
                .padding(start = 8.dp, top = 18.dp, end = 8.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = album.title,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (showPinnedTitle) 1f else 0f),
            )
            IconButton(onClick = { }) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite album", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ArtistTab(library: LibraryState, onArtistClick: (ArtistGroup) -> Unit) {
    val listState = rememberLazyListState()
    IndexedListWithRail(listState = listState) {
        item { SortHeader("Name") }
        items(library.artists, key = { it.name }) { artist ->
            MediaGroupRow(
                artwork = artist.tracks.firstOrNull()?.albumArtUri,
                title = artist.name,
                subtitle = "${artist.albums} albums | ${artist.tracks.size} tracks",
                onClick = { onArtistClick(artist) },
            )
        }
        if (library.artists.isEmpty()) item { EmptyInline("No artists found") }
    }
}

private enum class ArtistDetailTab(val label: String) {
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
    modifier: Modifier = Modifier,
) {
    val tabs = ArtistDetailTab.entries
    val pagerState = rememberPagerState(initialPage = 0) { tabs.size }
    val coroutineScope = rememberCoroutineScope()
    val artistTracks = remember(artist.tracks) { artist.tracks.sortedForArtistPlayback() }
    val artistAlbums = remember(albums) {
        albums.sortedWith(compareBy<AlbumGroup>({ it.releaseYear().takeIf { year -> year > 0 } ?: Int.MAX_VALUE }, { it.title.lowercase() }))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 18.dp, end = 8.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = artist.name,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { }) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite artist", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = pagerState.currentPage == index
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .clickable {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                    shape = RoundedCornerShape(28.dp),
                    color = if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                ) {
                    Text(
                        text = "${tab.label} (${if (tab == ArtistDetailTab.Track) artistTracks.size else artistAlbums.size})",
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 18.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 14.dp),
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (tabs[page]) {
                ArtistDetailTab.Track -> ArtistTrackTab(
                    albums = artistAlbums,
                    tracks = artistTracks,
                    onTrackClick = onTrackClick,
                )

                ArtistDetailTab.Album -> ArtistAlbumTab(
                    albums = artistAlbums,
                    onAlbumClick = onAlbumClick,
                )
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
        modifier = Modifier
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
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle")
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 12.dp),
                ) {
                    IconButton(
                        onClick = { tracks.firstOrNull()?.let { onTrackClick(it, tracks) } },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.onPrimary)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = if (groupIndex == 0) 12.dp else 24.dp, bottom = 8.dp),
                        )
                    }
                }
                items(group.tracks, key = { "artist-track-${album.id}-${it.id}" }) { track ->
                    val index = group.tracks.indexOf(track)
                    val isLastTrack = album == albums.lastOrNull() &&
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

        if (tracks.isEmpty()) item { EmptyInline("No tracks found") }
    }
}

@Composable
private fun ArtistAlbumHeader(album: AlbumGroup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlbumArt(album.tracks.firstOrNull()?.albumArtUri, Modifier.size(88.dp), RoundedCornerShape(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
        ) {
            Text(
                text = album.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 21.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = album.yearLabel(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 15.sp,
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
        modifier = Modifier
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
            item(span = { GridItemSpan(maxLineSpan) }) { EmptyInline("No albums found") }
        }
    }
}

@Composable
private fun FolderTab(library: LibraryState, onFolderClick: (FolderGroup) -> Unit) {
    val listState = rememberLazyListState()
    IndexedListWithRail(listState = listState) {
        item { SortHeader("Name") }
        items(library.folders, key = { it.path }) { folder ->
            MediaGroupRow(
                artwork = folder.tracks.firstOrNull()?.albumArtUri,
                title = folder.name,
                subtitle = folder.path,
                folder = true,
                onClick = { onFolderClick(folder) },
            )
        }
        if (library.folders.isEmpty()) item { EmptyInline("No folders found") }
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
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 18.dp, end = 8.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
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
            IconButton(onClick = { }) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite folder", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
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
            if (folder.tracks.isEmpty()) item { EmptyInline("No tracks found") }
        }
    }
}

@Composable
private fun IndexedListWithRail(
    listState: LazyListState,
    topPadding: Dp = 14.dp,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    Box {
        RoundedPanelList(listState = listState, topPadding = topPadding, content = content)
        AlphabetRail(
            modifier = Modifier
                .padding(top = topPadding + 16.dp)
                .align(Alignment.CenterEnd),
        )
    }
}

@Composable
private fun RoundedPanelList(
    listState: LazyListState,
    topPadding: Dp = 14.dp,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topPadding)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 46.dp, bottom = 116.dp),
        content = content,
    )
}

@Composable
private fun RoundedGridPanel(content: LazyGridScope.() -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
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
private fun SortHeader(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Default.Sort, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun DetailSortHeader(label: String, onShuffle: () -> Unit, onPlay: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Default.Sort, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
            IconButton(onClick = onShuffle, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Shuffle, contentDescription = "Shuffle")
            }
        }
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
            IconButton(onClick = onPlay, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
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
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = index.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(34.dp),
            )
            Text(
                text = track.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 20.sp,
                modifier = Modifier
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
                Icon(Icons.Default.MoreVert, contentDescription = "Track menu")
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

private fun List<Track>.sortedForAlbumPlayback(): List<Track> =
    groupForAlbumDetail().flatMap { it.tracks }

private fun List<Track>.sortedForArtistPlayback(): List<Track> =
    groupBy { it.albumId }
        .values
        .sortedWith(
            compareBy<List<Track>>(
                { tracks -> tracks.mapNotNull { it.year.takeIf { year -> year > 0 } }.minOrNull() ?: Int.MAX_VALUE },
                { tracks -> tracks.firstOrNull()?.album?.lowercase().orEmpty() },
            ),
        )
        .flatMap { it.sortedForAlbumPlayback() }

private fun AlbumGroup.releaseYear(): Int =
    tracks.mapNotNull { it.year.takeIf { year -> year > 0 } }.minOrNull() ?: 0

private fun AlbumGroup.yearLabel(): String =
    releaseYear().takeIf { it > 0 }?.toString() ?: "${tracks.size} tracks"

private fun List<Track>.groupForAlbumDetail(): List<AlbumTrackGroup> {
    if (any { it.discNumber > 0 }) {
        return groupBy { it.discNumber.takeIf { disc -> disc > 0 } ?: 1 }
            .map { (discNumber, tracks) ->
                AlbumTrackGroup(
                    name = "Disc $discNumber",
                    tracks = tracks.sortedWith(albumTrackComparator()),
                    discNumber = discNumber,
                )
            }
            .sortedBy { it.discNumber }
    }
    return groupBy { it.folder.ifBlank { "Unknown folder" } }
        .map { (folder, tracks) ->
            AlbumTrackGroup(
                name = folder.displayFolderName(),
                tracks = tracks.sortedWith(albumTrackComparator()),
            )
        }
        .sortedWith { first, second -> first.name.compareNaturally(second.name) }
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

private fun String.displayFolderName(): String =
    substringAfterLast('/').ifBlank { this }

private fun String.compareNaturally(other: String): Int {
    val firstParts = Regex("\\d+|\\D+").findAll(lowercase()).map { it.value }.toList()
    val secondParts = Regex("\\d+|\\D+").findAll(other.lowercase()).map { it.value }.toList()
    val maxSize = minOf(firstParts.size, secondParts.size)
    repeat(maxSize) { index ->
        val first = firstParts[index]
        val second = secondParts[index]
        val result = if (first.all(Char::isDigit) && second.all(Char::isDigit)) {
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
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlbumArt(track.albumArtUri, Modifier.size(52.dp), RoundedCornerShape(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 18.sp)
            Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
        IconButton(onClick = { }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Track menu")
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
) {
    Row(
        modifier = Modifier
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
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Column(modifier = Modifier.padding(start = 14.dp)) {
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 19.sp)
            Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ArtworkCard(title: String, subtitle: String, artwork: Uri?, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            AlbumArt(artwork, Modifier.fillMaxSize(), RoundedCornerShape(18.dp))
            if (title.contains("Favorite", ignoreCase = true)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.28f))
                )
                Text(title, color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center)
            }
        }
        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp), fontSize = 16.sp)
        Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    }
}

@Composable
private fun AlphabetRail(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .padding(end = 8.dp, bottom = 96.dp)
            .width(24.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ('A'..'Z').forEach {
                Text(it.toString(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(0.dp, Color.Transparent),
    ) {
        EmptyInline(message ?: "Nothing found")
    }
}

@Composable
private fun EmptyInline(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
