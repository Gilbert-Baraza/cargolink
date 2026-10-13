package com.example.cargolink.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.cargolink.presentation.viewmodel.ProfileState
import com.example.cargolink.presentation.viewmodel.ProfileViewModel
import com.example.cargolink.ui.theme.CargoLinkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    onNavigateToTrucks: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val profileState by viewModel.profileState.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var participationType by remember { mutableStateOf("") }

    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Success) {
            val user = (profileState as ProfileState.Success).user
            name = user.name
            phone = user.phone
            companyName = user.companyName ?: ""
            participationType = user.participationType
        }
    }

    val navyColor = Color(0xFF0D2845)
    val goldAccent = Color(0xFFFFB300)
    val goldBorder = Color(0xFFB57A25)
    val lightBgColor = Color(0xFFF4F6F9)
    val textDark = Color(0xFF212529)
    val textMuted = Color(0xFF495057)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBgColor)
    ) {
        when (val state = profileState) {
            is ProfileState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is ProfileState.Error -> {
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
                    Button(onClick = { viewModel.fetchProfile() }) {
                        Text("Retry")
                    }
                }
            }
            is ProfileState.Success -> {
                val user = state.user

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. TOP NAVY HEADER CARD
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = navyColor,
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp)
                        ) {
                            // Top Logo Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = "Logo",
                                        tint = goldAccent,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Avatar, Name, Role & Rating Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Circular Avatar with Amber Ring
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f))
                                        .border(3.dp, goldAccent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile Avatar",
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Name and Role Info
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.name.ifBlank { "ARTHUR KIBET" }.uppercase(),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        text = user.participationType.replace("_", " ").uppercase().ifBlank { "LOGISTICS MANAGER" },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.95f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = user.companyName ?: "KIBET & SONS FREIGHT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.90f)
                                    )
                                }

                                // Rating Badge
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "4.9",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Rating",
                                            tint = goldAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Text(
                                        text = "256 reviews",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.90f),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Verification Badges Row (High Contrast Badges)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                VerificationBadge(label = "ID VERIFIED", iconColor = Color(0xFF1E88E5))
                                VerificationBadge(label = "BUSINESS VERIFIED", iconColor = Color(0xFF1E88E5))
                                VerificationBadge(label = "PHONE VERIFIED", iconColor = Color(0xFFE65100))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. METRICS CARDS ROW (3 Overlapping Summary Cards)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "ACTIVE LISTINGS",
                            value = "12",
                            subtitle = if (user.participationType == "CARGO_OWNER") "Cargo" else "Trucks",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "JOBS COMPLETED",
                            value = "1,542",
                            subtitle = null,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "MEMBER SINCE",
                            value = "2018",
                            subtitle = null,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!isEditing) {
                        // 3. BUSINESS DETAILS SECTION
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "BUSINESS DETAILS",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = navyColor
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                DetailRow(
                                    icon = Icons.Default.LocationOn,
                                    label = "Business Address",
                                    value = "123 Enterprise Rd, Nairobi, Kenya"
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.4f))

                                DetailRow(
                                    icon = Icons.Default.Description,
                                    label = "Company Registration",
                                    value = user.companyName ?: "K&S-FREIGHT-NAIROBI"
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.4f))

                                DetailRow(
                                    icon = Icons.Default.Email,
                                    label = "Contact Email",
                                    value = user.email.ifBlank { "arthur.k@kibetandsons.co.ke" }
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.4f))

                                DetailRow(
                                    icon = Icons.Default.Phone,
                                    label = "Contact Phone",
                                    value = user.phone.ifBlank { "+254 712 345 678" }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4. VEHICLE FLEET SECTION
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (user.participationType == "CARGO_OWNER") "ACTIVE SHIPMENTS" else "VEHICLE FLEET",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = navyColor
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        FleetItem(count = "4x", type = "Flatbed")
                                        FleetItem(count = "2x", type = "Tanker")
                                        FleetItem(count = "2x", type = "Refrigerated")
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("8 Trucks", fontWeight = FontWeight.Bold, color = navyColor)
                                        Text("Active Fleet", style = MaterialTheme.typography.labelSmall, color = textMuted, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 5. RECENT REVIEWS SECTION
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "RECENT REVIEWS",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = navyColor
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                ReviewItem(
                                    reviewerName = "Arthur: cargo owner",
                                    rating = "5",
                                    comment = "Excellent service, on-time delivery. Reliable partner."
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ReviewItem(
                                    reviewerName = "Arthur: another partner",
                                    rating = "5",
                                    comment = "Professional and easy to work with. Handled fragile cargo perfectly."
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 6. ACTION BUTTONS (EDIT PROFILE / SETTINGS / LOGOUT)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isEditing = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.5.dp, goldBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = navyColor)
                            ) {
                                Text("EDIT PROFILE", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onNavigateToTrucks,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.5.dp, navyColor),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = navyColor)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SETTINGS", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = 24.dp)
                        ) {
                            Text(
                                text = "LOGOUT",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    } else {
                        // EDIT PROFILE FORM
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Edit Profile Details",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = navyColor
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Full Name", color = textMuted) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textDark,
                                        unfocusedTextColor = textDark,
                                        focusedBorderColor = navyColor,
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = { Text("Phone Number", color = textMuted) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textDark,
                                        unfocusedTextColor = textDark,
                                        focusedBorderColor = navyColor,
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = companyName,
                                    onValueChange = { companyName = it },
                                    label = { Text("Company Name / Registration", color = textMuted) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textDark,
                                        unfocusedTextColor = textDark,
                                        focusedBorderColor = navyColor,
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = participationType,
                                    onValueChange = { participationType = it },
                                    label = { Text("Role (TRUCK_DRIVER / CARGO_OWNER)", color = textMuted) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textDark,
                                        unfocusedTextColor = textDark,
                                        focusedBorderColor = navyColor,
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { isEditing = false },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = navyColor)
                                    ) {
                                        Text("Cancel", color = navyColor, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.updateProfile(name, phone, participationType, companyName)
                                            isEditing = false
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = navyColor,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
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
fun VerificationBadge(label: String, iconColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF0D2845),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFB57A25)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 6.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF343A40),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D2845),
                fontSize = 22.sp
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF495057),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFB57A25),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF495057),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D2845)
            )
        }
    }
}

@Composable
fun FleetItem(count: String, type: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.LocalShipping,
            contentDescription = null,
            tint = Color(0xFF0D2845),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = "$count $type", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF212529))
    }
}

@Composable
fun ReviewItem(reviewerName: String, rating: String, comment: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8F9FA),
        border = BorderStroke(1.dp, Color(0xFFDEE2E6))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = Color(0xFF0D2845)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reviewerName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D2845)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = rating,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212529)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = comment,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF212529),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    CargoLinkTheme {
        ProfileScreen()
    }
}
