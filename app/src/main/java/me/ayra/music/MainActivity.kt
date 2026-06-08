package me.ayra.music

import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ayra.music.ui.home.MainScreen
import me.ayra.music.ui.home.HomeTab
import me.ayra.music.ui.home.modernEnter
import me.ayra.music.ui.home.modernExit
import me.ayra.music.ui.home.modernPopEnter
import me.ayra.music.ui.home.modernPopExit
import me.ayra.music.ui.player.PlayerSheet
import me.ayra.music.ui.settings.SettingsScreen
import me.ayra.music.ui.theme.MusicTheme
import me.ayra.music.util.MusicPreferences
import java.util.Locale

const val EXTRA_OPEN_FULLSCREEN_PLAYER = "me.ayra.music.extra.OPEN_FULLSCREEN_PLAYER"

class MainActivity : ComponentActivity() {
    private var openPlayerRequest by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        consumeOpenPlayerIntent(intent)
        enableEdgeToEdge()
        setContent {
            MusicTheme {
                MusicApp(openPlayerRequest = openPlayerRequest)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeOpenPlayerIntent(intent)
    }

    private fun consumeOpenPlayerIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_FULLSCREEN_PLAYER, false) == true) {
            openPlayerRequest += 1
            intent.removeExtra(EXTRA_OPEN_FULLSCREEN_PLAYER)
        }
    }
}

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uri: Uri,
    val albumId: Long,
    val folder: String,
) {
    val albumArtUri: Uri
        get() = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
}

data class AlbumGroup(val id: Long, val title: String, val artist: String, val tracks: List<Track>)
data class ArtistGroup(val name: String, val albums: Int, val tracks: List<Track>)
data class FolderGroup(val name: String, val path: String, val tracks: List<Track>)
data class PlaylistGroup(val title: String, val tracks: List<Track>, val artwork: Uri?)

data class LibraryState(
    val loading: Boolean = false,
    val permissionGranted: Boolean = false,
    val tracks: List<Track> = emptyList(),
    val favorites: Set<Long> = emptySet(),
    val playlists: List<PlaylistGroup> = emptyList(),
    val error: String? = null,
) {
    val favoriteTracks: List<Track> get() = tracks.filter { it.id in favorites }
    val albums: List<AlbumGroup>
        get() = tracks.groupBy { it.albumId }
            .map { (_, items) -> AlbumGroup(items.first().albumId, items.first().album, items.first().artist, items) }
            .sortedBy { it.title.lowercase(Locale.getDefault()) }
    val artists: List<ArtistGroup>
        get() = tracks.groupBy { it.artist.ifBlank { "Unknown artist" } }
            .map { (name, items) -> ArtistGroup(name, items.map { it.albumId }.distinct().size, items) }
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
    val folders: List<FolderGroup>
        get() = tracks.groupBy { it.folder.ifBlank { "Unknown folder" } }
            .map { (path, items) -> FolderGroup(path.substringAfterLast('/').ifBlank { path }, path, items) }
            .sortedBy { it.path.lowercase(Locale.getDefault()) }
}

data class PlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val queue: List<Track> = emptyList(),
)

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MediaStoreRepository(application)
    private val preferences = MusicPreferences(application)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var restoredTrack = false
    private var lastSavedTrackId = -1L

    private val _library = MutableStateFlow(LibraryState())
    val library: StateFlow<LibraryState> = _library.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    init {
        connectController(application)
        viewModelScope.launch {
            while (true) {
                publishPlayerState()
                delay(500)
            }
        }
    }

    private fun connectController(context: Context) {
        val sessionToken = SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync().also { future ->
            future.addListener(
                {
                    val connectedController = runCatching { future.get() }.getOrNull() ?: return@addListener
                    controller = connectedController.also { mediaController ->
                        mediaController.addListener(object : Player.Listener {
                            override fun onIsPlayingChanged(isPlaying: Boolean) = publishPlayerState()
                            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = publishPlayerState()
                            override fun onPlaybackStateChanged(playbackState: Int) = publishPlayerState()
                            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) = publishPlayerState()
                            override fun onRepeatModeChanged(repeatMode: Int) = publishPlayerState()
                        })
                    }
                    restoreLastTrack(_library.value.tracks)
                    publishPlayerState()
                },
                ContextCompat.getMainExecutor(context),
            )
        }
    }

    fun loadLibrary(permissionGranted: Boolean) {
        _library.update { it.copy(permissionGranted = permissionGranted, loading = permissionGranted, error = null) }
        if (!permissionGranted) return
        viewModelScope.launch {
            runCatching { repository.loadTracks() }
                .onSuccess { tracks ->
                    _library.update {
                        it.copy(
                            loading = false,
                            tracks = tracks,
                            playlists = repository.buildPlaylists(tracks),
                            error = null,
                        )
                    }
                    restoreLastTrack(tracks)
                }
                .onFailure { throwable ->
                    _library.update { it.copy(loading = false, error = throwable.message ?: "Unable to load music") }
                }
        }
    }

    fun playTrack(track: Track, queue: List<Track>) {
        val player = controller ?: return
        if (queue.isEmpty()) return
        preferences.saveLastTrackId(track.id)
        lastSavedTrackId = track.id
        val startIndex = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        player.setMediaItems(queue.map { it.toMediaItem() }, startIndex, 0L)
        player.prepare()
        player.play()
        _playerState.update { it.copy(queue = queue) }
        publishPlayerState()
    }

    fun togglePlayPause() {
        val player = controller ?: return
        if (player.mediaItemCount == 0) return
        if (player.isPlaying) player.pause() else player.play()
        publishPlayerState()
    }

    fun previous() {
        val player = controller ?: return
        if (player.hasPreviousMediaItem()) player.seekToPreviousMediaItem() else player.seekTo(0)
        publishPlayerState()
    }

    fun next() {
        val player = controller ?: return
        if (player.hasNextMediaItem()) player.seekToNextMediaItem()
        publishPlayerState()
    }

    fun seekTo(positionMs: Long) {
        val player = controller ?: return
        player.seekTo(positionMs)
        publishPlayerState()
    }

    fun toggleShuffle() {
        val player = controller ?: return
        player.shuffleModeEnabled = !player.shuffleModeEnabled
    }

    fun toggleRepeat() {
        val player = controller ?: return
        player.repeatMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun toggleFavorite(trackId: Long) {
        _library.update { state ->
            val favorites = if (trackId in state.favorites) state.favorites - trackId else state.favorites + trackId
            state.copy(favorites = favorites)
        }
    }

    private fun publishPlayerState() {
        val player = controller ?: return
        val queue = _playerState.value.queue
        val currentTrack = queue.getOrNull(player.currentMediaItemIndex)
        if (currentTrack != null && currentTrack.id != lastSavedTrackId) {
            preferences.saveLastTrackId(currentTrack.id)
            lastSavedTrackId = currentTrack.id
        }
        _playerState.update {
            it.copy(
                currentTrack = currentTrack,
                isPlaying = player.isPlaying,
                durationMs = player.duration.takeIf { duration -> duration > 0 } ?: currentTrack?.durationMs ?: 0L,
                positionMs = player.currentPosition.coerceAtLeast(0L),
                shuffle = player.shuffleModeEnabled,
                repeatMode = player.repeatMode,
            )
        }
    }

    private fun restoreLastTrack(tracks: List<Track>) {
        val player = controller ?: return
        if (restoredTrack || tracks.isEmpty() || player.mediaItemCount > 0) return
        val trackId = preferences.loadLastTrackId()
        val startIndex = tracks.indexOfFirst { it.id == trackId }
        if (startIndex < 0) return

        restoredTrack = true
        lastSavedTrackId = trackId
        player.setMediaItems(tracks.map { it.toMediaItem() }, startIndex, 0L)
        player.prepare()
        _playerState.update { it.copy(queue = tracks) }
        publishPlayerState()
    }

    override fun onCleared() {
        controller?.release()
        controller = null
        controllerFuture?.let(MediaController::releaseFuture)
        controllerFuture = null
        super.onCleared()
    }
}

private fun Track.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setUri(uri)
        .setMediaId(id.toString())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(albumArtUri)
                .build()
        )
        .build()
}

class MediaStoreRepository(private val context: Context) {
    suspend fun loadTracks(): List<Track> = withContext(Dispatchers.IO) {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM)
            add(MediaStore.Audio.Media.DURATION)
            add(MediaStore.Audio.Media.ALBUM_ID)
            add(MediaStore.Audio.Media.DISPLAY_NAME)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) add(MediaStore.Audio.Media.RELATIVE_PATH)
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) add(MediaStore.Audio.Media.DATA)
        }.toTypedArray()

        val tracks = mutableListOf<Track>()
        context.contentResolver.query(
            collection,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val relativePathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
            } else {
                @Suppress("DEPRECATION")
                cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            }

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(collection, id)
                val title = cursor.getString(titleColumn)?.takeIf { it.isNotBlank() } ?: "Unknown title"
                val artist = cursor.getString(artistColumn)?.takeIf { it.isNotBlank() } ?: "Unknown artist"
                val album = cursor.getString(albumColumn)?.takeIf { it.isNotBlank() } ?: "Unknown album"
                val folder = cursor.getStringOrNull(relativePathColumn)?.trimEnd('/') ?: "Music"
                tracks += Track(
                    id = id,
                    title = title,
                    artist = artist,
                    album = album,
                    durationMs = cursor.getLong(durationColumn).coerceAtLeast(0L),
                    uri = uri,
                    albumId = cursor.getLong(albumIdColumn),
                    folder = folder,
                )
            }
        }
        tracks
    }

    fun buildPlaylists(tracks: List<Track>): List<PlaylistGroup> {
        if (tracks.isEmpty()) return emptyList()
        return listOf(
            PlaylistGroup("Recently added", tracks.take(50), tracks.firstOrNull()?.albumArtUri),
            PlaylistGroup("Most played", tracks.sortedBy { it.title }.take(50), tracks.getOrNull(1)?.albumArtUri),
        )
    }
}

private fun android.database.Cursor.getStringOrNull(column: Int): String? {
    return if (column >= 0 && !isNull(column)) getString(column) else null
}

private enum class RootRoute { Main, Settings }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MusicApp(openPlayerRequest: Int = 0, viewModel: MusicViewModel = viewModel()) {
    val context = LocalContext.current
    var rootRoute by rememberSaveable { mutableStateOf(RootRoute.Main) }
    val permission = remember { audioPermission() }
    var permissionGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionGranted = granted
        viewModel.loadLibrary(granted)
    }
    val notificationPermission = remember { notificationPermission() }
    var notificationPermissionRequested by rememberSaveable { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(permissionGranted) {
        viewModel.loadLibrary(permissionGranted)
    }

    LaunchedEffect(permissionGranted, notificationPermission) {
        if (
            permissionGranted &&
            notificationPermission != null &&
            !notificationPermissionRequested &&
            ContextCompat.checkSelfPermission(context, notificationPermission) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionRequested = true
            notificationPermissionLauncher.launch(notificationPermission)
        }
    }

    val library by viewModel.library.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val preferences = remember(context) { MusicPreferences(context) }
    var lastHomeTab by rememberSaveable { mutableStateOf(preferences.loadLastTab(HomeTab.Track.ordinal)) }

    LaunchedEffect(openPlayerRequest) {
        if (openPlayerRequest > 0) {
            rootRoute = RootRoute.Main
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = rootRoute,
                transitionSpec = {
                    if (targetState == RootRoute.Settings) {
                        modernEnter() togetherWith modernExit()
                    } else {
                        modernPopEnter() togetherWith modernPopExit()
                    }.using(SizeTransform(clip = false))
                },
                label = "root-nav",
            ) { route ->
                when (route) {
                    RootRoute.Main -> MainScreen(
                        library = library,
                        onRequestPermission = { permissionLauncher.launch(permission) },
                        onSettings = { rootRoute = RootRoute.Settings },
                        onTrackClick = viewModel::playTrack,
                        onToggleFavorite = viewModel::toggleFavorite,
                        initialTabIndex = lastHomeTab,
                        onTabSelected = { tabIndex ->
                            lastHomeTab = tabIndex
                            preferences.saveLastTab(tabIndex)
                        },
                    )

                    RootRoute.Settings -> SettingsScreen(onBack = { rootRoute = RootRoute.Main })
                }
            }

            PlayerSheet(
                playerState = playerState,
                isFavorite = playerState.currentTrack?.id in library.favorites,
                expandRequest = openPlayerRequest,
                onSettings = { rootRoute = RootRoute.Settings },
                onToggleFavorite = { playerState.currentTrack?.id?.let(viewModel::toggleFavorite) },
                onPlayPause = viewModel::togglePlayPause,
                onPrevious = viewModel::previous,
                onNext = viewModel::next,
                onSeek = viewModel::seekTo,
                onShuffle = viewModel::toggleShuffle,
                onRepeat = viewModel::toggleRepeat,
            )
        }
    }
}

private fun audioPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

private fun notificationPermission(): String? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.POST_NOTIFICATIONS
    } else {
        null
    }
}
