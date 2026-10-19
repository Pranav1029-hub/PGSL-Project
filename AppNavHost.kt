package com.gadvasu.livestock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gadvasu.livestock.AppContainer
import com.gadvasu.livestock.core.Enterprise
import com.gadvasu.livestock.core.Kind
import com.gadvasu.livestock.ui.batch.BatchCloseScreen
import com.gadvasu.livestock.ui.batch.BatchDetailScreen
import com.gadvasu.livestock.ui.batch.BatchListScreen
import com.gadvasu.livestock.ui.batch.NewBatchScreen
import com.gadvasu.livestock.ui.daily.DailyScreen
import com.gadvasu.livestock.ui.dashboard.DashboardScreen
import com.gadvasu.livestock.ui.home.HomeScreen
import com.gadvasu.livestock.ui.home.PoultryScreen
import com.gadvasu.livestock.ui.language.LanguageScreen
import com.gadvasu.livestock.ui.login.LoginScreen
import com.gadvasu.livestock.ui.reports.ReportsScreen
import com.gadvasu.livestock.ui.settings.SettingsScreen

object Routes {
    const val LANGUAGE = "language"
    const val LOGIN = "login"
    const val HOME = "home"
    const val POULTRY = "poultry"
    const val DASHBOARD = "dashboard"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"

    const val DAILY = "daily/{type}"
    const val BATCHES = "batches/{type}"
    const val BATCH_NEW = "batchNew/{type}"
    const val BATCH = "batch/{id}"
    const val BATCH_CLOSE = "batchClose/{id}"

    fun daily(e: Enterprise) = "daily/${e.key}"
    fun batches(e: Enterprise) = "batches/${e.key}"
    fun batchNew(e: Enterprise) = "batchNew/${e.key}"
    fun batch(id: String) = "batch/$id"
    fun batchClose(id: String) = "batchClose/$id"

    /** Home tile → daily form or batch list depending on the enterprise model. */
    fun forEnterprise(e: Enterprise) = if (e.kind == Kind.DAILY) daily(e) else batches(e)
}

/** Bottom-tab navigation: keep Home as the root of the stack. */
fun NavController.navigateTab(route: String) {
    if (route == Routes.HOME) {
        if (!popBackStack(Routes.HOME, inclusive = false)) navigate(Routes.HOME)
    } else {
        navigate(route) {
            popUpTo(Routes.HOME) { inclusive = false }
            launchSingleTop = true
        }
    }
}

/** After login / logout: throw the whole stack away and start fresh. */
fun NavHostController.restartAt(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
fun AppNavHost(startDestination: String, container: AppContainer) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = startDestination) {
        composable(Routes.LANGUAGE) { LanguageScreen(nav, container) }
        composable(Routes.LOGIN) { LoginScreen(nav, container) }
        composable(Routes.HOME) { HomeScreen(nav, container) }
        composable(Routes.POULTRY) { PoultryScreen(nav) }
        composable(Routes.DASHBOARD) { DashboardScreen(nav, container) }
        composable(Routes.REPORTS) { ReportsScreen(nav, container) }
        composable(Routes.SETTINGS) { SettingsScreen(nav, container) }

        composable(Routes.DAILY, arguments = listOf(navArgument("type") { type = NavType.StringType })) { entry ->
            val e = Enterprise.fromKey(entry.arguments?.getString("type"))
            if (e != null && e.kind == Kind.DAILY) DailyScreen(nav, container, e) else HomeScreen(nav, container)
        }
        composable(Routes.BATCHES, arguments = listOf(navArgument("type") { type = NavType.StringType })) { entry ->
            val e = Enterprise.fromKey(entry.arguments?.getString("type"))
            if (e != null && e.kind == Kind.BATCH) BatchListScreen(nav, container, e) else HomeScreen(nav, container)
        }
        composable(Routes.BATCH_NEW, arguments = listOf(navArgument("type") { type = NavType.StringType })) { entry ->
            val e = Enterprise.fromKey(entry.arguments?.getString("type"))
            if (e != null && e.kind == Kind.BATCH) NewBatchScreen(nav, container, e) else HomeScreen(nav, container)
        }
        composable(Routes.BATCH, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
            BatchDetailScreen(nav, container, entry.arguments?.getString("id") ?: "")
        }
        composable(Routes.BATCH_CLOSE, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
            BatchCloseScreen(nav, container, entry.arguments?.getString("id") ?: "")
        }
    }
}
