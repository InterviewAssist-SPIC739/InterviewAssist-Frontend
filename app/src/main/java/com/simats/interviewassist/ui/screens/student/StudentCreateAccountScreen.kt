package com.simats.interviewassist.ui.screens.student

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.*
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.data.models.RegisterRequest
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCreateAccountScreen(
    initialRole: String? = null,
    preferenceManager: com.simats.interviewassist.utils.PreferenceManager,
    onBack: () -> Unit,
    onContinue: (String, Int) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }

    // Password strength logic
    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasNumber = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }
    
    val strengthScore = listOf(hasMinLength, hasUppercase, hasNumber, hasSpecialChar).count { it }
    val strengthText = when {
        password.isEmpty() -> ""
        strengthScore <= 1 -> "Weak"
        strengthScore <= 3 -> "Medium"
        else -> "Strong"
    }
    val strengthColor = when {
        strengthScore <= 1 -> Color(0xFFE57373)
        strengthScore <= 3 -> Color(0xFFFFB74D)
        else -> Color(0xFF81C784)
    }

    val passwordsMatch = password.isNotEmpty() && password == confirmPassword
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    val isEmailValid = email.isEmpty() || emailRegex.matches(email.trim())
    
    val nameRegex = "^[a-zA-Z\\s]+$".toRegex()
    val isFirstNameValid = firstName.isEmpty() || nameRegex.matches(firstName.trim())
    val isLastNameValid = lastName.isEmpty() || nameRegex.matches(lastName.trim())
    
    var selectedRole by remember { mutableStateOf(initialRole ?: "Student") }
    
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LocalAppColors.current.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Back Button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .offset(x = (-12).dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = LocalAppColors.current.textTitle,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                color = LocalAppColors.current.textTitle,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Join the Interview Assist community",
                style = MaterialTheme.typography.bodyLarge,
                color = LocalAppColors.current.textBody
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (initialRole == null) {
                // Role Cards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SignupRoleCard(
                        label = "Student",
                        description = "Looking for jobs",
                        icon = Icons.Default.School,
                        isSelected = selectedRole == "Student",
                        onClick = { selectedRole = "Student" },
                        modifier = Modifier.weight(1f)
                    )
                    SignupRoleCard(
                        label = "Alumni",
                        description = "Share experiences",
                        icon = Icons.Default.WorkOutline,
                        isSelected = selectedRole == "Alumni",
                        onClick = { selectedRole = "Alumni" },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            } else {
                // Show fixed role info instead of selection cards
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    color = LocalAppColors.current.primaryHighlight,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (selectedRole == "Student") Icons.Default.School else Icons.Default.WorkOutline,
                            contentDescription = null,
                            tint = LocalAppColors.current.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Creating account as $selectedRole",
                            color = LocalAppColors.current.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Names Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SignupLabel("First Name")
                    SignupTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        placeholder = "John",
                        isError = !isFirstNameValid || (showErrors && firstName.isEmpty())
                    )
                    AnimatedVisibility(visible = !isFirstNameValid) {
                        Text(
                            text = "Invalid name",
                            color = LocalAppColors.current.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    SignupLabel("Last Name")
                    SignupTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        placeholder = "Doe",
                        isError = !isLastNameValid || (showErrors && lastName.isEmpty())
                    )
                    AnimatedVisibility(visible = !isLastNameValid) {
                        Text(
                            text = "Invalid name",
                            color = LocalAppColors.current.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SignupLabel("Email")
            SignupTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "john.doe@university.edu",
                isError = !isEmailValid || (showErrors && email.isEmpty())
            )
            AnimatedVisibility(visible = !isEmailValid) {
                Text(
                    text = "Please enter a valid email address",
                    color = LocalAppColors.current.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SignupLabel("Password")
            SignupTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Create a password",
                isPassword = true
            )

            // Password Strength Indicator
            if (password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(4) { index ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (index < strengthScore) strengthColor else LocalAppColors.current.divider.copy(alpha = 0.3f))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = strengthText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = strengthColor
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                // Password Requirements Checklist
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    RequirementItem("At least 8 characters", hasMinLength)
                    RequirementItem("One uppercase letter", hasUppercase)
                    RequirementItem("One number", hasNumber)
                    RequirementItem("One special character", hasSpecialChar)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SignupLabel("Confirm Password")
            SignupTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Confirm password",
                isPassword = true,
                isError = (confirmPassword.isNotEmpty() && !passwordsMatch) || (showErrors && confirmPassword.isEmpty())
            )
            
            AnimatedVisibility(visible = confirmPassword.isNotEmpty() && !passwordsMatch) {
                Text(
                    text = "Passwords do not match",
                    color = LocalAppColors.current.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Continue Button
            Button(
                onClick = {
                    showErrors = true
                    if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank() || 
                        !nameRegex.matches(firstName.trim()) || !nameRegex.matches(lastName.trim()) || 
                        strengthScore < 4 || !emailRegex.matches(email.trim()) || !passwordsMatch) {
                        
                        scope.launch {
                            snackbarHostState.showSnackbar("Please correct the errors in the form")
                        }
                        return@Button
                    }

                    isLoading = true
                    scope.launch {
                        try {
                            val request = RegisterRequest(
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                password = password,
                                role = selectedRole
                            )
                            val response = RetrofitClient.apiService.register(request)
                            isLoading = false
                            
                            if (response.isSuccessful) {
                                val registerResponse = response.body()
                                val user = registerResponse?.user
                                val token = registerResponse?.accessToken
                                val userId = user?.id ?: 0
                                
                                // Sync user details to preferences
                                if (user != null) {
                                    preferenceManager.saveUserId(user.id)
                                    preferenceManager.saveUserDetails(user.firstName, user.lastName, user.email)
                                    token?.let {
                                        preferenceManager.saveToken(it)
                                        RetrofitClient.setAuthToken(it)
                                    }
                                    // Reset prompt flag to show it once after account creation
                                    preferenceManager.setCompletionPromptShown(user.id, false)
                                }
                                
                                Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                onContinue(selectedRole, userId)
                            } else {
                                val errorBody = response.errorBody()?.string()
                                val errorMsg = try {
                                    if (errorBody != null) {
                                        JSONObject(errorBody).getString("error")
                                    } else "Unknown error"
                                } catch (e: Exception) {
                                    errorBody ?: "Unknown error"
                                }
                                snackbarHostState.showSnackbar(errorMsg)
                            }
                        } catch (e: Exception) {
                            isLoading = false
                            snackbarHostState.showSnackbar("Connection error: ${e.message}")
                        }
                    }
                },
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
                        text = "Continue as $selectedRole",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.surface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Powered by SIMATS Engineering",
                style = MaterialTheme.typography.labelLarge,
                color = LocalAppColors.current.textBody.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun RequirementItem(text: String, isMet: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isMet) Color(0xFF81C784) else LocalAppColors.current.textBody.copy(alpha = 0.3f),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isMet) LocalAppColors.current.textTitle else LocalAppColors.current.textBody.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun SignupLabel(text: String) {
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
fun SignupTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = LocalAppColors.current.borderLight) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        isError = isError,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        trailingIcon = {
            if (isError) {
                Icon(Icons.Default.Error, "Error", tint = LocalAppColors.current.error)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = LocalAppColors.current.textTitle,
            unfocusedTextColor = LocalAppColors.current.textTitle,
            unfocusedBorderColor = LocalAppColors.current.divider,
            focusedBorderColor = LocalAppColors.current.primary,
            errorBorderColor = LocalAppColors.current.error,
            unfocusedContainerColor = LocalAppColors.current.surfaceVariant,
            focusedContainerColor = LocalAppColors.current.surfaceVariant,
            errorContainerColor = LocalAppColors.current.errorBg
        ),
        singleLine = true
    )
}

@Composable
fun SignupRoleCard(
    label: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(20.dp)),
        color = if (isSelected) LocalAppColors.current.primaryHighlight else LocalAppColors.current.surfaceVariant,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) LocalAppColors.current.primary else LocalAppColors.current.textBody.copy(alpha = 0.4f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) LocalAppColors.current.primary else LocalAppColors.current.textTitle,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = if (isSelected) LocalAppColors.current.primary.copy(alpha = 0.7f) else LocalAppColors.current.textBody.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}
