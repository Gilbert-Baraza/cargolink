package com.example.cargolink.core.constants

object AppConstants {
    // 127.0.0.1:8000 works on physical devices IF you run 'adb reverse tcp:8000 tcp:8000'
    // Using 127.0.0.1 explicitly avoids potential IPv6 resolution issues with 'localhost'
    const val BASE_URL = "http://127.0.0.1:8000/"
    const val TAG = "CargoLink"
}
