package com.simats.interviewassist.ui.screens.alumni

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.utils.PreferenceManager
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.data.network.AssistQuestionResponse
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import kotlinx.coroutines.launch
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import java.io.File
import com.simats.interviewassist.ui.screens.student.ReportSheet


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniAssistScreen(
    preferenceManager: PreferenceManager,
    onNavigateToHome: () -> Unit,
    onNavigateToPosts: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToShareExperience: () -> Unit
) {
    var questions by remember { mutableStateOf<List<AssistQuestionResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var answeringQuestion by remember { mutableStateOf<AssistQuestionResponse?>(null) }
    var reportingQuestion by remember { mutableStateOf<AssistQuestionResponse?>(null) }
    var reportingAnswer by remember { mutableStateOf<com.simats.interviewassist.data.network.QuestionAnswerResponse?>(null) }
    var showAnswerSuccess by remember { mutableStateOf(false) }
    
    // Premium Notification State
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
    
    val answerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val reportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var isDeleting by remember { mutableIntStateOf(-1) }
    val context = androidx.compose.ui.platform.LocalContext.current
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

    fun fetchQuestions() {
        isLoading = true
        scope.launch {
            try {
                val response = RetrofitClient.apiService.getAssistQuestions()
                if (response.isSuccessful) {
                    questions = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                fetchNotificationsCount()
                fetchQuestions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        icon = { Icon(Icons.Default.Handshake, contentDescription = "Assist", modifier = Modifier.size(26.dp)) },
                        label = { Text("Assist", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        selected = true,
                        onClick = { },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LocalAppColors.current.primary,
                            selectedTextColor = LocalAppColors.current.primary,
                            indicatorColor = LocalAppColors.current.primaryHighlight
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(LocalAppColors.current.primaryHighlight, CircleShape)
                                .clip(CircleShape)
                                .clickable { onNavigateToProfile() }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(shape = CircleShape, modifier = Modifier.fillMaxSize()) {
                                val profilePicPath = preferenceManager.getProfilePicPath()
                                val initials = if (preferenceManager.getUserName().isNotEmpty()) 
                                    preferenceManager.getUserName().take(1).uppercase() else "A"
                                val fallback: @Composable () -> Unit = {
                                    Box(modifier = Modifier.fillMaxSize().background(LocalAppColors.current.primaryHighlight), contentAlignment = Alignment.Center) {
                                        Text(initials, style = MaterialTheme.typography.titleMedium, color = LocalAppColors.current.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
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
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        loading = { fallback() },
                                        error = { fallback() }
                                    )
                                } else {
                                    fallback()
                                }
                            }

                        }
                    }
                }
            }
            
            if (isLoading && questions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LocalAppColors.current.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    item {
                        Column(modifier = Modifier.padding(vertical = 12.dp)) {
                            Text(
                                "Help Students",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = LocalAppColors.current.textTitle
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Answer questions from juniors in companies you've worked at.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = LocalAppColors.current.textSecondary,
                                lineHeight = 24.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (questions.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                Text("No questions from your companies yet.", color = LocalAppColors.current.textSecondary)
                            }
                        }
                    }

                    items(questions) { question ->
                        AssistQuestionCard(
                            question = question,
                            isOwner = question.userId == preferenceManager.getUserId(),
                            isDeleting = isDeleting == question.id,
                            onDelete = {
                                isDeleting = question.id
                                scope.launch {
                                    try {
                                        val response = RetrofitClient.apiService.deleteQuestion(question.id)
                                        if (response.isSuccessful) {
                                            fetchQuestions()
                                        }
                                    } catch (e: Exception) {
                                        // Silent error
                                    } finally {
                                        isDeleting = -1
                                    }
                                }
                            },
                            onAnswerClick = { answeringQuestion = it },
                            onReportClick = { reportingQuestion = question },
                            onReportAnswerClick = { reportingAnswer = it }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }

        // Write Answer Bottom Sheet
        if (answeringQuestion != null) {
            ModalBottomSheet(
                onDismissRequest = { answeringQuestion = null },
                sheetState = answerSheetState,
                containerColor = LocalAppColors.current.surface,
                dragHandle = null
            ) {
                WriteAnswerSheet(
                    question = answeringQuestion!!,
                    onClose = {
                        scope.launch { answerSheetState.hide() }.invokeOnCompletion {
                            answeringQuestion = null
                        }
                    },
                    onPost = { answer ->
                        scope.launch {
                            try {
                                val roleAtCompany = if (preferenceManager.designationState.value.isNotBlank() && preferenceManager.currentCompanyState.value.isNotBlank()) {
                                    "${preferenceManager.designationState.value} @ ${preferenceManager.currentCompanyState.value}"
                                } else {
                                    preferenceManager.designationState.value.ifBlank { "Alumni" }
                                }
                                val response = RetrofitClient.apiService.answerQuestion(
                                    answeringQuestion!!.id,
                                    mapOf(
                                        "answer_text" to answer,
                                        "answerer_role" to roleAtCompany
                                    )
                                )
                                if (response.isSuccessful) {
                                    scope.launch { answerSheetState.hide() }.invokeOnCompletion {
                                        answeringQuestion = null
                                        android.widget.Toast.makeText(context, "Answer posted successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                        showAnswerSuccess = true
                                        fetchQuestions() // Refresh list
                                    }
                                }
                            } catch (e: Exception) { }
                        }
                    }
                )
            }
        }

        if (reportingQuestion != null || reportingAnswer != null) {
            val isReportingQuestion = reportingQuestion != null
            ModalBottomSheet(
                onDismissRequest = { 
                    reportingQuestion = null
                    reportingAnswer = null
                },
                sheetState = reportSheetState,
                containerColor = LocalAppColors.current.surface,
                dragHandle = null
            ) {
                ReportSheet(
                    title = if (isReportingQuestion) "Flag Question" else "Flag Answer",
                    onClose = {
                        scope.launch { reportSheetState.hide() }.invokeOnCompletion {
                            reportingQuestion = null
                            reportingAnswer = null
                        }
                    },
                    onSubmit = { reason ->
                        scope.launch {
                            try {
                                val response = if (isReportingQuestion) {
                                    RetrofitClient.apiService.reportQuestion(
                                        reportingQuestion!!.id,
                                        mapOf("reason" to reason)
                                    )
                                } else {
                                    RetrofitClient.apiService.reportAnswer(
                                        reportingAnswer!!.id,
                                        mapOf("reason" to reason)
                                    )
                                }
                                if (response.isSuccessful) {
                                    showNotification(if (isReportingQuestion) "Question reported" else "Answer reported")
                                } else {
                                    showNotification("Failed to report content")
                                }
                            } catch (e: Exception) {
                                showNotification("Error: ${e.message}")
                            } finally {
                                reportSheetState.hide()
                                reportingQuestion = null
                                reportingAnswer = null
                            }
                        }
                    }
                )
            }
        }

        // Success Dialog
        if (showAnswerSuccess) {
            AlertDialog(
                onDismissRequest = { showAnswerSuccess = false },
                confirmButton = {
                    TextButton(onClick = { showAnswerSuccess = false }) {
                        Text("OK", color = LocalAppColors.current.primary, fontWeight = FontWeight.Bold)
                    }
                },
                title = { Text("Success!", fontWeight = FontWeight.Bold) },
                text = { Text("Your answer has been submitted successfully.") },
                shape = RoundedCornerShape(24.dp),
                containerColor = LocalAppColors.current.surface
            )
        }

        // Premium Floating Notification
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
                        imageVector = if (globalNotificationMessage.contains("reported")) Icons.Default.Flag else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = globalNotificationMessage,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        }
    }
}

@Composable
fun AssistQuestionCard(
    question: AssistQuestionResponse,
    isOwner: Boolean = false,
    isDeleting: Boolean = false,
    onDelete: () -> Unit = {},
    onAnswerClick: (AssistQuestionResponse) -> Unit,
    onReportClick: () -> Unit,
    onReportAnswerClick: (com.simats.interviewassist.data.network.QuestionAnswerResponse) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = LocalAppColors.current.surface,
        border = BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.5f)),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
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
                        Box(contentAlignment = Alignment.Center) {
                            val initials = if (question.askedBy.isNotEmpty()) {
                                val parts = question.askedBy.split(" ")
                                if (parts.size >= 2) parts[0].take(1) + parts[1].take(1)
                                else question.askedBy.take(2)
                            } else "U"
                            Text(initials.uppercase(), color = LocalAppColors.current.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(question.askedBy, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.HomeWork, null, modifier = Modifier.size(12.dp), tint = LocalAppColors.current.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(question.companyName, style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.primary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(12.dp), tint = LocalAppColors.current.textSecondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(question.date, style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.textSecondary)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isOwner) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = LocalAppColors.current.error
                            )
                        } else {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.background(LocalAppColors.current.background, CircleShape).size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, null, tint = LocalAppColors.current.error, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(
                        onClick = onReportClick,
                        modifier = Modifier.background(LocalAppColors.current.background, CircleShape).size(32.dp)
                    ) {
                        Icon(Icons.Default.OutlinedFlag, null, tint = LocalAppColors.current.textSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                color = LocalAppColors.current.background, // Light gray/blue box for question
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    question.questionText,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = LocalAppColors.current.textTitle,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { expanded = !expanded }.padding(vertical = 8.dp)
                ) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ChatBubbleOutline, 
                        null, 
                        modifier = Modifier.size(18.dp), 
                        tint = LocalAppColors.current.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val answerCount = question.answers.size
                    Text(
                        if (answerCount == 1) "1 Answer" else "$answerCount Answers",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = LocalAppColors.current.primary
                    )
                }

                Button(
                    onClick = { onAnswerClick(question) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.BorderColor, null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Answer this", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            if (expanded && question.answers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                question.answers.forEach { answer ->
                    AlumniAnswerSheetItem(answer, onReport = { onReportAnswerClick(answer) })
                }
            }
        }
    }
}

@Composable
fun AlumniAnswerSheetItem(
    answer: com.simats.interviewassist.data.network.QuestionAnswerResponse,
    onReport: () -> Unit
) {
    Surface(
        color = LocalAppColors.current.background,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(16.dp), tint = LocalAppColors.current.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(answer.answererName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
                IconButton(onClick = onReport, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.OutlinedFlag, null, tint = LocalAppColors.current.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(answer.answererRole, style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.textSecondary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(answer.answerText, style = MaterialTheme.typography.bodyMedium, color = LocalAppColors.current.textTitle)
            Spacer(modifier = Modifier.height(8.dp))
            Text(answer.date, style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.iconTint, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }
    }
}

@Composable
fun WriteAnswerSheet(
    question: AssistQuestionResponse,
    onClose: () -> Unit,
    onPost: (String) -> Unit
) {
    var answerText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding() 
            .imePadding() 
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Your Expert Answer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
            IconButton(onClick = onClose, modifier = Modifier.background(LocalAppColors.current.background, CircleShape).size(36.dp)) {
                Icon(Icons.Default.Close, null, tint = LocalAppColors.current.textTitle, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Question Context Box
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = LocalAppColors.current.background,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QuestionAnswer, null, modifier = Modifier.size(14.dp), tint = LocalAppColors.current.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Question from ${question.askedBy} about ${question.companyName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalAppColors.current.textSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    question.questionText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = LocalAppColors.current.textTitle,
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            color = LocalAppColors.current.surface,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.5f))
        ) {
            TextField(
                value = answerText,
                onValueChange = { answerText = it },
                placeholder = { 
                    Text(
                        "Type your response here... Be detailed and helpful!", 
                        color = LocalAppColors.current.textSecondary.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyLarge
                    ) 
                },
                modifier = Modifier.fillMaxSize(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = LocalAppColors.current.primary
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = LocalAppColors.current.textTitle)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onPost(answerText) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp), // Thicker button
            colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
            shape = RoundedCornerShape(16.dp),
            enabled = answerText.trim().isNotEmpty()
        ) {
            Text("Post Answer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}


val dummyAssistQuestions = emptyList<AssistQuestionResponse>()
