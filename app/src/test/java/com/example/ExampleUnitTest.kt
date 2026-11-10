package com.example

import com.example.data.db.CachedArtworkEntity
import com.example.data.db.FavoriteArtworkEntity
import com.example.data.model.Artwork
import com.example.data.model.SlideshowConfig
import com.example.data.model.SoundscapeType
import com.example.ui.util.AmbientLightExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun slideshowConfig_defaultsAreValid() {
        val config = SlideshowConfig()
        assertEquals(15, config.intervalSeconds)
        assertTrue(config.autoHidePlacard)
        assertTrue(config.kenBurnsEffect)
        assertTrue(config.ambientBacklight)
        assertTrue(config.museumFrameMatte)
        assertEquals(SoundscapeType.MUTE, config.soundscape)
    }

    @Test
    fun favoriteArtworkEntity_roundTripConversionPreservesAllFields() {
        val artwork = Artwork(
            id = 436535,
            title = "Wheat Field with Cypresses",
            artistDisplayName = "Vincent van Gogh",
            artistDisplayBio = "Dutch, Zundert 1853–1890 Auvers-sur-Oise",
            objectDate = "1889",
            medium = "Oil on canvas",
            dimensions = "73.2 × 93.4 cm",
            department = "European Paintings",
            creditLine = "Purchase, The Annenberg Foundation Gift, 1993",
            accessionNumber = "1993.132",
            imageUrl = "https://collectionapi.metmuseum.org/api/image/436535.jpg",
            thumbnailUrl = "https://collectionapi.metmuseum.org/api/image/436535_small.jpg",
            culture = "Dutch",
            period = "Post-Impressionism",
            isHighlight = true,
            isFavorite = false,
            description = "Oil on canvas painting in the European Paintings collection."
        )

        val favoriteEntity = FavoriteArtworkEntity.fromArtwork(artwork)
        val restoredFromFavorite = favoriteEntity.toArtwork()
        assertEquals(artwork.copy(isFavorite = true), restoredFromFavorite)

        val cachedEntity = CachedArtworkEntity.fromArtwork(artwork, "dept_11")
        val restoredFromCache = cachedEntity.toArtwork()
        assertEquals(artwork, restoredFromCache)
        assertEquals("dept_11", cachedEntity.categoryId)
    }

    @Test
    fun ambientLightExtractor_returnsFallbackPaletteForAnyDepartment() {
        val nullPalette = AmbientLightExtractor.getFallbackPalette(null)
        assertNotNull(nullPalette)

        val asianArt = Artwork(
            id = 1001,
            title = "Landscape Scroll",
            artistDisplayName = "Unknown Artist",
            objectDate = "17th century",
            medium = "Ink on silk",
            department = "Asian Art",
            imageUrl = "https://example.org/image.jpg",
            thumbnailUrl = "https://example.org/thumb.jpg"
        )
        val palette = AmbientLightExtractor.getFallbackPalette(asianArt)
        assertNotNull(palette.dominantColor)
        assertNotNull(palette.secondaryColor)
        assertNotNull(palette.glowColor)
    }
}
