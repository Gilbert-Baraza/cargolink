package com.example.cargolink

import com.example.cargolink.data.models.NotificationDto
import org.junit.Assert.*
import org.junit.Test

class NotificationTest {

    @Test
    fun notificationDto_creation_preservesFieldsAndInitialState() {
        val notif = NotificationDto(
            id = "notif_100",
            recipientUid = "user_b",
            senderUid = "user_a",
            title = "New Transport Request",
            body = "You received a new booking request for Maize Grain.",
            eventType = "BOOKING_REQUEST",
            targetId = "req_123",
            isRead = false
        )

        assertEquals("notif_100", notif.id)
        assertEquals("user_b", notif.recipientUid)
        assertEquals("user_a", notif.senderUid)
        assertEquals("BOOKING_REQUEST", notif.eventType)
        assertEquals("req_123", notif.targetId)
        assertFalse(notif.isRead)
    }

    @Test
    fun notification_senderExclusion_rejectsNotificationToSelf() {
        val senderUid = "user_a"
        val recipientUid = "user_a"

        val isSelfNotificationAllowed = recipientUid.isNotBlank() && recipientUid != senderUid
        assertFalse(isSelfNotificationAllowed)
    }

    @Test
    fun notification_recipientMapping_forChatMessages_notifiesCounterpartOnly() {
        val senderUid = "driver_1"
        val cargoOwnerUid = "owner_2"
        val truckOwnerUid = "driver_1"

        val recipientUid = if (cargoOwnerUid == senderUid) truckOwnerUid else cargoOwnerUid

        assertEquals("owner_2", recipientUid)
        assertNotEquals(senderUid, recipientUid)
    }

    @Test
    fun notification_unreadCounter_calculatesUnreadCorrectly() {
        val notifications = listOf(
            NotificationDto(id = "1", isRead = false),
            NotificationDto(id = "2", isRead = true),
            NotificationDto(id = "3", isRead = false),
            NotificationDto(id = "4", isRead = false)
        )

        val unreadCount = notifications.count { !it.isRead }
        assertEquals(3, unreadCount)
    }

    @Test
    fun notification_deepLinkRouting_mapsTargetIdToCorrectScreen() {
        val bookingNotif = NotificationDto(
            eventType = "BOOKING_REQUEST",
            targetId = "req_555"
        )
        val chatNotif = NotificationDto(
            eventType = "NEW_MESSAGE",
            targetId = "conv_req_555"
        )

        val bookingRoute = "booking_detail/${bookingNotif.targetId}"
        val chatRoute = "chat/${chatNotif.targetId}"

        assertEquals("booking_detail/req_555", bookingRoute)
        assertEquals("chat/conv_req_555", chatRoute)
    }
}
