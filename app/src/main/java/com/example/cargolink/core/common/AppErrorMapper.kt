package com.example.cargolink.core.common

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException

object AppErrorMapper {

    fun map(throwable: Throwable?, defaultMessage: String = "An unexpected error occurred. Please try again."): String {
        if (throwable == null) return defaultMessage

        val className = throwable.javaClass.simpleName
        val msg = throwable.localizedMessage ?: throwable.message ?: ""

        if (throwable is FirebaseAuthUserCollisionException || className.contains("Collision") || msg.contains("ALREADY_IN_USE") || msg.contains("already exists")) {
            return "An account already exists with this email address. Please log in or reset your password."
        }

        if (throwable is FirebaseAuthWeakPasswordException || className.contains("WeakPassword")) {
            return "The password provided is too weak. Please use a stronger password with at least 6 characters."
        }

        if (throwable is FirebaseAuthInvalidCredentialsException || className.contains("InvalidCredentials") || msg.contains("INVALID_CREDENTIALS")) {
            return "Invalid email or password. Please check your credentials and try again."
        }

        if (throwable is FirebaseAuthInvalidUserException || className.contains("InvalidUser") || msg.contains("USER_NOT_FOUND")) {
            return "No account found with this email address. Please register a new account."
        }

        if (throwable is FirebaseAuthActionCodeException || className.contains("ActionCode")) {
            return "The action code or link is invalid or expired. Please request a new link."
        }

        if (throwable is FirebaseAuthRecentLoginRequiredException || className.contains("RecentLogin")) {
            return "For security reasons, please log out and log back in to perform this action."
        }

        if (throwable is FirebaseNetworkException || className.contains("Network") || msg.lowercase().contains("network")) {
            return "Network error. Please check your internet connection and try again."
        }

        if (throwable is FirebaseFirestoreException || className.contains("Firestore")) {
            if (msg.contains("PERMISSION_DENIED") || msg.lowercase().contains("permission")) {
                return "Permission denied. You are not authorized to perform this action."
            }
            if (msg.contains("NOT_FOUND")) {
                return "The requested record was not found or has been removed."
            }
            return "Database operation failed. Please try again."
        }

        if (msg.isNotBlank() && !msg.contains("Exception") && !msg.contains("com.google")) {
            return msg
        }

        return defaultMessage
    }
}
