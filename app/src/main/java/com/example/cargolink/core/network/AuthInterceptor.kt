package com.example.cargolink.core.network

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking {
            try {
                currentUser.getIdToken(false).await().token
            } catch (e: Exception) {
                null
            }
        }

        val authenticatedRequest = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(authenticatedRequest)
    }
}
