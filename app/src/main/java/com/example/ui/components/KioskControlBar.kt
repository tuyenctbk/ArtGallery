package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Dock
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SoundscapeType
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.GoldLeafBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.WarmIvory

@Composable
fun KioskControlBar(
    isVisible: Boolean,
    isPlaying: Boolean,
    currentIndex: Int,
    totalCount: Int,
    isFavorite: Boolean,
    currentDepartment: String,
    departments: List<String>,
    currentSoundscape: SoundscapeType,
    isDeskStandMode: Boolean,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenCategorySelection: () -> Unit,
    onSelectSoundscape: (SoundscapeType) -> Unit,
    onToggleDeskStand: () -> Unit,
    onOpenBrowser: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSoundMenuExpanded by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceGlass)
                .border(1.5.dp, GoldLeafBorder, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("kiosk_control_bar"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Museum Brand Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ChampagneGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MET",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = DarkObsidian
                        )
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ARTGALLERY TV",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = WarmIvory
                    )
                    Text(
                        text = "Met Museum • CC0 4K",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                        color = MutedSlate
                    )
                }
            }

            // Center: Slideshow Playback Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onPrev,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("control_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Artwork",
                        tint = WarmIvory,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ChampagneGold)
                        .testTag("control_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause Slideshow" else "Play Slideshow",
                        tint = DarkObsidian,
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("control_next_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Artwork",
                        tint = WarmIvory,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkObsidian.copy(alpha = 0.6f))
                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (totalCount > 0) "${currentIndex + 1} / $totalCount" else "0 / 0",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Serif,
                            fontSize = 13.sp
                        ),
                        color = ChampagneGold
                    )
                }
            }

            // Right: Gallery Filters, Soundscapes & Tools
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Dedicated Department & Art Movement Selection Screen Button
                TextButton(
                    onClick = onOpenCategorySelection,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkObsidian.copy(alpha = 0.4f))
                        .border(1.dp, GoldLeafBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .testTag("control_department_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Departments & Movements",
                        tint = ChampagneGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentDepartment,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = ChampagneGold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Soundscape Ambient Sound Dropdown
                Box {
                    IconButton(
                        onClick = { isSoundMenuExpanded = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("control_soundscape_button")
                    ) {
                        Icon(
                            imageVector = if (currentSoundscape != SoundscapeType.MUTE) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                            contentDescription = "Ambient Soundscapes",
                            tint = if (currentSoundscape != SoundscapeType.MUTE) ChampagneGold else MutedSlate,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = isSoundMenuExpanded,
                        onDismissRequest = { isSoundMenuExpanded = false },
                        modifier = Modifier
                            .background(SurfaceGlass)
                            .border(1.dp, GoldLeafBorder, RoundedCornerShape(12.dp))
                    ) {
                        SoundscapeType.values().forEach { sound ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = sound.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (sound == currentSoundscape) ChampagneGold else WarmIvory
                                        )
                                        Text(
                                            text = sound.description,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                            color = MutedSlate
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectSoundscape(sound)
                                    isSoundMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Desk Stand / Dock Toggle
                IconButton(
                    onClick = onToggleDeskStand,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("control_desk_stand_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Dock,
                        contentDescription = "Desk Stand Nightstand Mode",
                        tint = if (isDeskStandMode) ChampagneGold else MutedSlate,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Gallery Grid Browser
                IconButton(
                    onClick = onOpenBrowser,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("control_browser_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = "Browse Gallery",
                        tint = WarmIvory,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Settings Drawer Toggle
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("control_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = WarmIvory,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
