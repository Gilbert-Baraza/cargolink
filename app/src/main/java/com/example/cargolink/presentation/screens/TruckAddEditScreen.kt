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
import com.example.cargolink.presentation.viewmodel.TruckViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TruckAddEditScreen(
    truckId: String?,
    onSaveSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: TruckViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Truck, 1: Cargo
    
    // Truck Form State
    var regNumber by remember { mutableStateOf("KCB 456C") }
    var truckType by remember { mutableStateOf("Flatbed") }
    var payload by remember { mutableStateOf("28 Tons") }
    var startingLocation by remember { mutableStateOf("Nairobi (Nairobi County)") }
    var destination by remember { mutableStateOf("Mombasa") }
    var availabilityDate by remember { mutableStateOf("12 Oct, 2026") }
    var askingPrice by remember { mutableStateOf("120,000") }
    var notes by remember { mutableStateOf("Experienced driver, cargo straps included.") }

    // Cargo Form State
    var cargoType by remember { mutableStateOf("") }
    var pickupLocation by remember { mutableStateOf("") }
    var cargoWeight by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post a Listing", fontWeight = FontWeight.Bold) },
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
            BottomActionArea(onCancel = onNavigateBack, onSave = onSaveSuccess)
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
fun BottomActionArea(onCancel: () -> Unit, onSave: () -> Unit) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Preview Mode: Actions will save locally without submitting actual requests.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
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
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Save Listing", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
