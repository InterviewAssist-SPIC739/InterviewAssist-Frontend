package com.simats.interviewassist.ui.screens.student

import kotlinx.coroutines.launch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.*
import com.simats.interviewassist.R
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.utils.PreferenceManager
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import com.simats.interviewassist.data.models.CompanyResponse
import com.simats.interviewassist.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import com.simats.interviewassist.ui.screens.student.ProfileCompletionPopup
import com.simats.interviewassist.ui.screens.student.FilterBottomSheet
import com.simats.interviewassist.ui.screens.student.DateFilterButton


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StudentHomeScreen(
    preferenceManager: PreferenceManager,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMyQuestions: () -> Unit = {},
    onNavigateToSaved: () -> Unit = {},
    onNavigateToAskQuestion: () -> Unit = {},
    onNavigateToAllCompanies: () -> Unit = {},
    onNavigateToCompanyDetails: (Int, String) -> Unit = { _, _ -> },
    onNavigateToCompleteProfile: (String, Int) -> Unit = { _, _ -> },
    onNavigateToBecomeAlumni: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "IT Services", "AI Solutions", "Software", "Consulting")
    val textPrimary = LocalAppColors.current.textBody

    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current
    
    var selectedIndustries by remember { mutableStateOf(setOf<String>()) }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }
    var selectedDateRange by remember { mutableStateOf("All Time") }

    val userId = remember { preferenceManager.getUserId() }
    var showProfilePrompt by remember { mutableStateOf(false) }
    var userProfile by remember { mutableStateOf<com.simats.interviewassist.data.models.UserProfileResponse?>(null) }
    var unreadCount by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun fetchNotificationsCount() {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.getNotifications()
                if (response.isSuccessful) {
                    unreadCount = response.body()?.count { !it.isRead } ?: 0
                }
            } catch (e: Exception) { }
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
    
    LaunchedEffect(userId) {
        if (userId != -1) {
            try {
                val response = RetrofitClient.apiService.getUserProfile(userId)
                if (response.isSuccessful) {
                    userProfile = response.body()
                }
            } catch (e: Exception) { }
        }
    }

    LaunchedEffect(Unit) {
        if (userId != -1 && 
            !preferenceManager.isProfileCompleted(userId) && 
            !preferenceManager.isCompletionPromptShown(userId)) {
            showProfilePrompt = true
            preferenceManager.setCompletionPromptShown(userId, true)
        }
    }

    var allCompanies by remember { mutableStateOf<List<CompanyResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getCompanies()
            }
            if (response.isSuccessful) {
                allCompanies = response.body() ?: emptyList()
            } else {
                errorMessage = "Failed to load companies"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    val filteredCompanies = remember(searchQuery, selectedCategory, selectedIndustries, selectedDifficulty, selectedDateRange, allCompanies) {
        allCompanies.filter { company ->
            val matchesSearch = company.name.contains(searchQuery, ignoreCase = true) ||
                    (company.sector?.contains(searchQuery, ignoreCase = true) == true)
            
            val matchesCategory = selectedCategory == "All" || (company.sector?.contains(selectedCategory, ignoreCase = true) == true)
            
            val matchesIndustry = selectedIndustries.isEmpty() || (company.sector != null && selectedIndustries.contains(company.sector))
            
            val matchesDifficulty = selectedDifficulty == null || company.difficulty == selectedDifficulty
            
            matchesSearch && matchesCategory && matchesIndustry && matchesDifficulty
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
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", style = MaterialTheme.typography.labelMedium) },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LocalAppColors.current.primary,
                        selectedTextColor = LocalAppColors.current.primary,
                        unselectedIconColor = LocalAppColors.current.iconTint,
                        unselectedTextColor = LocalAppColors.current.iconTint,
                        indicatorColor = LocalAppColors.current.primaryHighlight
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BookmarkBorder, contentDescription = "Saved") },
                    label = { Text("Saved", style = MaterialTheme.typography.labelMedium) },
                    selected = false,
                    onClick = onNavigateToSaved,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = LocalAppColors.current.iconTint,
                        unselectedTextColor = LocalAppColors.current.iconTint
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Upgrade") },
                    label = { Text("Upgrade", style = MaterialTheme.typography.labelMedium) },
                    selected = false,
                    onClick = onNavigateToBecomeAlumni,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = LocalAppColors.current.iconTint,
                        unselectedTextColor = LocalAppColors.current.iconTint
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PersonOutline, contentDescription = "Profile") },
                    label = { Text("Profile", style = MaterialTheme.typography.labelMedium) },
                    selected = false,
                    onClick = onNavigateToProfile,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = LocalAppColors.current.iconTint,
                        unselectedTextColor = LocalAppColors.current.iconTint
                    )
                )
            }
        }
    },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAskQuestion,
                containerColor = LocalAppColors.current.primary,
                contentColor = LocalAppColors.current.surface,
                shape = CircleShape
            ) {
                Icon(Icons.Outlined.HelpOutline, contentDescription = "Ask Question")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LocalAppColors.current.background)
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
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                loading = { fallbackContent() },
                                error = { fallbackContent() }
                            )
                        } else {
                            fallbackContent()
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp)
            ) {
                // Search Bar
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search companies...", color = LocalAppColors.current.textSecondary) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = LocalAppColors.current.primary.copy(alpha = 0.5f),
                                unfocusedContainerColor = LocalAppColors.current.surface,
                                focusedContainerColor = LocalAppColors.current.surface,
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.Search, null, tint = LocalAppColors.current.iconTint)
                            },
                            singleLine = true
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = LocalAppColors.current.surface,
                            modifier = Modifier.size(56.dp).shadow(2.dp, RoundedCornerShape(16.dp))
                        ) {
                            IconButton(onClick = { 
                                focusManager.clearFocus()
                                showFilterSheet = true
                            }) {
                                Icon(Icons.Default.Tune, "Filter", tint = LocalAppColors.current.primary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Categories
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val isSelected = category == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LocalAppColors.current.primary,
                                    selectedLabelColor = LocalAppColors.current.surface,
                                    containerColor = LocalAppColors.current.surface,
                                    labelColor = LocalAppColors.current.textBody
                                ),
                                border = null,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Questions Card
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToMyQuestions() }
                            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = LocalAppColors.current.primary.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(24.dp),
                        color = LocalAppColors.current.primaryHighlight
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = LocalAppColors.current.surface
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ChatBubbleOutline, null, tint = LocalAppColors.current.primary, modifier = Modifier.size(24.dp))
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "My Questions",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = LocalAppColors.current.textTitle
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "View questions you've asked",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LocalAppColors.current.textBody
                                )
                            }
                            Surface(
                                color = LocalAppColors.current.primary,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${userProfile?.questionsCount ?: 0}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(36.dp))
                }

                // Top Companies
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Top Companies",
                            style = MaterialTheme.typography.titleLarge,
                            color = LocalAppColors.current.textTitle
                        )
                        TextButton(onClick = onNavigateToAllCompanies) {
                            Text(
                                "See All",
                                style = MaterialTheme.typography.labelLarge,
                                color = LocalAppColors.current.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = LocalAppColors.current.primary)
                        }
                    }
                } else if (errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage ?: "Unknown error",
                            color = LocalAppColors.current.error,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(filteredCompanies) { company ->
                        CompanyItem(
                            company = company,
                            onClick = { onNavigateToCompanyDetails(company.id, company.name) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            sheetState = sheetState,
            onDismiss = { showFilterSheet = false },
            onApply = { showFilterSheet = false },
            onReset = {
                selectedIndustries = emptySet()
                selectedDifficulty = null
                selectedDateRange = "All Time"
            },
            selectedIndustries = selectedIndustries,
            onIndustryToggle = { industry ->
                selectedIndustries = if (selectedIndustries.contains(industry)) {
                    selectedIndustries - industry
                } else {
                    selectedIndustries + industry
                }
            },
            selectedDifficulty = selectedDifficulty,
            onDifficultySelect = { selectedDifficulty = it },
            selectedDateRange = selectedDateRange,
            onDateRangeSelect = { selectedDateRange = it }
        )
    }

    if (showProfilePrompt) {
        ProfileCompletionPopup(
            onDismiss = { showProfilePrompt = false },
            onOpenProfile = {
                showProfilePrompt = false
                if (userId != -1) {
                    onNavigateToCompleteProfile("Student", userId)
                }
            }
        )
    }
}


// Removed ProfileCompletionPopup and FilterBottomSheet - Moved to StudentComponents.kt


// Removed Company and CompanyItem - Moved to StudentComponents.kt



