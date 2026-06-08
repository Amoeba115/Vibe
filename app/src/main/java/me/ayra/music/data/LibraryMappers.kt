package me.ayra.music.data

import android.net.Uri
import me.ayra.music.Track

data class ScannedTrack(
    val track: Track,
    val cacheKey: String,
    val source: String,
    val lastModifiedMs: Long,
    val sizeBytes: Long,
)

data class TrackSnapshot(
    val cacheKey: String,
    val source: String,
    val lastModifiedMs: Long,
    val sizeBytes: Long,
)

fun TrackEntity.toSnapshot(): TrackSnapshot =
    TrackSnapshot(
        cacheKey = cacheKey,
        source = source,
        lastModifiedMs = lastModifiedMs,
        sizeBytes = sizeBytes,
    )

fun ScannedTrack.toSnapshot(): TrackSnapshot =
    TrackSnapshot(
        cacheKey = cacheKey,
        source = source,
        lastModifiedMs = lastModifiedMs,
        sizeBytes = sizeBytes,
    )

fun TrackEntity.toTrack(): Track =
    Track(
        id = trackId,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        uri = Uri.parse(uriString),
        albumId = albumId,
        folder = folder,
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
        source = source,
        lastModifiedMs = lastModifiedMs,
        sizeBytes = sizeBytes,
    )

fun TrackEntity.toVgmMetadata(): VgmMetadataEntity =
    VgmMetadataEntity(
        path = cacheKey,
        lastModifiedMs = lastModifiedMs,
        sizeBytes = sizeBytes,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        folder = folder,
    )

fun VgmMetadataEntity.toEntity(uriString: String, trackId: Long): TrackEntity =
    TrackEntity(
        cacheKey = path,
        trackId = trackId,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        uriString = uriString,
        albumId = 0L,
        folder = folder,
        source = LibrarySource.Vgm,
        lastModifiedMs = lastModifiedMs,
        sizeBytes = sizeBytes,
    )
