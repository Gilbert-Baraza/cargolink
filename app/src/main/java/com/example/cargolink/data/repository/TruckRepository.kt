package com.example.cargolink.data.repository

import com.example.cargolink.core.common.Resource
import com.example.cargolink.data.models.TruckDto
import com.example.cargolink.data.remote.FirestoreDataSource

class TruckRepository {
    private val firestoreDataSource = FirestoreDataSource()

    suspend fun getMyTrucks(ownerUid: String): Resource<List<TruckDto>> {
        return try {
            val trucks = firestoreDataSource.getTrucksByOwner(ownerUid)
            Resource.Success(trucks)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch trucks")
        }
    }

    suspend fun getAllAvailableTrucks(): Resource<List<TruckDto>> {
        return try {
            val trucks = firestoreDataSource.getAllAvailableTrucks()
            Resource.Success(trucks)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch available trucks")
        }
    }

    suspend fun getTruckById(truckId: String): Resource<TruckDto> {
        return try {
            val truck = firestoreDataSource.getTruckById(truckId)
            if (truck != null) {
                Resource.Success(truck)
            } else {
                Resource.Error("Truck not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch truck details")
        }
    }

    suspend fun saveTruck(truck: TruckDto): Resource<Unit> {
        return try {
            val success = firestoreDataSource.saveTruck(truck)
            if (success) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to save truck to Firestore")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save truck")
        }
    }

    suspend fun deleteTruck(truckId: String): Resource<Unit> {
        return try {
            val success = firestoreDataSource.deleteTruck(truckId)
            if (success) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to delete truck")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete truck")
        }
    }
}
