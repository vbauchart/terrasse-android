package com.terrass.app.ui

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

@Composable
fun TerassApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "map",
        modifier = Modifier.fillMaxSize(),
    ) {
        composable("map") {
            MapScreen(
                onNavigateToAdd = { lat, lng ->
                    navController.navigate("terrace/add?lat=$lat&lng=$lng")
                },
                onNavigateToEdit = { id ->
                    navController.navigate("terrace/$id/edit")
                },
            )
        }

        composable(
            route = "terrace/add?lat={lat}&lng={lng}",
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType; defaultValue = "48.8566" },
                navArgument("lng") { type = NavType.StringType; defaultValue = "2.3522" },
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
