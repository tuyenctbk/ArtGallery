package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GalleryDarkColorScheme = darkColorScheme(
    primary = ChampagneGold,
    onPrimary = DarkObsidian,
    secondary = SoftGoldAccent,
    onSecondary = DarkObsidian,
    tertiary = MutedSlate,
    background = DarkObsidian,
    onBackground = WarmIvory,
    surface = CharcoalSlate,
    onSurface = WarmIvory,
    surfaceVariant = SurfaceGlass,
    outline = DarkBorder
)

@Composable
fun ArtGalleryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GalleryDarkColorScheme,
        typography = ArtGalleryTypography,
        content = content
    )
}
