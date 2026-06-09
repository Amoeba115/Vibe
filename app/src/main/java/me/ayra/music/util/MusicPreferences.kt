package me.ayra.music.util

import android.content.Context

class MusicPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("music_preferences", Context.MODE_PRIVATE)

    fun loadLastTab(defaultIndex: Int): Int = preferences.getInt(KEY_LAST_TAB, defaultIndex)

    fun saveLastTab(index: Int) {
        preferences.edit().putInt(KEY_LAST_TAB, index).apply()
    }

    fun loadLastTrackId(): Long = preferences.getLong(KEY_LAST_TRACK_ID, NO_TRACK_ID)

    fun saveLastTrackId(trackId: Long) {
        preferences.edit().putLong(KEY_LAST_TRACK_ID, trackId).apply()
    }

    fun loadLastQueueIds(): List<Long> =
        preferences.getString(KEY_LAST_QUEUE_IDS, null)
            ?.split(',')
            ?.mapNotNull { it.toLongOrNull() }
            .orEmpty()

    fun saveLastQueueIds(trackIds: List<Long>) {
        preferences.edit().putString(KEY_LAST_QUEUE_IDS, trackIds.joinToString(",")).apply()
    }

    fun loadSort(key: String, defaultValue: String): String =
        preferences.getString("$KEY_SORT_PREFIX$key", defaultValue) ?: defaultValue

    fun saveSort(key: String, value: String) {
        preferences.edit().putString("$KEY_SORT_PREFIX$key", value).apply()
    }

    fun loadThemeMode(): String = preferences.getString(KEY_THEME_MODE, THEME_MODE_AUTO) ?: THEME_MODE_AUTO

    fun saveThemeMode(value: String) {
        preferences.edit().putString(KEY_THEME_MODE, value).apply()
    }

    fun loadThemeColorSeed(): String = preferences.getString(KEY_THEME_COLOR_SEED, THEME_SEED_SYSTEM) ?: THEME_SEED_SYSTEM

    fun saveThemeColorSeed(value: String) {
        preferences.edit().putString(KEY_THEME_COLOR_SEED, value).apply()
    }

    fun loadAmoledMode(): Boolean = preferences.getBoolean(KEY_AMOLED_MODE, false)

    fun saveAmoledMode(value: Boolean) {
        preferences.edit().putBoolean(KEY_AMOLED_MODE, value).apply()
    }

    fun loadDisableAlbumDynamicColor(): Boolean = preferences.getBoolean(KEY_DISABLE_ALBUM_DYNAMIC_COLOR, false)

    fun saveDisableAlbumDynamicColor(value: Boolean) {
        preferences.edit().putBoolean(KEY_DISABLE_ALBUM_DYNAMIC_COLOR, value).apply()
    }

    fun loadPlaybackSpeed(): Float = preferences.getFloat(KEY_PLAYBACK_SPEED, 1f).coerceIn(0.5f, 2f)

    fun savePlaybackSpeed(value: Float) {
        preferences.edit().putFloat(KEY_PLAYBACK_SPEED, value.coerceIn(0.5f, 2f)).apply()
    }

    fun loadCrossfadeSeconds(): Int = preferences.getInt(KEY_CROSSFADE_SECONDS, 0).coerceIn(0, 5)

    fun saveCrossfadeSeconds(value: Int) {
        preferences.edit().putInt(KEY_CROSSFADE_SECONDS, value.coerceIn(0, 5)).apply()
    }

    fun loadShuffleEnabled(): Boolean = preferences.getBoolean(KEY_SHUFFLE_ENABLED, false)

    fun saveShuffleEnabled(value: Boolean) {
        preferences.edit().putBoolean(KEY_SHUFFLE_ENABLED, value).apply()
    }

    fun loadRepeatMode(): Int = preferences.getInt(KEY_REPEAT_MODE, 0).coerceIn(0, 2)

    fun saveRepeatMode(value: Int) {
        preferences.edit().putInt(KEY_REPEAT_MODE, value.coerceIn(0, 2)).apply()
    }

    fun loadVgmLoopMode(): String = preferences.getString(KEY_VGM_LOOP_MODE, VGM_LOOP_FOLLOW_APP) ?: VGM_LOOP_FOLLOW_APP

    fun saveVgmLoopMode(value: String) {
        preferences.edit().putString(KEY_VGM_LOOP_MODE, value).apply()
    }

    fun loadVgmLoopCount(): Float = preferences.getFloat(KEY_VGM_LOOP_COUNT, 2f).coerceIn(0f, 99f)

    fun saveVgmLoopCount(value: Float) {
        preferences.edit().putFloat(KEY_VGM_LOOP_COUNT, value.coerceIn(0f, 99f)).apply()
    }

    fun loadVgmFadeLengthSeconds(): Int = preferences.getInt(KEY_VGM_FADE_LENGTH_SECONDS, 3).coerceIn(0, 30)

    fun saveVgmFadeLengthSeconds(value: Int) {
        preferences.edit().putInt(KEY_VGM_FADE_LENGTH_SECONDS, value.coerceIn(0, 30)).apply()
    }

    fun loadVgmFadeDelaySeconds(): Int = preferences.getInt(KEY_VGM_FADE_DELAY_SECONDS, 0).coerceIn(0, 30)

    fun saveVgmFadeDelaySeconds(value: Int) {
        preferences.edit().putInt(KEY_VGM_FADE_DELAY_SECONDS, value.coerceIn(0, 30)).apply()
    }

    fun loadVgmDisableSubsongs(): Boolean = preferences.getBoolean(KEY_VGM_DISABLE_SUBSONGS, false)

    fun saveVgmDisableSubsongs(value: Boolean) {
        preferences.edit().putBoolean(KEY_VGM_DISABLE_SUBSONGS, value).apply()
    }

    fun loadVgmDownmixEnabled(): Boolean = preferences.getBoolean(KEY_VGM_DOWNMIX_ENABLED, false)

    fun saveVgmDownmixEnabled(value: Boolean) {
        preferences.edit().putBoolean(KEY_VGM_DOWNMIX_ENABLED, value).apply()
    }

    fun loadVgmDownmixChannels(): Int = preferences.getInt(KEY_VGM_DOWNMIX_CHANNELS, 2).coerceIn(1, 6)

    fun saveVgmDownmixChannels(value: Int) {
        preferences.edit().putInt(KEY_VGM_DOWNMIX_CHANNELS, value.coerceIn(1, 6)).apply()
    }

    fun loadVgmChannelOutput(): String = preferences.getString(KEY_VGM_CHANNEL_OUTPUT, "Auto") ?: "Auto"

    fun saveVgmChannelOutput(value: String) {
        preferences.edit().putString(KEY_VGM_CHANNEL_OUTPUT, value).apply()
    }

    fun registerSettingsListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterSettingsListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        const val THEME_MODE_AUTO = "Auto"
        const val THEME_MODE_LIGHT = "Light"
        const val THEME_MODE_DARK = "Dark"
        const val THEME_SEED_SYSTEM = "system"
        const val THEME_SEED_NEUTRAL = "neutral"
        const val VGM_LOOP_FOLLOW_APP = "FollowApp"
        const val KEY_PLAYBACK_SPEED = "playback_speed"

        private const val KEY_LAST_TAB = "last_tab"
        private const val KEY_LAST_TRACK_ID = "last_track_id"
        private const val KEY_LAST_QUEUE_IDS = "last_queue_ids"
        private const val KEY_SORT_PREFIX = "sort_"
        private const val NO_TRACK_ID = -1L
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_THEME_COLOR_SEED = "theme_color_seed"
        private const val KEY_AMOLED_MODE = "amoled_mode"
        private const val KEY_DISABLE_ALBUM_DYNAMIC_COLOR = "disable_album_dynamic_color"
        private const val KEY_CROSSFADE_SECONDS = "crossfade_seconds"
        private const val KEY_SHUFFLE_ENABLED = "shuffle_enabled"
        private const val KEY_REPEAT_MODE = "repeat_mode"
        private const val KEY_VGM_LOOP_MODE = "vgm_loop_mode"
        private const val KEY_VGM_LOOP_COUNT = "vgm_loop_count"
        private const val KEY_VGM_FADE_LENGTH_SECONDS = "vgm_fade_length_seconds"
        private const val KEY_VGM_FADE_DELAY_SECONDS = "vgm_fade_delay_seconds"
        private const val KEY_VGM_DISABLE_SUBSONGS = "vgm_disable_subsongs"
        private const val KEY_VGM_DOWNMIX_ENABLED = "vgm_downmix_enabled"
        private const val KEY_VGM_DOWNMIX_CHANNELS = "vgm_downmix_channels"
        private const val KEY_VGM_CHANNEL_OUTPUT = "vgm_channel_output"
    }
}
