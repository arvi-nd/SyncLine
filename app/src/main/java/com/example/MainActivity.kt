package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.navigation.GlobalChatNavGraph
import com.example.ui.theme.GlobalChatTheme
import com.example.viewmodel.GlobalChatViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GlobalChatViewModel by viewModels()

    private val notificationPermission =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { }

    /** Asks once, after sign-in, so meeting reminders can be shown. */
    private fun askForNotificationsOnce() {
        if (Build.VERSION.SDK_INT < 33 || !viewModel.isUserAuthenticated) return
        val prefs = getSharedPreferences("syncline_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("asked_notifications", false)) return
        prefs.edit().putBoolean("asked_notifications", true).apply()
        if (!com.example.reminders.MeetingReminders.canNotify(this)) {
            notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        unlockHighRefreshRate()
        enableEdgeToEdge()
        setContent {
            val isDark by viewModel.isDarkTheme.collectAsState()

            GlobalChatTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GlobalChatNavGraph(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        unlockHighRefreshRate()
        askForNotificationsOnce()
    }

    private fun unlockHighRefreshRate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = window.context.display
                val supportedModes = display?.supportedModes ?: emptyArray()
                val maxMode = supportedModes.maxByOrNull { it.refreshRate }
                val params = window.attributes
                if (maxMode != null && maxMode.modeId != 0) {
                    params.preferredDisplayModeId = maxMode.modeId
                }
                if (maxMode != null && maxMode.refreshRate > 0f) {
                    params.preferredRefreshRate = maxMode.refreshRate
                } else {
                    params.preferredRefreshRate = 120f
                }
                window.attributes = params
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                @Suppress("DEPRECATION")
                val display = window.windowManager.defaultDisplay
                @Suppress("DEPRECATION")
                val supportedModes = display?.supportedModes ?: emptyArray()
                val maxMode = supportedModes.maxByOrNull { it.refreshRate }
                val params = window.attributes
                if (maxMode != null && maxMode.modeId != 0) {
                    params.preferredDisplayModeId = maxMode.modeId
                }
                if (maxMode != null && maxMode.refreshRate > 0f) {
                    @Suppress("DEPRECATION")
                    params.preferredRefreshRate = maxMode.refreshRate
                } else {
                    @Suppress("DEPRECATION")
                    params.preferredRefreshRate = 120f
                }
                window.attributes = params
            }
        } catch (_: Exception) {
            // Fallback gracefully
        }
    }
}
