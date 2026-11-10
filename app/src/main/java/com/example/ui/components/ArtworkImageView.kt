package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import coil.Coil
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.data.model.Artwork
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.GoldLeafBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.WarmIvory
import com.example.ui.util.AmbientLightExtractor
import com.example.ui.util.AmbientPalette
import kotlin.math.roundToInt

@Composable
fun ArtworkImageView(
    artwork: Artwork?,
    kenBurnsEffect: Boolean,
    ambientBacklight: Boolean = true,
    ambientGlowIntensity: Float = 0.65f,
    onExploreClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentPalette by remember(artwork?.id) {
        mutableStateOf(AmbientLightExtractor.getFallbackPalette(artwork))
    }

    LaunchedEffect(artwork?.id) {
        if (artwork != null) {
            try {
                val loader = Coil.imageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(artwork.thumbnailUrl)
                    .allowHardware(false)
                    .build()
                val result = (loader.execute(request) as? SuccessResult)?.drawable
                val bitmap = (result as? android.graphics.drawable.BitmapDrawable)?.bitmap
                if (bitmap != null) {
                    val palette = AmbientLightExtractor.extractFromBitmap(bitmap)
                    currentPalette = palette
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val animatedDominant by animateColorAsState(
        targetValue = currentPalette.dominantColor,
        animationSpec = tween(1500),
        label = "AmbientDominant"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = currentPalette.secondaryColor,
        animationSpec = tween(1500),
        label = "AmbientSecondary"
    )
    val animatedGlow by animateColorAsState(
        targetValue = currentPalette.glowColor,
        animationSpec = tween(1500),
        label = "AmbientGlow"
    )

    val transition = rememberInfiniteTransition(label = "KenBurnsTransition")
    val scale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.09f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "KenBurnsScale"
    )

    val panOffsetX by transition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "KenBurnsPanX"
    )

    val panOffsetY by transition.animateFloat(
        initialValue = 10f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "KenBurnsPanY"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkObsidian)
            .testTag("artwork_image_view"),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic Ambient Light Backlight Layer (Philips Ambilight / Samsung The Frame Glow)
        if (ambientBacklight && artwork != null) {
            // Central radiant glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                animatedGlow.copy(alpha = 0.45f * ambientGlowIntensity),
                                animatedDominant.copy(alpha = 0.30f * ambientGlowIntensity),
                                animatedSecondary.copy(alpha = 0.18f * ambientGlowIntensity),
                                Color.Transparent
                            )
                        )
                    )
            )
            // Vertical atmospheric edge illumination
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                animatedDominant.copy(alpha = 0.25f * ambientGlowIntensity),
                                Color.Transparent,
                                animatedSecondary.copy(alpha = 0.35f * ambientGlowIntensity)
                            )
                        )
                    )
            )
        }

        if (artwork != null) {
            Crossfade(
                targetState = artwork.imageUrl,
                animationSpec = tween(1400),
                label = "ArtworkCrossfade"
            ) { imageUrl ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = artwork.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(if (kenBurnsEffect) scale else 1.0f)
                            .offset {
                                if (kenBurnsEffect) {
                                    IntOffset(panOffsetX.roundToInt(), panOffsetY.roundToInt())
                                } else {
                                    IntOffset.Zero
                                }
                            },
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = ChampagneGold,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Collections,
                                        contentDescription = "Image Placeholder",
                                        tint = MutedSlate,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = artwork.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = WarmIvory
                                    )
                                    Text(
                                        text = artwork.artistDisplayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ChampagneGold
                                    )
                                }
                            }
                        }
                    )

                    // Subtle museum vignette lighting around borders
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color(0x55000000),
                                        Color(0x99000000)
                                    )
                                )
                            )
                    )
                }
            }
        } else {
            // Empty State (e.g. Empty Private Gallery or filtered list)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceGlass)
                        .border(1.5.dp, GoldLeafBorder, RoundedCornerShape(24.dp))
                        .padding(horizontal = 40.dp, vertical = 32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(DarkObsidian)
                            .border(1.dp, ChampagneGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Empty Private Gallery",
                            tint = ChampagneGold,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Your Private Gallery is Empty",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = ChampagneGold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Mark masterworks as favorite while browsing to curate your personal museum collection.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSlate,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onExploreClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChampagneGold,
                            contentColor = DarkObsidian
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("empty_explore_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = null,
                            tint = DarkObsidian,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Explore Met Collection",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
