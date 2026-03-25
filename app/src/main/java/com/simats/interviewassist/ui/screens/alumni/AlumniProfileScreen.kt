package com.simats.interviewassist.ui.screens.alumni

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.*
import com.simats.interviewassist.ui.screens.student.ProfileActionItem
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.utils.ProfilePicManager
import coil.compose.SubcomposeAsyncImage
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImageContent


import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.simats.interviewassist.utils.PreferenceManager
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.data.models.UserProfileResponse
import kotlinx.coroutines.launch
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniProfileScreen(
    preferenceManager: PreferenceManager,
    viewUserId: Int? = null,
    onNavigateToHome: () -> Unit,
    onNavigateToAssist: () -> Unit = {},
    onNavigateToPosts: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToShareExperience: () -> Unit = {},
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

    LaunchedEffect(viewUserId) {
        val targetUserId = viewUserId ?: preferenceManager.getUserId()
        if (targetUserId != -1) {
            try {
                val response = RetrofitClient.apiService.getUserProfile(targetUserId)
                if (response.isSuccessful) {
                    userProfile = response.body()
                    // Only update preferences/cache if viewing own profile
                    if (viewUserId == null || viewUserId == preferenceManager.getUserId()) {
                        userProfile?.let { 
                            preferenceManager.saveUserDetails(it.firstName, it.lastName, it.email)
                            ProfilePicManager.saveBase64Image(context, it.profile?.profilePic, preferenceManager)
                        }
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
                shadowElevation = 16.dp,
                color = LocalAppColors.current.surface
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    windowInsets = NavigationBarDefaults.windowInsets
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Outlined.Home, contentDescription = "Home", modifier = Modifier.size(26.dp)) },
                        label = { Text("Home", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        selected = false,
                        onClick = onNavigateToHome,
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.4f),
                            unselectedTextColor = LocalAppColors.current.textBody.copy(alpha = 0.4f),
                            indicatorColor = LocalAppColors.current.primaryHighlight.copy(alpha = 0.5f)
                        )
                    )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Add, contentDescription = "Add", modifier = Modifier.size(26.dp)) },
                        label = { Text("Add", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        selected = false,
                        onClick = onNavigateToShareExperience,
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.4f),
                            unselectedTextColor = LocalAppColors.current.textBody.copy(alpha = 0.4f),
                            indicatorColor = LocalAppColors.current.primaryHighlight.copy(alpha = 0.5f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Outlined.Handshake, contentDescription = "Assist", modifier = Modifier.size(26.dp)) },
                        label = { Text("Assist", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        selected = false,
                        onClick = onNavigateToAssist,
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.4f),
                            unselectedTextColor = LocalAppColors.current.textBody.copy(alpha = 0.4f),
                            indicatorColor = LocalAppColors.current.primaryHighlight.copy(alpha = 0.5f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Outlined.Description, contentDescription = "Posts", modifier = Modifier.size(26.dp)) },
                        label = { Text("Posts", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        selected = false,
                        onClick = onNavigateToPosts,
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.4f),
                            unselectedTextColor = LocalAppColors.current.textBody.copy(alpha = 0.4f),
                            indicatorColor = LocalAppColors.current.primaryHighlight.copy(alpha = 0.5f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(26.dp)) },
                        label = { Text("Profile", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
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
        ) {
            // Premium Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = LocalAppColors.current.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onNavigateToHome,
                        modifier = Modifier.background(LocalAppColors.current.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LocalAppColors.current.textTitle)
                    }
                    Text(
                        text = if (viewUserId != null && viewUserId != preferenceManager.getUserId()) "Profile Details" else "My Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle
                    )
                    Spacer(modifier = Modifier.size(48.dp)) // Maintain horizontal alignment
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Spacer(modifier = Modifier.height(16.dp))
            // Profile Info Center
            // Centered Profile Box
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = LocalAppColors.current.surface,
                    border = BorderStroke(4.dp, LocalAppColors.current.primaryHighlight),
                    shadowElevation = 8.dp
                ) {
                    val profilePicTarget = if (viewUserId == null || viewUserId == preferenceManager.getUserId()) {
                        preferenceManager.getProfilePicPath()
                    } else {
                        userProfile?.profile?.profilePic
                    }

                    val fallbackContent: @Composable () -> Unit = {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.background(LocalAppColors.current.primaryHighlight).fillMaxSize()) {
                            val initials = if (userProfile != null) {
                                val f = userProfile?.firstName ?: ""
                                val l = userProfile?.lastName ?: ""
                                if (f.isNotEmpty() && l.isNotEmpty()) "${f[0]}${l[0]}" else "A"
                            } else {
                                if (firstName.isNotEmpty() && lastName.isNotEmpty()) "${firstName[0]}${lastName[0]}" else "A"
                            }
                            Text(
                                text = initials.uppercase(),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = LocalAppColors.current.primary
                            )
                        }
                    }

                    if (!profilePicTarget.isNullOrEmpty()) {
                                val imageModel: Any = when {
                                    profilePicTarget.length > 1000 && (profilePicTarget.startsWith("data:image") || profilePicTarget.length > 1000) -> {
                                        val cleanBase64 = if (profilePicTarget.contains(",")) profilePicTarget.substringAfter(",") else profilePicTarget
                                        try {
                                            android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                                        } catch (e: Exception) {
                                            ByteArray(0)
                                        }
                                    }
                                    profilePicTarget.startsWith("http") -> profilePicTarget
                                    profilePicTarget.startsWith("/") -> File(profilePicTarget)
                                    else -> "${RetrofitClient.BASE_URL.removeSuffix("/")}/${profilePicTarget.removePrefix("/")}"
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
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = LocalAppColors.current.success,
                    border = BorderStroke(3.dp, LocalAppColors.current.surface),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Verified, null, modifier = Modifier.size(20.dp), tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (userProfile != null) "${userProfile?.firstName} ${userProfile?.lastName}" else "$firstName $lastName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = LocalAppColors.current.textTitle
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 8.dp)) {
                Surface(
                    color = LocalAppColors.current.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.WorkOutline, null, modifier = Modifier.size(14.dp), tint = LocalAppColors.current.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        val profile = userProfile?.profile
                        val position = if (profile != null && !profile.currentCompany.isNullOrBlank()) {
                            "${profile.designation ?: "Alumni"} @ ${profile.currentCompany}"
                        } else {
                            major.ifBlank { "Alumni" }
                        }
                        Text(
                            text = position,
                            style = MaterialTheme.typography.titleSmall,
                            color = LocalAppColors.current.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Class of ${if (userProfile != null) userProfile?.profile?.expectedGradYear ?: "N/A" else gradYear.ifBlank { "N/A" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalAppColors.current.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Stats Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AlumniStatCard(
                    label = "Posts",
                    value = (userProfile?.experiencesCount ?: 0).toString(),
                    labelColor = LocalAppColors.current.primary,
                    modifier = Modifier.weight(1f)
                )
                AlumniStatCard(
                    label = "Helpful",
                    value = (userProfile?.totalHelpfulVotes ?: 0).toString(),
                    icon = Icons.Default.ThumbUp,
                    labelColor = LocalAppColors.current.success,
                    modifier = Modifier.weight(1f)
                )
                AlumniStatCard(
                    label = "Assisted",
                    value = (userProfile?.assistedCount ?: 0).toString(),
                    icon = Icons.Default.Handshake,
                    labelColor = LocalAppColors.current.warning,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Professional details if viewing someone else
            if (viewUserId != null && viewUserId != preferenceManager.getUserId()) {
                userProfile?.profile?.let { profile ->
                    ProfessionalDetailsSection(profile)
                }
            } else {
                // Action List (Only show for self)
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProfileActionItem(Icons.Outlined.PersonOutline, "Edit Profile", onClick = onNavigateToEditProfile)
                    ProfileActionItem(Icons.Outlined.Settings, "Settings", onClick = onNavigateToSettings)
                }
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
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Sign Out", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Composable
fun AlumniStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    labelColor: Color = LocalAppColors.current.primary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = LocalAppColors.current.surface,
        border = BorderStroke(1.dp, LocalAppColors.current.divider),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = LocalAppColors.current.textTitle
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, null, modifier = Modifier.size(12.dp), tint = labelColor)
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
@Composable
fun ProfessionalDetailsSection(profile: com.simats.interviewassist.data.models.ProfileData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = LocalAppColors.current.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "PROFESSIONAL INFORMATION",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = LocalAppColors.current.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            ProfileDetailRow(Icons.Outlined.Business, "Company", profile.currentCompany ?: "Not shared")
            ProfileDetailRow(Icons.Outlined.WorkOutline, "Designation", profile.designation ?: "Alumni")
            ProfileDetailRow(Icons.Outlined.School, "Major", profile.major ?: "N/A")
            ProfileDetailRow(Icons.Outlined.Language, "LinkedIn", profile.linkedinUrl ?: "Not provided", isLink = true)
            
            if (!profile.bio.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    color = LocalAppColors.current.surface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "BIO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = LocalAppColors.current.textSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = profile.bio!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LocalAppColors.current.textBody,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, isLink: Boolean = false) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(10.dp),
            color = LocalAppColors.current.surface
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(16.dp), tint = LocalAppColors.current.primary)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = LocalAppColors.current.textSecondary
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLink && value != "Not provided") LocalAppColors.current.primary else LocalAppColors.current.textTitle,
                maxLines = 1
            )
        }
    }
}
