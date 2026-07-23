package me.ayra.music.util

import android.content.Context

class MusicPreferences(
    context: Context,
) {
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
        preferences
            .getString(KEY_LAST_QUEUE_IDS, null)
            ?.split(',')
            ?.mapNotNull { it.toLongOrNull() }
            .orEmpty()

    fun saveLastQueueIds(trackIds: List<Long>) {
        preferences.edit().putString(KEY_LAST_QUEUE_IDS, trackIds.joinToString(",")).apply()
    }

    fun loadLastLibraryRefreshMs(): Long = preferences.getLong(KEY_LAST_LIBRARY_REFRESH_MS, 0L)

    fun saveLastLibraryRefreshMs(value: Long) {
        preferences.edit().putLong(KEY_LAST_LIBRARY_REFRESH_MS, value.coerceAtLeast(0L)).apply()
    }

    fun loadMediaStoreLibraryState(): String? = preferences.getString(KEY_MEDIASTORE_LIBRARY_STATE, null)

    fun saveMediaStoreLibraryState(value: String) {
        preferences.edit().putString(KEY_MEDIASTORE_LIBRARY_STATE, value).apply()
    }

    fun loadSort(
        key: String,
        defaultValue: String,
    ): String = preferences.getString("$KEY_SORT_PREFIX$key", defaultValue) ?: defaultValue

    fun saveSort(
        key: String,
        value: String,
    ) {
        preferences.edit().putString("$KEY_SORT_PREFIX$key", value).apply()
    }

    fun loadThemeMode(): String = preferences.getString(KEY_THEME_MODE, THEME_MODE_AUTO) ?: THEME_MODE_AUTO

    fun saveThemeMode(value: String) {
        preferences.edit().putString(KEY_THEME_MODE, value).apply()
    }

    fun loadThemeColorSeed(): String = preferences.getString(KEY_THEME_COLOR_SEED, THEME_SEED_DEFAULT) ?: THEME_SEED_DEFAULT

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

    fun loadMiniPlayerStyle(): String =
        preferences.getString(KEY_MINI_PLAYER_STYLE, MINI_PLAYER_STYLE_FLOATING) ?: MINI_PLAYER_STYLE_FLOATING

    fun saveMiniPlayerStyle(value: String) {
        val normalized =
            when (value) {
                MINI_PLAYER_STYLE_FILLED -> MINI_PLAYER_STYLE_FILLED
                else -> MINI_PLAYER_STYLE_FLOATING
            }
        preferences.edit().putString(KEY_MINI_PLAYER_STYLE, normalized).apply()
    }

    fun loadPlaybackSpeed(): Float = preferences.getFloat(KEY_PLAYBACK_SPEED, 1f).coerceIn(0.5f, 2f)

    fun savePlaybackSpeed(value: Float) {
        preferences.edit().putFloat(KEY_PLAYBACK_SPEED, value.coerceIn(0.5f, 2f)).apply()
    }

    fun loadCrossfadeSeconds(): Int = preferences.getInt(KEY_CROSSFADE_SECONDS, 0).coerceIn(0, 5)

    fun saveCrossfadeSeconds(value: Int) {
        preferences.edit().putInt(KEY_CROSSFADE_SECONDS, value.coerceIn(0, 5)).apply()
    }

    fun loadFancyPlayerEnabled(): Boolean = preferences.getBoolean(KEY_FANCY_PLAYER_ENABLED, false)

    fun saveFancyPlayerEnabled(value: Boolean) {
        preferences.edit().putBoolean(KEY_FANCY_PLAYER_ENABLED, value).apply()
    }

    fun loadFancyBackgroundEnabled(): Boolean = preferences.getBoolean(KEY_FANCY_BACKGROUND_ENABLED, false)

    fun saveFancyBackgroundEnabled(value: Boolean) {
        preferences.edit().putBoolean(KEY_FANCY_BACKGROUND_ENABLED, value).apply()
    }

    fun loadStopOnTaskRemoved(): Boolean = preferences.getBoolean(KEY_STOP_ON_TASK_REMOVED, false)

    fun saveStopOnTaskRemoved(value: Boolean) {
        preferences.edit().putBoolean(KEY_STOP_ON_TASK_REMOVED, value).apply()
    }

    fun loadPauseWhenVolumeZero(): Boolean = preferences.getBoolean(KEY_PAUSE_WHEN_VOLUME_ZERO, false)

    fun savePauseWhenVolumeZero(value: Boolean) {
        preferences.edit().putBoolean(KEY_PAUSE_WHEN_VOLUME_ZERO, value).apply()
    }

    fun loadHiddenFolders(): Set<String> = preferences.getStringSet(KEY_HIDDEN_FOLDERS, emptySet()).orEmpty()

    fun saveHiddenFolders(value: Set<String>) {
        preferences.edit().putStringSet(KEY_HIDDEN_FOLDERS, value).apply()
    }

    fun loadLastFolderTreePath(): List<String> =
        preferences
            .getString(KEY_LAST_FOLDER_TREE_PATH, null)
            ?.split('/')
            ?.filter { it.isNotBlank() }
            .orEmpty()

    fun saveLastFolderTreePath(value: List<String>) {
        preferences.edit().putString(KEY_LAST_FOLDER_TREE_PATH, value.joinToString("/")).apply()
    }

    fun loadShuffleEnabled(): Boolean = preferences.getBoolean(KEY_SHUFFLE_ENABLED, false)

    fun saveShuffleEnabled(value: Boolean) {
        preferences.edit().putBoolean(KEY_SHUFFLE_ENABLED, value).apply()
    }

    fun loadRepeatMode(): Int = preferences.getInt(KEY_REPEAT_MODE, 0).coerceIn(0, 2)

    fun saveRepeatMode(value: Int) {
        preferences.edit().putInt(KEY_REPEAT_MODE, value.coerceIn(0, 2)).apply()
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
        const val THEME_SEED_DEFAULT = "FF115315"
        const val MINI_PLAYER_STYLE_FLOATING = "floating"
        const val MINI_PLAYER_STYLE_FILLED = "filled"
        const val KEY_PLAYBACK_SPEED = "playback_speed"
        const val KEY_PAUSE_WHEN_VOLUME_ZERO = "pause_when_volume_zero"
        const val KEY_HIDDEN_FOLDERS = "hidden_folders"

        private const val KEY_LAST_TAB = "last_tab"
        private const val KEY_LAST_TRACK_ID = "last_track_id"
        private const val KEY_LAST_QUEUE_IDS = "last_queue_ids"
        private const val KEY_LAST_LIBRARY_REFRESH_MS = "last_library_refresh_ms"
        private const val KEY_MEDIASTORE_LIBRARY_STATE = "mediastore_library_state"
        private const val KEY_SORT_PREFIX = "sort_"
        private const val NO_TRACK_ID = -1L
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_THEME_COLOR_SEED = "theme_color_seed"
        private const val KEY_AMOLED_MODE = "amoled_mode"
        private const val KEY_DISABLE_ALBUM_DYNAMIC_COLOR = "disable_album_dynamic_color"
        private const val KEY_MINI_PLAYER_STYLE = "mini_player_style"
        private const val KEY_CROSSFADE_SECONDS = "crossfade_seconds"
        private const val KEY_FANCY_PLAYER_ENABLED = "fancy_player_enabled"
        private const val KEY_FANCY_BACKGROUND_ENABLED = "fancy_background_enabled"
        private const val KEY_STOP_ON_TASK_REMOVED = "stop_on_task_removed"
        private const val KEY_LAST_FOLDER_TREE_PATH = "last_folder_tree_path"
        private const val KEY_SHUFFLE_ENABLED = "shuffle_enabled"
        private const val KEY_REPEAT_MODE = "repeat_mode"
    }
}
