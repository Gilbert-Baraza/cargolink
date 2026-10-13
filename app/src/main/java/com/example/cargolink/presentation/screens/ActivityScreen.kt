package com.example.cargolink.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cargolink.data.models.TransportRequestDto
import com.example.cargolink.presentation.viewmodel.RequestActionState
import com.example.cargolink.presentation.viewmodel.RequestListState
import com.example.cargolink.presentation.viewmodel.TransportRequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    participationType: String? = null,
    onNavigateToDetails: (String) -> Unit = {},
    viewModel: TransportRequestViewModel = viewModel()
) {
    val isDriver = participationType == "TRUCK_DRIVER" || participationType == "TRUCK_OPERATOR"
    val requestListState by viewModel.requestListState.collectAsState()
    val requestActionState by viewModel.requestActionState.collectAsState()

    LaunchedEffect(isDriver) {
        if (isDriver) {
            viewModel.fetchTruckOwnerRequests()
        } else {
            viewModel.fetchCargoOwnerRequests()
        }
    }

    var selectedFilter by remember { mutableStateOf("ALL") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Booking History & Activity",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // History Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "All",
                    "PENDING" to "Pending",
                    "ACTIVE" to "Active",
                    "DELIVERED" to "Delivered",
                    "CLOSED" to "Closed"
                ).forEach { (filterKey, filterLabel) ->
                    FilterChip(
                        selected = selectedFilter == filterKey,
                        onClick = { selectedFilter = filterKey },
                        label = { Text(filterLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            if (requestActionState is RequestActionState.Error) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = (requestActionState as RequestActionState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = requestListState) {
                    is RequestListState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    is RequestListState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                if (isDriver) viewModel.fetchTruckOwnerRequests() else viewModel.fetchCargoOwnerRequests()
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                    is RequestListState.Success -> {
                        val filteredRequests = state.requests.filter { req ->
                            when (selectedFilter) {
                                "PENDING" -> req.status.uppercase() == "PENDING"
                                "ACTIVE" -> req.status.uppercase() in listOf("ACCEPTED", "IN_TRANSIT")
                                "DELIVERED" -> req.status.uppercase() == "DELIVERED"
                                "CLOSED" -> req.status.uppercase() in listOf("REJECTED", "CANCELLED")
                                else -> true
                            }
                        }

                        if (filteredRequests.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No booking history found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isDriver) {
                                        "Transport requests and shipments for your trucks will appear here."
                                    } else {
                                        "Requests and shipments you send to transporters will appear here."
                                    },
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredRequests) { request ->
                                    RequestItemCard(
                                        request = request,
                                        isDriver = isDriver,
                                        isActionLoading = requestActionState is RequestActionState.Loading,
                                        onClick = { onNavigateToDetails(request.id ?: "") },
                                        onAccept = { viewModel.acceptRequest(request) },
                                        onReject = { viewModel.rejectRequest(request) },
                                        onCancel = { viewModel.cancelRequest(request) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestItemCard(
    request: TransportRequestDto,
    isDriver: Boolean,
    isActionLoading: Boolean,
    onClick: () -> Unit = {},
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isDriver) "Cargo: ${request.cargoTitle}" else "Truck: ${request.truckTitle}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (request.initiatedBy == "TRUCK_DRIVER") "Driver Offer" else "Cargo Request",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                RequestStatusChip(request.status, request.isConfirmedByOwner)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${request.pickupLocation} → ${request.destination}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            if (request.proposedPrice != null && request.proposedPrice > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Proposed Price: ${request.currency} ${"%.0f".format(request.proposedPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            request.createdAt?.let { date ->
                Spacer(modifier = Modifier.height(2.dp))
                val formattedDate = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(date)
                Text(
                    text = "Date: $formattedDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (request.status == "PENDING") {
                val isDriverOffer = request.initiatedBy == "TRUCK_DRIVER"
                val canAcceptOrReject = (isDriver && !isDriverOffer) || (!isDriver && isDriverOffer)
                val canCancel = (isDriver && isDriverOffer) || (!isDriver && !isDriverOffer)

                if (canAcceptOrReject) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReject,
                            enabled = !isActionLoading,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Reject")
                        }
                        Button(
                            onClick = onAccept,
                            enabled = !isActionLoading,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Accept")
                        }
                    }
                } else if (canCancel) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            enabled = !isActionLoading,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(if (isDriverOffer) "Cancel Offer" else "Cancel Request")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestStatusChip(status: String, isConfirmed: Boolean = false) {
    val displayStatus = if (status.uppercase() == "DELIVERED" && isConfirmed) "CONFIRMED" else status.uppercase()
    val (bgColor, textColor) = when (displayStatus) {
        "CONFIRMED" -> Color(0xFF2E7D32).copy(alpha = 0.2f) to Color(0xFF2E7D32)
        "DELIVERED" -> Color(0xFF00897B).copy(alpha = 0.2f) to Color(0xFF00897B)
        "IN_TRANSIT" -> Color(0xFF1976D2).copy(alpha = 0.2f) to Color(0xFF1976D2)
        "ACCEPTED" -> Color(0xFF4CAF50).copy(alpha = 0.2f) to Color(0xFF4CAF50)
        "PENDING" -> Color(0xFFFFA000).copy(alpha = 0.2f) to Color(0xFFFFA000)
        "REJECTED", "CANCELLED" -> Color(0xFFF44336).copy(alpha = 0.2f) to Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = displayStatus.replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        )
    }
}
