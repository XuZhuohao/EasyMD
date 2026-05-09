package com.easymd.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.easymd.R
import com.easymd.core.model.AppSettings
import com.easymd.core.model.EditorTab
import com.easymd.ui.filelist.FileListScreen
import com.easymd.ui.editor.EditorScreen
import com.easymd.ui.preview.PreviewScreen
import com.easymd.ui.settings.SettingsScreen
import com.easymd.ui.onboarding.OnboardingScreen
import java.io.File

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Files      : Screen("files")
    object Editor     : Screen("editor")
    object Preview    : Screen("preview")
    object Settings   : Screen("settings")
}

@Composable
fun AppNavigation(settings: AppSettings = AppSettings()) {
    val navController = rememberNavController()

    // Shared open-document state (lifted to navigation level)
    var openFile by remember { mutableStateOf<File?>(null) }

    val startDest = if (settings.hasCompletedOnboarding) Screen.Files.route else Screen.Onboarding.route

    Scaffold(
        bottomBar = {
            val currentEntry by navController.currentBackStackEntryAsState()
            val currentDest = currentEntry?.destination
            // Hide bottom nav on onboarding
            val showNav = currentDest?.route != Screen.Onboarding.route

            AnimatedVisibility(visible = showNav, enter = fadeIn(), exit = fadeOut()) {
                NavigationBar {
                    val tabs = listOf(
                        Triple(Screen.Files,    Icons.Outlined.Folder,        R.string.nav_files),
                        Triple(Screen.Editor,   Icons.Outlined.Edit,          R.string.nav_editor),
                        Triple(Screen.Preview,  Icons.Outlined.Visibility,    R.string.nav_preview),
                        Triple(Screen.Settings, Icons.Outlined.Settings,      R.string.nav_settings),
                    )
                    tabs.forEach { (screen, icon, labelRes) ->
                        NavigationBarItem(
                            selected = currentDest?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(stringResource(labelRes)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController    = navController,
            startDestination = startDest,
            modifier         = Modifier.padding(padding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(onComplete = {
                    navController.navigate(Screen.Files.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Files.route) {
                FileListScreen(
                    onOpenFile = { file ->
                        openFile = file
                        navController.navigate(Screen.Editor.route)
                    }
                )
            }
            composable(Screen.Editor.route) {
                EditorScreen(
                    file = openFile,
                    onPreview = { navController.navigate(Screen.Preview.route) }
                )
            }
            composable(Screen.Preview.route) {
                PreviewScreen(file = openFile)
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
