package com.example.cargolink.data.repository

import com.example.cargolink.core.common.Resource
import com.example.cargolink.data.models.ConversationDto
import com.example.cargolink.data.models.MessageDto
import com.example.cargolink.data.models.TransportRequestDto
import com.example.cargolink.data.remote.FirestoreDataSource
import kotlinx.coroutines.flow.Flow

class ChatRepository {
    private val firestoreDataSource = FirestoreDataSource()

    suspend fun getOrCreateConversation(request: TransportRequestDto, currentUid: String): Resource<String> {
        return try {
            val convId = firestoreDataSource.getOrCreateConversation(request, currentUid)
            Resource.Success(convId)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to open conversation")
        }
    }

    fun getUserConversationsFlow(currentUid: String): Flow<List<ConversationDto>> {
        return firestoreDataSource.getUserConversationsFlow(currentUid)
    }

    fun getMessagesFlow(conversationId: String): Flow<List<MessageDto>> {
        return firestoreDataSource.getMessagesFlow(conversationId)
    }

    suspend fun sendMessage(
        conversationId: String,
        senderUid: String,
        senderName: String,
        messageText: String
    ): Resource<Unit> {
        return try {
            val success = firestoreDataSource.sendMessage(conversationId, senderUid, senderName, messageText)
            if (success) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to send message")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to send message")
        }
    }
}
