package com.simats.interviewassist.ui.screens.student

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.utils.PreferenceManager
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import java.io.File
import com.simats.interviewassist.data.network.RetrofitClient
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch


@Composable
fun StudentSavedItemsScreen(
    preferenceManager: PreferenceManager,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToBecomeAlumni: () -> Unit,
    onNavigateToExperienceDetail: (String) -> Unit
) {
    // Global Data State
    val savedItems = SavedExperiencesManager.savedExperiences

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
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = onNavigateToHome,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.5f),
                        unselectedTextColor = LocalAppColors.current.textBody.copy(alpha = 0.5f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Saved") },
                    label = { Text("Saved") },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LocalAppColors.current.primary,
                        selectedTextColor = LocalAppColors.current.primary,
                        indicatorColor = LocalAppColors.current.primaryHighlight
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Upgrade") },
                    label = { Text("Upgrade") },
                    selected = false,
                    onClick = onNavigateToBecomeAlumni,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.5f),
                        unselectedTextColor = LocalAppColors.current.textBody.copy(alpha = 0.5f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PersonOutline, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = onNavigateToProfile,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.5f),
                        unselectedTextColor = LocalAppColors.current.textBody.copy(alpha = 0.5f)
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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LocalAppColors.current.surface)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Interview Assist",
                        style = MaterialTheme.typography.titleLarge,
                        color = LocalAppColors.current.textTitle
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = LocalAppColors.current.primaryHighlight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Student",
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalAppColors.current.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToNotifications,
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(LocalAppColors.current.surfaceVariant)
                    ) {
                        Icon(Icons.Default.NotificationsNone, "Notifications", tint = LocalAppColors.current.textTitle, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(LocalAppColors.current.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Settings, "Settings", tint = LocalAppColors.current.textTitle, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    val profilePicPath by preferenceManager.profilePicPathState
                    val initialsText = preferenceManager.getUserName().split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .uppercase()

                    val fallbackContent: @Composable () -> Unit = {
                        Text(
                            text = initialsText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalAppColors.current.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .shadow(2.dp, CircleShape)
                            .background(LocalAppColors.current.primaryHighlight)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profilePicPath.isNullOrEmpty()) {
                            val imageModel: Any = when {
                                profilePicPath!!.length > 1000 && (profilePicPath!!.startsWith("data:image") || profilePicPath!!.length > 1000) -> {
                                    val cleanBase64 = if (profilePicPath!!.contains(",")) profilePicPath!!.substringAfter(",") else profilePicPath!!
                                    try {
                                        android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
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
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                loading = { fallbackContent() },
                                error = { fallbackContent() }
                            )
                        } else {
                            fallbackContent()
                        }
                    }
                }
            }

            var isRefreshing by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            
            LaunchedEffect(Unit) {
                isRefreshing = true
                SavedExperiencesManager.refreshSavedExperiences()
                isRefreshing = false
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Saved Items",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (isRefreshing && savedItems.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = LocalAppColors.current.primary)
                        }
                    }
                }

                items(savedItems, key = { it.experienceId }) { item ->
                    SavedExperienceCard(
                        item = item,
                        onClick = { onNavigateToExperienceDetail(item.experienceId) },
                        onUnsave = { 
                            scope.launch {
                                SavedExperiencesManager.unsaveExperience(item.experienceId)
                            }
                        }
                    )
                }

                if (savedItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No saved items yet",
                                color = LocalAppColors.current.iconTint,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun SavedExperienceCard(
    item: SavedExperienceItem,
    onClick: () -> Unit,
    onUnsave: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = LocalAppColors.current.surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = LocalAppColors.current.borderLight
                ) {
                    val profilePic = item.userProfilePic
                    val initialsText = item.userName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
                    
                    val fallbackContent: @Composable () -> Unit = {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = initialsText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalAppColors.current.primary
                            )
                        }
                    }

                    if (!profilePic.isNullOrEmpty()) {
                        val imageModel: Any = when {
                            profilePic.length > 1000 && (profilePic.startsWith("data:image") || profilePic.length > 1000) -> {
                                val cleanBase64 = if (profilePic.contains(",")) profilePic.substringAfter(",") else profilePic
                                try {
                                    android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                                } catch (e: Exception) {
                                    ByteArray(0)
                                }
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
                            loading = { fallbackContent() },
                            error = { fallbackContent() }
                        )
                    } else {
                        fallbackContent()
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.userName, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle, fontSize = 15.sp)
                        if (item.isUserVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.CheckCircle, null, tint = LocalAppColors.current.primary, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text(item.userRole, color = LocalAppColors.current.textSecondary, fontSize = 12.sp)
                }
                DifficultyChip(item.difficulty)
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = onUnsave,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Bookmark, 
                        contentDescription = "Unsave", 
                        tint = LocalAppColors.current.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interview Context
            Row {
                Text("Interview at ", fontSize = 14.sp, color = LocalAppColors.current.textTitle)
                Text(item.companyName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Snippet
            Text(
                text = item.myExperience ?: "",
                fontSize = 14.sp,
                color = LocalAppColors.current.textBody,
                lineHeight = 20.sp,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, null, tint = LocalAppColors.current.iconTint, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(item.date, fontSize = 12.sp, color = LocalAppColors.current.iconTint)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ThumbUp, null, tint = LocalAppColors.current.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${item.helpfulCount} Helpful", fontSize = 12.sp, color = LocalAppColors.current.textSecondary)
                }
            }
        }
    }
}

@Composable
fun DifficultyChip(difficulty: String) {
    val bgColor = when (difficulty) {
        "Hard" -> LocalAppColors.current.errorBg
        "Medium" -> LocalAppColors.current.warningBg
        else -> LocalAppColors.current.successBg
    }
    val textColor = when (difficulty) {
        "Hard" -> LocalAppColors.current.error
        "Medium" -> LocalAppColors.current.warning
        else -> LocalAppColors.current.success
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = difficulty,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
