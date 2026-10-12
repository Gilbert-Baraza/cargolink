package com.example.cargolink.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cargolink.core.common.Resource
import com.example.cargolink.data.models.TruckDto
import com.example.cargolink.data.repository.TruckRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TruckListState {
    object Loading : TruckListState()
    data class Success(val trucks: List<TruckDto>) : TruckListState()
    data class Error(val message: String) : TruckListState()
}

sealed class TruckDetailState {
    object Idle : TruckDetailState()
    object Loading : TruckDetailState()
    data class Success(val truck: TruckDto) : TruckDetailState()
    data class Error(val message: String) : TruckDetailState()
}

sealed class TruckActionState {
    object Idle : TruckActionState()
    object Loading : TruckActionState()
    object Success : TruckActionState()
    data class Error(val message: String) : TruckActionState()
}

class TruckViewModel : ViewModel() {
    private val truckRepository = TruckRepository()
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val _truckListState = MutableStateFlow<TruckListState>(TruckListState.Loading)
    val truckListState: StateFlow<TruckListState> = _truckListState.asStateFlow()

    private val _marketplaceTrucksState = MutableStateFlow<TruckListState>(TruckListState.Loading)
    val marketplaceTrucksState: StateFlow<TruckListState> = _marketplaceTrucksState.asStateFlow()

    private val _truckDetailState = MutableStateFlow<TruckDetailState>(TruckDetailState.Idle)
    val truckDetailState: StateFlow<TruckDetailState> = _truckDetailState.asStateFlow()

    private val _truckActionState = MutableStateFlow<TruckActionState>(TruckActionState.Idle)
    val truckActionState: StateFlow<TruckActionState> = _truckActionState.asStateFlow()

    fun fetchMyTrucks() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _truckListState.value = TruckListState.Loading
            when (val result = truckRepository.getMyTrucks(uid)) {
                is Resource.Success -> {
                    _truckListState.value = TruckListState.Success(result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _truckListState.value = TruckListState.Error(result.message ?: "Unknown error")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun fetchMarketplaceTrucks() {
        viewModelScope.launch {
            _marketplaceTrucksState.value = TruckListState.Loading
            when (val result = truckRepository.getAllAvailableTrucks()) {
                is Resource.Success -> {
                    _marketplaceTrucksState.value = TruckListState.Success(result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _marketplaceTrucksState.value = TruckListState.Error(result.message ?: "Unknown error")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun fetchTruckDetail(truckId: String) {
        viewModelScope.launch {
            _truckDetailState.value = TruckDetailState.Loading
            when (val result = truckRepository.getTruckById(truckId)) {
                is Resource.Success -> {
                    _truckDetailState.value = TruckDetailState.Success(result.data!!)
                }
                is Resource.Error -> {
                    _truckDetailState.value = TruckDetailState.Error(result.message ?: "Truck not found")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun saveTruck(
        id: String?,
        registrationNumber: String,
        truckType: String,
        maxPayloadKg: Double,
        currentLocation: String,
        preferredDestination: String? = null,
        description: String? = null,
        price: Double? = null,
        status: String = "available"
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _truckActionState.value = TruckActionState.Error("User not authenticated")
            return
        }
        viewModelScope.launch {
            _truckActionState.value = TruckActionState.Loading
            val truck = TruckDto(
                id = id,
                ownerUid = uid,
                registrationNumber = registrationNumber,
                truckType = truckType,
                maxPayloadKg = maxPayloadKg,
                currentLocation = currentLocation,
                preferredDestination = preferredDestination,
                price = price,
                description = description,
                status = status
            )
            when (val result = truckRepository.saveTruck(truck)) {
                is Resource.Success -> {
                    _truckActionState.value = TruckActionState.Success
                    fetchMyTrucks()
                }
                is Resource.Error -> {
                    _truckActionState.value = TruckActionState.Error(result.message ?: "Failed to save truck")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteTruck(truckId: String) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _truckActionState.value = TruckActionState.Error("User not authenticated")
            return
        }
        viewModelScope.launch {
            _truckActionState.value = TruckActionState.Loading
            when (val fetchResult = truckRepository.getTruckById(truckId)) {
                is Resource.Success -> {
                    if (fetchResult.data?.ownerUid != uid) {
                        _truckActionState.value = TruckActionState.Error("Unauthorized: You do not own this truck listing.")
                        return@launch
                    }
                }
                else -> {}
            }
            when (val result = truckRepository.deleteTruck(truckId)) {
                is Resource.Success -> {
                    _truckActionState.value = TruckActionState.Success
                    fetchMyTrucks()
                }
                is Resource.Error -> {
                    _truckActionState.value = TruckActionState.Error(result.message ?: "Failed to delete truck")
                }
                is Resource.Loading -> {}
            }
        }
    }
    
    fun resetActionState() {
        _truckActionState.value = TruckActionState.Idle
    }
}
