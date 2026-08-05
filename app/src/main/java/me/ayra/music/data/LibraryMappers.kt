package me.ayra.music.data

import android.net.Uri
import me.ayra.music.AudioInfo
import me.ayra.music.Track

data class ScannedTrack(
    val track: Track,
    val cacheKey: String,
    val source: String,
    val lastModifiedMs: Long,
    val sizeBytes: Long,
    val audioInfo: AudioInfo? = track.audioInfo,
)

data class TrackSnapshot(
    val cacheKey: String,
    val source: String,
    val lastModifiedMs: Long,
    val sizeBytes: Long,
    val trackNumber: Int,
    val discNumber: Int,
    val year: Int,
    val dateAddedMs: Long,
)

fun TrackEntity.toSnapshot(): TrackSnapshot =
    TrackSnapshot(
        cacheKey = cacheKey,
        source = source,
        lastModifiedMs = lastModifiedMs,
        sizeBytes = sizeBytes,
        trackNumber = trackNumber,
        discNumber = discNumber,
        year = year,
        dateAddedMs = dateAddedMs,
    )

fun ScannedTrack.toSnapshot(): TrackSnapshot =
    TrackSnapshot(
        cacheKey = cacheKey,
        source = source,
        lastModifiedMs = lastModifiedMs,
        sizeBytes = sizeBytes,
        trackNumber = track.trackNumber,
        discNumber = track.discNumber,
        year = track.year,
        dateAddedMs = track.dateAddedMs,
    )

fun TrackEntity.toTrack(audioInfo: AudioInfo? = null): Track =
    Track(
        id = trackId,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        uri = Uri.parse(uriString),
        albumId = albumId,
        folder = folder,
        trackNumber = trackNumber,
        discNumber = discNumber,
        year = year,
        dateAddedMs = dateAddedMs,
        fileType = fileType,
        audioInfo = audioInfo,
    )

fun ScannedTrack.toEntity(): TrackEntity =
    TrackEntity(
        cacheKey = cacheKey,
        trackId = track.id,
        title = track.title,
        artist = track.artist,
        album = track.album,
        durationMs = track.durationMs,
        uriString = track.uri.toString(),
        albumId = track.albumId,
        folder = track.folder,
        trackNumber = track.trackNumber,
        discNumber = track.discNumber,
        year = track.year,
        dateAddedMs = track.dateAddedMs,
        fileType = track.fileType,
        source = source,
        lastModifiedMs = lastModifiedMs,
        sizeBytes = sizeBytes,
    )

fun AudioInfoEntity.toAudioInfo(): AudioInfo =
    AudioInfo(
        trackId = trackId,
        codec = codec,
        sampleRate = sampleRate,
        bitDepth = bitDepth,
        bitrate = bitrate,
        channels = channels,
    )

fun AudioInfo.toEntity(): AudioInfoEntity =
    AudioInfoEntity(
        trackId = trackId,
        codec = codec,
        sampleRate = sampleRate,
        bitDepth = bitDepth,
        bitrate = bitrate,
        channels = channels,
    )
