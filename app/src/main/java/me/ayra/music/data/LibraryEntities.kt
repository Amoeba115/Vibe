package me.ayra.music.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracks",
    indices = [
        Index("track_id"),
        Index("source"),
        Index("album_id"),
        Index("artist"),
        Index("folder"),
    ],
)
data class TrackEntity(
    @PrimaryKey
    @ColumnInfo(name = "cache_key")
    val cacheKey: String,
    @ColumnInfo(name = "track_id")
    val trackId: Long,
    val title: String,
    val artist: String,
    val album: String,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    @ColumnInfo(name = "uri")
    val uriString: String,
    @ColumnInfo(name = "album_id")
    val albumId: Long,
    val folder: String,
    @ColumnInfo(name = "track_number", defaultValue = "0")
    val trackNumber: Int,
    @ColumnInfo(name = "disc_number", defaultValue = "0")
    val discNumber: Int,
    @ColumnInfo(name = "year", defaultValue = "0")
    val year: Int,
    val source: String,
    @ColumnInfo(name = "last_modified_ms")
    val lastModifiedMs: Long,
    @ColumnInfo(name = "size_bytes")
    val sizeBytes: Long,
)

@Entity(tableName = "favorite_tracks")
data class FavoriteEntity(
    @PrimaryKey
    @ColumnInfo(name = "track_id")
    val trackId: Long,
)

@Entity(tableName = "album_cache")
data class AlbumCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "album_id")
    val albumId: Long,
    val title: String,
    val artist: String,
    @ColumnInfo(name = "track_count")
    val trackCount: Int,
)

@Entity(tableName = "artist_cache")
data class ArtistCacheEntity(
    @PrimaryKey
    val name: String,
    @ColumnInfo(name = "album_count")
    val albumCount: Int,
    @ColumnInfo(name = "track_count")
    val trackCount: Int,
)

@Entity(tableName = "vgm_metadata_cache")
data class VgmMetadataEntity(
    @PrimaryKey
    val path: String,
    @ColumnInfo(name = "last_modified_ms")
    val lastModifiedMs: Long,
    @ColumnInfo(name = "size_bytes")
    val sizeBytes: Long,
    val title: String,
    val artist: String,
    val album: String,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    val folder: String,
)

object LibrarySource {
    const val MediaStore = "mediastore"
    const val Vgm = "vgm"
}
