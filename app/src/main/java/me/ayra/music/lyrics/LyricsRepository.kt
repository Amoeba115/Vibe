package me.ayra.music.lyrics

import android.content.Context
import me.ayra.music.Track
import me.ayra.music.data.LibraryDatabase
import me.ayra.music.data.LyricsCandidateEntity

class LyricsRepository(
    context: Context,
    private val providers: List<LyricsProvider> = listOf(LrclibLyricsProvider()),
) {
    private val localProvider = LocalLyricsProvider(context.applicationContext)
    private val libraryDao = LibraryDatabase.get(context.applicationContext).libraryDao()
    private val cache = LinkedHashMap<Long, Lyrics?>()
    private val selectedOverrides = LinkedHashMap<Long, Lyrics>()

    suspend fun lyricsFor(track: Track): Lyrics? {
        cache[track.id]?.let { return it }
        val lyrics =
            localProvider.find(track)
                ?: selectedOverrides[track.id]
                ?: providers.firstNotNullOfOrNull { provider ->
                    provider.search(track.title, track.artist)
                }
        cache[track.id] = lyrics
        return lyrics
    }

    suspend fun searchResults(query: String): List<LyricsSearchResult> =
        providers.flatMap { provider -> provider.searchResults(query) }

    suspend fun localLyricsFor(track: Track): Lyrics? = localProvider.find(track)

    fun selectedLyricsFor(trackId: Long): Lyrics? = selectedOverrides[trackId]

    fun setSelectedLyrics(
        trackId: Long,
        lyrics: Lyrics?,
    ) {
        if (lyrics == null) {
            selectedOverrides.remove(trackId)
            cache.remove(trackId)
        } else {
            selectedOverrides[trackId] = lyrics
            cache[trackId] = lyrics
        }
    }

    suspend fun saveLocal(
        track: Track,
        lyrics: Lyrics,
    ): Boolean {
        val saved = localProvider.save(track, lyrics)
        if (saved) {
            selectedOverrides.remove(track.id)
            cache[track.id] = lyrics.copy(source = LOCAL_SOURCE)
        }
        return saved
    }

    suspend fun deleteLocal(track: Track): Boolean {
        val deleted = localProvider.delete(track)
        if (deleted) {
            selectedOverrides[track.id]?.let { cache[track.id] = it } ?: cache.remove(track.id)
        }
        return deleted
    }

    suspend fun candidatesFor(tracks: List<Track>): Map<Long, List<LyricsCandidate>> {
        if (tracks.isEmpty()) return emptyMap()
        return libraryDao
            .loadLyricsCandidates(tracks.map(Track::id).distinct())
            .map(LyricsCandidateEntity::toLyricsCandidate)
            .groupBy(LyricsCandidate::trackId)
    }

    suspend fun replaceCandidates(
        track: Track,
        results: List<LyricsSearchResult>,
    ) {
        libraryDao.replaceLyricsCandidates(
            track.id,
            results.take(MAX_CANDIDATES_PER_TRACK).mapIndexed { index, result ->
                result.toEntity(track.id, index)
            },
        )
    }

    suspend fun updateCandidate(
        candidate: LyricsCandidate,
        lyricsText: String,
    ): LyricsCandidate? {
        val lyrics = parseLyrics(lyricsText, candidate.lyrics.source)
        if (lyrics.lines.isEmpty()) return null
        val updated = candidate.copy(result = candidate.result.copy(lyrics = lyrics))
        libraryDao.upsertLyricsCandidates(listOf(updated.toEntity()))
        return updated
    }

    suspend fun deleteCandidate(candidate: LyricsCandidate) {
        libraryDao.deleteLyricsCandidate(candidate.trackId, candidate.candidateIndex)
    }

    suspend fun selectCandidate(
        track: Track,
        candidate: LyricsCandidate,
    ): Boolean {
        val saved = saveLocal(track, candidate.lyrics)
        if (saved) {
            libraryDao.clearSelectedLyricsCandidate(track.id)
            libraryDao.selectLyricsCandidate(track.id, candidate.candidateIndex)
        }
        return saved
    }

    companion object {
        const val LOCAL_SOURCE = "Local"
        const val MAX_CANDIDATES_PER_TRACK = 5
    }
}

data class LyricsCandidate(
    val trackId: Long,
    val candidateIndex: Int,
    val result: LyricsSearchResult,
    val isSelected: Boolean,
) {
    val lyrics: Lyrics
        get() = result.lyrics
}

fun Lyrics.toEditableText(): String =
    lines.joinToString("\n") { line ->
        line.startMs?.let(::formatLrcTimestamp).orEmpty() + line.text
    }

private fun formatLrcTimestamp(timeMs: Long): String {
    val minutes = timeMs / 60_000
    val seconds = (timeMs % 60_000) / 1_000
    val hundredths = (timeMs % 1_000) / 10
    return "[%02d:%02d.%02d]".format(minutes, seconds, hundredths)
}

private fun LyricsSearchResult.toEntity(
    trackId: Long,
    candidateIndex: Int,
    isSelected: Boolean = false,
): LyricsCandidateEntity =
    LyricsCandidateEntity(
        trackId = trackId,
        candidateIndex = candidateIndex,
        resultId = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        lyricsText = lyrics.toEditableText(),
        lyricsSource = lyrics.source,
        isSelected = isSelected,
    )

private fun LyricsCandidate.toEntity(): LyricsCandidateEntity =
    result.toEntity(
        trackId = trackId,
        candidateIndex = candidateIndex,
        isSelected = isSelected,
    )

private fun LyricsCandidateEntity.toLyricsCandidate(): LyricsCandidate =
    LyricsCandidate(
        trackId = trackId,
        candidateIndex = candidateIndex,
        result =
            LyricsSearchResult(
                id = resultId,
                title = title,
                artist = artist,
                album = album,
                durationMs = durationMs,
                lyrics = parseLyrics(lyricsText, lyricsSource),
            ),
        isSelected = isSelected,
    )

fun parseLyrics(
    raw: String,
    source: String,
): Lyrics =
    when {
        raw.contains(Regex("<\\d{1,2}:\\d{2}(?:[.:]\\d{1,3})?>")) -> EnhancedLrcParser.parse(raw, source)
        raw.contains(Regex("\\[\\d{1,2}:\\d{2}(?:[.:]\\d{1,3})?]")) -> LrcParser.parse(raw, source)
        else -> LrcParser.parsePlain(raw, source)
    }
