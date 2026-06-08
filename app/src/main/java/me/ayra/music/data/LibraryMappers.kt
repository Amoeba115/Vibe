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
        trackNumber = trackNumber,
        discNumber = discNumber,
        year = year,
        dateAddedMs = dateAddedMs,
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
        trackNumber = 0,
        discNumber = 0,
        year = 0,
        dateAddedMs = lastModifiedMs,
        source = LibrarySource.Vgm,
        lastModifiedMs = lastModifiedMs,
        sizeBytes = sizeBytes,
    )
