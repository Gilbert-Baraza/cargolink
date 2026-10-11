package com.example.cargolink.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.cargolink.presentation.screens.*
import com.example.cargolink.presentation.viewmodel.AuthState
import com.example.cargolink.presentation.viewmodel.AuthViewModel
import com.example.cargolink.presentation.viewmodel.TruckViewModel

@Composable
fun CargoLinkNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val truckViewModel: TruckViewModel = viewModel()

    val startDestination = when (authState) {
        is AuthState.Authenticated -> Screen.Home.route
        else -> Screen.Welcome.route
    }

    val bottomBarScreens = listOf(
        Screen.Home.route,
        Screen.Explore.route,
        Screen.TruckList.route,
        Screen.Activity.route,
        Screen.Profile.route
    )

    val showBottomBar = currentDestination?.route in bottomBarScreens

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CargoLinkBottomBar(navController, currentDestination)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Register.route) {
                RegistrationScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onFindTruck = { navController.navigate(Screen.Explore.route) },
                    onPostCargo = { navController.navigate(Screen.TruckAddEdit.createRoute("new")) },
                    onOfferCapacity = { navController.navigate(Screen.TruckAddEdit.createRoute("new")) },
                    onViewMyListings = { navController.navigate(Screen.TruckList.route) },
                    onNavigateToDetails = { truckId ->
                        navController.navigate(Screen.TruckDetail.createRoute(truckId))
                    },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToExplore = { navController.navigate(Screen.Explore.route) }
                )
            }
            composable(Screen.Explore.route) {
                ExploreScreen(
                    onNavigateToDetails = { truckId ->
                        navController.navigate(Screen.TruckDetail.createRoute(truckId))
                    },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                )
            }
            composable(Screen.Activity.route) {
                ActivityScreen(
                    onNavigateToDetails = { id ->
                        navController.navigate(Screen.TruckDetail.createRoute("TRK-49122"))
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.Profile.route) { inclusive = true }
                        }
                    },
                    onNavigateToTrucks = {
                        navController.navigate(Screen.TruckList.route)
                    }
                )
            }
            composable(Screen.TruckList.route) {
                TruckListScreen(
                    onNavigateToAdd = {
                        navController.navigate(Screen.TruckAddEdit.createRoute("new"))
                    },
                    onNavigateToDetail = { truckId ->
                        navController.navigate(Screen.TruckDetail.createRoute(truckId))
                    },
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = truckViewModel
                )
            }
            composable(
                route = Screen.TruckAddEdit.route,
                arguments = listOf(navArgument("truckId") { type = NavType.StringType })
            ) { backStackEntry ->
                val truckId = backStackEntry.arguments?.getString("truckId")
                TruckAddEditScreen(
                    truckId = if (truckId == "new") null else truckId,
                    onSaveSuccess = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = truckViewModel
                )
            }
            composable(
                route = Screen.TruckDetail.route,
                arguments = listOf(navArgument("truckId") { type = NavType.StringType })
            ) { backStackEntry ->
                val truckId = backStackEntry.arguments?.getString("truckId") ?: ""
                TruckDetailScreen(
                    truckId = truckId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.CargoDetail.route,
                arguments = listOf(navArgument("cargoId") { type = NavType.StringType })
            ) { backStackEntry ->
                val cargoId = backStackEntry.arguments?.getString("cargoId") ?: ""
                CargoDetailScreen(
                    cargoId = cargoId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun CargoLinkBottomBar(navController: NavHostController, currentDestination: NavDestination?) {
    val items = listOf(
        BottomNavItem("Home", Screen.Home.route, Icons.Default.Home),
        BottomNavItem("Explore", Screen.Explore.route, Icons.Default.Explore),
        BottomNavItem("My Listings", Screen.TruckList.route, Icons.Default.Inventory),
        BottomNavItem("Activity", Screen.Activity.route, Icons.Default.Notifications),
        BottomNavItem("Profile", Screen.Profile.route, Icons.Default.AccountCircle)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                icon = {
                    if (item.route == Screen.Activity.route) {
                        BadgedBox(
                            badge = { 
                                Badge(
                                    containerColor = Color.Red,
                                    modifier = Modifier.size(6.dp)
                                ) 
                            }
                        ) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

data class BottomNavItem(val label: String, val route: String, val icon: ImageVector)
