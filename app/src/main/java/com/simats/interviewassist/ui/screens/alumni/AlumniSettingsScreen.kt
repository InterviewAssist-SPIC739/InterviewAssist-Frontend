package com.simats.interviewassist.ui.screens.alumni

import android.util.Base64

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.simats.interviewassist.ui.theme.*
import com.simats.interviewassist.ui.screens.student.SettingsSectionTitle
import com.simats.interviewassist.ui.screens.student.ToggleSettingsItem
import com.simats.interviewassist.ui.screens.student.NavigationSettingsItem
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.utils.PreferenceManager
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.data.models.UserProfileResponse
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniSettingsScreen(
    preferenceManager: PreferenceManager,
    onBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToHelpSupport: () -> Unit = {},
    onNavigateToPrivacySecurity: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onSignOut: () -> Unit
) {
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    var userProfile by remember { mutableStateOf<UserProfileResponse?>(null) }
    
    val firstName by preferenceManager.firstNameState
    val lastName by preferenceManager.lastNameState

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getUserProfile(preferenceManager.getUserId())
            if (response.isSuccessful) {
                userProfile = response.body()
            }
        } catch (e: Exception) { /* Handle error */ }
    }

    if (showTermsDialog) {
        PolicyDialog(
            title = "Terms of Service",
            content = "1. Acceptance of Terms: By using Interview Assist, you agree to these terms.\n\n2. User Conduct: You agree to share accurate interview experiences and respect other users. No hate speech or harassment is tolerated.\n\n3. Content Ownership: You retain ownership of your posts but grant us a non-exclusive license to display them on the platform.\n\n4. Termination: We reserve the right to suspend or terminate accounts that violate our community guidelines.\n\n5. Liability: Interview Assist is a platform for sharing experiences; we are not responsible for the accuracy of individual posts.",
            onDismiss = { showTermsDialog = false }
        )
    }

    if (showPrivacyDialog) {
        PolicyDialog(
            title = "Privacy Policy",
            content = "1. Data Collection: We collect your name, email, profile details, and any interview experiences you choose to share.\n\n2. Data Usage: Your data is used to provide service, enable networking between alumni and students, and improve platform features.\n\n3. Data Sharing: Your personal contact information is never sold. Interview experiences are shared with the Interview Assist community.\n\n4. Security: We use encryption and two-factor authentication to protect your account and personal data.\n\n5. Your Rights: You have the right to access, update, or delete your account and associated data at any time through the settings.",
            onDismiss = { showPrivacyDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings", 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LocalAppColors.current.textTitle)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LocalAppColors.current.surface)
            )
        },
        containerColor = LocalAppColors.current.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {

            // Preferences Section
            SettingsSectionTitle("Preferences")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface
            ) {
                Column {
                    val darkMode by preferenceManager.isDarkModeState
                    var notifications by remember { mutableStateOf(true) }
                    
                    ToggleSettingsItem(
                        icon = Icons.Outlined.ModeNight,
                        title = "Dark Mode",
                        checked = darkMode,
                        onCheckedChange = { preferenceManager.setDarkMode(it) }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp), color = LocalAppColors.current.divider)
                    ToggleSettingsItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        checked = notifications,
                        onCheckedChange = { notifications = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Account Section
            SettingsSectionTitle("Account")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface
            ) {
                Column {
                    NavigationSettingsItem(Icons.Outlined.Person, "Edit Profile", onClick = onNavigateToEditProfile)
                    Divider(modifier = Modifier.padding(horizontal = 16.dp), color = LocalAppColors.current.divider)
                    NavigationSettingsItem(
                        Icons.Outlined.Lock, 
                        "Change Password",
                        onClick = onNavigateToChangePassword
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp), color = LocalAppColors.current.divider)
                    NavigationSettingsItem(
                        Icons.Outlined.Security, 
                        "Privacy & Security", 
                        onClick = onNavigateToPrivacySecurity
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Support Section
            SettingsSectionTitle("Support")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface
            ) {
                Column {
                    NavigationSettingsItem(Icons.Outlined.HelpOutline, "Help & Support", onClick = onNavigateToHelpSupport)
                    Divider(modifier = Modifier.padding(horizontal = 16.dp), color = LocalAppColors.current.divider)
                    NavigationSettingsItem(Icons.Outlined.Description, "Terms of Service", onClick = { showTermsDialog = true })
                    Divider(modifier = Modifier.padding(horizontal = 16.dp), color = LocalAppColors.current.divider)
                    NavigationSettingsItem(Icons.Outlined.PrivacyTip, "Privacy Policy", onClick = { showPrivacyDialog = true })
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Sign Out Button
            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Interview Assist v1.0.0", 
                    fontSize = 12.sp, 
                    color = LocalAppColors.current.iconTint
                )
                Text(
                    "Made with ❤️ by Alumni", 
                    fontSize = 12.sp, 
                    color = LocalAppColors.current.iconTint,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PolicyDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(content, fontSize = 14.sp, lineHeight = 20.sp, color = LocalAppColors.current.textSecondary)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = LocalAppColors.current.primary)
            }
        },
        containerColor = LocalAppColors.current.surface,
        titleContentColor = LocalAppColors.current.textTitle,
        textContentColor = LocalAppColors.current.textSecondary,
        shape = RoundedCornerShape(20.dp)
    )
}
