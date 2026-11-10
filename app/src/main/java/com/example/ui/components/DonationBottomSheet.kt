package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.GoldLeafBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.WarmIvory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationBottomSheet(
    isPatron: Boolean = false,
    patronTier: String = "",
    onBecomePatron: (tierTitle: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    var selectedTierId by remember { mutableStateOf("patron_tier_silver") }
    var showQrCode by remember { mutableStateOf(false) }
    var patronSuccessMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkObsidian,
        contentColor = WarmIvory,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier.testTag("donation_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SurfaceGlass)
                            .border(1.dp, ChampagneGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Donation",
                            tint = ChampagneGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "SUPPORT ARTGALLERY TV",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            color = ChampagneGold
                        )
                        Text(
                            text = "Ad-Free CC0 Museum Quality Masterpieces",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("close_donation_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Sheet",
                        tint = WarmIvory,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = DarkBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // Active Patron Recognition Card
            if (isPatron) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceGlass)
                        .border(1.dp, ChampagneGold, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                        .testTag("active_patron_badge")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Active Patron",
                            tint = ChampagneGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Active Museum Patron ($patronTier)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = ChampagneGold
                            )
                            Text(
                                text = "Thank you for supporting public domain arts! You keep ArtGallery TV 100% ad-free.",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmIvory
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Subtitle Description
            Text(
                text = "ArtGallery TV brings world-renowned CC0 masterpieces to smart TVs and desktop frames without ads or tracking. Voluntary patronage funds 4K image optimization and gallery updates.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = WarmIvory,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: In-App Purchase Tiers
            Text(
                text = "Google Play In-App Tiers",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = ChampagneGold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PATRON_TIERS.forEach { tier ->
                    val isSelected = (selectedTierId == tier.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) SurfaceGlass else Color(0x33000000))
                            .border(
                                1.dp,
                                if (isSelected) ChampagneGold else DarkBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedTierId = tier.id }
                            .padding(12.dp)
                            .testTag("donation_tier_${tier.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = tier.iconDesc,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(
                                    text = tier.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isSelected) ChampagneGold else WarmIvory
                                )
                                Text(
                                    text = tier.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MutedSlate
                                )
                            }
                        }

                        Text(
                            text = tier.price,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            color = ChampagneGold,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Google Play Purchase Button
            val selectedTierObj = PATRON_TIERS.find { it.id == selectedTierId } ?: PATRON_TIERS[1]
            Button(
                onClick = {
                    onBecomePatron(selectedTierObj.title)
                    patronSuccessMessage = "Thank you! You supported as ${selectedTierObj.title}."
                    Toast.makeText(context, "Thank you for your patronage!", Toast.LENGTH_LONG).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChampagneGold,
                    contentColor = DarkObsidian
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("donation_google_play_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Donate with Google Play (${selectedTierObj.price})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (patronSuccessMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = ChampagneGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = patronSuccessMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = ChampagneGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Divider(color = DarkBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Alternative Support Channels (PayPal & Ko-fi)
            Text(
                text = "Alternative Donation Methods",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = ChampagneGold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // PayPal Button
                OutlinedButton(
                    onClick = { showQrCode = !showQrCode },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmIvory),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("donation_paypal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "PayPal QR",
                        tint = ChampagneGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showQrCode) "Hide PayPal" else "PayPal & QR",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                // Ko-fi Button
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://ko-fi.com/artgallerytv")
                            )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Ko-fi URL", "https://ko-fi.com/artgallerytv"))
                            Toast.makeText(context, "Ko-fi link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmIvory),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("donation_kofi_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = "Ko-fi",
                        tint = ChampagneGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tip on Ko-fi",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            // Expanded PayPal Details with QR Code and Direct Copy Links
            AnimatedVisibility(visible = showQrCode) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceGlass)
                        .border(1.dp, GoldLeafBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PayPal Direct Transfer",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = ChampagneGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Scan QR or copy direct link:",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmIvory
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "PayPal QR Code",
                            tint = Color.Black,
                            modifier = Modifier.size(114.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "paypal.me/ArtGalleryTV",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = ChampagneGold
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("PayPal Link", "https://paypal.me/ArtGalleryTV"))
                                Toast.makeText(context, "PayPal link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceGlass,
                                contentColor = WarmIvory
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Link",
                                tint = ChampagneGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Copy Link",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://paypal.me/ArtGalleryTV"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChampagneGold,
                                contentColor = DarkObsidian
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = "Open PayPal",
                                tint = DarkObsidian,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Open Web",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Note
            Text(
                text = "Privacy Guarantee: 100% Offline Capable & Zero User Tracking.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MutedSlate,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
