package com.example.cargolink

import com.example.cargolink.core.common.AppErrorMapper
import org.junit.Assert.*
import org.junit.Test

class AppErrorMapperTest {

    private class CustomCollisionException : Exception("ERROR_EMAIL_ALREADY_IN_USE")
    private class CustomWeakPasswordException : Exception("Weak password")
    private class CustomInvalidCredentialsException : Exception("ERROR_INVALID_CREDENTIALS")
    private class CustomInvalidUserException : Exception("ERROR_USER_NOT_FOUND")
    private class CustomNetworkException : Exception("Network connection lost")
    private class CustomFirestoreException : Exception("PERMISSION_DENIED: Missing permissions")

    @Test
    fun map_userCollision_returnsExistingAccountMessage() {
        val ex = CustomCollisionException()
        val result = AppErrorMapper.map(ex)
        assertTrue(result.contains("already exists"))
    }

    @Test
    fun map_invalidCredentials_returnsCredentialsMessage() {
        val ex = CustomInvalidCredentialsException()
        val result = AppErrorMapper.map(ex)
        assertTrue(result.contains("Invalid email or password"))
    }

    @Test
    fun map_invalidUser_returnsAccountNotFoundMessage() {
        val ex = CustomInvalidUserException()
        val result = AppErrorMapper.map(ex)
        assertTrue(result.contains("No account found"))
    }

    @Test
    fun map_weakPassword_returnsWeakPasswordMessage() {
        val ex = CustomWeakPasswordException()
        val result = AppErrorMapper.map(ex)
        assertTrue(result.contains("weak"))
    }

    @Test
    fun map_networkException_returnsNetworkMessage() {
        val ex = CustomNetworkException()
        val result = AppErrorMapper.map(ex)
        assertTrue(result.contains("Network error"))
    }

    @Test
    fun map_firestorePermissionDenied_returnsPermissionMessage() {
        val ex = CustomFirestoreException()
        val result = AppErrorMapper.map(ex)
        assertTrue(result.contains("Permission denied"))
    }

    @Test
    fun map_nullException_returnsDefaultMessage() {
        val result = AppErrorMapper.map(null, "Default fallback")
        assertEquals("Default fallback", result)
    }
}
