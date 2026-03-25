package com.simats.interviewassist.ui.screens.student

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.*
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.data.models.CompanyResponse
import com.simats.interviewassist.data.network.RetrofitClient
import kotlinx.coroutines.launch


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StudentAskQuestionScreen(
    companyName: String = "Cognizant", // Default value
    onBack: () -> Unit,
    onPostSuccess: () -> Unit = {}
) {
    var questionText by remember { mutableStateOf("") }
    val maxChars = 500
    var selectedCompany by remember { mutableStateOf<CompanyResponse?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var isPosting by remember { mutableStateOf(false) }
    var isLoadingCompanies by remember { mutableStateOf(false) }
    var companies by remember { mutableStateOf<List<CompanyResponse>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        isLoadingCompanies = true
        try {
            val response = RetrofitClient.apiService.getCompanies()
            if (response.isSuccessful) {
                companies = response.body() ?: emptyList()
                selectedCompany = companies.find { it.name.equals(companyName, ignoreCase = true) } 
                    ?: companies.firstOrNull()
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load companies"
        } finally {
            isLoadingCompanies = false
        }
    }

    val suggestedQuestions = listOf(
        "What's the interview process like?",
        "How should I prepare for the technical round?",
        "What's the work culture like?",
        "Do they offer remote work options?"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ask a Question",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LocalAppColors.current.textTitle)
                    }
                }
            )
        },
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Snackbar(data)
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
                .padding(24.dp)
        ) {
            if (errorMessage != null) {
                Surface(
                    color = LocalAppColors.current.errorBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        color = LocalAppColors.current.error,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Info Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(LocalAppColors.current.primaryHighlight, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.HelpOutline,
                            contentDescription = null,
                            tint = LocalAppColors.current.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Get answers from alumni",
                            fontWeight = FontWeight.Bold,
                            color = LocalAppColors.current.textTitle,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Your question will be visible to verified alumni who have worked at the selected company. They'll share their real experiences to help you.",
                            color = LocalAppColors.current.primary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Content Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Company Selector
                    Text(
                        "Select Company",
                        fontSize = 16.sp,
                        color = LocalAppColors.current.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { if (!isLoadingCompanies) expanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCompany?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Select Company", color = LocalAppColors.current.iconTint) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            leadingIcon = { 
                                if (isLoadingCompanies) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Outlined.Business, null, tint = LocalAppColors.current.primary)
                                }
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LocalAppColors.current.primary,
                                unfocusedBorderColor = LocalAppColors.current.divider,
                                unfocusedContainerColor = LocalAppColors.current.surfaceVariant.copy(alpha = 0.3f),
                                focusedContainerColor = LocalAppColors.current.surfaceVariant.copy(alpha = 0.3f),
                                focusedTextColor = LocalAppColors.current.textTitle,
                                unfocusedTextColor = LocalAppColors.current.textTitle
                            )
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(LocalAppColors.current.surface)
                        ) {
                            companies.forEach { company ->
                                DropdownMenuItem(
                                    text = { Text(company.name) },
                                    onClick = {
                                        selectedCompany = company
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Question Input
                    Text(
                        "Your Question",
                        fontSize = 16.sp,
                        color = LocalAppColors.current.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { if (it.length <= maxChars) questionText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = {
                            Text(
                                "e.g. How many technical rounds are there in the interview process?",
                                color = LocalAppColors.current.iconTint,
                                fontSize = 16.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.RateReview, null, tint = LocalAppColors.current.primary)
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LocalAppColors.current.primary,
                            unfocusedBorderColor = LocalAppColors.current.divider,
                            unfocusedContainerColor = LocalAppColors.current.surfaceVariant.copy(alpha = 0.3f),
                            focusedContainerColor = LocalAppColors.current.surfaceVariant.copy(alpha = 0.3f),
                            focusedTextColor = LocalAppColors.current.textTitle,
                            unfocusedTextColor = LocalAppColors.current.textTitle
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                    )

                    // Char Count
                    Text(
                        text = "${questionText.length}/$maxChars",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        fontSize = 13.sp,
                        color = LocalAppColors.current.iconTint
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Suggested Questions
            Text(
                "Suggested Questions",
                fontSize = 14.sp,
                color = LocalAppColors.current.textSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestedQuestions.forEach { suggestion ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = LocalAppColors.current.surface,
                        modifier = Modifier.clickable { questionText = suggestion }
                    ) {
                        Text(
                            text = suggestion,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            fontSize = 14.sp,
                            color = LocalAppColors.current.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Post Button
            Button(
                onClick = {
                    if (selectedCompany != null && questionText.isNotBlank()) {
                        isPosting = true
                        errorMessage = null
                        scope.launch {
                            try {
                                val response = RetrofitClient.apiService.askQuestion(
                                    selectedCompany!!.id,
                                    mapOf("question_text" to questionText)
                                )
                                if (response.isSuccessful) {
                                    showSuccessDialog = true
                                } else {
                                    val errorMsg = "Failed to post question: ${response.message()}"
                                    errorMessage = errorMsg
                                    snackbarHostState.showSnackbar(errorMsg)
                                }
                            } catch (e: Exception) {
                                val errorMsg = "Error: ${e.message}"
                                errorMessage = errorMsg
                                snackbarHostState.showSnackbar(errorMsg)
                            } finally {
                                isPosting = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
                shape = RoundedCornerShape(12.dp),
                enabled = questionText.isNotEmpty() && !isPosting && selectedCompany != null
            ) {
                if (isPosting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = LocalAppColors.current.surface)
                } else {
                    Text(
                        "Post Question",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = LocalAppColors.current.surface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Please keep your questions professional and relevant to get the best responses from alumni.",
                fontSize = 13.sp,
                color = LocalAppColors.current.iconTint,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 18.sp
            )
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { /* Don't dismiss by clicking outside */ },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onPostSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Back to Home", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Question Posted",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalAppColors.current.textTitle
                        )
                    }
                },
                text = {
                    Text(
                        "Your question was submitted successfully and is now visible to alumni. Good luck!",
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        color = LocalAppColors.current.textSecondary,
                        lineHeight = 22.sp
                    )
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = LocalAppColors.current.surface
            )
        }
    }
}
