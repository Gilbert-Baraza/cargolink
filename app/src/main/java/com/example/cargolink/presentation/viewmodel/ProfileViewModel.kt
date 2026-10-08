package com.example.cargolink.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cargolink.core.common.Resource
import com.example.cargolink.data.models.UserDto
import com.example.cargolink.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val user: UserDto) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            when (val result = userRepository.getProfile()) {
                is Resource.Success -> {
                    if (result.data != null) {
                        _profileState.value = ProfileState.Success(result.data)
                    } else {
                        _profileState.value = ProfileState.Error("Profile data is empty")
                    }
                }
                is Resource.Error -> {
                    _profileState.value = ProfileState.Error(result.message ?: "Failed to load profile")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updateProfile(name: String, phone: String, participationType: String) {
        viewModelScope.launch {
            val currentState = _profileState.value
            if (currentState is ProfileState.Success) {
                val updatedDto = currentState.user.copy(
                    name = name,
                    phone = phone,
                    participationType = participationType
                )
                when (val result = userRepository.updateProfile(updatedDto)) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            _profileState.value = ProfileState.Success(result.data)
                        }
                    }
                    is Resource.Error -> {
                        // handle error if needed
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }
}
