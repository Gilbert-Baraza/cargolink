package com.example.cargolink.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cargolink.core.common.Resource
import com.example.cargolink.data.models.CargoDto
import com.example.cargolink.data.models.TransportRequestDto
import com.example.cargolink.data.models.TruckDto
import com.example.cargolink.data.repository.TransportRequestRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RequestListState {
    object Loading : RequestListState()
    data class Success(val requests: List<TransportRequestDto>) : RequestListState()
    data class Error(val message: String) : RequestListState()
}

sealed class RequestActionState {
    object Idle : RequestActionState()
    object Loading : RequestActionState()
    object Success : RequestActionState()
    data class Error(val message: String) : RequestActionState()
}

sealed class RequestDetailState {
    object Idle : RequestDetailState()
    object Loading : RequestDetailState()
    data class Success(val request: TransportRequestDto) : RequestDetailState()
    data class Error(val message: String) : RequestDetailState()
}

class TransportRequestViewModel : ViewModel() {
    private val repository = TransportRequestRepository()
    private val notificationRepository = com.example.cargolink.data.repository.NotificationRepository()
    private val auth by lazy { FirebaseAuth.getInstance() }

    private fun sendNotif(recipientUid: String, title: String, body: String, eventType: String, targetId: String) {
        val senderUid = auth.currentUser?.uid ?: return
        if (recipientUid.isBlank() || recipientUid == senderUid) return
        viewModelScope.launch {
            notificationRepository.createNotification(
                com.example.cargolink.data.models.NotificationDto(
                    recipientUid = recipientUid,
                    senderUid = senderUid,
                    title = title,
                    body = body,
                    eventType = eventType,
                    targetId = targetId,
                    isRead = false
                )
            )
        }
    }

    private val _requestListState = MutableStateFlow<RequestListState>(RequestListState.Loading)
    val requestListState: StateFlow<RequestListState> = _requestListState.asStateFlow()

    private val _requestDetailState = MutableStateFlow<RequestDetailState>(RequestDetailState.Idle)
    val requestDetailState: StateFlow<RequestDetailState> = _requestDetailState.asStateFlow()

    private val _requestActionState = MutableStateFlow<RequestActionState>(RequestActionState.Idle)
    val requestActionState: StateFlow<RequestActionState> = _requestActionState.asStateFlow()

    fun fetchCargoOwnerRequests() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _requestListState.value = RequestListState.Loading
            when (val result = repository.getRequestsByCargoOwner(uid)) {
                is Resource.Success -> {
                    _requestListState.value = RequestListState.Success(result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _requestListState.value = RequestListState.Error(result.message ?: "Failed to fetch requests")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun fetchTruckOwnerRequests() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _requestListState.value = RequestListState.Loading
            when (val result = repository.getRequestsByTruckOwner(uid)) {
                is Resource.Success -> {
                    _requestListState.value = RequestListState.Success(result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _requestListState.value = RequestListState.Error(result.message ?: "Failed to fetch requests")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun createTransportRequest(
        cargo: CargoDto,
        truck: TruckDto,
        proposedPrice: Double? = null
    ) {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            _requestActionState.value = RequestActionState.Error("User not authenticated")
            return
        }

        // Security & Validation checks
        if (cargo.ownerUid != currentUid) {
            _requestActionState.value = RequestActionState.Error("Selected cargo requirement does not belong to you.")
            return
        }

        if (truck.ownerUid == currentUid) {
            _requestActionState.value = RequestActionState.Error("You cannot send a transport request to your own truck.")
            return
        }

        if (cargo.status.lowercase() != "available") {
            _requestActionState.value = RequestActionState.Error("Selected cargo requirement is not available.")
            return
        }

        if (truck.status.lowercase() != "available") {
            _requestActionState.value = RequestActionState.Error("Selected truck is not available.")
            return
        }

        viewModelScope.launch {
            _requestActionState.value = RequestActionState.Loading

            val targetTruckId = truck.id ?: ""
            if (targetTruckId.isNotBlank()) {
                val activeCheck = repository.hasActiveRequestForTruck(currentUid, targetTruckId)
                if (activeCheck is Resource.Success && activeCheck.data == true) {
                    _requestActionState.value = RequestActionState.Error("A booking request has already been submitted for this truck.")
                    return@launch
                }
            }

            val request = TransportRequestDto(
                cargoRequestId = cargo.id ?: "",
                truckId = targetTruckId,
                cargoOwnerUid = currentUid,
                truckOwnerUid = truck.ownerUid,
                cargoTitle = cargo.cargoType,
                truckTitle = truck.registrationNumber,
                pickupLocation = cargo.pickupLocation,
                destination = cargo.destination.ifBlank { truck.preferredDestination ?: "Flexible" },
                proposedPrice = proposedPrice ?: cargo.budget ?: truck.price,
                currency = cargo.currency,
                status = "PENDING"
            )

            when (val result = repository.createTransportRequest(request)) {
                is Resource.Success -> {
                    val createdId = result.data ?: ""
                    _requestActionState.value = RequestActionState.Success
                    sendNotif(
                        recipientUid = truck.ownerUid,
                        title = "New Booking Request",
                        body = "You received a new booking request for ${cargo.cargoType}.",
                        eventType = "BOOKING_REQUEST",
                        targetId = createdId
                    )
                    fetchCargoOwnerRequests()
                }
                is Resource.Error -> {
                    _requestActionState.value = RequestActionState.Error(result.message ?: "Failed to create request")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun createTransportOffer(
        cargo: CargoDto,
        truck: TruckDto,
        proposedPrice: Double? = null
    ) {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            _requestActionState.value = RequestActionState.Error("User not authenticated")
            return
        }

        if (truck.ownerUid != currentUid) {
            _requestActionState.value = RequestActionState.Error("Selected truck does not belong to you.")
            return
        }

        if (cargo.ownerUid == currentUid) {
            _requestActionState.value = RequestActionState.Error("You cannot submit an offer to your own cargo requirement.")
            return
        }

        if (cargo.status.lowercase() != "available") {
            _requestActionState.value = RequestActionState.Error("Selected cargo requirement is not available.")
            return
        }

        if (truck.status.lowercase() != "available") {
            _requestActionState.value = RequestActionState.Error("Selected truck is not available.")
            return
        }

        viewModelScope.launch {
            _requestActionState.value = RequestActionState.Loading

            val targetCargoId = cargo.id ?: ""
            val request = TransportRequestDto(
                cargoRequestId = targetCargoId,
                truckId = truck.id ?: "",
                cargoOwnerUid = cargo.ownerUid,
                truckOwnerUid = currentUid,
                cargoTitle = cargo.cargoType,
                truckTitle = truck.registrationNumber,
                pickupLocation = cargo.pickupLocation,
                destination = cargo.destination.ifBlank { truck.preferredDestination ?: "Flexible" },
                proposedPrice = proposedPrice ?: cargo.budget ?: truck.price,
                currency = cargo.currency,
                status = "PENDING",
                initiatedBy = "TRUCK_DRIVER"
            )

            when (val result = repository.createTransportRequest(request)) {
                is Resource.Success -> {
                    val createdId = result.data ?: ""
                    _requestActionState.value = RequestActionState.Success
                    sendNotif(
                        recipientUid = cargo.ownerUid,
                        title = "New Transport Offer",
                        body = "You received a new offer for ${cargo.cargoType}.",
                        eventType = "BOOKING_REQUEST",
                        targetId = createdId
                    )
                    fetchTruckOwnerRequests()
                }
                is Resource.Error -> {
                    _requestActionState.value = RequestActionState.Error(result.message ?: "Failed to submit offer")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun acceptRequest(request: TransportRequestDto) {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            _requestActionState.value = RequestActionState.Error("User not authenticated")
            return
        }

        val isDriverOffer = request.initiatedBy == "TRUCK_DRIVER"
        val isAuthorized = if (isDriverOffer) request.cargoOwnerUid == currentUid else request.truckOwnerUid == currentUid

        if (!isAuthorized) {
            _requestActionState.value = RequestActionState.Error("Unauthorized to accept this booking.")
            return
        }

        viewModelScope.launch {
            _requestActionState.value = RequestActionState.Loading
            when (val result = repository.acceptTransportRequest(request)) {
                is Resource.Success -> {
                    _requestActionState.value = RequestActionState.Success
                    sendNotif(
                        recipientUid = if (isDriverOffer) request.truckOwnerUid else request.cargoOwnerUid,
                        title = "Booking Accepted!",
                        body = "Booking for ${request.cargoTitle} has been accepted.",
                        eventType = "BOOKING_ACCEPTED",
                        targetId = request.id ?: ""
                    )
                    if (isDriverOffer) fetchCargoOwnerRequests() else fetchTruckOwnerRequests()
                }
                is Resource.Error -> {
                    _requestActionState.value = RequestActionState.Error(result.message ?: "Failed to accept request")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun rejectRequest(request: TransportRequestDto) {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            _requestActionState.value = RequestActionState.Error("User not authenticated")
            return
        }

        val isDriverOffer = request.initiatedBy == "TRUCK_DRIVER"
        val isAuthorized = if (isDriverOffer) request.cargoOwnerUid == currentUid else request.truckOwnerUid == currentUid

        if (!isAuthorized) {
            _requestActionState.value = RequestActionState.Error("Unauthorized to reject this booking.")
            return
        }

        viewModelScope.launch {
            _requestActionState.value = RequestActionState.Loading
            when (val result = repository.updateRequestStatus(request.id ?: "", "REJECTED")) {
                is Resource.Success -> {
                    _requestActionState.value = RequestActionState.Success
                    sendNotif(
                        recipientUid = if (isDriverOffer) request.truckOwnerUid else request.cargoOwnerUid,
                        title = "Booking Rejected",
                        body = "Booking request for ${request.cargoTitle} was rejected.",
                        eventType = "BOOKING_REJECTED",
                        targetId = request.id ?: ""
                    )
                    if (isDriverOffer) fetchCargoOwnerRequests() else fetchTruckOwnerRequests()
                }
                is Resource.Error -> {
                    _requestActionState.value = RequestActionState.Error(result.message ?: "Failed to reject request")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun cancelRequest(request: TransportRequestDto) {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            _requestActionState.value = RequestActionState.Error("User not authenticated")
            return
        }

        val isDriverOffer = request.initiatedBy == "TRUCK_DRIVER"
        val isAuthorized = if (isDriverOffer) request.truckOwnerUid == currentUid else request.cargoOwnerUid == currentUid

        if (!isAuthorized) {
            _requestActionState.value = RequestActionState.Error("Unauthorized to cancel this booking.")
            return
        }

        viewModelScope.launch {
            _requestActionState.value = RequestActionState.Loading
            when (val result = repository.updateRequestStatus(request.id ?: "", "CANCELLED")) {
                is Resource.Success -> {
                    _requestActionState.value = RequestActionState.Success
                    sendNotif(
                        recipientUid = if (isDriverOffer) request.cargoOwnerUid else request.truckOwnerUid,
                        title = "Booking Cancelled",
                        body = "Booking for ${request.cargoTitle} was cancelled.",
                        eventType = "BOOKING_CANCELLED",
                        targetId = request.id ?: ""
                    )
                    if (isDriverOffer) fetchTruckOwnerRequests() else fetchCargoOwnerRequests()
                }
                is Resource.Error -> {
                    _requestActionState.value = RequestActionState.Error(result.message ?: "Failed to cancel request")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun fetchRequestDetail(requestId: String) {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            _requestDetailState.value = RequestDetailState.Error("User not authenticated")
            return
        }

        viewModelScope.launch {
            _requestDetailState.value = RequestDetailState.Loading
            when (val result = repository.getRequestById(requestId)) {
                is Resource.Success -> {
                    val req = result.data
                    if (req != null) {
                        if (req.cargoOwnerUid == currentUid || req.truckOwnerUid == currentUid) {
                            _requestDetailState.value = RequestDetailState.Success(req)
                        } else {
                            _requestDetailState.value = RequestDetailState.Error("Unauthorized: You are not authorized to view this booking.")
                        }
                    } else {
                        _requestDetailState.value = RequestDetailState.Error("Transport booking not found")
                    }
                }
                is Resource.Error -> {
                    _requestDetailState.value = RequestDetailState.Error(result.message ?: "Failed to load request details")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun startTransit(request: TransportRequestDto) {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            _requestActionState.value = RequestActionState.Error("User not authenticated")
            return
        }

        if (request.truckOwnerUid != currentUid) {
            _requestActionState.value = RequestActionState.Error("Unauthorized: Only the driver can start transit.")
            return
        }

        if (request.status.uppercase() != "ACCEPTED") {
            _requestActionState.value = RequestActionState.Error("Invalid transition: Booking must be ACCEPTED before starting transit.")
            return
        }

        viewModelScope.launch {
            _requestActionState.value = RequestActionState.Loading
            when (val result = repository.updateRequestLifecycleStatus(request.id ?: "", "ACCEPTED", "IN_TRANSIT")) {
                is Resource.Success -> {
                    _requestActionState.value = RequestActionState.Success
                    sendNotif(
                        recipientUid = request.cargoOwnerUid,
                        title = "Shipment In Transit",
                        body = "Your shipment ${request.cargoTitle} is now on the way.",
                        eventType = "SHIPMENT_IN_TRANSIT",
                        targetId = request.id ?: ""
                    )
                    fetchRequestDetail(request.id ?: "")
                }
                is Resource.Error -> {
                    _requestActionState.value = RequestActionState.Error(result.message ?: "Failed to start transit")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun markDelivered(request: TransportRequestDto) {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            _requestActionState.value = RequestActionState.Error("User not authenticated")
            return
        }

        if (request.truckOwnerUid != currentUid) {
            _requestActionState.value = RequestActionState.Error("Unauthorized: Only the driver can mark delivery.")
            return
        }

        if (request.status.uppercase() != "IN_TRANSIT") {
            _requestActionState.value = RequestActionState.Error("Invalid transition: Shipment must be IN_TRANSIT before marking as delivered.")
            return
        }

        viewModelScope.launch {
            _requestActionState.value = RequestActionState.Loading
            when (val result = repository.updateRequestLifecycleStatus(request.id ?: "", "IN_TRANSIT", "DELIVERED")) {
                is Resource.Success -> {
                    _requestActionState.value = RequestActionState.Success
                    sendNotif(
                        recipientUid = request.cargoOwnerUid,
                        title = "Shipment Delivered",
                        body = "Your shipment ${request.cargoTitle} has been delivered. Please confirm receipt.",
                        eventType = "SHIPMENT_DELIVERED",
                        targetId = request.id ?: ""
                    )
                    fetchRequestDetail(request.id ?: "")
                }
                is Resource.Error -> {
                    _requestActionState.value = RequestActionState.Error(result.message ?: "Failed to mark delivered")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun confirmDeliveryReceipt(request: TransportRequestDto) {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            _requestActionState.value = RequestActionState.Error("User not authenticated")
            return
        }

        if (request.cargoOwnerUid != currentUid) {
            _requestActionState.value = RequestActionState.Error("Unauthorized: Only the cargo owner can confirm delivery.")
            return
        }

        if (request.status.uppercase() != "DELIVERED") {
            _requestActionState.value = RequestActionState.Error("Invalid confirmation: Shipment must be in DELIVERED status.")
            return
        }

        if (request.isConfirmedByOwner) {
            _requestActionState.value = RequestActionState.Error("Delivery has already been confirmed.")
            return
        }

        viewModelScope.launch {
            _requestActionState.value = RequestActionState.Loading
            when (val result = repository.confirmDeliveryReceipt(request.id ?: "", currentUid)) {
                is Resource.Success -> {
                    _requestActionState.value = RequestActionState.Success
                    sendNotif(
                        recipientUid = request.truckOwnerUid,
                        title = "Delivery Confirmed",
                        body = "The cargo owner confirmed delivery receipt for ${request.cargoTitle}.",
                        eventType = "DELIVERY_CONFIRMED",
                        targetId = request.id ?: ""
                    )
                    (_requestDetailState.value as? RequestDetailState.Success)?.let { currentState ->
                        _requestDetailState.value = RequestDetailState.Success(
                            currentState.request.copy(
                                isConfirmedByOwner = true,
                                confirmedAt = java.util.Date()
                            )
                        )
                    }
                    fetchRequestDetail(request.id ?: "")
                }
                is Resource.Error -> {
                    _requestActionState.value = RequestActionState.Error(result.message ?: "Failed to confirm delivery")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun resetActionState() {
        _requestActionState.value = RequestActionState.Idle
    }
}
