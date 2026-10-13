package com.example.cargolink.presentation.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cargolink.data.models.CargoDto
import com.example.cargolink.data.models.TruckDto
import com.example.cargolink.presentation.viewmodel.*
import com.example.cargolink.ui.theme.CargoLinkTheme
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargoDetailScreen(
    cargoId: String = "",
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
    viewModel: CargoViewModel = viewModel(),
    requestViewModel: TransportRequestViewModel = viewModel(),
    truckViewModel: TruckViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val cargoDetailState by viewModel.cargoDetailState.collectAsState()
    val cargoActionState by viewModel.cargoActionState.collectAsState()
    val requestListState by requestViewModel.requestListState.collectAsState()
    val requestActionState by requestViewModel.requestActionState.collectAsState()
    val truckListState by truckViewModel.truckListState.collectAsState()
    val profileState by profileViewModel.profileState.collectAsState()

    val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    val user = (profileState as? ProfileState.Success)?.user
    val isDriver = user?.participationType == "TRUCK_DRIVER" || user?.participationType == "TRUCK_OPERATOR"

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOfferDialog by remember { mutableStateOf(false) }

    LaunchedEffect(cargoId) {
        if (cargoId.isNotEmpty()) {
            viewModel.fetchCargoDetail(cargoId)
            requestViewModel.fetchTruckOwnerRequests()
        }
    }

    LaunchedEffect(showOfferDialog) {
        if (showOfferDialog) {
            truckViewModel.fetchMyTrucks()
        }
    }

    LaunchedEffect(cargoActionState) {
        if (cargoActionState is CargoActionState.Success) {
            viewModel.resetActionState()
            onNavigateBack()
        }
    }

    LaunchedEffect(requestActionState) {
        if (requestActionState is RequestActionState.Success) {
            requestViewModel.resetActionState()
            showOfferDialog = false
        }
    }

    val existingOffer = (requestListState as? RequestListState.Success)?.requests?.find {
        it.cargoRequestId == cargoId && (it.status == "PENDING" || it.status == "ACCEPTED")
    }
    val hasExistingOffer = existingOffer != null

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Cargo Requirement") },
            text = { Text("Are you sure you want to delete this cargo requirement? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteCargo(cargoId)
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

    if (showOfferDialog && cargoDetailState is CargoDetailState.Success) {
        val cargo = (cargoDetailState as CargoDetailState.Success).cargo
        MakeOfferDialog(
            cargo = cargo,
            truckListState = truckListState,
            actionState = requestActionState,
            onDismiss = { showOfferDialog = false },
            onSubmit = { selectedTruck, proposedPrice ->
                requestViewModel.createTransportOffer(cargo, selectedTruck, proposedPrice)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cargo Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            if (cargoDetailState is CargoDetailState.Success) {
                val cargo = (cargoDetailState as CargoDetailState.Success).cargo
                val isOwner = currentUid != null && currentUid == cargo.ownerUid

                if (!isOwner && isDriver) {
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
                                onClick = { showOfferDialog = true },
                                enabled = !hasExistingOffer && cargo.status.lowercase() == "available" && requestActionState !is RequestActionState.Loading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasExistingOffer) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                    contentColor = if (hasExistingOffer) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = when {
                                        existingOffer?.status == "ACCEPTED" -> "Offer Accepted"
                                        existingOffer?.status == "PENDING" -> "Offer Pending"
                                        else -> "Make Offer"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (hasExistingOffer) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "An offer for this cargo requirement has already been submitted (${existingOffer.status}).",
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
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = cargoDetailState) {
                is CargoDetailState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is CargoDetailState.Error -> {
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
                        Button(onClick = { viewModel.fetchCargoDetail(cargoId) }) {
                            Text("Retry")
                        }
                    }
                }
                is CargoDetailState.Success -> {
                    val cargo = state.cargo
                    val isOwner = currentUid != null && currentUid == cargo.ownerUid

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        if (cargoActionState is CargoActionState.Error) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Text(
                                    text = (cargoActionState as CargoActionState.Error).message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Status Tag
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Surface(
                                color = if (cargo.status.lowercase() == "available") MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = cargo.status.uppercase(),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (cargo.status.lowercase() == "available") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
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
                                    text = "${cargo.cargoType} (${cargo.weightKg} kg)",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Pickup: ${cargo.pickupLocation}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Surface(
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2, 
                                    contentDescription = null, 
                                    modifier = Modifier.padding(12.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Route Section
                        RouteCard(
                            origin = cargo.pickupLocation,
                            destination = cargo.destination,
                            subText = "Cargo Requirement Route"
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Info Grid
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                InfoBox(label = "Weight", value = "${cargo.weightKg} kg", modifier = Modifier.weight(1f))
                                InfoBox(label = "Preferred Date", value = cargo.preferredDate ?: "Flexible", modifier = Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                InfoBox(label = "Cargo Type", value = cargo.cargoType, modifier = Modifier.weight(1f))
                                InfoBox(label = "Status", value = cargo.status.uppercase(), modifier = Modifier.weight(1f))
                            }
                        }

                        if (!cargo.specialRequirements.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Special Requirements", 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = cargo.specialRequirements,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (cargo.budget != null && cargo.budget > 0) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "Offered Budget", 
                                        style = MaterialTheme.typography.labelMedium, 
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${cargo.currency} ${"%.0f".format(cargo.budget)}",
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
                                    onClick = { onNavigateToEdit(cargo.id ?: "") },
                                    enabled = cargoActionState !is CargoActionState.Loading,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Edit Requirement")
                                }
                                OutlinedButton(
                                    onClick = { showDeleteDialog = true },
                                    enabled = cargoActionState !is CargoActionState.Loading,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Delete Requirement")
                                }
                            }
                        }
                    }
                }
                is CargoDetailState.Idle -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeOfferDialog(
    cargo: CargoDto,
    truckListState: TruckListState,
    actionState: RequestActionState,
    onDismiss: () -> Unit,
    onSubmit: (selectedTruck: TruckDto, proposedPrice: Double?) -> Unit
) {
    var selectedTruck by remember { mutableStateOf<TruckDto?>(null) }
    var priceText by remember { mutableStateOf(cargo.budget?.let { "%.0f".format(it) } ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Make Transport Offer", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Cargo: ${cargo.cargoType} (${cargo.weightKg} kg)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Route: ${cargo.pickupLocation} → ${cargo.destination}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                Text("Select Truck for Transport", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                when (truckListState) {
                    is TruckListState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    is TruckListState.Error -> {
                        Text(truckListState.message, color = MaterialTheme.colorScheme.error)
                    }
                    is TruckListState.Success -> {
                        val availableTrucks = truckListState.trucks.filter { it.status.lowercase() == "available" }
                        if (availableTrucks.isEmpty()) {
                            Text("No available trucks listed. Please post an available truck listing first.", color = MaterialTheme.colorScheme.error)
                        } else {
                            availableTrucks.forEach { truck ->
                                val isSelected = selectedTruck?.id == truck.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedTruck = truck },
                                    label = { Text("${truck.registrationNumber} (${truck.truckType}) - ${truck.maxPayloadKg} kg") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Proposed Offer Price (${cargo.currency})") },
                    placeholder = { Text(cargo.budget?.let { "%.0f".format(it) } ?: "e.g. 45000") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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
                    val truck = selectedTruck
                    if (truck != null) {
                        val proposedPrice = priceText.toDoubleOrNull()
                        onSubmit(truck, proposedPrice)
                    }
                },
                enabled = selectedTruck != null && actionState !is RequestActionState.Loading
            ) {
                if (actionState is RequestActionState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Submit Offer")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CargoDetailPreview() {
    CargoLinkTheme {
        CargoDetailScreen()
    }
}
