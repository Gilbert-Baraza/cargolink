package com.example.cargolink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.cargolink.presentation.navigation.CargoLinkNavGraph
import com.example.cargolink.presentation.viewmodel.AuthState
import com.example.cargolink.presentation.viewmodel.AuthViewModel
import com.example.cargolink.ui.theme.CargoLinkTheme

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        splashScreen.setKeepOnScreenCondition {
            authViewModel.authState.value is AuthState.Initial
        }

        enableEdgeToEdge()
        setContent {
            CargoLinkTheme {
                val navController = rememberNavController()
                CargoLinkNavGraph(navController = navController, authViewModel = authViewModel)
            }
        }
    }
}
