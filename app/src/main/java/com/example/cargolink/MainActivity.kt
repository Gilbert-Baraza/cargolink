package com.example.cargolink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.cargolink.presentation.navigation.CargoLinkNavGraph
import com.example.cargolink.ui.theme.CargoLinkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CargoLinkTheme {
                val navController = rememberNavController()
                CargoLinkNavGraph(navController = navController)
            }
        }
    }
}
