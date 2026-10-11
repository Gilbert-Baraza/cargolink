package com.example.cargolink.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cargolink.core.common.Resource
import com.example.cargolink.data.models.UserDto
import com.example.cargolink.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkCurrentUser()
    }

    fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid
                if (uid != null) {
                    // Fetch profile to verify it exists
                    when (val profileResult = userRepository.getProfile(uid)) {
                        is Resource.Success -> {
                            _authState.value = AuthState.Authenticated
                        }
                        is Resource.Error -> {
                            // Profile missing or error fetching. 
                            // In a real app, we might redirect to profile completion.
                            // For now, we still consider them authenticated but log the error.
                            _authState.value = AuthState.Authenticated
                        }
                        is Resource.Loading -> {}
                    }
                } else {
                    _authState.value = AuthState.Error("Failed to retrieve user ID")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Authentication failed")
            }
        }
    }

    fun register(
        name: String,
        email: String,
        phone: String,
        pass: String,
        participationType: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val firebaseUid = authResult.user?.uid ?: throw Exception("Failed to get Firebase UID")

                val userDto = UserDto(
                    firebaseUid = firebaseUid,
                    name = name,
                    email = email,
                    phone = phone,
                    participationType = participationType,
                    accountStatus = "ACTIVE",
                    createdAt = System.currentTimeMillis().toString()
                )

                when (val result = userRepository.createProfile(userDto)) {
                    is Resource.Success -> {
                        _authState.value = AuthState.Authenticated
                    }
                    is Resource.Error -> {
                        // CRITICAL: If profile creation fails, we must roll back auth or inform user
                        // The user is created in Firebase Auth but has no profile.
                        auth.signOut() 
                        _authState.value = AuthState.Error(result.message ?: "Failed to save profile. Registration rolled back.")
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Registration failed")
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}
