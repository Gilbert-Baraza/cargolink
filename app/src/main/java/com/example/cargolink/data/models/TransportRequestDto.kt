package com.example.cargolink.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class TransportRequestDto(
    @DocumentId val id: String? = null,
    val cargoRequestId: String = "",
    val truckId: String = "",
    val cargoOwnerUid: String = "",
    val truckOwnerUid: String = "",
    val cargoTitle: String = "",
    val truckTitle: String = "",
    val pickupLocation: String = "",
    val destination: String = "",
    val proposedPrice: Double? = null,
    val currency: String = "KES",
    val status: String = "PENDING", // PENDING, ACCEPTED, IN_TRANSIT, DELIVERED, REJECTED, CANCELLED
    val initiatedBy: String = "CARGO_OWNER", // CARGO_OWNER, TRUCK_DRIVER
    @get:PropertyName("isConfirmedByOwner") @field:PropertyName("isConfirmedByOwner")
    val isConfirmedByOwner: Boolean = false,
    val confirmedAt: Date? = null,
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null
)
