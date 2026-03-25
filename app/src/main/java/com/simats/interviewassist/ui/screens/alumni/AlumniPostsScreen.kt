package com.simats.interviewassist.ui.screens.alumni

import android.graphics.BitmapFactory
import android.util.Base64

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
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.ui.theme.AppColors
import com.simats.interviewassist.utils.PreferenceManager
import com.simats.interviewassist.data.network.RetrofitClient
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.material.icons.automirrored.filled.*
import kotlinx.coroutines.launch
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImageContent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import com.simats.interviewassist.data.models.InterviewExperienceResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniPostsScreen(
    preferenceManager: PreferenceManager,
    onNavigateToHome: () -> Unit,
    onNavigateToAssist: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToShareExperience: () -> Unit,
    onNavigateToEditPost: (String, String) -> Unit = { _, _ -> },
    onViewPost: (String, String) -> Unit = { _, _ -> }
) {
    var experiences by remember { mutableStateOf<List<InterviewExperienceResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getMyExperiences()
            }
            if (response.isSuccessful) {
                experiences = response.body() ?: emptyList()
            } else {
                errorMessage = "Failed to load your posts"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    val scope = rememberCoroutineScope()
    var unreadCount by remember { mutableStateOf(0) }

    fun fetchNotificationsCount() {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.getNotifications()
                if (response.isSuccessful) {
                    unreadCount = response.body()?.count { !it.isRead } ?: 0
                }
            } catch (e: Exception) { /* Handle error or log */ }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                fetchNotificationsCount()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        bottomBar = {
            Surface(
                color = LocalAppColors.current.surface,
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.5f))
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
                        icon = { Icon(Icons.Default.Description, contentDescription = "Posts", modifier = Modifier.size(26.dp)) },
                        label = { Text("Posts", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        selected = true,
                        onClick = { },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LocalAppColors.current.primary,
                            selectedTextColor = LocalAppColors.current.primary,
                            indicatorColor = LocalAppColors.current.primaryHighlight
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile", modifier = Modifier.size(26.dp)) },
                        label = { Text("Profile", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        selected = false,
                        onClick = onNavigateToProfile,
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.4f),
                            unselectedTextColor = LocalAppColors.current.textBody.copy(alpha = 0.4f),
                            indicatorColor = LocalAppColors.current.primaryHighlight.copy(alpha = 0.5f)
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
            Surface(
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
                    Column {
                        Text(
                            text = "My Contributions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = LocalAppColors.current.textTitle
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Surface(
                                color = LocalAppColors.current.primaryHighlight,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "ALUMNI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LocalAppColors.current.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = LocalAppColors.current.surfaceVariant.copy(alpha = 0.5f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            IconButton(onClick = onNavigateToNotifications) {
                                BadgedBox(
                                    badge = {
                                        if (unreadCount > 0) {
                                            Badge(
                                                containerColor = LocalAppColors.current.primary,
                                                modifier = Modifier.size(8.dp)
                                            )
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.NotificationsNone, "Notifications", tint = LocalAppColors.current.textTitle, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        Surface(
                            color = LocalAppColors.current.surfaceVariant.copy(alpha = 0.5f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            IconButton(onClick = onNavigateToSettings) {
                                Icon(Icons.Default.Settings, "Settings", tint = LocalAppColors.current.textTitle, modifier = Modifier.size(20.dp))
                            }
                        }
                        val profilePicPath = preferenceManager.getProfilePicPath()
                        val headerInitials = run {
                            val fn = preferenceManager.firstNameState.value
                            val ln = preferenceManager.lastNameState.value
                            if (fn.isNotEmpty() && ln.isNotEmpty()) "${fn[0]}${ln[0]}" else "A"
                        }
                        val headerFallback: @Composable () -> Unit = {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(LocalAppColors.current.primaryHighlight)) {
                                Text(headerInitials.uppercase(), style = MaterialTheme.typography.labelLarge, color = LocalAppColors.current.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { onNavigateToProfile() },
                            color = LocalAppColors.current.primaryHighlight,
                            shape = CircleShape
                        ) {
                            if (!profilePicPath.isNullOrEmpty()) {
                                val imageModel: Any = when {
                                    profilePicPath.length > 1000 -> {
                                        val clean = if (profilePicPath.contains(",")) profilePicPath.substringAfter(",") else profilePicPath
                                        try { android.util.Base64.decode(clean, android.util.Base64.DEFAULT) } catch (e: Exception) { ByteArray(0) }
                                    }
                                    profilePicPath.startsWith("http") -> profilePicPath
                                    profilePicPath.startsWith("/") -> File(profilePicPath)
                                    else -> "${RetrofitClient.BASE_URL.removeSuffix("/")}/${profilePicPath.removePrefix("/")}"
                                }
                                SubcomposeAsyncImage(
                                    model = imageModel,
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    loading = { headerFallback() },
                                    error = { headerFallback() }
                                )
                            } else {
                                headerFallback()
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LocalAppColors.current.primary)
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage ?: "Error", color = LocalAppColors.current.error)
                }
            } else if (experiences.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PostAdd, null, modifier = Modifier.size(64.dp), tint = LocalAppColors.current.textSecondary.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No experiences shared yet", color = LocalAppColors.current.textSecondary)
                        Button(
                            onClick = onNavigateToShareExperience,
                            modifier = Modifier.padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary)
                        ) {
                            Text("Share Now")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    item {
                        Text(
                            "Your Impact",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = LocalAppColors.current.textTitle,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        // Stats Banner
                        TotalPostsBanner(count = experiences.size)
                        
                        Text(
                            "Contribution History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LocalAppColors.current.textTitle,
                            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
                        )
                    }

                    items(experiences) { exp ->
                        ContributionItem(
                            exp = exp,
                            preferenceManager = preferenceManager,
                            onEdit = { onNavigateToEditPost(exp.companyName ?: "Company", exp.id.toString()) },
                            onDelete = {
                                scope.launch {
                                    try {
                                        val response = withContext(kotlinx.coroutines.Dispatchers.IO) {
                                            RetrofitClient.apiService.deleteExperience(exp.id)
                                        }
                                        if (response.isSuccessful) {
                                            experiences = experiences.filter { it.id != exp.id }
                                        }
                                    } catch (e: Exception) { /* silent */ }
                                }
                            },
                            onViewPost = { onViewPost(exp.companyName ?: "Company", exp.id.toString()) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TotalPostsBanner(count: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF6366F1),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Total Posts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Filled.Article,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun ContributionItem(
    exp: InterviewExperienceResponse,
    preferenceManager: PreferenceManager,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onViewPost: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Experience?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete your experience at ${exp.companyName ?: "this company"} and remove it from the review queue. This action cannot be undone.", style = MaterialTheme.typography.bodyMedium, color = LocalAppColors.current.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.error)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = LocalAppColors.current.textSecondary)
                }
            },
            containerColor = LocalAppColors.current.surface
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewPost() },
        shape = RoundedCornerShape(28.dp),
        color = LocalAppColors.current.surface,
        border = BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.5f)),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: Avatar, Name, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = LocalAppColors.current.primaryHighlight
                    ) {
                        val profilePic = if (!exp.userProfilePic.isNullOrBlank()) exp.userProfilePic else preferenceManager.getProfilePicPath()
                        val cardInitials = if (exp.userName.isNotBlank() && exp.userName != "Unknown") {
                            exp.userName.take(1).uppercase()
                        } else {
                            preferenceManager.getUserName().take(1).uppercase().takeIf { it.isNotEmpty() } ?: "A"
                        }
                        val cardFallback: @Composable () -> Unit = {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(cardInitials, color = LocalAppColors.current.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (!profilePic.isNullOrEmpty()) {
                            val imageModel: Any = when {
                                profilePic.length > 1000 -> {
                                    val clean = if (profilePic.contains(",")) profilePic.substringAfter(",") else profilePic
                                    try { android.util.Base64.decode(clean, android.util.Base64.DEFAULT) } catch (e: Exception) { ByteArray(0) }
                                }
                                profilePic.startsWith("http") -> profilePic
                                profilePic.startsWith("/") -> File(profilePic)
                                else -> "${RetrofitClient.BASE_URL.removeSuffix("/")}/${profilePic.removePrefix("/")}"
                            }
                            SubcomposeAsyncImage(
                                model = imageModel,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                loading = { cardFallback() },
                                error = { cardFallback() }
                            )
                        } else {
                            cardFallback()
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Me", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
                        Text(exp.userRole, style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.textSecondary)
                    }
                }
                
                Surface(
                    color = when(exp.status) {
                        "approved" -> LocalAppColors.current.success.copy(alpha = 0.1f)
                        "rejected" -> LocalAppColors.current.error.copy(alpha = 0.1f)
                        else -> LocalAppColors.current.warning.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, when(exp.status) {
                        "approved" -> LocalAppColors.current.success.copy(alpha = 0.2f)
                        "rejected" -> LocalAppColors.current.error.copy(alpha = 0.2f)
                        else -> LocalAppColors.current.warning.copy(alpha = 0.2f)
                    })
                ) {
                    Text(
                        exp.status.uppercase(),
                        color = when(exp.status) {
                            "approved" -> LocalAppColors.current.success
                            "rejected" -> LocalAppColors.current.error
                            else -> LocalAppColors.current.warning
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Company & Difficulty
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exp.companyName ?: "Company",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle
                    )
                    Text(
                        text = "Interview Experience",
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalAppColors.current.textSecondary
                    )
                }
                
                Surface(
                    color = when(exp.difficulty) {
                        "Easy" -> LocalAppColors.current.success.copy(alpha = 0.1f)
                        "Medium" -> LocalAppColors.current.warning.copy(alpha = 0.1f)
                        "Hard" -> LocalAppColors.current.error.copy(alpha = 0.1f)
                        else -> LocalAppColors.current.surfaceVariant
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        exp.difficulty,
                        color = when(exp.difficulty) {
                            "Easy" -> LocalAppColors.current.success
                            "Medium" -> LocalAppColors.current.warning
                            "Hard" -> LocalAppColors.current.error
                            else -> LocalAppColors.current.textSecondary
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                exp.brief ?: "No details provided.",
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.textBody,
                lineHeight = 22.sp,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = LocalAppColors.current.divider.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Footer Row: Date & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = LocalAppColors.current.textSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(exp.date ?: "Recently", style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.textSecondary)
                    
                    if (exp.helpfulCount > 0) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(Icons.Default.ThumbUp, null, modifier = Modifier.size(14.dp), tint = LocalAppColors.current.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${exp.helpfulCount} Helpful", style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.primary, fontWeight = FontWeight.Bold)
                    }
                }
                
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Edit button — only for approved posts
                    if (exp.status == "approved") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = LocalAppColors.current.primary.copy(alpha = 0.07f),
                            shape = RoundedCornerShape(12.dp),
                            onClick = onEdit
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Icon(Icons.Default.Edit, null, tint = LocalAppColors.current.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LocalAppColors.current.primary)
                            }
                        }
                    }

                    // Delete button — always visible
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = LocalAppColors.current.error.copy(alpha = 0.07f),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { showDeleteDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(Icons.Default.DeleteOutline, null, tint = LocalAppColors.current.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LocalAppColors.current.error)
                        }
                    }
                }
            }
        }
    }
}
