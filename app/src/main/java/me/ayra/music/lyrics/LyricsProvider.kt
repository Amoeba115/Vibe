package me.ayra.music.lyrics

interface LyricsProvider {
    suspend fun search(
        title: String,
        artist: String,
        durationMs: Long = 0L,
    ): Lyrics?

    suspend fun searchResults(
        query: String,
    ): List<LyricsSearchResult> = emptyList()

    suspend fun searchResults(
        title: String,
        artist: String,
    ): List<LyricsSearchResult> = searchResults("$title $artist")
}
