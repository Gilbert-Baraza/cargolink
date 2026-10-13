package com.example.cargolink.presentation.navigation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import androidx.navigation.compose.rememberNavController
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
    val truckViewModel: TruckViewModel = viewModel()

    when (authState) {
        is AuthState.Initial -> {
            LoadingScreen("Checking session...")
        }
        is AuthState.Authenticated -> {
            AuthenticatedNavGraph(authViewModel, truckViewModel)
        }
        else -> {
            UnauthenticatedNavGraph(authViewModel)
        }
    }
}

@Composable
fun LoadingScreen(message: String = "Preparing CargoLink…") {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun AuthenticatedNavGraph(
    authViewModel: AuthViewModel,
    truckViewModel: TruckViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val profileViewModel: com.example.cargolink.presentation.viewmodel.ProfileViewModel = viewModel()
    val cargoViewModel: com.example.cargolink.presentation.viewmodel.CargoViewModel = viewModel()
    val requestViewModel: com.example.cargolink.presentation.viewmodel.TransportRequestViewModel = viewModel()
    val profileState by profileViewModel.profileState.collectAsState()

    val bottomBarScreens = listOf(
        Screen.Home.route,
        Screen.Explore.route,
        Screen.TruckList.route,
        Screen.MyListings.route,
        Screen.Activity.route,
        Screen.Profile.route
    )

    val showBottomBar = currentDestination?.route in bottomBarScreens

    val user = (profileState as? com.example.cargolink.presentation.viewmodel.ProfileState.Success)?.user

    fun navigateToTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CargoLinkBottomBar(navController, currentDestination, profileState)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    userName = user?.name?.substringBefore(" ") ?: "User",
                    participationType = user?.participationType,
                    onFindTruck = { navigateToTopLevel(Screen.Explore.route) },
                    onPostCargo = {
                        if (user?.participationType == "CARGO_OWNER") {
                            navigateToTopLevel(Screen.MyListings.route)
                        } else {
                            navController.navigate(Screen.TruckAddEdit.createRoute("new"))
                        }
                    },
                    onOfferCapacity = { navController.navigate(Screen.TruckAddEdit.createRoute("new")) },
                    onViewMyListings = {
                        if (user?.participationType == "CARGO_OWNER") {
                            navigateToTopLevel(Screen.MyListings.route)
                        } else {
                            navigateToTopLevel(Screen.TruckList.route)
                        }
                    },
                    onNavigateToDetails = { truckId ->
                        navController.navigate(Screen.TruckDetail.createRoute(truckId))
                    },
                    onNavigateToProfile = { navigateToTopLevel(Screen.Profile.route) },
                    onNavigateToExplore = { navigateToTopLevel(Screen.Explore.route) }
                )
            }
            composable(Screen.Explore.route) {
                ExploreScreen(
                    participationType = user?.participationType,
                    onNavigateToDetails = { id ->
                        if (user?.participationType == "TRUCK_DRIVER" || user?.participationType == "TRUCK_OPERATOR") {
                            navController.navigate(Screen.CargoDetail.createRoute(id))
                        } else {
                            navController.navigate(Screen.TruckDetail.createRoute(id))
                        }
                    },
                    onNavigateToProfile = { navigateToTopLevel(Screen.Profile.route) },
                    viewModel = truckViewModel,
                    cargoViewModel = cargoViewModel
                )
            }
            composable(Screen.Activity.route) {
                ActivityScreen(
                    participationType = user?.participationType,
                    onNavigateToDetails = { id ->
                        navController.navigate(Screen.BookingDetail.createRoute(id))
                    },
                    viewModel = requestViewModel
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        authViewModel.logout()
                    },
                    onNavigateToTrucks = {
                        if (user?.participationType == "CARGO_OWNER") {
                            navigateToTopLevel(Screen.MyListings.route)
                        } else {
                            navigateToTopLevel(Screen.TruckList.route)
                        }
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
            composable(Screen.MyListings.route) {
                EmptyShipmentsScreen(onNavigateBack = { navController.popBackStack() })
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
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate(Screen.TruckAddEdit.createRoute(id)) },
                    viewModel = truckViewModel,
                    cargoViewModel = cargoViewModel,
                    requestViewModel = requestViewModel
                )
            }
            composable(
                route = Screen.CargoDetail.route,
                arguments = listOf(navArgument("cargoId") { type = NavType.StringType })
            ) { backStackEntry ->
                val cargoId = backStackEntry.arguments?.getString("cargoId") ?: ""
                CargoDetailScreen(
                    cargoId = cargoId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate(Screen.TruckAddEdit.createRoute(id)) },
                    viewModel = cargoViewModel,
                    requestViewModel = requestViewModel,
                    truckViewModel = truckViewModel,
                    profileViewModel = profileViewModel
                )
            }
            composable(
                route = Screen.BookingDetail.route,
                arguments = listOf(navArgument("requestId") { type = NavType.StringType })
            ) { backStackEntry ->
                val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
                BookingDetailScreen(
                    requestId = requestId,
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = requestViewModel,
                    cargoViewModel = cargoViewModel,
                    truckViewModel = truckViewModel
                )
            }
        }
    }
}

@Composable
fun UnauthenticatedNavGraph(
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    val startDest = if (authState is AuthState.ProfileIncomplete) {
        Screen.Register.route
    } else {
        Screen.Welcome.route
    }

    NavHost(
        navController = navController,
        startDestination = startDest
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
                    // AuthState change handles navigation automatically
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Register.route) {
            RegistrationScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    authViewModel.resetState()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Welcome.route)
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun CargoLinkBottomBar(
    navController: NavHostController, 
    currentDestination: NavDestination?,
    profileState: com.example.cargolink.presentation.viewmodel.ProfileState
) {
    val participationType = (profileState as? com.example.cargolink.presentation.viewmodel.ProfileState.Success)?.user?.participationType
    val isDriver = participationType == "TRUCK_DRIVER" || participationType == "TRUCK_OPERATOR"

    val items = if (isDriver) {
        listOf(
            BottomNavItem("Home", Screen.Home.route, Icons.Default.Home),
            BottomNavItem("Find Cargo", Screen.Explore.route, Icons.Default.Search),
            BottomNavItem("My Trucks", Screen.TruckList.route, Icons.Default.LocalShipping),
            BottomNavItem("Activity", Screen.Activity.route, Icons.Default.Notifications),
            BottomNavItem("Profile", Screen.Profile.route, Icons.Default.AccountCircle)
        )
    } else {
        listOf(
            BottomNavItem("Home", Screen.Home.route, Icons.Default.Home),
            BottomNavItem("Find Trucks", Screen.Explore.route, Icons.Default.Search),
            BottomNavItem("My Shipments", Screen.MyListings.route, Icons.Default.Inventory),
            BottomNavItem("Activity", Screen.Activity.route, Icons.Default.Notifications),
            BottomNavItem("Profile", Screen.Profile.route, Icons.Default.AccountCircle)
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                Surface(
                    onClick = {
                        if (!selected) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier.animateContentSize()
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = if (selected) 16.dp else 12.dp,
                            vertical = 10.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        if (selected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

data class BottomNavItem(val label: String, val route: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyShipmentsScreen(onNavigateBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Shipments", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No shipments found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Post your first cargo requirement to start receiving offers from verified transporters.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Post First Shipment")
            }
        }
    }
}
