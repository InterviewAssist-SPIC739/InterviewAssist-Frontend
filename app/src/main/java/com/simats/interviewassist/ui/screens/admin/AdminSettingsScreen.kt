package com.simats.interviewassist.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
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
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.utils.PreferenceManager
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    preferenceManager: PreferenceManager,
    onBack: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToHelpSupport: () -> Unit = {},
    onNavigateToPrivacySecurity: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToAlumniRequests: () -> Unit = {},
    onNavigateToReviews: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {}
) {
    val darkModeEnabled by preferenceManager.isDarkModeState
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

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
        bottomBar = {
            NavigationBar(
                containerColor = LocalAppColors.current.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
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
                    icon = { Icon(Icons.Outlined.AssignmentLate, contentDescription = "Requests") },
                    label = { Text("Requests", fontSize = 10.sp) },
                    selected = false,
                    onClick = onNavigateToAlumniRequests,
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
            }
        },
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
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LocalAppColors.current.background)
                .padding(padding),
            contentPadding = PaddingValues(24.dp)
        ) {
            // Preferences Section
            item {
                Text(
                    "Preferences",
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
                        SettingsSwitchItem(
                            icon = Icons.Outlined.Nightlight,
                            label = "Dark Mode",
                            checked = darkModeEnabled,
                            onCheckedChange = { preferenceManager.setDarkMode(it) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = LocalAppColors.current.divider)
                        SettingsSwitchItem(
                            icon = Icons.Outlined.Notifications,
                            label = "Notifications",
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Account Section
            item {
                Text(
                    "Account",
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
                        SettingsNavItem(
                            icon = Icons.Outlined.Lock, 
                            label = "Change Password",
                            onClick = onNavigateToChangePassword
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = LocalAppColors.current.divider)
                        SettingsNavItem(
                            icon = Icons.Outlined.Shield, 
                            label = "Privacy & Security",
                            onClick = onNavigateToPrivacySecurity
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }


            // Sign Out
            item {
                Button(
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.error)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Sign Out", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
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
                        text = "Made with ❤️ by Alumni",
                        fontSize = 12.sp,
                        color = LocalAppColors.current.iconTint,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
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

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
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
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = LocalAppColors.current.textTitle,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = LocalAppColors.current.surface,
                checkedTrackColor = LocalAppColors.current.primary,
                uncheckedThumbColor = LocalAppColors.current.surface,
                uncheckedTrackColor = LocalAppColors.current.borderLight,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun SettingsNavItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
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
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = LocalAppColors.current.textTitle,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = LocalAppColors.current.borderLight,
            modifier = Modifier.size(20.dp)
        )
    }
}
