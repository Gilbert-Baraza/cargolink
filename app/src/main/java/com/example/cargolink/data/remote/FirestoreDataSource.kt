package com.example.cargolink.data.remote

import android.util.Log
import com.example.cargolink.data.models.CargoDto
import com.example.cargolink.data.models.ConversationDto
import com.example.cargolink.data.models.MessageDto
import com.example.cargolink.data.models.NotificationDto
import com.example.cargolink.data.models.TransportRequestDto
import com.example.cargolink.data.models.TruckDto
import com.example.cargolink.data.models.UserDto
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreDataSource {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val trucksCollection = db.collection("trucks")
    private val cargoCollection = db.collection("cargo")
    private val transportRequestsCollection = db.collection("transportRequests")
    private val conversationsCollection = db.collection("conversations")
    private val notificationsCollection = db.collection("notifications")
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

    suspend fun getUserMetrics(uid: String, isCargoOwner: Boolean): Pair<Int, Int> {
        return try {
            val activeListingsCount = if (isCargoOwner) {
                cargoCollection.whereEqualTo("ownerUid", uid).get().await().size()
            } else {
                trucksCollection.whereEqualTo("ownerUid", uid).get().await().size()
            }

            val completedRequests1 = transportRequestsCollection
                .whereEqualTo("cargoOwnerUid", uid)
                .whereEqualTo("status", "DELIVERED")
                .get()
                .await().size()

            val completedRequests2 = transportRequestsCollection
                .whereEqualTo("truckOwnerUid", uid)
                .whereEqualTo("status", "DELIVERED")
                .get()
                .await().size()

            Pair(activeListingsCount, completedRequests1 + completedRequests2)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user metrics", e)
            Pair(0, 0)
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

    // Transport Request methods
    suspend fun createTransportRequest(request: TransportRequestDto): String {
        return try {
            val requestMap = mutableMapOf<String, Any>(
                "cargoRequestId" to request.cargoRequestId,
                "truckId" to request.truckId,
                "cargoOwnerUid" to request.cargoOwnerUid,
                "truckOwnerUid" to request.truckOwnerUid,
                "cargoTitle" to request.cargoTitle,
                "truckTitle" to request.truckTitle,
                "pickupLocation" to request.pickupLocation,
                "destination" to request.destination,
                "currency" to request.currency,
                "status" to request.status,
                "initiatedBy" to request.initiatedBy.ifBlank { "CARGO_OWNER" },
                "isConfirmedByOwner" to false,
                "updatedAt" to FieldValue.serverTimestamp(),
                "createdAt" to FieldValue.serverTimestamp()
            )
            request.proposedPrice?.let { if (it > 0) requestMap["proposedPrice"] = it }

            val docRef = transportRequestsCollection.add(requestMap).await()
            Log.d(TAG, "Transport request created successfully with ID ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating transport request", e)
            throw e
        }
    }

    suspend fun getRequestsByCargoOwner(cargoOwnerUid: String): List<TransportRequestDto> {
        return try {
            val snapshot = transportRequestsCollection
                .whereEqualTo("cargoOwnerUid", cargoOwnerUid)
                .get()
                .await()
            val requests = snapshot.toObjects(TransportRequestDto::class.java)
            requests.sortedByDescending { it.createdAt ?: it.updatedAt ?: java.util.Date(0) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting requests for cargo owner: $cargoOwnerUid", e)
            emptyList()
        }
    }

    suspend fun getRequestsByTruckOwner(truckOwnerUid: String): List<TransportRequestDto> {
        return try {
            val snapshot = transportRequestsCollection
                .whereEqualTo("truckOwnerUid", truckOwnerUid)
                .get()
                .await()
            val requests = snapshot.toObjects(TransportRequestDto::class.java)
            requests.sortedByDescending { it.createdAt ?: it.updatedAt ?: java.util.Date(0) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting requests for truck owner: $truckOwnerUid", e)
            emptyList()
        }
    }

    suspend fun getRequestById(requestId: String): TransportRequestDto? {
        return try {
            val document = transportRequestsCollection.document(requestId).get().await()
            if (document.exists()) {
                document.toObject(TransportRequestDto::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting request by id: $requestId", e)
            null
        }
    }

    suspend fun updateRequestStatus(requestId: String, newStatus: String): Boolean {
        return try {
            transportRequestsCollection.document(requestId).update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating request status for $requestId", e)
            throw e
        }
    }

    suspend fun updateRequestLifecycleStatus(
        requestId: String,
        expectedCurrentStatus: String,
        targetStatus: String
    ): Boolean {
        return try {
            db.runTransaction { transaction ->
                val ref = transportRequestsCollection.document(requestId)
                val doc = transaction.get(ref)

                if (!doc.exists()) {
                    throw Exception("Transport request not found.")
                }

                val currentStatus = doc.getString("status") ?: ""
                if (currentStatus != expectedCurrentStatus) {
                    throw Exception("Invalid transition: Request status is '$currentStatus', expected '$expectedCurrentStatus'.")
                }

                transaction.update(ref, mapOf(
                    "status" to targetStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                null
            }.await()
            Log.d(TAG, "Transport request $requestId status updated from $expectedCurrentStatus to $targetStatus")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating lifecycle status for $requestId", e)
            throw e
        }
    }

    suspend fun confirmDeliveryReceipt(requestId: String, cargoOwnerUid: String): Boolean {
        return try {
            db.runTransaction { transaction ->
                val ref = transportRequestsCollection.document(requestId)
                val doc = transaction.get(ref)

                if (!doc.exists()) {
                    throw Exception("Transport request not found.")
                }

                val reqOwnerUid = doc.getString("cargoOwnerUid")
                if (reqOwnerUid != cargoOwnerUid) {
                    throw Exception("Unauthorized: Only the cargo owner can confirm delivery.")
                }

                val currentStatus = doc.getString("status") ?: ""
                if (currentStatus.uppercase() != "DELIVERED") {
                    throw Exception("Invalid confirmation: Shipment must be in DELIVERED status.")
                }

                val isAlreadyConfirmed = doc.getBoolean("isConfirmedByOwner") ?: false
                if (isAlreadyConfirmed) {
                    throw Exception("Delivery receipt has already been confirmed.")
                }

                transaction.update(ref, mapOf(
                    "isConfirmedByOwner" to true,
                    "confirmedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                null
            }.await()
            Log.d(TAG, "Transport request $requestId delivery confirmed by cargo owner $cargoOwnerUid")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error confirming delivery for request $requestId", e)
            throw e
        }
    }

    suspend fun hasActiveRequestForTruck(cargoOwnerUid: String, truckId: String): Boolean {
        return try {
            val snapshot = transportRequestsCollection
                .whereEqualTo("cargoOwnerUid", cargoOwnerUid)
                .whereEqualTo("truckId", truckId)
                .get()
                .await()
            val requests = snapshot.toObjects(TransportRequestDto::class.java)
            requests.any { it.status == "PENDING" || it.status == "ACCEPTED" }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking active request for truck", e)
            false
        }
    }

    suspend fun acceptTransportRequest(request: TransportRequestDto): Boolean {
        val reqId = request.id ?: throw Exception("Request ID is missing")
        return try {
            db.runTransaction { transaction ->
                // 1. All Reads First
                val requestRef = transportRequestsCollection.document(reqId)
                val requestDoc = transaction.get(requestRef)

                if (!requestDoc.exists() || requestDoc.getString("status") != "PENDING") {
                    throw Exception("Transport request is no longer pending.")
                }

                val targetCargoId = requestDoc.getString("cargoRequestId") ?: request.cargoRequestId
                val targetTruckId = requestDoc.getString("truckId") ?: request.truckId

                val cargoRef = if (targetCargoId.isNotBlank()) cargoCollection.document(targetCargoId) else null
                val cargoDoc = cargoRef?.let { transaction.get(it) }

                val truckRef = if (targetTruckId.isNotBlank()) trucksCollection.document(targetTruckId) else null
                val truckDoc = truckRef?.let { transaction.get(it) }

                // 2. Validate Read Snapshots
                if (cargoDoc != null && (!cargoDoc.exists() || cargoDoc.getString("status")?.lowercase() != "available")) {
                    throw Exception("Selected cargo requirement is no longer available.")
                }

                if (truckDoc != null && (!truckDoc.exists() || truckDoc.getString("status")?.lowercase() != "available")) {
                    throw Exception("Selected truck is no longer available.")
                }

                // 3. All Writes After Reads
                if (cargoRef != null) {
                    transaction.update(cargoRef, mapOf(
                        "status" to "booked",
                        "updatedAt" to FieldValue.serverTimestamp()
                    ))
                }

                if (truckRef != null) {
                    transaction.update(truckRef, mapOf(
                        "status" to "inactive",
                        "updatedAt" to FieldValue.serverTimestamp()
                    ))
                }

                transaction.update(requestRef, mapOf(
                    "status" to "ACCEPTED",
                    "updatedAt" to FieldValue.serverTimestamp()
                ))

                null
            }.await()

            Log.d(TAG, "Transport request $reqId accepted successfully in transaction")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error accepting transport request $reqId in transaction", e)
            throw e
        }
    }

    // In-App Messaging Methods
    suspend fun getOrCreateConversation(request: TransportRequestDto, currentUid: String): String {
        val reqId = request.id ?: throw Exception("Request ID is missing")
        val conversationId = "conv_$reqId"
        val docRef = conversationsCollection.document(conversationId)

        val doc = docRef.get().await()
        if (doc.exists()) {
            return conversationId
        }

        if (currentUid != request.cargoOwnerUid && currentUid != request.truckOwnerUid) {
            throw Exception("Unauthorized: You are not a participant in this booking.")
        }

        if (request.cargoOwnerUid == request.truckOwnerUid) {
            throw Exception("Self-conversations are not allowed.")
        }

        val conversationMap = mutableMapOf<String, Any>(
            "requestId" to reqId,
            "cargoOwnerUid" to request.cargoOwnerUid,
            "truckOwnerUid" to request.truckOwnerUid,
            "cargoTitle" to request.cargoTitle,
            "truckTitle" to request.truckTitle,
            "lastMessage" to "Conversation initiated",
            "lastSenderUid" to currentUid,
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        docRef.set(conversationMap).await()
        Log.d(TAG, "Conversation $conversationId created successfully")
        return conversationId
    }

    fun getUserConversationsFlow(currentUid: String): Flow<List<ConversationDto>> = callbackFlow {
        val listenerCargo = conversationsCollection
            .whereEqualTo("cargoOwnerUid", currentUid)
            .addSnapshotListener { snapshot1, error1 ->
                if (error1 != null) {
                    Log.e(TAG, "Error listening to cargo owner conversations", error1)
                    return@addSnapshotListener
                }

                val conversations1 = snapshot1?.toObjects(ConversationDto::class.java) ?: emptyList()

                conversationsCollection
                    .whereEqualTo("truckOwnerUid", currentUid)
                    .get()
                    .addOnSuccessListener { snapshot2 ->
                        val conversations2 = snapshot2?.toObjects(ConversationDto::class.java) ?: emptyList()
                        val combined = (conversations1 + conversations2)
                            .distinctBy { it.id }
                            .sortedByDescending { it.updatedAt ?: it.createdAt ?: java.util.Date(0) }
                        trySend(combined)
                    }
            }

        awaitClose {
            listenerCargo.remove()
        }
    }

    fun getMessagesFlow(conversationId: String): Flow<List<MessageDto>> = callbackFlow {
        val listener = conversationsCollection
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to messages for $conversationId", error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.toObjects(MessageDto::class.java) ?: emptyList()
                trySend(messages)
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun sendMessage(
        conversationId: String,
        senderUid: String,
        senderName: String,
        messageText: String
    ): Boolean {
        val trimmedText = messageText.trim()
        if (trimmedText.isBlank()) {
            throw Exception("Message text cannot be empty.")
        }

        val convRef = conversationsCollection.document(conversationId)
        val convDoc = convRef.get().await()

        if (convDoc.exists()) {
            val cargoOwnerUid = convDoc.getString("cargoOwnerUid") ?: ""
            val truckOwnerUid = convDoc.getString("truckOwnerUid") ?: ""

            if (senderUid != cargoOwnerUid && senderUid != truckOwnerUid) {
                throw Exception("Unauthorized: You are not a participant in this conversation.")
            }
        }

        val messageMap = mutableMapOf<String, Any>(
            "conversationId" to conversationId,
            "senderUid" to senderUid,
            "senderName" to senderName,
            "messageText" to trimmedText,
            "timestamp" to FieldValue.serverTimestamp()
        )

        val messagesCollection = convRef.collection("messages")
        messagesCollection.add(messageMap).await()

        if (convDoc.exists()) {
            convRef.update(
                mapOf(
                    "lastMessage" to trimmedText,
                    "lastSenderUid" to senderUid,
                    "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        Log.d(TAG, "Message sent to conversation $conversationId by $senderUid")
        return true
    }

    // FCM Token Methods
    suspend fun saveFcmToken(userId: String, token: String): Boolean {
        return try {
            val userTokenRef = usersCollection.document(userId).collection("fcmTokens").document(token.takeLast(16))
            val tokenMap = mapOf(
                "token" to token,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userTokenRef.set(tokenMap).await()
            Log.d(TAG, "Saved FCM token for user $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving FCM token for $userId", e)
            false
        }
    }

    // Notification Methods
    suspend fun createNotification(notification: NotificationDto): Boolean {
        return try {
            if (notification.recipientUid.isBlank() || notification.recipientUid == notification.senderUid) {
                return false
            }

            val notificationMap = mutableMapOf<String, Any>(
                "recipientUid" to notification.recipientUid,
                "senderUid" to notification.senderUid,
                "title" to notification.title,
                "body" to notification.body,
                "eventType" to notification.eventType,
                "targetId" to notification.targetId,
                "isRead" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )

            notificationsCollection.add(notificationMap).await()
            Log.d(TAG, "Created in-app notification for recipient ${notification.recipientUid}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating in-app notification", e)
            false
        }
    }

    fun getNotificationsFlow(recipientUid: String): Flow<List<NotificationDto>> = callbackFlow {
        val listener = notificationsCollection
            .whereEqualTo("recipientUid", recipientUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to notifications for $recipientUid", error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(NotificationDto::class.java) ?: emptyList()
                val sorted = list.sortedByDescending { it.createdAt ?: java.util.Date(0) }
                trySend(sorted)
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun markNotificationAsRead(notificationId: String): Boolean {
        return try {
            notificationsCollection.document(notificationId).update(
                mapOf(
                    "isRead" to true
                )
            ).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as read: $notificationId", e)
            false
        }
    }
}
