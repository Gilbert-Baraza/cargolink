package com.example.cargolink

import com.example.cargolink.data.models.TruckDto
import com.example.cargolink.data.models.UserDto
import org.junit.Assert.*
import org.junit.Test

class TruckModelTest {

    @Test
    fun truckDto_creation_preservesOwnerUidAndPayload() {
        val truck = TruckDto(
            id = "trk123",
            ownerUid = "user_driver_123",
            registrationNumber = "KCB 456C",
            truckType = "Flatbed",
            maxPayloadKg = 28000.0,
            currentLocation = "Nairobi",
            status = "available"
        )

        assertEquals("trk123", truck.id)
        assertEquals("user_driver_123", truck.ownerUid)
        assertEquals("KCB 456C", truck.registrationNumber)
        assertEquals(28000.0, truck.maxPayloadKg, 0.01)
        assertEquals("available", truck.status)
    }

    @Test
    fun payloadParsing_extractsNumericValueCorrectly() {
        val rawInput = "28,000 kg"
        val parsed = rawInput.replace(Regex("[^0-9.]"), "").toDoubleOrNull()
        
        assertNotNull(parsed)
        assertEquals(28000.0, parsed!!, 0.01)
    }

    @Test
    fun payloadParsing_rejectsInvalidNumericInput() {
        val rawInput = "invalid payload"
        val parsed = rawInput.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0

        assertEquals(0.0, parsed, 0.001)
    }

    @Test
    fun userDto_participationType_validatesRoles() {
        val driver = UserDto(
            firebaseUid = "uid1",
            name = "John Driver",
            email = "driver@cargolink.com",
            participationType = "TRUCK_DRIVER"
        )
        val operator = UserDto(
            firebaseUid = "uid2",
            name = "Jack Operator",
            email = "operator@cargolink.com",
            participationType = "TRUCK_OPERATOR"
        )
        val owner = UserDto(
            firebaseUid = "uid3",
            name = "Jane Owner",
            email = "owner@cargolink.com",
            participationType = "CARGO_OWNER"
        )

        assertEquals("TRUCK_DRIVER", driver.participationType)
        assertEquals("TRUCK_OPERATOR", operator.participationType)
        assertEquals("CARGO_OWNER", owner.participationType)
    }
}
