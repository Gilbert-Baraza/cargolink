package com.example.cargolink.presentation.navigation

sealed class Screen(val route: String) {
    object Foundation : Screen("foundation")
}
