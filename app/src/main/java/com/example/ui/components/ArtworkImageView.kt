package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.Coil
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.data.model.Artwork
import com.example.ui.theme.ChampagneGold
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
    ambientGlowIntensity: Float = 0.75f,
    museumFrameMatte: Boolean = true,
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
                    .setHeader("User-Agent", "ArtGalleryTV/1.0 (Android; Museum Frame)")
                    .allowHardware(false)
                    .build()
                val result = (loader.execute(request) as? SuccessResult)?.drawable
                val bitmap = (result as? android.graphics.drawable.BitmapDrawable)?.bitmap
                if (bitmap != null) {
                    val palette = AmbientLightExtractor.extractFromBitmap(bitmap)
                    currentPalette = palette
                }
            } catch (_: Exception) {
                // Keep fallback palette
            }
        }
    }

    val animatedDominant by animateColorAsState(
        targetValue = currentPalette.dominantColor,
        animationSpec = tween(1200),
        label = "AmbientDominant"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = currentPalette.secondaryColor,
        animationSpec = tween(1200),
        label = "AmbientSecondary"
    )
    val animatedGlow by animateColorAsState(
        targetValue = currentPalette.glowColor,
        animationSpec = tween(1200),
        label = "AmbientGlow"
    )

    val transition = rememberInfiniteTransition(label = "KenBurnsTransition")
    val scale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "KenBurnsScale"
    )

    val panOffsetX by transition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "KenBurnsPanX"
    )

    val panOffsetY by transition.animateFloat(
        initialValue = 8f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "KenBurnsPanY"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(DarkObsidian)
            .testTag("artwork_image_view"),
        contentAlignment = Alignment.Center
    ) {
        val isCompact = maxWidth < 740.dp
        val screenWidthDp = maxWidth
        val screenHeightDp = maxHeight
        val maxDimPx = maxOf(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat()).coerceAtLeast(1000f)

        // Dynamic Ambient Light Backlight Layer (Philips Ambilight / Samsung The Frame Glow)
        if (ambientBacklight && artwork != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                animatedGlow.copy(alpha = (0.68f * ambientGlowIntensity).coerceIn(0f, 0.92f)),
                                animatedDominant.copy(alpha = (0.52f * ambientGlowIntensity).coerceIn(0f, 0.88f)),
                                animatedSecondary.copy(alpha = (0.36f * ambientGlowIntensity).coerceIn(0f, 0.78f)),
                                DarkObsidian
                            ),
                            radius = maxDimPx * 0.82f
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                animatedDominant.copy(alpha = (0.38f * ambientGlowIntensity).coerceIn(0f, 0.72f)),
                                Color.Transparent,
                                animatedSecondary.copy(alpha = (0.46f * ambientGlowIntensity).coerceIn(0f, 0.82f))
                            )
                        )
                    )
            )
        }

        if (artwork != null) {
            Crossfade(
                targetState = artwork,
                animationSpec = tween(1000),
                label = "ArtworkCrossfade"
            ) { currentArt ->
                val outerHorizontalPadding = if (museumFrameMatte) {
                    if (isCompact) 14.dp else 44.dp
                } else {
                    0.dp
                }
                val outerVerticalPadding = if (museumFrameMatte) {
                    if (isCompact) 190.dp else 32.dp
                } else {
                    0.dp
                }
                val availW = (screenWidthDp - outerHorizontalPadding * 2).coerceAtLeast(160.dp)
                val availH = (screenHeightDp - outerVerticalPadding * 2).coerceAtLeast(160.dp)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = outerHorizontalPadding, vertical = outerVerticalPadding),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentArt.imageUrl)
                            .setHeader("User-Agent", "ArtGalleryTV/1.0 (Android; Museum Frame)")
                            .crossfade(true)
                            .build(),
                        contentDescription = currentArt.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        success = { state ->
                            val intrinsicSize = state.painter.intrinsicSize
                            val ratio = if (intrinsicSize.isSpecified && intrinsicSize.width > 0f && intrinsicSize.height > 0f) {
                                (intrinsicSize.width / intrinsicSize.height).coerceIn(0.45f, 2.4f)
                            } else {
                                4f / 3f
                            }
                            val matchHeightFirst = (availW.value / availH.value) > ratio

                            this@SubcomposeAsyncImage.SubcomposeAsyncImageContent(
                                modifier = Modifier
                                    .then(
                                        if (museumFrameMatte) {
                                            Modifier
                                                .aspectRatio(ratio, matchHeightConstraintsFirst = matchHeightFirst)
                                                .shadow(24.dp, RoundedCornerShape(10.dp))
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFFF6F1E3))
                                                .border(3.5.dp, ChampagneGold, RoundedCornerShape(10.dp))
                                                .padding(if (isCompact) 9.dp else 14.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .border(1.dp, Color(0xFFB8963E), RoundedCornerShape(4.dp))
                                        } else {
                                            Modifier.fillMaxSize()
                                        }
                                    )
                                    .scale(if (kenBurnsEffect) scale else 1.0f)
                                    .offset {
                                        if (kenBurnsEffect) {
                                            IntOffset(panOffsetX.roundToInt(), panOffsetY.roundToInt())
                                        } else {
                                            IntOffset.Zero
                                        }
                                    },
                                contentScale = if (museumFrameMatte) ContentScale.Crop else ContentScale.Fit
                            )
                        },
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isCompact) 320.dp else 440.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ProceduralMasterpieceCanvas(
                                    artwork = currentArt,
                                    palette = currentPalette
                                )
                                CircularProgressIndicator(
                                    color = ChampagneGold,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isCompact) 320.dp else 440.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ProceduralMasterpieceCanvas(
                                    artwork = currentArt,
                                    palette = currentPalette
                                )
                            }
                        }
                    )
                }
            }
        } else {
            // Empty State (e.g. Empty Private Gallery)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isCompact) 24.dp else 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceGlass)
                        .border(1.5.dp, GoldLeafBorder, RoundedCornerShape(24.dp))
                        .padding(horizontal = if (isCompact) 24.dp else 40.dp, vertical = 32.dp)
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
                        color = ChampagneGold,
                        textAlign = TextAlign.Center
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

@Composable
private fun ProceduralMasterpieceCanvas(
    artwork: Artwork,
    palette: AmbientPalette
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
    ) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    palette.secondaryColor,
                    palette.dominantColor,
                    palette.secondaryColor.copy(alpha = 0.9f)
                )
            )
        )

        val sunCenter = Offset(w * 0.72f, h * 0.28f)
        drawCircle(
            color = palette.glowColor.copy(alpha = 0.25f),
            radius = minOf(w, h) * 0.24f,
            center = sunCenter
        )
        drawCircle(
            color = palette.glowColor.copy(alpha = 0.5f),
            radius = minOf(w, h) * 0.14f,
            center = sunCenter
        )
        drawCircle(
            color = Color(0xFFFFF3C4).copy(alpha = 0.88f),
            radius = minOf(w, h) * 0.065f,
            center = sunCenter
        )

        val mountainPath = Path().apply {
            moveTo(0f, h * 0.72f)
            lineTo(w * 0.22f, h * 0.46f)
            lineTo(w * 0.45f, h * 0.62f)
            lineTo(w * 0.68f, h * 0.42f)
            lineTo(w, h * 0.65f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = mountainPath,
            color = palette.dominantColor.copy(alpha = 0.75f)
        )

        val hillsPath = Path().apply {
            moveTo(0f, h * 0.82f)
            quadraticTo(w * 0.28f, h * 0.64f, w * 0.55f, h * 0.78f)
            quadraticTo(w * 0.80f, h * 0.88f, w, h * 0.72f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = hillsPath,
            color = palette.glowColor.copy(alpha = 0.35f)
        )
    }
}
