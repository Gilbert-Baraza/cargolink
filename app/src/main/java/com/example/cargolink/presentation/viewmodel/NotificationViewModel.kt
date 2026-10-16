package com.example.cargolink.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cargolink.data.models.NotificationDto
import com.example.cargolink.data.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NotificationListState {
    object Loading : NotificationListState()
    data class Success(val notifications: List<NotificationDto>, val unreadCount: Int) : NotificationListState()
    data class Error(val message: String) : NotificationListState()
}

class NotificationViewModel : ViewModel() {
    private val repository = NotificationRepository()
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val _notificationListState = MutableStateFlow<NotificationListState>(NotificationListState.Loading)
    val notificationListState: StateFlow<NotificationListState> = _notificationListState.asStateFlow()

    init {
        registerFcmToken()
        loadNotifications()
    }

    fun registerFcmToken() {
        val uid = auth.currentUser?.uid ?: return
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (!token.isNullOrBlank()) {
                        viewModelScope.launch {
                            repository.saveFcmToken(uid, token)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Gracefully handle FCM registration errors
        }
    }

    fun loadNotifications() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _notificationListState.value = NotificationListState.Loading
            try {
                repository.getNotificationsFlow(uid).collect { list ->
                    val unread = list.count { !it.isRead }
                    _notificationListState.value = NotificationListState.Success(list, unread)
                }
            } catch (e: Exception) {
                _notificationListState.value = NotificationListState.Error(e.localizedMessage ?: "Failed to load notifications")
            }
        }
    }

    fun markAsRead(notification: NotificationDto) {
        val notifId = notification.id ?: return
        if (notification.isRead) return

        val currentState = _notificationListState.value
        if (currentState is NotificationListState.Success) {
            val updatedList = currentState.notifications.map {
                if (it.id == notifId) it.copy(isRead = true) else it
            }
            val newUnread = updatedList.count { !it.isRead }
            _notificationListState.value = NotificationListState.Success(updatedList, newUnread)
        }

        viewModelScope.launch {
            repository.markNotificationAsRead(notifId)
        }
    }

    fun markAllAsRead() {
        val currentState = _notificationListState.value
        if (currentState is NotificationListState.Success) {
            val unreadNotifs = currentState.notifications.filter { !it.isRead }
            if (unreadNotifs.isEmpty()) return

            val updatedList = currentState.notifications.map { it.copy(isRead = true) }
            _notificationListState.value = NotificationListState.Success(updatedList, 0)

            viewModelScope.launch {
                unreadNotifs.forEach { notif ->
                    notif.id?.let { repository.markNotificationAsRead(it) }
                }
            }
        }
    }

    fun sendNotificationEvent(
        recipientUid: String,
        title: String,
        body: String,
        eventType: String,
        targetId: String
    ) {
        val senderUid = auth.currentUser?.uid ?: return
        if (recipientUid.isBlank() || recipientUid == senderUid) return

        val notif = NotificationDto(
            recipientUid = recipientUid,
            senderUid = senderUid,
            title = title,
            body = body,
            eventType = eventType,
            targetId = targetId,
            isRead = false
        )

        viewModelScope.launch {
            repository.createNotification(notif)
        }
    }
}
