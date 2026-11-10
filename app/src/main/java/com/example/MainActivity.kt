package com.example

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.common.shake.UniversalShakeDetector
import com.example.ui.ArtGalleryApp
import com.example.ui.ArtGalleryViewModel
import com.example.ui.theme.ArtGalleryTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ArtGalleryViewModel by viewModels()
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private val shakeDetector = UniversalShakeDetector {
        if (!viewModel.isAnyModalOpen && viewModel.config.value.shakeToNext) {
            viewModel.shuffleArtwork()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.config.collect { cfg ->
                    shakeDetector.isEnabled = cfg.shakeToNext
                    shakeDetector.thresholdGForce = cfg.shakeThresholdG
                }
            }
        }

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

    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensor ->
            sensorManager?.registerListener(shakeDetector, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(shakeDetector)
    }

    @Suppress("GestureBackNavigation")
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode != KeyEvent.KEYCODE_DPAD_UP) {
            viewModel.onUserActivity()
        }

        // When a dialog or modal drawer is open, let Android TV's native focus system navigate
        // buttons, grid items, and chips with the D-Pad remote.
        if (viewModel.isAnyModalOpen) {
            return if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
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
                } else if (viewModel.isPatronDialogOpen.value) {
                    viewModel.closePatronDialog()
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
            KeyEvent.KEYCODE_INFO, KeyEvent.KEYCODE_I -> {
                viewModel.openDetailModal()
                true
            }
            KeyEvent.KEYCODE_C -> {
                viewModel.openCategorySelection()
                true
            }
            KeyEvent.KEYCODE_G, KeyEvent.KEYCODE_BOOKMARK -> {
                viewModel.openBrowser()
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_S -> {
                viewModel.openSettings()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
