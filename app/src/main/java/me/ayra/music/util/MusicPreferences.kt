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

    private companion object {
        const val KEY_LAST_TAB = "last_tab"
        const val KEY_LAST_TRACK_ID = "last_track_id"
        const val NO_TRACK_ID = -1L
    }
}
