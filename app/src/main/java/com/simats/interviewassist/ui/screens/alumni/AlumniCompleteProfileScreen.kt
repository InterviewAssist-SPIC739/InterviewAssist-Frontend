package com.simats.interviewassist.ui.screens.alumni

import androidx.compose.animation.*
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.*
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.utils.PreferenceManager
import com.simats.interviewassist.utils.ProfilePicManager
import com.simats.interviewassist.data.models.ProfileRequest
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import coil.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniCompleteProfileScreen(
    userId: Int, 
    preferenceManager: PreferenceManager,
    onComplete: () -> Unit, 
    onSkip: () -> Unit
) {
    val scope = rememberCoroutineScope()

    fun handleSkip() {
        scope.launch {
            try {
                RetrofitClient.apiService.skipProfile(mapOf("user_id" to userId))
                preferenceManager.setProfileSkipped(userId, true)
                onSkip()
            } catch (e: Exception) {
                // Fallback to navigation
                onSkip()
            }
        }
    }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isLoading by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    var phoneNumber by remember { mutableStateOf("+91 ") }
    var currentCompany by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var linkedinProfile by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var base64Image by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            // Convert to base64
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                    val byteArray = outputStream.toByteArray()
                    base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)
                } catch (e: Exception) {
                    Toast.makeText(context, "Error processing image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    var gradYearExpanded by remember { mutableStateOf(false) }
    var selectedGradYear by remember { mutableStateOf("") }
    val gradYears = (2000..2024).map { it.toString() }.reversed()

    // Validation State
    val phoneRegex = Regex("^\\+91 [6-9][0-9]{9}$")
    val linkedinRegex = Regex("^(https?://)?(www\\.)?linkedin\\.com/in/[a-zA-Z0-9_-]+/?$")
    
    val phoneDigits = if (phoneNumber.length > 4) phoneNumber.substring(4) else ""
    val isPhoneFormatInvalid = phoneDigits.isNotEmpty() && !phoneDigits[0].let { it in '6'..'9' }
    val isPhoneComplete = phoneRegex.matches(phoneNumber.trim())
    val isLinkedinValid = linkedinProfile.isEmpty() || linkedinRegex.matches(linkedinProfile.trim())

    val phoneNumberError = when {
        isPhoneFormatInvalid -> "Enter a valid phone number (+91 XXXXXXXXXX)"
        showErrors && !isPhoneComplete -> {
            if (phoneNumber == "+91 ") "Phone number is required" 
            else "Enter a valid phone number (+91 XXXXXXXXXX)"
        }
        else -> null
    }
    
    val currentCompanyError = if (showErrors && currentCompany.isBlank()) "Current company is required" else null
    val designationError = if (showErrors && designation.isBlank()) "Designation is required" else null
    val gradYearError = if (showErrors && selectedGradYear.isBlank()) "Please select your graduation year" else null
    val specializationError = if (showErrors && specialization.isBlank()) "Area of specialization is required" else null
    val linkedinProfileError = if (!isLinkedinValid) "Enter a valid LinkedIn URL" else null

    fun validateAndComplete() {
        showErrors = true

        if (isPhoneComplete && currentCompany.isNotBlank() && 
            designation.isNotBlank() && selectedGradYear.isNotBlank() &&
            specialization.isNotBlank() && isLinkedinValid) {
            isLoading = true
            scope.launch {
                try {
                    val request = ProfileRequest(
                        userId = userId,
                        phoneNumber = phoneNumber,
                        major = null,
                        expectedGradYear = selectedGradYear,
                        currentYear = "Alumni",
                        bio = bio,
                        profilePic = base64Image,
                        linkedinUrl = linkedinProfile,
                        currentCompany = currentCompany,
                        designation = designation,
                        specialization = specialization
                    )
                    val response = RetrofitClient.apiService.completeProfile(request)
                    isLoading = false
                    
                    if (response.isSuccessful) {
                        ProfilePicManager.saveBase64Image(context, base64Image, preferenceManager)
                        preferenceManager.setProfileCompleted(userId)
                        Toast.makeText(context, "Profile completed!", Toast.LENGTH_SHORT).show()
                        onComplete()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMsg = try {
                            if (errorBody != null) {
                                JSONObject(errorBody).getString("error")
                            } else "Update failed"
                        } catch (e: Exception) {
                            errorBody ?: "Update failed"
                        }
                        snackbarHostState.showSnackbar(errorMsg)
                    }
                } catch (e: Exception) {
                    isLoading = false
                    snackbarHostState.showSnackbar("Connection error: ${e.message}")
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Please correct the errors in the form")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LocalAppColors.current.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // App Logo Icon (Briefcase)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = LocalAppColors.current.primary)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LocalAppColors.current.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = null,
                    tint = LocalAppColors.current.surface,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Complete Your Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = LocalAppColors.current.textTitle,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Help students by sharing your journey",
                style = MaterialTheme.typography.bodyLarge,
                color = LocalAppColors.current.textBody,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.size(120.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(LocalAppColors.current.primaryHighlight),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "P", 
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalAppColors.current.primary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LocalAppColors.current.primary)
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Add Photo",
                        tint = LocalAppColors.current.surface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (selectedImageUri == null) "Add Photo" else "Change Photo",
                style = MaterialTheme.typography.labelLarge,
                color = LocalAppColors.current.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(40.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                ProfileLabel("Phone Number")
                ProfileTextField(
                    value = phoneNumber,
                    onValueChange = { input ->
                        if (input.startsWith("+91 ")) {
                            val digitsOnly = input.substring(4).filter { it.isDigit() }
                            if (digitsOnly.length <= 10) {
                                phoneNumber = "+91 " + digitsOnly
                            }
                        }
                    },
                    placeholder = "+1 (555) 000-0000",
                    isError = phoneNumberError != null,
                    errorMessage = phoneNumberError
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProfileLabel("Current Company")
                ProfileTextField(
                    value = currentCompany,
                    onValueChange = { 
                        currentCompany = it
                    },
                    placeholder = "e.g. Google",
                    isError = currentCompanyError != null,
                    errorMessage = currentCompanyError
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProfileLabel("Designation")
                ProfileTextField(
                    value = designation,
                    onValueChange = { 
                        designation = it
                    },
                    placeholder = "e.g. Software Engineer",
                    isError = designationError != null,
                    errorMessage = designationError
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProfileLabel("Graduation Year")
                ExposedDropdownMenuBox(
                    expanded = gradYearExpanded,
                    onExpandedChange = { gradYearExpanded = !gradYearExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedGradYear,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select year", color = LocalAppColors.current.borderLight) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = gradYearError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = LocalAppColors.current.divider,
                            focusedBorderColor = LocalAppColors.current.primary,
                            errorBorderColor = LocalAppColors.current.error,
                            unfocusedContainerColor = LocalAppColors.current.surfaceVariant,
                            focusedContainerColor = LocalAppColors.current.surfaceVariant,
                            errorContainerColor = LocalAppColors.current.errorBg
                        ),
                        trailingIcon = {
                            if (gradYearError != null) {
                                Icon(Icons.Default.Error, "Error", tint = LocalAppColors.current.error)
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradYearExpanded)
                            }
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = gradYearExpanded,
                        onDismissRequest = { gradYearExpanded = false }
                    ) {
                        gradYears.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    selectedGradYear = selectionOption
                                    gradYearExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
                AnimatedErrorText(gradYearError)

                Spacer(modifier = Modifier.height(24.dp))

                ProfileLabel("Area of Specialization")
                ProfileTextField(
                    value = specialization,
                    onValueChange = { specialization = it },
                    placeholder = "e.g. Machine Learning, Web Development",
                    isError = specializationError != null,
                    errorMessage = specializationError
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProfileLabel("LinkedIn Profile (Optional)")
                ProfileTextField(
                    value = linkedinProfile,
                    onValueChange = { 
                        linkedinProfile = it
                    },
                    placeholder = "linkedin.com/in/yourprofile",
                    isError = linkedinProfileError != null,
                    errorMessage = linkedinProfileError
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProfileLabel("Brief Bio")
                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 300) bio = it },
                    placeholder = { Text("Tell students about your journey and what you can help with...", color = LocalAppColors.current.borderLight) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = LocalAppColors.current.divider,
                        focusedBorderColor = LocalAppColors.current.primary,
                        unfocusedContainerColor = LocalAppColors.current.surfaceVariant,
                        focusedContainerColor = LocalAppColors.current.surfaceVariant
                    )
                )
                Text(
                    text = "${bio.length}/300",
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalAppColors.current.textBody.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Complete Profile Button
            Button(
                onClick = { if (!isLoading) validateAndComplete() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = LocalAppColors.current.surface,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Complete Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.surface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Skip Button
            TextButton(
                onClick = { handleSkip() },
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Skip for now",
                    style = MaterialTheme.typography.labelLarge,
                    color = LocalAppColors.current.textBody,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// Re-using common components for consistent look
@Composable
fun ProfileLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = LocalAppColors.current.textTitle,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = LocalAppColors.current.borderLight) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = LocalAppColors.current.divider,
                focusedBorderColor = LocalAppColors.current.primary,
                errorBorderColor = LocalAppColors.current.error,
                unfocusedContainerColor = LocalAppColors.current.surfaceVariant,
                focusedContainerColor = LocalAppColors.current.surfaceVariant,
                errorContainerColor = LocalAppColors.current.errorBg
            ),
            trailingIcon = {
                if (isError) {
                    Icon(Icons.Default.Error, "Error", tint = LocalAppColors.current.error)
                }
            },
            singleLine = true
        )
        AnimatedErrorText(errorMessage)
    }
}

@Composable
fun AnimatedErrorText(errorMessage: String?) {
    AnimatedVisibility(
        visible = errorMessage != null,
        enter = fadeIn() + expandVertically()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = LocalAppColors.current.error,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = errorMessage ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = LocalAppColors.current.error
            )
        }
    }
}
