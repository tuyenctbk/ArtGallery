package com.example

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.ArtGalleryApp
import com.example.ui.ArtGalleryViewModel
import com.example.ui.theme.ArtGalleryTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ArtGalleryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Enable immersive sticky fullscreen for authentic digital art frame on Android TV & Tablet Kiosk
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            ArtGalleryTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ArtGalleryApp(viewModel = viewModel)
                }
            }
        }
    }

    @Suppress("GestureBackNavigation")
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        viewModel.onUserActivity()

        // When a dialog or modal drawer is open, let Android TV's native focus system navigate
        // buttons, grid items, and chips with the D-Pad remote.
        if (viewModel.isAnyModalOpen) {
            return if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (viewModel.isCategorySelectionOpen.value) {
                    viewModel.closeCategorySelection()
                    true
                } else if (viewModel.isSettingsOpen.value) {
                    viewModel.closeSettings()
                    true
                } else if (viewModel.isBrowserOpen.value) {
                    viewModel.closeBrowser()
                    true
                } else if (viewModel.isDetailModalOpen.value) {
                    viewModel.closeDetailModal()
                    true
                } else {
                    super.onKeyDown(keyCode, event)
                }
            } else {
                super.onKeyDown(keyCode, event)
            }
        }

        // When in full frame slideshow view:
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_MEDIA_NEXT -> {
                viewModel.nextArtwork()
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                viewModel.prevArtwork()
                true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                viewModel.togglePlayPause()
                true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                viewModel.togglePlacard()
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_MENU -> {
                viewModel.openSettings()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
