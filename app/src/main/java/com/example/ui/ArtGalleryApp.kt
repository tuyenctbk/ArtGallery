package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.MutedSlate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AmbientClockOverlay
import com.example.ui.components.ArtGallerySplashScreen
import com.example.ui.components.ArtworkDetailsModal
import com.example.ui.components.ArtworkImageView
import com.example.ui.components.CategorySelectionDialog
import com.example.ui.components.GalleryBrowserDialog
import com.example.ui.components.KioskControlBar
import com.example.ui.components.MuseumPlacard
import com.example.ui.components.SettingsDrawer
import com.example.ui.components.SupportPatronDialog
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.GoldLeafBorder
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.WarmIvory

@Composable
fun ArtGalleryApp(
    viewModel: ArtGalleryViewModel = viewModel(),
    isDreamService: Boolean = false
) {
    val config by viewModel.config.collectAsState()
    val artworks by viewModel.artworks.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val timerProgress by viewModel.timerProgress.collectAsState()
    val isPlacardVisible by viewModel.isPlacardVisible.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isBrowserOpen by viewModel.isBrowserOpen.collectAsState()
    val isDetailModalOpen by viewModel.isDetailModalOpen.collectAsState()
    val isCategorySelectionOpen by viewModel.isCategorySelectionOpen.collectAsState()
    val isPatronDialogOpen by viewModel.isPatronDialogOpen.collectAsState()
    val isPatron by viewModel.isPatron.collectAsState()
    val patronTier by viewModel.patronTier.collectAsState()
    val isLoadingCategory by viewModel.isLoadingCategory.collectAsState()
    val activeCategory by viewModel.activeCategory.collectAsState()

    var showSplash by remember { mutableStateOf(!isDreamService) }

    val currentArtwork = if (artworks.isNotEmpty() && currentIndex in artworks.indices) {
        artworks[currentIndex]
    } else {
        null
    }

    if (viewModel.isAnyModalOpen) {
        BackHandler {
            when {
                isCategorySelectionOpen -> viewModel.closeCategorySelection()
                isSettingsOpen -> viewModel.closeSettings()
                isBrowserOpen -> viewModel.closeBrowser()
                isDetailModalOpen -> viewModel.closeDetailModal()
                isPatronDialogOpen -> viewModel.closePatronDialog()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("art_gallery_app_root")
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        viewModel.onUserActivity()
                        viewModel.togglePlacard()
                    },
                    onDoubleTap = {
                        viewModel.togglePlayPause()
                    }
                )
            }
            .pointerInput(Unit) {
                var dragAmount = 0f
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAmount > 50f) {
                            viewModel.prevArtwork()
                        } else if (dragAmount < -50f) {
                            viewModel.nextArtwork()
                        }
                        dragAmount = 0f
                    },
                    onHorizontalDrag = { _, delta ->
                        dragAmount += delta
                    }
                )
            }
    ) {
        // Fullscreen 4K Artwork Canvas with multi-axis Ken Burns motion and rich empty state
        ArtworkImageView(
            artwork = currentArtwork,
            kenBurnsEffect = config.kenBurnsEffect,
            ambientBacklight = config.ambientBacklight,
            ambientGlowIntensity = config.ambientGlowIntensity,
            onExploreClick = { viewModel.openCategorySelection() }
        )

        // Subtle ambient timeline progress hairline bar at the very bottom
        if (isPlaying && currentArtwork != null && timerProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .align(Alignment.BottomCenter)
                    .background(ChampagneGold.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = timerProgress)
                        .height(2.5.dp)
                        .background(ChampagneGold.copy(alpha = 0.85f))
                )
            }
        }

        // Top Kiosk Control Bar (Auto-hides with placard)
        KioskControlBar(
            isVisible = isPlacardVisible || viewModel.isAnyModalOpen,
            isPlaying = isPlaying,
            currentIndex = currentIndex,
            totalCount = artworks.size,
            isFavorite = currentArtwork?.let { viewModel.isFavorite(it.id) } ?: false,
            currentDepartment = activeCategory.title,
            departments = viewModel.departments,
            currentSoundscape = config.soundscape,
            isDeskStandMode = config.deskStandMode,
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onNext = { viewModel.nextArtwork() },
            onPrev = { viewModel.prevArtwork() },
            onToggleFavorite = { currentArtwork?.let { viewModel.toggleFavorite(it.id) } },
            onOpenCategorySelection = { viewModel.openCategorySelection() },
            onSelectSoundscape = { viewModel.setSoundscape(it) },
            onToggleDeskStand = { viewModel.toggleDeskStandMode() },
            onOpenBrowser = { viewModel.openBrowser() },
            onOpenSettings = { viewModel.openSettings() },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Loading Met Museum API Artworks Toast Banner
        AnimatedVisibility(
            visible = isLoadingCategory,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceGlass)
                    .border(1.dp, GoldLeafBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 22.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = ChampagneGold,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = androidx.compose.ui.res.stringResource(
                            com.example.R.string.loading_category,
                            activeCategory.title
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            color = WarmIvory
                        )
                    )
                }
            }
        }

        // Top-Right Ambient Nightstand Clock Overlay
        if (config.showClock || config.deskStandMode) {
            AmbientClockOverlay(
                isVisible = isPlacardVisible || config.deskStandMode,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 92.dp, end = 24.dp)
            )
        }

        // Bottom-Left Museum Placard Tag (Auto-hides after 5s)
        if (currentArtwork != null) {
            MuseumPlacard(
                artwork = currentArtwork,
                isVisible = isPlacardVisible,
                isFavorite = viewModel.isFavorite(currentArtwork.id),
                onToggleFavorite = { viewModel.toggleFavorite(currentArtwork.id) },
                onClickInfo = { viewModel.openDetailModal() },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            )
        }

        // Department & Art Movement Selection Screen Dialog
        if (isCategorySelectionOpen) {
            CategorySelectionDialog(
                categories = viewModel.museumCategories,
                activeCategory = activeCategory,
                onSelectCategory = { viewModel.selectCategory(it) },
                onClose = { viewModel.closeCategorySelection() }
            )
        }

        // Settings Drawer
        if (isSettingsOpen) {
            SettingsDrawer(
                config = config,
                departments = viewModel.departments,
                isPatron = isPatron,
                patronTier = patronTier,
                onBecomePatron = { viewModel.becomePatron(it) },
                onOpenPatronDialog = { viewModel.openPatronDialog() },
                onUpdateConfig = { viewModel.updateConfig(it) },
                onClose = { viewModel.closeSettings() }
            )
        }

        // Multi-Channel Support & Patronage Dialog (Google Play, PayPal & QR, Ko-fi)
        if (isPatronDialogOpen) {
            val activity = androidx.activity.compose.LocalActivity.current
            SupportPatronDialog(
                isPatron = isPatron,
                patronTier = patronTier,
                onBecomePatron = { viewModel.becomePatron(it) },
                onLaunchBilling = { sku ->
                    if (activity != null) {
                        viewModel.donationManager.launchBillingFlow(activity, sku)
                    } else {
                        viewModel.becomePatron(sku)
                    }
                },
                onClose = { viewModel.closePatronDialog() }
            )
        }

        // Rare Milestone Appreciation Pill (Only after 25+ slides or 5+ favorites, permanently dismissable)
        if (viewModel.isMilestoneCelebrationEligible() && isPlacardVisible && !viewModel.isAnyModalOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 28.dp, end = 28.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceGlass)
                    .border(1.dp, GoldLeafBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("milestone_patron_pill")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = ChampagneGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.R.string.milestone_banner_text),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = WarmIvory
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.openPatronDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChampagneGold,
                            contentColor = DarkObsidian
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.example.R.string.milestone_banner_support),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { viewModel.dismissMilestone() },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = androidx.compose.ui.res.stringResource(com.example.R.string.milestone_banner_dismiss),
                            tint = MutedSlate,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Gallery Grid Browser
        if (isBrowserOpen) {
            GalleryBrowserDialog(
                artworks = artworks,
                departments = viewModel.departments,
                currentDepartment = activeCategory.title,
                currentIndex = currentIndex,
                onSelectDepartment = { name ->
                    viewModel.museumCategories.find { it.title == name }?.let { cat ->
                        viewModel.selectCategory(cat)
                    }
                },
                onSelectArtwork = { viewModel.selectArtworkIndex(it) },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onClose = { viewModel.closeBrowser() }
            )
        }

        // Artwork Curatorial Detail Modal
        if (isDetailModalOpen && currentArtwork != null) {
            ArtworkDetailsModal(
                artwork = currentArtwork,
                isFavorite = viewModel.isFavorite(currentArtwork.id),
                onToggleFavorite = { viewModel.toggleFavorite(currentArtwork.id) },
                onClose = { viewModel.closeDetailModal() }
            )
        }

        // Animated Entrance Splash Screen
        if (showSplash) {
            ArtGallerySplashScreen(
                visible = showSplash,
                onDismiss = { showSplash = false }
            )
        }
    }
}
