package com.simats.interviewassist.ui.screens.alumni

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.data.models.CompanyResponse
import com.simats.interviewassist.data.models.QuestionResponse
import com.simats.interviewassist.data.models.AnswerResponse
import com.simats.interviewassist.data.models.InterviewExperienceResponse
import com.simats.interviewassist.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.simats.interviewassist.ui.screens.student.StatBox
import com.simats.interviewassist.ui.screens.student.OverviewTab
import com.simats.interviewassist.ui.screens.student.ExperiencesTab
import com.simats.interviewassist.utils.PreferenceManager
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniCompanyDetailsScreen(
    companyId: Int,
    companyName: String,
    onBack: () -> Unit,
    onExperienceClick: (String) -> Unit,
    preferenceManager: PreferenceManager,
    onNavigateToProfile: (Int) -> Unit = {},
    onNavigateToAskQuestion: (String) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var company by remember { mutableStateOf<CompanyResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var reportingExperience by remember { mutableStateOf<InterviewExperienceResponse?>(null) }
    var reportingQuestion by remember { mutableStateOf<QuestionResponse?>(null) }
    var reportingAnswerId by remember { mutableStateOf<Int?>(null) }

    val reportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var refreshTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(companyId, refreshTrigger) {
        isLoading = true
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getCompanyDetail(companyId)
            }
            if (response.isSuccessful) {
                company = response.body()
            } else {
                errorMessage = "Failed to load company details"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Experiences", "Q&A")

    // Global Notification State
    var showGlobalNotification by remember { mutableStateOf(false) }
    var globalNotificationMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val showNotification: (String) -> Unit = { message ->
        globalNotificationMessage = message
        showGlobalNotification = true
        scope.launch {
            kotlinx.coroutines.delay(2000)
            showGlobalNotification = false
        }
    }

    if (reportingExperience != null || reportingQuestion != null || reportingAnswerId != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                reportingExperience = null
                reportingQuestion = null
                reportingAnswerId = null
            },
            sheetState = reportSheetState,
            containerColor = LocalAppColors.current.surface,
            dragHandle = null
        ) {
            val title = when {
                reportingExperience != null -> "Flag Experience"
                reportingQuestion != null -> "Flag Question"
                else -> "Flag Answer"
            }
            com.simats.interviewassist.ui.screens.student.ReportSheet(
                title = title,
                onClose = {
                    scope.launch { reportSheetState.hide() }.invokeOnCompletion {
                        reportingExperience = null
                        reportingQuestion = null
                        reportingAnswerId = null
                    }
                },
                onSubmit = { reason ->
                    scope.launch {
                        try {
                            val response = when {
                                reportingExperience != null -> RetrofitClient.apiService.reportExperience(reportingExperience!!.id, mapOf("reason" to reason))
                                reportingQuestion != null -> RetrofitClient.apiService.reportQuestion(reportingQuestion!!.id, mapOf("reason" to reason))
                                else -> RetrofitClient.apiService.reportAnswer(reportingAnswerId!!, mapOf("reason" to reason))
                            }
                            
                            if (response.isSuccessful) {
                                showNotification("Report submitted successfully")
                            } else {
                                showNotification("Failed to submit report")
                            }
                        } catch (e: Exception) {
                            showNotification("Error: ${e.message}")
                        } finally {
                            reportSheetState.hide()
                            reportingExperience = null
                            reportingQuestion = null
                            reportingAnswerId = null
                        }
                    }
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                // No standard TopAppBar, we'll use a custom one with overlay
            },
            containerColor = LocalAppColors.current.surface
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    // Banner Background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFE0E7FF), LocalAppColors.current.surface)
                                )
                            )
                    )

                    // Back Button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 16.dp, top = 16.dp)
                            .background(LocalAppColors.current.surface, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = LocalAppColors.current.textTitle)
                    }

                    if (company != null) {
                        Surface(
                            modifier = Modifier
                                .padding(start = 24.dp)
                                .align(Alignment.BottomStart)
                                .size(90.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = LocalAppColors.current.surface,
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (!company!!.logo.isNullOrEmpty()) {
                                    val imageBytes = Base64.decode(company!!.logo, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = company!!.name,
                                            modifier = Modifier.size(60.dp)
                                        )
                                    }
                                } else if (!company!!.logoUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = company!!.logoUrl,
                                        contentDescription = company!!.name,
                                        modifier = Modifier.size(60.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.BusinessCenter,
                                        contentDescription = company!!.name,
                                        modifier = Modifier.size(50.dp),
                                        tint = LocalAppColors.current.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Company Name & Info
                    Text(
                        text = company?.name ?: companyName,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = LocalAppColors.current.iconTint)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(company?.location ?: "Loading...", fontSize = 14.sp, color = LocalAppColors.current.textSecondary)
                        Spacer(modifier = Modifier.width(24.dp))
                        Icon(Icons.Default.Public, null, modifier = Modifier.size(16.dp), tint = LocalAppColors.current.iconTint)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(company?.sector ?: "", fontSize = 14.sp, color = LocalAppColors.current.textSecondary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = company?.description ?: "",
                        fontSize = 15.sp,
                        color = LocalAppColors.current.textSecondary,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatBox(
                            value = "${company?.experiencesCount ?: 0}",
                            label = "Experiences",
                            bgColor = Color(0xFFEEF2FF),
                            textColor = Color(0xFF4F46E5),
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            value = "${company?.selectedCount ?: 0}",
                            label = "Selected",
                            bgColor = LocalAppColors.current.successBg,
                            textColor = LocalAppColors.current.success,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            value = company?.difficulty ?: "N/A",
                            label = "Difficulty",
                            bgColor = LocalAppColors.current.warningBg,
                            textColor = LocalAppColors.current.warning,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { 
                                company?.websiteUrl?.let { url ->
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(
                                        if (url.startsWith("http")) url else "https://$url"
                                    ))
                                    context.startActivity(intent)
                                } ?: showNotification("Website not available")
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                             border = androidx.compose.foundation.BorderStroke(1.dp, LocalAppColors.current.primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LocalAppColors.current.primary)
                        ) {
                            Text("Visit Website", fontWeight = FontWeight.Bold)
                        }
                        
                        val isFollowing = company?.isFollowing ?: false
                        Button(
                            onClick = { 
                                scope.launch {
                                    try {
                                        val response = withContext(Dispatchers.IO) {
                                            RetrofitClient.apiService.toggleFollowCompany(companyId)
                                        }
                                        if (response.isSuccessful) {
                                            val newStatus = response.body()?.isFollowing ?: !isFollowing
                                            company = company?.copy(isFollowing = newStatus)
                                            showNotification(if (newStatus) "Following ${company?.name}" else "Unfollowed ${company?.name}")
                                        }
                                    } catch (e: Exception) {
                                        showNotification("Error: ${e.message}")
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) LocalAppColors.current.divider else LocalAppColors.current.primary,
                                contentColor = if (isFollowing) LocalAppColors.current.textTitle else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isFollowing) "Following" else "Follow Company", fontWeight = FontWeight.Bold)
                        }
                    }


                    Spacer(modifier = Modifier.height(32.dp))

                    // Tabs
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            tabs.forEachIndexed { index, title ->
                                val isSelected = selectedTab == index
                                Column(
                                    modifier = Modifier
                                        .clickable { selectedTab = index }
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                         Icon(
                                            when(index) {
                                                0 -> Icons.Default.MenuBook
                                                1 -> Icons.Default.Groups
                                                else -> Icons.Default.ChatBubbleOutline
                                            },
                                            null,
                                            modifier = Modifier.size(18.dp),
                                            tint = if (isSelected) LocalAppColors.current.primary else LocalAppColors.current.textSecondary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = title,
                                            fontSize = 16.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) LocalAppColors.current.primary else LocalAppColors.current.textSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .width(70.dp)
                                                .height(2.dp)
                                                .background(LocalAppColors.current.primary)
                                        )
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = LocalAppColors.current.divider, thickness = 1.dp)
                    }

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = LocalAppColors.current.primary)
                        }
                    } else if (errorMessage != null) {
                        Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                            Text(errorMessage ?: "Error", color = LocalAppColors.current.error)
                        }
                    } else if (company != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        // Tab Content
                        when (selectedTab) {
                            0 -> company?.let { OverviewTab(it) }
                            1 -> company?.let { 
                                ExperiencesTab(
                                    company = it, 
                                    preferenceManager = preferenceManager, 
                                    onExperienceClick = onExperienceClick, 
                                    onNavigateToProfile = null, 
                                    showSaveOption = false, 
                                    onShowNotification = showNotification,
                                    onReportExperience = { exp -> reportingExperience = exp }
                                ) 
                            }
                            2 -> {
                                val isUserAlumniOfThisCompany = (company?.experiences?.any { 
                                    it.userId == preferenceManager.getUserId() && it.status == "approved" 
                                } ?: false) || (preferenceManager.currentCompanyState.value.equals(company?.name, ignoreCase = true))
                                
                                company?.let { 
                                    AlumniQuestionsTab(
                                        it, 
                                        preferenceManager, 
                                        canAnswer = isUserAlumniOfThisCompany, 
                                        onShowNotification = showNotification, 
                                        onRefresh = { refreshTrigger++ },
                                        onReportQuestion = { q: QuestionResponse -> reportingQuestion = q },
                                        onReportAnswer = { aId: Int -> reportingAnswerId = aId }
                                    ) 
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
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
                color = LocalAppColors.current.textTitle.copy(alpha = 0.9f), // Premium Dark
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(top = 48.dp),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (globalNotificationMessage.contains("posted")) Icons.Default.CheckCircle 
                                     else if (globalNotificationMessage.contains("saved")) Icons.Default.Bookmark
                                     else if (globalNotificationMessage.contains("reported")) Icons.Default.Flag
                                     else Icons.Default.Info,
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

@Composable
fun AlumniQuestionsTab(
    company: CompanyResponse, 
    preferenceManager: PreferenceManager,
    canAnswer: Boolean = false,
    onShowNotification: (String) -> Unit = {},
    onRefresh: () -> Unit = {},
    onReportQuestion: (QuestionResponse) -> Unit = {},
    onReportAnswer: (Int) -> Unit = {}
) {
    val questions = company.questions ?: emptyList()
    var isDeleting by remember { mutableIntStateOf(-1) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var showDeleteSuccess by remember { mutableStateOf(false) }
    var showAnswerDialog by remember { mutableStateOf(false) }
    var selectedQuestionId by remember { mutableIntStateOf(-1) }
    var answerText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    if (showDeleteSuccess) {
        AlertDialog(
            onDismissRequest = { showDeleteSuccess = false },
            confirmButton = {
                TextButton(onClick = { showDeleteSuccess = false }) {
                    Text("OK", color = LocalAppColors.current.primary, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("Deleted", fontWeight = FontWeight.Bold) },
            text = { Text("Your question has been deleted successfully.") },
            shape = RoundedCornerShape(24.dp),
            containerColor = LocalAppColors.current.surface
        )
    }

    Column {
        if (questions.isNotEmpty()) {
            questions.forEach { question ->
                val isOwner = question.userId == preferenceManager.getUserId()
                AlumniQuestionCard(
                    question = question,
                    canAnswer = canAnswer,
                    isOwner = isOwner,
                    isDeleting = isDeleting == question.id,
                    onDelete = {
                        isDeleting = question.id
                        scope.launch {
                            try {
                                val response = withContext(Dispatchers.IO) {
                                    RetrofitClient.apiService.deleteQuestion(question.id)
                                }
                                if (response.isSuccessful) {
                                    showDeleteSuccess = true
                                    onRefresh()
                                }
                            } catch (e: Exception) {
                                // Silent fail
                            } finally {
                                isDeleting = -1
                            }
                        }
                    },
                    onAnswerClick = {
                        selectedQuestionId = question.id
                        showAnswerDialog = true
                    },
                    onReportClick = { onReportQuestion(question) },
                    onReportAnswerClick = { answerId: Int -> onReportAnswer(answerId) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            Text(
                "No questions yet.",
                fontSize = 14.sp,
                color = LocalAppColors.current.textSecondary,
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                textAlign = TextAlign.Center
            )
        }


        // Verified Alumni Info Card
        Surface(
            color = LocalAppColors.current.primaryHighlight,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Lightbulb, null, tint = LocalAppColors.current.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "This section displays questions asked by students. Your insights help others succeed! Click 'Reply' to answer a question.",
                    fontSize = 14.sp,
                    color = LocalAppColors.current.primary,
                    lineHeight = 20.sp
                )
            }
        }
    }

    if (showAnswerDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showAnswerDialog = false },
            title = { Text("Answer Question", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = answerText,
                        onValueChange = { answerText = it },
                        label = { Text("Your Answer") },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        placeholder = { Text("Share your experience...") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (answerText.isNotBlank()) {
                            isSubmitting = true
                            scope.launch {
                                try {
                                        val roleAtCompany = if (preferenceManager.designationState.value.isNotBlank() && preferenceManager.currentCompanyState.value.isNotBlank()) {
                                            "${preferenceManager.designationState.value} @ ${preferenceManager.currentCompanyState.value}"
                                        } else {
                                            preferenceManager.designationState.value.ifBlank { "Alumni" }
                                        }
                                        val response = withContext(Dispatchers.IO) {
                                            RetrofitClient.apiService.answerQuestion(
                                                selectedQuestionId,
                                                mapOf(
                                                    "answer_text" to answerText,
                                                    "answerer_role" to roleAtCompany
                                                )
                                            )
                                        }
                                    if (response.isSuccessful) {
                                        android.widget.Toast.makeText(context, "Answer posted!", android.widget.Toast.LENGTH_SHORT).show()
                                        showAnswerDialog = false
                                        answerText = ""
                                        onRefresh()
                                    } else {
                                        android.widget.Toast.makeText(context, "Failed to post answer", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        }
                    },
                    enabled = !isSubmitting && answerText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary)
                ) {
                    if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    else Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAnswerDialog = false }, enabled = !isSubmitting) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AlumniQuestionCard(
    question: QuestionResponse,
    canAnswer: Boolean = false,
    isOwner: Boolean = false,
    isDeleting: Boolean = false,
    onDelete: () -> Unit = {},
    onAnswerClick: () -> Unit,
    onReportClick: () -> Unit = {},
    onReportAnswerClick: (Int) -> Unit = {}
) {
    Surface(
        color = LocalAppColors.current.surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LocalAppColors.current.divider),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        question.questionText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = LocalAppColors.current.textTitle,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Asked by ${question.askedBy} • ${question.date}",
                        fontSize = 13.sp,
                        color = LocalAppColors.current.iconTint
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isOwner) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = LocalAppColors.current.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, "Delete", tint = LocalAppColors.current.error, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(onClick = onReportClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.OutlinedFlag, "Report", tint = LocalAppColors.current.textSecondary, modifier = Modifier.size(18.dp))
                    }

                    if (canAnswer) {
                        TextButton(
                            onClick = onAnswerClick,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Reply, null, modifier = Modifier.size(16.dp), tint = LocalAppColors.current.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reply", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.primary)
                        }
                    }
                }
            }

            if (question.answers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                question.answers.forEach { answer ->
                    AlumniAnswerCard(answer, onReport = { onReportAnswerClick(answer.id) })
                }
            }
        }
    }
}

@Composable
fun AlumniAnswerCard(
    answer: AnswerResponse,
    onReport: () -> Unit = {}
) {
    Surface(
        color = LocalAppColors.current.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    color = LocalAppColors.current.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    val profilePic = answer.profilePic
                    if (!profilePic.isNullOrEmpty()) {
                        val imageModel: Any? = when {
                            profilePic.startsWith("http") -> profilePic
                            profilePic.startsWith("/data/user/") -> null
                            profilePic.length > 1000 -> {
                                val cleanBase64 = if (profilePic.contains(",")) profilePic.substringAfter(",") else profilePic
                                try { android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT) } catch (e: Exception) { profilePic }
                            }
                            else -> "${com.simats.interviewassist.data.network.RetrofitClient.BASE_URL.removeSuffix("/")}/${profilePic.removePrefix("/")}"
                        }
                        AsyncImage(
                            model = imageModel,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                if (answer.answererName.isNotBlank()) answer.answererName.first().toString() else "A",
                                fontWeight = FontWeight.Bold,
                                color = LocalAppColors.current.primary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(answer.answererName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF374151))
                        if (answer.isVerifiedAlumni) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified Alumni",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF1D9BF0) // Blue tick color
                            )
                        }
                    }
                    Text(
                        text = if (!answer.currentCompany.isNullOrBlank()) {
                            "${answer.answererRole} @ ${answer.currentCompany}"
                        } else {
                            answer.answererRole
                        }, 
                        fontSize = 12.sp, 
                        color = LocalAppColors.current.textSecondary
                    )
                }
                IconButton(onClick = onReport, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.OutlinedFlag, "Report", tint = LocalAppColors.current.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                answer.answerText,
                fontSize = 15.sp,
                color = LocalAppColors.current.textBody,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                answer.date,
                fontSize = 12.sp,
                color = LocalAppColors.current.iconTint
            )
        }
    }
}
