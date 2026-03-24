package com.terrass.app.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.terrass.app.ui.screens.addterrace.AddEditTerraceScreen
import com.terrass.app.ui.screens.map.MapScreen
import com.terrass.app.ui.screens.status.StatusScreen

@Composable
fun TerassApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "map",
        modifier = Modifier.fillMaxSize(),
        enterTransition = { slideInHorizontally { it } + fadeIn() },
        exitTransition = { slideOutHorizontally { -it / 3 } + fadeOut() },
        popEnterTransition = { slideInHorizontally { -it / 3 } + fadeIn() },
        popExitTransition = { slideOutHorizontally { it } + fadeOut() },
    ) {
        composable("map") {
            MapScreen(
                onNavigateToAdd = { lat, lng, zoom ->
                    navController.navigate("terrace/add?lat=$lat&lng=$lng&zoom=$zoom")
                },
                onNavigateToEdit = { id ->
                    navController.navigate("terrace/$id/edit")
                },
                onNavigateToStatus = {
                    navController.navigate("status")
                },
            )
        }

        composable("status") {
            StatusScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = "terrace/add?lat={lat}&lng={lng}&zoom={zoom}",
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType; defaultValue = "48.8566" },
                navArgument("lng") { type = NavType.StringType; defaultValue = "2.3522" },
                navArgument("zoom") { type = NavType.StringType; defaultValue = "12.0" },
            ),
        ) {
            AddEditTerraceScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = "terrace/{id}/edit",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
            ),
        ) {
            AddEditTerraceScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
