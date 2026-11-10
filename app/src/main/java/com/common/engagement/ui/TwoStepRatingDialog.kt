package com.common.engagement.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun TwoStepRatingDialog(
    appName: String,
    onLaunchStoreReview: () -> Unit,
    onRedirectInternalFeedback: () -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) }

    if (step == 1) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "Enjoying $appName?") },
            text = { Text(text = "Are you finding the app helpful so far?") },
            confirmButton = {
                TextButton(onClick = {
                    step = 2
                    onLaunchStoreReview()
                }) {
                    Text("Yes, loving it! 👍")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onRedirectInternalFeedback()
                    onDismiss()
                }) {
                    Text("Needs work 💬")
                }
            }
        )
    }
}
