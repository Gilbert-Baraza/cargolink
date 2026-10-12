package com.example.cargolink.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CargoDto(
    @DocumentId val id: String? = null,
    val ownerUid: String = "",
    val cargoType: String = "",
    val weightKg: Double = 0.0,
    val pickupLocation: String = "",
    val destination: String = "",
    val budget: Double? = null,
    val currency: String = "KES",
    val preferredDate: String? = null,
    val specialRequirements: String? = null,
    val status: String = "available", // available, inactive, booked
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null
)
