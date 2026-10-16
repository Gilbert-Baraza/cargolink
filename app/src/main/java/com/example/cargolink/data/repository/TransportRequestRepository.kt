package com.example.cargolink.data.repository

import com.example.cargolink.core.common.Resource
import com.example.cargolink.data.models.TransportRequestDto
import com.example.cargolink.data.remote.FirestoreDataSource

class TransportRequestRepository {
    private val firestoreDataSource = FirestoreDataSource()

    suspend fun createTransportRequest(request: TransportRequestDto): Resource<String> {
        return try {
            val createdId = firestoreDataSource.createTransportRequest(request)
            Resource.Success(createdId)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to create transport request")
        }
    }

    suspend fun getRequestsByCargoOwner(cargoOwnerUid: String): Resource<List<TransportRequestDto>> {
        return try {
            val requests = firestoreDataSource.getRequestsByCargoOwner(cargoOwnerUid)
            Resource.Success(requests)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch cargo owner requests")
        }
    }

    suspend fun getRequestsByTruckOwner(truckOwnerUid: String): Resource<List<TransportRequestDto>> {
        return try {
            val requests = firestoreDataSource.getRequestsByTruckOwner(truckOwnerUid)
            Resource.Success(requests)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch truck owner requests")
        }
    }

    suspend fun getRequestById(requestId: String): Resource<TransportRequestDto> {
        return try {
            val request = firestoreDataSource.getRequestById(requestId)
            if (request != null) {
                Resource.Success(request)
            } else {
                Resource.Error("Transport request not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch request details")
        }
    }

    suspend fun updateRequestStatus(requestId: String, newStatus: String): Resource<Unit> {
        return try {
            val success = firestoreDataSource.updateRequestStatus(requestId, newStatus)
            if (success) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to update request status")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update request status")
        }
    }

    suspend fun acceptTransportRequest(request: TransportRequestDto): Resource<Unit> {
        return try {
            val success = firestoreDataSource.acceptTransportRequest(request)
            if (success) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to accept transport request")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to accept transport request")
        }
    }

    suspend fun hasActiveRequestForTruck(cargoOwnerUid: String, truckId: String): Resource<Boolean> {
        return try {
            val exists = firestoreDataSource.hasActiveRequestForTruck(cargoOwnerUid, truckId)
            Resource.Success(exists)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to check existing requests")
        }
    }

    suspend fun updateRequestLifecycleStatus(
        requestId: String,
        expectedCurrentStatus: String,
        targetStatus: String
    ): Resource<Unit> {
        return try {
            val success = firestoreDataSource.updateRequestLifecycleStatus(requestId, expectedCurrentStatus, targetStatus)
            if (success) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to update status transition")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update status transition")
        }
    }

    suspend fun confirmDeliveryReceipt(requestId: String, cargoOwnerUid: String): Resource<Unit> {
        return try {
            val success = firestoreDataSource.confirmDeliveryReceipt(requestId, cargoOwnerUid)
            if (success) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to confirm delivery receipt")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to confirm delivery receipt")
        }
    }
}
