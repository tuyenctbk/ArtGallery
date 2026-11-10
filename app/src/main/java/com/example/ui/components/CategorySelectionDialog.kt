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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.CategoryType
import com.example.data.model.MuseumCategory
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.GoldLeafBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.WarmIvory

@Composable
fun CategorySelectionDialog(
    categories: List<MuseumCategory>,
    activeCategory: MuseumCategory,
    onSelectCategory: (MuseumCategory) -> Unit,
    onClose: () -> Unit
) {
    var selectedFilterType by remember { mutableStateOf<CategoryType?>(null) }

    val filterOptions = listOf(
        null to "All Categories",
        CategoryType.MOVEMENT to "Art Movements",
        CategoryType.DEPARTMENT to "Met Departments",
        CategoryType.FAVORITES to "Private Gallery"
    )

    val filteredCategories = remember(categories, selectedFilterType) {
        if (selectedFilterType == null) {
            categories
        } else {
            categories.filter { it.categoryType == selectedFilterType }
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
                .testTag("category_selection_dialog"),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = ChampagneGold,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "DEPARTMENTS & ART MOVEMENTS",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = ChampagneGold
                            )
                            Text(
                                text = "Filter Slideshow using Met Museum API Collections",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSlate
                            )
                        }
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_category_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Selection Screen",
                            tint = WarmIvory,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filter Tabs Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterOptions) { (type, label) ->
                        val isSelected = selectedFilterType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) ChampagneGold else SurfaceGlass)
                                .border(
                                    1.dp,
                                    if (isSelected) ChampagneGold else DarkBorder,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedFilterType = type }
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) DarkObsidian else WarmIvory
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Grid of Categories (Movements & Departments)
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 280.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredCategories) { category ->
                        val isActive = activeCategory.id == category.id
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceGlass),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .border(
                                    width = if (isActive) 2.dp else 1.dp,
                                    color = if (isActive) ChampagneGold else GoldLeafBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    onSelectCategory(category)
                                    onClose()
                                }
                                .testTag("category_card_${category.id}")
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .background(DarkObsidian),
                                    contentAlignment = Alignment.TopEnd
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(category.thumbnailUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = category.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (isActive) {
                                        Box(
                                            modifier = Modifier
                                                .padding(10.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(ChampagneGold)
                                                .padding(horizontal = 10.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = DarkObsidian,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "ACTIVE",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = DarkObsidian
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = when (category.categoryType) {
                                                CategoryType.MOVEMENT -> "ART MOVEMENT"
                                                CategoryType.DEPARTMENT -> "MET DEPARTMENT"
                                                CategoryType.FAVORITES -> "PRIVATE GALLERY"
                                                CategoryType.ALL -> "FULL COLLECTION"
                                            },
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            ),
                                            color = ChampagneGold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = category.title,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = 18.sp,
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = WarmIvory
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = category.description,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                        color = MutedSlate,
                                        maxLines = 2,
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
