package com.example.cargolink.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cargolink.core.common.Resource
import com.example.cargolink.data.models.ConversationDto
import com.example.cargolink.data.models.MessageDto
import com.example.cargolink.data.models.TransportRequestDto
import com.example.cargolink.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ConversationListState {
    object Loading : ConversationListState()
    data class Success(val conversations: List<ConversationDto>) : ConversationListState()
    data class Error(val message: String) : ConversationListState()
}

sealed class ChatMessageState {
    object Loading : ChatMessageState()
    data class Success(val messages: List<MessageDto>) : ChatMessageState()
    data class Error(val message: String) : ChatMessageState()
}

sealed class SendMessageState {
    object Idle : SendMessageState()
    object Loading : SendMessageState()
    object Success : SendMessageState()
    data class Error(val message: String) : SendMessageState()
}

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()
    private val notificationRepository = com.example.cargolink.data.repository.NotificationRepository()
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val _conversationListState = MutableStateFlow<ConversationListState>(ConversationListState.Loading)
    val conversationListState: StateFlow<ConversationListState> = _conversationListState.asStateFlow()

    private val _chatMessagesState = MutableStateFlow<ChatMessageState>(ChatMessageState.Loading)
    val chatMessagesState: StateFlow<ChatMessageState> = _chatMessagesState.asStateFlow()

    private val _sendMessageState = MutableStateFlow<SendMessageState>(SendMessageState.Idle)
    val sendMessageState: StateFlow<SendMessageState> = _sendMessageState.asStateFlow()

    private val _conversationOpenState = MutableStateFlow<Resource<String>?>(null)
    val conversationOpenState: StateFlow<Resource<String>?> = _conversationOpenState.asStateFlow()

    fun loadUserConversations() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _conversationListState.value = ConversationListState.Loading
            try {
                repository.getUserConversationsFlow(uid).collect { list ->
                    _conversationListState.value = ConversationListState.Success(list)
                }
            } catch (e: Exception) {
                _conversationListState.value = ConversationListState.Error(e.localizedMessage ?: "Failed to load conversations")
            }
        }
    }

    fun openConversationByRequest(request: TransportRequestDto, onReady: (String) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _conversationOpenState.value = Resource.Error("User not authenticated")
            return
        }

        val reqId = request.id ?: "req_${System.currentTimeMillis()}"
        val deterministicConvId = "conv_$reqId"

        // Navigate immediately so the UI responds instantly
        onReady(deterministicConvId)

        viewModelScope.launch {
            _conversationOpenState.value = Resource.Loading()
            when (val result = repository.getOrCreateConversation(request, uid)) {
                is Resource.Success -> {
                    _conversationOpenState.value = Resource.Success(result.data ?: deterministicConvId)
                }
                is Resource.Error -> {
                    _conversationOpenState.value = Resource.Error(result.message ?: "Failed to open conversation")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _chatMessagesState.value = ChatMessageState.Loading
            try {
                repository.getMessagesFlow(conversationId).collect { messages ->
                    _chatMessagesState.value = ChatMessageState.Success(messages)
                }
            } catch (e: Exception) {
                _chatMessagesState.value = ChatMessageState.Error(e.localizedMessage ?: "Failed to load messages")
            }
        }
    }

    fun sendMessage(conversationId: String, messageText: String) {
        val trimmed = messageText.trim()
        if (trimmed.isBlank()) {
            _sendMessageState.value = SendMessageState.Error("Message cannot be empty")
            return
        }

        val currentUser = auth.currentUser
        val uid = currentUser?.uid
        if (uid == null) {
            _sendMessageState.value = SendMessageState.Error("User not authenticated")
            return
        }

        val senderName = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "User"

        viewModelScope.launch {
            _sendMessageState.value = SendMessageState.Loading
            when (val result = repository.sendMessage(conversationId, uid, senderName, trimmed)) {
                is Resource.Success -> {
                    _sendMessageState.value = SendMessageState.Success
                    // Extract potential recipient UID from conversation ID or list
                    val convList = (_conversationListState.value as? ConversationListState.Success)?.conversations
                    val activeConv = convList?.find { it.id == conversationId }
                    val recipientUid = activeConv?.let {
                        if (it.cargoOwnerUid == uid) it.truckOwnerUid else it.cargoOwnerUid
                    }
                    if (!recipientUid.isNullOrBlank() && recipientUid != uid) {
                        notificationRepository.createNotification(
                            com.example.cargolink.data.models.NotificationDto(
                                recipientUid = recipientUid,
                                senderUid = uid,
                                title = "New message from $senderName",
                                body = trimmed,
                                eventType = "NEW_MESSAGE",
                                targetId = conversationId,
                                isRead = false
                            )
                        )
                    }
                }
                is Resource.Error -> {
                    _sendMessageState.value = SendMessageState.Error(result.message ?: "Failed to send message")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun resetSendMessageState() {
        _sendMessageState.value = SendMessageState.Idle
    }

    fun resetConversationOpenState() {
        _conversationOpenState.value = null
    }
}
