package com.example.cargolink.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cargolink.data.models.CargoDto
import com.example.cargolink.data.models.TruckDto
import com.example.cargolink.presentation.viewmodel.*
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TruckDetailScreen(
    truckId: String = "",
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
    viewModel: TruckViewModel = viewModel(),
    cargoViewModel: CargoViewModel = viewModel(),
    requestViewModel: TransportRequestViewModel = viewModel()
) {
    val truckDetailState by viewModel.truckDetailState.collectAsState()
    val truckActionState by viewModel.truckActionState.collectAsState()
    val cargoListState by cargoViewModel.cargoListState.collectAsState()
    val requestListState by requestViewModel.requestListState.collectAsState()
    val requestActionState by requestViewModel.requestActionState.collectAsState()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRequestDialog by remember { mutableStateOf(false) }

    LaunchedEffect(truckId) {
        if (truckId.isNotEmpty()) {
            viewModel.fetchTruckDetail(truckId)
            requestViewModel.fetchCargoOwnerRequests()
        }
    }

    LaunchedEffect(showRequestDialog) {
        if (showRequestDialog) {
            cargoViewModel.fetchMyCargo()
        }
    }

    LaunchedEffect(truckActionState) {
        if (truckActionState is TruckActionState.Success) {
            viewModel.resetActionState()
            onNavigateBack()
        }
    }

    LaunchedEffect(requestActionState) {
        if (requestActionState is RequestActionState.Success) {
            requestViewModel.resetActionState()
            showRequestDialog = false
        }
    }

    val existingRequest = (requestListState as? RequestListState.Success)?.requests?.find {
        it.truckId == truckId && (it.status == "PENDING" || it.status == "ACCEPTED")
    }
    val hasExistingRequest = existingRequest != null

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Truck Listing") },
            text = { Text("Are you sure you want to delete this truck listing? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteTruck(truckId)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRequestDialog && truckDetailState is TruckDetailState.Success) {
        val truck = (truckDetailState as TruckDetailState.Success).truck
        RequestBookingDialog(
            truck = truck,
            cargoListState = cargoListState,
            actionState = requestActionState,
            onDismiss = { showRequestDialog = false },
            onSubmit = { selectedCargo, proposedPrice ->
                requestViewModel.createTransportRequest(selectedCargo, truck, proposedPrice)
            }
        )
    }

    val isTruckOwner = (truckDetailState as? TruckDetailState.Success)?.truck?.ownerUid == currentUid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Truck Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        },
        bottomBar = {
            if (!isTruckOwner && truckDetailState is TruckDetailState.Success) {
                val truck = (truckDetailState as TruckDetailState.Success).truck
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { showRequestDialog = true },
                            enabled = !hasExistingRequest && truck.status.lowercase() == "available" && requestActionState !is RequestActionState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasExistingRequest) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                contentColor = if (hasExistingRequest) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = when {
                                    existingRequest?.status == "ACCEPTED" -> "Booking Accepted"
                                    existingRequest?.status == "PENDING" -> "Request Pending"
                                    else -> "Request Booking"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (hasExistingRequest) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "A booking request for this truck has already been submitted (${existingRequest?.status}).",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = truckDetailState) {
                is TruckDetailState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is TruckDetailState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchTruckDetail(truckId) }) {
                            Text("Retry")
                        }
                    }
                }
                is TruckDetailState.Success -> {
                    val truck = state.truck
                    val isOwner = currentUid != null && currentUid == truck.ownerUid

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        if (truckActionState is TruckActionState.Error) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Text(
                                    text = (truckActionState as TruckActionState.Error).message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Status Tag
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Surface(
                                color = if (truck.status.lowercase() == "available") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = truck.status.uppercase(),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (truck.status.lowercase() == "available") MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Header Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${truck.registrationNumber} (${truck.truckType})",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Max Payload: ${truck.maxPayloadKg} kg",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Box {
                                Surface(
                                    modifier = Modifier.size(56.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping, 
                                        contentDescription = null, 
                                        modifier = Modifier.padding(12.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Route Section
                        RouteCard(
                            origin = truck.currentLocation,
                            destination = truck.preferredDestination ?: "Flexible",
                            subText = "Transporter Listing"
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Info Grid
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                InfoBox(label = "Truck Type", value = truck.truckType, modifier = Modifier.weight(1f))
                                InfoBox(label = "Capacity", value = "${truck.maxPayloadKg} kg", modifier = Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                InfoBox(label = "Location", value = truck.currentLocation, modifier = Modifier.weight(1f))
                                InfoBox(label = "Status", value = truck.status.uppercase(), modifier = Modifier.weight(1f))
                            }
                        }

                        if (!truck.description.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "About this Truck", 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = truck.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (truck.price != null && truck.price > 0) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "Asking Price", 
                                        style = MaterialTheme.typography.labelMedium, 
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${truck.currency} ${"%.0f".format(truck.price)}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        if (isOwner) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onNavigateToEdit(truck.id ?: "") },
                                    enabled = truckActionState !is TruckActionState.Loading,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Edit Listing")
                                }
                                OutlinedButton(
                                    onClick = { showDeleteDialog = true },
                                    enabled = truckActionState !is TruckActionState.Loading,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Delete Listing")
                                }
                            }
                        }
                    }
                }
                is TruckDetailState.Idle -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestBookingDialog(
    truck: TruckDto,
    cargoListState: CargoListState,
    actionState: RequestActionState,
    onDismiss: () -> Unit,
    onSubmit: (CargoDto, Double?) -> Unit
) {
    var selectedCargo by remember { mutableStateOf<CargoDto?>(null) }
    var priceText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Transport Booking", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Selected Truck: ${truck.registrationNumber} (${truck.truckType})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text("Select Cargo Requirement:", style = MaterialTheme.typography.labelMedium)

                when (cargoListState) {
                    is CargoListState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    is CargoListState.Error -> {
                        Text(cargoListState.message, color = MaterialTheme.colorScheme.error)
                    }
                    is CargoListState.Success -> {
                        if (cargoListState.cargoList.isEmpty()) {
                            Text(
                                text = "You have no active cargo requirements. Please post a cargo requirement first.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedCargo?.cargoType ?: "Choose cargo requirement",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true).fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    cargoListState.cargoList.forEach { cargo ->
                                        DropdownMenuItem(
                                            text = { Text("${cargo.cargoType} (${cargo.weightKg} kg)") },
                                            onClick = {
                                                selectedCargo = cargo
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (selectedCargo != null) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Proposed Price (KES)") },
                        placeholder = { Text(truck.price?.let { "%.0f".format(it) } ?: "Enter proposed price") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (actionState is RequestActionState.Error) {
                    Text(
                        text = actionState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedCargo != null) {
                        val price = priceText.replace(Regex("[^0-9.]"), "").toDoubleOrNull()
                        onSubmit(selectedCargo!!, price)
                    }
                },
                enabled = selectedCargo != null && actionState !is RequestActionState.Loading
            ) {
                if (actionState is RequestActionState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Submit Request")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = actionState !is RequestActionState.Loading) {
                Text("Cancel")
            }
        }
    )
}
