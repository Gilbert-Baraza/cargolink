package com.example.cargolink.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cargolink.core.common.Resource
import com.example.cargolink.data.models.CargoDto
import com.example.cargolink.data.repository.CargoRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CargoListState {
    object Loading : CargoListState()
    data class Success(val cargoList: List<CargoDto>) : CargoListState()
    data class Error(val message: String) : CargoListState()
}

sealed class CargoDetailState {
    object Idle : CargoDetailState()
    object Loading : CargoDetailState()
    data class Success(val cargo: CargoDto) : CargoDetailState()
    data class Error(val message: String) : CargoDetailState()
}

sealed class CargoActionState {
    object Idle : CargoActionState()
    object Loading : CargoActionState()
    object Success : CargoActionState()
    data class Error(val message: String) : CargoActionState()
}

class CargoViewModel : ViewModel() {
    private val cargoRepository = CargoRepository()
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val _cargoListState = MutableStateFlow<CargoListState>(CargoListState.Loading)
    val cargoListState: StateFlow<CargoListState> = _cargoListState.asStateFlow()

    private val _marketplaceCargoState = MutableStateFlow<CargoListState>(CargoListState.Loading)
    val marketplaceCargoState: StateFlow<CargoListState> = _marketplaceCargoState.asStateFlow()

    private val _cargoDetailState = MutableStateFlow<CargoDetailState>(CargoDetailState.Idle)
    val cargoDetailState: StateFlow<CargoDetailState> = _cargoDetailState.asStateFlow()

    private val _cargoActionState = MutableStateFlow<CargoActionState>(CargoActionState.Idle)
    val cargoActionState: StateFlow<CargoActionState> = _cargoActionState.asStateFlow()

    fun fetchMyCargo() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _cargoListState.value = CargoListState.Loading
            when (val result = cargoRepository.getMyCargo(uid)) {
                is Resource.Success -> {
                    _cargoListState.value = CargoListState.Success(result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _cargoListState.value = CargoListState.Error(result.message ?: "Unknown error")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun fetchMarketplaceCargo() {
        viewModelScope.launch {
            _marketplaceCargoState.value = CargoListState.Loading
            when (val result = cargoRepository.getAllAvailableCargo()) {
                is Resource.Success -> {
                    _marketplaceCargoState.value = CargoListState.Success(result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _marketplaceCargoState.value = CargoListState.Error(result.message ?: "Unknown error")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun fetchCargoDetail(cargoId: String) {
        viewModelScope.launch {
            _cargoDetailState.value = CargoDetailState.Loading
            when (val result = cargoRepository.getCargoById(cargoId)) {
                is Resource.Success -> {
                    _cargoDetailState.value = CargoDetailState.Success(result.data!!)
                }
                is Resource.Error -> {
                    _cargoDetailState.value = CargoDetailState.Error(result.message ?: "Cargo requirement not found")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun saveCargo(
        id: String?,
        cargoType: String,
        weightKg: Double,
        pickupLocation: String,
        destination: String,
        budget: Double? = null,
        currency: String = "KES",
        preferredDate: String? = null,
        specialRequirements: String? = null,
        status: String = "available"
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _cargoActionState.value = CargoActionState.Error("User not authenticated")
            return
        }
        viewModelScope.launch {
            _cargoActionState.value = CargoActionState.Loading
            val cargo = CargoDto(
                id = id,
                ownerUid = uid,
                cargoType = cargoType,
                weightKg = weightKg,
                pickupLocation = pickupLocation,
                destination = destination,
                budget = budget,
                currency = currency,
                preferredDate = preferredDate,
                specialRequirements = specialRequirements,
                status = status
            )
            when (val result = cargoRepository.saveCargo(cargo)) {
                is Resource.Success -> {
                    _cargoActionState.value = CargoActionState.Success
                    fetchMyCargo()
                }
                is Resource.Error -> {
                    _cargoActionState.value = CargoActionState.Error(result.message ?: "Failed to save cargo requirement")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteCargo(cargoId: String) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _cargoActionState.value = CargoActionState.Error("User not authenticated")
            return
        }
        viewModelScope.launch {
            _cargoActionState.value = CargoActionState.Loading
            when (val fetchResult = cargoRepository.getCargoById(cargoId)) {
                is Resource.Success -> {
                    if (fetchResult.data?.ownerUid != uid) {
                        _cargoActionState.value = CargoActionState.Error("Unauthorized: You do not own this cargo requirement.")
                        return@launch
                    }
                }
                else -> {}
            }
            when (val result = cargoRepository.deleteCargo(cargoId)) {
                is Resource.Success -> {
                    _cargoActionState.value = CargoActionState.Success
                    fetchMyCargo()
                }
                is Resource.Error -> {
                    _cargoActionState.value = CargoActionState.Error(result.message ?: "Failed to delete cargo requirement")
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun resetActionState() {
        _cargoActionState.value = CargoActionState.Idle
    }
}
