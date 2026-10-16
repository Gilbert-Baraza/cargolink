package com.example.cargolink.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class MessageDto(
    @DocumentId val id: String? = null,
    val conversationId: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val messageText: String = "",
    @ServerTimestamp val timestamp: Date? = null
)
