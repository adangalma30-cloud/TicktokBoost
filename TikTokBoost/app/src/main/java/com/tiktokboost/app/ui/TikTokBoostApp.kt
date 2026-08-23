package com.tiktokboost.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.ui.screens.CoinsScreen
import com.tiktokboost.app.ui.screens.EarnScreen
import com.tiktokboost.app.ui.screens.ExchangeScreen
import com.tiktokboost.app.ui.screens.HistoryScreen
import com.tiktokboost.app.ui.screens.HomeScreen
import com.tiktokboost.app.ui.screens.LoginScreen
import com.tiktokboost.app.ui.screens.ProfileScreen
import com.tiktokboost.app.ui.screens.SettingsScreen
import com.tiktokboost.app.ui.screens.SignupScreen
import com.tiktokboost.app.ui.screens.WelcomeScreen
import com.tiktokboost.app.ui.theme.CardBg
import com.tiktokboost.app.ui.theme.TextSecondary
import com.tiktokboost.app.ui.theme.TikTokBg
import com.tiktokboost.app.ui.theme.TikTokCyan

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "main_home"
    const val EXCHANGE = "main_exchange"
    const val COINS = "main_coins"
    const val PROFILE = "main_profile"
    const val EARN = "earn"
    const val HISTORY = "history"
    const val SETTINGS = "settings"

    val bottomTabs = listOf(HOME, EXCHANGE, COINS, PROFILE)
}

private data class TabDef(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabDef(Routes.HOME, "Home", Icons.Filled.Home),
    TabDef(Routes.EXCHANGE, "Exchange", Icons.Filled.Refresh),
    TabDef(Routes.COINS, "Coins", Icons.Filled.Star),
    TabDef(Routes.PROFILE, "Profile", Icons.Filled.Person)
)

@Composable
fun TikTokBoostApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val startDest = remember {
        when {
            !Session.isOnboarded -> Routes.WELCOME
            !Session.isLoggedIn -> Routes.LOGIN
            else -> Routes.HOME
        }
    }

    Scaffold(
        containerColor = TikTokBg,
        bottomBar = {
            if (currentRoute in Routes.bottomTabs) {
                NavigationBar(containerColor = CardBg) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TikTokCyan,
                                selectedTextColor = TikTokCyan,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = CardBg
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onGetStarted = {
                        Session.isOnboarded = true
                        navController.navigate(Routes.SIGNUP) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    },
                    onLogin = {
                        Session.isOnboarded = true
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.SIGNUP) {
                SignupScreen(
                    onDone = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.SIGNUP) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onDone = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenExchange = { navController.navigate(Routes.EXCHANGE) },
                    onOpenEarn = { navController.navigate(Routes.EARN) },
                    onOpenHistory = { navController.navigate(Routes.HISTORY) }
                )
            }
            composable(Routes.EXCHANGE) {
                ExchangeScreen()
            }
            composable(Routes.COINS) {
                CoinsScreen(onEarn = { navController.navigate(Routes.EARN) })
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onSignOut = {
                        navController.navigate(Routes.WELCOME) { popUpTo(0) { inclusive = true } }
                    },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                    onHistory = { navController.navigate(Routes.HISTORY) }
                )
            }
            composable(Routes.EARN) {
                EarnScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.HISTORY) {
                HistoryScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onSignOut = {
                        navController.navigate(Routes.WELCOME) { popUpTo(0) { inclusive = true } }
                    }
                )
            }
        }
    }
}
