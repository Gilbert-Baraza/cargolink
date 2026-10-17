package com.example.cargolink.data.repository

import com.example.cargolink.core.common.Resource
import com.example.cargolink.data.models.NotificationDto
import com.example.cargolink.data.remote.FirestoreDataSource
import kotlinx.coroutines.flow.Flow

class NotificationRepository {
    private val firestoreDataSource = FirestoreDataSource()

    suspend fun saveFcmToken(userId: String, token: String): Resource<Unit> {
        return try {
            val success = firestoreDataSource.saveFcmToken(userId, token)
            if (success) Resource.Success(Unit) else Resource.Error("Failed to save FCM token")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save FCM token")
        }
    }

    suspend fun createNotification(notification: NotificationDto): Resource<Unit> {
        return try {
            val success = firestoreDataSource.createNotification(notification)
            if (success) Resource.Success(Unit) else Resource.Error("Failed to create notification")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to create notification")
        }
    }

    fun getNotificationsFlow(recipientUid: String): Flow<List<NotificationDto>> {
        return firestoreDataSource.getNotificationsFlow(recipientUid)
    }

    suspend fun markNotificationAsRead(notificationId: String): Resource<Unit> {
        return try {
            val success = firestoreDataSource.markNotificationAsRead(notificationId)
            if (success) Resource.Success(Unit) else Resource.Error("Failed to mark notification as read")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to mark notification as read")
        }
    }

    suspend fun markAllNotificationsAsRead(recipientUid: String): Resource<Unit> {
        return try {
            val success = firestoreDataSource.markAllNotificationsAsRead(recipientUid)
            if (success) Resource.Success(Unit) else Resource.Error("Failed to mark all notifications as read")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to mark all notifications as read")
        }
    }
}
