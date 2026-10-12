package com.example.cargolink.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cargolink.presentation.viewmodel.CargoActionState
import com.example.cargolink.presentation.viewmodel.CargoViewModel
import com.example.cargolink.presentation.viewmodel.ProfileState
import com.example.cargolink.presentation.viewmodel.ProfileViewModel
import com.example.cargolink.presentation.viewmodel.TruckActionState
import com.example.cargolink.presentation.viewmodel.TruckDetailState
import com.example.cargolink.presentation.viewmodel.TruckViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TruckAddEditScreen(
    truckId: String?,
    onSaveSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: TruckViewModel = viewModel(),
    cargoViewModel: CargoViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val profileState by profileViewModel.profileState.collectAsState()
    val userRole = (profileState as? ProfileState.Success)?.user?.participationType

    val isCargoOwner = userRole == "CARGO_OWNER"
    val isDriver = userRole == "TRUCK_DRIVER" || userRole == "TRUCK_OPERATOR"

    var selectedTab by remember { mutableIntStateOf(if (isCargoOwner) 1 else 0) } // 0: Truck, 1: Cargo

    LaunchedEffect(userRole) {
        if (isCargoOwner) {
            selectedTab = 1
        } else if (isDriver) {
            selectedTab = 0
        }
    }
    
    // Truck Form State
    var regNumber by remember { mutableStateOf("KCB 456C") }
    var truckType by remember { mutableStateOf("Flatbed") }
    var payload by remember { mutableStateOf("28000 kg") }
    var startingLocation by remember { mutableStateOf("Nairobi") }
    var destination by remember { mutableStateOf("Mombasa") }
    var availabilityDate by remember { mutableStateOf("12 Oct, 2026") }
    var askingPrice by remember { mutableStateOf("120000") }
    var notes by remember { mutableStateOf("Experienced driver, cargo straps included.") }

    // Cargo Form State
    var cargoType by remember { mutableStateOf("") }
    var pickupLocation by remember { mutableStateOf("") }
    var cargoWeight by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }

    val truckDetailState by viewModel.truckDetailState.collectAsState()
    val truckActionState by viewModel.truckActionState.collectAsState()
    val cargoActionState by cargoViewModel.cargoActionState.collectAsState()

    LaunchedEffect(truckId) {
        if (!truckId.isNullOrEmpty() && truckId != "new") {
            viewModel.fetchTruckDetail(truckId)
        }
    }

    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(truckDetailState) {
        if (truckDetailState is TruckDetailState.Success) {
            val truck = (truckDetailState as TruckDetailState.Success).truck
            regNumber = truck.registrationNumber
            truckType = truck.truckType
            payload = if (truck.maxPayloadKg > 0) "${truck.maxPayloadKg.toInt()} kg" else ""
            startingLocation = truck.currentLocation
            destination = truck.preferredDestination ?: ""
            askingPrice = truck.price?.let { "%.0f".format(it) } ?: ""
            notes = truck.description ?: ""
        }
    }

    LaunchedEffect(truckActionState) {
        if (truckActionState is TruckActionState.Success) {
            viewModel.resetActionState()
            onSaveSuccess()
        }
    }

    LaunchedEffect(cargoActionState) {
        if (cargoActionState is CargoActionState.Success) {
            cargoViewModel.resetActionState()
            onSaveSuccess()
        }
    }

    val onPerformSave: () -> Unit = {
        if (selectedTab == 0) {
            val trimmedReg = regNumber.trim()
            val trimmedType = truckType.trim()
            val trimmedLocation = startingLocation.trim()
            val maxPayloadDouble = payload.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
            val priceDouble = askingPrice.replace(Regex("[^0-9.]"), "").toDoubleOrNull()

            if (trimmedReg.isEmpty()) {
                validationError = "Registration number is required."
            } else if (trimmedLocation.isEmpty()) {
                validationError = "Starting location is required."
            } else if (maxPayloadDouble <= 0.0) {
                validationError = "Maximum payload capacity must be a positive number."
            } else {
                validationError = null
                viewModel.saveTruck(
                    id = if (truckId == "new") null else truckId,
                    registrationNumber = trimmedReg,
                    truckType = trimmedType.ifBlank { "Flatbed" },
                    maxPayloadKg = maxPayloadDouble,
                    currentLocation = trimmedLocation,
                    preferredDestination = destination.trim().ifBlank { null },
                    price = priceDouble,
                    description = notes.trim().ifBlank { null }
                )
            }
        } else {
            val trimmedCargoType = cargoType.trim()
            val trimmedPickup = pickupLocation.trim()
            val weightDouble = cargoWeight.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
            val budgetDouble = budget.replace(Regex("[^0-9.]"), "").toDoubleOrNull()

            if (trimmedCargoType.isEmpty()) {
                validationError = "Cargo type / description is required."
            } else if (trimmedPickup.isEmpty()) {
                validationError = "Pickup location is required."
            } else if (weightDouble <= 0.0) {
                validationError = "Shipment weight must be a positive number."
            } else {
                validationError = null
                cargoViewModel.saveCargo(
                    id = null,
                    cargoType = trimmedCargoType,
                    weightKg = weightDouble,
                    pickupLocation = trimmedPickup,
                    destination = destination.trim().ifBlank { "Nairobi" },
                    budget = budgetDouble,
                    preferredDate = availabilityDate.trim().ifBlank { null },
                    specialRequirements = notes.trim().ifBlank { null }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (truckId.isNullOrEmpty() || truckId == "new") {
                            if (selectedTab == 1) "Post Cargo Requirement" else "Post Truck Availability"
                        } else {
                            if (selectedTab == 1) "Edit Cargo Requirement" else "Edit Truck Listing"
                        }, 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Help */ }) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Help")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            BottomActionArea(
                onCancel = onNavigateBack, 
                onSave = onPerformSave,
                isLoading = truckActionState is TruckActionState.Loading || cargoActionState is CargoActionState.Loading
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (validationError != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = validationError!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (truckActionState is TruckActionState.Error) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (truckActionState as TruckActionState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (cargoActionState is CargoActionState.Error) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (cargoActionState as CargoActionState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            if (!isCargoOwner && !isDriver) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = { Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = { Text("Truck Availability") },
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primary,
                            activeContentColor = MaterialTheme.colorScheme.onPrimary,
                            inactiveContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    SegmentedButton(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = { Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = { Text("Cargo Requirements") },
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primary,
                            activeContentColor = MaterialTheme.colorScheme.onPrimary,
                            inactiveContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            if (selectedTab == 0) {
                TruckForm(
                    regNumber = regNumber, onRegChange = { regNumber = it },
                    truckType = truckType, onTypeChange = { truckType = it },
                    payload = payload, onPayloadChange = { payload = it },
                    startingLocation = startingLocation, onStartChange = { startingLocation = it },
                    destination = destination, onDestChange = { destination = it },
                    availabilityDate = availabilityDate, onDateChange = { availabilityDate = it },
                    askingPrice = askingPrice, onPriceChange = { askingPrice = it },
                    notes = notes, onNotesChange = { notes = it }
                )
            } else {
                CargoForm(
                    cargoType = cargoType, onCargoTypeChange = { cargoType = it },
                    pickupLocation = pickupLocation, onPickupChange = { pickupLocation = it },
                    destination = destination, onDestChange = { destination = it },
                    weight = cargoWeight, onWeightChange = { cargoWeight = it },
                    date = availabilityDate, onDateChange = { availabilityDate = it },
                    budget = budget, onBudgetChange = { budget = it },
                    notes = notes, onNotesChange = { notes = it }
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun TruckForm(
    regNumber: String, onRegChange: (String) -> Unit,
    truckType: String, onTypeChange: (String) -> Unit,
    payload: String, onPayloadChange: (String) -> Unit,
    startingLocation: String, onStartChange: (String) -> Unit,
    destination: String, onDestChange: (String) -> Unit,
    availabilityDate: String, onDateChange: (String) -> Unit,
    askingPrice: String, onPriceChange: (String) -> Unit,
    notes: String, onNotesChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ValidatedTextField(
            value = regNumber,
            onValueChange = onRegChange,
            label = "Registration Number",
            isValid = regNumber.isNotEmpty(),
            placeholder = "e.g. KCB 456C"
        )

        ExposedDropdownTextField(
            value = truckType,
            onValueChange = onTypeChange,
            label = "Truck / Body Type",
            options = listOf("Flatbed", "Box", "Refrigerated", "Tanker")
        )

        ValidatedTextField(
            value = payload,
            onValueChange = onPayloadChange,
            label = "Maximum Payload Capacity",
            isValid = payload.isNotEmpty(),
            placeholder = "e.g. 28 Tons"
        )

        OutlinedTextField(
            value = startingLocation,
            onValueChange = onStartChange,
            label = { Text("Starting Location *") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        Column {
            OutlinedTextField(
                value = destination,
                onValueChange = onDestChange,
                label = { Text("Destination *") },
                modifier = Modifier.fillMaxWidth(),
                isError = destination == "Mombasa",
                trailingIcon = { 
                    if (destination == "Mombasa") {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            if (destination == "Mombasa") {
                Text(
                    text = "Please select a valid destination from the dropdown list",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }

        OutlinedTextField(
            value = availabilityDate,
            onValueChange = onDateChange,
            label = { Text("Availability Date *") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        OutlinedTextField(
            value = askingPrice,
            onValueChange = onPriceChange,
            label = { Text("Asking Price (KES) *") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Additional Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun CargoForm(
    cargoType: String, onCargoTypeChange: (String) -> Unit,
    pickupLocation: String, onPickupChange: (String) -> Unit,
    destination: String, onDestChange: (String) -> Unit,
    weight: String, onWeightChange: (String) -> Unit,
    date: String, onDateChange: (String) -> Unit,
    budget: String, onBudgetChange: (String) -> Unit,
    notes: String, onNotesChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = cargoType,
            onValueChange = onCargoTypeChange,
            label = { Text("Cargo Type / Description *") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        OutlinedTextField(
            value = pickupLocation,
            onValueChange = onPickupChange,
            label = { Text("Pickup Location *") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        OutlinedTextField(
            value = destination,
            onValueChange = onDestChange,
            label = { Text("Destination *") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        OutlinedTextField(
            value = weight,
            onValueChange = onWeightChange,
            label = { Text("Shipment Weight *") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        OutlinedTextField(
            value = date,
            onValueChange = onDateChange,
            label = { Text("Preferred Delivery Date *") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        OutlinedTextField(
            value = budget,
            onValueChange = onBudgetChange,
            label = { Text("Offered Budget (KES) *") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Special Requirements") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isValid: Boolean,
    placeholder: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("$label *") },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            if (isValid) {
                Icon(Icons.Default.Check, contentDescription = "Valid", tint = Color(0xFF4CAF50))
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isValid) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (isValid) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text("$label *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true).fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun BottomActionArea(
    onCancel: () -> Unit, 
    onSave: () -> Unit,
    isLoading: Boolean = false
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onSave,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save Listing", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
