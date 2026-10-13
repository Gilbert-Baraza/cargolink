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
import com.example.cargolink.data.models.TransportRequestDto
import com.example.cargolink.data.models.TruckDto
import com.example.cargolink.presentation.viewmodel.*
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    requestId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: TransportRequestViewModel = viewModel(),
    cargoViewModel: CargoViewModel = viewModel(),
    truckViewModel: TruckViewModel = viewModel()
) {
    val requestDetailState by viewModel.requestDetailState.collectAsState()
    val requestActionState by viewModel.requestActionState.collectAsState()
    val cargoDetailState by cargoViewModel.cargoDetailState.collectAsState()
    val truckDetailState by truckViewModel.truckDetailState.collectAsState()

    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(requestId) {
        viewModel.fetchRequestDetail(requestId)
    }

    LaunchedEffect(requestDetailState) {
        if (requestDetailState is RequestDetailState.Success) {
            val req = (requestDetailState as RequestDetailState.Success).request
            if (req.cargoRequestId.isNotBlank()) {
                cargoViewModel.fetchCargoDetail(req.cargoRequestId)
            }
            if (req.truckId.isNotBlank()) {
                truckViewModel.fetchTruckDetail(req.truckId)
            }
        }
    }

    LaunchedEffect(requestActionState) {
        if (requestActionState is RequestActionState.Success) {
            viewModel.resetActionState()
            viewModel.fetchRequestDetail(requestId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = requestDetailState) {
                is RequestDetailState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is RequestDetailState.Error -> {
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
                        Button(onClick = { viewModel.fetchRequestDetail(requestId) }) {
                            Text("Retry")
                        }
                    }
                }
                is RequestDetailState.Success -> {
                    val request = state.request
                    val isDriver = currentUid != null && currentUid == request.truckOwnerUid
                    val isCargoOwner = currentUid != null && currentUid == request.cargoOwnerUid
                    val isActionLoading = requestActionState is RequestActionState.Loading

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (requestActionState is RequestActionState.Error) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = (requestActionState as RequestActionState.Error).message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Header Status Banner
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Booking #${request.id?.takeLast(6)?.uppercase() ?: ""}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            RequestStatusChip(request.status, request.isConfirmedByOwner)
                        }

                        // Lifecycle Progress Bar
                        ShipmentLifecycleProgress(status = request.status)

                        // Route Details Card
                        RouteCard(
                            origin = request.pickupLocation,
                            destination = request.destination,
                            subText = "Transport Booking Route"
                        )

                        // Associated Cargo Requirement Details
                        when (val cargoState = cargoDetailState) {
                            is CargoDetailState.Success -> {
                                val cargo = cargoState.cargo
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Cargo Details",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = cargo.cargoType,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Weight: ${cargo.weightKg} kg",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (!cargo.specialRequirements.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Notes: ${cargo.specialRequirements}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            else -> {
                                InfoBox(label = "Cargo Item", value = request.cargoTitle, modifier = Modifier.fillMaxWidth())
                            }
                        }

                        // Associated Truck Details
                        when (val truckState = truckDetailState) {
                            is TruckDetailState.Success -> {
                                val truck = truckState.truck
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Transporter Vehicle",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "${truck.registrationNumber} (${truck.truckType})",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Max Capacity: ${truck.maxPayloadKg} kg",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            else -> {
                                InfoBox(label = "Transporter Truck", value = request.truckTitle, modifier = Modifier.fillMaxWidth())
                            }
                        }

                        // Agreed / Proposed Pricing Card
                        if (request.proposedPrice != null && request.proposedPrice > 0) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Agreed Price",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${request.currency} ${"%.0f".format(request.proposedPrice)}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Lifecycle Transition Actions
                        if (isDriver) {
                            when (request.status.uppercase()) {
                                "PENDING" -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.rejectRequest(request) },
                                            enabled = !isActionLoading,
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Text("Reject")
                                        }
                                        Button(
                                            onClick = { viewModel.acceptRequest(request) },
                                            enabled = !isActionLoading,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Accept Request")
                                        }
                                    }
                                }
                                "ACCEPTED" -> {
                                    Button(
                                        onClick = { viewModel.startTransit(request) },
                                        enabled = !isActionLoading,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(Icons.Default.LocalShipping, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Start Transit", fontWeight = FontWeight.Bold)
                                    }
                                }
                                "IN_TRANSIT" -> {
                                    Button(
                                        onClick = { viewModel.markDelivered(request) },
                                        enabled = !isActionLoading,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Mark as Delivered", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (request.status.uppercase() == "DELIVERED") {
                            if (request.isConfirmedByOwner) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "✓ Delivery Confirmed",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32)
                                            )
                                            Text(
                                                text = "Receipt confirmed by cargo owner",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF1B5E20)
                                            )
                                        }
                                    }
                                }
                            } else if (isCargoOwner) {
                                Button(
                                    onClick = { viewModel.confirmDeliveryReceipt(request) },
                                    enabled = !isActionLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Confirm Delivery Receipt", fontWeight = FontWeight.Bold)
                                }
                            } else if (isDriver) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Awaiting Owner Confirmation",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                            Text(
                                                text = "Shipment marked as delivered. Pending receipt confirmation from cargo owner.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (isCargoOwner && request.status.uppercase() == "PENDING") {
                            OutlinedButton(
                                onClick = { viewModel.cancelRequest(request) },
                                enabled = !isActionLoading,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Cancel Request")
                            }
                        }
                    }
                }
                is RequestDetailState.Idle -> {}
            }
        }
    }
}

@Composable
fun ShipmentLifecycleProgress(status: String) {
    val steps = listOf("ACCEPTED", "IN_TRANSIT", "DELIVERED")
    val currentStepIndex = when (status.uppercase()) {
        "ACCEPTED" -> 0
        "IN_TRANSIT" -> 1
        "DELIVERED" -> 2
        else -> -1
    }

    if (currentStepIndex >= 0) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Shipment Progress",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.forEachIndexed { index, stepName ->
                        val isPassed = index <= currentStepIndex
                        val stepColor = if (isPassed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier.size(24.dp),
                                shape = CircleShape,
                                color = stepColor
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (index < currentStepIndex) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    } else {
                                        Text("${index + 1}", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stepName.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (index == currentStepIndex) FontWeight.Bold else FontWeight.Normal,
                                color = if (isPassed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (index < steps.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                                color = if (index < currentStepIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}
