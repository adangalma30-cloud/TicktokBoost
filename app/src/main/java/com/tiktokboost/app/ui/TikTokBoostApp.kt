package com.tiktokboost.app.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tiktokboost.app.R
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.ui.screens.AdminScreen
import com.tiktokboost.app.ui.screens.AnalyticsScreen
import com.tiktokboost.app.ui.screens.BoostScreen
import com.tiktokboost.app.ui.screens.CoinsScreen
import com.tiktokboost.app.ui.screens.CreatorProfileScreen
import com.tiktokboost.app.ui.screens.EarnScreen
import com.tiktokboost.app.ui.screens.ExchangeScreen
import com.tiktokboost.app.ui.screens.HistoryScreen
import com.tiktokboost.app.ui.screens.HomeScreen
import com.tiktokboost.app.ui.screens.LoginScreen
import com.tiktokboost.app.ui.screens.NotificationsScreen
import com.tiktokboost.app.ui.screens.BlockedUsersScreen
import com.tiktokboost.app.ui.screens.OnboardingScreen
import com.tiktokboost.app.ui.screens.SetupScreen
import com.tiktokboost.app.ui.screens.PremiumScreen
import com.tiktokboost.app.ui.screens.ReferralScreen
import com.tiktokboost.app.ui.screens.ProfileScreen
import com.tiktokboost.app.ui.screens.SettingsScreen
import com.tiktokboost.app.ui.screens.SignupScreen
import com.tiktokboost.app.ui.screens.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "main_home"
    const val DISCOVER = "main_discover"
    const val EARN = "main_earn"
    const val BOOST = "main_boost"
    const val PROFILE = "main_profile"
    const val COINS = "main_coins"          // kept for deep-links from old entry points
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val NOTIFICATIONS = "notifications"
    const val PREMIUM = "premium"
    const val ANALYTICS = "analytics"
    const val ADMIN = "admin"
    const val REFERRAL = "referral"
    const val SETUP = "setup"
    const val BLOCKED = "blocked"
    const val CREATOR = "creator/{userId}"

    val bottomTabs = listOf(HOME, DISCOVER, EARN, BOOST, PROFILE)
}

private data class TabDef(
    val route: String,
    val label: String,
    val icon: ImageVector? = null,
    val drawable: Int? = null
)

private val tabs = listOf(
    TabDef(Routes.HOME, "Home", icon = Icons.Filled.Home),
    TabDef(Routes.DISCOVER, "Discover", icon = Icons.Filled.Search),
    TabDef(Routes.EARN, "Earn", icon = Icons.Filled.Star),
    TabDef(Routes.BOOST, "Boost", drawable = R.drawable.ic_bolt),
    TabDef(Routes.PROFILE, "Profile", icon = Icons.Filled.Person)
)

@Composable
fun TikTokBoostApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val cs = MaterialTheme.colorScheme

    Scaffold(
        containerColor = cs.background,
        bottomBar = {
            if (currentRoute in Routes.bottomTabs) {
                NavigationBar(containerColor = cs.surface, tonalElevation = 0.dp) {
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
                            icon = {
                                if (tab.drawable != null) {
                                    Icon(painterResource(tab.drawable), contentDescription = tab.label)
                                } else {
                                    Icon(tab.icon!!, contentDescription = tab.label)
                                }
                            },
                            label = { Text(tab.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = cs.primary,
                                selectedTextColor = cs.primary,
                                unselectedIconColor = cs.onSurfaceVariant,
                                unselectedTextColor = cs.onSurfaceVariant,
                                indicatorColor = cs.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(260)) + slideInVertically(tween(300)) { it / 24 } },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(260)) },
            popExitTransition = { fadeOut(tween(200)) + slideOutVertically(tween(260)) { it / 24 } }
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(onDone = {
                    navController.navigate(postSplashStart()) { popUpTo(Routes.SPLASH) { inclusive = true } }
                })
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onGetStarted = {
                        Session.isOnboarded = true
                        navController.navigate(Routes.SIGNUP) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                    },
                    onLogin = {
                        Session.isOnboarded = true
                        navController.navigate(Routes.LOGIN) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                    }
                )
            }
            composable(Routes.SIGNUP) {
                SignupScreen(
                    onDone = {
                        navController.navigate(Routes.SETUP) { popUpTo(Routes.SIGNUP) { inclusive = true } }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SETUP) {
                SetupScreen(
                    onFinish = {
                        navController.navigate(Routes.HOME) { popUpTo(Routes.SETUP) { inclusive = true } }
                    }
                )
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onDone = { navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenDiscover = { navController.navigate(Routes.DISCOVER) },
                    onOpenBoost = { navController.navigate(Routes.BOOST) },
                    onOpenEarn = { navController.navigate(Routes.EARN) },
                    onOpenHistory = { navController.navigate(Routes.HISTORY) },
                    onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) }
                )
            }
            composable(Routes.DISCOVER) {
                ExchangeScreen(
                    onOpenPremium = { navController.navigate(Routes.PREMIUM) },
                    onOpenCreator = { id -> navController.navigate("creator/$id") }
                )
            }
            composable(
                Routes.CREATOR,
                arguments = listOf(androidx.navigation.navArgument("userId") { type = androidx.navigation.NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("userId") ?: return@composable
                CreatorProfileScreen(userId = id, onBack = { navController.popBackStack() })
            }
            composable(Routes.EARN) {
                EarnScreen(
                    onBack = null, showTopBar = false,
                    onOpenDiscover = { navController.navigate(Routes.DISCOVER) },
                    onOpenProfile = { navController.navigate(Routes.PROFILE) },
                    onOpenReferral = { navController.navigate(Routes.REFERRAL) }
                )
            }
            composable(Routes.BOOST) {
                BoostScreen(
                    onOpenPremium = { navController.navigate(Routes.PREMIUM) },
                    onOpenAnalytics = { navController.navigate(Routes.ANALYTICS) }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onSignOut = { navController.navigate(Routes.ONBOARDING) { popUpTo(0) { inclusive = true } } },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                    onHistory = { navController.navigate(Routes.HISTORY) },
                    onPremium = { navController.navigate(Routes.PREMIUM) },
                    onAnalytics = { navController.navigate(Routes.ANALYTICS) }
                )
            }
            composable(Routes.COINS) {
                CoinsScreen(onEarn = { navController.navigate(Routes.EARN) })
            }
            composable(Routes.HISTORY) {
                HistoryScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.NOTIFICATIONS) {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.PREMIUM) {
                PremiumScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ANALYTICS) {
                AnalyticsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPremium = { navController.navigate(Routes.PREMIUM) }
                )
            }
            composable(Routes.ADMIN) {
                AdminScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.REFERRAL) {
                ReferralScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.BLOCKED) {
                BlockedUsersScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onSignOut = { navController.navigate(Routes.ONBOARDING) { popUpTo(0) { inclusive = true } } },
                    onAdmin = { navController.navigate(Routes.ADMIN) },
                    onPremium = { navController.navigate(Routes.PREMIUM) },
                    onBlocked = { navController.navigate(Routes.BLOCKED) }
                )
            }
        }
    }
}

private fun postSplashStart(): String = when {
    !Session.isOnboarded -> Routes.ONBOARDING
    !Session.isLoggedIn -> Routes.LOGIN
    else -> Routes.HOME
}
