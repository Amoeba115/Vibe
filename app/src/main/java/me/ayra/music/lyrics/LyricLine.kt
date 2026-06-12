package me.ayra.music.lyrics

data class LyricWord(
    val startMs: Long,
    val text: String,
)

data class LyricLine(
    val startMs: Long?,
    val text: String,
    val words: List<LyricWord> = emptyList(),
)

data class Lyrics(
    val lines: List<LyricLine>,
    val source: String,
) {
    val synchronized: Boolean
        get() = lines.any { it.startMs != null }
}

data class LyricsSearchResult(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val lyrics: Lyrics,
)
