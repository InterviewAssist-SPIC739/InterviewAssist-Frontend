package com.simats.interviewassist.ui.screens.admin

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.data.models.NotificationResponse
import com.simats.interviewassist.data.models.MarkReadRequest
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.ui.theme.LocalAppColors
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificationsScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String, Int) -> Unit
) {
    var notifications by remember { mutableStateOf<List<NotificationResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("All") }
    var searchText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val filters = listOf("All", "Unread", "Read")

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
                val response = RetrofitClient.apiService.getAdminNotifications()
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
        notifications = notifications.map { 
            if (it.id == id) it.copy(isRead = true) else it
        }
        
        scope.launch {
            try {
                RetrofitClient.apiService.markAdminNotificationsRead(com.simats.interviewassist.data.models.MarkReadRequest(listOf(id)))
            } catch (e: Exception) { }
        }
    }

    fun markAllAsRead() {
        val unreadIds = notifications.filter { !it.isRead }.map { it.id }
        if (unreadIds.isEmpty()) return

        notifications = notifications.map { it.copy(isRead = true) }
        
        scope.launch {
            try {
                RetrofitClient.apiService.markAdminNotificationsRead(com.simats.interviewassist.data.models.MarkReadRequest(unreadIds))
            } catch (e: Exception) { }
        }
    }

    fun deleteNotification(id: Int) {
        val previousList = notifications
        notifications = notifications.filter { it.id != id }
        
        scope.launch {
            try {
                val response = RetrofitClient.apiService.deleteAdminNotification(id)
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

    val filteredNotifications = notifications.filter { 
        val matchesFilter = when (selectedFilter) {
            "Unread" -> !it.isRead
            "Read" -> it.isRead
            else -> true
        }
        matchesFilter &&
        (it.title.contains(searchText, ignoreCase = true) || it.description.contains(searchText, ignoreCase = true))
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
        containerColor = LocalAppColors.current.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search notifications...", color = LocalAppColors.current.iconTint) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LocalAppColors.current.primary) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LocalAppColors.current.surface,
                    unfocusedContainerColor = LocalAppColors.current.surface,
                    focusedBorderColor = LocalAppColors.current.primary,
                    unfocusedBorderColor = LocalAppColors.current.divider
                ),
                singleLine = true
            )

            // Filter Tabs (More like the alumni version)
            TabRow(
                selectedTabIndex = filters.indexOf(selectedFilter),
                containerColor = LocalAppColors.current.background, // Match screen bg
                contentColor = LocalAppColors.current.primary,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[filters.indexOf(selectedFilter)]),
                        color = LocalAppColors.current.primary,
                        height = 2.dp
                    )
                }
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Tab(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        text = {
                            Text(
                                filter,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) LocalAppColors.current.primary else LocalAppColors.current.textSecondary
                            )
                        }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LocalAppColors.current.primary)
                }
            } else if (filteredNotifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = LocalAppColors.current.iconTint
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No notifications found",
                            color = LocalAppColors.current.textSecondary,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredNotifications, key = { it.id }) { notification ->
                        NotificationItem(
                            notification = notification,
                            timeAgo = getTimeAgo(notification.date),
                            onClick = {
                                if (!notification.isRead) {
                                    markAsRead(notification.id)
                                }
                                if (notification.targetId != null) {
                                    onNavigateToDetail(notification.type, notification.targetId)
                                }
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
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = themeColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = themeColor, modifier = Modifier.size(22.dp))
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
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!notification.isRead) {
                            Surface(
                                color = LocalAppColors.current.primary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "NEW",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
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
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        timeAgo,
                        fontSize = 12.sp,
                        color = LocalAppColors.current.textSecondary.copy(alpha = 0.7f)
                    )
                    
                    if (notification.targetId != null) {
                        Text(
                            "View Details",
                            fontSize = 12.sp,
                            color = LocalAppColors.current.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
