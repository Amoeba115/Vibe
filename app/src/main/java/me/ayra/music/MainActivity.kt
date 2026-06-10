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
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import me.ayra.music.data.FavoriteEntity
import me.ayra.music.data.FavoriteItemEntity
import me.ayra.music.data.CustomPlaylistEntity
import me.ayra.music.data.CustomPlaylistTrackEntity
import me.ayra.music.data.LibraryDao
import me.ayra.music.data.LibraryDatabase
import me.ayra.music.data.LibrarySource
import me.ayra.music.data.ScannedTrack
import me.ayra.music.data.TrackSnapshot
import me.ayra.music.data.TrackStatsEntity
import me.ayra.music.data.toEntity
import me.ayra.music.data.toSnapshot
import me.ayra.music.data.toTrack
import me.ayra.music.data.toVgmMetadata
import me.ayra.music.ui.home.HomeTab
import me.ayra.music.ui.home.MainScreen
import me.ayra.music.ui.navigation.MainRoute
import me.ayra.music.ui.navigation.rememberMusicNavigator
import me.ayra.music.ui.player.PlayerSheet
import me.ayra.music.ui.theme.MusicTheme
import me.ayra.music.ui.theme.ThemeMode
import me.ayra.music.util.MusicPreferences
import java.io.File
import java.util.Locale

const val EXTRA_OPEN_FULLSCREEN_PLAYER = "me.ayra.music.extra.OPEN_FULLSCREEN_PLAYER"

class MainActivity : ComponentActivity() {
    private var openPlayerRequest by mutableIntStateOf(0)

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
    val trackNumber: Int = 0,
    val discNumber: Int = 0,
    val year: Int = 0,
    val dateAddedMs: Long = 0L,
) {
    val albumArtUri: Uri
        get() = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
}

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
    val permissionGranted: Boolean = false,
    val allTracks: List<Track> = emptyList(),
    val tracks: List<Track> = emptyList(),
    val favorites: Set<Long> = emptySet(),
    val favoriteItems: List<FavoriteItem> = emptyList(),
    val playlists: List<PlaylistGroup> = emptyList(),
    val trackStats: Map<Long, TrackStats> = emptyMap(),
    val hiddenFolders: Set<String> = emptySet(),
    val error: String? = null,
) {
    val favoriteTracks: List<Track> get() = tracks.filter { it.id in favorites }

    fun isFavoriteItem(
        type: String,
        key: String,
    ): Boolean = favoriteItems.any { it.type == type && it.key == key }

    val albums: List<AlbumGroup>
        get() =
            tracks
                .groupBy { it.albumId }
                .map { (_, items) -> AlbumGroup(items.first().albumId, items.first().album, items.first().artist, items) }
                .sortedBy { it.title.lowercase(Locale.getDefault()) }
    val artists: List<ArtistGroup>
        get() =
            tracks
                .groupBy { it.artist.ifBlank { "Unknown artist" } }
                .map { (name, items) -> ArtistGroup(name, items.map { it.albumId }.distinct().size, items) }
                .sortedBy { it.name.lowercase(Locale.getDefault()) }
    val folders: List<FolderGroup>
        get() =
            tracks
                .groupBy { it.folder.ifBlank { "Unknown folder" } }
                .map { (path, items) -> FolderGroup(path.substringAfterLast('/').ifBlank { path }, path, items) }
                .sortedBy { it.path.lowercase(Locale.getDefault()) }
    val allFolders: List<FolderGroup>
        get() =
            allTracks
                .groupBy { it.folder.ifBlank { "Unknown folder" } }
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

class MusicViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val libraryScanner = LibraryScanner(application)
    private val preferences = MusicPreferences(application)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var restoredTrack = false
    private var lastSavedTrackId = -1L
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

                                        override fun onMediaItemTransition(
                                            mediaItem: MediaItem?,
                                            reason: Int,
                                        ) = publishPlayerState()

                                        override fun onPlaybackStateChanged(playbackState: Int) = publishPlayerState()

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
                        publishPlayerState()
                    },
                    ContextCompat.getMainExecutor(context),
                )
            }
    }

    fun loadLibrary(permissionGranted: Boolean) {
        _library.update { it.copy(permissionGranted = permissionGranted, loading = permissionGranted, scanning = permissionGranted, error = null) }
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
                            allTracks = cached.tracks,
                            tracks = visibleTracks,
                            favorites = cached.favorites,
                            favoriteItems = cached.favoriteItems,
                            playlists = libraryScanner.buildPlaylists(visibleTracks),
                            trackStats = cached.trackStats,
                            hiddenFolders = hiddenFolders,
                            error = null,
                        )
                    }
                    restoreOrSyncPlayerQueue(visibleTracks)
                }
            if (!permissionGranted) {
                _library.update { it.copy(loading = false, scanning = false) }
                return@launch
            }

            runCatching {
                libraryScanner.refreshLibrary { partial ->
                    if (partial.tracks.isNotEmpty()) {
                        withContext(Dispatchers.Main.immediate) {
                            if (activeSnapshot.isNotEmpty()) return@withContext
                            if (partial.snapshot == activeSnapshot) return@withContext
                            activeSnapshot = partial.snapshot
                            val hiddenFolders = preferences.loadHiddenFolders()
                            val visibleTracks = partial.tracks.filterVisible(hiddenFolders)
                            _library.update {
                                it.copy(
                                    loading = false,
                                    allTracks = partial.tracks,
                                    tracks = visibleTracks,
                                    favorites = partial.favorites,
                                    favoriteItems = partial.favoriteItems,
                                    playlists = libraryScanner.buildPlaylists(visibleTracks),
                                    trackStats = partial.trackStats,
                                    hiddenFolders = hiddenFolders,
                                    error = null,
                                )
                            }
                            restoreOrSyncPlayerQueue(visibleTracks)
                        }
                    }
                }
            }.onSuccess { refreshed ->
                if (refreshed.snapshot == activeSnapshot) {
                    _library.update { it.copy(loading = false, scanning = false, error = null) }
                    return@onSuccess
                }
                val hiddenFolders = preferences.loadHiddenFolders()
                val visibleTracks = refreshed.tracks.filterVisible(hiddenFolders)
                _library.update {
                    it.copy(
                        loading = false,
                        scanning = false,
                        allTracks = refreshed.tracks,
                        tracks = visibleTracks,
                        favorites = refreshed.favorites,
                        favoriteItems = refreshed.favoriteItems,
                        playlists = libraryScanner.buildPlaylists(visibleTracks),
                        trackStats = refreshed.trackStats,
                        hiddenFolders = hiddenFolders,
                        error = null,
                    )
                }
                restoreOrSyncPlayerQueue(visibleTracks)
            }.onFailure { throwable ->
                _library.update { it.copy(loading = false, scanning = false, error = throwable.message ?: "Unable to load music") }
            }
        }
    }

    fun rescanLibrary() {
        loadLibrary(_library.value.permissionGranted)
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
        recordTrackPlayed(track.id)
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
        if (player.mediaItemCount == 0) return
        _playerState.value.currentTrack?.id?.let(::recordTrackSkipped)
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
        _playerState.value.currentTrack?.id?.let(::recordTrackSkipped)
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
            )
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
            state.copy(favorites = favorites, favoriteItems = updatedItems)
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
                val updatedTracks = state.tracks.filterNot { it.id in deletedIds }
                state.copy(
                    tracks = updatedTracks,
                    favorites = state.favorites - deletedIds,
                    favoriteItems = state.favoriteItems.filterNot { it.type == FavoriteType.Track && it.key.toLongOrNull() in deletedIds },
                    playlists = libraryScanner.buildPlaylists(updatedTracks),
                )
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
        saveLastQueue(tracks)
        tracks.firstOrNull()?.let {
            preferences.saveLastTrackId(it.id)
            lastSavedTrackId = it.id
            recordTrackPlayed(it.id)
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
            )
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
    private val mediaStoreScanner = MediaStoreScanner(context)
    private val vgmFileScanner = VgmFileScanner(context)
    private var customPlaylistsCache: List<PlaylistGroup> = emptyList()

    suspend fun loadCachedLibrary(): CachedLibrary =
        withContext(Dispatchers.IO) {
            val cachedTracks = dao.loadTracks()
            val favoriteItems = dao.loadLibraryFavorites()
            val trackStats = dao.loadTrackStats().toTrackStatsMap()
            val tracks = cachedTracks.map { it.toTrack() }
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
            val vgmTracks =
                if (BuildConfig.IS_VGM_BUILD) {
                    vgmFileScanner.loadTracks(dao) { partialTracks ->
                        val sortedPartial =
                            (audioTracks + partialTracks)
                                .distinctBy { it.track.uri }
                                .sortedForLibrary()
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
                } else {
                    emptyList()
                }

            dao.replaceSourceTracks(LibrarySource.MediaStore, audioTracks.map { it.toEntity() })
            if (BuildConfig.IS_VGM_BUILD) {
                val vgmEntities = vgmTracks.map { it.toEntity() }
                dao.replaceSourceTracks(LibrarySource.Vgm, vgmEntities)
                dao.replaceVgmMetadata(vgmEntities.map { it.toVgmMetadata() })
            } else {
                dao.replaceSourceTracks(LibrarySource.Vgm, emptyList())
            }

            val cachedTracks = dao.loadTracks()
            dao.replaceDerivedCaches(cachedTracks)
            val tracks = cachedTracks.map { it.toTrack() }
            customPlaylistsCache = dao.loadCustomPlaylists(tracks)
            CachedLibrary(
                tracks = tracks,
                favorites = favorites,
                favoriteItems = favoriteItems,
                trackStats = dao.loadTrackStats().toTrackStatsMap(),
                snapshot = cachedTracks.map { it.toSnapshot() },
            )
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
                        "file" -> track.uri.path?.let { File(it).delete() }
                        else -> dao.loadTracks()
                            .firstOrNull { it.trackId == track.id }
                            ?.source
                            ?.takeIf { it == LibrarySource.Vgm }
                            ?.let { track.uri.path?.let { path -> File(path).delete() } }
                            ?: appContext.contentResolver.delete(track.uri, null, null)
                    }
                }
            }
            val ids = uniqueTracks.map { it.id }
            if (ids.isNotEmpty()) {
                dao.deleteTracksByIds(ids)
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
        return customPlaylistsCache.filter { playlist -> playlist.tracks.any { track -> track in tracks } } + listOf(
            PlaylistGroup("recently-added", "Recently added", tracks.take(50), tracks.firstOrNull()?.albumArtUri),
            PlaylistGroup("most-played", "Most played", tracks.sortedBy { it.title }.take(50), tracks.getOrNull(1)?.albumArtUri),
        )
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

class MediaStoreScanner(
    private val context: Context,
) {
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

        val scanned = mutableListOf<ScannedTrack>()
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
                    scanned +=
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
                    if (scanned.size == 1) {
                        onPartial(scanned.reuseUnchangedCache(dao))
                    }
                }
            }
        return scanned.reuseUnchangedCache(dao)
    }

    private suspend fun List<ScannedTrack>.reuseUnchangedCache(dao: LibraryDao): List<ScannedTrack> {
        if (isEmpty()) return emptyList()
        val cached = dao.loadTracksByCacheKey(map { it.cacheKey }).associateBy { it.cacheKey }
        return map { scanned ->
            val cachedTrack = cached[scanned.cacheKey]
            if (
                cachedTrack != null &&
                cachedTrack.lastModifiedMs == scanned.lastModifiedMs &&
                cachedTrack.sizeBytes == scanned.sizeBytes &&
                cachedTrack.trackNumber == scanned.track.trackNumber &&
                cachedTrack.discNumber == scanned.track.discNumber &&
                cachedTrack.dateAddedMs == scanned.track.dateAddedMs
            ) {
                scanned.copy(track = cachedTrack.toTrack())
            } else {
                scanned
            }
        }
    }
}

private fun List<Track>.filterVisible(hiddenFolders: Set<String>): List<Track> {
    if (hiddenFolders.isEmpty()) return this
    return filterNot { track -> track.folder in hiddenFolders }
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

class VgmFileScanner(
    private val context: Context,
) {
    suspend fun loadTracks(
        dao: LibraryDao,
        onPartial: suspend (List<ScannedTrack>) -> Unit = {},
    ): List<ScannedTrack> {
        val files = mutableListOf<File>()
        scanRoots().forEach { root ->
            root.walkReadableFiles { file ->
                if (!file.isVgmFile()) return@walkReadableFiles
                files += file
                if (files.size == 1) {
                    onPartial(listOf(file.toScannedTrack(emptyMap())))
                }
            }
        }
        val cached =
            if (files.isEmpty()) {
                emptyMap()
            } else {
                dao.loadVgmMetadata(files.map { it.absolutePath }).associateBy { it.path }
            }
        return files
            .map { file -> file.toScannedTrack(cached) }
            .distinctBy { it.track.uri }
            .sortedWith(compareBy({ it.track.folder.lowercase(Locale.ROOT) }, { it.track.title.lowercase(Locale.ROOT) }))
    }

    private fun File.toScannedTrack(cached: Map<String, me.ayra.music.data.VgmMetadataEntity>): ScannedTrack {
        val lastModifiedMs = lastModified().coerceAtLeast(0L)
        val sizeBytes = length().coerceAtLeast(0L)
        val cachedMetadata = cached[absolutePath]
        val track =
            if (
                cachedMetadata != null &&
                cachedMetadata.lastModifiedMs == lastModifiedMs &&
                cachedMetadata.sizeBytes == sizeBytes
            ) {
                Track(
                    id = stableVgmTrackId(absolutePath),
                    title = cachedMetadata.title,
                    artist = cachedMetadata.artist,
                    album = cachedMetadata.album,
                    durationMs = cachedMetadata.durationMs,
                    uri = Uri.fromFile(this),
                    albumId = 0L,
                    folder = cachedMetadata.folder,
                    dateAddedMs = lastModifiedMs,
                )
            } else {
                toVgmTrack()
            }
        return ScannedTrack(
            track = track,
            cacheKey = absolutePath,
            source = LibrarySource.Vgm,
            lastModifiedMs = lastModifiedMs,
            sizeBytes = sizeBytes,
        )
    }

    private fun scanRoots(): List<File> {
        val roots = linkedSetOf<File>()
        @Suppress("DEPRECATION")
        Environment
            .getExternalStorageDirectory()
            ?.takeIf { it.exists() && it.isDirectory }
            ?.let(roots::add)
        @Suppress("DEPRECATION")
        context
            .getExternalMediaDirs()
            .filterNotNull()
            .mapNotNull { it.storageRootOrNull() }
            .forEach(roots::add)
        context
            .getExternalFilesDirs(null)
            .filterNotNull()
            .mapNotNull { it.storageRootOrNull() }
            .forEach(roots::add)
        return roots.filter { it.exists() && it.isDirectory && it.canRead() }
    }

    private fun File.storageRootOrNull(): File? {
        val segments = absoluteFile.toPath().map { it.toString() }
        val androidIndex = segments.indexOf("Android")
        if (androidIndex <= 0) return this.takeIf { it.exists() && it.isDirectory }
        return File(segments.take(androidIndex).joinToString(File.separator))
            .takeIf { it.exists() && it.isDirectory }
    }

    private suspend fun File.walkReadableFiles(onFile: suspend (File) -> Unit) {
        val pending = ArrayDeque<File>()
        pending += this
        while (pending.isNotEmpty()) {
            val current = pending.removeLast()
            if (!current.canRead() || current.shouldSkipScan()) continue
            if (current.isFile) {
                onFile(current)
                continue
            }
            val children = runCatching { current.listFiles() }.getOrNull() ?: continue
            children.forEach { child -> pending += child }
        }
    }

    private fun File.shouldSkipScan(): Boolean {
        if (name.startsWith(".")) return true
        val normalized = absolutePath.replace('\\', '/')
        return normalized.contains("/Android/data/") || normalized.contains("/Android/obb/")
    }

    private fun File.isVgmFile(): Boolean = isFile && extension.lowercase(Locale.ROOT) in VGM_EXTENSIONS

    private fun File.toVgmTrack(): Track {
        val folderPath = parentFile?.absolutePath?.toDisplayFolder() ?: "VGM"
        return Track(
            id = stableVgmTrackId(absolutePath),
            title = name.substringBeforeLast('.', name),
            artist = "VGM",
            album = folderPath.substringAfterLast('/').ifBlank { "VGM" },
            durationMs = 0L,
            uri = Uri.fromFile(this),
            albumId = 0L,
            folder = folderPath,
            dateAddedMs = lastModified().coerceAtLeast(0L),
        )
    }

    private fun String.toDisplayFolder(): String {
        @Suppress("DEPRECATION")
        val storageRoot = Environment.getExternalStorageDirectory()?.absolutePath?.replace('\\', '/')
        val normalized = replace('\\', '/').trimEnd('/')
        return storageRoot
            ?.takeIf { normalized.startsWith(it) }
            ?.let { normalized.removePrefix(it).trimStart('/').ifBlank { "VGM" } }
            ?: normalized
    }

    private fun stableVgmTrackId(path: String): Long {
        var hash = 1125899906842597L
        path.lowercase(Locale.ROOT).forEach { char ->
            hash = 31L * hash + char.code
        }
        return -(hash and Long.MAX_VALUE).coerceAtLeast(1L)
    }
}

internal val VGM_EXTENSIONS =
    """
208 2dx 2dx9 3do 3ds 4 8 800 9tav a3c aa3 aaf aax abc abk acb acm acx ad adc adm adm2
adp adpcm adpcmx ads adw adx afc afs2 agsc ahv ahx ai aifc aix akb al al2 amb ams amx an2
ao ap apc apm as4 asbin asd asf asr ast at3 at9 atsl atsl3 atsl4 atslx atx aud audio audio_data
audiopkg aus awa awb awc awd awx b1s baa baf baka bank bao bar bcstm bcv bcwav bdm bfstm bfwav
bg00 bgm bgw bigrp bik bika binka bk2 bkh bkr blk bmdx bms bnk bnm bns bnsf bo2 brstm brstmspm
brwav brwsd bsnd btsnd bvg bwav bx cads caf cat cbd2 cbx cd cfn chd chk ckb ckd cks cnk cpk cps
crd csa csb csmp cvs cwav cxb cxk cxs d2 da data dax dbm dcs dct ddsp de2 dec dic diva dmsg drm
ds2 dsb dsf dsp dspw dtk dty dvi dyx e4x eam eas eda emff enm eno ens esf exa ezw fag fda filp
fish flx fsb fsv fwav fwse g1l gbts gca gcm gcub gcw ged genh gin gmd gms grn gsf gsp gtd gwb
gwm h4m hab hbd hca hd hd2 hd3 hdr hdt his hps hsf hvqm hwas hwb hwd hwx hx2 hx3 hxc hxd hxg
hxx iab iadp iap idmsf idsp idvi idwav idx idxma ifs ikm ild ilf ilv ima imc imf imx int is14
isb isd isws itl ivag ivaud ivb ivs ixa joe jstm k2sb ka1a kat kces kcey km9 kma kmx kno kns koe
kovs kraw ktac ktsl2asbin ktss kvs kwa l l00 laac lac3 ladpcm laif laifc laiff lasf lbin ldat ldt
lep lflac lin lm0 lm1 lm2 lm3 lm4 lm5 lm6 lm7 lmp2 lmp3 lmp4 lmpc logg lopus lp lpcm lpk lps lrmh
lse lsf lstm lwav lwd lwma mab mad map mc3 mca mcadpcm mcg mds mdsp med mhk mi4 mib mic mio mjb
mogg mon move mpds mpdsp mpf mps ms msa msb msd mse msf msh mss msv msvp msx mta mta2 mtaf mtt mul
mups mus musc musx mvb mwa mwv mxst myspd n64 naac ndp nds nfx nlsd no nop nps npsf nsa nsopus nub
nub2 nus3audio nus3bank nusnub nwa nwav nxa nxms nxopus nxse oga ogg_ ogl ogs ogv oma omu oor opu
opusnx opusx oto ovb owp p04 p08 p16 p1d p2a p2bt p3d paf past patch3audio pcm pdt phd pk pona pos
ps3 psb psf psh psn pth pwb qwv r rac rad rak ras raw rda res rkv rof rpgmvo rrds rsd rsf rsm rsnd
rsoundast rsoundsnd rsp rstm rvw rvws rwar rwav rws rwsd rwx rxx s14 s3s s3v sab sad saf sag sam
sap sb0 sb1 sb2 sb3 sb4 sb5 sb6 sb7 sbin sbk sbr sbv sc scd sch sd9 sdd sdf sdl sdp sdt sdx se se3
seb sed seg sem sf0 sfa sfl sfs sfx sgb sgd sgt shaa shsa sig skx slb sli sm0 sm1 sm2 sm3 sm4 sm5
sm6 sm7 smh smk smp smv sn0 snb snd snds sng sngw snr sns snu snz sod son sounds spc sph spk spm
sps spsd spsis14 spsis22 spt spw srcd sre srsa ss2 ssd ssf ssm ssp sspr sss ster sth stm str stream
strm sts sts_cp3 stv stx svag svg svs swag swar swav swd switch switch_audio sx sxd sxd2 sxd3 szd
szd1 szd3 tad tgq tgv thp tmx tra trk trs tsdse3 tsdse4 tun txth txtp u0 ue4opus ueba ueopus ulw
um3 usm utk uv v v0 va3 vab vag vai vam vas vb vbk vbx vca vcb vdm vds vgi vgm vgmstream vgs vgv
vh vid vig vis vm4 vms vmu voi vp6 vpk vs vsf vsv vxn w waa wac wad waf wam was wavc wave wavebatch
wavm wavx wax way wb wb2 wbd wbk wd wem wic wiive wip wlv wmw wp2 wpd wsd wsi wst wua wv2 wv6 wvd
wve wvp wvs wvx wxd wxv x x360audio xa xa2 xa30 xag xai xau xav xb xbw xen xhd xma xma2 xmd xms
xmu xmv xnb xopus xps xse xsew xsf xsh xss xst xvag xwav xwb xwc xwm xwma xws xwv ydsp ymf zic zsd
zsm zss zwv
    """.trimIndent()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .toSet()

private fun android.database.Cursor.getStringOrNull(column: Int): String? = if (column >= 0 && !isNull(column)) getString(column) else null

@Composable
fun MusicApp(
    openPlayerRequest: Int = 0,
    viewModel: MusicViewModel = viewModel(),
) {
    val context = LocalContext.current
    val navigator = rememberMusicNavigator()
    val permission = remember { audioPermission() }
    var permissionGranted by remember {
        mutableStateOf(hasLibraryPermission(context, permission))
    }
    val manageStorageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            permissionGranted = hasLibraryPermission(context, permission)
            viewModel.loadLibrary(permissionGranted)
        }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            permissionGranted = hasLibraryPermission(context, permission)
            if (permissionGranted) {
                viewModel.loadLibrary(true)
            } else if (needsManageExternalStoragePermission(context)) {
                manageStorageLauncher.launch(manageExternalStorageIntent(context))
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
    val hidePlayerSheet = navigator.currentRoute == MainRoute.Settings || playlistEditMode
    var playerExpandRequest by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(openPlayerRequest) {
        if (openPlayerRequest > 0) {
            while (navigator.canGoBack()) {
                navigator.back()
            }
            playerExpandRequest = openPlayerRequest
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MainScreen(
                library = library,
                navigator = navigator,
                currentQueue = playerState.queue,
                currentTrackId = playerState.currentTrack?.id,
                onRequestPermission = {
                    if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(permission)
                    } else if (needsManageExternalStoragePermission(context)) {
                        manageStorageLauncher.launch(manageExternalStorageIntent(context))
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
                onCreatePlaylist = viewModel::createPlaylist,
                onAddTracksToPlaylist = viewModel::addTracksToPlaylist,
                onAddTracksToCurrentQueue = viewModel::addTracksToCurrentQueue,
                onReplaceCurrentQueue = viewModel::replaceCurrentQueue,
                onReplacePlaylistTracks = viewModel::replacePlaylistTracks,
                onRenamePlaylist = viewModel::renamePlaylist,
                onDeletePlaylists = viewModel::deletePlaylists,
                onDeleteTracksPermanently = ::requestPermanentDelete,
                onAddTracksToRoute = { tracks -> navigator.navigate(MainRoute.AddToTracks(tracks.map { it.id })) },
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
                    onExpandRequestConsumed = { playerExpandRequest = 0 },
                    onSettings = { navigator.navigate(MainRoute.Settings) },
                    onAddTo = { track -> navigator.navigate(MainRoute.AddToPlaylist(track.id)) },
                    onToggleFavorite = { playerState.currentTrack?.id?.let(viewModel::toggleFavorite) },
                    onPlayPause = viewModel::togglePlayPause,
                    onPrevious = viewModel::previous,
                    onNext = viewModel::next,
                    onSeek = viewModel::seekTo,
                    onShuffle = viewModel::toggleShuffle,
                    onQueueTrackClick = { track -> viewModel.playTrack(track, playerState.queue) },
                    onRepeat = viewModel::toggleRepeat,
                )
            }
        }
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
    val audioGranted = ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED
    return audioGranted && !needsManageExternalStoragePermission(context)
}

private fun needsManageExternalStoragePermission(context: Context): Boolean =
    BuildConfig.IS_VGM_BUILD &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
        !Environment.isExternalStorageManager()

private fun manageExternalStorageIntent(context: Context): Intent =
    Intent(
        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
        Uri.parse("package:${context.packageName}"),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

private fun notificationPermission(): String? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.POST_NOTIFICATIONS
    } else {
        null
    }
