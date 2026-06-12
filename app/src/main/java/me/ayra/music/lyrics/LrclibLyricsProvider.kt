package me.ayra.music.lyrics

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

class LrclibLyricsProvider(
    private val client: HttpClient = defaultClient(),
) : LyricsProvider {
    override suspend fun search(
        title: String,
        artist: String,
    ): Lyrics? =
        withContext(Dispatchers.IO) {
            runCatching {
                val response =
                    client.get("https://lrclib.net/api/search") {
                        parameter("track_name", title)

                        if (!artist.isUnknownArtist()) {
                            parameter("artist_name", artist)
                        }
                    }
                if (!response.status.isSuccess()) return@runCatching null
                val body = response.body<String>()
                val records = Json.parseToJsonElement(body).jsonArray

                val titleWeight = 85
                val artistWeight = 15

                val best =
                    records
                        .map { record ->
                            val obj = record.jsonObject

                            val titleScore =
                                similarity(
                                    title,
                                    obj["trackName"]
                                        ?.jsonPrimitive
                                        ?.content
                                        .orEmpty(),
                                )

                            val artistScore =
                                if (artist.isUnknownArtist()) {
                                    100
                                } else {
                                    similarity(
                                        artist,
                                        obj["artistName"]
                                            ?.jsonPrimitive
                                            ?.content
                                            .orEmpty(),
                                    )
                                }

                            val totalScore =
                                if (artist.isUnknownArtist()) {
                                    titleScore
                                } else {
                                    (
                                        titleScore * titleWeight +
                                            artistScore * artistWeight
                                    ) / 100
                                }

                            record to totalScore
                        }.maxByOrNull { it.second }
                        ?.takeIf { it.second >= 70 } // minimum similarity
                        ?.first
                best?.jsonObject?.let { obj ->
                    val synced = obj["syncedLyrics"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                    val plain = obj["plainLyrics"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                    when {
                        synced != null -> parseLyrics(synced, "LRCLIB")
                        plain != null -> LrcParser.parsePlain(plain, "LRCLIB")
                        else -> null
                    }
                }
            }.getOrNull()
        }

    override suspend fun searchResults(query: String): List<LyricsSearchResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response =
                    client.get("https://lrclib.net/api/search") {
                        parameter("q", query)
                    }
                if (!response.status.isSuccess()) return@runCatching emptyList()
                Json
                    .parseToJsonElement(response.body<String>())
                    .jsonArray
                    .mapNotNull { record ->
                        val obj = record.jsonObject
                        val synced = obj["syncedLyrics"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                        val plain = obj["plainLyrics"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                        val lyrics =
                            when {
                                synced != null -> parseLyrics(synced, "LRCLIB")
                                plain != null -> LrcParser.parsePlain(plain, "LRCLIB")
                                else -> return@mapNotNull null
                            }
                        LyricsSearchResult(
                            id = obj["id"]?.jsonPrimitive?.longOrNull ?: 0L,
                            title = obj["trackName"]?.jsonPrimitive?.content.orEmpty(),
                            artist = obj["artistName"]?.jsonPrimitive?.content.orEmpty(),
                            album = obj["albumName"]?.jsonPrimitive?.content.orEmpty(),
                            durationMs =
                                (
                                    obj["duration"]
                                        ?.jsonPrimitive
                                        ?.doubleOrNull
                                        ?: 0.0
                                ).times(1000)
                                    .toLong(),
                            lyrics = lyrics,
                        )
                    }
            }.getOrDefault(emptyList())
        }

    private fun String?.isUnknownArtist(): Boolean {
        val value = this?.trim()?.lowercase() ?: return true

        return value.isBlank() ||
            value == "unknown" ||
            value == "unknown artist" ||
            value == "<unknown>" ||
            value == "n/a" ||
            value == "various artists"
    }

    private fun similarity(
        a: String,
        b: String,
    ): Int {
        val left = normalize(a)
        val right = normalize(b)

        if (left == right) {
            return 100
        }

        val leftTokens =
            left
                .split(' ')
                .filter { it.isNotBlank() }

        val rightTokens =
            right
                .split(' ')
                .filter { it.isNotBlank() }

        if (leftTokens.isEmpty() || rightTokens.isEmpty()) {
            return 0
        }

        val leftSet = leftTokens.toSet()
        val rightSet = rightTokens.toSet()

        val intersection =
            leftSet.intersect(rightSet).size

        val union =
            leftSet.union(rightSet).size

        var score =
            (
                intersection.toFloat() /
                    union.toFloat() * 100f
            ).toInt()

        val extraWords =
            (rightSet - leftSet)

        score -= extraWords.size * 10

        return score.coerceIn(0, 100)
    }

    private fun normalize(text: String): String =
        text
            .lowercase()
            .replace('(', ' ')
            .replace(')', ' ')
            .replace('[', ' ')
            .replace(']', ' ')
            .replace(Regex("""[^\p{L}\p{N}]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

    companion object {
        private fun defaultClient(): HttpClient =
            HttpClient(Android) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
    }
}
