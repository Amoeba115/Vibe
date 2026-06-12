package me.ayra.music.lyrics

object LrcParser {
    private val lineTimeRegex = Regex("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?]")

    fun parse(
        raw: String,
        source: String = "LRC",
    ): Lyrics {
        if (!raw.contains(lineTimeRegex)) return parsePlain(raw, source)
        val lines =
            raw
                .lineSequence()
                .flatMap { line ->
                    val matches = lineTimeRegex.findAll(line).toList()
                    if (matches.isEmpty()) {
                        emptySequence()
                    } else {
                        val text = line.replace(lineTimeRegex, "").trim()
                        matches
                            .mapNotNull { match ->
                                match.toTimestampMs()?.let { timestamp ->
                                    LyricLine(startMs = timestamp, text = text)
                                }
                            }.asSequence()
                    }
                }.sortedBy { it.startMs ?: Long.MAX_VALUE }
                .toList()
        return Lyrics(lines = lines.ifEmpty { parsePlain(raw, source).lines }, source = source)
    }

    fun parsePlain(
        raw: String,
        source: String = "Plain",
    ): Lyrics =
        Lyrics(
            lines =
                raw
                    .lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { LyricLine(startMs = null, text = it) }
                    .toList(),
            source = source,
        )
}

internal fun MatchResult.toTimestampMs(): Long? {
    val minutes = groupValues.getOrNull(1)?.toLongOrNull() ?: return null
    val seconds = groupValues.getOrNull(2)?.toLongOrNull() ?: return null
    val fraction = groupValues.getOrNull(3).orEmpty()
    val millis =
        when (fraction.length) {
            0 -> 0L
            1 -> fraction.toLongOrNull()?.times(100L)
            2 -> fraction.toLongOrNull()?.times(10L)
            else -> fraction.take(3).toLongOrNull()
        } ?: return null
    return minutes * 60_000L + seconds * 1_000L + millis
}
