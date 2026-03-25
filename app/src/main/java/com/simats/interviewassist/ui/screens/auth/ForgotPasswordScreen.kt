package com.simats.interviewassist.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.simats.interviewassist.ui.theme.*
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.data.models.ForgotPasswordRequest
import com.simats.interviewassist.data.models.VerifyOTPRequest
import com.simats.interviewassist.data.models.ResetPasswordRequest
import org.json.JSONObject
import kotlinx.coroutines.launch

enum class ForgotPasswordStep(val progress: Float) {
    EMAIL(0.25f), OTP(0.5f), NEW_PASSWORD(0.75f), SUCCESS(1f)
}

@Composable
fun ForgotPasswordScreen(
    role: String,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    var currentStep by remember { mutableStateOf(ForgotPasswordStep.EMAIL) }
    var email by remember { mutableStateOf("") }
    var otpValues = remember { mutableStateListOf("", "", "", "", "", "") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Validation state
    var showErrors by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var otpError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val progress by animateFloatAsState(
        targetValue = currentStep.progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "StepProgress"
    )

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun sendOtp() {
        showErrors = true
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (email.isBlank()) {
            emailError = "Email is required"
            return
        } else if (!emailRegex.matches(email.trim())) {
            emailError = "Please enter a valid email address"
            return
        }
        
        emailError = null
        showErrors = false
        isLoading = true
        
        scope.launch {
            try {
                val request = ForgotPasswordRequest(email = email, role = role)
                val response = RetrofitClient.apiService.forgotPassword(request)
                isLoading = false
                
                if (response.isSuccessful) {
                    currentStep = ForgotPasswordStep.OTP
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = try {
                        if (errorBody != null) JSONObject(errorBody).getString("error") else "Failed to send OTP"
                    } catch (e: Exception) {
                        "Failed to send OTP"
                    }
                    snackbarHostState.showSnackbar(errorMsg)
                }
            } catch (e: Exception) {
                isLoading = false
                snackbarHostState.showSnackbar("Connection error: ${e.message}")
            }
        }
    }

    fun verifyOtp() {
        if (!otpValues.all { it.isNotBlank() }) {
            showErrors = true
            otpError = "Please enter the complete 6-digit code"
            return
        }
        
        showErrors = false
        isLoading = true
        val otpString = otpValues.joinToString("")
        
        scope.launch {
            try {
                val request = VerifyOTPRequest(email = email, role = role, otp = otpString)
                val response = RetrofitClient.apiService.verifyOTP(request)
                isLoading = false
                
                if (response.isSuccessful) {
                    currentStep = ForgotPasswordStep.NEW_PASSWORD
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = try {
                        if (errorBody != null) JSONObject(errorBody).getString("error") else "Invalid OTP"
                    } catch (e: Exception) {
                        "Invalid OTP"
                    }
                    otpError = errorMsg
                    showErrors = true
                }
            } catch (e: Exception) {
                isLoading = false
                snackbarHostState.showSnackbar("Connection error: ${e.message}")
            }
        }
    }

    fun resetPassword() {
        showErrors = true
        var isValid = true
        val strengthScore = listOf(
            newPassword.length >= 8,
            newPassword.any { it.isUpperCase() },
            newPassword.any { it.isDigit() },
            newPassword.any { !it.isLetterOrDigit() }
        ).count { it }

        if (newPassword.isBlank()) {
            passwordError = "Password is required"
            isValid = false
        } else if (strengthScore < 4) {
            passwordError = "Please meet all password requirements"
            isValid = false
        }
        
        if (confirmPassword != newPassword) {
            confirmPasswordError = "Passwords do not match"
            isValid = false
        }
        
        if (!isValid) return
        
        showErrors = false
        isLoading = true
        val otpString = otpValues.joinToString("")
        
        scope.launch {
            try {
                val request = ResetPasswordRequest(
                    email = email, 
                    role = role, 
                    otp = otpString, 
                    newPassword = newPassword
                )
                val response = RetrofitClient.apiService.resetPassword(request)
                isLoading = false
                
                if (response.isSuccessful) {
                    currentStep = ForgotPasswordStep.SUCCESS
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = try {
                        if (errorBody != null) JSONObject(errorBody).getString("error") else "Failed to reset password"
                    } catch (e: Exception) {
                        "Failed to reset password"
                    }
                    snackbarHostState.showSnackbar(errorMsg)
                }
            } catch (e: Exception) {
                isLoading = false
                snackbarHostState.showSnackbar("Connection error: ${e.message}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LocalAppColors.current.surface,
        topBar = {
            if (currentStep != ForgotPasswordStep.SUCCESS) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 40.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        IconButton(
                            onClick = {
                                when (currentStep) {
                                    ForgotPasswordStep.EMAIL -> onBack()
                                    ForgotPasswordStep.OTP -> currentStep = ForgotPasswordStep.EMAIL
                                    ForgotPasswordStep.NEW_PASSWORD -> currentStep = ForgotPasswordStep.OTP
                                    else -> {}
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = LocalAppColors.current.textTitle)
                        }
                        
                        // Small progress indicator at top
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(0.6f)
                                .height(6.dp)
                                .clip(CircleShape),
                            color = LocalAppColors.current.primary,
                            trackColor = LocalAppColors.current.divider.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

        Crossfade(targetState = currentStep, label = "ForgotPasswordFlow") { step ->
            Column(modifier = Modifier.fillMaxWidth()) {
                when (step) {
                    ForgotPasswordStep.EMAIL -> {
                        EmailStep(
                            email = email,
                            onEmailChange = { 
                                email = it
                                if (emailError != null) emailError = null
                            },
                            showErrors = showErrors,
                            errorMessage = emailError,
                            onNext = { sendOtp() },
                            isLoading = isLoading
                        )
                    }
                    ForgotPasswordStep.OTP -> {
                        OtpStep(
                            email = email,
                            otpValues = otpValues,
                            showErrors = showErrors,
                            errorMessage = otpError,
                            onNext = { verifyOtp() },
                            isLoading = isLoading,
                            onResend = { sendOtp() }
                        )
                    }
                    ForgotPasswordStep.NEW_PASSWORD -> {
                        NewPasswordStep(
                            newPassword = newPassword,
                            confirmPassword = confirmPassword,
                            showPassword = showPassword,
                            showErrors = showErrors,
                            passwordError = passwordError,
                            confirmPasswordError = confirmPasswordError,
                            onNewPasswordChange = { 
                                newPassword = it
                                if (passwordError != null) passwordError = null
                            },
                            onConfirmPasswordChange = { 
                                confirmPassword = it
                                if (confirmPasswordError != null) confirmPasswordError = null
                            },
                            onTogglePassword = { showPassword = !showPassword },
                            onNext = { resetPassword() },
                            isLoading = isLoading
                        )
                    }
                    ForgotPasswordStep.SUCCESS -> {
                        SuccessStep(onFinish = onFinish)
                    }
                }
            }
        }
    }
    }
}


@Composable
fun EmailStep(
    email: String,
    onEmailChange: (String) -> Unit,
    showErrors: Boolean,
    errorMessage: String?,
    isLoading: Boolean,
    onNext: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // Icon Header
        Surface(
            modifier = Modifier
                .size(80.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = LocalAppColors.current.primary.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(24.dp),
            color = LocalAppColors.current.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Forgot Password?",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = JakartaFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = LocalAppColors.current.textTitle
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Enter your email address and we'll send you an OTP to reset your password.",
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = JakartaFontFamily),
            color = LocalAppColors.current.textBody,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Email Address",
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JakartaFontFamily),
                color = LocalAppColors.current.textTitle,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = { Text("example@gmail.com", color = LocalAppColors.current.iconTint) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = showErrors && errorMessage != null,
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
                trailingIcon = {
                    if (showErrors && errorMessage != null) {
                        Icon(Icons.Default.Error, "Error", tint = LocalAppColors.current.error)
                    }
                },
                singleLine = true
            )
            AnimatedErrorText(if (showErrors) errorMessage else null)
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    PremiumButton(
        text = "Send OTP",
        onClick = onNext,
        enabled = email.isNotBlank() && !isLoading,
        isLoading = isLoading
    )
}

@Composable
fun OtpStep(
    email: String,
    otpValues: MutableList<String>,
    showErrors: Boolean,
    errorMessage: String?,
    isLoading: Boolean,
    onResend: () -> Unit,
    onNext: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // Icon Header
        Surface(
            modifier = Modifier
                .size(80.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = LocalAppColors.current.primary.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(24.dp),
            color = LocalAppColors.current.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Verify OTP",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = JakartaFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = LocalAppColors.current.textTitle
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "We have sent a 6-digit code to\n$email",
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = JakartaFontFamily),
            color = LocalAppColors.current.textBody,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        val focusRequesters = remember { List(6) { FocusRequester() } }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            otpValues.forEachIndexed { index, value ->
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        if (it.length <= 1) {
                            otpValues[index] = it
                            if (it.isNotEmpty() && index < 5) {
                                focusRequesters[index + 1].requestFocus()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .focusRequester(focusRequesters[index]),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LocalAppColors.current.textTitle,
                        unfocusedTextColor = LocalAppColors.current.textTitle,
                        unfocusedBorderColor = if (value.isNotEmpty()) LocalAppColors.current.primary else LocalAppColors.current.divider,
                        focusedBorderColor = LocalAppColors.current.primary,
                        unfocusedContainerColor = LocalAppColors.current.surfaceVariant,
                        focusedContainerColor = LocalAppColors.current.surfaceVariant
                    ),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JakartaFontFamily
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }
        }
        AnimatedErrorText(if (showErrors) errorMessage else null)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Didn't receive code? Resend",
            color = LocalAppColors.current.primary,
            style = MaterialTheme.typography.labelLarge.copy(fontFamily = JakartaFontFamily),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(enabled = !isLoading) { onResend() }
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    PremiumButton(
        text = "Verify OTP",
        onClick = onNext,
        enabled = otpValues.all { it.isNotBlank() } && !isLoading,
        isLoading = isLoading
    )
}

@Composable
fun NewPasswordStep(
    newPassword:  String,
    confirmPassword:  String,
    showPassword:  Boolean,
    showErrors: Boolean,
    passwordError: String?,
    confirmPasswordError: String?,
    isLoading: Boolean,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onNext: () -> Unit
) {
    val hasMinLength = newPassword.length >= 8
    val hasUppercase = newPassword.any { it.isUpperCase() }
    val hasNumber = newPassword.any { it.isDigit() }
    val hasSpecialChar = newPassword.any { !it.isLetterOrDigit() }

    val strengthScore = listOf(hasMinLength, hasUppercase, hasNumber, hasSpecialChar).count { it }

    val strengthText = when (strengthScore) {
        0 -> ""
        1 -> "Weak"
        2 -> "Fair"
        3 -> "Good"
        4 -> "Strong"
        else -> ""
    }
    val strengthColor = when {
        strengthScore <= 1 -> Color(0xFFE57373)
        strengthScore <= 3 -> Color(0xFFFFB74D)
        else -> Color(0xFF81C784)
    }

    val passwordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // Icon Header
        Surface(
            modifier = Modifier
                .size(80.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = LocalAppColors.current.primary.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(24.dp),
            color = LocalAppColors.current.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Set New Password",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = JakartaFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = LocalAppColors.current.textTitle
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Create a new secure password for your account.",
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = JakartaFontFamily),
            color = LocalAppColors.current.textBody,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "New Password",
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JakartaFontFamily),
                color = LocalAppColors.current.textTitle,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                placeholder = { Text("........", color = LocalAppColors.current.iconTint) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (showErrors && passwordError != null) {
                            Icon(Icons.Default.Error, "Error", tint = LocalAppColors.current.error)
                        }
                        IconButton(onClick = onTogglePassword) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = LocalAppColors.current.iconTint
                            )
                        }
                    }
                },
                isError = showErrors && passwordError != null,
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
            AnimatedErrorText(if (showErrors) passwordError else null)

            // Password Strength Indicator
            if (newPassword.isNotEmpty()) {
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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    ForgotPasswordRequirementItem("At least 8 characters", hasMinLength)
                    ForgotPasswordRequirementItem("One uppercase letter", hasUppercase)
                    ForgotPasswordRequirementItem("One number", hasNumber)
                    ForgotPasswordRequirementItem("One special character", hasSpecialChar)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Confirm Password",
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JakartaFontFamily),
                color = LocalAppColors.current.textTitle,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                placeholder = { Text("........", color = LocalAppColors.current.iconTint) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                isError = (confirmPassword.isNotEmpty() && !passwordsMatch) || (showErrors && confirmPasswordError != null),
                trailingIcon = {
                    if ((confirmPassword.isNotEmpty() && !passwordsMatch) || (showErrors && confirmPasswordError != null)) {
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
            AnimatedErrorText(if (showErrors) confirmPasswordError else null)
            androidx.compose.animation.AnimatedVisibility(visible = confirmPassword.isNotEmpty() && !passwordsMatch) {
                Text(
                    text = "Passwords do not match",
                    color = LocalAppColors.current.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp).fillMaxWidth()
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    PremiumButton(
        text = "Change Password",
        onClick = onNext,
        enabled = strengthScore >= 4 && passwordsMatch && !isLoading,
        isLoading = isLoading
    )
}

@Composable
private fun ForgotPasswordRequirementItem(text: String, isMet: Boolean) {
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
fun SuccessStep(onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Shimmering success icon
        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Surface(
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = LocalAppColors.current.success.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(32.dp),
            color = LocalAppColors.current.success
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Password Changed!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = JakartaFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = LocalAppColors.current.textTitle
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your password has been successfully reset. You can now log in with your new password.",
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = JakartaFontFamily),
            color = LocalAppColors.current.textBody,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(56.dp))

        PremiumButton(
            text = "Back to Login",
            onClick = onFinish,
            icon = Icons.Default.ArrowBack
        )
    }
}

@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "ButtonScale"
    )

    // Shimmer effect properties
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by shimmerTransition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(if (enabled) 10.dp else 0.dp, RoundedCornerShape(16.dp), spotColor = LocalAppColors.current.primary)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) {
                    Brush.linearGradient(
                        colors = listOf(LocalAppColors.current.primary, Color(0xFF1565C0), LocalAppColors.current.primary)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(LocalAppColors.current.divider, LocalAppColors.current.divider)
                    )
                }
            )
            .clickable(interactionSource = interactionSource, indication = null, enabled = enabled) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = LocalAppColors.current.surface,
                modifier = Modifier.size(24.dp)
            )
        } else {
            if (enabled) {
                // Shimmer sweep overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    LocalAppColors.current.surface.copy(alpha = 0.18f),
                                    Color.Transparent
                                ),
                                start = Offset(shimmerOffset - 300f, 0f),
                                end = Offset(shimmerOffset + 300f, 0f)
                            )
                        )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) LocalAppColors.current.surface else LocalAppColors.current.textBody.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = JakartaFontFamily),
                    color = if (enabled) LocalAppColors.current.surface else LocalAppColors.current.textBody.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AnimatedErrorText(errorMessage: String?) {
    androidx.compose.animation.AnimatedVisibility(
        visible = errorMessage != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
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
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = JakartaFontFamily),
                color = LocalAppColors.current.error
            )
        }
    }
}

