package com.example.cargolink.data.remote

import com.example.cargolink.data.models.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface ApiService {
    @GET("api/users/me/")
    suspend fun getProfile(): Response<UserDto>

    @POST("api/users/me/")
    suspend fun createProfile(@Body user: UserDto): Response<UserDto>

    @PATCH("api/users/me/")
    suspend fun updateProfile(@Body user: UserDto): Response<UserDto>
}
