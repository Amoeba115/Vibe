package me.ayra.music.lyrics

object LyricSynchronizer {
    fun activeLineIndex(
        lyrics: Lyrics?,
        positionMs: Long,
    ): Int {
        val lines = lyrics?.lines.orEmpty()
        if (lines.isEmpty()) return -1
        val timedLines = lines.withIndex().filter { it.value.startMs != null }
        if (timedLines.isEmpty()) return -1
        return timedLines.lastOrNull { (_, line) -> (line.startMs ?: Long.MAX_VALUE) <= positionMs }?.index ?: -1
    }

    fun activeWordIndex(
        line: LyricLine,
        positionMs: Long,
    ): Int = line.words.indexOfLast { it.startMs <= positionMs }
}

