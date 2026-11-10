package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteArtworkDao {

    @Query("SELECT * FROM favorite_artworks ORDER BY savedTimestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteArtworkEntity>>

    @Query("SELECT id FROM favorite_artworks")
    fun getFavoriteIdsFlow(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(artwork: FavoriteArtworkEntity)

    @Query("DELETE FROM favorite_artworks WHERE id = :id")
    suspend fun deleteFavoriteById(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_artworks WHERE id = :id)")
    suspend fun isFavorite(id: Int): Boolean

    @Query("SELECT * FROM cached_artworks WHERE categoryId = :categoryId ORDER BY cachedTimestamp DESC")
    suspend fun getCachedArtworksByCategory(categoryId: String): List<CachedArtworkEntity>

    @Query("SELECT * FROM cached_artworks ORDER BY cachedTimestamp DESC LIMIT 40")
    suspend fun getAllCachedArtworks(): List<CachedArtworkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedArtworks(artworks: List<CachedArtworkEntity>)
}
