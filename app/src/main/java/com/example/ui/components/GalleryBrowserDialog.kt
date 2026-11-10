package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Artwork
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.GoldLeafBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.WarmIvory

@Composable
fun GalleryBrowserDialog(
    artworks: List<Artwork>,
    departments: List<String>,
    currentDepartment: String,
    currentIndex: Int,
    onSelectDepartment: (String) -> Unit,
    onSelectArtwork: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(artworks, searchQuery) {
        if (searchQuery.isBlank()) {
            artworks
        } else {
            artworks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artistDisplayName.contains(searchQuery, ignoreCase = true) ||
                        it.medium.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkObsidian)
                .testTag("gallery_browser_dialog"),
            color = DarkObsidian
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "METROPOLITAN MUSEUM CATALOG",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            color = ChampagneGold
                        )
                        Text(
                            text = "Browse CC0 Public Domain Masterpieces (${filteredList.size} Artworks)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSlate
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_browser_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Browser",
                            tint = WarmIvory,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search & Filter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search artwork title or artist...", color = MutedSlate) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = ChampagneGold) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gallery_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChampagneGold,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = WarmIvory,
                            unfocusedTextColor = WarmIvory,
                            focusedContainerColor = SurfaceGlass,
                            unfocusedContainerColor = SurfaceGlass
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Department Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(departments) { dept ->
                        val isSelected = dept == currentDepartment
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) ChampagneGold else SurfaceGlass)
                                .border(1.dp, if (isSelected) ChampagneGold else DarkBorder, RoundedCornerShape(20.dp))
                                .clickable { onSelectDepartment(dept) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = dept,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) DarkObsidian else WarmIvory
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Grid of Artworks
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 220.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(filteredList) { index, artwork ->
                        val actualIndex = artworks.indexOfFirst { it.id == artwork.id }
                        val isCurrent = actualIndex == currentIndex
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceGlass),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) ChampagneGold else DarkBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onSelectArtwork(if (actualIndex >= 0) actualIndex else index)
                                    onClose()
                                }
                                .testTag("artwork_card_${artwork.id}")
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .background(DarkObsidian),
                                    contentAlignment = Alignment.TopEnd
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(artwork.thumbnailUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = artwork.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    IconButton(
                                        onClick = { onToggleFavorite(artwork.id) },
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (artwork.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (artwork.isFavorite) Color(0xFFE53935) else WarmIvory
                                        )
                                    }
                                }

                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = artwork.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = WarmIvory,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = artwork.artistDisplayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                        color = ChampagneGold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${artwork.objectDate} • ${artwork.medium}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                        color = MutedSlate,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
