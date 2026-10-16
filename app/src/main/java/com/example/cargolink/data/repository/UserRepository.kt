package com.example.cargolink.data.repository

import com.example.cargolink.core.common.Resource
import com.example.cargolink.data.models.UserDto
import com.example.cargolink.data.remote.FirestoreDataSource

class UserRepository {
    private val firestoreDataSource = FirestoreDataSource()

    suspend fun getProfile(uid: String): Resource<UserDto> {
        return try {
            val user = firestoreDataSource.getUserProfile(uid)
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("Profile not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch profile")
        }
    }

    suspend fun createProfile(userDto: UserDto): Resource<UserDto> {
        return try {
            val success = firestoreDataSource.saveUserProfile(userDto)
            if (success) {
                Resource.Success(userDto)
            } else {
                Resource.Error("Failed to save profile to Firestore")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to create profile")
        }
    }

    suspend fun updateProfile(userDto: UserDto): Resource<UserDto> {
        // Reuse createProfile as Firestore .set() handles updates (merging or overwriting)
        return createProfile(userDto)
    }

    suspend fun getUserMetrics(uid: String, isCargoOwner: Boolean): Pair<Int, Int> {
        return firestoreDataSource.getUserMetrics(uid, isCargoOwner)
    }
}
