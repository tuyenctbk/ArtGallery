package com.example.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Artwork(
    val id: Int,
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
    val thumbnailUrl: String = imageUrl,
    val culture: String = "",
    val period: String = "",
    val isHighlight: Boolean = true,
    val isFavorite: Boolean = false,
    val description: String = ""
)

enum class SoundscapeType(val displayName: String, val description: String) {
    MUTE("Mute", "Silent museum atmosphere"),
    MUSEUM_HALL("Museum Gallery", "Quiet Echoes of a Grand Art Hall"),
    RAIN_SKYLIGHT("Skylight Rain", "Gentle Raindrops on Glass Dome"),
    GENTLE_PIANO("Gallery Piano", "Soft Ambient Classical Melodies"),
    HEARTH_FIRE("Warm Hearth", "Cozy Crackling Museum Fireplace"),
    OCEAN_BREEZE("Seaside Gallery", "Calm Waves & Soft Coastal Wind")
}

enum class CategoryType {
    ALL, FAVORITES, DEPARTMENT, MOVEMENT
}

@Immutable
data class MuseumCategory(
    val id: String,
    val title: String,
    val categoryType: CategoryType,
    val departmentId: Int? = null,
    val query: String = "",
    val thumbnailUrl: String,
    val description: String
)

@Immutable
data class SlideshowConfig(
    val intervalSeconds: Int = 15,
    val autoHidePlacard: Boolean = true,
    val placardTimeoutSeconds: Int = 5,
    val showClock: Boolean = true,
    val soundscape: SoundscapeType = SoundscapeType.MUTE,
    val departmentFilter: String = "All Masterpieces",
    val activeCategory: MuseumCategory? = null,
    val favoritesOnly: Boolean = false,
    val kenBurnsEffect: Boolean = true,
    val deskStandMode: Boolean = false,
    val showMuseumLogo: Boolean = true,
    val ambientBacklight: Boolean = true,
    val ambientGlowIntensity: Float = 0.65f
)
