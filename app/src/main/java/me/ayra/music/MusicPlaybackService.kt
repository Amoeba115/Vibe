package me.ayra.music

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.C
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class MusicPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = createSessionPlayer()
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(playerActivityPendingIntent())
            .setCallback(playbackSessionCallback())
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (me.ayra.music.util.MusicPreferences(this).loadStopOnTaskRemoved()) {
            player?.stop()
            stopSelf()
            return
        }
        if (player == null || !player.playWhenReady) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    private fun playerActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
            .putExtra(EXTRA_OPEN_FULLSCREEN_PLAYER, true)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    @UnstableApi
    private fun playbackSessionCallback(): MediaSession.Callback {
        return object : MediaSession.Callback {
            override fun onPlaybackResumption(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo,
                isForPlayback: Boolean,
            ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
                return Futures.immediateFuture(emptyResumptionItems())
            }

            @Deprecated("Media3 calls the overload with isForPlayback on current versions")
            override fun onPlaybackResumption(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo,
            ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
                return Futures.immediateFuture(emptyResumptionItems())
            }
        }
    }

    @UnstableApi
    private fun emptyResumptionItems(): MediaSession.MediaItemsWithStartPosition {
        return MediaSession.MediaItemsWithStartPosition(
            emptyList<MediaItem>(),
            C.INDEX_UNSET,
            C.TIME_UNSET,
        )
    }

    private fun createSessionPlayer(): Player {
        val preferences = me.ayra.music.util.MusicPreferences(this)
        return ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(musicAudioAttributes(), true)
            setPlaybackSpeed(preferences.loadPlaybackSpeed())
            shuffleModeEnabled = preferences.loadShuffleEnabled()
            repeatMode = preferences.loadRepeatMode()
        }
    }

    private fun musicAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
}
