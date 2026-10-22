package com.example.primecheck.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.primecheck.ui.about.AboutScreen
import com.example.primecheck.ui.history.HistoryScreen
import com.example.primecheck.ui.home.HomeScreen
import com.example.primecheck.ui.settings.SettingsScreen
import com.example.primecheck.ui.splash.SplashScreen

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Splash : Screen("splash", "Splash", Icons.Filled.Calculate, Icons.Outlined.Calculate)
    object Home : Screen("home", "Home", Icons.Filled.Calculate, Icons.Outlined.Calculate)
    object History : Screen("history", "History", Icons.Filled.History, Icons.Outlined.History)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    object About : Screen("about", "About", Icons.Filled.Info, Icons.Outlined.Info)
}

@Composable
fun PrimeCheckApp(
    viewModel: PrimeCheckViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.History,
        Screen.Settings,
        Screen.About
    )

    val showBottomBar = currentRoute != null && currentRoute != Screen.Splash.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    settings = settings
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    historyList = uiState.history,
                    settings = settings,
                    onItemClick = { historyItem ->
                        viewModel.restoreHistoryItem(historyItem)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onDeleteItem = viewModel::deleteHistoryItem,
                    onClearAll = viewModel::clearAllHistory
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    settings = settings,
                    onSettingsChange = viewModel::updateSettings,
                    onClearHistory = viewModel::clearAllHistory,
                    onResetSettings = viewModel::resetSettings
                )
            }

            composable(Screen.About.route) {
                AboutScreen()
            }
        }
    }
}
