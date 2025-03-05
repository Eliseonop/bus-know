package com.tcontur.qrbus.core

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tcontur.qrbus.core.login.LoginScreen
import com.tcontur.qrbus.core.login.SessionManager
import com.tcontur.qrbus.core.map.OsmdroidMapView
import com.tcontur.qrbus.core.models.AppRoute

@Composable
fun NavigationWrapper(context: Context) {

    val navController = rememberNavController()
    val sessionManager = remember { SessionManager(context) }
    val startDestination =
        if (sessionManager.isLogged() != null) AppRoute.MapRoute else AppRoute.LoginRoute
    NavHost(navController = navController, startDestination = startDestination) {
        composable<AppRoute.LoginRoute> {
            LoginScreen(
                navigateToHome = {
                    navController.navigate(AppRoute.MapRoute) {
                        popUpTo(AppRoute.LoginRoute) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                context = context
            )

        }
        composable<AppRoute.MapRoute> {
            OsmdroidMapView(navController)
        }

    }
}

@Preview
@Composable
fun NavigationWrapperPreview() {
    NavigationWrapper(context = LocalContext.current)
}