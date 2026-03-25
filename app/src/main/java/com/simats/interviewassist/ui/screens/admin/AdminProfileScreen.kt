package com.simats.interviewassist.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.simats.interviewassist.data.network.RetrofitClient
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.utils.PreferenceManager
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    preferenceManager: PreferenceManager,
    onBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToReviews: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToAlumniRequests: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val apiService = RetrofitClient.apiService

    var showCreateAdminDialog by remember { mutableStateOf(false) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }

    var adminEmail by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var adminNewPassword by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    var snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = LocalAppColors.current.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 10.sp) },
                    selected = false,
                    onClick = onNavigateToHome,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.5f),
                        unselectedTextColor = LocalAppColors.current.textBody.copy(alpha = 0.5f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Shield, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 10.sp) },
                    selected = false,
                    onClick = onNavigateToDashboard,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.5f),
                        unselectedTextColor = LocalAppColors.current.textBody.copy(alpha = 0.5f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Description, contentDescription = "Reviews") },
                    label = { Text("Reviews", fontSize = 10.sp) },
                    selected = false,
                    onClick = onNavigateToReviews,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.5f),
                        unselectedTextColor = LocalAppColors.current.textBody.copy(alpha = 0.5f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.People, contentDescription = "Users") },
                    label = { Text("Users", fontSize = 10.sp) },
                    selected = false,
                    onClick = onNavigateToUsers,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.5f),
                        unselectedTextColor = LocalAppColors.current.textBody.copy(alpha = 0.5f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.AssignmentLate, contentDescription = "Requests") },
                    label = { Text("Requests", fontSize = 10.sp) },
                    selected = false,
                    onClick = onNavigateToAlumniRequests,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.5f),
                        unselectedTextColor = LocalAppColors.current.textBody.copy(alpha = 0.5f)
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LocalAppColors.current.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Profile Image with Shield Badge
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = LocalAppColors.current.primaryHighlight,
                    border = androidx.compose.foundation.BorderStroke(2.dp, LocalAppColors.current.surface)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (preferenceManager.getUserName().isNotEmpty()) 
                                preferenceManager.getUserName().take(1).uppercase() else "UP",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalAppColors.current.primary
                        )
                    }
                }
                Surface(
                    modifier = Modifier.size(32.dp).offset(x = 4.dp, y = 4.dp),
                    shape = CircleShape,
                    color = LocalAppColors.current.purple, // Purple badge color
                    border = androidx.compose.foundation.BorderStroke(2.dp, LocalAppColors.current.surface)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Security,
                            null,
                            modifier = Modifier.size(18.dp),
                            tint = LocalAppColors.current.surface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Name and Role
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = preferenceManager.getUserName(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalAppColors.current.textTitle
                )
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    color = LocalAppColors.current.primaryHighlight,
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        "Admin",
                        fontSize = 12.sp,
                        color = LocalAppColors.current.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Super Admin",
                fontSize = 16.sp,
                color = LocalAppColors.current.textBody,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Account Info Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                color = LocalAppColors.current.surface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Account Info",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    AccountDetailItem(
                        icon = Icons.Outlined.Email,
                        label = "Email",
                        value = preferenceManager.getEmail(),
                        iconBgColor = LocalAppColors.current.purpleBg
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    

                    AccountDetailItem(
                        icon = Icons.Outlined.CalendarToday,
                        label = "Member Since",
                        value = "January 2023",
                        iconBgColor = LocalAppColors.current.primaryHighlight
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Admin Management Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                color = LocalAppColors.current.surface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = LocalAppColors.current.primaryHighlight
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    null,
                                    modifier = Modifier.size(20.dp),
                                    tint = LocalAppColors.current.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Admin Management",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalAppColors.current.textTitle
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminManagementItem(
                            icon = Icons.Outlined.PersonAdd,
                            title = "Add New Admin",
                            subtitle = "Create a new administrative account",
                            color = LocalAppColors.current.primary,
                            onClick = { 
                                adminEmail = ""
                                adminPassword = ""
                                showCreateAdminDialog = true 
                            }
                        )
                        
                        AdminManagementItem(
                            icon = Icons.Outlined.Password,
                            title = "Reset Admin Password",
                            subtitle = "Update another admin's credentials",
                            color = LocalAppColors.current.error,
                            onClick = { 
                                adminEmail = ""
                                adminNewPassword = ""
                                showResetPasswordDialog = true 
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clickable { onNavigateToSettings() },
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Settings, null, tint = LocalAppColors.current.iconTint, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Settings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LocalAppColors.current.textTitle,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ChevronRight, null, tint = LocalAppColors.current.borderLight)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Out Button
            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.error),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ExitToApp, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showCreateAdminDialog) {
            AlertDialog(
                onDismissRequest = { showCreateAdminDialog = false },
                title = { Text("Create New Admin") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = adminEmail,
                            onValueChange = { adminEmail = it },
                            label = { Text("Admin Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = { adminPassword = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                try {
                                    val response = apiService.createAdmin(com.simats.interviewassist.data.models.CreateAdminRequest(adminEmail, adminPassword))
                                    if (response.isSuccessful) {
                                        snackbarHostState.showSnackbar(response.body()?.get("message") ?: "Admin created successfully")
                                        showCreateAdminDialog = false
                                    } else {
                                        val errorMsg = try {
                                            val errorBody = response.errorBody()?.string()
                                            val json = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                                            json["error"]?.toString() ?: response.message()
                                        } catch (e: Exception) {
                                            response.message()
                                        }
                                        snackbarHostState.showSnackbar("Error: $errorMsg")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Failed: ${e.message}")
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading && adminEmail.isNotEmpty() && adminPassword.isNotEmpty()
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        else Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateAdminDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showResetPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showResetPasswordDialog = false },
                title = { Text("Reset Admin Password") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = adminEmail,
                            onValueChange = { adminEmail = it },
                            label = { Text("Admin Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = adminNewPassword,
                            onValueChange = { adminNewPassword = it },
                            label = { Text("New Password") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                try {
                                    val response = apiService.updateAdminPassword(com.simats.interviewassist.data.models.UpdateAdminPasswordRequest(adminEmail, adminNewPassword))
                                    if (response.isSuccessful) {
                                        snackbarHostState.showSnackbar(response.body()?.get("message") ?: "Password updated successfully")
                                        showResetPasswordDialog = false
                                    } else {
                                        val errorMsg = try {
                                            val errorBody = response.errorBody()?.string()
                                            val json = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                                            json["error"]?.toString() ?: response.message()
                                        } catch (e: Exception) {
                                            response.message()
                                        }
                                        snackbarHostState.showSnackbar("Error: $errorMsg")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Failed: ${e.message}")
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading && adminEmail.isNotEmpty() && adminNewPassword.isNotEmpty()
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        else Text("Update")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetPasswordDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun AccountDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    iconBgColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = iconBgColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = LocalAppColors.current.purple) // Icon color
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = LocalAppColors.current.iconTint)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = LocalAppColors.current.textTitle)
        }
    }
}

@Composable
fun AdminManagementItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = LocalAppColors.current.background.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, modifier = Modifier.size(20.dp), tint = color)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
                Text(subtitle, fontSize = 11.sp, color = LocalAppColors.current.textSecondary)
            }
            Icon(Icons.Default.ChevronRight, null, tint = LocalAppColors.current.borderLight, modifier = Modifier.size(16.dp))
        }
    }
}
