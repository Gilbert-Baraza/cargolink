package com.example.cargolink.data.remote

import android.util.Log
import com.example.cargolink.data.models.CargoDto
import com.example.cargolink.data.models.TruckDto
import com.example.cargolink.data.models.UserDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

class FirestoreDataSource {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val trucksCollection = db.collection("trucks")
    private val cargoCollection = db.collection("cargo")
    private val TAG = "FirestoreDataSource"

    // User Profile methods
    suspend fun saveUserProfile(user: UserDto): Boolean {
        return try {
            val uid = user.firebaseUid ?: throw Exception("Firebase UID is missing")
            usersCollection.document(uid).set(user).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile", e)
            throw e
        }
    }

    suspend fun getUserProfile(uid: String): UserDto? {
        return try {
            val document = usersCollection.document(uid).get().await()
            if (document.exists()) {
                document.toObject(UserDto::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile", e)
            null
        }
    }

    // Truck methods
    suspend fun saveTruck(truck: TruckDto): Boolean {
        return try {
            // Use a Map to ensure fields are named exactly as expected in Firestore
            // and to avoid sending the 'id' field to the database as a field.
            val truckMap = mutableMapOf(
                "ownerUid" to truck.ownerUid,
                "registrationNumber" to truck.registrationNumber,
                "truckType" to truck.truckType,
                "maxPayloadKg" to truck.maxPayloadKg,
                "description" to truck.description,
                "currentLocation" to truck.currentLocation,
                "preferredDestination" to truck.preferredDestination,
                "availabilityDate" to truck.availabilityDate,
                "price" to truck.price,
                "currency" to truck.currency,
                "status" to truck.status,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (truck.id != null && truck.id != "new") {
                trucksCollection.document(truck.id).set(truckMap, com.google.firebase.firestore.SetOptions.merge()).await()
            } else {
                truckMap["createdAt"] = FieldValue.serverTimestamp()
                trucksCollection.add(truckMap).await()
            }
            Log.d(TAG, "Truck saved successfully for owner: ${truck.ownerUid}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving truck", e)
            false
        }
    }

    suspend fun getTrucksByOwner(ownerUid: String): List<TruckDto> {
        return try {
            Log.d(TAG, "Fetching trucks for ownerUid: $ownerUid")
            
            val snapshot = trucksCollection
                .whereEqualTo("ownerUid", ownerUid)
                .get()
                .await()
            
            Log.d(TAG, "Query returned ${snapshot.size()} documents")
            
            val trucks = snapshot.toObjects(TruckDto::class.java)
            
            val sortedTrucks = trucks.sortedByDescending { it.createdAt }
            
            Log.d(TAG, "Successfully mapped ${sortedTrucks.size} trucks")
            sortedTrucks
        } catch (e: Exception) {
            Log.e(TAG, "Error getting trucks by owner: ${e.message}", e)
            throw e
        }
    }

    suspend fun getAllAvailableTrucks(): List<TruckDto> {
        return try {
            val snapshot = trucksCollection
                .whereEqualTo("status", "available")
                .get()
                .await()
            
            val trucks = snapshot.toObjects(TruckDto::class.java)
            trucks.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all available trucks: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getTruckById(truckId: String): TruckDto? {
        return try {
            val document = trucksCollection.document(truckId).get().await()
            if (document.exists()) {
                document.toObject(TruckDto::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting truck by id", e)
            null
        }
    }

    suspend fun deleteTruck(truckId: String): Boolean {
        return try {
            trucksCollection.document(truckId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting truck", e)
            false
        }
    }

    // Cargo methods
    suspend fun saveCargo(cargo: CargoDto): Boolean {
        return try {
            val cargoMap = mutableMapOf<String, Any>(
                "ownerUid" to cargo.ownerUid,
                "cargoType" to cargo.cargoType,
                "weightKg" to cargo.weightKg,
                "pickupLocation" to cargo.pickupLocation,
                "destination" to cargo.destination,
                "currency" to cargo.currency,
                "status" to cargo.status,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            cargo.budget?.let { if (it > 0) cargoMap["budget"] = it }
            cargo.preferredDate?.let { if (it.isNotBlank()) cargoMap["preferredDate"] = it }
            cargo.specialRequirements?.let { if (it.isNotBlank()) cargoMap["specialRequirements"] = it }

            if (cargo.id != null && cargo.id != "new") {
                cargoCollection.document(cargo.id).set(cargoMap, com.google.firebase.firestore.SetOptions.merge()).await()
            } else {
                cargoMap["createdAt"] = FieldValue.serverTimestamp()
                cargoCollection.add(cargoMap).await()
            }
            Log.d(TAG, "Cargo requirement saved successfully for owner: ${cargo.ownerUid}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving cargo requirement for owner: ${cargo.ownerUid}", e)
            throw e
        }
    }

    suspend fun getCargoByOwner(ownerUid: String): List<CargoDto> {
        return try {
            val snapshot = cargoCollection
                .whereEqualTo("ownerUid", ownerUid)
                .get()
                .await()
            val cargoList = snapshot.toObjects(CargoDto::class.java)
            cargoList.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cargo by owner", e)
            emptyList()
        }
    }

    suspend fun getAllAvailableCargo(): List<CargoDto> {
        return try {
            val snapshot = cargoCollection
                .whereEqualTo("status", "available")
                .get()
                .await()
            val cargoList = snapshot.toObjects(CargoDto::class.java)
            cargoList.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all available cargo", e)
            emptyList()
        }
    }

    suspend fun getCargoById(cargoId: String): CargoDto? {
        return try {
            val document = cargoCollection.document(cargoId).get().await()
            if (document.exists()) {
                document.toObject(CargoDto::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cargo by id", e)
            null
        }
    }

    suspend fun deleteCargo(cargoId: String): Boolean {
        return try {
            cargoCollection.document(cargoId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting cargo requirement", e)
            false
        }
    }
}
