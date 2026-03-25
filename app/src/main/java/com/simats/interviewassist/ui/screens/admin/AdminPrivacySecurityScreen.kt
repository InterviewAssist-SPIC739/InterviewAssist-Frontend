package com.simats.interviewassist.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.utils.PreferenceManager
import com.simats.interviewassist.utils.PdfManager
import com.simats.interviewassist.data.network.RetrofitClient
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPrivacySecurityScreen(
    preferenceManager: PreferenceManager,
    onBack: () -> Unit = {}
) {
    var isDownloading by remember { mutableStateOf(false) }
    
    var show2FADialog by remember { mutableStateOf(false) }
    var is2FAEnabled by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }
    var secondaryEmail by remember { mutableStateOf("") }
    var isLoadingProfile by remember { mutableStateOf(true) }
    var isSaving2FA by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        val userId = preferenceManager.getUserId()
        if (userId != -1) {
            try {
                val response = RetrofitClient.apiService.getUserProfile(userId)
                if (response.isSuccessful) {
                    val userData = response.body()
                    if (userData != null) {
                        is2FAEnabled = userData.twoFactorEnabled == true
                        phoneNumber = userData.phoneNumber ?: userData.profile?.phoneNumber ?: ""
                        secondaryEmail = userData.secondaryEmail ?: ""
                    }
                }
            } catch (e: Exception) {
                // Ignore
            } finally {
                isLoadingProfile = false
            }
        } else {
            isLoadingProfile = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Privacy & Security", 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LocalAppColors.current.textTitle)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LocalAppColors.current.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LocalAppColors.current.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LocalAppColors.current.background)
                .padding(padding),
            contentPadding = PaddingValues(24.dp)
        ) {
            // Privacy Section
            item {
                Text(
                    "Privacy",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = LocalAppColors.current.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface)
                ) {
                    Column {
                        PrivacyNavItem(
                            icon = Icons.Outlined.FileDownload,
                            label = "Download My Data",
                            subtitle = if (isDownloading) "Generating PDF..." else "Get a copy of your data",
                            onClick = {
                                if (isDownloading) return@PrivacyNavItem
                                
                                val userId = preferenceManager.getUserId()
                                if (userId == -1) {
                                    scope.launch { snackbarHostState.showSnackbar("User not found. Please log in again.") }
                                    return@PrivacyNavItem
                                }
                                
                                isDownloading = true
                                scope.launch {
                                    try {
                                        val response = RetrofitClient.apiService.getUserProfile(userId)
                                        if (response.isSuccessful) {
                                            val userData = response.body()
                                            if (userData != null) {
                                                val uri = PdfManager.generateUserDataPdf(context, userData)
                                                if (uri != null) {
                                                    snackbarHostState.showSnackbar("Data downloaded to your Downloads folder.")
                                                } else {
                                                    snackbarHostState.showSnackbar("Failed to generate PDF.")
                                                }
                                            } else {
                                                snackbarHostState.showSnackbar("Profile data is empty.")
                                            }
                                        } else {
                                            snackbarHostState.showSnackbar("Failed to fetch profile: ${response.message()}")
                                        }
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Error: ${e.message}")
                                    } finally {
                                        isDownloading = false
                                    }
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Security Section
            item {
                Text(
                    "Security",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = LocalAppColors.current.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface)
                ) {
                    Column {
                        PrivacyNavItem(
                            icon = Icons.Outlined.Shield,
                            label = "Two-Factor Authentication",
                            subtitle = if (isLoadingProfile) "Loading..." else if (is2FAEnabled) "Enabled" else "Disabled",
                            onClick = {
                                if (!isLoadingProfile) {
                                    show2FADialog = true
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Danger Zone
            item {
                Text(
                    "Danger Zone",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = LocalAppColors.current.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = LocalAppColors.current.errorBg
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(20.dp), tint = LocalAppColors.current.error)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Delete Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalAppColors.current.error
                            )
                            Text(
                                text = "Permanently delete your account",
                                fontSize = 12.sp,
                                color = LocalAppColors.current.textSecondary
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            null,
                            tint = Color(0xFFFCA5A5),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        if (show2FADialog) {
            AlertDialog(
                onDismissRequest = { 
                    if (!isSaving2FA) show2FADialog = false 
                },
                containerColor = LocalAppColors.current.surface,
                title = { 
                    Text(
                        "Two-Factor Authentication", 
                        color = LocalAppColors.current.textTitle,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Enable 2FA via Email", 
                                modifier = Modifier.weight(1f), 
                                color = LocalAppColors.current.textTitle,
                                fontSize = 16.sp
                            )
                            Switch(
                                checked = is2FAEnabled,
                                onCheckedChange = { is2FAEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = LocalAppColors.current.surface,
                                    checkedTrackColor = LocalAppColors.current.primary,
                                    uncheckedThumbColor = LocalAppColors.current.surface,
                                    uncheckedTrackColor = LocalAppColors.current.borderLight
                                )
                            )
                        }
                        if (is2FAEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = secondaryEmail,
                                onValueChange = { secondaryEmail = it },
                                label = { Text("Secondary Email for OTP", color = LocalAppColors.current.textSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = LocalAppColors.current.textTitle,
                                    unfocusedTextColor = LocalAppColors.current.textTitle,
                                    focusedBorderColor = LocalAppColors.current.primary,
                                    unfocusedBorderColor = LocalAppColors.current.divider
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Mobile Number", color = LocalAppColors.current.textSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = LocalAppColors.current.textTitle,
                                    unfocusedTextColor = LocalAppColors.current.textTitle,
                                    focusedBorderColor = LocalAppColors.current.primary,
                                    unfocusedBorderColor = LocalAppColors.current.divider
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (is2FAEnabled && phoneNumber.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Phone number required to enable 2FA") }
                                return@Button
                            }
                            isSaving2FA = true
                            scope.launch {
                                try {
                                    val request = com.simats.interviewassist.data.models.Toggle2FARequest(
                                        role = "Admin",
                                        phoneNumber = phoneNumber,
                                        secondaryEmail = secondaryEmail,
                                        enable = is2FAEnabled
                                    )
                                    val response = RetrofitClient.apiService.toggle2FA(request)
                                    if (response.isSuccessful) {
                                        show2FADialog = false
                                        snackbarHostState.showSnackbar("2FA Settings Updated")
                                    } else {
                                        snackbarHostState.showSnackbar("Failed to update 2FA")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                } finally {
                                    isSaving2FA = false
                                }
                            }
                        },
                        enabled = !isSaving2FA,
                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary)
                    ) {
                        Text(if (isSaving2FA) "Saving..." else "Save", color = LocalAppColors.current.surface)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { show2FADialog = false },
                        enabled = !isSaving2FA
                    ) {
                        Text("Cancel", color = LocalAppColors.current.textSecondary)
                    }
                }
            )
        }
    }
}

@Composable
fun PrivacyNavItem(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = LocalAppColors.current.divider
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = LocalAppColors.current.textTitle)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = LocalAppColors.current.textTitle
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = LocalAppColors.current.textSecondary
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = LocalAppColors.current.borderLight,
            modifier = Modifier.size(20.dp)
        )
    }
}
