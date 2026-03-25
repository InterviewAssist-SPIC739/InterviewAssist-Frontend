package com.simats.interviewassist.ui.screens.admin

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.data.models.CompanyResponse
import com.simats.interviewassist.data.models.InterviewExperienceResponse
import com.simats.interviewassist.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import com.simats.interviewassist.ui.screens.student.StatBox
import com.simats.interviewassist.ui.screens.student.ExamSection
import com.simats.interviewassist.ui.screens.student.ProcessStep
import com.simats.interviewassist.ui.screens.student.ExamPatternTable
import com.simats.interviewassist.ui.screens.student.HiringTimeline
import com.simats.interviewassist.ui.screens.student.OverviewTab
import com.simats.interviewassist.ui.screens.student.ExperiencesTab
import com.simats.interviewassist.utils.PreferenceManager
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import android.widget.Toast
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCompanyDetailsScreen(
    companyId: Int,
    companyName: String,
    onBack: () -> Unit,
    onNavigateToExperienceDetail: (String) -> Unit,
    preferenceManager: PreferenceManager,
    onNavigateToProfile: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var company by remember { mutableStateOf<CompanyResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val showNotification: (String) -> Unit = { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(companyId) {
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
    val scope = rememberCoroutineScope()

    val refreshCompany = {
        scope.launch {
            isLoading = true
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getCompanyDetail(companyId)
                }
                if (response.isSuccessful) {
                    company = response.body()
                }
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    val onDeleteExperience: (Int) -> Unit = { experienceId ->
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.deleteExperience(experienceId)
                }
                if (response.isSuccessful) {
                    Toast.makeText(context, "Experience deleted", Toast.LENGTH_SHORT).show()
                    refreshCompany()
                } else {
                    Toast.makeText(context, "Failed to delete experience", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val onDeleteQuestion: (Int) -> Unit = { questionId ->
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.deleteQuestion(questionId)
                }
                if (response.isSuccessful) {
                    Toast.makeText(context, "Question deleted", Toast.LENGTH_SHORT).show()
                    refreshCompany()
                } else {
                    Toast.makeText(context, "Failed to delete question", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val onDeleteAnswer: (Int) -> Unit = { answerId ->
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.deleteAnswer(answerId)
                }
                if (response.isSuccessful) {
                    Toast.makeText(context, "Answer deleted", Toast.LENGTH_SHORT).show()
                    refreshCompany()
                } else {
                    Toast.makeText(context, "Failed to delete answer", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = LocalAppColors.current.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section (matches reference image)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                // Banner Background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color(0xFFF1F5F9))
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
                            .padding(start = 16.dp)
                            .align(Alignment.BottomStart)
                            .size(72.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = LocalAppColors.current.surface,
                        shadowElevation = 8.dp
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

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(24.dp))

                // Company Name & Info
                Text(
                    text = company?.name ?: companyName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalAppColors.current.textTitle
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp), tint = Color(0xFF64748B))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(company?.location ?: "Loading...", fontSize = 15.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.width(28.dp))
                    Icon(Icons.Default.Public, null, modifier = Modifier.size(18.dp), tint = Color(0xFF64748B))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(company?.sector ?: "", fontSize = 15.sp, color = Color(0xFF64748B))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = company?.description ?: "",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Removed Website and Follow buttons for Admin
                
                Spacer(modifier = Modifier.height(24.dp))

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Cards (matches reference image colors)
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

                Spacer(modifier = Modifier.height(32.dp))

                // Visit Website Action for Admin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = {
                            company?.websiteUrl?.let { url ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(
                                    if (url.startsWith("http")) url else "https://$url"
                                ))
                                context.startActivity(intent)
                            } ?: showNotification("Website not available")
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, LocalAppColors.current.primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LocalAppColors.current.primary)
                    ) {
                        Icon(Icons.Default.Language, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Visit Website", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = LocalAppColors.current.primary,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = LocalAppColors.current.primary,
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        when(index) {
                                            0 -> Icons.Outlined.Book
                                            1 -> Icons.Outlined.People
                                            else -> Icons.Outlined.QuestionAnswer
                                        },
                                        null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(title, fontSize = 12.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LocalAppColors.current.primary)
                    }
                } else if (errorMessage != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        Text(errorMessage ?: "Error", color = LocalAppColors.current.error)
                    }
                } else if (company != null) {
                    // Content Based on Tab
                    when (selectedTab) {
                        0 -> company?.let { OverviewTab(it) }
                1 -> company?.let { AdminExperiencesTab(it, onNavigateToExperienceDetail, onDeleteExperience, onNavigateToProfile) }
                        2 -> company?.let { AdminQuestionsTab(it, onDeleteQuestion, onDeleteAnswer) }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}


@Composable
fun AdminExperiencesTab(
    company: CompanyResponse,
    onExperienceClick: (String) -> Unit,
    onDeleteExperience: (Int) -> Unit,
    onNavigateToProfile: (Int) -> Unit = {}
) {
    Column {
        val experiences = (company.experiences ?: emptyList())
        Text(
            text = "${experiences.size} experiences shared",
            fontSize = 14.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        experiences.forEach { experience ->
            AdminExperienceCard(
                experience = experience,
                companyName = company.name,
                onClick = { onExperienceClick(experience.id.toString()) },
                onDelete = { onDeleteExperience(experience.id) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AdminExperienceCard(experience: InterviewExperienceResponse, companyName: String, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Info Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    color = Color(0xFFF1F5F9),
                    shape = CircleShape
                ) {
                    val profilePic = experience.userProfilePic
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
                            val initials = if (experience.userName.isNotBlank()) {
                                val parts = experience.userName.split(" ")
                                if (parts.size >= 2) "${parts[0][0]}${parts[1][0]}".uppercase()
                                else parts[0].take(2).uppercase()
                            } else "U"
                            Text(
                                text = initials,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = experience.userName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalAppColors.current.textTitle
                        )
                        if (experience.isUserVerified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                modifier = Modifier.size(16.dp),
                                tint = LocalAppColors.current.primary
                            )
                        }
                    }
                    Text(
                        text = experience.userRole,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }

                // Difficulty Tag
                Surface(
                    color = when(experience.difficulty) {
                        "Easy" -> LocalAppColors.current.successBg
                        "Medium" -> LocalAppColors.current.warningBg
                        "Hard" -> LocalAppColors.current.errorBg
                        else -> Color(0xFFF8FAFC)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = experience.difficulty,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(experience.difficulty) {
                            "Easy" -> LocalAppColors.current.success
                            "Medium" -> LocalAppColors.current.warning
                            "Hard" -> Color(0xFF991B1B)
                            else -> Color(0xFF64748B)
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, null, tint = Color(0xFF94A3B8))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(LocalAppColors.current.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete", color = LocalAppColors.current.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = LocalAppColors.current.error) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interview Context
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Interview at ", fontSize = 15.sp, color = Color(0xFF64748B))
                Text(companyName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
                Spacer(modifier = Modifier.width(12.dp))
                
                Surface(
                    color = if (experience.isSelected) LocalAppColors.current.primaryHighlight else LocalAppColors.current.errorBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (experience.isSelected) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            null,
                            modifier = Modifier.size(12.dp),
                            tint = if (experience.isSelected) LocalAppColors.current.primary else LocalAppColors.current.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (experience.isSelected) "Selected" else "Rejected",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (experience.isSelected) LocalAppColors.current.primary else LocalAppColors.current.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                experience.workMode?.let { AdminBadge(icon = Icons.Default.Computer, text = it) }
                experience.candidateType?.let { AdminBadge(text = it) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Experience Snippet
            val content = experience.brief ?: experience.myExperience ?: ""
            Text(
                text = content.take(150) + if (content.length > 150) "..." else "",
                fontSize = 15.sp,
                color = Color(0xFF475569),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp), tint = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(experience.date, fontSize = 13.sp, color = Color(0xFF94A3B8))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ThumbUpOffAlt, null, modifier = Modifier.size(18.dp), tint = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${experience.helpfulCount} Helpful", fontSize = 13.sp, color = Color(0xFF94A3B8))
                }
            }
        }
    }
}

@Composable
fun AdminQuestionsTab(
    company: CompanyResponse,
    onDeleteQuestion: (Int) -> Unit,
    onDeleteAnswer: (Int) -> Unit
) {
    Column {
        val questions = company.questions ?: emptyList()
        
        // Stats for Questions
        Text(
            text = "${questions.size} questions posted",
            fontSize = 14.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (questions.isNotEmpty()) {
            questions.forEach { question ->
                AdminQuestionCard(question, onDeleteQuestion, onDeleteAnswer)
                Spacer(modifier = Modifier.height(20.dp))
            }
        } else {
            // Empty State
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.QuestionAnswer, null, modifier = Modifier.size(48.dp), tint = Color(0xFFCBD5E1))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No questions yet", fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    Text("Questions asked by students will appear here.", fontSize = 14.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Verified Alumni Info Card
        Surface(
            color = Color(0xFFF0F9FF),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Lightbulb, null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Questions are answered by verified alumni who have worked at this company. Use the Ask Question button to post your own question!",
                    fontSize = 14.sp,
                    color = Color(0xFF0369A1),
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun AdminQuestionCard(
    question: com.simats.interviewassist.data.models.QuestionResponse,
    onDeleteQuestion: (Int) -> Unit,
    onDeleteAnswer: (Int) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Question") },
            text = { Text("Are you sure you want to delete this question? All associated answers will also be removed.") },
            confirmButton = {
                TextButton(onClick = { 
                    showDeleteConfirm = false
                    onDeleteQuestion(question.id)
                }) {
                    Text("Delete", color = LocalAppColors.current.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
            containerColor = LocalAppColors.current.surface
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(
                    text = question.questionText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = LocalAppColors.current.textTitle,
                    lineHeight = 26.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.DeleteOutline, "Delete Question", tint = LocalAppColors.current.error.copy(alpha = 0.7f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Asked by ${question.askedBy} • ${question.date}",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (question.answers.isNotEmpty()) {
                question.answers.forEach { answer ->
                    AdminAnswerCard(answer, onDeleteAnswer)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                Surface(
                    color = LocalAppColors.current.warningBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No answers yet. Alumni will respond soon!",
                        fontSize = 14.sp,
                        color = LocalAppColors.current.warning,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun AdminAnswerCard(
    answer: com.simats.interviewassist.data.models.AnswerResponse,
    onDeleteAnswer: (Int) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Answer") },
            text = { Text("Are you sure you want to delete this answer?") },
            confirmButton = {
                TextButton(onClick = { 
                    showDeleteConfirm = false
                    onDeleteAnswer(answer.id)
                }) {
                    Text("Delete", color = LocalAppColors.current.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
            containerColor = LocalAppColors.current.surface
        )
    }

    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Answerer Icon/Initials
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    color = LocalAppColors.current.primaryHighlight,
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
                                text = if (answer.answererName.isNotBlank()) answer.answererName.first().toString().uppercase() else "A",
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
                        Text(
                            text = answer.answererName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = LocalAppColors.current.textTitle
                        )
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
                        color = Color(0xFF64748B)
                    )
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.DeleteOutline, "Delete Answer", tint = LocalAppColors.current.error.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = answer.answerText,
                fontSize = 15.sp,
                color = Color(0xFF475569),
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = answer.date,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun AdminBadge(icon: androidx.compose.ui.graphics.vector.ImageVector? = null, text: String) {
    Surface(
        color = Color(0xFFF1F5F9),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(14.dp), tint = Color(0xFF64748B))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(text, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
        }
    }
}
