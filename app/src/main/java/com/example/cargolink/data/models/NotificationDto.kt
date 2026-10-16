package com.example.cargolink.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class NotificationDto(
    @DocumentId val id: String? = null,
    val recipientUid: String = "",
    val senderUid: String = "",
    val title: String = "",
    val body: String = "",
    val eventType: String = "", // BOOKING_REQUEST, BOOKING_ACCEPTED, BOOKING_REJECTED, BOOKING_CANCELLED, SHIPMENT_IN_TRANSIT, SHIPMENT_DELIVERED, DELIVERY_CONFIRMED, NEW_MESSAGE
    val targetId: String = "", // requestId or conversationId
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Date? = null
)
