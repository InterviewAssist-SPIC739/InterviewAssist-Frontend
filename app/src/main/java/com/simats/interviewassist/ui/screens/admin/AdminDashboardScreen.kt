package com.simats.interviewassist.ui.screens.admin

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.utils.PreferenceManager
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.simats.interviewassist.data.network.RetrofitClient
import kotlinx.coroutines.launch
import com.simats.interviewassist.data.models.NotificationResponse
import com.simats.interviewassist.data.models.DashboardStatsResponse


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    preferenceManager: PreferenceManager,
    onNavigateToHome: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToReviews: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToAlumniRequests: () -> Unit = {},
    onNavigateToReports: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var unreadCount by remember { mutableStateOf(0) }
    var stats by remember { mutableStateOf<DashboardStatsResponse>(DashboardStatsResponse(0, 0, 0, 0, 0)) }
    var isLoading by remember { mutableStateOf(true) }

    fun refreshData() {
        scope.launch {
            if (stats.totalUsers == 0) isLoading = true
            try {
                // 1. Fetch unread count directly for accuracy
                val notificationsResponse = RetrofitClient.apiService.getAdminNotifications()
                if (notificationsResponse.isSuccessful) {
                    unreadCount = notificationsResponse.body()?.count { !it.isRead } ?: 0
                }

                // 2. Fetch dashboard stats
                val statsResponse = RetrofitClient.apiService.getDashboardStats()
                if (statsResponse.isSuccessful) {
                    val body = statsResponse.body()
                    if (body != null) {
                        stats = body
                        // Only use stats for unreadCount if notificationsResponse failed
                        if (!notificationsResponse.isSuccessful) {
                            unreadCount = body.unreadNotificationsCount
                        }
                    }
                }
                
                // 3. Fetch reports separately
                val reportsResponse = RetrofitClient.apiService.getReports()
                if (reportsResponse.isSuccessful) {
                    val reports = reportsResponse.body()
                    if (reports != null) {
                        stats = stats.copy(reportsCount = reports.size)
                    }
                }
            } catch (e: Exception) { 
            } finally {
                isLoading = false
            }
        }
    }

    // Refresh count and stats on resume
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    
    LaunchedEffect(Unit) {
        refreshData()
    }
    
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                refreshData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 10.sp) },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LocalAppColors.current.primary,
                        selectedTextColor = LocalAppColors.current.primary,
                        indicatorColor = LocalAppColors.current.primaryHighlight
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
                    IconButton(
                        onClick = onNavigateToNotifications,
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(LocalAppColors.current.surfaceVariant)
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = LocalAppColors.current.primary,
                                        contentColor = Color.White
                                    ) {
                                        Text(if (unreadCount > 99) "99+" else unreadCount.toString(), fontSize = 10.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                if (unreadCount > 0) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                                "Notifications",
                                tint = LocalAppColors.current.textTitle,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(LocalAppColors.current.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Settings, "Settings", tint = LocalAppColors.current.textTitle, modifier = Modifier.size(22.dp))
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
                            preferenceManager.getUserName().take(1).uppercase() else "UP"
                        Text(initials, fontSize = 14.sp, color = LocalAppColors.current.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }


            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp)
            ) {
                item {
                    Text(
                        text = "Dashboard",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Stats Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            DashboardStatCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToUsers() },
                                icon = Icons.Default.PeopleAlt,
                                value = if (isLoading && stats.totalUsers == 0) "..." else stats.totalUsers.toString(),
                                label = "Total Users",
                                color = LocalAppColors.current.primary
                            )
                            DashboardStatCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToReviews() },
                                icon = Icons.Default.Description,
                                value = if (isLoading && stats.pendingReviews == 0) "..." else stats.pendingReviews.toString(),
                                label = "Pending Reviews",
                                color = LocalAppColors.current.warning
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            DashboardStatCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToReports() },
                                icon = Icons.Default.Error,
                                value = if (isLoading && stats.reportsCount == 0) "..." else stats.reportsCount.toString(),
                                label = "Reports",
                                color = LocalAppColors.current.error
                            )
                            DashboardStatCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToAlumniRequests() },
                                icon = Icons.Default.TrendingUp,
                                value = if (isLoading && stats.newAlumni == 0) "..." else stats.newAlumni.toString(),
                                label = "New Alumni",
                                color = LocalAppColors.current.success
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Recent Activity
                item {
                    Text(
                        text = "Recent Activity",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            if (stats.recentActivities.isEmpty()) {
                                Text(
                                    "No recent activity",
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    textAlign = TextAlign.Center,
                                    color = LocalAppColors.current.textSecondary,
                                    fontSize = 14.sp
                                )
                            } else {
                                stats.recentActivities.forEach { activity ->
                                    RecentActivityItem(
                                        activity.userName,
                                        activity.action,
                                        activity.target,
                                        activity.time
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = color.copy(alpha = 1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, modifier = Modifier.size(24.dp), tint = LocalAppColors.current.surface)
                    }
                }
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = LocalAppColors.current.borderLight,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalAppColors.current.textTitle
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = LocalAppColors.current.textSecondary
                )
            }
        }
    }
}

@Composable
fun RecentActivityItem(
    user: String,
    action: String,
    target: String,
    time: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(10.dp)
                .background(LocalAppColors.current.primary, CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)) {
                        append(user)
                    }
                    append("  $action  ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)) {
                        append(target)
                    }
                    append(" .")
                },
                fontSize = 14.sp,
                color = LocalAppColors.current.textBody,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = time,
                fontSize = 12.sp,
                color = LocalAppColors.current.iconTint
            )
        }
    }
}
