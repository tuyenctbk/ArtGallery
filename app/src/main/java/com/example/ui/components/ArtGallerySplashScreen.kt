package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.GoldLeafBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.WarmIvory
import kotlinx.coroutines.delay

@Composable
fun ArtGallerySplashScreen(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(1800L)
            onDismiss()
        }
    }

    val transition = rememberInfiniteTransition(label = "SplashTransition")
    val pulseScale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(800)) + scaleOut(targetScale = 1.05f, animationSpec = tween(800)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkObsidian)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
                .testTag("art_gallery_splash_screen"),
            contentAlignment = Alignment.Center
        ) {
            // Background subtle museum frame border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .border(1.dp, GoldLeafBorder.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // Gold museum emblem badge
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(100.dp)
                        .shadow(24.dp, CircleShape, spotColor = ChampagneGold)
                        .clip(CircleShape)
                        .background(SurfaceGlass)
                        .border(2.dp, ChampagneGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Museum,
                        contentDescription = "Museum Emblem",
                        tint = ChampagneGold,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Museum Institution Subtitle
                Text(
                    text = "THE METROPOLITAN MUSEUM OF ART",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.5.sp,
                        fontSize = 13.sp
                    ),
                    color = ChampagneGold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // App Title
                Text(
                    text = "ARTGALLERY TV",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        letterSpacing = 3.sp
                    ),
                    color = WarmIvory
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tagline badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceGlass)
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "4K CC0 Public Domain Masterpieces • Ambient Daydream",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MutedSlate
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Loading Indicator & Hint
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = ChampagneGold,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Opening curated gallery...",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = MutedSlate
                    )
                }
            }
        }
    }
}
