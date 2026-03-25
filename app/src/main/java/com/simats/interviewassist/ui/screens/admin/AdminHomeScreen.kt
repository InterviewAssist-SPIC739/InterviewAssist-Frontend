package com.simats.interviewassist.ui.screens.admin

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import coil.compose.AsyncImage
import com.simats.interviewassist.utils.PreferenceManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.simats.interviewassist.data.network.RetrofitClient
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.*
import com.simats.interviewassist.R
import com.simats.interviewassist.ui.screens.student.Company
import com.simats.interviewassist.ui.screens.student.getMockCompany
import com.simats.interviewassist.ui.screens.student.ExamSection
import com.simats.interviewassist.ui.screens.student.ProcessStep
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.BorderStroke
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.data.models.*
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    preferenceManager: PreferenceManager,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onAddCompany: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToAlumniRequests: () -> Unit = {},
    onNavigateToReviews: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToCompanyDetails: (Int, String) -> Unit = { _, _ -> }
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddCompanySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Form states
    var companyName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Medium") }
    val examPattern = remember { mutableStateListOf<ExamSection>() }
    val hiringProcess = remember { mutableStateListOf<ProcessStep>() }

    // Sub-form states for lists
    var showAddExamSection by remember { mutableStateOf(false) }
    var sectionName by remember { mutableStateOf("") }
    var sectionQuestions by remember { mutableStateOf("") }
    var sectionTime by remember { mutableStateOf("") }
    var sectionLevel by remember { mutableStateOf("Medium") }

    var showAddHiringStep by remember { mutableStateOf(false) }
    var stepTitle by remember { mutableStateOf("") }
    var stepDuration by remember { mutableStateOf("") }

    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var logoBase64 by remember { mutableStateOf<String?>(null) }
    var editingCompanyId by remember { mutableStateOf<Int?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<CompanyResponse?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val adminCompanies = remember { mutableStateListOf<CompanyResponse>() }
    var isFetching by remember { mutableStateOf(false) }
    var unreadCount by remember { mutableStateOf(0) }

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

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        logoUri = uri
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()
            logoBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
        }
    }

    // Use Lifecycle Observer to refresh count whenever user returns to this screen
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                fetchNotificationsCount()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        isFetching = true
        try {
            val response = RetrofitClient.apiService.getCompanies()
            if (response.isSuccessful) {
                adminCompanies.clear()
                response.body()?.let { list -> adminCompanies.addAll(list) }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error fetching companies", Toast.LENGTH_SHORT).show()
        } finally {
            isFetching = false
        }
    }

    val filteredCompanies = remember(searchQuery, adminCompanies.toList()) {
        adminCompanies.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || 
            (it.sector?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    if (showAddCompanySheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showAddCompanySheet = false
                // Reset fields
                companyName = ""
                location = ""
                industry = ""
                website = ""
                logoUri = null
                logoBase64 = null
                editingCompanyId = null
            },
            sheetState = sheetState,
            containerColor = LocalAppColors.current.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = LocalAppColors.current.borderLight) }
        ) {
            AddCompanySheetContent(
                companyName = companyName,
                onCompanyNameChange = { companyName = it },
                location = location,
                onLocationChange = { location = it },
                industry = industry,
                onIndustryChange = { industry = it },
                website = website,
                onWebsiteChange = { website = it },
                description = description,
                onDescriptionChange = { description = it },
                difficulty = difficulty,
                onDifficultyChange = { difficulty = it },
                examSections = examPattern,
                hiringSteps = hiringProcess,
                isEditing = editingCompanyId != null,
                onAddSection = { name, qs, time, level -> 
                    examPattern.add(ExamSection(name, qs, time, level)) 
                },
                onAddStep = { title, duration -> 
                    hiringProcess.add(ProcessStep(title, duration)) 
                },
                onPickLogo = { launcher.launch("image/*") },
                logoUri = logoUri,
                onCancel = { showAddCompanySheet = false },
                onAdd = {
                    if (companyName.isNotBlank()) {
                        scope.launch {
                            try {
                                val request = AddCompanyRequest(
                                    name = companyName,
                                    location = location,
                                    sector = industry,
                                    logo = logoBase64,
                                    difficulty = difficulty,
                                    description = description,
                                    websiteUrl = website,
                                    examPattern = examPattern.toList(),
                                    hiringProcess = hiringProcess.toList()
                                )
                                val response = if (editingCompanyId != null) {
                                    RetrofitClient.apiService.updateCompany(editingCompanyId!!, request)
                                } else {
                                    RetrofitClient.apiService.addCompany(request)
                                }

                                if (response.isSuccessful) {
                                    val company = response.body()?.company
                                    if (company != null) {
                                        if (editingCompanyId != null) {
                                            val index = adminCompanies.indexOfFirst { it.id == editingCompanyId }
                                            if (index != -1) adminCompanies[index] = company
                                            Toast.makeText(context, "Company details updated successfully", Toast.LENGTH_SHORT).show()
                                        } else {
                                            adminCompanies.add(0, company)
                                            Toast.makeText(context, "Company added successfully", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    showAddCompanySheet = false
                                    // Reset fields
                                    editingCompanyId = null
                                    companyName = ""
                                    location = ""
                                    industry = ""
                                    website = ""
                                    description = ""
                                    difficulty = "Medium"
                                    examPattern.clear()
                                    hiringProcess.clear()
                                    logoUri = null
                                    logoBase64 = null
                                } else {
                                    Toast.makeText(context, "Failed to save company", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
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
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LocalAppColors.current.primary,
                        selectedTextColor = LocalAppColors.current.primary,
                        indicatorColor = LocalAppColors.current.primaryHighlight
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
                            .clip(CircleShape)
                            .background(LocalAppColors.current.primaryHighlight)
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
                // Section Title and Add Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Companies",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalAppColors.current.textTitle
                        )
                        Button(
                            onClick = { showAddCompanySheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Company", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Search Bar
                item {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search companies...", color = LocalAppColors.current.textBody.copy(alpha = 0.5f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = LocalAppColors.current.surface,
                            unfocusedContainerColor = LocalAppColors.current.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Company List
                items(filteredCompanies) { company ->
                    AdminCompanyItem(
                        company = company,
                        onClick = { onNavigateToCompanyDetails(company.id, company.name) },
                        onEdit = {
                            editingCompanyId = company.id
                            companyName = company.name
                            location = company.location ?: ""
                            industry = company.sector ?: ""
                            website = company.websiteUrl ?: ""
                            description = company.description ?: ""
                            difficulty = company.difficulty
                            examPattern.clear()
                            company.examPattern?.let { examPattern.addAll(it) }
                            hiringProcess.clear()
                            company.hiringProcess?.let { hiringProcess.addAll(it) }
                            logoBase64 = company.logo
                            // Note: logoUri remains null as it's for picking new images
                            showAddCompanySheet = true
                        },
                        onDelete = {
                            showDeleteConfirm = company
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (showDeleteConfirm != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = null },
                title = { Text("Delete Company", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete ${showDeleteConfirm?.name}? This action cannot be undone.", color = LocalAppColors.current.textBody) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val companyToDelete = showDeleteConfirm!!
                            scope.launch {
                                try {
                                    val response = RetrofitClient.apiService.deleteCompany(companyToDelete.id)
                                    if (response.isSuccessful) {
                                        adminCompanies.remove(companyToDelete)
                                        Toast.makeText(context, "Company deleted", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to delete company", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                                showDeleteConfirm = null
                            }
                        }
                    ) {
                        Text("Delete", color = LocalAppColors.current.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = null }) {
                        Text("Cancel", color = LocalAppColors.current.textSecondary)
                    }
                },
                containerColor = LocalAppColors.current.surface
            )
        }
    }
}

@Composable
fun AdminCompanyItem(company: CompanyResponse, onClick: () -> Unit = {}, onEdit: () -> Unit = {}, onDelete: () -> Unit = {}) {
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = LocalAppColors.current.background
            ) {
                if (company.logo != null) {
                    val bitmap = remember(company.logo) {
                        try {
                            val bytes = Base64.decode(company.logo, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } catch (e: Exception) { null }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = company.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Business, null, modifier = Modifier.padding(12.dp), tint = LocalAppColors.current.iconTint)
                    }
                } else {
                    Icon(Icons.Default.Business, null, modifier = Modifier.padding(12.dp), tint = LocalAppColors.current.iconTint)
                }
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = company.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalAppColors.current.textTitle
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = LocalAppColors.current.textBody)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = company.location ?: "",
                            fontSize = 14.sp,
                            color = LocalAppColors.current.textBody
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BusinessCenter, null, modifier = Modifier.size(14.dp), tint = LocalAppColors.current.textBody)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = company.sector ?: "",
                            fontSize = 14.sp,
                            color = LocalAppColors.current.textBody
                        )
                    }
                }
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = LocalAppColors.current.textBody)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp)) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = LocalAppColors.current.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = LocalAppColors.current.error, modifier = Modifier.size(20.dp)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCompanySheetContent(
    companyName: String,
    onCompanyNameChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    industry: String,
    onIndustryChange: (String) -> Unit,
    website: String,
    onWebsiteChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    difficulty: String,
    onDifficultyChange: (String) -> Unit,
    examSections: SnapshotStateList<ExamSection>,
    hiringSteps: SnapshotStateList<ProcessStep>,
    onAddSection: (String, Int, String, String) -> Unit,
    onAddStep: (String, String) -> Unit,
    onPickLogo: () -> Unit,
    logoUri: Uri?,
    isEditing: Boolean = false,
    onCancel: () -> Unit,
    onAdd: () -> Unit
) {
    var showSectionForm by remember { mutableStateOf(false) }
    var showStepForm by remember { mutableStateOf(false) }

    // Sub-form internal states
    var newSectionName by remember { mutableStateOf("") }
    var newSectionQs by remember { mutableStateOf("") }
    var newSectionTime by remember { mutableStateOf("") }
    var newSectionLevel by remember { mutableStateOf("Medium") }

    var newStepTitle by remember { mutableStateOf("") }
    var newStepDuration by remember { mutableStateOf("") }

    var editingSectionIndex by remember { mutableStateOf(-1) }
    var editingStepIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isEditing) "Edit Company Details" else "Add Company Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LocalAppColors.current.textTitle
            )
            IconButton(onClick = {
                editingSectionIndex = -1
                editingStepIndex = -1
                onCancel()
            }) {
                Icon(Icons.Default.Close, null, tint = LocalAppColors.current.iconTint)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Logo Picker
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LocalAppColors.current.background)
                    .clickable { onPickLogo() },
                contentAlignment = Alignment.Center
            ) {
                if (logoUri != null) {
                    AsyncImage(
                        model = logoUri,
                        contentDescription = "Company Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = LocalAppColors.current.primary, modifier = Modifier.size(32.dp))
                        Text("Add Logo", fontSize = 12.sp, color = LocalAppColors.current.primary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Basic Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.primary)
        Spacer(modifier = Modifier.height(16.dp))
        
        AdminInputField(label = "Company Name", value = companyName, onValueChange = onCompanyNameChange, placeholder = "e.g. Google")
        Spacer(modifier = Modifier.height(20.dp))
        AdminInputField(label = "Location", value = location, onValueChange = onLocationChange, placeholder = "e.g. Mountain View, CA")
        Spacer(modifier = Modifier.height(20.dp))
        AdminInputField(label = "Industry", value = industry, onValueChange = onIndustryChange, placeholder = "e.g. Technology")
        Spacer(modifier = Modifier.height(20.dp))
        AdminInputField(label = "Website", value = website, onValueChange = onWebsiteChange, placeholder = "https://...")
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("Overview Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.primary)
        Spacer(modifier = Modifier.height(16.dp))

        AdminInputField(
            label = "Company Description", 
            value = description, 
            onValueChange = onDescriptionChange, 
            placeholder = "Briefly describe the company...",
            singleLine = false,
            minLines = 3
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Column {
            Text(
                "Interview Difficulty",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Easy", "Medium", "Hard").forEach { level ->
                    val isSelected = difficulty == level
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDifficultyChange(level) },
                        color = if (isSelected) LocalAppColors.current.primary else LocalAppColors.current.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            level,
                            modifier = Modifier.padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            color = if (isSelected) LocalAppColors.current.surface else LocalAppColors.current.textSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        // Exam Pattern Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Exam Pattern", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
            TextButton(onClick = { 
                showSectionForm = !showSectionForm
                if (!showSectionForm) editingSectionIndex = -1
            }) {
                Text(if (showSectionForm) "Done" else "+ Add Section")
            }
        }

        if (showSectionForm) {
            Surface(
                color = LocalAppColors.current.background,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 16.dp),
                border = BorderStroke(1.dp, LocalAppColors.current.divider)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AdminInputField(label = "Section Name", value = newSectionName, onValueChange = { newSectionName = it }, placeholder = "e.g. Aptitude")
                    Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            AdminInputField(label = "Qs", value = newSectionQs, onValueChange = { newSectionQs = it }, placeholder = "20")
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AdminInputField(label = "Time", value = newSectionTime, onValueChange = { newSectionTime = it }, placeholder = "30m")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Section Difficulty", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = LocalAppColors.current.textSecondary)
                    Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Easy", "Medium", "Hard").forEach { level ->
                            val isSelected = newSectionLevel == level
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { newSectionLevel = level },
                                color = if (isSelected) LocalAppColors.current.primary else LocalAppColors.current.divider,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    level,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp,
                                    color = if (isSelected) LocalAppColors.current.surface else LocalAppColors.current.textSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Button(
                        onClick = {
                            if (newSectionName.isNotBlank()) {
                                val section = ExamSection(newSectionName, newSectionQs.toIntOrNull() ?: 0, newSectionTime, newSectionLevel)
                                if (editingSectionIndex != -1) {
                                    examSections[editingSectionIndex] = section
                                } else {
                                    examSections.add(section)
                                }
                                newSectionName = ""
                                newSectionQs = ""
                                newSectionTime = ""
                                newSectionLevel = "Medium"
                                editingSectionIndex = -1
                                showSectionForm = false
                            }
                        },
                        modifier = Modifier.align(Alignment.End).padding(top = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary)
                    ) {
                        Text("Save Section", fontSize = 12.sp)
                    }
                    Text("Note: Use the 'Add Company' button to save all changes.", fontSize = 10.sp, color = LocalAppColors.current.textSecondary, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        examSections.forEachIndexed { index, section ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                color = LocalAppColors.current.background,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(section.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${section.questions} Qs • ${section.time}", color = LocalAppColors.current.textSecondary, fontSize = 12.sp)
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = when(section.level) {
                                "Easy" -> LocalAppColors.current.successBg
                                "Medium" -> LocalAppColors.current.warningBg
                                "Hard" -> LocalAppColors.current.errorBg
                                else -> LocalAppColors.current.background
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                section.level,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when(section.level) {
                                    "Easy" -> LocalAppColors.current.success
                                    "Medium" -> LocalAppColors.current.warning
                                    "Hard" -> LocalAppColors.current.error
                                    else -> LocalAppColors.current.textSecondary
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(
                            onClick = {
                                newSectionName = section.name
                                newSectionQs = section.questions.toString()
                                newSectionTime = section.time
                                newSectionLevel = section.level
                                editingSectionIndex = index
                                showSectionForm = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Edit, "Edit", tint = if (editingSectionIndex == index) LocalAppColors.current.primary else LocalAppColors.current.primary.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                        }
                        
                        IconButton(
                            onClick = { 
                                examSections.removeAt(index)
                                if (editingSectionIndex == index) editingSectionIndex = -1
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Delete", tint = LocalAppColors.current.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hiring Process Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hiring Process", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
            TextButton(onClick = { 
                showStepForm = !showStepForm 
                if (!showStepForm) editingStepIndex = -1
            }) {
                Text(if (showStepForm) "Done" else "+ Add Step")
            }
        }

        if (showStepForm) {
            Surface(
                color = LocalAppColors.current.background,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 16.dp),
                border = BorderStroke(1.dp, LocalAppColors.current.divider)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AdminInputField(label = "Step Title", value = newStepTitle, onValueChange = { newStepTitle = it }, placeholder = "e.g. Technical Interview")
                    Spacer(modifier = Modifier.height(12.dp))
                    AdminInputField(label = "Duration", value = newStepDuration, onValueChange = { newStepDuration = it }, placeholder = "e.g. 1 Hour")
                    
                    Button(
                        onClick = {
                            if (newStepTitle.isNotBlank()) {
                                val step = ProcessStep(newStepTitle, newStepDuration)
                                if (editingStepIndex != -1) {
                                    hiringSteps[editingStepIndex] = step
                                } else {
                                    hiringSteps.add(step)
                                }
                                newStepTitle = ""
                                newStepDuration = ""
                                editingStepIndex = -1
                                showStepForm = false
                            }
                        },
                        modifier = Modifier.align(Alignment.End).padding(top = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary)
                    ) {
                        Text("Save Step", fontSize = 12.sp)
                    }
                }
            }
        }

        hiringSteps.forEachIndexed { index, step ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                color = LocalAppColors.current.background,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(step.title, fontWeight = FontWeight.Medium)
                        Text(step.duration, color = LocalAppColors.current.textSecondary, fontSize = 12.sp)
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                newStepTitle = step.title
                                newStepDuration = step.duration
                                editingStepIndex = index
                                showStepForm = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Edit, "Edit", tint = if (editingStepIndex == index) LocalAppColors.current.primary else LocalAppColors.current.primary.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                        }
                        
                        IconButton(
                            onClick = { 
                                hiringSteps.removeAt(index)
                                if (editingStepIndex == index) editingStepIndex = -1
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Delete", tint = LocalAppColors.current.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.surfaceVariant)
            ) {
                Text("Cancel", color = LocalAppColors.current.textTitle, fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = onAdd,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary)
            ) {
                Text(if (isEditing) "Update Company" else "Add Company", color = LocalAppColors.current.surface, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = LocalAppColors.current.textBody,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = LocalAppColors.current.textSecondary.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LocalAppColors.current.primary,
                unfocusedBorderColor = LocalAppColors.current.divider,
                focusedContainerColor = LocalAppColors.current.surface,
                unfocusedContainerColor = LocalAppColors.current.surface
            ),
            singleLine = singleLine,
            minLines = minLines
        )
    }
}
