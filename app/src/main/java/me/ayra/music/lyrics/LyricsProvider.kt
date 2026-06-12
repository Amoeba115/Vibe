package me.ayra.music.lyrics

interface LyricsProvider {
    suspend fun search(
        title: String,
        artist: String,
    ): Lyrics?

    suspend fun searchResults(
        query: String,
    ): List<LyricsSearchResult> = emptyList()
}
