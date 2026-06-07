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
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
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
                    val folderPath = currentRoute.removePrefix(ROUTE_FOLDER_PREFIX)
                    val folder = library.folders.firstOrNull { it.path == folderPath }
                    if (folder != null) {
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
                    } else {
                        HomeScreen(
                            library = library,
                            onRequestPermission = onRequestPermission,
                            onSettings = onSettings,
                            onTrackClick = onTrackClick,
                            onToggleFavorite = onToggleFavorite,
                            onFolderClick = { route = ROUTE_FOLDER_PREFIX + it.path },
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

@Composable
private fun HomeScreen(
    library: LibraryState,
    onRequestPermission: () -> Unit,
    onSettings: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onFolderClick: (FolderGroup) -> Unit,
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
            !library.permissionGranted -> PermissionState(onRequestPermission)
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
                    HomeTab.Album -> AlbumTab(library, onTrackClick)
                    HomeTab.Artist -> ArtistTab(library, onTrackClick)
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
private fun AlbumTab(library: LibraryState, onTrackClick: (Track, List<Track>) -> Unit) {
    RoundedGridPanel {
        item(span = { GridItemSpan(maxLineSpan) }) { SortHeader("Release") }
        items(library.albums, key = { it.id }) { album ->
            ArtworkCard(
                title = album.title,
                subtitle = "${album.artist} | ${album.tracks.size} tracks",
                artwork = album.tracks.firstOrNull()?.albumArtUri,
                modifier = Modifier.clickable { album.tracks.firstOrNull()?.let { onTrackClick(it, album.tracks) } },
            )
        }
        if (library.albums.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { EmptyInline("No albums found") }
        }
    }
}

@Composable
private fun ArtistTab(library: LibraryState, onTrackClick: (Track, List<Track>) -> Unit) {
    val listState = rememberLazyListState()
    IndexedListWithRail(listState = listState) {
        item { SortHeader("Name") }
        items(library.artists, key = { it.name }) { artist ->
            MediaGroupRow(
                artwork = artist.tracks.firstOrNull()?.albumArtUri,
                title = artist.name,
                subtitle = "${artist.albums} albums | ${artist.tracks.size} tracks",
                onClick = { artist.tracks.firstOrNull()?.let { onTrackClick(it, artist.tracks) } },
            )
        }
        if (library.artists.isEmpty()) item { EmptyInline("No artists found") }
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
                    Icons.Default.Folder,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
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
            .width(28.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ('A'..'Z').forEach {
                Text(it.toString(), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
