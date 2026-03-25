package com.simats.interviewassist.ui.screens.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
fun StudentPrivacySecurityScreen(
    preferenceManager: PreferenceManager,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {

    var isDownloading by remember { mutableStateOf(false) }
    
    var show2FADialog by remember { mutableStateOf(false) }
    var is2FAEnabled by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }
    var secondaryEmail by remember { mutableStateOf("") }
    var isLoadingProfile by remember { mutableStateOf(true) }
    var isSaving2FA by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    
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
                        fontSize = 18.sp, 
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Privacy Section
            PrivacySecuritySectionTitle("Privacy")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface
            ) {
                Column {

                    PrivacyNavigationItem(
                        icon = Icons.Outlined.FileDownload,
                        title = "Download My Data",
                        subtitle = if (isDownloading) "Generating PDF..." else "Get a copy of your data",
                        onClick = {
                            if (isDownloading) return@PrivacyNavigationItem
                            
                            val userId = preferenceManager.getUserId()
                            if (userId == -1) {
                                scope.launch { snackbarHostState.showSnackbar("User not found. Please log in again.") }
                                return@PrivacyNavigationItem
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

            Spacer(modifier = Modifier.height(32.dp))

            // Security Section
            PrivacySecuritySectionTitle("Security")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface
            ) {
                Column {
                    PrivacyNavigationItem(
                        icon = Icons.Outlined.Shield,
                        title = "Two-Factor Authentication",
                        subtitle = if (isLoadingProfile) "Loading..." else if (is2FAEnabled) "Enabled" else "Disabled",
                        onClick = {
                            if (!isLoadingProfile) {
                                show2FADialog = true
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Danger Zone Section
            PrivacySecuritySectionTitle("Danger Zone")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface
            ) {
                Column {
                    PrivacyNavigationItem(
                        icon = Icons.Outlined.Delete,
                        title = "Delete Account",
                        subtitle = "Permanently delete your account",
                        isDestructive = true,
                        onClick = { showDeleteDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
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
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isSaving2FA = true
                            scope.launch {
                                try {
                                    val request = com.simats.interviewassist.data.models.Toggle2FARequest(
                                        role = "Student",
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

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
                containerColor = LocalAppColors.current.surface,
                title = { 
                    Text(
                        "Delete Account", 
                        color = LocalAppColors.current.error,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Text(
                        "Are you sure you want to permanently delete your account? This action cannot be undone and all your data will be cleared.",
                        color = LocalAppColors.current.textSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isDeleting = true
                            scope.launch {
                                try {
                                    val response = RetrofitClient.apiService.deleteAccount()
                                    if (response.isSuccessful) {
                                        snackbarHostState.showSnackbar("Account deleted successfully")
                                        preferenceManager.clear()
                                        onLogout()
                                    } else {
                                        val errorMsg = response.errorBody()?.string() ?: "Failed to delete account"
                                        snackbarHostState.showSnackbar(errorMsg)
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                } finally {
                                    isDeleting = false
                                    showDeleteDialog = false
                                }
                            }
                        },
                        enabled = !isDeleting,
                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.error)
                    ) {
                        Text(if (isDeleting) "Deleting..." else "Delete", color = LocalAppColors.current.surface)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false },
                        enabled = !isDeleting
                    ) {
                        Text("Cancel", color = LocalAppColors.current.textSecondary)
                    }
                }
            )
        }
    }
}

@Composable
fun PrivacySecuritySectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = LocalAppColors.current.textSecondary,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )
}



@Composable
fun PrivacyNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val tintColor = if (isDestructive) LocalAppColors.current.error else LocalAppColors.current.primary
    val iconBgColor = if (isDestructive) LocalAppColors.current.errorBg else LocalAppColors.current.surfaceHighlight
    val titleColor = if (isDestructive) LocalAppColors.current.error else LocalAppColors.current.textTitle

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = iconBgColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = tintColor)
            }
        }
        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
            Text(subtitle, fontSize = 12.sp, color = LocalAppColors.current.textSecondary)
        }
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = if (isDestructive) LocalAppColors.current.error.copy(alpha = 0.5f) else LocalAppColors.current.borderLight,
            modifier = Modifier.size(20.dp)
        )
    }
}
