package com.simats.interviewassist.ui.screens.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.animation.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.simats.interviewassist.data.models.InterviewExperienceResponse
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.ui.screens.student.HelpfulExperiencesManager
import com.simats.interviewassist.ui.screens.student.StatusBadge
import com.simats.interviewassist.ui.screens.student.ExperienceSectionCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.util.Base64
import android.graphics.BitmapFactory
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniExperienceDetailScreen(
    companyName: String, // Route compatibility
    experienceId: String,
    preferenceManager: com.simats.interviewassist.utils.PreferenceManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var experience by remember { mutableStateOf<InterviewExperienceResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val expIdInt = experienceId.toIntOrNull() ?: 0

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
    val scope = rememberCoroutineScope()

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
                        modifier = Modifier.padding(start = 16.dp)
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LocalAppColors.current.textTitle, modifier = Modifier.size(20.dp))
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LocalAppColors.current.surface,
                    titleContentColor = LocalAppColors.current.textTitle
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
                // Premium Profile Header
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
                            // Robust profile picture logic
                            val profilePic = if (!exp.userProfilePic.isNullOrBlank()) {
                                exp.userProfilePic
                            } else if (exp.userId == preferenceManager.getUserId()) {
                                preferenceManager.getProfilePicPath()
                            } else null

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
                                    val parts = exp.userName.split(" ")
                                    if (parts.size >= 2) "${parts[0][0]}${parts[1][0]}".uppercase()
                                    else parts[0].take(2).uppercase()
                                } else "U"
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = initials,
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
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val diffColor = when(exp.difficulty) {
                        "Easy" -> LocalAppColors.current.success
                        "Medium" -> LocalAppColors.current.warning
                        "Hard" -> LocalAppColors.current.error
                        else -> LocalAppColors.current.textSecondary
                    }
                    StatusBadge(exp.difficulty, diffColor.copy(alpha = 0.1f), diffColor)
                    
                    exp.workMode?.let { StatusBadge(it, LocalAppColors.current.surfaceVariant, LocalAppColors.current.textSecondary) }
                    exp.candidateType?.let { StatusBadge(it, LocalAppColors.current.surfaceVariant, LocalAppColors.current.textSecondary) }
                    
                    if (exp.isSelected) {
                        StatusBadge("Selected", LocalAppColors.current.success.copy(alpha = 0.1f), LocalAppColors.current.success)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    
                    // About the Author
                    ExperienceSectionCard(
                        title = "About the Author",
                        icon = Icons.Default.Person,
                        containerColor = LocalAppColors.current.primary,
                        contentColor = Color.White,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = exp.brief ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            lineHeight = 24.sp
                        )
                    }

                    // How I got the interview
                    exp.applicationProcess?.let {
                        ExperienceSectionCard(
                            title = "How I got the interview",
                            icon = Icons.Default.Bolt,
                            containerColor = LocalAppColors.current.warning,
                            contentColor = Color.White,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                lineHeight = 24.sp
                            )
                        }
                    }

                    // Interview Process Breakdown
                    if (!exp.interviewRounds.isNullOrEmpty()) {
                        ExperienceSectionCard(
                            title = "Interview Process Breakdown",
                            icon = Icons.Default.Timeline,
                            containerColor = LocalAppColors.current.purple,
                            contentColor = Color.White,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            exp.interviewRounds.forEach { round ->
                                Row(modifier = Modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.Top) {
                                    Text(
                                        "• ",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                    Column {
                                        Text(
                                            text = round.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = round.duration,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // My Experience (Detailed)
                    ExperienceSectionCard(
                        title = "My Experience (Detailed)",
                        icon = Icons.AutoMirrored.Filled.Article,
                        containerColor = LocalAppColors.current.textTitle,
                        contentColor = Color.White,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = exp.myExperience ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            lineHeight = 24.sp
                        )
                    }

                    // Questions Asked
                    if (!exp.technicalQuestions.isNullOrEmpty() || !exp.behavioralQuestions.isNullOrEmpty()) {
                        ExperienceSectionCard(
                            title = "Questions Asked",
                            icon = Icons.AutoMirrored.Filled.HelpOutline,
                            containerColor = LocalAppColors.current.primary,
                            contentColor = Color.White,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            if (!exp.technicalQuestions.isNullOrEmpty()) {
                                Text(
                                    "TECHNICAL QUESTIONS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                exp.technicalQuestions.forEach {
                                    Text("• $it", style = MaterialTheme.typography.bodyMedium, color = Color.White, modifier = Modifier.padding(bottom = 6.dp))
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            
                            if (!exp.behavioralQuestions.isNullOrEmpty()) {
                                Text(
                                    "BEHAVIORAL QUESTIONS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                exp.behavioralQuestions.forEach {
                                    Text("• $it", style = MaterialTheme.typography.bodyMedium, color = Color.White, modifier = Modifier.padding(bottom = 6.dp))
                                }
                            }
                        }
                    }

                    // Mistakes Made
                    if (!exp.mistakes.isNullOrEmpty()) {
                        ExperienceSectionCard(
                            title = "Mistakes I Made",
                            icon = Icons.Default.Warning,
                            containerColor = LocalAppColors.current.error,
                            contentColor = Color.White,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            exp.mistakes.forEachIndexed { index, mistake ->
                                 Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.Top) {
                                    Text("${index + 1}. ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(mistake, style = MaterialTheme.typography.bodyMedium, color = Color.White, lineHeight = 22.sp)
                                }
                            }
                        }
                    }

                    // Preparation Strategy
                    if (!exp.preparationStrategy.isNullOrEmpty()) {
                        ExperienceSectionCard(
                            title = "Preparation Strategy",
                            icon = Icons.Default.School,
                            containerColor = LocalAppColors.current.success,
                            contentColor = Color.White,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            exp.preparationStrategy.forEach { (category, tools) ->
                                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                    Text(category.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    tools.forEach { tool ->
                                        Text("• $tool", style = MaterialTheme.typography.bodyMedium, color = Color.White, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
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
                            containerColor = LocalAppColors.current.purple,
                            contentColor = Color.White,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            exp.finalAdvice.forEachIndexed { index, advice ->
                                Row(modifier = Modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.Top) {
                                    Text("${index + 1}. ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(advice, style = MaterialTheme.typography.bodyMedium, color = Color.White, lineHeight = 22.sp)
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Floating Notification Popup (Premium Style)
    androidx.compose.animation.AnimatedVisibility(
        visible = showGlobalNotification,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
        modifier = Modifier.align(Alignment.TopCenter)
    ) {
        Surface(
            color = LocalAppColors.current.textTitle.copy(alpha = 0.95f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(top = 80.dp),
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (globalNotificationMessage.contains("helpful")) Icons.Default.ThumbUp else Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = globalNotificationMessage,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    }
}
