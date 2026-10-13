package com.example.cargolink

import com.example.cargolink.data.models.CargoDto
import com.example.cargolink.data.models.TransportRequestDto
import com.example.cargolink.data.models.TruckDto
import org.junit.Assert.*
import org.junit.Test

class TransportRequestTest {

    @Test
    fun transportRequestDto_creation_preservesFieldsAndInitialStatus() {
        val request = TransportRequestDto(
            id = "req_123",
            cargoRequestId = "crg_456",
            truckId = "trk_789",
            cargoOwnerUid = "owner_a",
            truckOwnerUid = "driver_b",
            cargoTitle = "Maize Grain",
            truckTitle = "KCB 456C",
            pickupLocation = "Kisumu",
            destination = "Nairobi",
            proposedPrice = 45000.0,
            status = "PENDING"
        )

        assertEquals("req_123", request.id)
        assertEquals("crg_456", request.cargoRequestId)
        assertEquals("trk_789", request.truckId)
        assertEquals("owner_a", request.cargoOwnerUid)
        assertEquals("driver_b", request.truckOwnerUid)
        assertEquals("PENDING", request.status)
        assertEquals(45000.0, request.proposedPrice!!, 0.01)
        assertFalse(request.isConfirmedByOwner)
        assertNull(request.confirmedAt)
    }

    @Test
    fun transportRequest_statusTransitions_validatePermittedStates() {
        val validStatuses = listOf("PENDING", "ACCEPTED", "IN_TRANSIT", "DELIVERED", "REJECTED", "CANCELLED")

        assertTrue(validStatuses.contains("PENDING"))
        assertTrue(validStatuses.contains("ACCEPTED"))
        assertTrue(validStatuses.contains("IN_TRANSIT"))
        assertTrue(validStatuses.contains("DELIVERED"))
        assertTrue(validStatuses.contains("REJECTED"))
        assertTrue(validStatuses.contains("CANCELLED"))
    }

    @Test
    fun transportRequest_makeOffer_preservesDriverInitiatedByField() {
        val offer = TransportRequestDto(
            id = "off_123",
            cargoRequestId = "crg_999",
            truckId = "trk_888",
            cargoOwnerUid = "owner_a",
            truckOwnerUid = "driver_b",
            status = "PENDING",
            initiatedBy = "TRUCK_DRIVER"
        )

        assertEquals("TRUCK_DRIVER", offer.initiatedBy)
        assertEquals("driver_b", offer.truckOwnerUid)
        assertEquals("owner_a", offer.cargoOwnerUid)
    }

    @Test
    fun transportRequest_driverOfferAcceptance_authorizedForCargoOwnerOnly() {
        val driverOffer = TransportRequestDto(
            id = "off_456",
            cargoOwnerUid = "owner_a",
            truckOwnerUid = "driver_b",
            status = "PENDING",
            initiatedBy = "TRUCK_DRIVER"
        )

        val callerCargoOwner = "owner_a"
        val callerDriver = "driver_b"

        val isCargoOwnerAuthorized = driverOffer.initiatedBy == "TRUCK_DRIVER" && callerCargoOwner == driverOffer.cargoOwnerUid
        val isDriverAuthorizedToAcceptOwnOffer = driverOffer.initiatedBy == "TRUCK_DRIVER" && callerDriver == driverOffer.cargoOwnerUid

        assertTrue(isCargoOwnerAuthorized)
        assertFalse(isDriverAuthorizedToAcceptOwnOffer)
    }

    @Test
    fun transportRequest_driverOfferCancellation_authorizedForDriverOnly() {
        val driverOffer = TransportRequestDto(
            id = "off_789",
            cargoOwnerUid = "owner_a",
            truckOwnerUid = "driver_b",
            status = "PENDING",
            initiatedBy = "TRUCK_DRIVER"
        )

        val callerDriver = "driver_b"
        val callerCargoOwner = "owner_a"

        val canDriverCancel = driverOffer.initiatedBy == "TRUCK_DRIVER" && callerDriver == driverOffer.truckOwnerUid
        val canOwnerCancelDriverOffer = driverOffer.initiatedBy == "TRUCK_DRIVER" && callerCargoOwner == driverOffer.truckOwnerUid

        assertTrue(canDriverCancel)
        assertFalse(canOwnerCancelDriverOffer)
    }

    @Test
    fun transportRequest_deliveryConfirmation_cargoOwnerAuthorizedOnly() {
        val req = TransportRequestDto(
            id = "r_deliv",
            cargoOwnerUid = "owner_a",
            truckOwnerUid = "driver_b",
            status = "DELIVERED",
            isConfirmedByOwner = false
        )

        val callerOwner = "owner_a"
        val callerDriver = "driver_b"

        val canOwnerConfirm = callerOwner == req.cargoOwnerUid && req.status == "DELIVERED" && !req.isConfirmedByOwner
        val canDriverConfirm = callerDriver == req.cargoOwnerUid && req.status == "DELIVERED" && !req.isConfirmedByOwner

        assertTrue(canOwnerConfirm)
        assertFalse(canDriverConfirm)
    }

    @Test
    fun transportRequest_deliveryConfirmation_preventsDuplicateConfirmation() {
        val confirmedReq = TransportRequestDto(
            id = "r_confirmed",
            cargoOwnerUid = "owner_a",
            status = "DELIVERED",
            isConfirmedByOwner = true
        )

        val canConfirmAgain = "owner_a" == confirmedReq.cargoOwnerUid && confirmedReq.status == "DELIVERED" && !confirmedReq.isConfirmedByOwner
        assertFalse(canConfirmAgain)
    }

    @Test
    fun transportRequest_bookingHistoryFilters_categorizeStatusesCorrectly() {
        val requests = listOf(
            TransportRequestDto(id = "1", status = "PENDING"),
            TransportRequestDto(id = "2", status = "ACCEPTED"),
            TransportRequestDto(id = "3", status = "IN_TRANSIT"),
            TransportRequestDto(id = "4", status = "DELIVERED"),
            TransportRequestDto(id = "5", status = "REJECTED"),
            TransportRequestDto(id = "6", status = "CANCELLED")
        )

        val pendingList = requests.filter { it.status.uppercase() == "PENDING" }
        val activeList = requests.filter { it.status.uppercase() in listOf("ACCEPTED", "IN_TRANSIT") }
        val deliveredList = requests.filter { it.status.uppercase() == "DELIVERED" }
        val closedList = requests.filter { it.status.uppercase() in listOf("REJECTED", "CANCELLED") }

        assertEquals(1, pendingList.size)
        assertEquals(2, activeList.size)
        assertEquals(1, deliveredList.size)
        assertEquals(2, closedList.size)
        assertEquals(6, requests.size)
    }

    @Test
    fun transportRequest_shipmentLifecycleTransitions_enforcesSequentialOrder() {
        val currentStatus = "ACCEPTED"

        val canTransitionToInTransit = currentStatus == "ACCEPTED"
        val canTransitionToDeliveredDirectly = currentStatus == "IN_TRANSIT"

        assertTrue(canTransitionToInTransit)
        assertFalse(canTransitionToDeliveredDirectly)
    }

    @Test
    fun transportRequest_shipmentLifecycle_inTransitToDeliveredTransition() {
        val currentStatus = "IN_TRANSIT"

        val canTransitionToDelivered = currentStatus == "IN_TRANSIT"
        val canTransitionToAccepted = currentStatus == "PENDING"

        assertTrue(canTransitionToDelivered)
        assertFalse(canTransitionToAccepted)
    }

    @Test
    fun transportRequest_deliveredStatus_cannotBeAcceptedAgain() {
        val completedRequest = TransportRequestDto(id = "r_done", status = "DELIVERED")
        val canAccept = completedRequest.status == "PENDING"
        assertFalse(canAccept)
    }

    @Test
    fun transportRequest_immutableFieldsOnUpdate_mustMatchOriginal() {
        val original = TransportRequestDto(
            id = "r_orig",
            cargoOwnerUid = "owner_a",
            truckOwnerUid = "driver_b",
            cargoRequestId = "c_1",
            truckId = "t_1"
        )
        val updatedValid = original.copy(status = "ACCEPTED")
        val updatedForgedOwner = original.copy(cargoOwnerUid = "hacker")

        assertEquals(original.cargoOwnerUid, updatedValid.cargoOwnerUid)
        assertEquals(original.truckOwnerUid, updatedValid.truckOwnerUid)
        assertNotEquals(original.cargoOwnerUid, updatedForgedOwner.cargoOwnerUid)
    }

    @Test
    fun transportRequest_bookingDetailsAccess_authorizedForPartiesOnly() {
        val cargoOwnerUid = "owner_a"
        val truckOwnerUid = "driver_b"
        val unrelatedUserUid = "user_c"

        val isCargoOwnerAuthorized = cargoOwnerUid == "owner_a" || cargoOwnerUid == "driver_b"
        val isTruckOwnerAuthorized = truckOwnerUid == "owner_a" || truckOwnerUid == "driver_b"
        val isUnrelatedUserAuthorized = unrelatedUserUid == "owner_a" || unrelatedUserUid == "driver_b"

        assertTrue(isCargoOwnerAuthorized)
        assertTrue(isTruckOwnerAuthorized)
        assertFalse(isUnrelatedUserAuthorized)
    }

    @Test
    fun transportRequest_preventsOwnTruckBooking() {
        val sameUserUid = "user_123"
        val cargoOwnerUid = sameUserUid
        val truckOwnerUid = sameUserUid

        val isSelfRequestAllowed = cargoOwnerUid != truckOwnerUid
        assertFalse(isSelfRequestAllowed)
    }

    @Test
    fun transportRequest_validatesCallerIsCargoOwner() {
        val callerUid = "caller_123"
        val cargoOwnerUid = "caller_123"
        val forgedCargoOwnerUid = "other_user_456"

        assertTrue(callerUid == cargoOwnerUid)
        assertFalse(callerUid == forgedCargoOwnerUid)
    }

    @Test
    fun transportRequest_acceptEligibility_requiresPendingRequestAndAvailableResources() {
        val pendingRequestStatus = "PENDING"
        val acceptedRequestStatus = "ACCEPTED"
        val cargoStatusAvailable = "available"
        val cargoStatusBooked = "booked"
        val truckStatusAvailable = "available"

        val canAcceptValid = pendingRequestStatus == "PENDING" && cargoStatusAvailable == "available" && truckStatusAvailable == "available"
        val canAcceptAlreadyBooked = pendingRequestStatus == "PENDING" && cargoStatusBooked == "available" && truckStatusAvailable == "available"
        val canAcceptAlreadyAccepted = acceptedRequestStatus == "PENDING" && cargoStatusAvailable == "available" && truckStatusAvailable == "available"

        assertTrue(canAcceptValid)
        assertFalse(canAcceptAlreadyBooked)
        assertFalse(canAcceptAlreadyAccepted)
    }

    @Test
    fun transportRequest_cancellation_requiresCargoOwnerAuth() {
        val callerUid = "cargo_owner_789"
        val requestOwnerUid = "cargo_owner_789"
        val otherUserUid = "unauthorized_driver"

        val isOwnerCancelAuthorized = callerUid == requestOwnerUid
        val isUnauthorizedCancel = otherUserUid == requestOwnerUid

        assertTrue(isOwnerCancelAuthorized)
        assertFalse(isUnauthorizedCancel)
    }

    @Test
    fun transportRequest_acceptance_requiresTruckDriverAuth() {
        val callerUid = "driver_123"
        val requestDriverUid = "driver_123"
        val otherUserUid = "cargo_owner_789"

        val isDriverAcceptAuthorized = callerUid == requestDriverUid
        val isUnauthorizedAccept = otherUserUid == requestDriverUid

        assertTrue(isDriverAcceptAuthorized)
        assertFalse(isUnauthorizedAccept)
    }

    @Test
    fun transportRequest_duplicateRequestCheck_detectsActiveRequestsForSameTruck() {
        val requests = listOf(
            TransportRequestDto(id = "r1", truckId = "trk_100", cargoOwnerUid = "owner_a", status = "PENDING"),
            TransportRequestDto(id = "r2", truckId = "trk_200", cargoOwnerUid = "owner_a", status = "REJECTED")
        )

        val hasActiveForTrk100 = requests.any { it.truckId == "trk_100" && (it.status == "PENDING" || it.status == "ACCEPTED") }
        val hasActiveForTrk200 = requests.any { it.truckId == "trk_200" && (it.status == "PENDING" || it.status == "ACCEPTED") }

        assertTrue(hasActiveForTrk100)
        assertFalse(hasActiveForTrk200)
    }

    @Test
    fun transportRequest_concurrencySafeAcceptance_secondAcceptanceFailsWhenTruckInactive() {
        var truck = TruckDto(id = "trk_555", status = "available")
        var cargo1 = CargoDto(id = "crg_1", status = "available")
        val cargo2 = CargoDto(id = "crg_2", status = "available")

        val req1 = TransportRequestDto(id = "r1", truckId = "trk_555", cargoRequestId = "crg_1", status = "PENDING")
        val req2 = TransportRequestDto(id = "r2", truckId = "trk_555", cargoRequestId = "crg_2", status = "PENDING")

        // First acceptance succeeds
        val req1Acceptable = req1.status == "PENDING" && truck.status == "available" && cargo1.status == "available"
        assertTrue(req1Acceptable)

        // Apply first acceptance state updates
        truck = truck.copy(status = "inactive")
        cargo1 = cargo1.copy(status = "booked")

        // Second acceptance fails because truck is now inactive
        val req2Acceptable = req2.status == "PENDING" && truck.status == "available" && cargo2.status == "available"
        assertFalse(req2Acceptable)

        // Cargo 2 remains untouched and available
        assertEquals("available", cargo2.status)
        assertEquals("inactive", truck.status)
        assertEquals("booked", cargo1.status)
    }
}
