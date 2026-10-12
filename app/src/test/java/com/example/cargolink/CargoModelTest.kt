package com.example.cargolink

import com.example.cargolink.data.models.CargoDto
import org.junit.Assert.*
import org.junit.Test

class CargoModelTest {

    @Test
    fun cargoDto_creation_preservesOwnerUidAndWeight() {
        val cargo = CargoDto(
            id = "crg123",
            ownerUid = "owner_uid_456",
            cargoType = "Maize Grain",
            weightKg = 15000.0,
            pickupLocation = "Kisumu",
            destination = "Nairobi",
            budget = 45000.0,
            status = "available"
        )

        assertEquals("crg123", cargo.id)
        assertEquals("owner_uid_456", cargo.ownerUid)
        assertEquals("Maize Grain", cargo.cargoType)
        assertEquals(15000.0, cargo.weightKg, 0.01)
        assertEquals("Kisumu", cargo.pickupLocation)
        assertEquals("Nairobi", cargo.destination)
        assertEquals(45000.0, cargo.budget!!, 0.01)
        assertEquals("available", cargo.status)
    }

    @Test
    fun cargoWeightParsing_extractsNumericValueCorrectly() {
        val rawInput = "15,000 kg"
        val parsed = rawInput.replace(Regex("[^0-9.]"), "").toDoubleOrNull()

        assertNotNull(parsed)
        assertEquals(15000.0, parsed!!, 0.01)
    }

    @Test
    fun cargoWeightParsing_rejectsInvalidNumericInput() {
        val rawInput = "no weight"
        val parsed = rawInput.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0

        assertEquals(0.0, parsed, 0.001)
    }
}
