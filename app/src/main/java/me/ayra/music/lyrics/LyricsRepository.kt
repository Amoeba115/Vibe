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
