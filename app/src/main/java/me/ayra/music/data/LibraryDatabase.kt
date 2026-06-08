package me.ayra.music.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TrackEntity::class,
        FavoriteEntity::class,
        AlbumCacheEntity::class,
        ArtistCacheEntity::class,
        VgmMetadataEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class LibraryDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao

    companion object {
        @Volatile
        private var instance: LibraryDatabase? = null

        fun get(context: Context): LibraryDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LibraryDatabase::class.java,
                    "music_library.db",
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
