package com.simats.interviewassist.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.simats.interviewassist.ui.theme.*
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.utils.PreferenceManager
import com.simats.interviewassist.utils.ProfilePicManager
import com.simats.interviewassist.data.models.*
import org.json.JSONObject
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import android.graphics.Paint
import android.graphics.BlurMaskFilter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    preferenceManager: PreferenceManager,
    onCreateAccount: (String) -> Unit,
    onForgotPassword: (String) -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    var selectedRole by remember { mutableStateOf("Student") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }
    var showSuspendedDialog by remember { mutableStateOf(false) }
    var showPendingDialog by remember { mutableStateOf(false) }
    var showRejectedDialog by remember { mutableStateOf(false) }
    
    var showOtpDialog by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Validation state
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    suspend fun fetchAndCacheProfile(userId: Int) {
        try {
            val profileResponse = RetrofitClient.apiService.getUserProfile(userId)
            if (profileResponse.isSuccessful) {
                val profileData = profileResponse.body()
                profileData?.let { data ->
                    // Save basic details
                    preferenceManager.saveUserDetails(data.firstName, data.lastName, data.email)
                    
                    // Save full profile details
                    preferenceManager.saveFullProfile(
                        major = data.profile?.major,
                        gradYear = data.profile?.expectedGradYear,
                        currentYear = data.profile?.currentYear,
                        bio = data.profile?.bio,
                        secondaryEmail = data.secondaryEmail,
                        phoneNumber = data.profile?.phoneNumber
                    )
                    
                    // Save profile picture properly (decoding base64 or handling URL)
                    ProfilePicManager.saveBase64Image(context, data.profile?.profilePic, preferenceManager)
                }
            }
        } catch (e: Exception) {
            // Silently fail, main login succeeded
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "SignInButtonScale"
    )

    // Shimmer animation
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

    // Entrance animations
    val entranceDelay = 100
    val animStates = List(6) { i ->
        remember { mutableStateOf(false) }
    }
    
    LaunchedEffect(Unit) {
        animStates.forEachIndexed { index, state ->
            kotlinx.coroutines.delay(entranceDelay.toLong() * index)
            state.value = true
        }
    }
    fun validateAndLogin() {
        showErrors = true
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        emailError = when {
            email.isBlank() -> "Email is required"
            !emailRegex.matches(email.trim()) -> "Please enter a valid email address"
            else -> null
        }
        passwordError = when {
            password.isBlank() -> "Password is required"
            else -> null
        }
        
        if (emailError == null && passwordError == null) {
            isLoading = true
            scope.launch {
                try {
                    val request = LoginRequest(
                        email = email.trim(),
                        password = password.trim(),
                        role = selectedRole
                    )
                    val response = RetrofitClient.apiService.login(request)
                    
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        val user = loginResponse?.user
                        val token = loginResponse?.accessToken
                        val requiresOtp = loginResponse?.requiresOtp == true
                        
                        if (requiresOtp) {
                            isLoading = false
                            showOtpDialog = true
                        } else {
                            if (user != null) {
                                preferenceManager.saveUserId(user.id)
                                preferenceManager.saveUserDetails(user.firstName, user.lastName, user.email)
                                token?.let {
                                    preferenceManager.saveToken(it)
                                    RetrofitClient.setAuthToken(it)
                                }
                                preferenceManager.saveStatus(user.status)
                                
                                // Sync status from backend
                                if (user.hasCompletedProfile == true) {
                                    preferenceManager.setProfileCompleted(user.id)
                                }
                                if (user.profileSkipped == true) {
                                                                      preferenceManager.setProfileSkipped(user.id, true)
                                }
                                
                                // Reset prompt flag to allow showing it once after this login
                                preferenceManager.setCompletionPromptShown(user.id, false)
                                
                                // PROACTIVE FETCH: Get full profile details immediately
                                fetchAndCacheProfile(user.id)
                            }

                            isLoading = false
                            onLoginSuccess(selectedRole)
                        }
                    } else {
                        isLoading = false
                        val errorBody = response.errorBody()?.string()
                        val errorMsg = try {
                            if (errorBody != null) {
                                JSONObject(errorBody).getString("error")
                            } else "Login failed"
                        } catch (e: Exception) {
                            errorBody ?: "Login failed"
                        }
                        if (errorMsg.contains("Suspended", ignoreCase = true)) {
                            showSuspendedDialog = true
                        } else if (errorMsg.contains("pending", ignoreCase = true)) {
                            showPendingDialog = true
                        } else if (errorMsg.contains("rejected", ignoreCase = true)) {
                            showRejectedDialog = true
                        } else {
                            snackbarHostState.showSnackbar(errorMsg)
                        }
                    }
                } catch (e: Exception) {
                    isLoading = false
                    snackbarHostState.showSnackbar("Connection error: ${e.message}")
                }
            }
        }
    }

    // --- Suspended Account Dialog ---
    if (showSuspendedDialog) {
        AlertDialog(
            onDismissRequest = { showSuspendedDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = LocalAppColors.current.surface
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = LocalAppColors.current.errorBg,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Block,
                                contentDescription = null,
                                tint = LocalAppColors.current.error,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "Account Suspended",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "You are Suspended.\nPlease contact admin.",
                        fontSize = 15.sp,
                        color = LocalAppColors.current.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Button(
                        onClick = { showSuspendedDialog = false },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary)
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
    // --- Rejected Account Dialog ---
    if (showRejectedDialog) {
        AlertDialog(
            onDismissRequest = { showRejectedDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = LocalAppColors.current.surface
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = LocalAppColors.current.errorBg,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                tint = LocalAppColors.current.error,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "Access Rejected",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Your access has been rejected by the admin.",
                        fontSize = 15.sp,
                        color = LocalAppColors.current.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Button(
                        onClick = { showRejectedDialog = false },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary)
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // --- Account Pending Dialog ---
    if (showPendingDialog) {
        AlertDialog(
            onDismissRequest = { showPendingDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = LocalAppColors.current.surface
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = LocalAppColors.current.primaryHighlight,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = LocalAppColors.current.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "Account Pending Approval",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Your account activation is pending.\nPlease wait for admin approval.",
                        fontSize = 15.sp,
                        color = LocalAppColors.current.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Button(
                        onClick = { showPendingDialog = false },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary)
                    ) {
                        Text("Dismiss", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            MeshBackground()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
            // --- SECTION 1: HEADER (Logo, Welcome, Role Selection) ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Logo & Title Section
                AnimatedVisibility(
                    visible = animStates[0].value,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 2 }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 24.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(84.dp)
                                .shadow(32.dp, RoundedCornerShape(24.dp), spotColor = LocalAppColors.current.primary.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(24.dp),
                            color = LocalAppColors.current.primary,
                            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Welcome Back",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = LocalAppColors.current.textTitle,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Sign in to continue to Interview Assist",
                            style = MaterialTheme.typography.bodyLarge,
                            color = LocalAppColors.current.textBody,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.alpha(0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Role Selection
                AnimatedVisibility(
                    visible = animStates[1].value,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { h -> h / 2 }
                ) {
                    RoleSelector(
                        selectedRole = selectedRole,
                        onRoleSelected = { role: String -> selectedRole = role }
                    )
                }
            }

            // --- SECTION 2: FORM (Inputs, Forgot Password, Sign In) ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Input Fields
                AnimatedVisibility(
                    visible = animStates[2].value,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 2 }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Email Address",
                            style = MaterialTheme.typography.titleSmall,
                            color = LocalAppColors.current.textTitle,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                if (emailError != null) emailError = null
                            },
                            placeholder = { Text("example@gmail.com", color = LocalAppColors.current.textSecondary.copy(alpha = 0.5f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            isError = showErrors && emailError != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LocalAppColors.current.textTitle,
                                unfocusedTextColor = LocalAppColors.current.textTitle,
                                errorTextColor = LocalAppColors.current.textTitle,
                                unfocusedBorderColor = LocalAppColors.current.divider,
                                focusedBorderColor = LocalAppColors.current.primary,
                                errorBorderColor = LocalAppColors.current.error,
                                unfocusedContainerColor = LocalAppColors.current.surface.copy(alpha = 0.6f),
                                focusedContainerColor = LocalAppColors.current.surface,
                                errorContainerColor = LocalAppColors.current.errorBg.copy(alpha = 0.6f)
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.Email, null, tint = LocalAppColors.current.primary.copy(alpha = 0.6f))
                            },
                            singleLine = true
                        )
                        AnimatedVisibility(
                            visible = showErrors && emailError != null,
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
                                    text = emailError ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JakartaFontFamily),
                                    color = LocalAppColors.current.error
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Password",
                            style = MaterialTheme.typography.titleSmall,
                            color = LocalAppColors.current.textTitle,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                if (passwordError != null) passwordError = null
                            },
                            placeholder = { Text("••••••••", color = LocalAppColors.current.textSecondary.copy(alpha = 0.5f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            isError = showErrors && passwordError != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LocalAppColors.current.textTitle,
                                unfocusedTextColor = LocalAppColors.current.textTitle,
                                errorTextColor = LocalAppColors.current.textTitle,
                                unfocusedBorderColor = LocalAppColors.current.divider,
                                focusedBorderColor = LocalAppColors.current.primary,
                                errorBorderColor = LocalAppColors.current.error,
                                unfocusedContainerColor = LocalAppColors.current.surface.copy(alpha = 0.6f),
                                focusedContainerColor = LocalAppColors.current.surface,
                                errorContainerColor = LocalAppColors.current.errorBg.copy(alpha = 0.6f)
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.Lock, null, tint = LocalAppColors.current.primary.copy(alpha = 0.6f))
                            },
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = null, tint = LocalAppColors.current.iconTint)
                                }
                            },
                            singleLine = true
                        )
                        
                        AnimatedVisibility(
                            visible = showErrors && passwordError != null,
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
                                    text = passwordError ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JakartaFontFamily),
                                    color = LocalAppColors.current.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Forgot Password?",
                                color = LocalAppColors.current.primary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { onForgotPassword(selectedRole) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Sign In Button
                AnimatedVisibility(
                    visible = animStates[3].value,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 2 }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .shadow(24.dp, RoundedCornerShape(18.dp), spotColor = LocalAppColors.current.primary.copy(alpha = 0.5f))
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(LocalAppColors.current.primary, Color(0xFF1E40AF))
                                )
                            )
                            .clickable(interactionSource = interactionSource, indication = null, enabled = !isLoading) {
                                validateAndLogin()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        } else {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.White.copy(alpha = 0.15f),
                                                Color.Transparent
                                            ),
                                            start = Offset(shimmerOffset - 300f, 0f),
                                            end = Offset(shimmerOffset + 300f, 0f)
                                        )
                                    )
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Sign In",
                                    fontSize = 18.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION 3: FOOTER (Signup & Powered by) ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Create Account
                // Create Account
                if (selectedRole != "Admin") {
                    AnimatedVisibility(
                        visible = animStates[4].value,
                        enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 2 }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = "Don't have an account? ",
                                color = LocalAppColors.current.textBody,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Create Account",
                                color = LocalAppColors.current.primary,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.clickable { onCreateAccount(selectedRole) }
                            )
                        }
                    }
                }
                   // SIMATS Footer
                AnimatedVisibility(
                    visible = animStates[5].value,
                    enter = fadeIn(tween(600))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "© 2026 Powered by SIMATS Engineering",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = LocalAppColors.current.textBody.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

    if (showOtpDialog) {
        AlertDialog(
            onDismissRequest = { 
                showOtpDialog = false
                otpCode = ""
            },
            containerColor = LocalAppColors.current.surface,
            title = { 
                Text(
                    "Two-Factor Authentication", 
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = JakartaFontFamily, fontWeight = FontWeight.Bold),
                    color = LocalAppColors.current.textTitle
                ) 
            },
            text = {
                Column {
                    Text(
                        "Please enter the 6-digit OTP sent to your registered email address.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JakartaFontFamily),
                        color = LocalAppColors.current.textSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it },
                        placeholder = { Text("000000", color = LocalAppColors.current.iconTint) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LocalAppColors.current.textTitle,
                            unfocusedTextColor = LocalAppColors.current.textTitle,
                            focusedBorderColor = LocalAppColors.current.primary,
                            unfocusedBorderColor = LocalAppColors.current.divider
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (otpCode.isNotBlank()) {
                            isLoading = true
                            scope.launch {
                                try {
                                    val request = VerifyLoginOtpRequest(
                                        email = email,
                                        role = selectedRole,
                                        otp = otpCode
                                    )
                                    val otpResponse = RetrofitClient.apiService.verifyLoginOtp(request)
                                    isLoading = false
                                    if (otpResponse.isSuccessful) {
                                        val loginResp = otpResponse.body()
                                        val user = loginResp?.user
                                        val token = loginResp?.accessToken
                                        
                                        showOtpDialog = false
                                        otpCode = ""
                                        
                                        if (user != null) {
                                            preferenceManager.saveUserId(user.id)
                                            preferenceManager.saveUserDetails(user.firstName, user.lastName, user.email)
                                            token?.let {
                                                preferenceManager.saveToken(it)
                                                RetrofitClient.setAuthToken(it)
                                            }
                                            preferenceManager.setCompletionPromptShown(user.id, false)
                                            
                                            fetchAndCacheProfile(user.id)
                                        }

                                        onLoginSuccess(selectedRole)
                                    } else {
                                        snackbarHostState.showSnackbar("OTP Verification Failed")
                                    }
                                } catch (e: Exception) {
                                    isLoading = false
                                    snackbarHostState.showSnackbar("Connection error: ${e.message}")
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary)
                ) {
                    Text("Verify", color = LocalAppColors.current.surface, fontFamily = JakartaFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showOtpDialog = false
                    otpCode = ""
                }) {
                    Text("Cancel", color = LocalAppColors.current.textSecondary, fontFamily = JakartaFontFamily)
                }
            }
        )
    }
}

@Composable
fun MeshBackground() {
    val primaryColor = LocalAppColors.current.primary
    val surfaceColor = LocalAppColors.current.surface
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
            .drawBehind {
                val canvasWidth = size.width
                val canvasHeight = size.height
                
                // Drawing blobs for a mesh effect
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(canvasWidth * 0.9f, canvasHeight * 0.1f),
                        radius = canvasWidth * 0.8f
                    )
                )
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(canvasWidth * 0.1f, canvasHeight * 0.9f),
                        radius = canvasWidth * 0.7f
                    )
                )
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF6366F1).copy(alpha = 0.05f), Color.Transparent),
                        center = Offset(canvasWidth * 0.5f, canvasHeight * 0.5f),
                        radius = canvasWidth * 0.6f
                    )
                )
            }
    )
}

@Composable
fun RoleSelector(
    selectedRole: String,
    onRoleSelected: (String) -> Unit
) {
    val roles = listOf("Student", "Alumni", "Admin")
    val selectedIndex = roles.indexOf(selectedRole)
    
    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy),
        label = "roleSelectionAnim"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = LocalAppColors.current.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, LocalAppColors.current.divider.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.padding(4.dp)) {
            // Sliding background
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val width = maxWidth / roles.size
                Surface(
                    modifier = Modifier
                        .width(width)
                        .fillMaxHeight()
                        .offset(x = width * animatedIndex),
                    shape = RoundedCornerShape(12.dp),
                    color = LocalAppColors.current.surface,
                    shadowElevation = 4.dp
                ) {}
            }
            
            Row(modifier = Modifier.fillMaxSize()) {
                roles.forEach { role ->
                    val isSelected = selectedRole == role
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onRoleSelected(role) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = role,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) LocalAppColors.current.primary else LocalAppColors.current.textSecondary
                        )
                    }
                }
            }
        }
    }
}

