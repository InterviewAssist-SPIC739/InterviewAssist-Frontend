package com.simats.interviewassist.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.utils.PreferenceManager
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.simats.interviewassist.data.network.RetrofitClient
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextOverflow
import com.simats.interviewassist.data.models.NotificationResponse


data class UserItem(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val status: String = "active",
    val joinedDate: String = "Jan 2024",
    val isSuspended: Boolean = false,
    val phoneNumber: String? = null,
    val secondaryEmail: String? = null,
    val profilePic: String? = null,
    val profile: com.simats.interviewassist.data.models.ProfileData? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    preferenceManager: PreferenceManager,
    onNavigateToHome: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToAlumniRequests: () -> Unit = {},
    onNavigateToReviews: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()
    var showDetailsSheet by remember { mutableStateOf(false) }
    var detailedUser by remember { mutableStateOf<UserItem?>(null) }
    var showUnsuspendDialog by remember { mutableStateOf(false) }
    var userToUnsuspend by remember { mutableStateOf<UserItem?>(null) }
    var showSuspendDialog by remember { mutableStateOf(false) }
    var userToSuspend by remember { mutableStateOf<UserItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<UserItem?>(null) }
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Students", "Alumni")
    
    // Detailed Profile States
    var isProfileLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    val usersList = remember { mutableStateListOf<UserItem>() }
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

    // Refresh count on resume
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                fetchNotificationsCount()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val response = RetrofitClient.apiService.getUsers()
            if (response.isSuccessful) {
                usersList.clear()
                response.body()?.let { list ->
                    val mappedUsers = list.map { userData ->
                        UserItem(
                            id = userData.id,
                            name = "${userData.firstName} ${userData.lastName}",
                            email = userData.email,
                            role = userData.role.lowercase(),
                            status = userData.status ?: "active",
                            joinedDate = "Joined",
                            isSuspended = userData.isSuspended ?: false,
                            phoneNumber = userData.phoneNumber,
                            secondaryEmail = userData.secondaryEmail,
                            profilePic = userData.profilePic
                        )
                    }
                    usersList.addAll(mappedUsers)
                }
            } else {
                Toast.makeText(context, "Failed to fetch users", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    val filteredUsers = remember(searchQuery, usersList.toList(), selectedTabIndex) {
        val currentRole = if (selectedTabIndex == 0) "student" else "alumni"
        usersList.filter { 
            it.role == currentRole && 
            !(it.role == "alumni" && it.status == "pending") && (
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.email.contains(searchQuery, ignoreCase = true) 
            )
        }
    }

    if (showUnsuspendDialog && userToUnsuspend != null) {
        UnsuspendUserDialog(
            user = userToUnsuspend!!,
            onDismiss = { showUnsuspendDialog = false },
            onConfirm = {
                val userBeingUnsuspended = userToUnsuspend!!
                showUnsuspendDialog = false
                scope.launch {
                    try {
                        RetrofitClient.apiService.unsuspendUser(userBeingUnsuspended.id)
                        val index = usersList.indexOfFirst { it.id == userBeingUnsuspended.id }
                        if (index != -1) {
                            usersList[index] = usersList[index].copy(isSuspended = false, status = "active")
                            if (detailedUser?.id == userBeingUnsuspended.id) {
                                detailedUser = usersList[index]
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to reactivate user", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    if (showSuspendDialog && userToSuspend != null) {
        SuspendUserDialog(
            user = userToSuspend!!,
            onDismiss = { showSuspendDialog = false },
            onConfirm = {
                val userBeingSuspended = userToSuspend!!
                showSuspendDialog = false
                scope.launch {
                    try {
                        RetrofitClient.apiService.suspendUser(userBeingSuspended.id)
                        val index = usersList.indexOfFirst { it.id == userBeingSuspended.id }
                        if (index != -1) {
                            usersList[index] = usersList[index].copy(isSuspended = true, status = "suspended")
                            if (detailedUser?.id == userBeingSuspended.id) {
                                detailedUser = usersList[index]
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to suspend user", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    if (showDeleteDialog && userToDelete != null) {
        DeleteUserDialog(
            user = userToDelete!!,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                val userBeingDeleted = userToDelete!!
                showDeleteDialog = false
                scope.launch {
                    try {
                        val response = RetrofitClient.apiService.deleteUser(userBeingDeleted.id)
                        if (response.isSuccessful) {
                            usersList.removeIf { it.id == userBeingDeleted.id }
                            Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to delete user", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    if (showDetailsSheet && detailedUser != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                showDetailsSheet = false
                detailedUser = null 
            },
            sheetState = sheetState,
            containerColor = LocalAppColors.current.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = LocalAppColors.current.borderLight) }
        ) {
            UserDetailsSheetContent(
                user = detailedUser!!,
                isLoading = isProfileLoading,
                onClose = { 
                    showDetailsSheet = false
                    detailedUser = null
                },
                onToggleStatus = {
                    if (detailedUser!!.isSuspended) {
                        userToUnsuspend = detailedUser
                        showUnsuspendDialog = true
                    } else {
                        userToSuspend = detailedUser
                        showSuspendDialog = true
                    }
                    showDetailsSheet = false
                }
            )
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
                    icon = { Icon(Icons.Default.People, contentDescription = "Users") },
                    label = { Text("Users", fontSize = 10.sp) },
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LocalAppColors.current.background)
                .padding(padding)
        ) {
            // Top Header (Matching Home Screen)
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
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(
                                        LocalAppColors.current.purple,
                                        LocalAppColors.current.primary
                                    )
                                )
                            )
                    ) {
                        Text(
                            text = "Admin",
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LocalAppColors.current.primaryHighlight)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = if (preferenceManager.getUserName().isNotEmpty()) 
                            preferenceManager.getUserName().take(1).uppercase() else "UP"
                        Text(initials, fontSize = 12.sp, color = LocalAppColors.current.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section Title and Counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = null,
                    tint = LocalAppColors.current.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "All Users",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalAppColors.current.textTitle
                )
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    color = LocalAppColors.current.primaryHighlight,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LocalAppColors.current.primary.copy(alpha = 0.2f))
                ) {
                    Text(
                        "${filteredUsers.size} ${tabTitles[selectedTabIndex]}",
                        fontSize = 12.sp,
                        color = LocalAppColors.current.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or email...", color = LocalAppColors.current.textBody.copy(alpha = 0.4f)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = LocalAppColors.current.primary.copy(alpha = 0.6f)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = LocalAppColors.current.textBody.copy(alpha = 0.4f)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = LocalAppColors.current.surface,
                        unfocusedContainerColor = LocalAppColors.current.divider.copy(alpha = 0.5f),
                        focusedIndicatorColor = LocalAppColors.current.primary,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = LocalAppColors.current.primary
                    ),
                    singleLine = true
                )
            }

            // Tabs - Modern Pill Style
            Surface(
                color = LocalAppColors.current.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        val isSelected = selectedTabIndex == index
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable { selectedTabIndex = index },
                            color = if (isSelected) LocalAppColors.current.surface else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = if (isSelected) 2.dp else 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val count = if (index == 0) usersList.count { it.role == "student" } 
                                            else usersList.count { it.role == "alumni" && it.status != "pending" }
                                
                                Text(
                                    text = "$title ($count)",
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) LocalAppColors.current.primary else LocalAppColors.current.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LocalAppColors.current.primary)
                }
            } else if (filteredUsers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No users found", color = LocalAppColors.current.textSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredUsers) { user ->
                        AdminUserCard(
                            user = user,
                            onViewDetails = {
                                detailedUser = user
                                showDetailsSheet = true
                                // Fetch detailed profile
                                scope.launch {
                                    isProfileLoading = true
                                    try {
                                        val response = RetrofitClient.apiService.getUserProfile(user.id)
                                        if (response.isSuccessful) {
                                            response.body()?.let { profileData ->
                                                val prof = profileData.profile
                                                detailedUser = user.copy(
                                                    phoneNumber = prof?.phoneNumber
                                                        ?: profileData.phoneNumber
                                                        ?: user.phoneNumber,
                                                    secondaryEmail = profileData.secondaryEmail
                                                        ?: user.secondaryEmail,
                                                    profilePic = prof?.profilePic
                                                        ?: profileData.profilePic
                                                        ?: user.profilePic,
                                                    profile = prof
                                                )
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Silently fail or log
                                    } finally {
                                        isProfileLoading = false
                                    }
                                }
                            },
                            onToggleStatus = {
                                val isFunctionallyActive = user.status == "active" || (user.role == "student" && user.status == "pending")
                                if (isFunctionallyActive || user.isSuspended) {
                                    if (user.isSuspended) {
                                        userToUnsuspend = user
                                        showUnsuspendDialog = true
                                    } else {
                                        userToSuspend = user
                                        showSuspendDialog = true
                                    }
                                }
                            },
                            onDelete = {
                                userToDelete = user
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserCard(
    user: UserItem,
    onViewDetails: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            LocalAppColors.current.surface,
                            LocalAppColors.current.divider.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Avatar with Status Glow
                Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                    val statusColor = when {
                        user.status.lowercase() == "active" || (user.role == "student" && user.status == "pending") -> LocalAppColors.current.success
                        user.status.lowercase() == "suspended" || user.status.lowercase() == "rejected" -> LocalAppColors.current.error
                        user.status.lowercase() == "pending" -> LocalAppColors.current.warning
                        else -> LocalAppColors.current.divider
                    }
                    
                    // Ripple/Glow effect around avatar
                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        color = statusColor.copy(alpha = 0.1f),
                        border = BorderStroke(2.dp, statusColor.copy(alpha = 0.2f))
                    ) {}

                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = CircleShape,
                        color = LocalAppColors.current.primaryHighlight
                    ) {
                    val profilePic = user.profilePic
                    if (!profilePic.isNullOrEmpty() && user.role.lowercase() != "admin") {
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
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalAppColors.current.primary
                            )
                        }
                    }
                    }
                    
                    // Status Dot
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Info Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            val nameParts = user.name.trim().split(" ")
                            val firstName = nameParts.firstOrNull() ?: ""
                            val lastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else ""
                            
                            Text(
                                text = firstName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = LocalAppColors.current.textTitle
                            )
                            if (lastName.isNotEmpty()) {
                                Text(
                                    text = lastName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LocalAppColors.current.textTitle.copy(alpha = 0.8f)
                                )
                            }
                        }
                        
                        // Action Buttons at top right
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onViewDetails, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.Visibility, null, tint = LocalAppColors.current.iconTint, modifier = Modifier.size(18.dp))
                            }
                            val isFunctionallyActive = user.status == "active" || (user.role == "student" && user.status == "pending")
                            if (isFunctionallyActive || user.isSuspended) {
                                IconButton(onClick = onToggleStatus, modifier = Modifier.size(32.dp)) {
                                    val icon = if (user.isSuspended) Icons.Outlined.PersonAddAlt1 else Icons.Outlined.Block
                                    val tint = if (user.isSuspended) LocalAppColors.current.success else LocalAppColors.current.error
                                    Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
                                }
                            }
                            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.Delete, null, tint = LocalAppColors.current.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Role Badge
                        Surface(
                            color = LocalAppColors.current.divider.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                user.role.uppercase(),
                                fontSize = 10.sp,
                                color = LocalAppColors.current.textBody,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                softWrap = false,
                                maxLines = 1
                            )
                        }
                        
                        // Status Badge
                        val badgeColor = when {
                            user.status.lowercase() == "active" || (user.role == "student" && user.status == "pending") -> LocalAppColors.current.success
                            user.status.lowercase() == "suspended" || user.status.lowercase() == "rejected" -> LocalAppColors.current.error
                            user.status.lowercase() == "pending" -> LocalAppColors.current.warning
                            else -> LocalAppColors.current.textSecondary
                        }
                        
                        Surface(
                            color = badgeColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = if (user.role == "student" && user.status == "pending") "ACTIVE" 
                                       else if (user.status == "pending") "PENDING APPROVAL" 
                                       else user.status.uppercase(),
                                fontSize = 10.sp,
                                color = badgeColor,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                softWrap = false,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserDetailsSheetContent(
    user: UserItem,
    isLoading: Boolean = false,
    onClose: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val sheetColors = LocalAppColors.current
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
        color = sheetColors.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(24.dp)
                    .padding(bottom = 100.dp), // Extra space for fixed buttons
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
                    CircularProgressIndicator(color = sheetColors.primary)
                } else {
                    // Profile Section
                    Box(contentAlignment = Alignment.Center) {
                        val statusColor = when {
                            user.status.lowercase() == "active" || (user.role == "student" && user.status == "pending") -> sheetColors.success
                            user.status.lowercase() == "suspended" || user.status.lowercase() == "rejected" -> sheetColors.error
                            user.status.lowercase() == "pending" -> sheetColors.warning
                            else -> sheetColors.divider
                        }
                        
                        Surface(
                            modifier = Modifier.size(110.dp),
                            shape = CircleShape,
                            color = statusColor.copy(alpha = 0.05f),
                            border = BorderStroke(2.dp, statusColor.copy(alpha = 0.1f))
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
                                        try { Base64.decode(cleanBase64, Base64.DEFAULT) } catch (e: Exception) { profilePic }
                                    }
                                    else -> "${RetrofitClient.BASE_URL.removeSuffix("/")}/${profilePic.removePrefix("/")}"
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
                        val statusColor = when {
                            user.status.lowercase() == "active" || (user.role == "student" && user.status == "pending") -> sheetColors.success
                            user.status.lowercase() == "suspended" || user.status.lowercase() == "rejected" -> sheetColors.error
                            user.status.lowercase() == "pending" -> sheetColors.warning
                            else -> sheetColors.textSecondary
                        }
                        
                        val statusBg = when {
                            user.status.lowercase() == "active" || (user.role == "student" && user.status == "pending") -> sheetColors.successBg
                            user.status.lowercase() == "suspended" || user.status.lowercase() == "rejected" -> sheetColors.errorBg
                            user.status.lowercase() == "pending" -> sheetColors.warning.copy(alpha = 0.1f)
                            else -> sheetColors.divider
                        }

                        Surface(
                            color = statusBg,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = if (user.role == "student" && user.status == "pending") "ACTIVE" 
                                       else if (user.status == "pending") "PENDING APPROVAL" 
                                       else user.status.uppercase(),
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
                        val isStudent = user.role.lowercase() == "student"
                        
                        DetailRow(label = "Contact Number", value = user.phoneNumber ?: "Not provided", icon = Icons.Outlined.Phone)

                        if (user.profile != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("ACADEMIC BACKGROUND", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = sheetColors.primary)
                            
                            DetailRow(
                                label = if (isStudent) "Major" else "Specialization",
                                value = (if (isStudent) user.profile.major else user.profile.specialization) ?: "Not provided",
                                icon = Icons.Outlined.School
                            )

                            if (isStudent) {
                                DetailRow(
                                    label = "Current Year of Study",
                                    value = user.profile.currentYear ?: "Not provided",
                                    icon = Icons.Outlined.Layers
                                )
                            }
                            DetailRow(
                                label = if (isStudent) "Expected Graduation" else "Year of Graduation",
                                value = user.profile.expectedGradYear ?: "Not provided",
                                icon = Icons.Outlined.DateRange
                            )

                            if (!isStudent) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("PROFESSIONAL PROFILE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = sheetColors.primary)
                                
                                DetailRow(
                                    label = "Current Organization",
                                    value = user.profile.currentCompany ?: "Not provided",
                                    icon = Icons.Outlined.Business
                                )
                                DetailRow(
                                    label = "Professional Designation",
                                    value = user.profile.designation ?: "Not provided",
                                    icon = Icons.Outlined.WorkOutline
                                )
                                DetailRow(
                                    label = "LinkedIn Profile",
                                    value = user.profile.linkedinUrl ?: "Not provided",
                                    icon = Icons.Outlined.Public
                                )
                            }

                            if (!user.profile.bio.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "BIO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = sheetColors.primary
                                )
                                Text(
                                    user.profile.bio,
                                    fontSize = 14.sp,
                                    color = sheetColors.textSecondary,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Fixed Bottom Action Bar
            if (!isLoading) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                    color = sheetColors.surface,
                    shadowElevation = 16.dp,
                    border = BorderStroke(1.dp, sheetColors.divider.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 20.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Close Button
                        OutlinedButton(
                            onClick = onClose,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, sheetColors.divider.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = sheetColors.textSecondary)
                        ) {
                            Text("Dismiss", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Suspend Action Button
                        val isFunctionallyActive = user.status == "active" || (user.role == "student" && user.status == "pending")
                        if (isFunctionallyActive || user.isSuspended) {
                            val actionText = if (user.isSuspended) "Reactivate" else "Suspend"
                            val actionColor = if (user.isSuspended) sheetColors.success else sheetColors.error
                            
                            Button(
                                onClick = {
                                    onToggleStatus()
                                    onClose()
                                },
                                modifier = Modifier.weight(1.4f).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = actionColor)
                            ) {
                                Text(actionText, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnsuspendUserDialog(
    user: UserItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = LocalAppColors.current.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Unsuspend User?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = LocalAppColors.current.iconTint)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "Are you sure you want to unsuspend ${user.name}? They will regain access to the platform.",
                    fontSize = 14.sp,
                    color = LocalAppColors.current.textSecondary,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LocalAppColors.current.divider
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = LocalAppColors.current.textTitle, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LocalAppColors.current.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Unsuspend", color = LocalAppColors.current.surface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuspendUserDialog(
    user: UserItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = LocalAppColors.current.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Suspend User?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = LocalAppColors.current.iconTint)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "Are you sure you want to suspend ${user.name}? They will lose access to the platform until reactivated.",
                    fontSize = 14.sp,
                    color = LocalAppColors.current.textSecondary,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LocalAppColors.current.divider
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = LocalAppColors.current.textTitle, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LocalAppColors.current.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Suspend", color = LocalAppColors.current.surface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteUserDialog(
    user: UserItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = LocalAppColors.current.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Delete User?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = LocalAppColors.current.iconTint)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "Are you sure you want to permanently delete ${user.name}? This action cannot be undone and all associated data (profile, experiences) will be removed.",
                    fontSize = 14.sp,
                    color = LocalAppColors.current.textSecondary,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LocalAppColors.current.divider
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = LocalAppColors.current.textTitle, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LocalAppColors.current.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Delete", color = LocalAppColors.current.surface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
