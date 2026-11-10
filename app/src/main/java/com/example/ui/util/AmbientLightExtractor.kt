package com.example.ui.util

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import com.example.data.model.Artwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

data class AmbientPalette(
    val dominantColor: Color = Color(0xFF242834),
    val secondaryColor: Color = Color(0xFF141720),
    val glowColor: Color = Color(0xFFCBB064)
)

object AmbientLightExtractor {

    suspend fun extractFromBitmap(bitmap: Bitmap): AmbientPalette = withContext(Dispatchers.Default) {
        try {
            // Downsample to 24x24 for instant computation
            val smallBitmap = Bitmap.createScaledBitmap(bitmap, 24, 24, true)
            var redSum = 0L
            var greenSum = 0L
            var blueSum = 0L
            val pixelCount = 24 * 24

            var maxSat = -1f
            var mostVibrantPixel = 0

            var topRed = 0L
            var topGreen = 0L
            var topBlue = 0L

            for (y in 0 until 24) {
                for (x in 0 until 24) {
                    val pixel = smallBitmap.getPixel(x, y)
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8) and 0xFF
                    val b = pixel and 0xFF

                    redSum += r
                    greenSum += g
                    blueSum += b

                    if (y < 6) {
                        topRed += r
                        topGreen += g
                        topBlue += b
                    }

                    // Find vibrant color with high saturation
                    val maxVal = max(r, max(g, b)).toFloat()
                    val minVal = min(r, min(g, b)).toFloat()
                    val sat = if (maxVal == 0f) 0f else (maxVal - minVal) / maxVal
                    val brightness = maxVal / 255f

                    // Favor colors with good saturation and reasonable brightness
                    if (sat > maxSat && brightness in 0.2f..0.9f) {
                        maxSat = sat
                        mostVibrantPixel = pixel
                    }
                }
            }

            if (smallBitmap != bitmap) {
                smallBitmap.recycle()
            }

            val avgR = (redSum / pixelCount).toInt().coerceIn(0, 255)
            val avgG = (greenSum / pixelCount).toInt().coerceIn(0, 255)
            val avgB = (blueSum / pixelCount).toInt().coerceIn(0, 255)

            val vibR = (mostVibrantPixel shr 16) and 0xFF
            val vibG = (mostVibrantPixel shr 8) and 0xFF
            val vibB = mostVibrantPixel and 0xFF

            val topR = (topRed / (24 * 6)).toInt().coerceIn(0, 255)
            val topG = (topGreen / (24 * 6)).toInt().coerceIn(0, 255)
            val topB = (topBlue / (24 * 6)).toInt().coerceIn(0, 255)

            // Warm and rich tones for ambient glow
            val glowColor = if (maxSat > 0.25f) {
                Color(vibR, vibG, vibB)
            } else {
                Color(topR, topG, topB)
            }

            val dominantColor = Color(avgR, avgG, avgB)
            val secondaryColor = Color(
                (avgR * 0.6f + vibR * 0.4f).toInt().coerceIn(0, 255),
                (avgG * 0.6f + vibG * 0.4f).toInt().coerceIn(0, 255),
                (avgB * 0.6f + vibB * 0.4f).toInt().coerceIn(0, 255)
            )

            AmbientPalette(
                dominantColor = dominantColor,
                secondaryColor = secondaryColor,
                glowColor = glowColor
            )
        } catch (e: Exception) {
            e.printStackTrace()
            AmbientPalette()
        }
    }

    fun getFallbackPalette(artwork: Artwork?): AmbientPalette {
        if (artwork == null) return AmbientPalette()
        val text = (artwork.title + " " + artwork.medium + " " + artwork.department).lowercase()
        return when {
            text.contains("cypresses") || text.contains("sunflowers") || text.contains("gogh") -> {
                AmbientPalette(
                    dominantColor = Color(0xFF6B5822),
                    secondaryColor = Color(0xFF2C394B),
                    glowColor = Color(0xFFD4A338)
                )
            }
            text.contains("water lily") || text.contains("monet") || text.contains("pond") -> {
                AmbientPalette(
                    dominantColor = Color(0xFF24463C),
                    secondaryColor = Color(0xFF1E2E40),
                    glowColor = Color(0xFF488B77)
                )
            }
            text.contains("wave") || text.contains("hokusai") || text.contains("japanese") -> {
                AmbientPalette(
                    dominantColor = Color(0xFF1B324B),
                    secondaryColor = Color(0xFF17202A),
                    glowColor = Color(0xFF3B6B9B)
                )
            }
            text.contains("sargent") || text.contains("madame") -> {
                AmbientPalette(
                    dominantColor = Color(0xFF3A2426),
                    secondaryColor = Color(0xFF1B181E),
                    glowColor = Color(0xFF8B424A)
                )
            }
            text.contains("rembrandt") || text.contains("vermeer") || text.contains("dutch") -> {
                AmbientPalette(
                    dominantColor = Color(0xFF42311E),
                    secondaryColor = Color(0xFF1C1712),
                    glowColor = Color(0xFF9E773A)
                )
            }
            else -> {
                AmbientPalette(
                    dominantColor = Color(0xFF2D3545),
                    secondaryColor = Color(0xFF161A24),
                    glowColor = Color(0xFFCBB064)
                )
            }
        }
    }
}
