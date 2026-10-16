package com.example.cargolink.core.notifications

import android.util.Log
import com.example.cargolink.data.remote.FirestoreDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CargoLinkMessagingService : FirebaseMessagingService() {

    private val firestoreDataSource = FirestoreDataSource()
    private val TAG = "CargoLinkMessagingService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM Token: $token")
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUid != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    firestoreDataSource.saveFcmToken(currentUid, token)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save refreshed FCM token", e)
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "CargoLink Notification"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        val targetId = remoteMessage.data["targetId"]
        val eventType = remoteMessage.data["eventType"]

        NotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            body = body,
            targetId = targetId,
            eventType = eventType
        )
    }
}
