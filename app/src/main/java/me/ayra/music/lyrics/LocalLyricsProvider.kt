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
                            parseLyrics(file.readText(), "Local")
                        } else {
                            null
                        }
                    }.getOrNull()
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

