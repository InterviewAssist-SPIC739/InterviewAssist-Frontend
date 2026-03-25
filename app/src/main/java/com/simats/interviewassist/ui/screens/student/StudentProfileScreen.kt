package com.simats.interviewassist.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.utils.PreferenceManager
import com.simats.interviewassist.utils.ProfilePicManager
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.data.models.UserProfileResponse
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun StudentProfileScreen(
    preferenceManager: PreferenceManager,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToHelpSupport: () -> Unit = {},
    onNavigateToMyQuestions: () -> Unit = {},
    onNavigateToBecomeAlumni: () -> Unit = {},
    onNavigateToSaved: () -> Unit = {},
    onSignOut: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var userProfile by remember { mutableStateOf<UserProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val context = LocalContext.current
    
    // Reactive states from preferences for instant loading
    val firstName by preferenceManager.firstNameState
    val lastName by preferenceManager.lastNameState
    val email by preferenceManager.emailState
    val major by preferenceManager.majorState
    val gradYear by preferenceManager.gradYearState
    val currentYear by preferenceManager.currentYearState
    val bio by preferenceManager.bioState
    val phoneNumber by preferenceManager.phoneNumberState
    val secondaryEmail by preferenceManager.secondaryEmailState

    LaunchedEffect(Unit) {
        val userId = preferenceManager.getUserId()
        if (userId != -1) {
            try {
                val response = RetrofitClient.apiService.getUserProfile(userId)
                if (response.isSuccessful) {
                    userProfile = response.body()
                    // Update preferences with latest name/email from backend
                    userProfile?.let { 
                        preferenceManager.saveUserDetails(it.firstName, it.lastName, it.email)
                        // Cache profile picture locally
                        ProfilePicManager.saveBase64Image(context, it.profile?.profilePic, preferenceManager)
                    }
                }
            } catch (e: Exception) {
                // Keep local data on error
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }
    Scaffold(
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = LocalAppColors.current.surface
            ) {
                NavigationBar(
                    containerColor = LocalAppColors.current.surface,
                    tonalElevation = 0.dp,
                    windowInsets = NavigationBarDefaults.windowInsets
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(24.dp)) },
                        label = { Text("Home", style = MaterialTheme.typography.labelMedium) },
                        selected = false,
                        onClick = onNavigateToHome,
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = LocalAppColors.current.textSecondary.copy(alpha = 0.6f),
                            unselectedTextColor = LocalAppColors.current.textSecondary.copy(alpha = 0.6f),
                            indicatorColor = LocalAppColors.current.primaryHighlight
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.BookmarkBorder, contentDescription = "Saved", modifier = Modifier.size(24.dp)) },
                        label = { Text("Saved", style = MaterialTheme.typography.labelMedium) },
                        selected = false,
                        onClick = onNavigateToSaved,
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = LocalAppColors.current.textSecondary.copy(alpha = 0.6f),
                            unselectedTextColor = LocalAppColors.current.textSecondary.copy(alpha = 0.6f),
                            indicatorColor = LocalAppColors.current.primaryHighlight
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Upgrade", modifier = Modifier.size(24.dp)) },
                        label = { Text("Upgrade", style = MaterialTheme.typography.labelMedium) },
                        selected = false,
                        onClick = onNavigateToBecomeAlumni,
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = LocalAppColors.current.textSecondary.copy(alpha = 0.6f),
                            unselectedTextColor = LocalAppColors.current.textSecondary.copy(alpha = 0.6f),
                            indicatorColor = LocalAppColors.current.primaryHighlight
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(24.dp)) },
                        label = { Text("Profile", style = MaterialTheme.typography.labelMedium) },
                        selected = true,
                        onClick = { },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LocalAppColors.current.primary,
                            selectedTextColor = LocalAppColors.current.primary,
                            indicatorColor = LocalAppColors.current.primaryHighlight
                        )
                    )
                }
            }
        },
        containerColor = LocalAppColors.current.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(LocalAppColors.current.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LocalAppColors.current.textTitle)
                }
                Text(
                    text = "My Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LocalAppColors.current.textTitle
                )
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.background(LocalAppColors.current.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = LocalAppColors.current.textTitle)
                }
            }

            // Profile Header
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = LocalAppColors.current.surface,
                    border = BorderStroke(4.dp, LocalAppColors.current.primaryHighlight),
                    shadowElevation = 8.dp
                ) {
                    val profilePicPath by preferenceManager.profilePicPathState
                    val initialsText = (if (userProfile != null) "${userProfile?.firstName?.firstOrNull() ?: ""}${userProfile?.lastName?.firstOrNull() ?: ""}" else "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}").uppercase()

                    val fallbackContent: @Composable () -> Unit = {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(LocalAppColors.current.primaryHighlight)) {
                            Text(
                                text = initialsText,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = LocalAppColors.current.primary
                            )
                        }
                    }

                    if (!profilePicPath.isNullOrEmpty()) {
                        val imageModel: Any = when {
                            profilePicPath!!.length > 1000 -> {
                                val cleanBase64 = if (profilePicPath!!.contains(",")) profilePicPath!!.substringAfter(",") else profilePicPath!!
                                try {
                                    Base64.decode(cleanBase64, Base64.DEFAULT)
                                } catch (e: Exception) {
                                    ByteArray(0)
                                }
                            }
                            profilePicPath!!.startsWith("http") -> profilePicPath!!
                            profilePicPath!!.startsWith("/") -> File(profilePicPath!!)
                            else -> "${RetrofitClient.BASE_URL.removeSuffix("/")}/${profilePicPath!!.removePrefix("/")}"
                        }
                        SubcomposeAsyncImage(
                            model = imageModel,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            loading = { fallbackContent() },
                            error = { fallbackContent() }
                        )
                    } else {
                        fallbackContent()
                    }
                }
                IconButton(
                    onClick = onNavigateToEditProfile,
                    modifier = Modifier
                        .size(36.dp)
                        .background(LocalAppColors.current.primary, CircleShape)
                        .border(2.dp, LocalAppColors.current.surface, CircleShape)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = LocalAppColors.current.surface)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = if (userProfile != null) "${userProfile?.firstName} ${userProfile?.lastName}" else "$firstName $lastName", 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.ExtraBold, 
                color = LocalAppColors.current.textTitle
            )
            Text(
                text = (userProfile?.profile?.major ?: major).ifBlank { "Student" }, 
                style = MaterialTheme.typography.bodyMedium, 
                color = LocalAppColors.current.textSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    icon = Icons.Outlined.BookmarkBorder,
                    value = "${userProfile?.savedCount ?: 0}",
                    label = "Saved",
                    onClick = onNavigateToSaved,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Outlined.HelpOutline,
                    value = "${userProfile?.questionsCount ?: 0}",
                    label = "My Questions",
                    onClick = onNavigateToMyQuestions,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Academic Info
            AcademicInfoSection(
                userProfile = userProfile, 
                cachedMajor = major,
                cachedCurrentYear = currentYear,
                cachedGradYear = gradYear
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            // Contact Info
            ContactInfoSection(
                userProfile = userProfile,
                cachedEmail = email,
                cachedSecondaryEmail = secondaryEmail,
                cachedPhone = phoneNumber
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Become an Alumni Banner
            AlumniBanner(onClick = onNavigateToBecomeAlumni)

            Spacer(modifier = Modifier.height(24.dp))

            // Action Items
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileActionItem(Icons.Outlined.PersonOutline, "Edit Profile", onClick = onNavigateToEditProfile)
                ProfileActionItem(Icons.Outlined.Settings, "App Settings", onClick = onNavigateToSettings)
                ProfileActionItem(Icons.Outlined.HelpOutline, "Help & Support", onClick = onNavigateToHelpSupport)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Out Button
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = LocalAppColors.current.error
                ),
                border = BorderStroke(1.dp, LocalAppColors.current.error.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Sign Out", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Version 1.0.0 • Interview Assist", 
                fontSize = 12.sp, 
                color = LocalAppColors.current.iconTint
            )
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector, 
    value: String, 
    label: String, 
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = LocalAppColors.current.surface,
        border = BorderStroke(1.dp, LocalAppColors.current.divider),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = LocalAppColors.current.primaryHighlight,
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = LocalAppColors.current.primary, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LocalAppColors.current.textTitle
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = LocalAppColors.current.textSecondary
            )
        }
    }
}

@Composable
fun AcademicInfoSection(
    userProfile: UserProfileResponse?, 
    cachedMajor: String,
    cachedCurrentYear: String,
    cachedGradYear: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LocalAppColors.current.surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Academic Info", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold, 
                color = LocalAppColors.current.textTitle
            )
            Spacer(modifier = Modifier.height(20.dp))
            AcademicItem(
                Icons.Outlined.School, 
                "Class of", 
                if (userProfile != null) {
                    "${userProfile.profile?.currentYear ?: "N/A"} (${userProfile.profile?.expectedGradYear ?: "N/A"})"
                } else if (cachedCurrentYear.isNotBlank()) {
                    "$cachedCurrentYear ($cachedGradYear)"
                } else {
                    "Not specified"
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            AcademicItem(
                Icons.Outlined.AutoStories, 
                "Major", 
                (userProfile?.profile?.major ?: cachedMajor).ifBlank { "Not specified" }
            )
        }
    }
}

@Composable
fun ContactInfoSection(
    userProfile: UserProfileResponse?,
    cachedEmail: String,
    cachedSecondaryEmail: String,
    cachedPhone: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LocalAppColors.current.surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Contact Info", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold, 
                color = LocalAppColors.current.textTitle
            )
            Spacer(modifier = Modifier.height(20.dp))
            AcademicItem(
                Icons.Outlined.Email, 
                "Email", 
                userProfile?.email ?: cachedEmail
            )
            Spacer(modifier = Modifier.height(16.dp))
            AcademicItem(
                Icons.Outlined.Email, 
                "Secondary Email", 
                (userProfile?.secondaryEmail ?: cachedSecondaryEmail).ifBlank { "Not added" }
            )
            Spacer(modifier = Modifier.height(16.dp))
            AcademicItem(
                Icons.Outlined.Phone, 
                "Phone", 
                (userProfile?.profile?.phoneNumber ?: cachedPhone).ifBlank { "Not added" }
            )
        }
    }
}

@Composable
fun AcademicItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = LocalAppColors.current.surfaceHighlight
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = LocalAppColors.current.primary)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.textSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
        }
    }
}

@Composable
fun AlumniBanner(onClick: () -> Unit = {}) {
    Surface(
        color = LocalAppColors.current.surfaceHighlight,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(LocalAppColors.current.primary, Color(0xFF1E3A8A))
                    )
                )
                .clickable { onClick() }
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Become an Alumni", 
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White, 
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "New", 
                            color = Color.White, 
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Got placed? Share your experiences and help juniors achieve their goals!", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 20.sp
                )
            }
            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileActionItem(icon: ImageVector, title: String, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = LocalAppColors.current.surface
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = LocalAppColors.current.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = LocalAppColors.current.primary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                title, 
                modifier = Modifier.weight(1f), 
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold, 
                color = LocalAppColors.current.textTitle
            )
            Icon(Icons.Default.ChevronRight, null, tint = LocalAppColors.current.iconTint.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        }
    }
}
