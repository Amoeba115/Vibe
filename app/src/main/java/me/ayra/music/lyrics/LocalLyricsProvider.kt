package me.ayra.music.lyrics

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.ayra.music.Track
import java.io.File

class LocalLyricsProvider(
    private val context: Context,
) {
    suspend fun find(track: Track): Lyrics? =
        withContext(Dispatchers.IO) {
            lyricCandidates(track)
                .firstNotNullOfOrNull { file ->
                    runCatching {
                        if (file.isFile && file.canRead()) {
                            parseLyrics(file.readText(), LyricsRepository.LOCAL_SOURCE)
                        } else {
                            null
                        }
                    }.getOrNull()
                }
        }

    suspend fun save(
        track: Track,
        lyrics: Lyrics,
    ): Boolean =
        withContext(Dispatchers.IO) {
            val target = writableLyricFile(track) ?: return@withContext false
            runCatching {
                target.parentFile?.mkdirs()
                target.writeText(lyrics.toLrcText())
            }.isSuccess
        }

    suspend fun delete(track: Track): Boolean =
        withContext(Dispatchers.IO) {
            lyricCandidates(track)
                .firstOrNull { it.isFile && it.canWrite() }
                ?.let { runCatching { it.delete() }.getOrDefault(false) }
                ?: false
        }

    private fun writableLyricFile(track: Track): File? =
        lyricCandidates(track).firstOrNull { candidate ->
            candidate.parentFile?.let { it.exists() && it.canWrite() } == true
        } ?: run {
            if (track.uri.scheme == "file") {
                track.uri.path?.let { path -> File(path).withExtension("lrc") }
            } else {
                queryFilePath(track.uri)?.let { path -> File(path).withExtension("lrc") }
            }
        }

    private fun lyricCandidates(track: Track): List<File> {
        val candidates = linkedSetOf<File>()
        if (track.uri.scheme == "file") {
            track.uri.path?.let { path ->
                File(path).let { file ->
                    candidates += file.withExtension("lrc")
                    candidates += File(file.parentFile, "${track.title}.lrc")
                }
            }
        }

        queryFilePath(track.uri)?.let { path ->
            File(path).let { file ->
                candidates += file.withExtension("lrc")
                candidates += File(file.parentFile, "${track.title}.lrc")
            }
        }

        @Suppress("DEPRECATION")
        val externalRoot = Environment.getExternalStorageDirectory()
        val folder = track.folder.trim('/').replace('\\', '/')
        if (folder.isNotBlank()) {
            val directory = File(externalRoot, folder)
            candidates += File(directory, "${track.title}.lrc")
        }
        return candidates.toList()
    }

    private fun queryFilePath(uri: Uri): String? {
        if (uri.scheme != "content") return null
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        return runCatching {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        }.getOrNull()
    }

    private fun File.withExtension(extension: String): File =
        File(parentFile, "${nameWithoutExtension}.$extension")
}

private fun Lyrics.toLrcText(): String =
    if (synchronized) {
        lines.joinToString(separator = "\n") { line ->
            line.startMs?.let { "${it.toLrcTimestamp()}${line.text}" } ?: line.text
        }
    } else {
        lines.joinToString(separator = "\n") { it.text }
    }

private fun Long.toLrcTimestamp(): String {
    val totalCentiseconds = (coerceAtLeast(0L) / 10L).toInt()
    val minutes = totalCentiseconds / 6_000
    val seconds = (totalCentiseconds % 6_000) / 100
    val centiseconds = totalCentiseconds % 100
    return "[${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}.${centiseconds.toString().padStart(2, '0')}]"
}
