package com.example.cargolink.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cargolink.ui.theme.CargoLinkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    onNavigateToDetails: (String) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All (12)") }
    val filters = listOf("All (12)", "Bookings (3)", "Status Updates (6)", "Alerts (3)")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Activity",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column {
                // Preview Mode Banner
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Preview Mode: Viewing simulated activity feed and updates.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = MaterialTheme.colorScheme.outline,
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            enabled = true,
                            selected = selectedFilter == filter
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Activity List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sampleActivities) { activity ->
                    ActivityCard(activity, onNavigateToDetails)
                }
            }
        }
    }
}

@Composable
fun ActivityCard(activity: ActivityData, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                verticalAlignment = Alignment.Top
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(activity.iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = activity.icon,
                        contentDescription = null,
                        tint = activity.iconTintColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activity.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (activity.statusBadge != null) {
                            StatusBadge(activity.statusBadge, activity.statusBadgeColor)
                        } else {
                            Text(
                                text = activity.topRightText ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = activity.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (activity.price != null || activity.timeAgo != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (activity.price != null) {
                                Text(
                                    text = activity.price,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            
                            if (activity.timeAgo != null) {
                                Text(
                                    text = activity.timeAgo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (activity.hasActions) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { /* TODO */ },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Decline")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { onClick(activity.id) },
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            "View Details", 
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 10.sp
            )
        )
    }
}

data class ActivityData(
    val id: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconTintColor: Color,
    val category: String,
    val title: String,
    val description: String,
    val price: String? = null,
    val timeAgo: String? = null,
    val topRightText: String? = null,
    val statusBadge: String? = null,
    val statusBadgeColor: Color = Color.Transparent,
    val hasActions: Boolean = false
)

val sampleActivities = listOf(
    ActivityData(
        id = "1",
        icon = Icons.Default.Inventory2,
        iconBgColor = Color(0xFFF59E0B), // Solid Amber
        iconTintColor = Color(0xFF102A43), // PrimaryNavy contrast
        category = "New Request",
        topRightText = "New Request",
        title = "Offer for Scania G460",
        description = "Kamau Logistics requested Nairobi → Mombasa for 28 Tons",
        price = "KES 120,000",
        timeAgo = "15m ago",
        hasActions = true
    ),
    ActivityData(
        id = "2",
        icon = Icons.Default.LocalShipping,
        iconBgColor = Color(0xFF2196F3).copy(alpha = 0.15f),
        iconTintColor = Color(0xFF2196F3),
        category = "Status Update",
        title = "In Transit",
        description = "SCANIA R450 • Nairobi → Mombasa\nCargo loaded & on the way.",
        statusBadge = "IN TRANSIT",
        statusBadgeColor = Color(0xFF2196F3),
        timeAgo = "2h ago"
    ),
    ActivityData(
        id = "3",
        icon = Icons.Default.CheckCircle,
        iconBgColor = Color(0xFF4CAF50).copy(alpha = 0.15f),
        iconTintColor = Color(0xFF4CAF50),
        category = "Status Update",
        title = "Booking Accepted",
        description = "Maize Shipment • Kisumu → Nairobi\nCarrier accepted offer.",
        statusBadge = "ACCEPTED",
        statusBadgeColor = Color(0xFF4CAF50),
        timeAgo = "4h ago"
    ),
    ActivityData(
        id = "4",
        icon = Icons.Default.Warning,
        iconBgColor = Color(0xFF9E9E9E).copy(alpha = 0.15f),
        iconTintColor = Color(0xFF9E9E9E),
        category = "System Alert",
        topRightText = "Yesterday",
        title = "Document Verified",
        description = "Truck KCB 456C\nVerification successful.",
        timeAgo = null
    )
)

@Preview(showBackground = true)
@Composable
fun ActivityScreenPreview() {
    CargoLinkTheme {
        ActivityScreen()
    }
}
