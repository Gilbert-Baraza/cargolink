package com.example.cargolink.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ConversationDto(
    @DocumentId val id: String? = null,
    val requestId: String = "",
    val cargoOwnerUid: String = "",
    val truckOwnerUid: String = "",
    val cargoTitle: String = "",
    val truckTitle: String = "",
    val lastMessage: String = "",
    val lastSenderUid: String = "",
    @ServerTimestamp val lastMessageTimestamp: Date? = null,
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null
)
