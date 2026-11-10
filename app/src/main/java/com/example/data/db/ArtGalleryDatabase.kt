package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteArtworkEntity::class, CachedArtworkEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ArtGalleryDatabase : RoomDatabase() {

    abstract fun favoriteArtworkDao(): FavoriteArtworkDao

    companion object {
        @Volatile
        private var INSTANCE: ArtGalleryDatabase? = null

        fun getInstance(context: Context): ArtGalleryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArtGalleryDatabase::class.java,
                    "art_gallery_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
