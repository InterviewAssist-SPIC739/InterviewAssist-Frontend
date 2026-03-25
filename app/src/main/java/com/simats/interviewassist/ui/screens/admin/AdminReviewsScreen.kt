package com.simats.interviewassist.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.simats.interviewassist.data.models.InterviewExperienceResponse
import com.simats.interviewassist.data.models.ReviewExperienceRequest
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.utils.PreferenceManager
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReviewsScreen(
    preferenceManager: PreferenceManager,
    onNavigateToHome: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToAlumniRequests: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToUserProfile: (Int) -> Unit = {},
    onNavigateToReviewDetail: (Int, String) -> Unit = { _, _ -> }
) {
    val sheetState = rememberModalBottomSheetState()
    var showDetailsSheet by remember { mutableStateOf(false) }
    var detailedUser by remember { mutableStateOf<UserReviewItem?>(null) }
    var isProfileLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var pendingExperiences by remember { mutableStateOf<List<InterviewExperienceResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var unreadCount by remember { mutableStateOf(0) }

    fun fetchPendingReviews() {
        scope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.apiService.getPendingExperiences()
                if (response.isSuccessful) {
                    pendingExperiences = (response.body() ?: emptyList()).filter { !it.isUserSuspended }
                } else {
                    Toast.makeText(context, "Failed to load reviews", Toast.LENGTH_SHORT).show()
                }
                
                // Also fetch notifications count for the bottom bar badge
                val notifResponse = RetrofitClient.apiService.getAdminNotifications()
                if (notifResponse.isSuccessful) {
                    unreadCount = notifResponse.body()?.count { !it.isRead } ?: 0
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchPendingReviews()
    }

    suspend fun handleReview(experienceId: Int, status: String) {
        try {
            val response = RetrofitClient.apiService.reviewExperience(experienceId, ReviewExperienceRequest(status))
            if (response.isSuccessful) {
                Toast.makeText(context, "Experience ${status.lowercase()}ed", Toast.LENGTH_SHORT).show()
                fetchPendingReviews()
            } else {
                Toast.makeText(context, "Action failed", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun handleStatusToggle(user: UserReviewItem) {
        try {
            val response = when {
                user.status == "pending" -> RetrofitClient.apiService.approveUser(user.id)
                user.isSuspended -> RetrofitClient.apiService.unsuspendUser(user.id)
                else -> RetrofitClient.apiService.suspendUser(user.id)
            }
            
            if (response.isSuccessful) {
                val action = when {
                    user.status == "pending" -> "approved"
                    user.isSuspended -> "activated"
                    else -> "suspended"
                }
                Toast.makeText(context, "User $action successfully", Toast.LENGTH_SHORT).show()
                
                // Refresh profile data if sheet is still open
                val updatedProfile = RetrofitClient.apiService.getUserProfile(user.id)
                if (updatedProfile.isSuccessful) {
                    updatedProfile.body()?.let { data ->
                        detailedUser = detailedUser?.copy(
                            status = data.status ?: "active",
                            isSuspended = data.isSuspended ?: false
                        )
                    }
                }
            } else {
                Toast.makeText(context, "Failed to update user status", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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
                    icon = { Icon(Icons.Default.AssignmentLate, contentDescription = "Requests") },
                    label = { Text("Requests", fontSize = 10.sp) },
                    selected = false,
                    onClick = onNavigateToAlumniRequests,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.5f),
                        unselectedTextColor = LocalAppColors.current.textBody.copy(alpha = 0.5f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Description, contentDescription = "Reviews") },
                    label = { Text("Reviews", fontSize = 10.sp) },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LocalAppColors.current.primary,
                        selectedTextColor = LocalAppColors.current.primary,
                        indicatorColor = LocalAppColors.current.primaryHighlight
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LocalAppColors.current.background)
                .padding(padding)
        ) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LocalAppColors.current.surface)
                    .padding(24.dp, 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Interview Assist",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle
                    )
                    Surface(
                        color = LocalAppColors.current.purpleBg,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Admin",
                            fontSize = 12.sp,
                            color = LocalAppColors.current.purple,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = LocalAppColors.current.error,
                                        modifier = Modifier.size(8.dp)
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Default.NotificationsNone, "Notifications", tint = LocalAppColors.current.textTitle)
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings", tint = LocalAppColors.current.textTitle)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(LocalAppColors.current.primaryHighlight, CircleShape)
                            .clip(CircleShape)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = if (preferenceManager.getUserName().isNotEmpty()) 
                            preferenceManager.getUserName().take(1).uppercase() else "A"
                        Text(initials, fontSize = 14.sp, color = LocalAppColors.current.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LocalAppColors.current.primary)
                }
            } else if (pendingExperiences.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.ContentPasteOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = LocalAppColors.current.textSecondary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "All Caught Up!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalAppColors.current.textTitle
                        )
                        Text(
                            text = "There are no experiences waiting for review.",
                            fontSize = 14.sp,
                            color = LocalAppColors.current.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { fetchPendingReviews() },
                            colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primaryHighlight, contentColor = LocalAppColors.current.primary)
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Refresh")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Pending Reviews",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalAppColors.current.textTitle
                        )
                        Text(
                            text = "You have ${pendingExperiences.size} experiences to review",
                            fontSize = 14.sp,
                            color = LocalAppColors.current.textSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(pendingExperiences) { experience ->
                        ReviewCard(
                            experience = experience,
                            onClick = { 
                                onNavigateToReviewDetail(experience.id, experience.companyName ?: "Experience") 
                            },
                            onViewProfile = {
                                detailedUser = UserReviewItem(
                                    id = experience.userId,
                                    name = experience.userName,
                                    email = "Loading...", // Will be updated by profile fetch
                                    role = experience.userRole,
                                    profilePic = experience.userProfilePic
                                )
                                showDetailsSheet = true
                                scope.launch {
                                    isProfileLoading = true
                                    try {
                                        val response = RetrofitClient.apiService.getUserProfile(experience.userId)
                                        if (response.isSuccessful) {
                                            response.body()?.let { profileData ->
                                                val prof = profileData.profile
                                                detailedUser = detailedUser?.copy(
                                                    email = profileData.email,
                                                    phoneNumber = prof?.phoneNumber ?: profileData.phoneNumber,
                                                    profilePic = prof?.profilePic ?: profileData.profilePic ?: experience.userProfilePic,
                                                    profile = prof,
                                                    status = profileData.status ?: "active",
                                                    isSuspended = profileData.isSuspended ?: false
                                                )
                                            }
                                        }
                                    } catch (e: Exception) { } finally {
                                        isProfileLoading = false
                                    }
                                }
                            },
                            onApprove = { 
                                scope.launch { handleReview(experience.id, "approved") } 
                            },
                            onReject = { 
                                scope.launch { handleReview(experience.id, "rejected") } 
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDetailsSheet && detailedUser != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                showDetailsSheet = false
                detailedUser = null 
            },
            sheetState = sheetState,
            containerColor = LocalAppColors.current.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = LocalAppColors.current.divider.copy(alpha = 0.4f)) }
        ) {
            ReviewUserSheet(
                user = detailedUser!!,
                isLoading = isProfileLoading,
                onClose = { 
                    showDetailsSheet = false
                    detailedUser = null
                },
                onToggleStatus = { user ->
                    scope.launch { handleStatusToggle(user) }
                }
            )
        }
    }
}

@Composable
fun ReviewUserSheet(
    user: UserReviewItem,
    isLoading: Boolean = false,
    onClose: () -> Unit,
    onToggleStatus: (UserReviewItem) -> Unit
) {
    val sheetColors = LocalAppColors.current
    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
        color = sheetColors.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .size(40.dp, 4.dp)
                    .clip(CircleShape)
                    .background(sheetColors.divider)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.height(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = sheetColors.primary)
                }
            } else {
                // Profile Section
                Box(contentAlignment = Alignment.Center) {
                    val statusColor = when (user.status.lowercase()) {
                        "active" -> sheetColors.success
                        "suspended", "rejected" -> sheetColors.error
                        "pending" -> sheetColors.warning
                        else -> sheetColors.divider
                    }
                    
                    Surface(
                        modifier = Modifier.size(110.dp),
                        shape = CircleShape,
                        color = statusColor.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(2.dp, statusColor.copy(alpha = 0.1f))
                    ) {}

                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = sheetColors.primaryHighlight,
                        shadowElevation = 4.dp
                    ) {
                        val profilePic = user.profilePic
                        if (!profilePic.isNullOrEmpty() && user.role.lowercase() != "admin") {
                            val imageModel: Any? = when {
                                profilePic.startsWith("http") -> profilePic
                                profilePic.startsWith("/data/user/") -> null
                                profilePic.length > 1000 -> {
                                    val cleanBase64 = if (profilePic.contains(",")) profilePic.substringAfter(",") else profilePic
                                    try { android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT) } catch (e: Exception) { profilePic }
                                }
                                else -> "${com.simats.interviewassist.data.network.RetrofitClient.BASE_URL.removeSuffix("/")}/${profilePic.removePrefix("/")}"
                            }
                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                val initials = if (user.name.contains(" ")) {
                                    user.name.split(" ").filter { it.isNotEmpty() }.map { it.first() }.joinToString("")
                                } else {
                                    user.name.take(1)
                                }
                                Text(
                                    initials,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = sheetColors.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = user.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = sheetColors.textTitle
                )
                Text(
                    text = user.email,
                    fontSize = 16.sp,
                    color = sheetColors.textSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Status Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusColor = when (user.status.lowercase()) {
                        "active" -> sheetColors.success
                        "suspended", "rejected" -> sheetColors.error
                        "pending" -> sheetColors.warning
                        else -> sheetColors.textSecondary
                    }
                    
                    val statusBg = when (user.status.lowercase()) {
                        "active" -> sheetColors.successBg
                        "suspended", "rejected" -> sheetColors.errorBg
                        "pending" -> sheetColors.warning.copy(alpha = 0.1f)
                        else -> sheetColors.divider
                    }

                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = if (user.status == "pending") "PENDING APPROVAL" else user.status.uppercase(),
                            fontSize = 12.sp,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = sheetColors.divider.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = user.role.uppercase(),
                            fontSize = 12.sp,
                            color = sheetColors.textTitle,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ReviewDetailRow(label = "Phone Number", value = user.phoneNumber ?: "Not provided", icon = Icons.Outlined.Phone)
                    
                    if (user.profile != null) {
                        ReviewDetailRow(label = "Major", value = user.profile.major ?: "N/A", icon = Icons.Outlined.School)
                        ReviewDetailRow(label = "Expected Graduation", value = user.profile.expectedGradYear ?: "N/A", icon = Icons.Outlined.DateRange)
                        
                        if (!user.profile.bio.isNullOrBlank()) {
                            ReviewDetailRow(label = "Bio", value = user.profile.bio ?: "", icon = Icons.Outlined.Info)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Primary Action Button
                val actionText = when {
                    user.status == "rejected" || user.status == "pending" -> "Approve User"
                    user.isSuspended -> "Activate User"
                    else -> "Suspend User"
                }
                val actionColor = when {
                    user.status == "rejected" || user.status == "pending" -> sheetColors.success
                    user.isSuspended -> sheetColors.success
                    else -> sheetColors.error
                }

                Button(
                    onClick = {
                        onToggleStatus(user)
                        onClose()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = actionColor)
                ) {
                    Text(actionText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Details", color = sheetColors.textSecondary)
                }
            }
        }
    }
}

data class UserReviewItem(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val status: String = "active",
    val isSuspended: Boolean = false,
    val phoneNumber: String? = null,
    val profilePic: String? = null,
    val profile: com.simats.interviewassist.data.models.ProfileData? = null
)

@Composable
fun ReviewDetailRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = LocalAppColors.current.primaryHighlight.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = LocalAppColors.current.primary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = LocalAppColors.current.textSecondary)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LocalAppColors.current.textTitle)
        }
    }
}

@Composable
fun ReviewCard(
    experience: InterviewExperienceResponse,
    onClick: () -> Unit,
    onViewProfile: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onViewProfile() }
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = LocalAppColors.current.primaryHighlight
                ) {
                    val profilePic = experience.userProfilePic
                    if (!profilePic.isNullOrEmpty()) {
                        val imageModel: Any? = when {
                            profilePic.startsWith("http") -> profilePic
                            profilePic.startsWith("/data/user/") -> null
                            profilePic.length > 1000 -> {
                                val cleanBase64 = if (profilePic.contains(",")) profilePic.substringAfter(",") else profilePic
                                try { android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT) } catch (e: Exception) { profilePic }
                            }
                            else -> "${com.simats.interviewassist.data.network.RetrofitClient.BASE_URL.removeSuffix("/")}/${profilePic.removePrefix("/")}"
                        }
                        AsyncImage(
                            model = imageModel,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            val initial = experience.userName.take(1).uppercase()
                            Text(initial, color = LocalAppColors.current.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = experience.userName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle
                    )
                    Text(
                        text = "${experience.userRole} @ ${experience.companyName}",
                        fontSize = 12.sp,
                        color = LocalAppColors.current.textSecondary
                    )
                }
                Surface(
                    color = when(experience.difficulty) {
                        "Easy" -> LocalAppColors.current.successBg
                        "Medium" -> LocalAppColors.current.warningBg
                        "Hard" -> LocalAppColors.current.errorBg
                        else -> LocalAppColors.current.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = experience.difficulty ?: "Medium",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(experience.difficulty) {
                            "Easy" -> LocalAppColors.current.success
                            "Medium" -> LocalAppColors.current.warning
                            "Hard" -> LocalAppColors.current.error
                            else -> LocalAppColors.current.textSecondary
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = experience.brief ?: "No brief provided.",
                fontSize = 14.sp,
                color = LocalAppColors.current.textBody,
                maxLines = 3,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LocalAppColors.current.error),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LocalAppColors.current.error)
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reject")
                }
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.success)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Approve")
                }
            }
        }
    }
}