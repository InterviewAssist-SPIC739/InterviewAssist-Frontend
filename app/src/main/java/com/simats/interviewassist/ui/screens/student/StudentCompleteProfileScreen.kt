package com.simats.interviewassist.ui.screens.student

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
fun StudentCompleteProfileScreen(
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
                // Fallback to navigation anyway
                onSkip()
            }
        }
    }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isLoading by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    var phoneNumber by remember { mutableStateOf("+91 ") }
    var major by remember { mutableStateOf("") }
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
    
    var expGradYearExpanded by remember { mutableStateOf(false) }
    var selectedExpGradYear by remember { mutableStateOf("") }
    val gradYears = listOf("2024", "2025", "2026", "2027", "2028")

    var currentYearExpanded by remember { mutableStateOf(false) }
    var selectedCurrentYear by remember { mutableStateOf("") }
    val currentYears = listOf("1st Year", "2nd Year", "3rd Year", "Final Year")

    // Validation State
    val phoneRegex = Regex("^\\+91 [6-9][0-9]{9}$")
    val phoneDigits = if (phoneNumber.length > 4) phoneNumber.substring(4) else ""
    val isPhoneFormatInvalid = phoneDigits.isNotEmpty() && !phoneDigits[0].let { it in '6'..'9' }
    val isPhoneComplete = phoneRegex.matches(phoneNumber.trim())
    
    val phoneNumberError = when {
        isPhoneFormatInvalid -> "Enter a valid phone number (+91 XXXXXXXXXX)"
        showErrors && !isPhoneComplete -> {
            if (phoneNumber == "+91 ") "Phone number is required" 
            else "Enter a valid phone number (+91 XXXXXXXXXX)"
        }
        else -> null
    }
    
    val majorError = if (showErrors && major.isBlank()) "Major is required" else null
    val expGradYearError = if (showErrors && selectedExpGradYear.isBlank()) "Please select expected graduation year" else null
    val currentYearError = if (showErrors && selectedCurrentYear.isBlank()) "Please select your current year" else null

    fun validateAndComplete() {
        showErrors = true
        
        if (isPhoneComplete && major.isNotBlank() && 
            selectedExpGradYear.isNotBlank() && selectedCurrentYear.isNotBlank()) {
            isLoading = true
            scope.launch {
                try {
                    val request = ProfileRequest(
                        userId = userId,
                        phoneNumber = phoneNumber,
                        major = major,
                        expectedGradYear = selectedExpGradYear,
                        currentYear = selectedCurrentYear,
                        bio = bio,
                        profilePic = base64Image
                    )
                    val response = RetrofitClient.apiService.completeProfile(request)
                    isLoading = false
                    
                    if (response.isSuccessful) {
                        preferenceManager.setProfileCompleted(userId)
                        ProfilePicManager.saveBase64Image(context, base64Image, preferenceManager)
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

            // App Logo
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = LocalAppColors.current.primary)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LocalAppColors.current.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
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
                text = "Tell us about yourself",
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
                    placeholder = "+91 1234567890",
                    isError = phoneNumberError != null,
                    errorMessage = phoneNumberError
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProfileLabel("Major / Department")
                ProfileTextField(
                    value = major,
                    onValueChange = { 
                        major = it
                    },
                    placeholder = "e.g. Computer Science",
                    isError = majorError != null,
                    errorMessage = majorError
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProfileLabel("Expected Graduation Year")
                ExposedDropdownMenuBox(
                    expanded = expGradYearExpanded,
                    onExpandedChange = { expGradYearExpanded = !expGradYearExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedExpGradYear,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select year", color = LocalAppColors.current.iconTint) },
                        trailingIcon = {
                            if (expGradYearError != null) {
                                Icon(Icons.Default.Error, "Error", tint = LocalAppColors.current.error)
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expGradYearExpanded)
                            }
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = expGradYearError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LocalAppColors.current.textTitle,
                            unfocusedTextColor = LocalAppColors.current.textTitle,
                            unfocusedBorderColor = LocalAppColors.current.divider,
                            focusedBorderColor = LocalAppColors.current.primary,
                            errorBorderColor = LocalAppColors.current.error,
                            unfocusedContainerColor = LocalAppColors.current.surfaceVariant,
                            focusedContainerColor = LocalAppColors.current.surfaceVariant,
                            errorContainerColor = LocalAppColors.current.errorBg
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expGradYearExpanded,
                        onDismissRequest = { expGradYearExpanded = false }
                    ) {
                        gradYears.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    selectedExpGradYear = selectionOption
                                    expGradYearExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
                AnimatedErrorText(expGradYearError)

                Spacer(modifier = Modifier.height(24.dp))

                ProfileLabel("Current Year")
                ExposedDropdownMenuBox(
                    expanded = currentYearExpanded,
                    onExpandedChange = { currentYearExpanded = !currentYearExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCurrentYear,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select year", color = LocalAppColors.current.iconTint) },
                        trailingIcon = {
                            if (currentYearError != null) {
                                Icon(Icons.Default.Error, "Error", tint = LocalAppColors.current.error)
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = currentYearExpanded)
                            }
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = currentYearError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LocalAppColors.current.textTitle,
                            unfocusedTextColor = LocalAppColors.current.textTitle,
                            unfocusedBorderColor = LocalAppColors.current.divider,
                            focusedBorderColor = LocalAppColors.current.primary,
                            errorBorderColor = LocalAppColors.current.error,
                            unfocusedContainerColor = LocalAppColors.current.surfaceVariant,
                            focusedContainerColor = LocalAppColors.current.surfaceVariant,
                            errorContainerColor = LocalAppColors.current.errorBg
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = currentYearExpanded,
                        onDismissRequest = { currentYearExpanded = false }
                    ) {
                        currentYears.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    selectedCurrentYear = selectionOption
                                    currentYearExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
                AnimatedErrorText(currentYearError)

                Spacer(modifier = Modifier.height(24.dp))

                ProfileLabel("About Me")
                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 200) bio = it },
                    placeholder = { Text("Tell us about your interests and career goals...", color = LocalAppColors.current.iconTint) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LocalAppColors.current.textTitle,
                        unfocusedTextColor = LocalAppColors.current.textTitle,
                        unfocusedBorderColor = LocalAppColors.current.divider,
                        focusedBorderColor = LocalAppColors.current.primary,
                        unfocusedContainerColor = LocalAppColors.current.surfaceVariant,
                        focusedContainerColor = LocalAppColors.current.surfaceVariant
                    )
                )
                Text(
                    text = "${bio.length}/200",
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
            placeholder = { Text(placeholder, color = LocalAppColors.current.iconTint) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = LocalAppColors.current.textTitle,
                unfocusedTextColor = LocalAppColors.current.textTitle,
                errorTextColor = LocalAppColors.current.textTitle,
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
