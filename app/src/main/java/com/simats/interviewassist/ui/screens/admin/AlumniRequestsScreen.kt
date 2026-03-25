package com.simats.interviewassist.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.simats.interviewassist.data.network.RetrofitClient
import kotlinx.coroutines.launch
import com.simats.interviewassist.data.models.NotificationResponse
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Brush
import com.simats.interviewassist.data.models.*

data class AlumniRequest(
    val id: Int,
    val name: String,
    val email: String,
    val info: String,
    val initials: String,
    val isUpgrade: Boolean = false,
    val profilePic: String? = null,
    val userData: UserData? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniRequestsScreen(
    preferenceManager: PreferenceManager,
    onBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToAlumniRequests: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToReviews: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("New Alumni", "Student Upgrades")
    
    val newAlumniRequests = remember { mutableStateListOf<AlumniRequest>() }
    val studentUpgradeRequests = remember { mutableStateListOf<AlumniRequest>() }
    
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState()
    var showDetailsSheet by remember { mutableStateOf(false) }
    var selectedRequest by remember { mutableStateOf<AlumniRequest?>(null) }
    var isProfileLoading by remember { mutableStateOf(false) }
    var unreadCount by remember { mutableIntStateOf(0) }

    fun fetchNotificationsCount() {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.getAdminNotifications()
                if (response.isSuccessful) {
                    unreadCount = response.body()?.count { !it.isRead } ?: 0
                }
            } catch (e: Exception) { }
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                fetchNotificationsCount()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val alumniResponse = RetrofitClient.apiService.getPendingAlumni()
            if (alumniResponse.isSuccessful) {
                newAlumniRequests.clear()
                alumniResponse.body()?.let { list ->
                    newAlumniRequests.addAll(list.map { user ->
                        val initials = if (user.firstName.isNotBlank()) user.firstName.take(1) + (user.lastName.take(1) ?: "") else "?"
                        AlumniRequest(
                            id = user.id,
                            name = "${user.firstName} ${user.lastName}",
                            email = user.email,
                            info = "${user.profile?.major ?: "Alumni"} • ${user.profile?.expectedGradYear ?: "N/A"}",
                            initials = initials,
                            profilePic = user.profilePic,
                            userData = user,
                            isUpgrade = false
                        )
                    })
                }
            }

            val upgradeResponse = RetrofitClient.apiService.getPendingUpgrades()
            if (upgradeResponse.isSuccessful) {
                studentUpgradeRequests.clear()
                upgradeResponse.body()?.let { list ->
                    studentUpgradeRequests.addAll(list.map { user ->
                        val initials = if (user.firstName.isNotBlank()) user.firstName.take(1) + (user.lastName.take(1) ?: "") else "?"
                        AlumniRequest(
                            id = user.id,
                            name = "${user.firstName} ${user.lastName}",
                            email = user.email,
                            info = "${user.profile?.currentCompany ?: "Student"} • ${user.profile?.designation ?: "Pending Review"}",
                            initials = initials,
                            profilePic = user.profilePic,
                            userData = user,
                            isUpgrade = true
                        )
                    })
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    suspend fun handleApproval(request: AlumniRequest) {
        try {
            val response = RetrofitClient.apiService.approveUser(request.id)
            if (response.isSuccessful) {
                if (request.isUpgrade) {
                    studentUpgradeRequests.remove(request)
                } else {
                    newAlumniRequests.remove(request)
                }
                Toast.makeText(context, "Request approved", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to approve: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun handleRejection(request: AlumniRequest) {
        try {
            val response = RetrofitClient.apiService.rejectUser(request.id)
            if (response.isSuccessful) {
                if (request.isUpgrade) {
                    studentUpgradeRequests.remove(request)
                } else {
                    newAlumniRequests.remove(request)
                }
                Toast.makeText(context, "Request rejected", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to reject: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = LocalAppColors.current.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home", fontSize = 10.sp) },
                    selected = false,
                    onClick = onNavigateToHome,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.5f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Shield, null) },
                    label = { Text("Dashboard", fontSize = 10.sp) },
                    selected = false,
                    onClick = onNavigateToDashboard,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.5f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AssignmentLate, null) },
                    label = { Text("Requests", fontSize = 10.sp) },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LocalAppColors.current.primary,
                        indicatorColor = LocalAppColors.current.primaryHighlight
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Description, null) },
                    label = { Text("Reviews", fontSize = 10.sp) },
                    selected = false,
                    onClick = onNavigateToReviews,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.5f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.People, null) },
                    label = { Text("Users", fontSize = 10.sp) },
                    selected = false,
                    onClick = onNavigateToUsers,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.5f)
                    )
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LocalAppColors.current.background)
                .padding(padding)
        ) {
            // Decorative background element
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                LocalAppColors.current.primary.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Premium Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = LocalAppColors.current.surface,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Interview Assist",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = LocalAppColors.current.textTitle,
                                letterSpacing = (-0.5).sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(LocalAppColors.current.purple, LocalAppColors.current.primary)
                                            )
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "ADMIN",
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.2.sp
                                    )
                                }
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onNavigateToNotifications,
                                modifier = Modifier.background(LocalAppColors.current.surfaceVariant, CircleShape).size(40.dp)
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadCount > 0) {
                                            Badge(containerColor = LocalAppColors.current.error, modifier = Modifier.size(6.dp))
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.NotificationsNone, null, modifier = Modifier.size(20.dp), tint = LocalAppColors.current.textTitle)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = onNavigateToSettings,
                                modifier = Modifier.background(LocalAppColors.current.surfaceVariant, CircleShape).size(40.dp)
                            ) {
                                Icon(Icons.Default.Settings, "Settings", modifier = Modifier.size(20.dp), tint = LocalAppColors.current.textTitle)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                modifier = Modifier.size(42.dp).clickable { onNavigateToProfile() },
                                shape = CircleShape,
                                border = BorderStroke(2.dp, LocalAppColors.current.primaryHighlight),
                                color = LocalAppColors.current.surfaceVariant
                            ) {
                                val initials = preferenceManager.getUserName().take(1).uppercase().ifEmpty { "A" }
                                Box(contentAlignment = Alignment.Center) {
                                    Text(initials, fontSize = 16.sp, color = LocalAppColors.current.primary, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }

                val currentList = if (selectedTab == 0) newAlumniRequests else studentUpgradeRequests

                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Pending Approvals",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalAppColors.current.textTitle
                            )
                            Text(
                                text = "Review and verify member status",
                                fontSize = 14.sp,
                                color = LocalAppColors.current.textSecondary
                            )
                        }
                        Surface(
                            color = LocalAppColors.current.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${currentList.size}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = LocalAppColors.current.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Premium Tab Bar
                    Surface(
                        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                        color = LocalAppColors.current.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tabs.forEachIndexed { index, title ->
                                val isSelected = selectedTab == index
                                Surface(
                                    modifier = Modifier.weight(1f).height(46.dp).clickable { selectedTab = index },
                                    color = if (isSelected) LocalAppColors.current.surface else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp),
                                    shadowElevation = if (isSelected) 4.dp else 0.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = title,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            color = if (isSelected) LocalAppColors.current.primary else LocalAppColors.current.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = LocalAppColors.current.primary)
                        }
                    } else if (currentList.isEmpty()) {
                        EmptyStateView()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            items(currentList) { request ->
                                PremiumAlumniRequestCard(
                                    request = request,
                                    onClick = {
                                        selectedRequest = request
                                        showDetailsSheet = true
                                    },
                                    onApprove = { scope.launch { handleApproval(request) } },
                                    onReject = { scope.launch { handleRejection(request) } }
                                )
                            }
                        }
                    }
                }
            }

            if (showDetailsSheet && selectedRequest != null) {
                ModalBottomSheet(
                    onDismissRequest = { showDetailsSheet = false },
                    sheetState = sheetState,
                    containerColor = LocalAppColors.current.surface,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    PremiumRequestDetailsSheetContent(
                        request = selectedRequest!!,
                        onApprove = {
                            scope.launch {
                                handleApproval(selectedRequest!!)
                                showDetailsSheet = false
                            }
                        },
                        onReject = {
                            scope.launch {
                                handleRejection(selectedRequest!!)
                                showDetailsSheet = false
                            }
                        },
                        onClose = { showDetailsSheet = false }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView() {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = LocalAppColors.current.primaryHighlight.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.DoneAll,
                        null,
                        modifier = Modifier.size(60.dp),
                        tint = LocalAppColors.current.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("No Pending Tasks", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Great job! All requests have been addressed.",
                fontSize = 15.sp,
                color = LocalAppColors.current.textSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun PremiumAlumniRequestCard(
    request: AlumniRequest,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        color = LocalAppColors.current.surface,
        border = BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.5f)),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile Avatar with decorative ring
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = LocalAppColors.current.primary.copy(alpha = 0.05f),
                        border = BorderStroke(3.dp, Brush.sweepGradient(
                            listOf(LocalAppColors.current.primary, LocalAppColors.current.purple, LocalAppColors.current.primary)
                        ))
                    ) {
                        val profilePic = request.profilePic
                        if (!profilePic.isNullOrEmpty()) {
                            val imageModel: Any? = when {
                                profilePic.startsWith("http") -> profilePic
                                profilePic.startsWith("/data/user/") -> null
                                profilePic.length > 1000 -> {
                                    val cleanBase64 = if (profilePic.contains(",")) profilePic.substringAfter(",") else profilePic
                                    try { Base64.decode(cleanBase64, Base64.DEFAULT) } catch (e: Exception) { profilePic }
                                }
                                else -> "${RetrofitClient.BASE_URL.removeSuffix("/")}/${profilePic.removePrefix("/")}"
                            }
                            AsyncImage(
                                model = imageModel,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {

                            Box(contentAlignment = Alignment.Center) {
                                Text(request.initials, fontSize = 24.sp, fontWeight = FontWeight.Black, color = LocalAppColors.current.primary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = request.name,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            color = LocalAppColors.current.textTitle,
                            modifier = Modifier.weight(1f)
                        )
                        if (request.isUpgrade) {
                            Surface(
                                color = LocalAppColors.current.primaryHighlight,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "UPGRADE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LocalAppColors.current.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.School, null, modifier = Modifier.size(14.dp), tint = LocalAppColors.current.textSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(request.info, fontSize = 13.sp, color = LocalAppColors.current.textSecondary, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Email, null, modifier = Modifier.size(14.dp), tint = LocalAppColors.current.primary.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(request.email, fontSize = 13.sp, color = LocalAppColors.current.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
                
                Icon(Icons.Default.ChevronRight, null, tint = LocalAppColors.current.divider)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.error.copy(alpha = 0.1f), contentColor = LocalAppColors.current.error),
                    elevation = null
                ) {
                    Text("Decline", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
                
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.success),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text("Approve", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun PremiumRequestDetailsSheetContent(
    request: AlumniRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .navigationBarsPadding()
    ) {

        // Decorative Header Section
        Box(
            modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(20.dp))
                .background(Brush.horizontalGradient(listOf(LocalAppColors.current.primary, LocalAppColors.current.purple)))
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), contentAlignment = Alignment.CenterStart) {
                Text(
                    if (request.isUpgrade) "UPGRADE APPLICATION" else "MEMBER REGISTRATION",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Professional Body
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = LocalAppColors.current.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                val user = request.userData
                val profile = user?.profile
                
                DetailSectionTitle("MEMBER IDENTITY")
                IdentityRow(request.name, request.email, request.initials, request.profilePic)
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = LocalAppColors.current.divider.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(24.dp))

                DetailSectionTitle("ACADEMIC BACKGROUND")
                DetailGridItem(
                    Icons.Outlined.School, 
                    "Specialization", 
                    (if (request.isUpgrade) profile?.major else profile?.specialization) ?: "Not provided"
                )
                
                if (request.isUpgrade) {
                    DetailGridItem(
                        Icons.Outlined.Layers, 
                        "Current Year of Study", 
                        profile?.currentYear ?: "Not provided"
                    )
                }
                DetailGridItem(
                    Icons.Outlined.CalendarToday, 
                    if (request.isUpgrade) "Expected Graduation" else "Year of Graduation", 
                    profile?.expectedGradYear ?: "Not provided"
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = LocalAppColors.current.divider.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(24.dp))

                if (!request.isUpgrade) {
                    DetailSectionTitle("PROFESSIONAL PROFILE")
                    DetailGridItem(
                        Icons.Outlined.Business, 
                        "Current Organization", 
                        profile?.currentCompany ?: "Not provided"
                    )
                    DetailGridItem(
                        Icons.Outlined.WorkOutline, 
                        "Professional Designation", 
                        profile?.designation ?: "Not provided"
                    )
                    DetailGridItem(Icons.Outlined.Public, "LinkedIn Profile", profile?.linkedinUrl ?: "Not provided", true)
                }
                
                if (!profile?.bio.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Surface(color = LocalAppColors.current.surface, shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "BIO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = LocalAppColors.current.textSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(profile!!.bio!!, fontSize = 14.sp, color = LocalAppColors.current.textTitle, lineHeight = 20.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                DetailSectionTitle("CONTACT INFORMATION")
                DetailGridItem(Icons.Outlined.PhoneAndroid, "Phone Number", user?.phoneNumber ?: "Not provided")
                DetailGridItem(Icons.Outlined.AlternateEmail, "Personal Email", user?.secondaryEmail ?: "Not provided")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onReject,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.error, contentColor = Color.White)
            ) {
                Text("Decline", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
            Button(
                onClick = onApprove,
                modifier = Modifier.weight(1.2f).height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.success, contentColor = Color.White)
            ) {
                Text("Approve", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        TextButton(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Close Details", color = LocalAppColors.current.textSecondary)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DetailSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        color = LocalAppColors.current.primary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun IdentityRow(name: String, email: String, initials: String, pic: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(56.dp), shape = CircleShape, color = LocalAppColors.current.surface) {
            val profilePic = pic
            if (!profilePic.isNullOrEmpty()) {
                val imageModel: Any? = when {
                    profilePic.startsWith("http") -> profilePic
                    profilePic.startsWith("/data/user/") -> null
                    profilePic.length > 1000 -> {
                        val cleanBase64 = if (profilePic.contains(",")) profilePic.substringAfter(",") else profilePic
                        try { Base64.decode(cleanBase64, Base64.DEFAULT) } catch (e: Exception) { profilePic }
                    }
                    else -> "${RetrofitClient.BASE_URL.removeSuffix("/")}/${profilePic.removePrefix("/")}"
                }
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(contentAlignment = Alignment.Center) { Text(initials, fontWeight = FontWeight.Black, color = LocalAppColors.current.primary) }
            }

        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(name, fontSize = 18.sp, fontWeight = FontWeight.Black, color = LocalAppColors.current.textTitle)
            Text(email, fontSize = 13.sp, color = LocalAppColors.current.textSecondary)
        }
    }
}

@Composable
fun DetailGridItem(icon: ImageVector, label: String, value: String, isLink: Boolean = false) {
    Row(modifier = Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(32.dp), shape = RoundedCornerShape(10.dp), color = LocalAppColors.current.surface) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(16.dp), tint = LocalAppColors.current.primary)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = LocalAppColors.current.textSecondary)
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLink) LocalAppColors.current.primary else LocalAppColors.current.textTitle,
                maxLines = 1
            )
        }
    }
}
