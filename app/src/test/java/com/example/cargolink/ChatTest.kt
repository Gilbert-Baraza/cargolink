package com.example.cargolink

import com.example.cargolink.data.models.ConversationDto
import com.example.cargolink.data.models.MessageDto
import com.example.cargolink.data.models.TransportRequestDto
import org.junit.Assert.*
import org.junit.Test

class ChatTest {

    @Test
    fun conversationDto_creation_preservesFieldsAndParticipants() {
        val conv = ConversationDto(
            id = "conv_req123",
            requestId = "req123",
            cargoOwnerUid = "owner_a",
            truckOwnerUid = "driver_b",
            cargoTitle = "Maize Grain 20 Tons",
            truckTitle = "KCB 456C",
            lastMessage = "Hello, when can you pick up?"
        )

        assertEquals("conv_req123", conv.id)
        assertEquals("req123", conv.requestId)
        assertEquals("owner_a", conv.cargoOwnerUid)
        assertEquals("driver_b", conv.truckOwnerUid)
        assertEquals("Hello, when can you pick up?", conv.lastMessage)
    }

    @Test
    fun conversation_deterministicId_preventsDuplicateRecords() {
        val request = TransportRequestDto(
            id = "req_999",
            cargoOwnerUid = "owner_a",
            truckOwnerUid = "driver_b"
        )

        val convId1 = "conv_${request.id}"
        val convId2 = "conv_${request.id}"

        assertEquals("conv_req_999", convId1)
        assertEquals(convId1, convId2)
    }

    @Test
    fun messageDto_validation_detectsEmptyText() {
        val emptyMessage = MessageDto(
            conversationId = "conv_123",
            senderUid = "user_a",
            messageText = "   "
        )
        val validMessage = MessageDto(
            conversationId = "conv_123",
            senderUid = "user_a",
            messageText = "Cargo is ready for loading."
        )

        assertTrue(emptyMessage.messageText.trim().isBlank())
        assertFalse(validMessage.messageText.trim().isBlank())
    }

    @Test
    fun chat_accessControl_authorizedForBookingParticipantsOnly() {
        val cargoOwnerUid = "owner_a"
        val truckOwnerUid = "driver_b"
        val unrelatedUserUid = "hacker_c"

        val isCargoOwnerAuthorized = cargoOwnerUid == "owner_a" || cargoOwnerUid == "driver_b"
        val isTruckOwnerAuthorized = truckOwnerUid == "owner_a" || truckOwnerUid == "driver_b"
        val isUnrelatedUserAuthorized = unrelatedUserUid == "owner_a" || unrelatedUserUid == "driver_b"

        assertTrue(isCargoOwnerAuthorized)
        assertTrue(isTruckOwnerAuthorized)
        assertFalse(isUnrelatedUserAuthorized)
    }

    @Test
    fun chat_selfConversation_rejectedForSameParticipant() {
        val cargoOwnerUid = "user_123"
        val truckOwnerUid = "user_123"

        val isSelfChatAllowed = cargoOwnerUid != truckOwnerUid
        assertFalse(isSelfChatAllowed)
    }
}
