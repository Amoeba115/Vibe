package me.ayra.music.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface LibraryDao {
    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    suspend fun loadTracks(): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE cache_key IN (:cacheKeys)")
    suspend fun loadTracksByCacheKey(cacheKeys: List<String>): List<TrackEntity>

    @Upsert
    suspend fun upsertTracks(tracks: List<TrackEntity>)

    @Query("DELETE FROM tracks")
    suspend fun deleteAllTracks()

    @Query("DELETE FROM tracks WHERE source = :source")
    suspend fun deleteTracksBySource(source: String)

    @Query("DELETE FROM tracks WHERE source = :source AND cache_key NOT IN (:cacheKeys)")
    suspend fun deleteMissingTracks(source: String, cacheKeys: List<String>)

    @Query("SELECT track_id FROM favorite_tracks")
    suspend fun loadFavoriteIds(): List<Long>

    @Upsert
    suspend fun upsertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorite_tracks WHERE track_id = :trackId")
    suspend fun deleteFavorite(trackId: Long)

    @Upsert
    suspend fun upsertAlbums(albums: List<AlbumCacheEntity>)

    @Query("DELETE FROM album_cache")
    suspend fun deleteAlbumCache()

    @Upsert
    suspend fun upsertArtists(artists: List<ArtistCacheEntity>)

    @Query("DELETE FROM artist_cache")
    suspend fun deleteArtistCache()

    @Query("SELECT * FROM vgm_metadata_cache WHERE path IN (:paths)")
    suspend fun loadVgmMetadata(paths: List<String>): List<VgmMetadataEntity>

    @Upsert
    suspend fun upsertVgmMetadata(metadata: List<VgmMetadataEntity>)

    @Query("DELETE FROM vgm_metadata_cache")
    suspend fun deleteAllVgmMetadata()

    @Query("DELETE FROM vgm_metadata_cache WHERE path NOT IN (:paths)")
    suspend fun deleteMissingVgmMetadata(paths: List<String>)

    @Transaction
    suspend fun replaceSourceTracks(source: String, tracks: List<TrackEntity>) {
        if (tracks.isEmpty()) {
            deleteTracksBySource(source)
        } else {
            deleteMissingTracks(source, tracks.map { it.cacheKey })
            upsertTracks(tracks)
        }
    }

    @Transaction
    suspend fun replaceDerivedCaches(tracks: List<TrackEntity>) {
        deleteAlbumCache()
        deleteArtistCache()
        upsertAlbums(
            tracks.groupBy { it.albumId }
                .map { (_, items) ->
                    AlbumCacheEntity(
                        albumId = items.first().albumId,
                        title = items.first().album,
                        artist = items.first().artist,
                        trackCount = items.size,
                    )
                },
        )
        upsertArtists(
            tracks.groupBy { it.artist.ifBlank { "Unknown artist" } }
                .map { (name, items) ->
                    ArtistCacheEntity(
                        name = name,
                        albumCount = items.map { it.albumId }.distinct().size,
                        trackCount = items.size,
                    )
                },
        )
    }

    @Transaction
    suspend fun replaceVgmMetadata(metadata: List<VgmMetadataEntity>) {
        if (metadata.isEmpty()) {
            deleteAllVgmMetadata()
        } else {
            deleteMissingVgmMetadata(metadata.map { it.path })
            upsertVgmMetadata(metadata)
        }
    }
}
