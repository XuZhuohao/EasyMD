package com.easymd

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.easymd.core.data.SettingsRepository
import com.easymd.core.model.AppSettings
import com.easymd.ui.navigation.AppNavigation
import com.easymd.ui.theme.EasyMDTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepo: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val intentFile = resolveIntentFile(intent)

        val settingsFlow = settingsRepo.settings.stateIn(
            lifecycleScope, SharingStarted.Eagerly, AppSettings()
        )

        setContent {
            val settings by settingsFlow.collectAsState()

            EasyMDTheme(appTheme = settings.theme) {
                AppNavigation(
                    settings = settings,
                    initialFile = intentFile
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        recreate()
    }

    private fun resolveIntentFile(intent: Intent?): File? {
        if (intent?.action != Intent.ACTION_VIEW) return null
        val uri = intent.data ?: return null
        return try {
            val rawName = uri.lastPathSegment?.substringAfterLast('/') ?: "shared"
            val safeName = if (rawName.endsWith(".md", ignoreCase = true)) rawName else "$rawName.md"
            val dest = File(cacheDir, safeName)
            contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { out -> input.copyTo(out) }
            }
            dest
        } catch (e: Exception) {
            null
        }
    }
}
