package com.simats.interviewassist.ui.screens.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import com.simats.interviewassist.ui.theme.*
import java.util.*
import android.app.DatePickerDialog
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.simats.interviewassist.data.models.*
import com.simats.interviewassist.ui.screens.student.ProcessStep
import com.simats.interviewassist.ui.screens.student.ExperienceSectionCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniShareExperienceScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToPosts: () -> Unit = {},
    onNavigateToAssist: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    var currentStep by remember { mutableIntStateOf(1) }
    
    // Step 1 State
    var companyName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var interviewDate by remember { mutableStateOf("") }
    var interviewMode by remember { mutableStateOf("") }
    var experienceLevel by remember { mutableStateOf("") }
    var outcome by remember { mutableStateOf("") }
    
    // API State
    var companies by remember { mutableStateOf<List<CompanyResponse>>(emptyList()) }
    var isCompaniesLoading by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isCompaniesLoading = true
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getCompanies()
            }
            if (response.isSuccessful) {
                companies = response.body() ?: emptyList()
            } else {
                Toast.makeText(context, "Failed to load companies", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isCompaniesLoading = false
        }
    }

    // Step 2 State
    var difficulty by remember { mutableStateOf("Medium") }
    var background by remember { mutableStateOf("") }
    var howGotInterview by remember { mutableStateOf("") }
    var processBreakdown by remember { mutableStateOf("") }
    var questionsAsked by remember { mutableStateOf("") }
    var mistakesMade by remember { mutableStateOf("") }
    var prepStrategy by remember { mutableStateOf("") }
    var finalAdvice by remember { mutableStateOf("") }
    var myExperience by remember { mutableStateOf("") }

    // Preview State
    var showSectionPreview by remember { mutableStateOf(false) }
    var previewSectionTitle by remember { mutableStateOf("") }
    var previewSectionContent by remember { mutableStateOf("") }

    // Validation State
    var showStep1Errors by remember { mutableStateOf(false) }
    var showStep2Errors by remember { mutableStateOf(false) }

    fun validateStep1(): Boolean {
        return companyName.isNotEmpty() && role.isNotEmpty() &&
                interviewDate.isNotEmpty() && interviewMode.isNotEmpty() &&
                experienceLevel.isNotEmpty() && outcome.isNotEmpty()
    }

    fun validateStep2(): Boolean {
        return background.isNotEmpty() && howGotInterview.isNotEmpty() &&
                processBreakdown.isNotEmpty() && questionsAsked.isNotEmpty() &&
                mistakesMade.isNotEmpty() && prepStrategy.isNotEmpty() &&
                finalAdvice.isNotEmpty() && myExperience.isNotEmpty()
    }

    Scaffold(
        bottomBar = {
            if (currentStep == 1) {
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
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(26.dp)) },
                            label = { Text("Add", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                            selected = true,
                            onClick = { },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = LocalAppColors.current.primary,
                                selectedTextColor = LocalAppColors.current.primary,
                                indicatorColor = LocalAppColors.current.primaryHighlight
                            )
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Outlined.Handshake, contentDescription = "Assist", modifier = Modifier.size(26.dp)) },
                            label = { Text("Assist", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                            selected = false,
                            onClick = onNavigateToAssist,
                            colors = NavigationBarItemDefaults.colors(
                                unselectedIconColor = LocalAppColors.current.textBody.copy(alpha = 0.4f),
                                unselectedTextColor = LocalAppColors.current.textBody.copy(alpha = 0.4f),
                                indicatorColor = LocalAppColors.current.primaryHighlight.copy(alpha = 0.5f)
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
            }
        },
        topBar = {
            Surface(
                color = LocalAppColors.current.surface,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 12.dp, bottom = 20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (currentStep > 1) currentStep-- else onBack()
                            },
                            modifier = Modifier.background(LocalAppColors.current.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LocalAppColors.current.textTitle)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Share Experience",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = LocalAppColors.current.textTitle
                            )
                            Text(
                                "Step $currentStep of 3",
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalAppColors.current.textSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Stepper
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepItem(
                            number = 1,
                            label = "Company",
                            isActive = currentStep == 1,
                            isCompleted = currentStep > 1,
                            onClick = { if (currentStep > 1) currentStep = 1 }
                        )
                        Box(modifier = Modifier.weight(1f).height(2.dp).background(if (currentStep > 1) LocalAppColors.current.primary else LocalAppColors.current.divider))
                        StepItem(
                            number = 2,
                            label = "Details",
                            isActive = currentStep == 2,
                            isCompleted = currentStep > 2,
                            onClick = { if (currentStep > 2) currentStep = 2 }
                        )
                        Box(modifier = Modifier.weight(1f).height(2.dp).background(if (currentStep > 2) LocalAppColors.current.primary else LocalAppColors.current.divider))
                        StepItem(
                            number = 3,
                            label = "Review",
                            isActive = currentStep == 3,
                            isCompleted = currentStep > 3,
                            onClick = { if (currentStep > 3) currentStep = 3 }
                        )
                    }
                }
            }
        },
        containerColor = LocalAppColors.current.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .navigationBarsPadding()
        ) {
            if (currentStep == 1) {
                // Step 1: Company Information
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = LocalAppColors.current.surface,
                    border = BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.5f)),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LocalAppColors.current.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Apartment, null, tint = LocalAppColors.current.primary)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Company Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
                                Text("Basic details about the interview", style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.textSecondary)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        LabelledDropdown(
                            label = "Company Name",
                            value = companyName,
                            placeholder = "Select Company",
                            options = companies.map { it.name },
                            onSelected = { companyName = it },
                            isMandatory = true,
                            isError = showStep1Errors && companyName.isEmpty()
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        LabelledTextField(
                            label = "Role Interviewed For",
                            value = role,
                            onValueChange = { role = it },
                            placeholder = "e.g. Software Engineer, Product Manager",
                            isMandatory = true,
                            isError = showStep1Errors && role.isEmpty()
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        val context = LocalContext.current
                        val calendar = Calendar.getInstance()
                        val datePickerDialog = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                interviewDate = "${month + 1}/$dayOfMonth/$year"
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )

                        LabelledTextField(
                            label = "Interview Date",
                            value = interviewDate,
                            onValueChange = { interviewDate = it },
                            placeholder = "MM/DD/YYYY",
                            isMandatory = true,
                            isError = showStep1Errors && interviewDate.isEmpty(),
                            trailingIcon = { 
                                IconButton(onClick = { datePickerDialog.show() }) {
                                    Icon(Icons.Outlined.CalendarToday, null, tint = LocalAppColors.current.primary, modifier = Modifier.size(20.dp))
                                }
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = LocalAppColors.current.surface,
                    border = BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.5f)),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LocalAppColors.current.surfaceVariant,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.HelpOutline, null, tint = LocalAppColors.current.textSecondary)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Process Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
                                Text("Context for the recruitment process", style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.textSecondary)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        LabelledDropdown(
                            label = "Interview Mode",
                            value = interviewMode,
                            placeholder = "Select mode...",
                            options = listOf("On-campus", "Off-campus", "Virtual"),
                            onSelected = { interviewMode = it },
                            isMandatory = true,
                            isError = showStep1Errors && interviewMode.isEmpty()
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        LabelledDropdown(
                            label = "Experience Level",
                            value = experienceLevel,
                            placeholder = "Select level...",
                            options = listOf("Internship", "Entry Level", "Mid Level", "Senior"),
                            onSelected = { experienceLevel = it },
                            isMandatory = true,
                            isError = showStep1Errors && experienceLevel.isEmpty()
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        LabelledDropdown(
                            label = "Outcome",
                            value = outcome,
                            placeholder = "Select outcome...",
                            options = listOf("Selected", "Waitlisted", "Not Selected"),
                            onSelected = { outcome = it },
                            isMandatory = true,
                            isError = showStep1Errors && outcome.isEmpty()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { 
                        if (validateStep1()) {
                            currentStep = 2 
                        } else {
                            showStep1Errors = true
                            Toast.makeText(context, "Please fill all mandatory fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Continue to Details", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = LocalAppColors.current.surface)
                }
            } else if (currentStep == 2) {
                // Step 2: Experience Details
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = LocalAppColors.current.primary.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, LocalAppColors.current.primary.copy(alpha = 0.1f)),
                    shadowElevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = LocalAppColors.current.primary, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Write Your Story", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LocalAppColors.current.primary)
                                Text("Share your journey and help others prepare.", style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.textSecondary)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = LocalAppColors.current.surface,
                    border = BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.5f)),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Overall Difficulty Level", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DifficultyChip("Easy", difficulty == "Easy") { difficulty = "Easy" }
                            DifficultyChip("Medium", difficulty == "Medium") { difficulty = "Medium" }
                            DifficultyChip("Hard", difficulty == "Hard") { difficulty = "Hard" }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Premium Categorized Sections
                EditSectionCard(
                    title = "About the Author/Brief",
                    icon = Icons.Outlined.Person,
                    containerColor = LocalAppColors.current.primary,
                    contentColor = Color.White,
                    value = background,
                    onValueChange = { background = it },
                    placeholder = "Tell us about your profile and preparation level..."
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                EditSectionCard(
                    title = "How I got the interview",
                    icon = Icons.Outlined.Bolt,
                    containerColor = LocalAppColors.current.warning,
                    contentColor = Color.White,
                    value = howGotInterview,
                    onValueChange = { howGotInterview = it },
                    placeholder = "Referral, LinkedIn, Career Portal, etc."
                )

                Spacer(modifier = Modifier.height(16.dp))

                EditSectionCard(
                    title = "Interview Process Breakdown",
                    icon = Icons.Outlined.Timeline,
                    containerColor = LocalAppColors.current.purple,
                    contentColor = Color.White,
                    value = processBreakdown,
                    onValueChange = { processBreakdown = it },
                    placeholder = "Round 1: Screening\nRound 2: Technical Interview"
                )

                Spacer(modifier = Modifier.height(16.dp))

                EditSectionCard(
                    title = "My Detailed Experience",
                    icon = Icons.Outlined.Article,
                    containerColor = LocalAppColors.current.textTitle,
                    contentColor = Color.White,
                    value = myExperience,
                    onValueChange = { myExperience = it },
                    placeholder = "Describe each round and your overall feel..."
                )

                Spacer(modifier = Modifier.height(16.dp))

                EditSectionCard(
                    title = "Questions Asked",
                    icon = Icons.Outlined.HelpOutline,
                    containerColor = LocalAppColors.current.primary,
                    contentColor = Color.White,
                    value = questionsAsked,
                    onValueChange = { questionsAsked = it },
                    placeholder = "List technical or behavioral questions (one per line)..."
                )

                Spacer(modifier = Modifier.height(16.dp))

                EditSectionCard(
                    title = "Mistakes I Made",
                    icon = Icons.Outlined.Warning,
                    containerColor = LocalAppColors.current.error,
                    contentColor = Color.White,
                    value = mistakesMade,
                    onValueChange = { mistakesMade = it },
                    placeholder = "What would you do differently next time? (one per line)..."
                )

                Spacer(modifier = Modifier.height(16.dp))

                EditSectionCard(
                    title = "Preparation Strategy",
                    icon = Icons.Outlined.School,
                    containerColor = LocalAppColors.current.success,
                    contentColor = Color.White,
                    value = prepStrategy,
                    onValueChange = { prepStrategy = it },
                    placeholder = "Java: LeetCode\nSystem Design: Grokking the System Design"
                )

                Spacer(modifier = Modifier.height(16.dp))

                EditSectionCard(
                    title = "Final Advice & Conclusion",
                    icon = Icons.Outlined.VolunteerActivism,
                    containerColor = LocalAppColors.current.purple,
                    contentColor = Color.White,
                    value = finalAdvice,
                    onValueChange = { finalAdvice = it },
                    placeholder = "Tips for future candidates (one per line)..."
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { currentStep = 1 },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.divider),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Back", color = LocalAppColors.current.textTitle, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { 
                            if (validateStep2()) {
                                currentStep = 3 
                            } else {
                                showStep2Errors = true
                                Toast.makeText(context, "Please fill all mandatory fields", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1.5f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Review & Submit", color = LocalAppColors.current.surface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else if (currentStep == 3) {
                // Step 3: Review & Submit
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = LocalAppColors.current.success.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, LocalAppColors.current.success.copy(alpha = 0.2f)),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CheckCircle, null, tint = LocalAppColors.current.success)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Almost Done!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LocalAppColors.current.success)
                            Text(
                                "Your experience will be reviewed by admins before going live.",
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalAppColors.current.success.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = LocalAppColors.current.surface,
                    border = BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.5f)),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Basic Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        SummaryRow("Company", companyName.ifEmpty { "---" })
                        SummaryRow("Role", role.ifEmpty { "---" })
                        SummaryRow("Difficulty", difficulty, isChip = true)
                        SummaryRow("Outcome", outcome.ifEmpty { "---" })
                        SummaryRow("Work Mode", interviewMode)
                        SummaryRow("Candidate Type", experienceLevel)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Detailed Sections Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
                Spacer(modifier = Modifier.height(16.dp))

                val completedSections = mutableListOf<Triple<String, String, androidx.compose.ui.graphics.vector.ImageVector>>()
                if (background.isNotEmpty()) completedSections.add(Triple("Your Background", background, Icons.Outlined.Person))
                if (howGotInterview.isNotEmpty()) completedSections.add(Triple("How I Got Interview", howGotInterview, Icons.Outlined.Bolt))
                if (processBreakdown.isNotEmpty()) completedSections.add(Triple("Process Breakdown", processBreakdown, Icons.Outlined.Timeline))
                if (myExperience.isNotEmpty()) completedSections.add(Triple("My Experience", myExperience, Icons.Outlined.Article))
                if (questionsAsked.isNotEmpty()) completedSections.add(Triple("Questions Asked", questionsAsked, Icons.Outlined.HelpOutline))
                if (mistakesMade.isNotEmpty()) completedSections.add(Triple("Mistakes Made", mistakesMade, Icons.Outlined.Warning))
                if (prepStrategy.isNotEmpty()) completedSections.add(Triple("Preparation Strategy", prepStrategy, Icons.Outlined.School))
                if (finalAdvice.isNotEmpty()) completedSections.add(Triple("Final Advice", finalAdvice, Icons.Outlined.VolunteerActivism))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    completedSections.forEach { (title, content, icon) ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = LocalAppColors.current.surface,
                            border = BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.5f)),
                            onClick = {
                                previewSectionTitle = title
                                previewSectionContent = content
                                showSectionPreview = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = LocalAppColors.current.primary.copy(alpha = 0.08f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(icon, null, tint = LocalAppColors.current.primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
                                    Text(
                                        content.take(80).let { if (it.length < content.length) "$it..." else it },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LocalAppColors.current.textSecondary,
                                        maxLines = 1
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = LocalAppColors.current.textSecondary.copy(alpha = 0.5f))
                            }
                        }
                    }
                    if (completedSections.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = LocalAppColors.current.surfaceVariant.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "No detailed sections provided",
                                modifier = Modifier.padding(24.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = LocalAppColors.current.textSecondary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "By submitting, you confirm that this is a genuine interview experience and follows our community guidelines. Your contribution helps thousands of students prepare better.",
                    fontSize = 12.sp,
                    color = LocalAppColors.current.iconTint,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { currentStep = 2 },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.divider),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Back", color = LocalAppColors.current.textTitle, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { 
                            val selectedCompany = companies.find { it.name == companyName }
                            if (selectedCompany != null) {
                                isSubmitting = true
                                scope.launch {
                                    try {
                                        // Parse helper functions
                                        val parsedRounds = processBreakdown.split("\n").filter { it.isNotBlank() }.map { line ->
                                            val parts = line.split(":", limit = 2)
                                            ProcessStep(parts[0].trim(), if (parts.size > 1) parts[1].trim() else "N/A")
                                        }
                                        
                                        val parsedPrep = prepStrategy.split("\n").filter { it.isNotBlank() }.mapNotNull { line ->
                                            val parts = line.split(":", limit = 2)
                                            if (parts.size == 2) {
                                                parts[0].trim() to parts[1].split(",").map { it.trim() }.filter { it.isNotBlank() }
                                            } else {
                                                "General" to listOf(line.trim())
                                            }
                                        }.groupBy({ it.first }, { it.second }).mapValues { it.value.flatten() }

                                        val request = ExperienceRequest(
                                            companyId = selectedCompany.id,
                                            userRole = role,
                                            difficulty = difficulty,
                                            isSelected = outcome == "Selected",
                                            workMode = interviewMode,
                                            candidateType = experienceLevel,
                                            myExperience = myExperience,
                                            brief = background,
                                            applicationProcess = howGotInterview,
                                            interviewRounds = if (parsedRounds.isNotEmpty()) parsedRounds else null,
                                            technicalQuestions = if (questionsAsked.isNotBlank()) questionsAsked.split("\n").filter { it.isNotBlank() } else null,
                                            behavioralQuestions = emptyList(),
                                            mistakes = if (mistakesMade.isNotBlank()) mistakesMade.split("\n").filter { it.isNotBlank() } else null,
                                            preparationStrategy = if (parsedPrep.isNotEmpty()) parsedPrep else null,
                                            finalAdvice = if (finalAdvice.isNotBlank()) finalAdvice.split("\n").filter { it.isNotBlank() } else null
                                        )
                                        val response = withContext(Dispatchers.IO) {
                                            RetrofitClient.apiService.submitExperience(request)
                                        }
                                        if (response.isSuccessful) {
                                            currentStep = 4
                                        } else {
                                            Toast.makeText(context, "Submission failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Please select a valid company", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1.5f).height(56.dp),
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Submit Experience", color = LocalAppColors.current.surface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else if (currentStep == 4) {
                // Success Step
                Column(
                    modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = LocalAppColors.current.success.copy(alpha = 0.1f),
                        modifier = Modifier.size(120.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, null, tint = LocalAppColors.current.success, modifier = Modifier.size(60.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        "Contribution Submitted!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "Your experience has been sent for review. It will be live once accepted by the admin team.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = LocalAppColors.current.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        lineHeight = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Back to Dashboard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { currentStep = 1 }, // Reset for another post
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, LocalAppColors.current.primary)
                    ) {
                        Text("Share Another Experience", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LocalAppColors.current.primary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
        }

        // Section Preview Bottom Sheet
        if (showSectionPreview) {
            ModalBottomSheet(
                onDismissRequest = { showSectionPreview = false },
                containerColor = LocalAppColors.current.surface,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                dragHandle = { BottomSheetDefaults.DragHandle(color = LocalAppColors.current.divider) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        previewSectionTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = LocalAppColors.current.background,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.5f))
                    ) {
                        Text(
                            previewSectionContent,
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = LocalAppColors.current.textBody,
                            lineHeight = 26.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { showSectionPreview = false },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Close Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun StepItem(number: Int, label: String, isActive: Boolean, isCompleted: Boolean, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = isCompleted || isActive) { onClick() }
    ) {
        Surface(
            shape = CircleShape,
            color = if (isActive || isCompleted) LocalAppColors.current.primary else LocalAppColors.current.divider.copy(alpha = 0.5f),
            modifier = Modifier.size(36.dp),
            border = if (isActive) BorderStroke(4.dp, LocalAppColors.current.primaryHighlight) else null
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text(
                        text = number.toString(),
                        color = if (isActive) Color.White else LocalAppColors.current.textSecondary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) LocalAppColors.current.primary else LocalAppColors.current.textSecondary,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isMandatory: Boolean = false,
    isError: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isError) LocalAppColors.current.error else LocalAppColors.current.textTitle,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (isMandatory) {
                Text(" *", color = LocalAppColors.current.error, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = LocalAppColors.current.textSecondary.copy(alpha = 0.5f), style = MaterialTheme.typography.bodyLarge) },
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(16.dp),
            isError = isError,
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LocalAppColors.current.primary,
                unfocusedBorderColor = if (isError) LocalAppColors.current.error else LocalAppColors.current.divider.copy(alpha = 0.5f),
                errorBorderColor = LocalAppColors.current.error,
                unfocusedContainerColor = LocalAppColors.current.background,
                focusedContainerColor = LocalAppColors.current.background
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelledDropdown(
    label: String,
    value: String,
    placeholder: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    isMandatory: Boolean = false,
    isError: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isError) LocalAppColors.current.error else LocalAppColors.current.textTitle,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (isMandatory) {
                Text(" *", color = LocalAppColors.current.error, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isError) LocalAppColors.current.error else if (expanded) LocalAppColors.current.primary else LocalAppColors.current.divider.copy(alpha = 0.5f)),
                color = LocalAppColors.current.background
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (value.isEmpty()) placeholder else value,
                        color = if (value.isEmpty()) LocalAppColors.current.textSecondary.copy(alpha = 0.5f) else LocalAppColors.current.textTitle,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Icon(
                        if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        null,
                        tint = if (isError) LocalAppColors.current.error else LocalAppColors.current.textSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(LocalAppColors.current.surface)
            ) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption, style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            onSelected(selectionOption)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
fun DifficultyChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = when (text) {
        "Easy" -> if (isSelected) LocalAppColors.current.success.copy(alpha = 0.15f) else LocalAppColors.current.background
        "Medium" -> if (isSelected) LocalAppColors.current.warning.copy(alpha = 0.15f) else LocalAppColors.current.background
        "Hard" -> if (isSelected) LocalAppColors.current.error.copy(alpha = 0.15f) else LocalAppColors.current.background
        else -> LocalAppColors.current.background
    }
    val borderColor = when (text) {
        "Easy" -> if (isSelected) LocalAppColors.current.success else LocalAppColors.current.divider.copy(alpha = 0.5f)
        "Medium" -> if (isSelected) LocalAppColors.current.warning else LocalAppColors.current.divider.copy(alpha = 0.5f)
        "Hard" -> if (isSelected) LocalAppColors.current.error else LocalAppColors.current.divider.copy(alpha = 0.5f)
        else -> LocalAppColors.current.divider.copy(alpha = 0.5f)
    }
    val textColor = when (text) {
        "Easy" -> if (isSelected) LocalAppColors.current.success else LocalAppColors.current.textSecondary
        "Medium" -> if (isSelected) LocalAppColors.current.warning else LocalAppColors.current.textSecondary
        "Hard" -> if (isSelected) LocalAppColors.current.error else LocalAppColors.current.textSecondary
        else -> LocalAppColors.current.textSecondary
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.height(52.dp).width(100.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        color = backgroundColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = textColor, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isChip: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = LocalAppColors.current.textSecondary)
        if (isChip) {
            val bgColor = when (value) {
                "Easy" -> LocalAppColors.current.success.copy(alpha = 0.1f)
                "Medium" -> LocalAppColors.current.warning.copy(alpha = 0.1f)
                "Hard" -> LocalAppColors.current.error.copy(alpha = 0.1f)
                else -> LocalAppColors.current.divider.copy(alpha = 0.5f)
            }
            val textColor = when (value) {
                "Easy" -> LocalAppColors.current.success
                "Medium" -> LocalAppColors.current.warning
                "Hard" -> LocalAppColors.current.error
                else -> LocalAppColors.current.textBody
            }
            Surface(
                color = bgColor,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, textColor.copy(alpha = 0.2f))
            ) {
                Text(
                    value,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val spacingPx = mainAxisSpacing.roundToPx()
        val crossSpacingPx = crossAxisSpacing.roundToPx()
        
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        
        placeables.forEach { placeable ->
            if (currentRowWidth + placeable.width + (if (currentRow.isEmpty()) 0 else spacingPx) > constraints.maxWidth) {
                rows.add(currentRow)
                currentRow = mutableListOf(placeable)
                currentRowWidth = placeable.width
            } else {
                currentRowWidth += placeable.width + (if (currentRow.isEmpty()) 0 else spacingPx)
                currentRow.add(placeable)
            }
        }
        rows.add(currentRow)
        
        val totalHeight = rows.sumOf { it.maxOfOrNull { p -> p.height } ?: 0 } + (rows.size - 1) * crossSpacingPx
        
        layout(constraints.maxWidth, if (totalHeight > 0) totalHeight else 0) {
            var yOffset = 0
            rows.forEach { row ->
                var xOffset = 0
                val rowHeight = row.maxOfOrNull { it.height } ?: 0
                row.forEach { placeable ->
                    placeable.placeRelative(xOffset, yOffset)
                    xOffset += placeable.width + spacingPx
                }
                yOffset += rowHeight + crossSpacingPx
            }
        }
    }
}
