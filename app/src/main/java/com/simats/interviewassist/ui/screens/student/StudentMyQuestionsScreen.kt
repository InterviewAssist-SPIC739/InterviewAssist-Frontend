package com.simats.interviewassist.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.*
import kotlinx.coroutines.launch
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.data.network.AssistQuestionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMyQuestionsScreen(
    onBack: () -> Unit
) {
    var userQuestions by remember { mutableStateOf<List<AssistQuestionResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteNotification by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun fetchQuestions() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getMyQuestions()
                }
                if (response.isSuccessful) {
                    userQuestions = response.body() ?: emptyList()
                } else {
                    errorMessage = "Failed to load questions"
                }
            } catch (e: Exception) {
                errorMessage = "An error occurred: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchQuestions()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "My Questions", 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Bold,
                            color = LocalAppColors.current.textTitle
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LocalAppColors.current.textTitle)
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
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LocalAppColors.current.primary)
                    }
                } else if (errorMessage != null) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Text(errorMessage ?: "", color = LocalAppColors.current.error, fontSize = 15.sp)
                    }
                } else if (userQuestions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("You haven't asked any questions yet.", color = LocalAppColors.current.iconTint, fontSize = 15.sp)
                    }
                } else {
                    userQuestions.forEach { userQuestion ->
                        val isPending = userQuestion.answers.isEmpty()
                        QuestionItem(
                            company = userQuestion.companyName,
                            question = userQuestion.questionText,
                            askedTime = userQuestion.date,
                            status = if (isPending) "Pending" else "Answered",
                            alumniName = userQuestion.answers.firstOrNull()?.answererName,
                            alumniRole = userQuestion.answers.firstOrNull()?.answererRole,
                            answer = userQuestion.answers.firstOrNull()?.answerText,
                            answerTime = userQuestion.answers.firstOrNull()?.date,
                            onDelete = {
                                scope.launch {
                                    try {
                                        val response = withContext(Dispatchers.IO) {
                                            RetrofitClient.apiService.deleteQuestion(userQuestion.id)
                                        }
                                        if (response.isSuccessful) {
                                            fetchQuestions()
                                            showDeleteNotification = true
                                            kotlinx.coroutines.delay(2000)
                                            showDeleteNotification = false
                                        }
                                    } catch (e: Exception) {
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Animated Delete Notification Popup
        androidx.compose.animation.AnimatedVisibility(
            visible = showDeleteNotification,
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
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = LocalAppColors.current.surface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Question deleted",
                        color = LocalAppColors.current.surface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class QuestionData(
    val company: String,
    val question: String,
    val askedTime: String,
    val status: String,
    val alumniName: String? = null,
    val alumniRole: String? = null,
    val answer: String? = null,
    val answerTime: String? = null
)

@Composable
fun QuestionItem(
    company: String,
    question: String,
    askedTime: String,
    status: String,
    alumniName: String? = null,
    alumniRole: String? = null,
    answer: String? = null,
    answerTime: String? = null,
    onDelete: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = LocalAppColors.current.surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Company and Status/Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = LocalAppColors.current.primaryHighlight,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = company,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = LocalAppColors.current.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status)
                    if (onDelete != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Question", tint = LocalAppColors.current.error, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Question Text
            Text(
                text = question,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = LocalAppColors.current.textTitle,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Asked  $askedTime",
                fontSize = 12.sp,
                color = LocalAppColors.current.iconTint
            )

            // Answer Block (if applicable)
            if (status == "Answered" && answer != null) {
                Spacer(modifier = Modifier.height(20.dp))
                AnswerBlock(
                    name = alumniName ?: "",
                    role = alumniRole ?: "",
                    answer = answer,
                    time = answerTime ?: ""
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val bgColor = if (status == "Answered") LocalAppColors.current.successBg else LocalAppColors.current.warningBg
    val textColor = if (status == "Answered") LocalAppColors.current.success else LocalAppColors.current.warning
    val icon = if (status == "Answered") Icons.Default.CheckCircle else Icons.Default.Schedule

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = textColor, modifier = Modifier.size(14.dp))
            Text(status, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun AnswerBlock(
    name: String,
    role: String,
    answer: String,
    time: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LocalAppColors.current.background
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Blue indicator line like in screenshot
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(LocalAppColors.current.primary, RoundedCornerShape(2.dp))
                    .align(Alignment.Top)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                // Alumni Profile Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = LocalAppColors.current.surfaceHighlight
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                name.split(" ").map { it.take(1) }.joinToString(""),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalAppColors.current.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
                        Text(role, fontSize = 11.sp, color = LocalAppColors.current.textSecondary)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Answer Text
                Text(
                    text = answer,
                    fontSize = 13.sp,
                    color = LocalAppColors.current.textBody,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Answered  $time",
                    fontSize = 11.sp,
                    color = LocalAppColors.current.iconTint
                )
            }
        }
    }
}
