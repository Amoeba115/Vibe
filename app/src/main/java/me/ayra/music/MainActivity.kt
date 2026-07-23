package me.ayra.music

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil3.compose.AsyncImage
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ayra.music.data.AudioInfoEntity
import me.ayra.music.data.CustomPlaylistEntity
import me.ayra.music.data.CustomPlaylistTrackEntity
import me.ayra.music.data.FavoriteEntity
import me.ayra.music.data.FavoriteItemEntity
import me.ayra.music.data.LibraryDao
import me.ayra.music.data.LibraryDatabase
import me.ayra.music.data.LibrarySource
import me.ayra.music.data.ScannedTrack
import me.ayra.music.data.TrackSnapshot
import me.ayra.music.data.TrackStatsEntity
import me.ayra.music.data.TrackEntity
import me.ayra.music.data.toAudioInfo
import me.ayra.music.data.toEntity
import me.ayra.music.data.toSnapshot
import me.ayra.music.data.toTrack
import me.ayra.music.lyrics.Lyrics
import me.ayra.music.lyrics.LyricsRepository
import me.ayra.music.ui.home.HomeTab
import me.ayra.music.ui.home.MainScreen
import me.ayra.music.ui.navigation.MainRoute
import me.ayra.music.ui.navigation.rememberMusicNavigator
import me.ayra.music.ui.player.PlayerSheet
import me.ayra.music.ui.theme.MusicTheme
import me.ayra.music.ui.theme.ThemeMode
import me.ayra.music.util.MusicPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.Normalizer
import java.util.Locale

const val EXTRA_OPEN_FULLSCREEN_PLAYER = "me.ayra.music.extra.OPEN_FULLSCREEN_PLAYER"

class MainActivity : ComponentActivity() {
    private var openPlayerRequest by mutableIntStateOf(0)
    private var openViewUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        consumeOpenPlayerIntent(intent)
        enableEdgeToEdge()
        setContent {
            val preferences = remember(this) { MusicPreferences(this) }
            var themeMode by remember {
                mutableStateOf(
                    ThemeMode.entries.firstOrNull { it.name == preferences.loadThemeMode() } ?: ThemeMode.Auto,
                )
            }
            var themeColorSeed by remember { mutableStateOf(preferences.loadThemeColorSeed()) }
            var amoledMode by remember { mutableStateOf(preferences.loadAmoledMode()) }
            MusicTheme(
                themeMode = themeMode,
                themeColorSeed = themeColorSeed,
                amoledMode = amoledMode,
                onThemeModeChange = {
                    themeMode = it
                    preferences.saveThemeMode(it.name)
                },
                onThemeColorSeedChange = {
                    themeColorSeed = it
                    preferences.saveThemeColorSeed(it)
                },
                onAmoledModeChange = {
                    amoledMode = it
                    preferences.saveAmoledMode(it)
                },
            ) {
                MusicApp(
                    openPlayerRequest = openPlayerRequest,
                    openViewUri = openViewUri,
                    onOpenViewUriConsumed = { openViewUri = null },
                )
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
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.data?.let { uri ->
                openViewUri = uri
                openPlayerRequest += 1
            }
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
    val trackNumber: Int = 0,
    val discNumber: Int = 0,
    val year: Int = 0,
    val dateAddedMs: Long = 0L,
    val audioInfo: AudioInfo? = null,
) {
    val albumArtUri: Uri
        get() = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
}

data class AudioInfo(
    val trackId: Long,
    val codec: String?,
    val sampleRate: Int?,
    val bitDepth: Int?,
    val bitrate: Int?,
    val channels: Int?,
)

data class AlbumGroup(
    val id: Long,
    val title: String,
    val artist: String,
    val tracks: List<Track>,
)

data class ArtistGroup(
    val name: String,
    val albums: Int,
    val tracks: List<Track>,
)

data class FolderGroup(
    val name: String,
    val path: String,
    val tracks: List<Track>,
)

data class PlaylistGroup(
    val id: String,
    val title: String,
    val tracks: List<Track>,
    val artwork: Uri?,
    val createdAt: Long = 0L,
)

data class TrackStats(
    val trackId: Long,
    val playCount: Int = 0,
    val lastPlayed: Long = 0L,
    val skipCount: Int = 0,
)

data class FavoriteItem(
    val type: String,
    val key: String,
    val addedAt: Long,
)

object FavoriteType {
    const val Track = "track"
    const val Artist = "artist"
    const val Folder = "folder"
    const val Album = "album"
    const val Playlist = "playlist"
}

data class LibraryState(
    val loading: Boolean = false,
    val scanning: Boolean = false,
    val scannedTrackCount: Int = 0,
    val importingPlaylist: Boolean = false,
    val playlistImportMessage: String? = null,
    val permissionGranted: Boolean = false,
    val allTracks: List<Track> = emptyList(),
    val tracks: List<Track> = emptyList(),
    val favorites: Set<Long> = emptySet(),
    val favoriteItems: List<FavoriteItem> = emptyList(),
    val playlists: List<PlaylistGroup> = emptyList(),
    val trackStats: Map<Long, TrackStats> = emptyMap(),
    val hiddenFolders: Set<String> = emptySet(),
    val favoriteTracks: List<Track> = emptyList(),
    val albums: List<AlbumGroup> = emptyList(),
    val artists: List<ArtistGroup> = emptyList(),
    val folders: List<FolderGroup> = emptyList(),
    val allFolders: List<FolderGroup> = emptyList(),
    val tracksById: Map<Long, Track> = emptyMap(),
    val albumsById: Map<Long, AlbumGroup> = emptyMap(),
    val artistsByName: Map<String, ArtistGroup> = emptyMap(),
    val foldersByPath: Map<String, FolderGroup> = emptyMap(),
    val error: String? = null,
) {
    fun isFavoriteItem(
        type: String,
        key: String,
    ): Boolean = favoriteItems.any { it.type == type && it.key == key }
}

data class PlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val queue: List<Track> = emptyList(),
    val lyrics: Lyrics? = null,
    val lyricsLoading: Boolean = false,
)

class MusicViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val libraryScanner = LibraryScanner(application)
    private val lyricsRepository = LyricsRepository(application)
    private val preferences = MusicPreferences(application)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var restoredTrack = false
    private var lastSavedTrackId = -1L
    private var lastObservedTrackId: Long? = null
    private var loadingLyricsTrackId: Long? = null
    private var loadedLyricsTrackId: Long? = null
    private var pendingExternalUri: Uri? = null
    private val settingsListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                MusicPreferences.KEY_PLAYBACK_SPEED -> controller?.setPlaybackSpeed(preferences.loadPlaybackSpeed())
                MusicPreferences.KEY_HIDDEN_FOLDERS -> applyHiddenFolders()
            }
        }

    private val _library = MutableStateFlow(LibraryState())
    val library: StateFlow<LibraryState> = _library.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    init {
        preferences.registerSettingsListener(settingsListener)
        connectController(application)
        viewModelScope.launch {
            while (true) {
                publishPlayerState()
                pauseIfVolumeZero(application)
                delay(500)
            }
        }
    }

    private fun connectController(context: Context) {
        val sessionToken = SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
        controllerFuture =
            MediaController.Builder(context, sessionToken).buildAsync().also { future ->
                future.addListener(
                    {
                        val connectedController = runCatching { future.get() }.getOrNull() ?: return@addListener
                        controller =
                            connectedController.also { mediaController ->
                                mediaController.setPlaybackSpeed(preferences.loadPlaybackSpeed())
                                mediaController.shuffleModeEnabled = preferences.loadShuffleEnabled()
                                mediaController.repeatMode = preferences.loadRepeatMode()
                                mediaController.addListener(
                                    object : Player.Listener {
                                        override fun onIsPlayingChanged(isPlaying: Boolean) = publishPlayerState()

                                        override fun onPlayWhenReadyChanged(
                                            playWhenReady: Boolean,
                                            reason: Int,
                                        ) = publishPlayerState()

                                        override fun onMediaItemTransition(
                                            mediaItem: MediaItem?,
                                            reason: Int,
                                        ) = publishPlayerState()

                                        override fun onPlaybackStateChanged(playbackState: Int) = publishPlayerState()

                                        override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) =
                                            publishPlayerState()

                                        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                                            preferences.saveShuffleEnabled(shuffleModeEnabled)
                                            publishPlayerState()
                                        }

                                        override fun onRepeatModeChanged(repeatMode: Int) {
                                            preferences.saveRepeatMode(repeatMode)
                                            publishPlayerState()
                                        }
                                    },
                                )
                            }
                        restoreOrSyncPlayerQueue(_library.value.tracks)
                        pendingExternalUri?.let { uri ->
                            pendingExternalUri = null
                            playExternalUri(uri)
                        }
                        publishPlayerState()
                    },
                    ContextCompat.getMainExecutor(context),
                )
            }
    }

    fun loadLibrary(
        permissionGranted: Boolean,
        forceRefresh: Boolean = false,
    ) {
        _library.update {
            it.copy(
                permissionGranted = permissionGranted,
                loading = permissionGranted,
                scanning = permissionGranted,
                scannedTrackCount = 0,
                error = null,
            )
        }
        viewModelScope.launch {
            var activeSnapshot = emptyList<TrackSnapshot>()
            runCatching { libraryScanner.loadCachedLibrary() }
                .onSuccess { cached ->
                    activeSnapshot = cached.snapshot
                    val hiddenFolders = preferences.loadHiddenFolders()
                    val visibleTracks = cached.tracks.filterVisible(hiddenFolders)
                    _library.update {
                        it.copy(
                            loading = permissionGranted && cached.tracks.isEmpty(),
                            scannedTrackCount = visibleTracks.size,
                            allTracks = cached.tracks,
                            tracks = visibleTracks,
                            favorites = cached.favorites,
                            favoriteItems = cached.favoriteItems,
                            playlists = libraryScanner.buildPlaylists(visibleTracks),
                            trackStats = cached.trackStats,
                            hiddenFolders = hiddenFolders,
                            error = null,
                        ).withDerivedCollections()
                    }
                    restoreOrSyncPlayerQueue(visibleTracks)
                }
            if (!permissionGranted) {
                _library.update { it.copy(loading = false, scanning = false) }
                return@launch
            }

            val shouldRefresh =
                if (forceRefresh) {
                    true
                } else {
                    runCatching {
                        libraryScanner.shouldAutoRefreshLibrary(_library.value.allTracks.isNotEmpty())
                    }.getOrElse { false }
                }
            if (!shouldRefresh) {
                _library.update { it.copy(loading = false, scanning = false, scannedTrackCount = it.tracks.size, error = null) }
                return@launch
            }

            runCatching {
                libraryScanner.refreshLibrary { partial ->
                    if (partial.tracks.isNotEmpty()) {
                        withContext(Dispatchers.Main.immediate) {
                            _library.update {
                                it.copy(
                                    loading = false,
                                    scannedTrackCount = partial.tracks.size,
                                    error = null,
                                )
                            }
                        }
                    }
                }
            }.onSuccess { refreshed ->
                if (refreshed.snapshot == activeSnapshot) {
                    _library.update { it.copy(loading = false, scanning = false, scannedTrackCount = it.tracks.size, error = null) }
                    return@onSuccess
                }
                val hiddenFolders = preferences.loadHiddenFolders()
                val visibleTracks = refreshed.tracks.filterVisible(hiddenFolders)
                _library.update {
                    it.copy(
                        loading = false,
                        scanning = false,
                        scannedTrackCount = visibleTracks.size,
                        allTracks = refreshed.tracks,
                        tracks = visibleTracks,
                        favorites = refreshed.favorites,
                        favoriteItems = refreshed.favoriteItems,
                        playlists = libraryScanner.buildPlaylists(visibleTracks),
                        trackStats = refreshed.trackStats,
                        hiddenFolders = hiddenFolders,
                        error = null,
                    ).withDerivedCollections()
                }
                restoreOrSyncPlayerQueue(visibleTracks)
            }.onFailure { throwable ->
                _library.update {
                    it.copy(
                        loading = false,
                        scanning = false,
                        scannedTrackCount = it.tracks.size,
                        error = throwable.message ?: "Unable to load music",
                    )
                }
            }
        }
    }

    fun rescanLibrary() {
        loadLibrary(
            permissionGranted = _library.value.permissionGranted,
            forceRefresh = true,
        )
    }

    fun setHiddenFolders(folders: Set<String>) {
        preferences.saveHiddenFolders(folders)
        applyHiddenFolders()
    }

    fun playTrack(
        track: Track,
        queue: List<Track>,
    ) {
        val player = controller ?: return
        if (queue.isEmpty()) return
        preferences.saveLastTrackId(track.id)
        saveLastQueue(queue)
        lastSavedTrackId = track.id
        val startIndex = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        player.setMediaItems(queue.map { it.toMediaItem() }, startIndex, 0L)
        player.prepare()
        player.play()
        lastObservedTrackId = null
        _playerState.update { it.copy(queue = queue) }
        publishPlayerState()
    }

    fun playExternalUri(uri: Uri) {
        val player =
            controller ?: run {
                pendingExternalUri = uri
                return
            }
        val track = getApplication<Application>().trackFromExternalUri(uri)
        player.setMediaItem(track.toMediaItem())
        player.prepare()
        player.play()
        _playerState.update { it.copy(queue = listOf(track), currentTrack = track) }
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
        if (player.mediaItemCount == 0) return
        _playerState.value.currentTrack
            ?.id
            ?.let(::recordTrackSkipped)
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        } else {
            player.seekTo(player.mediaItemCount - 1, 0L)
        }
        publishPlayerState()
    }

    fun next() {
        val player = controller ?: return
        if (player.mediaItemCount == 0) return
        _playerState.value.currentTrack
            ?.id
            ?.let(::recordTrackSkipped)
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        } else {
            player.seekTo(0, 0L)
        }
        publishPlayerState()
    }

    fun seekTo(positionMs: Long) {
        val player = controller ?: return
        player.seekTo(positionMs)
        publishPlayerState()
    }

    fun toggleShuffle() {
        val player = controller ?: return
        val enabled = !player.shuffleModeEnabled
        player.shuffleModeEnabled = enabled
        preferences.saveShuffleEnabled(enabled)
        _playerState.update { it.copy(shuffle = enabled) }
    }

    private fun recordTrackPlayed(trackId: Long) {
        val now = System.currentTimeMillis()
        _library.update { state ->
            val current = state.trackStats[trackId] ?: TrackStats(trackId)
            state.copy(trackStats = state.trackStats + (trackId to current.copy(playCount = current.playCount + 1, lastPlayed = now)))
        }
        viewModelScope.launch {
            libraryScanner.recordTrackPlayed(trackId, now)
        }
    }

    private fun recordTrackSkipped(trackId: Long) {
        _library.update { state ->
            val current = state.trackStats[trackId] ?: TrackStats(trackId)
            state.copy(trackStats = state.trackStats + (trackId to current.copy(skipCount = current.skipCount + 1)))
        }
        viewModelScope.launch {
            libraryScanner.recordTrackSkipped(trackId)
        }
    }

    private fun pauseIfVolumeZero(context: Context) {
        val player = controller ?: return
        if (!preferences.loadPauseWhenVolumeZero() || !player.isPlaying) return
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            player.pause()
        }
    }

    private fun applyHiddenFolders() {
        val hiddenFolders = preferences.loadHiddenFolders()
        _library.update { state ->
            val sourceTracks = state.allTracks.ifEmpty { state.tracks }
            val tracks = sourceTracks.filterVisible(hiddenFolders)
            state.copy(
                allTracks = sourceTracks,
                tracks = tracks,
                playlists = libraryScanner.buildPlaylists(tracks),
                hiddenFolders = hiddenFolders,
            ).withDerivedCollections()
        }
    }

    fun toggleRepeat() {
        val player = controller ?: return
        val repeatMode =
            when (player.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        player.repeatMode = repeatMode
        preferences.saveRepeatMode(repeatMode)
        _playerState.update { it.copy(repeatMode = repeatMode) }
    }

    fun toggleFavorite(trackId: Long) {
        toggleFavoriteItem(FavoriteType.Track, trackId.toString())
    }

    fun toggleFavoriteItem(
        type: String,
        key: String,
    ) {
        var favoriteNow = false
        val normalizedKey = key.trim()
        if (normalizedKey.isEmpty()) return
        _library.update { state ->
            val exists = state.favoriteItems.any { it.type == type && it.key == normalizedKey }
            favoriteNow = !exists
            val updatedItems =
                if (exists) {
                    state.favoriteItems.filterNot { it.type == type && it.key == normalizedKey }
                } else {
                    listOf(FavoriteItem(type, normalizedKey, System.currentTimeMillis())) + state.favoriteItems
                }
            val favorites =
                if (type == FavoriteType.Track) {
                    val trackId = normalizedKey.toLongOrNull()
                    when {
                        trackId == null -> state.favorites
                        favoriteNow -> state.favorites + trackId
                        else -> state.favorites - trackId
                    }
                } else {
                    state.favorites
                }
            state.copy(favorites = favorites, favoriteItems = updatedItems).withDerivedCollections()
        }
        viewModelScope.launch {
            libraryScanner.setFavoriteItem(type, normalizedKey, favoriteNow)
        }
    }

    fun createPlaylist(
        name: String,
        tracks: List<Track>,
    ) {
        val normalizedName = name.trim()
        if (normalizedName.isEmpty()) return
        viewModelScope.launch {
            libraryScanner.createPlaylist(normalizedName, tracks)
            val visibleTracks = _library.value.tracks
            _library.update { state ->
                state.copy(playlists = libraryScanner.buildPlaylists(visibleTracks))
            }
        }
    }

    fun addTracksToPlaylist(
        playlistId: String,
        tracks: List<Track>,
    ) {
        if (tracks.isEmpty()) return
        viewModelScope.launch {
            libraryScanner.addTracksToPlaylist(playlistId, tracks)
            val visibleTracks = _library.value.tracks
            _library.update { state ->
                state.copy(playlists = libraryScanner.buildPlaylists(visibleTracks))
            }
        }
    }

    fun renamePlaylist(
        playlistId: String,
        name: String,
    ) {
        val normalizedName = name.trim()
        if (normalizedName.isEmpty()) return
        viewModelScope.launch {
            libraryScanner.renamePlaylist(playlistId, normalizedName)
            val visibleTracks = _library.value.tracks
            _library.update { state ->
                state.copy(playlists = libraryScanner.buildPlaylists(visibleTracks))
            }
        }
    }

    fun deletePlaylists(playlistIds: List<String>) {
        val customPlaylistIds = playlistIds.filter { it.startsWith("custom-") }.distinct()
        if (customPlaylistIds.isEmpty()) return
        viewModelScope.launch {
            libraryScanner.deletePlaylists(customPlaylistIds)
            val visibleTracks = _library.value.tracks
            _library.update { state ->
                val deleted = customPlaylistIds.toSet()
                state.copy(
                    playlists = libraryScanner.buildPlaylists(visibleTracks),
                    favoriteItems = state.favoriteItems.filterNot { it.type == FavoriteType.Playlist && it.key in deleted },
                )
            }
        }
    }

    fun deleteTracksPermanently(tracks: List<Track>) {
        if (tracks.isEmpty()) return
        viewModelScope.launch {
            libraryScanner.deleteTracksPermanently(tracks)
            _library.update { state ->
                val deletedIds = tracks.mapTo(mutableSetOf()) { it.id }
                val updatedAllTracks = state.allTracks.filterNot { it.id in deletedIds }
                val updatedTracks = updatedAllTracks.filterVisible(state.hiddenFolders)
                state.copy(
                    allTracks = updatedAllTracks,
                    tracks = updatedTracks,
                    favorites = state.favorites - deletedIds,
                    favoriteItems = state.favoriteItems.filterNot { it.type == FavoriteType.Track && it.key.toLongOrNull() in deletedIds },
                    playlists = libraryScanner.buildPlaylists(updatedTracks),
                ).withDerivedCollections()
            }
        }
    }

    fun addTracksToCurrentQueue(tracks: List<Track>) {
        val player = controller ?: return
        if (tracks.isEmpty()) return
        val currentQueue = _playerState.value.queue
        val existingIds = currentQueue.mapTo(mutableSetOf()) { it.id }
        val newTracks = tracks.filter { existingIds.add(it.id) }
        if (newTracks.isEmpty()) return
        val updatedQueue = currentQueue + newTracks
        player.addMediaItems(newTracks.map { it.toMediaItem() })
        saveLastQueue(updatedQueue)
        _playerState.update { it.copy(queue = updatedQueue) }
        publishPlayerState()
    }

    fun replaceCurrentQueue(tracks: List<Track>) {
        val player = controller ?: return
        if (tracks.isEmpty()) return
        player.setMediaItems(tracks.map { it.toMediaItem() }, 0, 0L)
        player.prepare()
        player.play()
        lastObservedTrackId = null
        saveLastQueue(tracks)
        tracks.firstOrNull()?.let {
            preferences.saveLastTrackId(it.id)
            lastSavedTrackId = it.id
        }
        _playerState.update { it.copy(queue = tracks, currentTrack = tracks.firstOrNull()) }
        publishPlayerState()
    }

    fun replacePlaylistTracks(
        playlistId: String,
        tracks: List<Track>,
    ) {
        viewModelScope.launch {
            libraryScanner.replacePlaylistTracks(playlistId, tracks)
            val visibleTracks = _library.value.tracks
            _library.update { state ->
                state.copy(playlists = libraryScanner.buildPlaylists(visibleTracks))
            }
        }
    }

    fun importPlaylist(uri: Uri) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            _library.update { it.copy(importingPlaylist = true, playlistImportMessage = null) }
            runCatching {
                val candidateTracks =
                    _library.value.allTracks
                        .ifEmpty { _library.value.tracks }
                        .ifEmpty { runCatching { libraryScanner.loadCachedLibrary().tracks }.getOrDefault(emptyList()) }
                val imported =
                    withContext(Dispatchers.IO) {
                        val text =
                            app.contentResolver
                                .openInputStream(uri)
                                ?.use { input ->
                                    input.bufferedReader().use { it.readText() }
                                }.orEmpty()
                        parseImportedPlaylists(uri, text, candidateTracks)
                    }
                if (imported.isEmpty()) return@runCatching PlaylistImportResult.NoEntries
                var createdCount = 0
                imported.forEach { playlist ->
                    val tracks =
                        if (playlist.tracks.isNotEmpty()) {
                            playlist.tracks
                        } else {
                            withContext(Dispatchers.IO) {
                                app.resolvePlaylistEntriesFromMediaStore(playlist.entries, candidateTracks)
                            }
                        }
                    if (tracks.isNotEmpty()) {
                        libraryScanner.createPlaylist(playlist.name, tracks)
                        createdCount++
                    }
                }
                if (createdCount == 0) return@runCatching PlaylistImportResult.NoMatches
                val visibleTracks = _library.value.tracks
                _library.update { state ->
                    state.copy(playlists = libraryScanner.buildPlaylists(visibleTracks))
                }
                PlaylistImportResult.Success(createdCount)
            }.onSuccess { result ->
                val message =
                    when (result) {
                        is PlaylistImportResult.Success ->
                            app.getString(R.string.import_playlist_success_count, result.playlistCount)
                        PlaylistImportResult.NoEntries ->
                            app.getString(R.string.import_playlist_failed_no_entries)
                        PlaylistImportResult.NoMatches ->
                            app.getString(R.string.import_playlist_failed_no_matches)
                    }
                _library.update {
                    it.copy(
                        importingPlaylist = false,
                        playlistImportMessage = message,
                    )
                }
            }.onFailure {
                _library.update { state ->
                    state.copy(
                        importingPlaylist = false,
                        playlistImportMessage = app.getString(R.string.import_playlist_failed_generic),
                    )
                }
            }
        }
    }

    fun exportPlaylistsJson(): String = exportCustomPlaylists(_library.value.playlists.filter { it.id.startsWith("custom-") })

    private fun publishPlayerState() {
        val player = controller ?: return
        val queue =
            _playerState.value.queue
                .takeIf { it.isNotEmpty() }
                ?: controllerQueueFromLibrary(_library.value.tracks)
        val currentTrack =
            queue.getOrNull(player.currentMediaItemIndex)
                ?: player.currentMediaItem
                    ?.mediaId
                    ?.toLongOrNull()
                    ?.let { mediaId -> _library.value.tracks.firstOrNull { it.id == mediaId } }
        if (currentTrack?.id != null && currentTrack.id != lastObservedTrackId) {
            lastObservedTrackId = currentTrack.id
            recordTrackPlayed(currentTrack.id)
        }
        if (currentTrack != null && currentTrack.id != lastSavedTrackId) {
            preferences.saveLastTrackId(currentTrack.id)
            if (queue.isNotEmpty()) saveLastQueue(queue)
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
                queue = queue,
                lyrics = if (currentTrack?.id == it.currentTrack?.id) it.lyrics else null,
                lyricsLoading = if (currentTrack?.id == it.currentTrack?.id) it.lyricsLoading else false,
            )
        }
        if (currentTrack != null) {
            loadLyrics(currentTrack)
        } else {
            loadedLyricsTrackId = null
            loadingLyricsTrackId = null
        }
    }

    private fun loadLyrics(track: Track) {
        if (loadedLyricsTrackId == track.id || loadingLyricsTrackId == track.id) return
        loadingLyricsTrackId = track.id
        _playerState.update { it.copy(lyrics = null, lyricsLoading = true) }
        viewModelScope.launch {
            val lyrics = lyricsRepository.lyricsFor(track)
            if (_playerState.value.currentTrack?.id == track.id) {
                loadedLyricsTrackId = track.id
                _playerState.update { it.copy(lyrics = lyrics, lyricsLoading = false) }
            }
            if (loadingLyricsTrackId == track.id) {
                loadingLyricsTrackId = null
            }
        }
    }

    private fun restoreOrSyncPlayerQueue(tracks: List<Track>) {
        val player = controller ?: return
        if (tracks.isEmpty()) return
        if (player.mediaItemCount > 0) {
            val queue = controllerQueueFromLibrary(tracks)
            if (queue.isNotEmpty()) {
                restoredTrack = true
                _playerState.update { it.copy(queue = queue) }
            }
            publishPlayerState()
            return
        }
        if (restoredTrack) return
        val trackId = preferences.loadLastTrackId()
        val queue = restoredQueueFromPreferences(tracks).ifEmpty { tracks }
        val startIndex = queue.indexOfFirst { it.id == trackId }
        if (startIndex < 0) return

        restoredTrack = true
        lastSavedTrackId = trackId
        player.shuffleModeEnabled = preferences.loadShuffleEnabled()
        player.repeatMode = preferences.loadRepeatMode()
        player.setMediaItems(queue.map { it.toMediaItem() }, startIndex, 0L)
        player.prepare()
        _playerState.update { it.copy(queue = queue) }
        publishPlayerState()
    }

    private fun saveLastQueue(queue: List<Track>) {
        preferences.saveLastQueueIds(queue.map { it.id }.distinct())
    }

    private fun restoredQueueFromPreferences(tracks: List<Track>): List<Track> {
        val tracksById = tracks.associateBy { it.id }
        return preferences.loadLastQueueIds().mapNotNull(tracksById::get)
    }

    private fun controllerQueueFromLibrary(tracks: List<Track>): List<Track> {
        val player = controller ?: return emptyList()
        if (tracks.isEmpty() || player.mediaItemCount == 0) return emptyList()
        val tracksById = tracks.associateBy { it.id }
        return (0 until player.mediaItemCount)
            .mapNotNull { index ->
                player
                    .getMediaItemAt(index)
                    .mediaId
                    .toLongOrNull()
                    ?.let(tracksById::get)
            }
    }

    override fun onCleared() {
        preferences.unregisterSettingsListener(settingsListener)
        controller?.release()
        controller = null
        controllerFuture?.let(MediaController::releaseFuture)
        controllerFuture = null
        super.onCleared()
    }
}

private fun Track.toMediaItem(): MediaItem =
    MediaItem
        .Builder()
        .setUri(uri)
        .setMediaId(id.toString())
        .setMediaMetadata(
            MediaMetadata
                .Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(albumArtUri)
                .build(),
        ).build()

private fun Context.trackFromExternalUri(uri: Uri): Track {
    val displayName =
        if (uri.scheme == "content") {
            runCatching {
                contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
            }.getOrNull()
        } else {
            uri.lastPathSegment
        }
    val title = displayName?.substringBeforeLast('.')?.takeIf { it.isNotBlank() } ?: "External audio"
    return Track(
        id = stableExternalTrackId(uri.toString()),
        title = title,
        artist = "Unknown artist",
        album = "Unknown album",
        durationMs = 0L,
        uri = uri,
        albumId = 0L,
        folder = uri.path?.substringBeforeLast('/', missingDelimiterValue = "External") ?: "External",
    )
}

private fun stableExternalTrackId(value: String): Long {
    var hash = 1125899906842597L
    value.forEach { hash = 31 * hash + it.code }
    return hash and Long.MAX_VALUE
}

private data class ImportedPlaylist(
    val name: String,
    val entries: List<String>,
    val tracks: List<Track>,
)

private sealed interface PlaylistImportResult {
    data class Success(
        val playlistCount: Int,
    ) : PlaylistImportResult

    data object NoEntries : PlaylistImportResult

    data object NoMatches : PlaylistImportResult
}

private fun parseImportedPlaylists(
    uri: Uri,
    text: String,
    libraryTracks: List<Track>,
): List<ImportedPlaylist> {
    if (text.isBlank()) return emptyList()
    val name =
        uri.lastPathSegment
            ?.substringAfterLast('/')
            ?.substringBeforeLast('.')
            ?.ifBlank { null } ?: "Imported playlist"
    return if (text.trimStart().startsWith("{")) {
        parseAmsPlaylists(text, name, libraryTracks)
    } else {
        val entries = text.readM3uEntries()
        listOf(ImportedPlaylist(name, entries, resolvePlaylistEntries(entries, libraryTracks)))
    }
}

private fun parseAmsPlaylists(
    text: String,
    fallbackName: String,
    libraryTracks: List<Track>,
): List<ImportedPlaylist> =
    runCatching {
        val root = JSONObject(text)
        val playlists = root.optJSONArray("playlists")
        if (playlists != null) {
            (0 until playlists.length()).mapNotNull { index ->
                playlists.optJSONObject(index)?.toImportedPlaylist(fallbackName, libraryTracks)
            }
        } else {
            listOfNotNull(root.toImportedPlaylist(fallbackName, libraryTracks))
        }
    }.getOrDefault(emptyList())

private fun JSONObject.toImportedPlaylist(
    fallbackName: String,
    libraryTracks: List<Track>,
): ImportedPlaylist? {
    val entries = optJSONArray("tracks") ?: return null
    val references =
        (0 until entries.length()).mapNotNull { index ->
            val item = entries.get(index)
            when (item) {
                is JSONObject -> item.optString("uri").ifBlank { item.optString("path") }.ifBlank { item.optString("title") }
                else -> item?.toString()
            }?.takeIf { it.isNotBlank() }
        }
    return ImportedPlaylist(
        name = optString("name").ifBlank { fallbackName },
        entries = references,
        tracks = resolvePlaylistEntries(references, libraryTracks),
    )
}

private fun String.readM3uEntries(): List<String> =
    lineSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() && !it.startsWith("#") }
        .toList()

private fun resolvePlaylistEntries(
    entries: List<String>,
    libraryTracks: List<Track>,
): List<Track> {
    val byUri = libraryTracks.associateBy { it.uri.toString() }
    val byPath = libraryTracks.mapNotNull { track -> track.uri.path?.replace('\\', '/')?.let { it to track } }.toMap()
    val byName = libraryTracks.associateBy { it.title.lowercase(Locale.ROOT) }
    val candidates =
        libraryTracks.map { track ->
            TrackMatchCandidate(
                track = track,
                normalizedTitle = normalizeForMatch(track.title),
                normalizedArtist = normalizeForMatch(track.artist),
                normalizedAlbum = normalizeForMatch(track.album),
                normalizedFolder = normalizeForMatch(track.folder.replace('\\', '/')),
            )
        }
    return entries
        .mapNotNull { raw ->
            val normalized = raw.replace('\\', '/')
            byUri[raw]
                ?: byPath[normalized]
                ?: libraryTracks.firstOrNull { it.uri.path?.replace('\\', '/') == normalized }
                ?: byName[File(normalized).nameWithoutExtension.lowercase(Locale.ROOT)]
                ?: resolveByRelativePathHeuristics(normalized, candidates)
        }.distinctBy { it.id }
}

private fun Context.resolvePlaylistEntriesFromMediaStore(
    entries: List<String>,
    libraryTracks: List<Track>,
): List<Track> {
    if (entries.isEmpty() || libraryTracks.isEmpty()) return emptyList()
    val tracksById = libraryTracks.associateBy { it.id }
    val projection =
        buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.DISPLAY_NAME)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) add(MediaStore.Audio.Media.RELATIVE_PATH)
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) add(MediaStore.Audio.Media.DATA)
        }.toTypedArray()

    val indexedByPath = mutableMapOf<String, Long>()
    val indexedByTail = mutableMapOf<String, Long?>()
    val indexedByFileName = mutableMapOf<String, Long?>()
    contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        "${MediaStore.Audio.Media.IS_MUSIC} != 0",
        null,
        null,
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        val relativePathColumn =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
            } else {
                @Suppress("DEPRECATION")
                cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            }
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val displayName = cursor.getString(nameColumn).orEmpty()
            val mediaPath =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val relative = cursor.getStringOrNull(relativePathColumn).orEmpty()
                    "$relative$displayName"
                } else {
                    cursor.getStringOrNull(relativePathColumn).orEmpty()
                }
            val normalizedPath = normalizePathForMatch(mediaPath)
            if (normalizedPath.isNotBlank()) {
                indexedByPath[normalizedPath] = id
                normalizedPath.pathTailsForMatch().forEach { tail ->
                    indexedByTail.putUnique(tail, id)
                }
            }
            val normalizedDisplayName = normalizePathForMatch(displayName)
            if (normalizedDisplayName.isNotBlank()) {
                indexedByFileName.putUnique(normalizedDisplayName.substringAfterLast('/'), id)
            }
        }
    }

    return entries
        .asSequence()
        .mapNotNull { entry ->
            val normalizedEntry = normalizePathForMatch(entry)
            if (normalizedEntry.isBlank()) return@mapNotNull null
            val fileName = normalizedEntry.substringAfterLast('/')
            val id =
                indexedByPath[normalizedEntry]
                    ?: normalizedEntry
                        .pathTailsForMatch()
                        .asSequence()
                        .mapNotNull { tail -> indexedByTail[tail] }
                        .firstOrNull()
                    ?: indexedByFileName[fileName]
            id?.let(tracksById::get)
        }.distinctBy { it.id }
        .toList()
}

private data class TrackMatchCandidate(
    val track: Track,
    val normalizedTitle: String,
    val normalizedArtist: String,
    val normalizedAlbum: String,
    val normalizedFolder: String,
)

private fun resolveByRelativePathHeuristics(
    entryPath: String,
    candidates: List<TrackMatchCandidate>,
): Track? {
    if (candidates.isEmpty()) return null
    val normalizedPath = normalizeForMatch(entryPath.replace('\\', '/'))
    val rawSegments = entryPath.replace('\\', '/').split('/').filter { it.isNotBlank() }
    if (rawSegments.isEmpty()) return null
    val normalizedSegments = rawSegments.map(::normalizeForMatch)
    val filename = rawSegments.last()
    val filenameStem = File(filename).nameWithoutExtension
    val titleHints = extractTitleHints(filenameStem).map(::normalizeForMatch).filter { it.isNotBlank() }
    val artistHint = normalizedSegments.getOrNull(normalizedSegments.lastIndex - 2).orEmpty()
    val albumHint = normalizedSegments.getOrNull(normalizedSegments.lastIndex - 1).orEmpty()

    val best =
        candidates.maxByOrNull { candidate ->
            var score = 0
            if (titleHints.any { it == candidate.normalizedTitle }) score += 70
            if (titleHints.any { hint -> hint.contains(candidate.normalizedTitle) || candidate.normalizedTitle.contains(hint) }) score += 35
            if (artistHint.isNotBlank() && (candidate.normalizedArtist.contains(artistHint) || artistHint.contains(candidate.normalizedArtist))) score += 20
            if (albumHint.isNotBlank() && (candidate.normalizedAlbum.contains(albumHint) || albumHint.contains(candidate.normalizedAlbum))) score += 15
            if (candidate.normalizedFolder.isNotBlank() && normalizedPath.contains(candidate.normalizedFolder)) score += 25
            score
        }
    return best?.takeIf {
        val bestTitleMatched =
            titleHints.any { hint ->
                hint == it.normalizedTitle || hint.contains(it.normalizedTitle) || it.normalizedTitle.contains(hint)
            }
        bestTitleMatched
    }?.track
}

private fun extractTitleHints(filenameStem: String): List<String> {
    if (filenameStem.isBlank()) return emptyList()
    val base = filenameStem.trim()
    val hints = linkedSetOf(base)
    base.substringAfterLast(" - ", missingDelimiterValue = base).trim().takeIf { it.isNotBlank() }?.let(hints::add)
    base.substringAfterLast(" – ", missingDelimiterValue = base).trim().takeIf { it.isNotBlank() }?.let(hints::add)
    base.replaceFirst(Regex("^\\d+\\s*[-.]\\s*"), "").trim().takeIf { it.isNotBlank() }?.let(hints::add)
    base.replaceFirst(Regex("^[A-Za-zÀ-ÖØ-öø-ÿ0-9 '&._]+\\s*-\\s*\\d+\\s*-\\s*"), "").trim()
        .takeIf { it.isNotBlank() }
        ?.let(hints::add)
    return hints.toList()
}

private fun normalizeForMatch(value: String): String {
    val ascii =
        Normalizer
            .normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
    return ascii
        .lowercase(Locale.ROOT)
        .replace('_', ' ')
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()
        .replace(Regex("\\s+"), " ")
}

private fun normalizePathForMatch(value: String): String {
    if (value.isBlank()) return ""
    val decodedInput = Uri.decode(value).trim()
    val uriPath =
        runCatching {
            val parsed = Uri.parse(decodedInput)
            if (parsed.scheme.equals("file", ignoreCase = true)) {
                parsed.path
            } else {
                null
            }
        }.getOrNull()
    val ascii =
        Normalizer
            .normalize(uriPath ?: decodedInput, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
    val slashNormalized = ascii.replace('\\', '/').trim()
    val withoutDotSegments =
        slashNormalized
            .split('/')
            .filter { segment -> segment.isNotBlank() && segment != "." && segment != ".." }
            .joinToString("/")
    val withoutVolume =
        withoutDotSegments.replace(
            Regex("^[0-9a-fA-F]{4}-[0-9a-fA-F]{4}/"),
            "",
        )
    return withoutVolume
        .lowercase(Locale.ROOT)
        .replace(Regex("[^a-z0-9/._\\-\\s]+"), "")
        .replace(Regex("/+"), "/")
        .trim('/')
}

private fun String.pathTailsForMatch(maxSegments: Int = 4): List<String> {
    val segments = split('/').filter { it.isNotBlank() }
    if (segments.isEmpty()) return emptyList()
    val maxWindow = minOf(maxSegments, segments.size)
    return (maxWindow downTo 1).map { count -> segments.takeLast(count).joinToString("/") }
}

private fun MutableMap<String, Long?>.putUnique(
    key: String,
    value: Long,
) {
    val hasKey = containsKey(key)
    val existing = this[key]
    this[key] =
        when {
            !hasKey -> value
            existing == value -> value
            else -> null
        }
}

private fun exportCustomPlaylists(playlists: List<PlaylistGroup>): String {
    val root = JSONObject()
    val array = JSONArray()
    playlists.forEach { playlist ->
        val playlistJson = JSONObject()
        playlistJson.put("name", playlist.title)
        val tracksJson = JSONArray()
        playlist.tracks.forEach { track ->
            tracksJson.put(
                JSONObject()
                    .put("uri", track.uri.toString())
                    .put("title", track.title)
                    .put("artist", track.artist)
                    .put("album", track.album),
            )
        }
        playlistJson.put("tracks", tracksJson)
        array.put(playlistJson)
    }
    root.put("format", "ams")
    root.put("version", 1)
    root.put("playlists", array)
    return root.toString(2)
}

data class CachedLibrary(
    val tracks: List<Track>,
    val favorites: Set<Long>,
    val favoriteItems: List<FavoriteItem>,
    val trackStats: Map<Long, TrackStats>,
    val snapshot: List<TrackSnapshot> = emptyList(),
)

private suspend fun LibraryDao.loadLibraryFavorites(): List<FavoriteItem> {
    val itemFavorites = loadFavoriteItems().map { FavoriteItem(it.type, it.key, it.addedAt) }
    val itemTrackIds =
        itemFavorites
            .filter { it.type == FavoriteType.Track }
            .mapNotNull { it.key.toLongOrNull() }
            .toSet()
    val legacyTrackFavorites =
        loadFavoriteIds()
            .filterNot { it in itemTrackIds }
            .map { FavoriteItem(FavoriteType.Track, it.toString(), 0L) }
    return (itemFavorites + legacyTrackFavorites).sortedByDescending { it.addedAt }
}

private fun List<FavoriteItem>.trackIds(): Set<Long> =
    filter { it.type == FavoriteType.Track }
        .mapNotNull { it.key.toLongOrNull() }
        .toSet()

private fun List<TrackStatsEntity>.toTrackStatsMap(): Map<Long, TrackStats> =
    associate { entity ->
        entity.trackId to
            TrackStats(
                trackId = entity.trackId,
                playCount = entity.playCount,
                lastPlayed = entity.lastPlayed,
                skipCount = entity.skipCount,
            )
    }

class LibraryScanner(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val dao = LibraryDatabase.get(context).libraryDao()
    private val preferences = MusicPreferences(appContext)
    private val mediaStoreScanner = MediaStoreScanner(context)
    private var customPlaylistsCache: List<PlaylistGroup> = emptyList()

    suspend fun loadCachedLibrary(): CachedLibrary =
        withContext(Dispatchers.IO) {
            val cachedTracks = dao.loadTracks()
            val audioInfo =
                dao
                    .loadAudioInfoByTrackIdsChunked(cachedTracks.map { it.trackId })
                    .associate { it.trackId to it.toAudioInfo() }
            val favoriteItems = dao.loadLibraryFavorites()
            val trackStats = dao.loadTrackStats().toTrackStatsMap()
            val tracks = cachedTracks.map { it.toTrack(audioInfo[it.trackId]) }
            customPlaylistsCache = dao.loadCustomPlaylists(tracks)
            CachedLibrary(
                tracks = tracks,
                favorites = favoriteItems.trackIds(),
                favoriteItems = favoriteItems,
                trackStats = trackStats,
                snapshot = cachedTracks.map { it.toSnapshot() },
            )
        }

    suspend fun refreshLibrary(onPartial: suspend (CachedLibrary) -> Unit = {}): CachedLibrary =
        withContext(Dispatchers.IO) {
            val favoriteItems = dao.loadLibraryFavorites()
            val favorites = favoriteItems.trackIds()
            val trackStats = dao.loadTrackStats().toTrackStatsMap()
            val audioTracks =
                mediaStoreScanner.loadTracks(dao) { partialTracks ->
                    val sortedPartial = partialTracks.sortedForLibrary()
                    onPartial(
                        CachedLibrary(
                            tracks = sortedPartial.map { it.track },
                            favorites = favorites,
                            favoriteItems = favoriteItems,
                            trackStats = trackStats,
                            snapshot = sortedPartial.map { it.toSnapshot() },
                        ),
                    )
                }
            val mediaStoreState = MediaStoreLibraryState.fromScannedTracks(audioTracks)

            dao.replaceSourceTracks(LibrarySource.MediaStore, audioTracks.map { it.toEntity() })
            val audioInfoEntities = audioTracks.mapNotNull { it.audioInfo?.toEntity() }
            dao.deleteAllAudioInfo()
            if (audioInfoEntities.isNotEmpty()) dao.upsertAudioInfo(audioInfoEntities)

            val cachedTracks = dao.loadTracks()
            val audioInfo =
                dao
                    .loadAudioInfoByTrackIdsChunked(cachedTracks.map { it.trackId })
                    .associate { it.trackId to it.toAudioInfo() }
            dao.replaceDerivedCaches(cachedTracks)
            val tracks = cachedTracks.map { it.toTrack(audioInfo[it.trackId]) }
            customPlaylistsCache = dao.loadCustomPlaylists(tracks)
            preferences.saveMediaStoreLibraryState(mediaStoreState.encode())
            preferences.saveLastLibraryRefreshMs(System.currentTimeMillis())
            CachedLibrary(
                tracks = tracks,
                favorites = favorites,
                favoriteItems = favoriteItems,
                trackStats = dao.loadTrackStats().toTrackStatsMap(),
                snapshot = cachedTracks.map { it.toSnapshot() },
            )
        }

    suspend fun shouldAutoRefreshLibrary(hasCachedTracks: Boolean): Boolean =
        withContext(Dispatchers.IO) {
            if (!hasCachedTracks) return@withContext true
            val now = System.currentTimeMillis()
            val lastRefreshMs = preferences.loadLastLibraryRefreshMs()
            if (lastRefreshMs > 0L && now - lastRefreshMs < AUTO_REFRESH_MIN_INTERVAL_MS) {
                return@withContext false
            }
            val elapsed = now - lastRefreshMs
            val cachedState =
                preferences
                    .loadMediaStoreLibraryState()
                    ?.let(MediaStoreLibraryState::decode) ?: return@withContext elapsed >= AUTO_REFRESH_FALLBACK_MS
            val currentState = mediaStoreScanner.loadLibraryState() ?: return@withContext elapsed >= AUTO_REFRESH_FALLBACK_MS
            if (!cachedState.hasUsableSignal() || !currentState.hasUsableSignal()) {
                return@withContext elapsed >= AUTO_REFRESH_FALLBACK_MS
            }
            currentState.isMeaningfullyDifferentFrom(cachedState)
        }

    suspend fun setFavorite(
        trackId: Long,
        favorite: Boolean,
    ) = withContext(Dispatchers.IO) {
        setFavoriteItem(FavoriteType.Track, trackId.toString(), favorite)
    }

    suspend fun setFavoriteItem(
        type: String,
        key: String,
        favorite: Boolean,
    ) = withContext(Dispatchers.IO) {
        if (favorite) {
            dao.upsertFavoriteItem(FavoriteItemEntity(type, key, System.currentTimeMillis()))
            if (type == FavoriteType.Track) {
                key.toLongOrNull()?.let { dao.upsertFavorite(FavoriteEntity(it)) }
            }
        } else {
            dao.deleteFavoriteItem(type, key)
            if (type == FavoriteType.Track) {
                key.toLongOrNull()?.let { dao.deleteFavorite(it) }
            }
        }
    }

    suspend fun recordTrackPlayed(
        trackId: Long,
        playedAt: Long,
    ) = withContext(Dispatchers.IO) {
        val current = dao.loadTrackStats(trackId)
        dao.upsertTrackStats(
            TrackStatsEntity(
                trackId = trackId,
                playCount = (current?.playCount ?: 0) + 1,
                lastPlayed = playedAt,
                skipCount = current?.skipCount ?: 0,
            ),
        )
    }

    suspend fun recordTrackSkipped(trackId: Long) =
        withContext(Dispatchers.IO) {
            val current = dao.loadTrackStats(trackId)
            dao.upsertTrackStats(
                TrackStatsEntity(
                    trackId = trackId,
                    playCount = current?.playCount ?: 0,
                    lastPlayed = current?.lastPlayed ?: 0L,
                    skipCount = (current?.skipCount ?: 0) + 1,
                ),
            )
        }

    suspend fun createPlaylist(
        name: String,
        tracks: List<Track>,
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val id = "custom-$now"
        dao.upsertCustomPlaylist(CustomPlaylistEntity(id, name, now))
        dao.replaceCustomPlaylistTracks(
            id,
            tracks.distinctBy { it.id }.mapIndexed { index, track ->
                CustomPlaylistTrackEntity(id, track.id, index, now)
            },
        )
        customPlaylistsCache = dao.loadCustomPlaylists(dao.loadTracks().map { it.toTrack() })
    }

    suspend fun addTracksToPlaylist(
        playlistId: String,
        tracks: List<Track>,
    ) = withContext(Dispatchers.IO) {
        val existing = dao.loadCustomPlaylistTracks().filter { it.playlistId == playlistId }
        val existingIds = existing.map { it.trackId }.toSet()
        val now = System.currentTimeMillis()
        val additions =
            tracks
                .distinctBy { it.id }
                .filterNot { it.id in existingIds }
                .mapIndexed { index, track ->
                    CustomPlaylistTrackEntity(playlistId, track.id, existing.size + index, now)
                }
        if (additions.isNotEmpty()) {
            dao.upsertCustomPlaylistTracks(additions)
            customPlaylistsCache = dao.loadCustomPlaylists(dao.loadTracks().map { it.toTrack() })
        }
    }

    suspend fun renamePlaylist(
        playlistId: String,
        name: String,
    ) = withContext(Dispatchers.IO) {
        if (!playlistId.startsWith("custom-")) return@withContext
        dao.renameCustomPlaylist(playlistId, name)
        customPlaylistsCache = dao.loadCustomPlaylists(dao.loadTracks().map { it.toTrack() })
    }

    suspend fun deletePlaylists(playlistIds: List<String>) =
        withContext(Dispatchers.IO) {
            playlistIds
                .filter { it.startsWith("custom-") }
                .distinct()
                .forEach { playlistId ->
                    dao.deleteCustomPlaylistTracks(playlistId)
                    dao.deleteCustomPlaylist(playlistId)
                    dao.deleteFavoriteItem(FavoriteType.Playlist, playlistId)
                }
            customPlaylistsCache = dao.loadCustomPlaylists(dao.loadTracks().map { it.toTrack() })
        }

    suspend fun deleteTracksPermanently(tracks: List<Track>) =
        withContext(Dispatchers.IO) {
            val uniqueTracks = tracks.distinctBy { it.id }
            uniqueTracks.forEach { track ->
                runCatching {
                    when (track.uri.scheme) {
                        "file" -> {
                            track.uri.path?.let { File(it).delete() }
                        }

                        else -> {
                            appContext.contentResolver.delete(track.uri, null, null)
                        }
                    }
                }
            }
            val ids = uniqueTracks.map { it.id }
            if (ids.isNotEmpty()) {
                dao.deleteTracksByIds(ids)
                dao.deleteAudioInfoByTrackIds(ids)
                ids.forEach {
                    dao.deleteFavorite(it)
                    dao.deleteFavoriteItem(FavoriteType.Track, it.toString())
                }
            }
            customPlaylistsCache = dao.loadCustomPlaylists(dao.loadTracks().map { it.toTrack() })
        }

    suspend fun replacePlaylistTracks(
        playlistId: String,
        tracks: List<Track>,
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        dao.replaceCustomPlaylistTracks(
            playlistId,
            tracks.distinctBy { it.id }.mapIndexed { index, track ->
                CustomPlaylistTrackEntity(playlistId, track.id, index, now)
            },
        )
        customPlaylistsCache = dao.loadCustomPlaylists(dao.loadTracks().map { it.toTrack() })
    }

    fun buildPlaylists(tracks: List<Track>): List<PlaylistGroup> {
        if (tracks.isEmpty()) return emptyList()
        return customPlaylistsCache.filter { playlist -> playlist.tracks.any { track -> track in tracks } } +
            listOf(
                PlaylistGroup("recently-added", "Recently added", tracks.take(50), tracks.firstOrNull()?.albumArtUri),
                PlaylistGroup("most-played", "Most played", tracks.sortedBy { it.title }.take(50), tracks.getOrNull(1)?.albumArtUri),
            )
    }

    companion object {
        private const val AUTO_REFRESH_MIN_INTERVAL_MS = 30 * 60 * 1000L
        private const val AUTO_REFRESH_FALLBACK_MS = 6 * 60 * 60 * 1000L
        private const val SQLITE_IN_LIMIT = 900
    }

    private suspend fun LibraryDao.loadAudioInfoByTrackIdsChunked(trackIds: List<Long>): List<AudioInfoEntity> {
        if (trackIds.isEmpty()) return emptyList()
        val result = ArrayList<AudioInfoEntity>(trackIds.size)
        trackIds
            .distinct()
            .chunked(SQLITE_IN_LIMIT)
            .forEach { chunk -> result += loadAudioInfo(chunk) }
        return result
    }
}

private suspend fun LibraryDao.loadCustomPlaylists(tracks: List<Track>): List<PlaylistGroup> {
    val tracksById = tracks.associateBy { it.id }
    val playlistTracks = loadCustomPlaylistTracks().groupBy { it.playlistId }
    return loadCustomPlaylists().map { playlist ->
        val items =
            playlistTracks[playlist.playlistId]
                .orEmpty()
                .sortedBy { it.position }
                .mapNotNull { tracksById[it.trackId] }
        PlaylistGroup(
            id = playlist.playlistId,
            title = playlist.name,
            tracks = items,
            artwork = items.firstOrNull()?.albumArtUri,
            createdAt = playlist.createdAt,
        )
    }
}

data class MediaStoreLibraryState(
    val trackCount: Int,
    val latestModifiedSeconds: Long,
    val latestAddedSeconds: Long,
    val idSum: Long,
    val sizeSum: Long,
    val modifiedSumSeconds: Long,
) {
    fun encode(): String = "$trackCount:$latestModifiedSeconds:$latestAddedSeconds:$idSum:$sizeSum:$modifiedSumSeconds"

    fun hasUsableSignal(): Boolean =
        trackCount > 0 ||
            latestModifiedSeconds > 0L ||
            latestAddedSeconds > 0L ||
            idSum > 0L ||
            sizeSum > 0L ||
            modifiedSumSeconds > 0L

    fun isMeaningfullyDifferentFrom(other: MediaStoreLibraryState): Boolean {
        if (trackCount != other.trackCount) return true
        if (latestModifiedSeconds > 0L && other.latestModifiedSeconds > 0L && latestModifiedSeconds != other.latestModifiedSeconds) {
            return true
        }
        if (latestAddedSeconds > 0L && other.latestAddedSeconds > 0L && latestAddedSeconds != other.latestAddedSeconds) {
            return true
        }
        if (sizeSum > 0L && other.sizeSum > 0L && sizeSum != other.sizeSum) return true
        if (modifiedSumSeconds > 0L && other.modifiedSumSeconds > 0L && modifiedSumSeconds != other.modifiedSumSeconds) {
            return true
        }
        if (idSum > 0L && other.idSum > 0L && idSum != other.idSum) return true
        return false
    }

    companion object {
        fun decode(value: String): MediaStoreLibraryState? {
            val parts = value.split(':')
            if (parts.size != 3 && parts.size != 6) return null
            val count = parts[0].toIntOrNull() ?: return null
            val latestModified = parts[1].toLongOrNull() ?: return null
            val latestAdded = parts[2].toLongOrNull() ?: return null
            return MediaStoreLibraryState(
                trackCount = count.coerceAtLeast(0),
                latestModifiedSeconds = latestModified.coerceAtLeast(0L),
                latestAddedSeconds = latestAdded.coerceAtLeast(0L),
                idSum = parts.getOrNull(3)?.toLongOrNull()?.coerceAtLeast(0L) ?: 0L,
                sizeSum = parts.getOrNull(4)?.toLongOrNull()?.coerceAtLeast(0L) ?: 0L,
                modifiedSumSeconds = parts.getOrNull(5)?.toLongOrNull()?.coerceAtLeast(0L) ?: 0L,
            )
        }

        fun fromScannedTracks(tracks: List<ScannedTrack>): MediaStoreLibraryState =
            MediaStoreLibraryState(
                trackCount = tracks.size,
                latestModifiedSeconds = tracks.maxOfOrNull { it.lastModifiedMs / 1_000L } ?: 0L,
                latestAddedSeconds = tracks.maxOfOrNull { it.track.dateAddedMs / 1_000L } ?: 0L,
                idSum = tracks.fold(0L) { acc, item -> acc + item.track.id.coerceAtLeast(0L) },
                sizeSum = tracks.fold(0L) { acc, item -> acc + item.sizeBytes.coerceAtLeast(0L) },
                modifiedSumSeconds = tracks.fold(0L) { acc, item -> acc + (item.lastModifiedMs / 1_000L).coerceAtLeast(0L) },
            )
    }
}

class MediaStoreScanner(
    private val context: Context,
) {
    suspend fun loadLibraryState(): MediaStoreLibraryState? =
        withContext(Dispatchers.IO) {
            val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection =
                arrayOf(
                    "COUNT(${MediaStore.Audio.Media._ID}) AS track_count",
                    "MAX(${MediaStore.Audio.Media.DATE_MODIFIED}) AS latest_modified",
                    "MAX(${MediaStore.Audio.Media.DATE_ADDED}) AS latest_added",
                    "SUM(${MediaStore.Audio.Media._ID}) AS id_sum",
                    "SUM(${MediaStore.Audio.Media.SIZE}) AS size_sum",
                    "SUM(${MediaStore.Audio.Media.DATE_MODIFIED}) AS modified_sum",
                )
            runCatching {
                context.contentResolver
                    .query(
                        collection,
                        projection,
                        "${MediaStore.Audio.Media.IS_MUSIC} != 0",
                        null,
                        null,
                    )?.use { cursor ->
                        if (!cursor.moveToFirst()) return@use null
                        MediaStoreLibraryState(
                            trackCount = cursor.getInt(0).coerceAtLeast(0),
                            latestModifiedSeconds = if (cursor.isNull(1)) 0L else cursor.getLong(1).coerceAtLeast(0L),
                            latestAddedSeconds = if (cursor.isNull(2)) 0L else cursor.getLong(2).coerceAtLeast(0L),
                            idSum = if (cursor.isNull(3)) 0L else cursor.getLong(3).coerceAtLeast(0L),
                            sizeSum = if (cursor.isNull(4)) 0L else cursor.getLong(4).coerceAtLeast(0L),
                            modifiedSumSeconds = if (cursor.isNull(5)) 0L else cursor.getLong(5).coerceAtLeast(0L),
                        )
                    }
            }.getOrNull()
        }

    suspend fun loadTracks(
        dao: LibraryDao,
        onPartial: suspend (List<ScannedTrack>) -> Unit = {},
    ): List<ScannedTrack> {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection =
            buildList {
                add(MediaStore.Audio.Media._ID)
                add(MediaStore.Audio.Media.TITLE)
                add(MediaStore.Audio.Media.ARTIST)
                add(MediaStore.Audio.Media.ALBUM)
                add(MediaStore.Audio.Media.DURATION)
                add(MediaStore.Audio.Media.ALBUM_ID)
                add(MediaStore.Audio.Media.TRACK)
                add(MediaStore.Audio.Media.YEAR)
                add(MediaStore.Audio.Media.DISPLAY_NAME)
                add(MediaStore.Audio.Media.DATE_ADDED)
                add(MediaStore.Audio.Media.DATE_MODIFIED)
                add(MediaStore.Audio.Media.SIZE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) add(MediaStore.Audio.Media.RELATIVE_PATH)
                @Suppress("DEPRECATION")
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) add(MediaStore.Audio.Media.DATA)
            }.toTypedArray()

        val rawScanned = mutableListOf<ScannedTrack>()
        var emittedCount = 0
        context.contentResolver
            .query(
                collection,
                projection,
                "${MediaStore.Audio.Media.IS_MUSIC} != 0",
                null,
                "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC",
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val trackNumberColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val relativePathColumn =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
                    val trackMetadata = cursor.getInt(trackNumberColumn).toTrackMetadata()
                    val rawTrack =
                        ScannedTrack(
                            track =
                                Track(
                                    id = id,
                                    title = title,
                                    artist = artist,
                                    album = album,
                                    durationMs = cursor.getLong(durationColumn).coerceAtLeast(0L),
                                    uri = uri,
                                    albumId = cursor.getLong(albumIdColumn),
                                    folder = folder,
                                    trackNumber = trackMetadata.trackNumber,
                                    discNumber = trackMetadata.discNumber,
                                    year = cursor.getInt(yearColumn).takeIf { it > 0 } ?: 0,
                                    dateAddedMs = cursor.getLong(dateAddedColumn).coerceAtLeast(0L) * 1_000L,
                                ),
                            cacheKey = uri.toString(),
                            source = LibrarySource.MediaStore,
                            lastModifiedMs = cursor.getLong(dateModifiedColumn).coerceAtLeast(0L) * 1_000L,
                            sizeBytes = cursor.getLong(sizeColumn).coerceAtLeast(0L),
                        )
                    rawScanned += rawTrack
                    if (rawScanned.size - emittedCount >= PARTIAL_EMIT_BATCH_SIZE) {
                        emittedCount = rawScanned.size
                        onPartial(rawScanned.toList())
                    }
                }
            }
        if (rawScanned.isNotEmpty() && emittedCount != rawScanned.size) {
            onPartial(rawScanned.toList())
        }
        return rawScanned.reuseUnchangedCache(dao)
    }

    private suspend fun List<ScannedTrack>.reuseUnchangedCache(dao: LibraryDao): List<ScannedTrack> {
        if (isEmpty()) return emptyList()
        val cached = dao.loadTracksByCacheKeyChunked(map { it.cacheKey }).associateBy { it.cacheKey }
        val cachedAudioInfo =
            dao
                .loadAudioInfoByTrackIdsChunked(cached.values.map { it.trackId })
                .associate { it.trackId to it.toAudioInfo() }
        val resolved = MutableList<ScannedTrack?>(size) { null }
        forEachIndexed { index, scanned ->
            val cachedTrack = cached[scanned.cacheKey]
            if (
                cachedTrack != null &&
                cachedTrack.lastModifiedMs == scanned.lastModifiedMs &&
                cachedTrack.sizeBytes == scanned.sizeBytes &&
                cachedTrack.trackNumber == scanned.track.trackNumber &&
                cachedTrack.discNumber == scanned.track.discNumber &&
                cachedTrack.dateAddedMs == scanned.track.dateAddedMs
            ) {
                val audioInfo = cachedAudioInfo[cachedTrack.trackId]
                resolved[index] = scanned.copy(track = cachedTrack.toTrack(audioInfo), audioInfo = audioInfo)
            } else {
                resolved[index] = scanned.copy(track = scanned.track.copy(audioInfo = null), audioInfo = null)
            }
        }
        return resolved.mapNotNull { it }
    }

    private suspend fun LibraryDao.loadAudioInfoByTrackIdsChunked(trackIds: List<Long>): List<AudioInfoEntity> {
        if (trackIds.isEmpty()) return emptyList()
        val result = ArrayList<AudioInfoEntity>(trackIds.size)
        trackIds
            .distinct()
            .chunked(SQLITE_IN_LIMIT)
            .forEach { chunk -> result += loadAudioInfo(chunk) }
        return result
    }

    private suspend fun LibraryDao.loadTracksByCacheKeyChunked(cacheKeys: List<String>): List<TrackEntity> {
        if (cacheKeys.isEmpty()) return emptyList()
        val result = ArrayList<TrackEntity>(cacheKeys.size)
        cacheKeys
            .distinct()
            .chunked(SQLITE_IN_LIMIT)
            .forEach { chunk -> result += loadTracksByCacheKey(chunk) }
        return result
    }

    companion object {
        private const val PARTIAL_EMIT_BATCH_SIZE = 250
        private const val SQLITE_IN_LIMIT = 900
    }
}

private fun List<Track>.filterVisible(hiddenFolders: Set<String>): List<Track> {
    if (hiddenFolders.isEmpty()) return this
    return filterNot { track -> track.folder in hiddenFolders }
}

private fun LibraryState.withDerivedCollections(): LibraryState {
    val derivedTracksById = tracks.associateBy(Track::id)
    val derivedFavoriteTracks = tracks.filter { it.id in favorites }
    val derivedAlbums =
        tracks
            .groupBy { it.albumId }
            .values
            .map { items -> AlbumGroup(items.first().albumId, items.first().album, items.first().artist, items) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
    val derivedArtists =
        tracks
            .groupBy { it.artist.ifBlank { "Unknown artist" } }
            .map { (name, items) -> ArtistGroup(name, items.map { it.albumId }.distinct().size, items) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    val derivedFolders = tracks.toFolderGroups()
    val derivedAllFolders = allTracks.toFolderGroups()
    return copy(
        favoriteTracks = derivedFavoriteTracks,
        albums = derivedAlbums,
        artists = derivedArtists,
        folders = derivedFolders,
        allFolders = derivedAllFolders,
        tracksById = derivedTracksById,
        albumsById = derivedAlbums.associateBy { it.id },
        artistsByName = derivedArtists.associateBy { it.name },
        foldersByPath = derivedFolders.associateBy { it.path },
    )
}

private fun List<Track>.toFolderGroups(): List<FolderGroup> =
    groupBy { it.folder.ifBlank { "Unknown folder" } }
        .map { (path, items) -> FolderGroup(path.substringAfterLast('/').ifBlank { path }, path, items) }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.path })

private fun Context.extractAudioInfo(track: Track): AudioInfo? = extractMediaAudioInfo(track)

private fun Context.extractMediaAudioInfo(track: Track): AudioInfo? =
    runCatching {
        val extractor = MediaExtractor()
        try {
            contentResolver.openAssetFileDescriptor(track.uri, "r")?.use { descriptor ->
                if (descriptor.length >= 0) {
                    extractor.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                } else {
                    extractor.setDataSource(descriptor.fileDescriptor)
                }
            } ?: return@runCatching null
            val format =
                (0 until extractor.trackCount)
                    .asSequence()
                    .map { extractor.getTrackFormat(it) }
                    .firstOrNull { it.getStringOrNull(MediaFormat.KEY_MIME)?.startsWith("audio/") == true }
                    ?: return@runCatching null
            AudioInfo(
                trackId = track.id,
                codec = format.getStringOrNull(MediaFormat.KEY_MIME)?.toAudioCodecLabel(track.uri),
                sampleRate = format.getIntOrNull(MediaFormat.KEY_SAMPLE_RATE),
                bitDepth = format.getIntOrNull("bits-per-sample") ?: format.getIntOrNull("pcm-encoding").pcmEncodingBitDepth(),
                bitrate = format.getIntOrNull(MediaFormat.KEY_BIT_RATE),
                channels = format.getIntOrNull(MediaFormat.KEY_CHANNEL_COUNT),
            )
        } finally {
            extractor.release()
        }
    }.getOrNull()

private fun MediaFormat.getStringOrNull(key: String): String? = runCatching { if (containsKey(key)) getString(key) else null }.getOrNull()

private fun MediaFormat.getIntOrNull(key: String): Int? =
    runCatching { if (containsKey(key)) getInteger(key) else null }.getOrNull()?.takeIf { it > 0 }

private fun Int?.pcmEncodingBitDepth(): Int? =
    when (this) {
        2 -> 16
        3 -> 8
        4 -> 32
        21 -> 24
        22 -> 32
        else -> null
    }

private fun String.toAudioCodecLabel(uri: Uri): String {
    val mime = substringAfter("audio/", this).substringBefore(';').lowercase(Locale.ROOT)
    val label =
        when {
            mime.contains("flac") -> "FLAC"
            mime.contains("mpeg") || mime == "mp3" -> "MP3"
            mime.contains("mp4a") || mime.contains("aac") -> "AAC"
            mime.contains("opus") -> "OPUS"
            mime.contains("vorbis") || mime.contains("ogg") -> "OGG"
            mime.contains("wav") || mime.contains("raw") -> "PCM"
            else -> null
        }
    return label
        ?: uri.lastPathSegment
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.uppercase(Locale.ROOT)
            ?.ifBlank { null }
        ?: mime.uppercase(Locale.ROOT)
}

private data class TrackMetadata(
    val discNumber: Int,
    val trackNumber: Int,
)

private fun Int.toTrackMetadata(): TrackMetadata {
    val normalized = this % 1_000
    val disc = (this / 1_000).takeIf { it > 0 } ?: 0
    val track = normalized.takeIf { it > 0 } ?: takeIf { it > 0 } ?: 0
    return TrackMetadata(discNumber = disc, trackNumber = track)
}

private fun List<ScannedTrack>.sortedForLibrary(): List<ScannedTrack> = sortedBy { it.track.title.lowercase(Locale.getDefault()) }

private fun android.database.Cursor.getStringOrNull(column: Int): String? = if (column >= 0 && !isNull(column)) getString(column) else null

@Composable
fun MusicApp(
    openPlayerRequest: Int = 0,
    openViewUri: Uri? = null,
    onOpenViewUriConsumed: () -> Unit = {},
    viewModel: MusicViewModel = viewModel(),
) {
    val context = LocalContext.current
    val navigator = rememberMusicNavigator()
    val permission = remember { audioPermission() }
    var permissionGranted by remember {
        mutableStateOf(hasLibraryPermission(context, permission))
    }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            permissionGranted = hasLibraryPermission(context, permission)
            if (permissionGranted) {
                viewModel.loadLibrary(true)
            } else {
                viewModel.loadLibrary(false)
            }
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
    var fancyBackgroundEnabled by remember { mutableStateOf(preferences.loadFancyBackgroundEnabled()) }
    var lastHomeTab by rememberSaveable { mutableStateOf(preferences.loadLastTab(HomeTab.Favorite.ordinal)) }
    var playlistEditMode by rememberSaveable { mutableStateOf(false) }
    var pendingDeleteTracks by remember { mutableStateOf(emptyList<Track>()) }
    val deleteRequestLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.deleteTracksPermanently(pendingDeleteTracks)
            }
            pendingDeleteTracks = emptyList()
        }
    var pendingExportText by remember { mutableStateOf<String?>(null) }
    val importPlaylistLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let(viewModel::importPlaylist)
        }
    val exportPlaylistLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            val text = pendingExportText
            pendingExportText = null
            if (uri != null && text != null) {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(text.toByteArray())
                }
            }
        }

    fun requestPermanentDelete(tracks: List<Track>) {
        val uniqueTracks = tracks.distinctBy { it.id }
        if (uniqueTracks.isEmpty()) return
        val contentUris = uniqueTracks.map { it.uri }.filter { it.scheme == "content" }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && contentUris.isNotEmpty()) {
            pendingDeleteTracks = uniqueTracks
            val request = MediaStore.createDeleteRequest(context.contentResolver, contentUris)
            deleteRequestLauncher.launch(IntentSenderRequest.Builder(request.intentSender).build())
        } else {
            viewModel.deleteTracksPermanently(uniqueTracks)
        }
    }

    fun shareTrack(track: Track) {
        val intent =
            Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, track.uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        context.startActivity(Intent.createChooser(intent, null))
    }
    val hidePlayerSheet = navigator.currentRoute == MainRoute.Settings || playlistEditMode
    var playerExpandRequest by rememberSaveable { mutableIntStateOf(0) }
    var playerPlaylistExpandRequest by rememberSaveable { mutableIntStateOf(0) }
    val showFancyBackground =
        fancyBackgroundEnabled &&
            navigator.currentRoute != MainRoute.Settings &&
            playerState.currentTrack?.albumId?.let { it != 0L } == true

    LaunchedEffect(openPlayerRequest) {
        if (openPlayerRequest > 0) {
            while (navigator.canGoBack()) {
                navigator.back()
            }
            playerExpandRequest = openPlayerRequest
        }
    }

    LaunchedEffect(openViewUri) {
        openViewUri?.let { uri ->
            viewModel.playExternalUri(uri)
            playerExpandRequest++
            onOpenViewUriConsumed()
        }
    }

    DisposableEffect(preferences) {
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                fancyBackgroundEnabled = preferences.loadFancyBackgroundEnabled()
            }
        preferences.registerSettingsListener(listener)
        onDispose { preferences.unregisterSettingsListener(listener) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (showFancyBackground) {
                FancyAppBackground(
                    artwork = playerState.currentTrack?.albumArtUri,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            MainScreen(
                library = library,
                navigator = navigator,
                currentQueue = playerState.queue,
                currentTrackId = playerState.currentTrack?.id,
                fancyBackgroundEnabled = showFancyBackground,
                onRequestPermission = {
                    if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(permission)
                    } else {
                        permissionGranted = true
                        viewModel.loadLibrary(true)
                    }
                },
                onTrackClick = viewModel::playTrack,
                onToggleFavorite = viewModel::toggleFavorite,
                onToggleFavoriteItem = viewModel::toggleFavoriteItem,
                onRescan = viewModel::rescanLibrary,
                onHiddenFoldersChanged = viewModel::setHiddenFolders,
                onImportPlaylist = {
                    importPlaylistLauncher.launch(
                        arrayOf(
                            "audio/x-mpegurl",
                            "application/vnd.apple.mpegurl",
                            "application/json",
                            "text/plain",
                            "*/*",
                        ),
                    )
                },
                onExportPlaylists = {
                    pendingExportText = viewModel.exportPlaylistsJson()
                    exportPlaylistLauncher.launch("playlists")
                },
                onCreatePlaylist = viewModel::createPlaylist,
                onAddTracksToPlaylist = viewModel::addTracksToPlaylist,
                onAddTracksToCurrentQueue = viewModel::addTracksToCurrentQueue,
                onReplaceCurrentQueue = viewModel::replaceCurrentQueue,
                onReplacePlaylistTracks = viewModel::replacePlaylistTracks,
                onRenamePlaylist = viewModel::renamePlaylist,
                onDeletePlaylists = viewModel::deletePlaylists,
                onDeleteTracksPermanently = ::requestPermanentDelete,
                onAddTracksToRoute = { tracks -> navigator.navigate(MainRoute.AddToTracks(tracks.map { it.id })) },
                onAddTracksToCurrentQueueRoute = { navigator.navigate(MainRoute.AddTracksToCurrentQueue) },
                onCurrentQueueTracksAdded = { playerPlaylistExpandRequest++ },
                onPlaylistEditModeChanged = { playlistEditMode = it },
                initialTabIndex = lastHomeTab,
                onTabSelected = { tabIndex ->
                    lastHomeTab = tabIndex
                    preferences.saveLastTab(tabIndex)
                },
            )

            if (!hidePlayerSheet) {
                PlayerSheet(
                    playerState = playerState,
                    isFavorite = playerState.currentTrack?.id in library.favorites,
                    expandRequest = playerExpandRequest,
                    playlistExpandRequest = playerPlaylistExpandRequest,
                    onExpandRequestConsumed = { playerExpandRequest = 0 },
                    onPlaylistExpandRequestConsumed = { playerPlaylistExpandRequest = 0 },
                    onSettings = { navigator.navigate(MainRoute.Settings) },
                    onAddTo = { track -> navigator.navigate(MainRoute.AddToPlaylist(track.id)) },
                    onDeleteTrack = { track -> requestPermanentDelete(listOf(track)) },
                    onShareTrack = ::shareTrack,
                    onAlbum = { track ->
                        library.albumsById[track.albumId]?.let { album ->
                            navigator.navigate(MainRoute.Album(album.id))
                        }
                    },
                    onArtist = { track ->
                        library.artistsByName[track.artist]?.let { artist ->
                            navigator.navigate(MainRoute.Artist(artist.name))
                        }
                    },
                    onChannelOutput = { navigator.navigate(MainRoute.Settings) },
                    onAddTracksToCurrentQueueRoute = { navigator.navigate(MainRoute.AddTracksToCurrentQueue) },
                    onReplaceCurrentQueue = viewModel::replaceCurrentQueue,
                    onToggleFavorite = { playerState.currentTrack?.id?.let(viewModel::toggleFavorite) },
                    onPlayPause = viewModel::togglePlayPause,
                    onPrevious = viewModel::previous,
                    onNext = viewModel::next,
                    onSeek = viewModel::seekTo,
                    onShuffle = viewModel::toggleShuffle,
                    onQueueTrackClick = { track -> viewModel.playTrack(track, playerState.queue) },
                    onRepeat = viewModel::toggleRepeat,
                    onTrackMetadataUpdated = viewModel::rescanLibrary,
                )
            }
        }
    }
}

@Composable
private fun FancyAppBackground(
    artwork: Uri?,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    if (artwork == null) return
    Box(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
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

private fun audioPermission(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

private fun hasLibraryPermission(
    context: Context,
    audioPermission: String,
): Boolean {
    return ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED
}

private fun notificationPermission(): String? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.POST_NOTIFICATIONS
    } else {
        null
    }
