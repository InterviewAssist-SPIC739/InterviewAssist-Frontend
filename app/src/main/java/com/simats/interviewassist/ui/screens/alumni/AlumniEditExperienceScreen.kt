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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.ui.screens.student.ExperienceSectionCard
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.data.models.ExperienceRequest
import com.simats.interviewassist.data.models.InterviewExperienceResponse
import com.simats.interviewassist.ui.screens.student.ProcessStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniEditExperienceScreen(
    companyName: String,
    experienceId: String,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var experience by remember { mutableStateOf<InterviewExperienceResponse?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // State for all editable fields
    var brief by remember { mutableStateOf("") }
    var applicationProcess by remember { mutableStateOf("") }
    var interviewProcess by remember { mutableStateOf("") }
    var myExperience by remember { mutableStateOf("") }
    var technicalQuestions by remember { mutableStateOf("") }
    var behavioralQuestions by remember { mutableStateOf("") }
    var mistakes by remember { mutableStateOf("") }
    var prepStrategy by remember { mutableStateOf("") }
    var finalAdvice by remember { mutableStateOf("") }

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
                val exp = response.body()
                experience = exp
                exp?.let {
                    brief = it.brief ?: ""
                    applicationProcess = it.applicationProcess ?: ""
                    interviewProcess = it.interviewRounds?.joinToString("\n") { step -> "${step.title}: ${step.duration}" } ?: ""
                    myExperience = it.myExperience ?: ""
                    technicalQuestions = it.technicalQuestions?.joinToString("\n") ?: ""
                    behavioralQuestions = it.behavioralQuestions?.joinToString("\n") ?: ""
                    mistakes = it.mistakes?.joinToString("\n") ?: ""
                    prepStrategy = it.preparationStrategy?.map { entry -> "${entry.key}: ${entry.value.joinToString(", ")}" }?.joinToString("\n") ?: ""
                    finalAdvice = it.finalAdvice?.joinToString("\n") ?: ""
                }
            } else {
                errorMessage = "Failed to load experience details"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    val handleSave = {
        scope.launch {
            isSaving = true
            try {
                // Parse helper functions
                val parsedRounds = interviewProcess.split("\n").filter { it.isNotBlank() }.map { line ->
                    val parts = line.split(":", limit = 2)
                    ProcessStep(parts[0].trim(), if (parts.size > 1) parts[1].trim() else "")
                }
                
                val parsedPrep = prepStrategy.split("\n").filter { it.isNotBlank() }.mapNotNull { line ->
                    val parts = line.split(":", limit = 2)
                    if (parts.size == 2) {
                        parts[0].trim() to parts[1].split(",").map { it.trim() }.filter { it.isNotBlank() }
                    } else null
                }.toMap()

                val request = ExperienceRequest(
                    companyId = experience?.id ?: 0, // In update, company_id might be ignored by backend but model requires it
                    userRole = experience?.userRole ?: "Alumni",
                    difficulty = experience?.difficulty ?: "Medium",
                    isSelected = experience?.isSelected ?: false,
                    workMode = experience?.workMode,
                    candidateType = experience?.candidateType,
                    myExperience = myExperience,
                    brief = brief,
                    applicationProcess = applicationProcess,
                    interviewRounds = if (parsedRounds.isNotEmpty()) parsedRounds else null,
                    technicalQuestions = if (technicalQuestions.isNotBlank()) technicalQuestions.split("\n").filter { it.isNotBlank() } else null,
                    behavioralQuestions = if (behavioralQuestions.isNotBlank()) behavioralQuestions.split("\n").filter { it.isNotBlank() } else null,
                    mistakes = if (mistakes.isNotBlank()) mistakes.split("\n").filter { it.isNotBlank() } else null,
                    preparationStrategy = if (parsedPrep.isNotEmpty()) parsedPrep else null,
                    finalAdvice = if (finalAdvice.isNotBlank()) finalAdvice.split("\n").filter { it.isNotBlank() } else null
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.updateExperience(expIdInt, request)
                }

                if (response.isSuccessful) {
                    showSuccessDialog = true
                } else {
                    errorMessage = "Failed to update experience: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error saving: ${e.message}"
            } finally {
                isSaving = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Experience", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = LocalAppColors.current.primary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                    } else {
                        TextButton(onClick = { handleSave() }) {
                            Text("Save", color = LocalAppColors.current.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LocalAppColors.current.surface)
            )
        },
        containerColor = LocalAppColors.current.background
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LocalAppColors.current.primary)
            }
        } else if (errorMessage != null && experience == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(errorMessage ?: "Error", color = LocalAppColors.current.error)
                    Button(onClick = onBack) { Text("Go Back") }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                Text(
                    "Keep your experience details up to date to help juniors prepare better.",
                    fontSize = 14.sp,
                    color = LocalAppColors.current.textSecondary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.errorBg.copy(alpha = 0.5f)),
                        modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth()
                    ) {
                        Text(errorMessage!!, color = LocalAppColors.current.error, modifier = Modifier.padding(12.dp))
                    }
                }

                // Brief Section
                EditSectionCard(
                    title = "About the Author/Brief",
                    icon = Icons.Default.Person,
                    containerColor = LocalAppColors.current.primaryHighlight,
                    contentColor = LocalAppColors.current.primary,
                    value = brief,
                    onValueChange = { brief = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Application Process
                EditSectionCard(
                    title = "How I got the interview",
                    icon = Icons.Default.Bolt,
                    containerColor = LocalAppColors.current.purpleBg,
                    contentColor = LocalAppColors.current.purple,
                    value = applicationProcess,
                    onValueChange = { applicationProcess = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interview Process Breakdown
                EditSectionCard(
                    title = "Interview Process Breakdown",
                    icon = Icons.Default.Timeline,
                    containerColor = Color(0xFFEEF2FF),
                    contentColor = Color(0xFF4F46E5),
                    value = interviewProcess,
                    onValueChange = { interviewProcess = it },
                    placeholder = "e.g. Round 1: Online Test"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // My Experience
                EditSectionCard(
                    title = "My Detailed Experience",
                    icon = Icons.Default.Article,
                    containerColor = LocalAppColors.current.surfaceVariant,
                    contentColor = Color(0xFF374151),
                    value = myExperience,
                    onValueChange = { myExperience = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Technical Questions
                EditSectionCard(
                    title = "Questions Asked",
                    icon = Icons.Default.HelpOutline,
                    containerColor = LocalAppColors.current.warningBg,
                    contentColor = Color(0xFFEA580C),
                    value = technicalQuestions,
                    onValueChange = { technicalQuestions = it },
                    placeholder = "One question per line..."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mistakes
                EditSectionCard(
                    title = "Mistakes I Made",
                    icon = Icons.Default.Warning,
                    containerColor = LocalAppColors.current.errorBg,
                    contentColor = LocalAppColors.current.error,
                    value = mistakes,
                    onValueChange = { mistakes = it },
                    placeholder = "One mistake per line..."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Preparation Strategy
                EditSectionCard(
                    title = "Preparation Strategy",
                    icon = Icons.Default.School,
                    containerColor = LocalAppColors.current.successBg,
                    contentColor = LocalAppColors.current.success,
                    value = prepStrategy,
                    onValueChange = { prepStrategy = it },
                    placeholder = "Category: Tool 1, Tool 2..."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Final Advice
                EditSectionCard(
                    title = "Final Advice & Conclusion",
                    icon = Icons.Default.VolunteerActivism,
                    containerColor = Color(0xFFF5F3FF),
                    contentColor = Color(0xFF7C3AED),
                    value = finalAdvice,
                    onValueChange = { finalAdvice = it },
                    placeholder = "One tip per line..."
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { handleSave() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Save Changes", color = LocalAppColors.current.surface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* Don't dismiss on outside click */ },
            title = { Text("Update Submitted", fontWeight = FontWeight.Bold) },
            text = { Text("Your experience has been updated and sent for review. It will be live once accepted by the admin team.") },
            confirmButton = {
                TextButton(onClick = { 
                    showSuccessDialog = false
                    onSave() 
                }) {
                    Text("OK", fontWeight = FontWeight.Bold, color = LocalAppColors.current.primary)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = LocalAppColors.current.surface
        )
    }
}

