package com.simats.interviewassist.ui.screens.student

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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.data.models.NotificationResponse
import com.simats.interviewassist.data.models.MarkReadRequest
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.ui.theme.*
import com.simats.interviewassist.ui.theme.LocalAppColors
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Unread", "Read")
    val scope = rememberCoroutineScope()
    
    var notifications by remember { mutableStateOf<List<NotificationResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun getTimeAgo(dateString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = sdf.parse(dateString) ?: return dateString
            val now = Date()
            val diff = now.time - date.time

            when {
                diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
                diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
                diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
                diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
                else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
            }
        } catch (e: Exception) {
            dateString
        }
    }

    fun fetchNotifications() {
        scope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.apiService.getNotifications()
                if (response.isSuccessful) {
                    notifications = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    fun markAsRead(id: Int) {
        // Optimistic update
        notifications = notifications.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        
        scope.launch {
            try {
                RetrofitClient.apiService.markNotificationsRead(MarkReadRequest(listOf(id)))
            } catch (e: Exception) {
                // Optional: Revert on failure
            }
        }
    }

    fun markAllAsRead() {
        val unreadIds = notifications.filter { !it.isRead }.map { it.id }
        if (unreadIds.isEmpty()) return

        // Optimistic update
        notifications = notifications.map { it.copy(isRead = true) }
        
        scope.launch {
            try {
                RetrofitClient.apiService.markNotificationsRead(MarkReadRequest(unreadIds))
            } catch (e: Exception) {
                // Optional: Revert on failure
            }
        }
    }

    fun deleteNotification(id: Int) {
        // Optimistic update
        val previousList = notifications
        notifications = notifications.filter { it.id != id }
        
        scope.launch {
            try {
                val response = RetrofitClient.apiService.deleteNotification(id)
                if (!response.isSuccessful) {
                    notifications = previousList
                }
            } catch (e: Exception) {
                notifications = previousList
            }
        }
    }

    // Refresh on resume
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                fetchNotifications()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val pullRefreshState = remember { mutableStateOf(false) }

    val displayNotifications = when (selectedTab) {
        1 -> notifications.filter { !it.isRead }
        2 -> notifications.filter { it.isRead }
        else -> notifications
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notifications",
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
                actions = {
                    IconButton(onClick = { markAllAsRead() }) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Mark all as read", tint = LocalAppColors.current.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LocalAppColors.current.surface)
            )
        },
        containerColor = LocalAppColors.current.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = LocalAppColors.current.surface,
                contentColor = LocalAppColors.current.primary,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = LocalAppColors.current.primary,
                        height = 2.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) LocalAppColors.current.primary else LocalAppColors.current.textSecondary
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (isLoading && notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LocalAppColors.current.primary)
                    }
                } else if (displayNotifications.isEmpty()) {
                    EmptyNotificationsState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(displayNotifications) { notification ->
                            NotificationItem(
                                notification = notification,
                                timeAgo = getTimeAgo(notification.date),
                                onClick = {
                                    if (!notification.isRead) {
                                        markAsRead(notification.id)
                                    }
                                    // Add navigation if target_id exists here if needed
                                },
                                onMarkAsRead = { markAsRead(notification.id) },
                                onDelete = { deleteNotification(notification.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationResponse,
    timeAgo: String,
    onClick: () -> Unit,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    val themeColor = when (notification.type) {
        "Experience" -> Color(0xFF4CAF50)
        "Upgrade" -> Color(0xFFFF9800)
        "Registration" -> Color(0xFF2196F3)
        else -> LocalAppColors.current.primary
    }

    val icon = when (notification.type) {
        "Experience" -> Icons.Default.HistoryEdu
        "Upgrade" -> Icons.Default.Stars
        "Registration" -> Icons.Default.PersonAdd
        else -> Icons.Default.Notifications
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (notification.isRead) Color.Transparent else LocalAppColors.current.primary.copy(alpha = 0.05f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = themeColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = themeColor, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        notification.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = LocalAppColors.current.textTitle,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notification.isRead) {
                        IconButton(
                            onClick = onMarkAsRead,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Mark as read",
                                tint = LocalAppColors.current.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Delete notification",
                            tint = LocalAppColors.current.textSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    notification.description,
                    fontSize = 14.sp,
                    color = LocalAppColors.current.textSecondary,
                    lineHeight = 20.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    timeAgo,
                    fontSize = 12.sp,
                    color = LocalAppColors.current.textSecondary.copy(alpha = 0.7f)
                )
            }
        }
    }
}



