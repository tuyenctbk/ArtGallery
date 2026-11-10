package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Artwork

@Entity(tableName = "favorite_artworks")
data class FavoriteArtworkEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val artistDisplayName: String,
    val artistDisplayBio: String = "",
    val objectDate: String = "",
    val medium: String = "",
    val dimensions: String = "",
    val department: String = "",
    val creditLine: String = "",
    val accessionNumber: String = "",
    val imageUrl: String,
    val thumbnailUrl: String = "",
    val culture: String = "",
    val period: String = "",
    val description: String = "",
    val savedTimestamp: Long = System.currentTimeMillis()
) {
    fun toArtwork(): Artwork {
        return Artwork(
            id = id,
            title = title,
            artistDisplayName = artistDisplayName,
            artistDisplayBio = artistDisplayBio,
            objectDate = objectDate,
            medium = medium,
            dimensions = dimensions,
            department = department,
            creditLine = creditLine,
            accessionNumber = accessionNumber,
            imageUrl = imageUrl,
            thumbnailUrl = if (thumbnailUrl.isNotEmpty()) thumbnailUrl else imageUrl,
            culture = culture,
            period = period,
            isHighlight = true,
            isFavorite = true,
            description = description
        )
    }

    companion object {
        fun fromArtwork(art: Artwork): FavoriteArtworkEntity {
            return FavoriteArtworkEntity(
                id = art.id,
                title = art.title,
                artistDisplayName = art.artistDisplayName,
                artistDisplayBio = art.artistDisplayBio,
                objectDate = art.objectDate,
                medium = art.medium,
                dimensions = art.dimensions,
                department = art.department,
                creditLine = art.creditLine,
                accessionNumber = art.accessionNumber,
                imageUrl = art.imageUrl,
                thumbnailUrl = art.thumbnailUrl,
                culture = art.culture,
                period = art.period,
                description = art.description
            )
        }
    }
}
