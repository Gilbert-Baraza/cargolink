package com.example.cargolink.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cargolink.presentation.screens.FoundationScreen

@Composable
fun CargoLinkNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Foundation.route
    ) {
        composable(Screen.Foundation.route) {
            FoundationScreen()
        }
    }
}
