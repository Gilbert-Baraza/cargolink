package com.example.cargolink.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class TruckDto(
    @DocumentId val id: String? = null,
    val ownerUid: String = "",
    val registrationNumber: String = "",
    val truckType: String = "",
    val maxPayloadKg: Double = 0.0,
    val description: String? = null,
    val currentLocation: String = "",
    val preferredDestination: String? = null,
    val availabilityDate: Date? = null,
    val price: Double? = null,
    val currency: String = "USD",
    val status: String = "available", // available, inactive
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null
)
