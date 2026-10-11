package com.example.cargolink.data.models

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("firebase_uid") val firebaseUid: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("participation_type") val participationType: String = "",
    @SerializedName("account_status") val accountStatus: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)
