package com.simats.interviewassist.ui.screens.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.*
import com.simats.interviewassist.ui.screens.student.CompanyItem
import com.simats.interviewassist.ui.screens.student.getMockCompany
import com.simats.interviewassist.ui.screens.student.FilterBottomSheet
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.utils.PreferenceManager
import com.simats.interviewassist.data.models.CompanyResponse
import com.simats.interviewassist.data.network.RetrofitClient
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import kotlinx.coroutines.launch
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.simats.interviewassist.ui.screens.student.ProfileCompletionPopup
import com.simats.interviewassist.ui.screens.student.DateFilterButton
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniHomeScreen(
    preferenceManager: PreferenceManager,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToPosts: () -> Unit = {},
    onNavigateToAssist: () -> Unit = {},
    onNavigateToShareExperience: () -> Unit = {},
    onNavigateToAllCompanies: () -> Unit = {},
    onNavigateToCompanyDetails: (Int, String) -> Unit = { _, _ -> },
    onNavigateToCompleteProfile: (String, Int) -> Unit = { _, _ -> }
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "IT Services", "AI Solutions", "Software", "Consulting")

    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current
    
    var selectedIndustries by remember { mutableStateOf(setOf<String>()) }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }
    var selectedDateRange by remember { mutableStateOf("All Time") }

    val userId = remember { preferenceManager.getUserId() }
    var showProfilePrompt by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
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
    val scope = rememberCoroutineScope()
    var unreadCount by remember { mutableStateOf(0) }

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

    val filteredCompanies = remember(searchQuery, selectedCategory, selectedIndustries, selectedDifficulty, allCompanies) {
        allCompanies.filter { company ->
            val matchesSearch = company.name.contains(searchQuery, ignoreCase = true) ||
                    (company.sector?.contains(searchQuery, ignoreCase = true) == true)
            
            val matchesCategory = selectedCategory == "All" || 
                    (company.sector?.contains(selectedCategory, ignoreCase = true) == true)
            
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
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(26.dp)) },
                        label = { Text("Home", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        selected = true,
                        onClick = { },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LocalAppColors.current.primary,
                            selectedTextColor = LocalAppColors.current.primary,
                            indicatorColor = LocalAppColors.current.primaryHighlight
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToShareExperience,
                containerColor = LocalAppColors.current.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add Experience", fontWeight = FontWeight.Bold) }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LocalAppColors.current.background)
                .padding(padding)
        ) {
            // Premium Header
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
                    Column {
                        Text(
                            text = "Interview Assist",
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
                        val fallbackContent: @Composable () -> Unit = {
                            val initials = if (preferenceManager.getUserName().isNotEmpty()) 
                                preferenceManager.getUserName().take(1).uppercase() else "A"
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(LocalAppColors.current.primaryHighlight)) {
                                Text(initials, style = MaterialTheme.typography.titleSmall, color = LocalAppColors.current.primary, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { onNavigateToProfile() },
                            color = LocalAppColors.current.primaryHighlight,
                            shape = CircleShape,
                            border = BorderStroke(2.dp, LocalAppColors.current.primary.copy(alpha = 0.1f))
                        ) {
                            if (!profilePicPath.isNullOrEmpty()) {
                                val imageModel: Any = when {
                                    profilePicPath.length > 1000 && (profilePicPath.startsWith("data:image") || profilePicPath.length > 1000) -> {
                                        val cleanBase64 = if (profilePicPath.contains(",")) profilePicPath.substringAfter(",") else profilePicPath
                                        try {
                                            android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                                        } catch (e: Exception) {
                                            ByteArray(0)
                                        }
                                    }
                                    profilePicPath.startsWith("http") -> profilePicPath
                                    profilePicPath.startsWith("/") -> File(profilePicPath)
                                    else -> "${RetrofitClient.BASE_URL.removeSuffix("/")}/${profilePicPath.removePrefix("/")}"
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
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp)
            ) {
                // Search & Filter
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by company or sector", style = MaterialTheme.typography.bodyLarge, color = LocalAppColors.current.textSecondary.copy(alpha = 0.6f)) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = LocalAppColors.current.textSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LocalAppColors.current.surface,
                                unfocusedContainerColor = LocalAppColors.current.surface,
                                focusedBorderColor = LocalAppColors.current.primary,
                                unfocusedBorderColor = LocalAppColors.current.divider
                            ),
                            singleLine = true
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = LocalAppColors.current.primary,
                            modifier = Modifier.size(56.dp).clickable { 
                                focusManager.clearFocus()
                                showFilterSheet = true
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Tune, "Filter", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
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

                // Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "Search Results" else "Explore Companies",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = LocalAppColors.current.textTitle
                        )
                        if (searchQuery.isBlank()) {
                            TextButton(onClick = onNavigateToAllCompanies) {
                                Text("View All", style = MaterialTheme.typography.labelLarge, color = LocalAppColors.current.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
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
                            modifier = Modifier.padding(16.dp)
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
                    Spacer(modifier = Modifier.height(48.dp))
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
                    onNavigateToCompleteProfile("Alumni", userId)
                }
            }
        )
    }
}
