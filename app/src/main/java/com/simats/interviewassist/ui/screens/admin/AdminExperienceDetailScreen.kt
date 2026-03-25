package com.simats.interviewassist.ui.screens.admin

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.*
import com.simats.interviewassist.data.models.InterviewExperienceResponse
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.ui.screens.student.StatusBadge
import com.simats.interviewassist.ui.screens.student.ExperienceSectionCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.LocalAppColors
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.launch
import com.simats.interviewassist.data.models.ReviewExperienceRequest
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminExperienceDetailScreen(
    companyName: String,
    experienceId: String,
    isReviewMode: Boolean = false,
    reportId: Int? = null,
    onBack: () -> Unit,
    onNavigateToUserProfile: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    var showDetailsSheet by remember { mutableStateOf(false) }
    var detailedUser by remember { mutableStateOf<ExpUserItem?>(null) }

    val scope = rememberCoroutineScope()
    var experience by remember { mutableStateOf<InterviewExperienceResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isActionLoading by remember { mutableStateOf(false) }

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

    suspend fun handleStatusToggle(user: ExpUserItem) {
        try {
            val response = when {
                user.status == "pending" -> RetrofitClient.apiService.approveUser(user.id)
                user.isSuspended -> RetrofitClient.apiService.unsuspendUser(user.id)
                else -> RetrofitClient.apiService.suspendUser(user.id)
            }
            
            if (response.isSuccessful) {
                val action = when {
                    user.status == "pending" -> "approved"
                    user.isSuspended -> "activated"
                    else -> "suspended"
                }
                Toast.makeText(context, "User $action successfully", Toast.LENGTH_SHORT).show()
                
                // Refresh profile data if sheet is still open
                val updatedProfile = RetrofitClient.apiService.getUserProfile(user.id)
                if (updatedProfile.isSuccessful) {
                    updatedProfile.body()?.let { data ->
                        detailedUser = detailedUser?.copy(
                            status = data.status ?: "active",
                            isSuspended = data.isSuspended ?: false
                        )
                    }
                }
            } else {
                Toast.makeText(context, "Failed to update user status", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    var userProfile by remember { mutableStateOf<com.simats.interviewassist.data.models.UserProfileResponse?>(null) }
    var isProfileLoading by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = companyName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle,
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
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back", 
                            modifier = Modifier.size(20.dp),
                            tint = LocalAppColors.current.textTitle
                        )
                    }
                },
                actions = {},
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
                            val profilePic = exp.userProfilePic
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
                                val initials = if (exp.userName.isNotBlank()) {
                                    val parts = exp.userName.split(" ")
                                    if (parts.size >= 2) "${parts[0][0]}${parts[1][0]}".uppercase()
                                    else parts[0].take(2).uppercase()
                                } else "U"
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = initials,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LocalAppColors.current.primary
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
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
                                text = if (!exp.userProfileCompany.isNullOrBlank()) {
                                    "${exp.userProfileRole ?: "Alumni"} @ ${exp.userProfileCompany}"
                                } else {
                                    "Alumni"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = LocalAppColors.current.textSecondary,
                                fontWeight = FontWeight.Medium
                            )


                            if (isReviewMode || reportId != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = if (reportId != null) LocalAppColors.current.errorBg else LocalAppColors.current.primaryHighlight,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (reportId != null) "REPORTED CONTENT" else "REVIEWING EXPERIENCE",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (reportId != null) LocalAppColors.current.error else LocalAppColors.current.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "${exp.userRole} @ $companyName",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (reportId != null) LocalAppColors.current.error else LocalAppColors.current.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        
                        // Current Status Badge
                        Surface(
                            color = when(exp.status) {
                                "approved" -> LocalAppColors.current.successBg
                                "rejected" -> LocalAppColors.current.errorBg
                                else -> LocalAppColors.current.warningBg
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = exp.status.replaceFirstChar { it.uppercase() },
                                color = when(exp.status) {
                                    "approved" -> LocalAppColors.current.success
                                    "rejected" -> LocalAppColors.current.error
                                    else -> LocalAppColors.current.warning
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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
                    
                    ExperienceSectionCard(
                        title = "About the Author",
                        icon = Icons.Default.Person,
                        containerColor = LocalAppColors.current.primary.copy(alpha = 0.15f),
                        contentColor = LocalAppColors.current.primary,
                        border = BorderStroke(1.dp, LocalAppColors.current.primary.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = exp.brief.let { if (it.isNullOrBlank()) "Not provided" else it },
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
                
                // Admin Action Section
                if (exp.status == "pending" || reportId != null) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = LocalAppColors.current.surface,
                        tonalElevation = 2.dp,
                        border = BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                if (reportId != null) "Moderation Decision" else "Review Decision",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = LocalAppColors.current.textTitle
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                if (reportId != null) "Decide whether to keep this reported content or remove it from the platform."
                                else "Approve this experience to make it public, or reject it with feedback to the user.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LocalAppColors.current.textSecondary
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                var isSubmitting by remember { mutableStateOf(false) }
                                
                                if (reportId != null) {
                                    // Report Mode Buttons
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                isSubmitting = true
                                                try {
                                                    val res = RetrofitClient.apiService.keepContent(reportId)
                                                    if (res.isSuccessful) {
                                                        Toast.makeText(context, "Report dismissed, experience kept", Toast.LENGTH_SHORT).show()
                                                        onBack()
                                                    }
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                } finally {
                                                    isSubmitting = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(54.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LocalAppColors.current.textTitle),
                                        border = BorderStroke(1.dp, LocalAppColors.current.divider),
                                        enabled = !isSubmitting
                                    ) {
                                        Icon(Icons.Default.CheckCircleOutline, null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Keep", fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                isSubmitting = true
                                                try {
                                                    val res = RetrofitClient.apiService.removeContent(reportId)
                                                    if (res.isSuccessful) {
                                                        Toast.makeText(context, "Experience removed permanently", Toast.LENGTH_SHORT).show()
                                                        onBack()
                                                    }
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                } finally {
                                                    isSubmitting = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(54.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.error),
                                        enabled = !isSubmitting
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Remove", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    // Review Mode Buttons
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                isSubmitting = true
                                                try {
                                                    val res = RetrofitClient.apiService.reviewExperience(exp.id, ReviewExperienceRequest("rejected"))
                                                    if (res.isSuccessful) {
                                                        Toast.makeText(context, "Experience rejected", Toast.LENGTH_SHORT).show()
                                                        onBack()
                                                    }
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                } finally {
                                                    isSubmitting = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(54.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LocalAppColors.current.error),
                                        border = BorderStroke(1.dp, LocalAppColors.current.error),
                                        enabled = !isSubmitting
                                    ) {
                                        Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Reject", fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                isSubmitting = true
                                                try {
                                                    val res = RetrofitClient.apiService.reviewExperience(exp.id, ReviewExperienceRequest("approved"))
                                                    if (res.isSuccessful) {
                                                        Toast.makeText(context, "Experience approved!", Toast.LENGTH_SHORT).show()
                                                        onBack()
                                                    }
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                } finally {
                                                    isSubmitting = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(54.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.success),
                                        enabled = !isSubmitting
                                    ) {
                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Approve", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    if (showDetailsSheet && detailedUser != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                showDetailsSheet = false
                detailedUser = null 
            },
            sheetState = sheetState,
            containerColor = LocalAppColors.current.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = LocalAppColors.current.divider.copy(alpha = 0.4f)) }
        ) {
            ExpUserSheet(
                user = detailedUser!!,
                isLoading = isProfileLoading,
                onClose = { 
                    showDetailsSheet = false
                    detailedUser = null
                },
                onToggleStatus = { user ->
                    scope.launch { handleStatusToggle(user) }
                }
            )
        }
    }
}

@Composable
fun ExpUserSheet(
    user: ExpUserItem,
    isLoading: Boolean = false,
    onClose: () -> Unit,
    onToggleStatus: (ExpUserItem) -> Unit
) {
    val sheetColors = LocalAppColors.current
    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
        color = sheetColors.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .size(40.dp, 4.dp)
                    .clip(CircleShape)
                    .background(sheetColors.divider)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.height(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = sheetColors.primary)
                }
            } else {
                // Profile Section
                Box(contentAlignment = Alignment.Center) {
                    val statusColor = when (user.status.lowercase()) {
                        "active" -> sheetColors.success
                        "suspended", "rejected" -> sheetColors.error
                        "pending" -> sheetColors.warning
                        else -> sheetColors.divider
                    }
                    
                    Surface(
                        modifier = Modifier.size(110.dp),
                        shape = CircleShape,
                        color = statusColor.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(2.dp, statusColor.copy(alpha = 0.1f))
                    ) {}

                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = sheetColors.primaryHighlight,
                        shadowElevation = 4.dp
                    ) {
                        val profilePic = user.profilePic
                        if (!profilePic.isNullOrEmpty() && user.role.lowercase() != "admin") {
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
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                val initials = if (user.name.contains(" ")) {
                                    user.name.split(" ").filter { it.isNotEmpty() }.map { it.first() }.joinToString("")
                                } else {
                                    user.name.take(1)
                                }
                                Text(
                                    initials,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = sheetColors.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = user.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = sheetColors.textTitle
                )
                Text(
                    text = user.email,
                    fontSize = 16.sp,
                    color = sheetColors.textSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Status Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusColor = when (user.status.lowercase()) {
                        "active" -> sheetColors.success
                        "suspended", "rejected" -> sheetColors.error
                        "pending" -> sheetColors.warning
                        else -> sheetColors.textSecondary
                    }
                    
                    val statusBg = when (user.status.lowercase()) {
                        "active" -> sheetColors.successBg
                        "suspended", "rejected" -> sheetColors.errorBg
                        "pending" -> sheetColors.warning.copy(alpha = 0.1f)
                        else -> sheetColors.divider
                    }

                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = if (user.status == "pending") "PENDING APPROVAL" else user.status.uppercase(),
                            fontSize = 12.sp,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = sheetColors.divider.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = user.role.uppercase(),
                            fontSize = 12.sp,
                            color = sheetColors.textTitle,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val isStudent = user.role.lowercase() == "student"
                    ExpDetailRow(label = "Phone Number", value = user.phoneNumber ?: "Not provided", icon = Icons.Outlined.Phone)
                    
                    if (user.profile != null) {
                        ExpDetailRow(
                            label = if (isStudent) "Major" else "Specialization", 
                            value = (if (isStudent) user.profile.major else user.profile.specialization) ?: "Not provided", 
                            icon = Icons.Outlined.School
                        )
                        
                        if (isStudent) {
                            ExpDetailRow(label = "Current Year", value = user.profile.currentYear ?: "N/A", icon = Icons.Outlined.Layers)
                        }
                        
                        ExpDetailRow(label = if (isStudent) "Expected Graduation" else "Year of Graduation", value = user.profile.expectedGradYear ?: "N/A", icon = Icons.Outlined.DateRange)
                        
                        if (!isStudent) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("PROFESSIONAL PROFILE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = sheetColors.primary)
                            ExpDetailRow(label = "Current Organization", value = user.profile.currentCompany ?: "Not provided", icon = Icons.Outlined.Business)
                            ExpDetailRow(label = "Designation", value = user.profile.designation ?: "Not provided", icon = Icons.Outlined.WorkOutline)
                        }

                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("BIO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = sheetColors.primary)
                    Text(
                        user.bio ?: user.profile?.bio ?: "Not provided", 
                        fontSize = 14.sp, 
                        color = sheetColors.textSecondary, 
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Primary Action Button
                val actionText = when {
                    user.status == "rejected" || user.status == "pending" -> "Approve User"
                    user.isSuspended -> "Activate User"
                    else -> "Suspend User"
                }
                val actionColor = when {
                    user.status == "rejected" || user.status == "pending" -> sheetColors.success
                    user.isSuspended -> sheetColors.success
                    else -> sheetColors.error
                }

                Button(
                    onClick = {
                        onToggleStatus(user)
                        onClose()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = actionColor)
                ) {
                    Text(actionText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Details", color = sheetColors.textSecondary)
                }
            }
        }
    }
}

@Composable
fun ExpDetailRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = LocalAppColors.current.primaryHighlight.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = LocalAppColors.current.primary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = LocalAppColors.current.textSecondary)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LocalAppColors.current.textTitle)
        }
    }
}

data class ExpUserItem(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val status: String = "active",
    val isSuspended: Boolean = false,
    val phoneNumber: String? = null,
    val profilePic: String? = null,
    val bio: String? = null,
    val profile: com.simats.interviewassist.data.models.ProfileData? = null
)
