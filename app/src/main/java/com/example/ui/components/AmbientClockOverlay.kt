package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.GoldLeafBorder
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.WarmIvory
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AmbientClockOverlay(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)
            delay(1000L)
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .testTag("ambient_clock_overlay")
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceGlass)
                .border(1.dp, GoldLeafBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Text(
                text = currentTime,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                ),
                color = ChampagneGold
            )
            Text(
                text = currentDate,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = WarmIvory
            )
        }
    }
}
