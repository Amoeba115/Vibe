package me.ayra.music

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import me.ayra.music.util.MusicPreferences

@UnstableApi
class HybridPlayer(
    context: Context,
    looper: Looper = Looper.getMainLooper(),
) : SimpleBasePlayer(looper) {
    private val appContext = context.applicationContext
    private val applicationHandler = Handler(looper)
    private val preferences = MusicPreferences(appContext)
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val exoPlayer = ExoPlayer.Builder(appContext).setLooper(looper).build()
    private val vgmPlayer: Player? = createVgmPlayer(looper)
    private val stateLock = Any()
    private val settingsListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            applyVgmSettings()
        }

    private var activePlayer: Player? = null
    private var playlist: List<MediaItem> = emptyList()
    private var currentIndex = C.INDEX_UNSET
    private var pendingStartPositionMs = 0L
    private var playWhenReady = false
    private var repeatMode = Player.REPEAT_MODE_OFF
    private var shuffleModeEnabled = false
    private var volume = 1f
    private var playbackParameters = PlaybackParameters.DEFAULT
    private var playbackSuppressionReason = PlaybackSuppressionReason.NONE
    private var resumeAfterAudioFocusGain = false
    private var noisyReceiverRegistered = false
    private var released = false

    private val audioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            applicationHandler.post {
                if (released || activePlayer !== vgmPlayer) return@post
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pauseForAudioFocusLoss()
                    AudioManager.AUDIOFOCUS_LOSS -> {
                        pauseForAudioFocusLoss()
                        resumeAfterAudioFocusGain = false
                        playWhenReady = false
                        abandonVgmAudioFocus()
                    }
                    AudioManager.AUDIOFOCUS_GAIN -> resumeAfterAudioFocusGain()
                }
            }
        }

    private val audioFocusRequest =
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setAcceptsDelayedFocusGain(false)
            .setOnAudioFocusChangeListener(audioFocusChangeListener, applicationHandler)
            .build()

    private val noisyReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                    applicationHandler.post { pauseForNoisyOutput() }
                }
            }
        }

    private val childListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            maybeAdvanceAfterEnded(playbackState)
            invalidateStateOnApplicationThread()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) = invalidateStateOnApplicationThread()

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            activePlayer?.let { this@HybridPlayer.playWhenReady = it.playWhenReady }
            invalidateStateOnApplicationThread()
        }

        override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) = invalidateStateOnApplicationThread()
    }

    init {
        exoPlayer.setAudioAttributes(musicAudioAttributes(), true)
        preferences.registerSettingsListener(settingsListener)
        exoPlayer.addListener(childListener)
        vgmPlayer?.addListener(childListener)
    }

    override fun getState(): State {
        val currentPlayer = activePlayer
        val currentDuration = currentPlayer?.duration?.takeIf { it > 0 } ?: C.TIME_UNSET
        val currentPosition = currentPlayer?.currentPosition?.coerceAtLeast(0L) ?: 0L
        val currentPlaybackState = currentPlayer?.playbackState ?: Player.STATE_IDLE
        val currentSuppressionReason = media3PlaybackSuppressionReason(currentPlayer)
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
            .setPlaybackSuppressionReason(currentSuppressionReason)
            .setPlayWhenReady(playWhenReady, Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
            .setRepeatMode(repeatMode)
            .setShuffleModeEnabled(shuffleModeEnabled)
            .setVolume(volume)
            .setPlaybackParameters(playbackParameters)
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
                playActivePlayer()
            }
        } else {
            clearSuppression()
            resumeAfterAudioFocusGain = false
            player?.pause()
            if (player === vgmPlayer) abandonVgmAudioFocus()
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
        val childRepeatMode = childRepeatMode()
        exoPlayer.repeatMode = childRepeatMode
        vgmPlayer?.repeatMode = childRepeatMode
        applyVgmSettings()
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

    override fun handleSetPlaybackParameters(playbackParameters: PlaybackParameters): ListenableFuture<Any> {
        this.playbackParameters = playbackParameters
        exoPlayer.playbackParameters = playbackParameters
        if (activePlayer === exoPlayer) {
            activePlayer?.playbackParameters = playbackParameters
        }
        invalidateState()
        return immediateFuture()
    }

    override fun handleStop(): ListenableFuture<Any> {
        stopChildren()
        abandonVgmAudioFocus()
        invalidateState()
        return immediateFuture()
    }

    override fun handleRelease(): ListenableFuture<Any> {
        if (!released) {
            released = true
            exoPlayer.removeListener(childListener)
            vgmPlayer?.removeListener(childListener)
            preferences.unregisterSettingsListener(settingsListener)
            abandonVgmAudioFocus()
            exoPlayer.release()
            vgmPlayer?.release()
        }
        return immediateFuture()
    }

    private fun openCurrentItem() {
        val item = synchronized(stateLock) { playlist.getOrNull(currentIndex) } ?: return
        val nextPlayer = playerFor(item)
        if (nextPlayer !== activePlayer) {
            if (activePlayer === vgmPlayer) abandonVgmAudioFocus()
            activePlayer?.stop()
            activePlayer = nextPlayer
            clearSuppression()
            resumeAfterAudioFocusGain = false
        } else {
            nextPlayer.stop()
        }
        nextPlayer.repeatMode = childRepeatMode()
        nextPlayer.shuffleModeEnabled = shuffleModeEnabled
        nextPlayer.volume = volume
        if (nextPlayer === exoPlayer) {
            nextPlayer.playbackParameters = playbackParameters
        }
        nextPlayer.setMediaItem(item, pendingStartPositionMs)
        nextPlayer.prepare()
        if (playWhenReady) playActivePlayer()
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
        clearSuppression()
        resumeAfterAudioFocusGain = false
        unregisterNoisyReceiver()
    }

    private fun playActivePlayer() {
        val player = activePlayer ?: return
        if (player === vgmPlayer) {
            if (!requestVgmAudioFocus()) {
                playbackSuppressionReason = PlaybackSuppressionReason.AUDIO_FOCUS_LOSS
                playWhenReady = false
                invalidateState()
                return
            }
            clearSuppression()
            registerNoisyReceiver()
        }
        player.play()
    }

    private fun requestVgmAudioFocus(): Boolean =
        audioManager.requestAudioFocus(audioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED

    private fun abandonVgmAudioFocus() {
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
        unregisterNoisyReceiver()
    }

    private fun pauseForAudioFocusLoss() {
        val player = activePlayer ?: return
        if (player !== vgmPlayer) return
        resumeAfterAudioFocusGain = playWhenReady && player.isPlaying
        playbackSuppressionReason = PlaybackSuppressionReason.AUDIO_FOCUS_LOSS
        player.pause()
        unregisterNoisyReceiver()
        invalidateState()
    }

    private fun resumeAfterAudioFocusGain() {
        if (playbackSuppressionReason == PlaybackSuppressionReason.AUDIO_FOCUS_LOSS) {
            playbackSuppressionReason = PlaybackSuppressionReason.NONE
        }
        if (resumeAfterAudioFocusGain && playWhenReady && activePlayer === vgmPlayer) {
            resumeAfterAudioFocusGain = false
            registerNoisyReceiver()
            activePlayer?.play()
        } else {
            resumeAfterAudioFocusGain = false
        }
        invalidateState()
    }

    private fun pauseForNoisyOutput() {
        val player = activePlayer ?: return
        if (player !== vgmPlayer || !playWhenReady) return
        playbackSuppressionReason = PlaybackSuppressionReason.NOISY
        resumeAfterAudioFocusGain = false
        playWhenReady = false
        player.pause()
        abandonVgmAudioFocus()
        invalidateState()
    }

    private fun clearSuppression() {
        playbackSuppressionReason = PlaybackSuppressionReason.NONE
    }

    private fun registerNoisyReceiver() {
        if (noisyReceiverRegistered) return
        appContext.registerReceiver(noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        noisyReceiverRegistered = true
    }

    private fun unregisterNoisyReceiver() {
        if (!noisyReceiverRegistered) return
        runCatching { appContext.unregisterReceiver(noisyReceiver) }
        noisyReceiverRegistered = false
    }

    private fun media3PlaybackSuppressionReason(currentPlayer: Player?): Int {
        return when {
            playbackSuppressionReason == PlaybackSuppressionReason.AUDIO_FOCUS_LOSS ->
                Player.PLAYBACK_SUPPRESSION_REASON_TRANSIENT_AUDIO_FOCUS_LOSS
            playbackSuppressionReason == PlaybackSuppressionReason.NOISY ->
                Player.PLAYBACK_SUPPRESSION_REASON_UNSUITABLE_AUDIO_OUTPUT
            else -> currentPlayer?.playbackSuppressionReason ?: Player.PLAYBACK_SUPPRESSION_REASON_NONE
        }
    }

    private fun childRepeatMode(): Int = Player.REPEAT_MODE_OFF

    private fun createVgmPlayer(looper: Looper): Player? {
        return runCatching {
            val settings = createVgmSettings()
            val settingsClass = settings.javaClass
            val adapterClass = Class.forName("me.ayra.vgmstream.media3.VgmPlayerAdapter")
            adapterClass
                .getConstructor(Context::class.java, settingsClass, Looper::class.java)
                .newInstance(appContext, settings, looper) as Player
        }.getOrNull()
    }

    private fun applyVgmSettings() {
        val player = vgmPlayer ?: return
        runCatching {
            val settings = createVgmSettings()
            player.javaClass.getMethod("setSettings", settings.javaClass).invoke(player, settings)
        }
    }

    private fun createVgmSettings(): Any {
        val settingsClass = Class.forName("me.ayra.vgmstream.VgmSettings")
        val loopModeClass = Class.forName("me.ayra.vgmstream.LoopMode")
        val channelOutputClass = Class.forName("me.ayra.vgmstream.ChannelOutput")
        val savedLoopMode = preferences.loadVgmLoopMode()
        val loopModeName =
            when {
                savedLoopMode != MusicPreferences.VGM_LOOP_FOLLOW_APP -> savedLoopMode
                repeatMode == Player.REPEAT_MODE_ONE -> "Forever"
                else -> "Normal"
            }
        val loopModeConstants = loopModeClass.enumConstants.orEmpty()
        val loopMode = loopModeConstants.firstOrNull { (it as Enum<*>).name == loopModeName }
            ?: loopModeConstants.first { (it as Enum<*>).name == "Normal" }
        val channelOutputName = preferences.loadVgmChannelOutput()
        val channelOutputConstants = channelOutputClass.enumConstants.orEmpty()
        val channelOutput = channelOutputConstants.firstOrNull { (it as Enum<*>).name == channelOutputName }
            ?: channelOutputConstants.first { (it as Enum<*>).name == "Auto" }
        return settingsClass
            .getConstructor(
                Double::class.javaPrimitiveType,
                Long::class.javaPrimitiveType,
                loopModeClass,
                Long::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                channelOutputClass,
            ).newInstance(
                preferences.loadVgmLoopCount().toDouble(),
                preferences.loadVgmFadeLengthSeconds() * 1_000L,
                loopMode,
                preferences.loadVgmFadeDelaySeconds() * 1_000L,
                preferences.loadVgmDisableSubsongs(),
                if (preferences.loadVgmDownmixEnabled()) preferences.loadVgmDownmixChannels() else 0,
                channelOutput,
            )
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

    private fun musicAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

    private fun maybeAdvanceAfterEnded(playbackState: Int) {
        if (playbackState != Player.STATE_ENDED || !playWhenReady) return
        val nextIndex = synchronized(stateLock) {
            when {
                currentIndex == C.INDEX_UNSET || playlist.isEmpty() -> C.INDEX_UNSET
                repeatMode == Player.REPEAT_MODE_ONE -> {
                    pendingStartPositionMs = 0L
                    currentIndex
                }
                currentIndex + 1 in playlist.indices -> {
                    currentIndex += 1
                    pendingStartPositionMs = 0L
                    currentIndex
                }
                repeatMode == Player.REPEAT_MODE_ALL -> {
                    currentIndex = 0
                    pendingStartPositionMs = 0L
                    currentIndex
                }
                else -> C.INDEX_UNSET
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
            .add(Player.COMMAND_SET_SPEED_AND_PITCH)
            .add(Player.COMMAND_GET_TEXT)
            .add(Player.COMMAND_GET_TRACKS)
            .add(Player.COMMAND_RELEASE)
            .build()
    }
}

private enum class PlaybackSuppressionReason {
    NONE,
    AUDIO_FOCUS_LOSS,
    NOISY,
}
