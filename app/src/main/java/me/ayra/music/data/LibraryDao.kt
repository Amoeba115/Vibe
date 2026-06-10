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

    @Query("DELETE FROM tracks WHERE track_id IN (:trackIds)")
    suspend fun deleteTracksByIds(trackIds: List<Long>)

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

    @Query("SELECT * FROM favorite_items ORDER BY added_at DESC")
    suspend fun loadFavoriteItems(): List<FavoriteItemEntity>

    @Upsert
    suspend fun upsertFavoriteItem(favorite: FavoriteItemEntity)

    @Query("DELETE FROM favorite_items WHERE type = :type AND favorite_key = :key")
    suspend fun deleteFavoriteItem(type: String, key: String)

    @Query("SELECT * FROM track_stats")
    suspend fun loadTrackStats(): List<TrackStatsEntity>

    @Query("SELECT * FROM track_stats WHERE track_id = :trackId")
    suspend fun loadTrackStats(trackId: Long): TrackStatsEntity?

    @Upsert
    suspend fun upsertTrackStats(stats: TrackStatsEntity)

    @Query("SELECT * FROM custom_playlists ORDER BY created_at ASC")
    suspend fun loadCustomPlaylists(): List<CustomPlaylistEntity>

    @Query("SELECT * FROM custom_playlist_tracks ORDER BY playlist_id ASC, position ASC")
    suspend fun loadCustomPlaylistTracks(): List<CustomPlaylistTrackEntity>

    @Upsert
    suspend fun upsertCustomPlaylist(playlist: CustomPlaylistEntity)

    @Query("UPDATE custom_playlists SET name = :name WHERE playlist_id = :playlistId")
    suspend fun renameCustomPlaylist(
        playlistId: String,
        name: String,
    )

    @Query("DELETE FROM custom_playlists WHERE playlist_id = :playlistId")
    suspend fun deleteCustomPlaylist(playlistId: String)

    @Query("DELETE FROM custom_playlist_tracks WHERE playlist_id = :playlistId")
    suspend fun deleteCustomPlaylistTracks(playlistId: String)

    @Upsert
    suspend fun upsertCustomPlaylistTracks(tracks: List<CustomPlaylistTrackEntity>)

    @Transaction
    suspend fun replaceCustomPlaylistTracks(
        playlistId: String,
        tracks: List<CustomPlaylistTrackEntity>,
    ) {
        deleteCustomPlaylistTracks(playlistId)
        if (tracks.isNotEmpty()) upsertCustomPlaylistTracks(tracks)
    }

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
