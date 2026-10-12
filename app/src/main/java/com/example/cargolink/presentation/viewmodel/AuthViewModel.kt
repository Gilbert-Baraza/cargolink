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
    data class Loading(val message: String = "Preparing CargoLink…") : AuthState()
    object Authenticated : AuthState()
    data class ProfileIncomplete(val uid: String, val email: String?) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
    object RegistrationSuccess : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val userRepository = UserRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkCurrentUser()
    }

    fun checkCurrentUser() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                _authState.value = AuthState.Loading("Checking session...")
                when (val profileResult = userRepository.getProfile(currentUser.uid)) {
                    is Resource.Success -> {
                        _authState.value = AuthState.Authenticated
                    }
                    is Resource.Error -> {
                        auth.signOut()
                        _authState.value = AuthState.Unauthenticated
                    }
                    is Resource.Loading -> {}
                }
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading("Logging in...")
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid
                if (uid != null) {
                    when (val profileResult = userRepository.getProfile(uid)) {
                        is Resource.Success -> {
                            _authState.value = AuthState.Authenticated
                        }
                        is Resource.Error -> {
                            _authState.value = AuthState.Error("Profile not found. Please register an account.")
                        }
                        is Resource.Loading -> {}
                    }
                } else {
                    _authState.value = AuthState.Error("Failed to retrieve user ID")
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid email or password. Please check your credentials."
                    is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "No account found with this email. Please register first."
                    is com.google.firebase.FirebaseNetworkException -> "Network error. Please check your internet connection and try again."
                    else -> "Login failed: ${e.localizedMessage}"
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun register(
        name: String,
        email: String,
        phone: String,
        pass: String,
        participationType: String,
        ownerKind: String?,
        companyName: String?
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading("Creating account...")
            try {
                // If there's an existing user (e.g. from ProfileIncomplete state), don't create new auth
                val currentUser = auth.currentUser
                val firebaseUid = if (currentUser != null && currentUser.email == email) {
                    currentUser.uid
                } else {
                    val authResult = try {
                        auth.createUserWithEmailAndPassword(email, pass).await()
                    } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                        // Attempt sign in to recover missing profile for existing auth account
                        auth.signInWithEmailAndPassword(email, pass).await()
                    }
                    authResult.user?.uid ?: throw Exception("Failed to get Firebase UID")
                }

                val userDto = UserDto(
                    firebaseUid = firebaseUid,
                    name = name,
                    email = email,
                    phone = phone,
                    participationType = participationType,
                    ownerKind = ownerKind,
                    companyName = companyName,
                    accountStatus = "ACTIVE",
                    createdAt = System.currentTimeMillis().toString()
                )

                when (val result = userRepository.createProfile(userDto)) {
                    is Resource.Success -> {
                        auth.signOut()
                        _authState.value = AuthState.RegistrationSuccess
                    }
                    is Resource.Error -> {
                        _authState.value = AuthState.Error("Profile creation failed: ${result.message}. Please check details and try again.")
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "An account already exists with this email address."
                    is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> "Password is too weak. Please use a stronger password."
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                    is com.google.firebase.FirebaseNetworkException -> "Network error. Please check your internet connection and try again."
                    else -> "Registration failed: ${e.localizedMessage}. Please try again."
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun resetState() {
        _authState.value = AuthState.Unauthenticated
    }
}
