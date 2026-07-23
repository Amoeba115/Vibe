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
    @ColumnInfo(name = "date_added_ms", defaultValue = "0")
    val dateAddedMs: Long,
    val source: String,
    @ColumnInfo(name = "last_modified_ms")
    val lastModifiedMs: Long,
    @ColumnInfo(name = "size_bytes")
    val sizeBytes: Long,
)

@Entity(
    tableName = "audio_info",
    indices = [Index("track_id")],
)
data class AudioInfoEntity(
    @PrimaryKey
    @ColumnInfo(name = "track_id")
    val trackId: Long,
    val codec: String?,
    @ColumnInfo(name = "sample_rate")
    val sampleRate: Int?,
    @ColumnInfo(name = "bit_depth")
    val bitDepth: Int?,
    val bitrate: Int?,
    val channels: Int?,
)

@Entity(tableName = "favorite_tracks")
data class FavoriteEntity(
    @PrimaryKey
    @ColumnInfo(name = "track_id")
    val trackId: Long,
)

@Entity(
    tableName = "favorite_items",
    primaryKeys = ["type", "favorite_key"],
)
data class FavoriteItemEntity(
    val type: String,
    @ColumnInfo(name = "favorite_key")
    val key: String,
    @ColumnInfo(name = "added_at")
    val addedAt: Long,
)

@Entity(tableName = "track_stats")
data class TrackStatsEntity(
    @PrimaryKey
    @ColumnInfo(name = "track_id")
    val trackId: Long,
    @ColumnInfo(name = "play_count")
    val playCount: Int = 0,
    @ColumnInfo(name = "last_played")
    val lastPlayed: Long = 0L,
    @ColumnInfo(name = "skip_count")
    val skipCount: Int = 0,
)

@Entity(tableName = "custom_playlists")
data class CustomPlaylistEntity(
    @PrimaryKey
    @ColumnInfo(name = "playlist_id")
    val playlistId: String,
    val name: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)

@Entity(
    tableName = "custom_playlist_tracks",
    primaryKeys = ["playlist_id", "track_id"],
    indices = [Index("playlist_id"), Index("track_id")],
)
data class CustomPlaylistTrackEntity(
    @ColumnInfo(name = "playlist_id")
    val playlistId: String,
    @ColumnInfo(name = "track_id")
    val trackId: Long,
    @ColumnInfo(name = "position")
    val position: Int,
    @ColumnInfo(name = "added_at")
    val addedAt: Long,
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

object LibrarySource {
    const val MediaStore = "mediastore"
}
