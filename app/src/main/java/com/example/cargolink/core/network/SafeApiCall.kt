package com.example.cargolink.core.network

import com.example.cargolink.core.common.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): Resource<T> {
    return withContext(Dispatchers.IO) {
        try {
            Resource.Success(apiCall())
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> Resource.Error("Network error. Please check your internet connection.")
                is HttpException -> {
                    val code = throwable.code()
                    val errorMsg = throwable.localizedMessage ?: "Unknown error"
                    Resource.Error("Error $code: $errorMsg")
                }
                else -> Resource.Error(throwable.localizedMessage ?: "An unexpected error occurred.")
            }
        }
    }
}
