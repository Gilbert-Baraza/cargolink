package com.example.cargolink.data.remote

import android.util.Log
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
    private val TAG = "FirestoreDataSource"

    // User Profile methods
    suspend fun saveUserProfile(user: UserDto): Boolean {
        return try {
            val uid = user.firebaseUid ?: throw Exception("Firebase UID is missing")
            usersCollection.document(uid).set(user).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile", e)
            false
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
                trucksCollection.document(truck.id).update(truckMap as Map<String, Any>).await()
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
            
            // IMPORTANT FIX: Removed .orderBy("createdAt") because it requires a composite index 
            // in Firestore when combined with .whereEqualTo(). 
            // Without the index, the query fails silently and returns empty in your previous code.
            val snapshot = trucksCollection
                .whereEqualTo("ownerUid", ownerUid)
                .get()
                .await()
            
            Log.d(TAG, "Query returned ${snapshot.size()} documents")
            
            val trucks = snapshot.toObjects(TruckDto::class.java)
            
            // Sort in memory instead to avoid the mandatory index requirement for now.
            val sortedTrucks = trucks.sortedByDescending { it.createdAt }
            
            Log.d(TAG, "Successfully mapped ${sortedTrucks.size} trucks")
            sortedTrucks
        } catch (e: Exception) {
            Log.e(TAG, "Error getting trucks by owner: ${e.message}", e)
            // Rethrowing or returning empty, but the Log.e will now show the actual error in Logcat.
            throw e
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
}
