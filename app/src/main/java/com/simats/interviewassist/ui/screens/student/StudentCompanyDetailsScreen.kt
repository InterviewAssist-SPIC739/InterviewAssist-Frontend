package com.simats.interviewassist.ui.screens.student

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import com.simats.interviewassist.data.models.CompanyResponse
import com.simats.interviewassist.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import coil.compose.AsyncImage
import com.simats.interviewassist.data.models.InterviewExperienceResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCompanyDetailsScreen(
    companyId: Int,
    companyName: String,
    onBack: () -> Unit,
    onExperienceClick: (String) -> Unit,
    onNavigateToProfile: (Int) -> Unit = {},
    onNavigateToAskQuestion: (String) -> Unit = {},
    preferenceManager: com.simats.interviewassist.utils.PreferenceManager? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var company by remember { mutableStateOf<CompanyResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var reportingExperience by remember { mutableStateOf<com.simats.interviewassist.data.models.InterviewExperienceResponse?>(null) }
    var reportingQuestion by remember { mutableStateOf<com.simats.interviewassist.data.models.QuestionResponse?>(null) }
    var reportingAnswer by remember { mutableStateOf<com.simats.interviewassist.data.models.AnswerResponse?>(null) }

    var refreshTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(companyId, refreshTrigger) {
        isLoading = true
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getCompanyDetail(companyId)
            }
            if (response.isSuccessful) {
                company = response.body()
                errorMessage = null
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
    
    var showAskQuestionSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val reportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    if (showAskQuestionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAskQuestionSheet = false },
            sheetState = sheetState,
            containerColor = LocalAppColors.current.surface
        ) {
            var isPosting by remember { mutableStateOf(false) }
            AskQuestionSheet(
                companyName = companyName,
                onDismiss = { if (!isPosting) showAskQuestionSheet = false },
                onPostQuestion = { question ->
                    isPosting = true
                    scope.launch {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                RetrofitClient.apiService.askQuestion(
                                    companyId,
                                    mapOf("question_text" to question)
                                )
                            }
                            if (response.isSuccessful) {
                                showAskQuestionSheet = false
                                showNotification("Question posted successfully")
                                refreshTrigger++
                            } else {
                                showNotification("Failed to post question")
                            }
                        } catch (e: Exception) {
                            showNotification("Error: ${e.message}")
                        } finally {
                            isPosting = false
                        }
                    }
                }
            )
        }
    }

    if (reportingExperience != null || reportingQuestion != null || reportingAnswer != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                reportingExperience = null
                reportingQuestion = null
                reportingAnswer = null
            },
            sheetState = reportSheetState,
            containerColor = LocalAppColors.current.surface,
            dragHandle = null
        ) {
            ReportSheet(
                title = when {
                    reportingExperience != null -> "Flag Experience"
                    reportingQuestion != null -> "Flag Question"
                    else -> "Flag Answer"
                },
                onClose = {
                    scope.launch { reportSheetState.hide() }.invokeOnCompletion {
                        reportingExperience = null
                        reportingQuestion = null
                        reportingAnswer = null
                    }
                },
                onSubmit = { reason ->
                    scope.launch {
                        try {
                            val response = when {
                                reportingExperience != null -> RetrofitClient.apiService.reportExperience(reportingExperience!!.id, mapOf("reason" to reason))
                                reportingQuestion != null -> RetrofitClient.apiService.reportQuestion(reportingQuestion!!.id, mapOf("reason" to reason))
                                else -> RetrofitClient.apiService.reportAnswer(reportingAnswer!!.id, mapOf("reason" to reason))
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
                            reportingAnswer = null
                        }
                    }
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {},
            containerColor = LocalAppColors.current.surface
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
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

                    // Logo
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
                        Text(company?.location ?: "Loading...", style = MaterialTheme.typography.bodyMedium, color = LocalAppColors.current.textSecondary)
                        Spacer(modifier = Modifier.width(24.dp))
                        Icon(Icons.Default.Public, null, modifier = Modifier.size(16.dp), tint = LocalAppColors.current.iconTint)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(company?.sector ?: "", style = MaterialTheme.typography.bodyMedium, color = LocalAppColors.current.textSecondary)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = company?.description ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = LocalAppColors.current.textBody,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatBox(
                            value = "${company?.experiencesCount ?: 0}",
                            label = "Experiences",
                            bgColor = LocalAppColors.current.primaryHighlight,
                            textColor = LocalAppColors.current.primary,
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

                    Spacer(modifier = Modifier.height(28.dp))

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
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, LocalAppColors.current.divider),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LocalAppColors.current.textTitle)
                        ) {
                            Icon(Icons.Default.Language, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Website", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
                            modifier = Modifier.weight(1f).height(54.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) LocalAppColors.current.divider else LocalAppColors.current.primary,
                                contentColor = if (isFollowing) LocalAppColors.current.textTitle else Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(if (isFollowing) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isFollowing) "Following" else "Follow", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
                                                0 -> Icons.AutoMirrored.Filled.MenuBook
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
                        when (selectedTab) {
                            0 -> company?.let { OverviewTab(it) }
                            1 -> company?.let { 
                                ExperiencesTab(
                                    company = it, 
                                    preferenceManager = preferenceManager!!,
                                    onExperienceClick = onExperienceClick,
                                    onNavigateToProfile = null,
                                    onShowNotification = showNotification,
                                    onReportExperience = { exp -> reportingExperience = exp }
                                ) 
                            }
                            2 -> company?.let { 
                                QuestionsTab(
                                    it, 
                                    preferenceManager!!,
                                    onAskQuestion = { showAskQuestionSheet = true },
                                    onRefresh = { refreshTrigger++ },
                                    onReportQuestion = { q -> reportingQuestion = q },
                                    onReportAnswer = { a -> reportingAnswer = a }
                                ) 
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        // Notification Popup
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
