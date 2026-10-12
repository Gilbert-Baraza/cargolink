package com.example.cargolink.data.repository

import com.example.cargolink.core.common.Resource
import com.example.cargolink.data.models.CargoDto
import com.example.cargolink.data.remote.FirestoreDataSource

class CargoRepository {
    private val firestoreDataSource = FirestoreDataSource()

    suspend fun getMyCargo(ownerUid: String): Resource<List<CargoDto>> {
        return try {
            val cargoList = firestoreDataSource.getCargoByOwner(ownerUid)
            Resource.Success(cargoList)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch cargo listings")
        }
    }

    suspend fun getAllAvailableCargo(): Resource<List<CargoDto>> {
        return try {
            val cargoList = firestoreDataSource.getAllAvailableCargo()
            Resource.Success(cargoList)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch available cargo")
        }
    }

    suspend fun getCargoById(cargoId: String): Resource<CargoDto> {
        return try {
            val cargo = firestoreDataSource.getCargoById(cargoId)
            if (cargo != null) {
                Resource.Success(cargo)
            } else {
                Resource.Error("Cargo requirement not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch cargo details")
        }
    }

    suspend fun saveCargo(cargo: CargoDto): Resource<Unit> {
        return try {
            val success = firestoreDataSource.saveCargo(cargo)
            if (success) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to save cargo to Firestore")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save cargo requirement")
        }
    }

    suspend fun deleteCargo(cargoId: String): Resource<Unit> {
        return try {
            val success = firestoreDataSource.deleteCargo(cargoId)
            if (success) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to delete cargo requirement")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete cargo requirement")
        }
    }
}
