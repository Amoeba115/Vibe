package me.ayra.music.lyrics

object EnhancedLrcParser {
    private val lineTimeRegex = Regex("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?]")
    private val wordTimeRegex = Regex("<(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?>")

    fun parse(raw: String, source: String = "Enhanced LRC"): Lyrics {
        if (!raw.contains(wordTimeRegex)) return LrcParser.parse(raw, source)
        val lines =
            raw
                .lineSequence()
                .mapNotNull(::parseLine)
                .sortedBy { it.startMs ?: Long.MAX_VALUE }
                .toList()
        return Lyrics(lines = lines, source = source)
    }

    private fun parseLine(line: String): LyricLine? {
        val lineStart = lineTimeRegex.find(line)?.toTimestampMs()
        val withoutLineTimes = line.replace(lineTimeRegex, "")
        val wordMatches = wordTimeRegex.findAll(withoutLineTimes).toList()
        if (lineStart == null && wordMatches.isEmpty()) return null

        val words = mutableListOf<LyricWord>()
        wordMatches.forEachIndexed { index, match ->
            val start = match.toTimestampMs() ?: return@forEachIndexed
            val wordStart = match.range.last + 1
            val wordEnd = wordMatches.getOrNull(index + 1)?.range?.first ?: withoutLineTimes.length
            val word = withoutLineTimes.substring(wordStart, wordEnd).trim()
            if (word.isNotEmpty()) words += LyricWord(start, word)
        }
        val text = withoutLineTimes.replace(wordTimeRegex, "").trim()
        return LyricLine(
            startMs = lineStart ?: words.firstOrNull()?.startMs,
            text = text,
            words = words,
        )
    }
}

