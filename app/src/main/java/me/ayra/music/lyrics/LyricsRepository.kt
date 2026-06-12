package me.ayra.music.lyrics

import android.content.Context
import me.ayra.music.Track

class LyricsRepository(
    context: Context,
    private val providers: List<LyricsProvider> = listOf(LrclibLyricsProvider()),
) {
    private val localProvider = LocalLyricsProvider(context.applicationContext)
    private val cache = LinkedHashMap<Long, Lyrics?>()

    suspend fun lyricsFor(track: Track): Lyrics? {
        cache[track.id]?.let { return it }
        val lyrics =
            localProvider.find(track)
                ?: providers.firstNotNullOfOrNull { provider ->
                    provider.search(track.title, track.artist)
                }
        cache[track.id] = lyrics
        return lyrics
    }

    suspend fun searchResults(query: String): List<LyricsSearchResult> =
        providers.flatMap { provider -> provider.searchResults(query) }

    suspend fun saveLocal(
        track: Track,
        lyrics: Lyrics,
    ): Boolean {
        val saved = localProvider.save(track, lyrics)
        if (saved) cache[track.id] = lyrics.copy(source = LOCAL_SOURCE)
        return saved
    }

    suspend fun deleteLocal(track: Track): Boolean {
        val deleted = localProvider.delete(track)
        if (deleted) cache.remove(track.id)
        return deleted
    }

    companion object {
        const val LOCAL_SOURCE = "Local"
    }
}

fun parseLyrics(
    raw: String,
    source: String,
): Lyrics =
    when {
        raw.contains(Regex("<\\d{1,2}:\\d{2}(?:[.:]\\d{1,3})?>")) -> EnhancedLrcParser.parse(raw, source)
        raw.contains(Regex("\\[\\d{1,2}:\\d{2}(?:[.:]\\d{1,3})?]")) -> LrcParser.parse(raw, source)
        else -> LrcParser.parsePlain(raw, source)
    }
