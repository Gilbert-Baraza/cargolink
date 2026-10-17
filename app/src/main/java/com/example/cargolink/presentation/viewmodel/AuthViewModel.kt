package com.example.cargolink.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cargolink.core.common.AppErrorMapper
import com.example.cargolink.core.common.Resource
import com.example.cargolink.data.models.UserDto
import com.example.cargolink.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
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
                        _authState.value = AuthState.ProfileIncomplete(currentUser.uid, currentUser.email)
                    }
                    is Resource.Loading -> {}
                }
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun login(email: String, pass: String) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        if (trimmedEmail.isBlank() || trimmedPass.isBlank()) {
            _authState.value = AuthState.Error("Please enter your email and password.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading("Logging in...")
            try {
                val result = auth.signInWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                val uid = result.user?.uid
                if (uid != null) {
                    when (val profileResult = userRepository.getProfile(uid)) {
                        is Resource.Success -> {
                            _authState.value = AuthState.Authenticated
                        }
                        is Resource.Error -> {
                            _authState.value = AuthState.ProfileIncomplete(uid, trimmedEmail)
                        }
                        is Resource.Loading -> {}
                    }
                } else {
                    _authState.value = AuthState.Error("Failed to retrieve user credentials.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(AppErrorMapper.map(e, "Login failed. Please check your credentials."))
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
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()
        val trimmedRole = participationType.trim()
        val trimmedCompany = companyName?.trim()

        viewModelScope.launch {
            _authState.value = AuthState.Loading("Creating account...")
            try {
                // If there's an existing authenticated user whose profile was incomplete
                val currentUser = auth.currentUser
                val firebaseUid = if (currentUser != null && currentUser.email == trimmedEmail) {
                    currentUser.uid
                } else {
                    val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, pass).await()
                    authResult.user?.uid ?: throw Exception("Failed to retrieve user ID")
                }

                val userDto = UserDto(
                    firebaseUid = firebaseUid,
                    name = trimmedName,
                    email = trimmedEmail,
                    phone = trimmedPhone,
                    participationType = trimmedRole,
                    ownerKind = ownerKind,
                    companyName = trimmedCompany,
                    accountStatus = "ACTIVE",
                    createdAt = System.currentTimeMillis().toString()
                )

                when (val result = userRepository.createProfile(userDto)) {
                    is Resource.Success -> {
                        auth.signOut()
                        _authState.value = AuthState.RegistrationSuccess
                    }
                    is Resource.Error -> {
                        _authState.value = AuthState.ProfileIncomplete(firebaseUid, trimmedEmail)
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: FirebaseAuthUserCollisionException) {
                _authState.value = AuthState.Error("An account already exists with this email address. Please log in or reset your password.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(AppErrorMapper.map(e, "Registration failed. Please try again."))
            }
        }
    }

    fun completeProfile(
        name: String,
        phone: String,
        participationType: String,
        ownerKind: String?,
        companyName: String?
    ) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid
        val email = currentUser?.email ?: ""

        if (uid == null) {
            _authState.value = AuthState.Error("User session expired. Please log in.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading("Saving profile details...")
            val userDto = UserDto(
                firebaseUid = uid,
                name = name.trim(),
                email = email.trim(),
                phone = phone.trim(),
                participationType = participationType.trim(),
                ownerKind = ownerKind,
                companyName = companyName?.trim(),
                accountStatus = "ACTIVE",
                createdAt = System.currentTimeMillis().toString()
            )

            when (val result = userRepository.createProfile(userDto)) {
                is Resource.Success -> {
                    _authState.value = AuthState.Authenticated
                }
                is Resource.Error -> {
                    _authState.value = AuthState.Error(result.message ?: "Failed to save profile. Please try again.")
                }
                is Resource.Loading -> {}
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
