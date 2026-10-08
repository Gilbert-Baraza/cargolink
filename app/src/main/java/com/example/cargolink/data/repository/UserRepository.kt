package com.example.cargolink.data.repository

import com.example.cargolink.core.common.Resource
import com.example.cargolink.core.network.RetrofitClient
import com.example.cargolink.core.network.safeApiCall
import com.example.cargolink.data.models.UserDto

class UserRepository {
    private val api = RetrofitClient.apiService

    suspend fun getProfile(): Resource<UserDto> {
        return safeApiCall {
            val response = api.getProfile()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception(response.errorBody()?.string() ?: "Failed to fetch profile")
            }
        }
    }

    suspend fun createProfile(userDto: UserDto): Resource<UserDto> {
        return safeApiCall {
            val response = api.createProfile(userDto)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception(response.errorBody()?.string() ?: "Failed to create profile")
            }
        }
    }

    suspend fun updateProfile(userDto: UserDto): Resource<UserDto> {
        return safeApiCall {
            val response = api.updateProfile(userDto)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception(response.errorBody()?.string() ?: "Failed to update profile")
            }
        }
    }
}
