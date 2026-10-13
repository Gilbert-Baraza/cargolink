package com.example.cargolink.presentation.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object Profile : Screen("profile")
    object Home : Screen("home")
    object Explore : Screen("explore")
    object Activity : Screen("activity")
    object MyListings : Screen("my_listings")

    object TruckList : Screen("truck_list")
    object TruckAddEdit : Screen("truck_add_edit/{truckId}") {
        fun createRoute(truckId: String = "new") = "truck_add_edit/$truckId"
    }
    object TruckDetail : Screen("truck_detail/{truckId}") {
        fun createRoute(truckId: String) = "truck_detail/$truckId"
    }
    object CargoDetail : Screen("cargo_detail/{cargoId}") {
        fun createRoute(cargoId: String) = "cargo_detail/$cargoId"
    }
    object BookingDetail : Screen("booking_detail/{requestId}") {
        fun createRoute(requestId: String) = "booking_detail/$requestId"
    }
}
