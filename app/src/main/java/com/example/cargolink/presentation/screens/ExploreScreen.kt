package com.example.cargolink.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cargolink.presentation.viewmodel.CargoListState
import com.example.cargolink.presentation.viewmodel.CargoViewModel
import com.example.cargolink.presentation.viewmodel.TruckListState
import com.example.cargolink.presentation.viewmodel.TruckViewModel
import com.example.cargolink.ui.theme.CargoLinkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    participationType: String? = null,
    onNavigateToDetails: (String) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: TruckViewModel = viewModel(),
    cargoViewModel: CargoViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Determine the view mode based on the user's role
    val viewMode = if (participationType == "TRUCK_DRIVER" || participationType == "TRUCK_OPERATOR") "Cargo" else "Trucks"

    val marketplaceTrucksState by viewModel.marketplaceTrucksState.collectAsState()
    val marketplaceCargoState by cargoViewModel.marketplaceCargoState.collectAsState()

    LaunchedEffect(viewMode) {
        if (viewMode == "Trucks") {
            viewModel.fetchMarketplaceTrucks()
        } else {
            cargoViewModel.fetchMarketplaceCargo()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (viewMode == "Cargo") "Find Cargo" else "Find Trucks",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications, 
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp, start = 8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "JD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                // Search and Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(if (viewMode == "Cargo") "Search cargo routes..." else "Search truck routes...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { /* TODO */ }) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Tune, contentDescription = "Filter")
                                    Text("Filter", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true
                    )
                }
            }

            // Results List / Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                if (viewMode == "Trucks") {
                    when (val state = marketplaceTrucksState) {
                        is TruckListState.Loading -> {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                        is TruckListState.Error -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.message, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.fetchMarketplaceTrucks() }) {
                                    Text("Retry")
                                }
                            }
                        }
                        is TruckListState.Success -> {
                            val filteredTrucks = state.trucks.filter {
                                searchQuery.isBlank() ||
                                        it.registrationNumber.contains(searchQuery, ignoreCase = true) ||
                                        it.currentLocation.contains(searchQuery, ignoreCase = true) ||
                                        it.truckType.contains(searchQuery, ignoreCase = true)
                            }
                            if (filteredTrucks.isEmpty()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No trucks available",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Check back later when transporters post their availability.",
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
                                    items(filteredTrucks) { truck ->
                                        TruckListingCard(
                                            truck = TruckSample(
                                                id = truck.id ?: "",
                                                companyName = truck.registrationNumber,
                                                isVerified = true,
                                                rating = 4.8,
                                                origin = truck.currentLocation,
                                                destination = truck.preferredDestination ?: "Flexible",
                                                capacity = "${truck.maxPayloadKg} kg",
                                                availabilityDate = "Available Now",
                                                price = if (truck.price != null && truck.price > 0) "${truck.currency} ${"%.0f".format(truck.price)}" else "Negotiable",
                                                status = truck.status.uppercase()
                                            ),
                                            onClick = { onNavigateToDetails(truck.id ?: "") }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    when (val state = marketplaceCargoState) {
                        is CargoListState.Loading -> {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                        is CargoListState.Error -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.message, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { cargoViewModel.fetchMarketplaceCargo() }) {
                                    Text("Retry")
                                }
                            }
                        }
                        is CargoListState.Success -> {
                            val filteredCargo = state.cargoList.filter {
                                searchQuery.isBlank() ||
                                        it.cargoType.contains(searchQuery, ignoreCase = true) ||
                                        it.pickupLocation.contains(searchQuery, ignoreCase = true) ||
                                        it.destination.contains(searchQuery, ignoreCase = true)
                            }
                            if (filteredCargo.isEmpty()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No cargo available",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Check back later when owners post their requirements.",
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
                                    items(filteredCargo) { cargo ->
                                        CargoListingCard(
                                            cargo = CargoSample(
                                                id = cargo.id ?: "",
                                                ownerName = cargo.cargoType,
                                                cargoType = cargo.cargoType,
                                                origin = cargo.pickupLocation,
                                                destination = cargo.destination,
                                                weight = "${cargo.weightKg} kg",
                                                date = cargo.preferredDate ?: "Flexible",
                                                budget = if (cargo.budget != null && cargo.budget > 0) "${cargo.currency} ${"%.0f".format(cargo.budget)}" else "Negotiable"
                                            ),
                                            onClick = { onNavigateToDetails(cargo.id ?: "") }
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
}

@Composable
fun TruckListingCard(truck: TruckSample, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        truck.companyName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (truck.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        truck.rating.toString(), 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        Icons.Default.Star, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary, 
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${truck.origin} → ${truck.destination}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.LocationOn, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary, 
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Capacity", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(truck.capacity, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(modifier = Modifier.weight(1.2f)) {
                    Text("Availability Date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(truck.availabilityDate, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Asking Price", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(truck.price, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChipSmall(truck.status, Color(0xFF4CAF50))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { /* TODO */ },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Contact")
                    }
                    Button(
                        onClick = { /* TODO */ },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text("Book Now")
                    }
                }
            }
        }
    }
}

@Composable
fun CargoListingCard(cargo: CargoSample, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    cargo.ownerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    cargo.cargoType, 
                    style = MaterialTheme.typography.labelLarge, 
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "${cargo.origin} → ${cargo.destination}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Weight", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(cargo.weight, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(modifier = Modifier.weight(1.2f)) {
                    Text("Preferred Date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(cargo.date, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Budget", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(cargo.budget, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChipSmall("PENDING", Color(0xFF2196F3))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { /* TODO */ },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Details")
                    }
                    Button(
                        onClick = { /* TODO */ },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Make Offer")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChipSmall(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}

// Sample Data Models
data class TruckSample(
    val id: String,
    val companyName: String,
    val isVerified: Boolean,
    val rating: Double,
    val origin: String,
    val destination: String,
    val capacity: String,
    val availabilityDate: String,
    val price: String,
    val status: String
)

data class CargoSample(
    val id: String,
    val ownerName: String,
    val cargoType: String,
    val origin: String,
    val destination: String,
    val weight: String,
    val date: String,
    val budget: String
)

val sampleTrucks = listOf(
    TruckSample("1", "J. Kamau Logistics", true, 4.8, "Nairobi", "Mombasa", "28 Tons", "12 Oct, 2026", "KES 120,000", "AVAILABLE"),
    TruckSample("2", "Kariuki Transporters", true, 4.6, "Mombasa", "Eldoret", "8 Tons", "14 Oct, 2026", "KES 65,000", "AVAILABLE"),
    TruckSample("3", "East African Freight", false, 4.9, "Nairobi", "Kisumu", "30 Tons", "15 Oct, 2026", "KES 150,000", "AVAILABLE")
)

val sampleCargo = listOf(
    CargoSample("c1", "Agri-Supply Ltd", "MAIZE", "Kisumu", "Nairobi", "15 Tons", "13 Oct, 2026", "KES 45,000"),
    CargoSample("c2", "Build-It Co", "CEMENT", "Mombasa", "Eldoret", "33 Tons", "16 Oct, 2026", "KES 90,000")
)

@Preview(showBackground = true)
@Composable
fun ExploreScreenPreview() {
    CargoLinkTheme {
        ExploreScreen()
    }
}
