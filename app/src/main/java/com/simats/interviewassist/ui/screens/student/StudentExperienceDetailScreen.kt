package com.simats.interviewassist.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.simats.interviewassist.data.models.InterviewExperienceResponse
import com.simats.interviewassist.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import android.util.Base64
import android.graphics.BitmapFactory
import coil.compose.SubcomposeAsyncImage
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImageContent
import com.simats.interviewassist.ui.screens.student.ReportSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentExperienceDetailScreen(
    companyName: String, // Keeping for route compatibility, but will use from API if available
    experienceId: String,
    preferenceManager: com.simats.interviewassist.utils.PreferenceManager,
    onNavigateToProfile: (Int) -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var experience by remember { mutableStateOf<InterviewExperienceResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val expIdInt = experienceId.toIntOrNull() ?: 0
    val reportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showReportSheet by remember { mutableStateOf(false) }

    LaunchedEffect(expIdInt) {
        if (expIdInt == 0) {
            errorMessage = "Invalid experience ID"
            return@LaunchedEffect
        }
        isLoading = true
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getExperienceDetail(expIdInt)
            }
            if (response.isSuccessful) {
                experience = response.body()
            } else {
                errorMessage = "Failed to load experience details"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    val scrollState = rememberScrollState()
    
    // Global Notification State
    var showGlobalNotification by remember { mutableStateOf(false) }
    var globalNotificationMessage by remember { mutableStateOf("") }

    val showNotification: (String) -> Unit = { message ->
        globalNotificationMessage = message
        showGlobalNotification = true
        scope.launch {
            kotlinx.coroutines.delay(2000)
            showGlobalNotification = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Interview Experience",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle,
                        modifier = Modifier.padding(start = 20.dp)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .background(LocalAppColors.current.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp), tint = LocalAppColors.current.textTitle)
                    }
                },
                actions = {
                    val isSaved = experience?.isSaved ?: false
                    var isSaving by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { 
                            if (!isSaving && experience != null) {
                                isSaving = true
                                scope.launch {
                                    try {
                                        val response = RetrofitClient.apiService.toggleSave(experience!!.id)
                                        if (response.isSuccessful) {
                                            val data = response.body()
                                            if (data != null) {
                                                experience = experience?.copy(isSaved = data.isSaved)
                                                showNotification(if (data.isSaved) "Experience saved" else "Removed from saved")
                                            }
                                        } else {
                                            showNotification("Failed to save")
                                        }
                                    } catch (e: Exception) {
                                        showNotification("Error: ${e.message}")
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            }
                        },
                        enabled = !isSaving
                    ) {
                        Icon(
                            if (isSaved) Icons.Filled.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) LocalAppColors.current.primary else LocalAppColors.current.iconTint
                        )
                    }
                    IconButton(onClick = { /* Share action */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = LocalAppColors.current.iconTint)
                    }

                    var showMenu by remember { mutableStateOf(false) }
                    
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = LocalAppColors.current.iconTint)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Report Experience") },
                                leadingIcon = { Icon(Icons.Default.Flag, null, tint = LocalAppColors.current.error) },
                                onClick = {
                                    showMenu = false
                                    showReportSheet = true
                                }
                            )
                        }
                    }

                    if (showReportSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showReportSheet = false },
                            sheetState = reportSheetState,
                            containerColor = LocalAppColors.current.surface,
                            dragHandle = null
                        ) {
                            ReportSheet(
                                title = "Flag Experience",
                                onClose = {
                                    scope.launch { reportSheetState.hide() }.invokeOnCompletion {
                                        showReportSheet = false
                                    }
                                },
                                onSubmit = { reason ->
                                    scope.launch {
                                        try {
                                            val response = RetrofitClient.apiService.reportExperience(expIdInt, mapOf("reason" to reason))
                                            if (response.isSuccessful) {
                                                showNotification("Experience reported successfully")
                                            } else {
                                                showNotification("Failed to submit report")
                                            }
                                        } catch (e: Exception) {
                                            showNotification("Error: ${e.message}")
                                        } finally {
                                            reportSheetState.hide()
                                            showReportSheet = false
                                        }
                                    }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LocalAppColors.current.surface,
                    scrolledContainerColor = LocalAppColors.current.surface
                )
            )
        },
        containerColor = LocalAppColors.current.surface
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LocalAppColors.current.primary)
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(errorMessage ?: "Error", color = LocalAppColors.current.error)
            }
        } else if (experience != null) {
            val exp = experience!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = LocalAppColors.current.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = LocalAppColors.current.primaryHighlight,
                            border = BorderStroke(2.dp, LocalAppColors.current.primary.copy(alpha = 0.1f))
                        ) {
                            val profilePic = exp.userProfilePic
                            if (!profilePic.isNullOrEmpty()) {
                                val imageModel: Any? = when {
                                    profilePic.startsWith("http") -> profilePic
                                    profilePic.startsWith("/data/user/") -> null
                                    profilePic.length > 1000 -> {
                                        val cleanBase64 = if (profilePic.contains(",")) profilePic.substringAfter(",") else profilePic
                                        try { android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT) } catch (e: Exception) { profilePic }
                                    }
                                    else -> "${RetrofitClient.BASE_URL.removeSuffix("/")}/${profilePic.removePrefix("/")}"
                                }
                                AsyncImage(
                                    model = imageModel,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                val initials = if (exp.userName.isNotBlank()) {
                                    val parts = exp.userName.trim().split(" ")
                                    if (parts.size >= 2) "${parts[0][0]}${parts.last()[0]}"
                                    else parts[0].take(2).uppercase()
                                } else "U"
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = initials.uppercase(),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = LocalAppColors.current.primary
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = exp.userName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = LocalAppColors.current.textTitle
                                )
                                if (exp.isUserVerified) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        modifier = Modifier.size(16.dp),
                                        tint = LocalAppColors.current.primary
                                    )
                                }
                            }
                            
                            Text(
                                text = "${exp.userRole} @ $companyName",
                                style = MaterialTheme.typography.bodySmall,
                                color = LocalAppColors.current.textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Badges Row
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val difficultyColor = when(exp.difficulty) {
                        "Easy" -> LocalAppColors.current.success
                        "Medium" -> LocalAppColors.current.warning
                        "Hard" -> LocalAppColors.current.error
                        else -> LocalAppColors.current.textSecondary
                    }
                    val difficultyBg = when(exp.difficulty) {
                        "Easy" -> LocalAppColors.current.successBg
                        "Medium" -> LocalAppColors.current.warningBg
                        "Hard" -> LocalAppColors.current.errorBg
                        else -> LocalAppColors.current.surfaceVariant
                    }
                    
                    Surface(color = difficultyBg, shape = RoundedCornerShape(10.dp)) {
                        Text(
                            text = exp.difficulty,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = difficultyColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    
                    exp.workMode?.let {
                        Surface(color = LocalAppColors.current.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelMedium,
                                color = LocalAppColors.current.textSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    
                    if (exp.isSelected) {
                        Surface(color = LocalAppColors.current.successBg, shape = RoundedCornerShape(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Verified, null, modifier = Modifier.size(14.dp), tint = LocalAppColors.current.success)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Selected", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = LocalAppColors.current.success)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    
                     ExperienceSectionCard(
                        title = "About the Author",
                        icon = Icons.Default.Person,
                        containerColor = LocalAppColors.current.primary.copy(alpha = 0.15f),
                        contentColor = LocalAppColors.current.primary,
                        border = BorderStroke(1.dp, LocalAppColors.current.primary.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = exp.brief ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = LocalAppColors.current.textBody,
                            lineHeight = 24.sp
                        )
                    }

                    // How I got the interview
                    exp.applicationProcess?.let {
                        ExperienceSectionCard(
                            title = "How I got the interview",
                            icon = Icons.Default.Bolt,
                            containerColor = LocalAppColors.current.warning.copy(alpha = 0.15f),
                            contentColor = LocalAppColors.current.warning,
                            border = BorderStroke(1.dp, LocalAppColors.current.warning.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyLarge,
                                color = LocalAppColors.current.textBody,
                                lineHeight = 24.sp
                            )
                        }
                    }

                    // Interview Process Breakdown
                    if (!exp.interviewRounds.isNullOrEmpty()) {
                        ExperienceSectionCard(
                            title = "Interview Process Breakdown",
                            icon = Icons.Default.Timeline,
                            containerColor = LocalAppColors.current.primary.copy(alpha = 0.15f),
                            contentColor = LocalAppColors.current.primary,
                            border = BorderStroke(1.dp, LocalAppColors.current.primary.copy(alpha = 0.3f))
                        ) {
                            exp.interviewRounds.forEach { round ->
                                Row(modifier = Modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.Top) {
                                    Text(
                                        "• ",
                                        fontWeight = FontWeight.Bold,
                                        color = LocalAppColors.current.primary,
                                        fontSize = 18.sp
                                    )
                                    Column {
                                        Text(
                                            text = round.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = LocalAppColors.current.textTitle
                                        )
                                        Text(
                                            text = round.duration,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LocalAppColors.current.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // My Experience (Detailed)
                    ExperienceSectionCard(
                        title = "My Experience (Detailed)",
                        icon = Icons.Default.Article,
                        containerColor = LocalAppColors.current.surfaceVariant.copy(alpha = 0.7f),
                        contentColor = LocalAppColors.current.textTitle,
                        border = BorderStroke(1.dp, LocalAppColors.current.divider)
                    ) {
                        Text(
                            text = exp.myExperience ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = LocalAppColors.current.textBody,
                            lineHeight = 24.sp
                        )
                    }

                    // Questions Asked
                    if (!exp.technicalQuestions.isNullOrEmpty() || !exp.behavioralQuestions.isNullOrEmpty()) {
                        ExperienceSectionCard(
                            title = "Questions Asked",
                            icon = Icons.Default.HelpOutline,
                            containerColor = LocalAppColors.current.primary.copy(alpha = 0.15f),
                            contentColor = LocalAppColors.current.primary,
                            border = BorderStroke(1.dp, LocalAppColors.current.primary.copy(alpha = 0.3f))
                        ) {
                            if (!exp.technicalQuestions.isNullOrEmpty()) {
                                Text(
                                    "TECHNICAL QUESTIONS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = LocalAppColors.current.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                exp.technicalQuestions.forEach {
                                    Text("• $it", style = MaterialTheme.typography.bodyMedium, color = LocalAppColors.current.textBody, modifier = Modifier.padding(bottom = 6.dp))
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            
                            if (!exp.behavioralQuestions.isNullOrEmpty()) {
                                Text(
                                    "BEHAVIORAL QUESTIONS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = LocalAppColors.current.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                exp.behavioralQuestions.forEach {
                                    Text("• $it", style = MaterialTheme.typography.bodyMedium, color = LocalAppColors.current.textBody, modifier = Modifier.padding(bottom = 6.dp))
                                }
                            }
                        }
                    }

                    // Mistakes Made
                    if (!exp.mistakes.isNullOrEmpty()) {
                        ExperienceSectionCard(
                            title = "Mistakes I Made",
                            icon = Icons.Default.Warning,
                            containerColor = LocalAppColors.current.error.copy(alpha = 0.15f),
                            contentColor = LocalAppColors.current.error,
                            border = BorderStroke(1.dp, LocalAppColors.current.error.copy(alpha = 0.3f))
                        ) {
                            exp.mistakes.forEachIndexed { index, mistake ->
                                 Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.Top) {
                                    Text("${index + 1}. ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = LocalAppColors.current.error)
                                    Text(mistake, style = MaterialTheme.typography.bodyMedium, color = LocalAppColors.current.textBody, lineHeight = 22.sp)
                                }
                            }
                        }
                    }

                    // Preparation Strategy
                    if (!exp.preparationStrategy.isNullOrEmpty()) {
                        ExperienceSectionCard(
                            title = "Preparation Strategy",
                            icon = Icons.Default.School,
                            containerColor = LocalAppColors.current.success.copy(alpha = 0.15f),
                            contentColor = LocalAppColors.current.success,
                            border = BorderStroke(1.dp, LocalAppColors.current.success.copy(alpha = 0.3f))
                        ) {
                            exp.preparationStrategy.forEach { (category, tools) ->
                                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                    Text(category.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = LocalAppColors.current.success)
                                    tools.forEach { tool ->
                                        Text("• $tool", style = MaterialTheme.typography.bodyMedium, color = LocalAppColors.current.textBody, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Final Advice
                    if (!exp.finalAdvice.isNullOrEmpty()) {
                        ExperienceSectionCard(
                            title = "Final Advice & Conclusion",
                            icon = Icons.Default.VolunteerActivism,
                            containerColor = LocalAppColors.current.primaryHighlight,
                            contentColor = LocalAppColors.current.primary,
                            border = BorderStroke(1.dp, LocalAppColors.current.primary.copy(alpha = 0.5f))
                        ) {
                            exp.finalAdvice.forEachIndexed { index, advice ->
                                Row(modifier = Modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.Top) {
                                    Text("${index + 1}. ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = LocalAppColors.current.primary)
                                    Text(advice, style = MaterialTheme.typography.bodyMedium, color = LocalAppColors.current.textBody, lineHeight = 22.sp)
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Bottom Action Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = LocalAppColors.current.surface,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LocalAppColors.current.divider)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Was this helpful?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
                            Text("Help fellow students by rating it", style = MaterialTheme.typography.bodySmall, color = LocalAppColors.current.textSecondary)
                        }
                        val isHelpful = exp.isHelpful
                        var isProcessing by remember { mutableStateOf(false) }
                        Button(
                            onClick = { 
                                if (!isProcessing) {
                                    isProcessing = true
                                    scope.launch {
                                        try {
                                            val response = RetrofitClient.apiService.toggleHelpful(exp.id)
                                            if (response.isSuccessful) {
                                                val data = response.body()
                                                if (data != null) {
                                                    experience = experience?.copy(
                                                        isHelpful = data.isHelpful,
                                                        helpfulCount = data.helpfulCount
                                                    )
                                                    showNotification(if (data.isHelpful) "Marked as helpful" else "Removed from helpful")
                                                }
                                            } else {
                                                showNotification("Failed to update")
                                            }
                                        } catch (e: Exception) {
                                            showNotification("Error: ${e.message}")
                                        } finally {
                                            isProcessing = false
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isHelpful) LocalAppColors.current.primary else LocalAppColors.current.primaryHighlight,
                                contentColor = if (isHelpful) LocalAppColors.current.surface else LocalAppColors.current.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(
                                if (isHelpful) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt, 
                                null, 
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isHelpful) "Helpful" else "Yes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Floating Notification Popup (Premium Style)
    androidx.compose.animation.AnimatedVisibility(
        visible = showGlobalNotification,
        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(initialOffsetY = { -it }),
        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }),
        modifier = Modifier.align(Alignment.TopCenter)
    ) {
        Surface(
            color = LocalAppColors.current.textTitle.copy(alpha = 0.9f),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(top = 48.dp),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (globalNotificationMessage.contains("helpful")) Icons.Default.ThumbUp else Icons.Default.Info,
                    contentDescription = null,
                    tint = LocalAppColors.current.surface,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = globalNotificationMessage,
                    color = LocalAppColors.current.surface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    }
}

