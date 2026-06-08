package me.ayra.music

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

@UnstableApi
class HybridPlayer(
    context: Context,
    looper: Looper = Looper.getMainLooper(),
) : SimpleBasePlayer(looper) {
    private val appContext = context.applicationContext
    private val applicationHandler = Handler(looper)
    private val exoPlayer = ExoPlayer.Builder(appContext).setLooper(looper).build()
    private val vgmPlayer: Player? = createVgmPlayer(looper)
    private val stateLock = Any()

    private var activePlayer: Player? = null
    private var playlist: List<MediaItem> = emptyList()
    private var currentIndex = C.INDEX_UNSET
    private var pendingStartPositionMs = 0L
    private var playWhenReady = false
    private var repeatMode = Player.REPEAT_MODE_OFF
    private var shuffleModeEnabled = false
    private var volume = 1f
    private var released = false

    private val childListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            maybeAdvanceAfterEnded(playbackState)
            invalidateStateOnApplicationThread()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) = invalidateStateOnApplicationThread()
    }

    init {
        exoPlayer.addListener(childListener)
        vgmPlayer?.addListener(childListener)
    }

    override fun getState(): State {
        val currentPlayer = activePlayer
        val currentDuration = currentPlayer?.duration?.takeIf { it > 0 } ?: C.TIME_UNSET
        val currentPosition = currentPlayer?.currentPosition?.coerceAtLeast(0L) ?: 0L
        val currentPlaybackState = currentPlayer?.playbackState ?: Player.STATE_IDLE
        val items = synchronized(stateLock) {
            playlist.mapIndexed { index, item ->
                MediaItemData.Builder(mediaItemUid(item, index))
                    .setMediaItem(item)
                    .setDurationUs(if (index == currentIndex) currentDuration.toDurationUs() else C.TIME_UNSET)
                    .setIsSeekable(true)
                    .build()
            }
        }
        return State.Builder()
            .setAvailableCommands(AVAILABLE_COMMANDS)
            .setPlaylist(items)
            .setCurrentMediaItemIndex(currentIndex)
            .setContentPositionMs(currentPosition)
            .setContentBufferedPositionMs(PositionSupplier { activePlayer?.bufferedPosition ?: currentPosition })
            .setPlaybackState(currentPlaybackState)
            .setPlayWhenReady(playWhenReady, Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
            .setRepeatMode(repeatMode)
            .setShuffleModeEnabled(shuffleModeEnabled)
            .setVolume(volume)
            .build()
    }

    override fun handleSetMediaItems(
        mediaItems: MutableList<MediaItem>,
        startIndex: Int,
        startPositionMs: Long,
    ): ListenableFuture<Any> {
        stopChildren()
        synchronized(stateLock) {
            playlist = mediaItems.toList()
            currentIndex = when {
                playlist.isEmpty() -> C.INDEX_UNSET
                startIndex != C.INDEX_UNSET -> startIndex.coerceIn(playlist.indices)
                else -> 0
            }
            pendingStartPositionMs = startPositionMs.takeUnless { it == C.TIME_UNSET } ?: 0L
        }
        invalidateState()
        return immediateFuture()
    }

    override fun handleAddMediaItems(index: Int, mediaItems: List<MediaItem>): ListenableFuture<Any> {
        synchronized(stateLock) {
            val targetIndex = index.coerceIn(0, playlist.size)
            playlist = playlist.toMutableList().apply { addAll(targetIndex, mediaItems) }
            if (currentIndex == C.INDEX_UNSET && playlist.isNotEmpty()) {
                currentIndex = 0
            } else if (currentIndex >= targetIndex) {
                currentIndex += mediaItems.size
            }
        }
        invalidateState()
        return immediateFuture()
    }

    override fun handleMoveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int): ListenableFuture<Any> {
        synchronized(stateLock) {
            val currentItem = playlist.getOrNull(currentIndex)
            val movedItems = playlist.subList(fromIndex, toIndex).toList()
            playlist = playlist.toMutableList().apply {
                subList(fromIndex, toIndex).clear()
                addAll(newIndex.coerceIn(0, size), movedItems)
            }
            currentIndex = currentItem?.let { playlist.indexOf(it) }?.takeIf { it >= 0 }
                ?: playlist.indices.firstOrNull()
                ?: C.INDEX_UNSET
        }
        invalidateState()
        return immediateFuture()
    }

    override fun handleReplaceMediaItems(
        fromIndex: Int,
        toIndex: Int,
        mediaItems: List<MediaItem>,
    ): ListenableFuture<Any> {
        synchronized(stateLock) {
            val currentItem = playlist.getOrNull(currentIndex)
            playlist = playlist.toMutableList().apply {
                subList(fromIndex, toIndex).clear()
                addAll(fromIndex.coerceIn(0, size), mediaItems)
            }
            currentIndex = currentItem?.let { playlist.indexOf(it) }?.takeIf { it >= 0 }
                ?: playlist.indices.firstOrNull()
                ?: C.INDEX_UNSET
        }
        invalidateState()
        return immediateFuture()
    }

    override fun handleRemoveMediaItems(fromIndex: Int, toIndex: Int): ListenableFuture<Any> {
        synchronized(stateLock) {
            val currentItem = playlist.getOrNull(currentIndex)
            playlist = playlist.toMutableList().apply { subList(fromIndex, toIndex).clear() }
            currentIndex = currentItem?.let { playlist.indexOf(it) }?.takeIf { it >= 0 }
                ?: playlist.indices.firstOrNull()
                ?: C.INDEX_UNSET
        }
        if (playlist.isEmpty()) stopChildren()
        invalidateState()
        return immediateFuture()
    }

    override fun handlePrepare(): ListenableFuture<Any> {
        openCurrentItem()
        return immediateFuture()
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<Any> {
        this.playWhenReady = playWhenReady
        val player = activePlayer
        if (playWhenReady) {
            if (player == null && currentIndex != C.INDEX_UNSET) {
                openCurrentItem()
            } else {
                player?.play()
            }
        } else {
            player?.pause()
        }
        invalidateState()
        return immediateFuture()
    }

    override fun handleSeek(mediaItemIndex: Int, positionMs: Long, seekCommand: Int): ListenableFuture<Any> {
        val targetIndex = mediaItemIndex.takeUnless { it == C.INDEX_UNSET } ?: currentIndex
        if (targetIndex == C.INDEX_UNSET) return immediateFuture()
        val targetPosition = positionMs.takeUnless { it == C.TIME_UNSET } ?: 0L
        val switchItem = targetIndex != currentIndex
        synchronized(stateLock) {
            if (targetIndex in playlist.indices) currentIndex = targetIndex
            pendingStartPositionMs = targetPosition
        }
        if (switchItem || activePlayer == null) {
            openCurrentItem()
        } else {
            activePlayer?.seekTo(targetPosition)
        }
        invalidateState()
        return immediateFuture()
    }

    override fun handleSetRepeatMode(repeatMode: Int): ListenableFuture<Any> {
        this.repeatMode = repeatMode
        exoPlayer.repeatMode = repeatMode
        vgmPlayer?.repeatMode = repeatMode
        invalidateState()
        return immediateFuture()
    }

    override fun handleSetShuffleModeEnabled(shuffleModeEnabled: Boolean): ListenableFuture<Any> {
        this.shuffleModeEnabled = shuffleModeEnabled
        exoPlayer.shuffleModeEnabled = shuffleModeEnabled
        vgmPlayer?.shuffleModeEnabled = shuffleModeEnabled
        invalidateState()
        return immediateFuture()
    }

    override fun handleSetVolume(volume: Float, volumeOperationType: Int): ListenableFuture<Any> {
        this.volume = volume.coerceIn(0f, 1f)
        exoPlayer.volume = this.volume
        vgmPlayer?.volume = this.volume
        invalidateState()
        return immediateFuture()
    }

    override fun handleStop(): ListenableFuture<Any> {
        stopChildren()
        invalidateState()
        return immediateFuture()
    }

    override fun handleRelease(): ListenableFuture<Any> {
        if (!released) {
            released = true
            exoPlayer.removeListener(childListener)
            vgmPlayer?.removeListener(childListener)
            exoPlayer.release()
            vgmPlayer?.release()
        }
        return immediateFuture()
    }

    private fun openCurrentItem() {
        val item = synchronized(stateLock) { playlist.getOrNull(currentIndex) } ?: return
        val nextPlayer = playerFor(item)
        if (nextPlayer !== activePlayer) {
            activePlayer?.stop()
            activePlayer = nextPlayer
        } else {
            nextPlayer.stop()
        }
        nextPlayer.repeatMode = repeatMode
        nextPlayer.shuffleModeEnabled = shuffleModeEnabled
        nextPlayer.volume = volume
        nextPlayer.setMediaItem(item, pendingStartPositionMs)
        nextPlayer.prepare()
        if (playWhenReady) nextPlayer.play()
        invalidateState()
    }

    private fun playerFor(item: MediaItem): Player {
        return if (item.localConfiguration?.uri?.isVgmUri() == true && vgmPlayer != null) {
            vgmPlayer
        } else {
            exoPlayer
        }
    }

    private fun stopChildren() {
        activePlayer?.stop()
        activePlayer = null
        playWhenReady = false
    }

    private fun createVgmPlayer(looper: Looper): Player? {
        return runCatching {
            val settingsClass = Class.forName("me.ayra.vgmstream.VgmSettings")
            val settings = settingsClass.getConstructor().newInstance()
            val adapterClass = Class.forName("me.ayra.vgmstream.media3.VgmPlayerAdapter")
            adapterClass
                .getConstructor(Context::class.java, settingsClass, Looper::class.java)
                .newInstance(appContext, settings, looper) as Player
        }.getOrNull()
    }

    private fun Uri.isVgmUri(): Boolean {
        if (scheme != "file") return false
        val extension = lastPathSegment?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase()
            ?: return false
        return extension in VGM_EXTENSIONS
    }

    private fun invalidateStateOnApplicationThread() {
        if (Looper.myLooper() == applicationHandler.looper) {
            invalidateState()
        } else {
            applicationHandler.post { if (!released) invalidateState() }
        }
    }

    private fun mediaItemUid(item: MediaItem, index: Int): Any =
        item.mediaId.takeIf { it.isNotBlank() } ?: "hybrid-$index"

    private fun Long.toDurationUs(): Long =
        if (this == C.TIME_UNSET || this <= 0L) C.TIME_UNSET else this * 1_000L

    private fun immediateFuture(): ListenableFuture<Any> =
        Futures.immediateFuture(Unit)

    private fun maybeAdvanceAfterEnded(playbackState: Int) {
        if (playbackState != Player.STATE_ENDED || !playWhenReady) return
        val nextIndex = synchronized(stateLock) {
            val candidate = currentIndex + 1
            if (candidate in playlist.indices) {
                currentIndex = candidate
                pendingStartPositionMs = 0L
                candidate
            } else {
                C.INDEX_UNSET
            }
        }
        if (nextIndex != C.INDEX_UNSET) {
            applicationHandler.post { if (!released) openCurrentItem() }
        }
    }

    private companion object {
        val AVAILABLE_COMMANDS: Player.Commands = Player.Commands.Builder()
            .add(Player.COMMAND_PLAY_PAUSE)
            .add(Player.COMMAND_PREPARE)
            .add(Player.COMMAND_STOP)
            .add(Player.COMMAND_SEEK_TO_DEFAULT_POSITION)
            .add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS)
            .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_NEXT)
            .add(Player.COMMAND_SEEK_TO_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_BACK)
            .add(Player.COMMAND_SEEK_FORWARD)
            .add(Player.COMMAND_SET_SHUFFLE_MODE)
            .add(Player.COMMAND_SET_REPEAT_MODE)
            .add(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)
            .add(Player.COMMAND_GET_TIMELINE)
            .add(Player.COMMAND_GET_METADATA)
            .add(Player.COMMAND_SET_MEDIA_ITEM)
            .add(Player.COMMAND_CHANGE_MEDIA_ITEMS)
            .add(Player.COMMAND_GET_AUDIO_ATTRIBUTES)
            .add(Player.COMMAND_GET_VOLUME)
            .add(Player.COMMAND_SET_VOLUME)
            .add(Player.COMMAND_GET_TEXT)
            .add(Player.COMMAND_GET_TRACKS)
            .add(Player.COMMAND_RELEASE)
            .build()
    }
}
