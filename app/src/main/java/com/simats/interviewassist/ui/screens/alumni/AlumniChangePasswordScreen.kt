package com.simats.interviewassist.ui.screens.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.data.models.ChangePasswordRequest
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniChangePasswordScreen(
    onBack: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Change Password", 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textTitle
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LocalAppColors.current.textTitle)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LocalAppColors.current.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LocalAppColors.current.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Password Fields Container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    AlumniPasswordField(
                        label = "Current Password",
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        isVisible = currentPasswordVisible,
                        onToggleVisibility = { currentPasswordVisible = !currentPasswordVisible },
                        placeholder = "Enter current password"
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    AlumniPasswordField(
                        label = "New Password",
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        isVisible = newPasswordVisible,
                        onToggleVisibility = { newPasswordVisible = !newPasswordVisible },
                        placeholder = "Enter new password"
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    AlumniPasswordField(
                        label = "Confirm New Password",
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        isVisible = confirmPasswordVisible,
                        onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
                        placeholder = "Confirm new password"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Password Requirements
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surfaceHighlight
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Password Requirements", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = LocalAppColors.current.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AlumniRequirementItem("At least 8 characters long")
                    AlumniRequirementItem("Contains uppercase and lowercase letters")
                    AlumniRequirementItem("Contains at least one number")
                    AlumniRequirementItem("Contains at least one special character")
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // Update Button
            Button(
                onClick = {
                    val hasMinLength = newPassword.length >= 8
                    val hasUppercase = newPassword.any { it.isUpperCase() }
                    val hasLowercase = newPassword.any { it.isLowerCase() }
                    val hasNumber = newPassword.any { it.isDigit() }
                    val hasSpecialChar = newPassword.any { !it.isLetterOrDigit() }
                    
                    if (!hasMinLength || !hasUppercase || !hasLowercase || !hasNumber || !hasSpecialChar) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Password does not meet all requirements")
                        }
                        return@Button
                    }
                    
                    if (newPassword != confirmPassword) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Passwords do not match")
                        }
                        return@Button
                    }
                    
                    isLoading = true
                    scope.launch {
                        try {
                            val request = ChangePasswordRequest(
                                oldPassword = currentPassword,
                                newPassword = newPassword
                            )
                            val response = RetrofitClient.apiService.changePassword(request)
                            isLoading = false
                            
                            if (response.isSuccessful) {
                                snackbarHostState.showSnackbar("Password updated successfully!")
                                delay(2000)
                                onBack()
                            } else {
                                val errorBody = response.errorBody()?.string()
                                val errorMsg = try {
                                    if (errorBody != null) JSONObject(errorBody).getString("error")
                                    else "Update failed"
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
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = LocalAppColors.current.surface, modifier = Modifier.size(24.dp))
                } else {
                    Text("Update Password", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.surface)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AlumniPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    placeholder: String
) {
    Column {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LocalAppColors.current.textTitle)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = LocalAppColors.current.iconTint, fontSize = 15.sp) },
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility",
                        tint = LocalAppColors.current.iconTint
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFF0F0F0),
                focusedBorderColor = LocalAppColors.current.primary,
                unfocusedContainerColor = Color(0xFFFBFBFB),
                focusedContainerColor = Color(0xFFFBFBFB)
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 15.sp)
        )
    }
}

@Composable
fun AlumniRequirementItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text("•", color = LocalAppColors.current.primary, modifier = Modifier.padding(end = 8.dp))
        Text(text, fontSize = 12.sp, color = Color(0xFF3B82F6).copy(alpha = 0.8f))
    }
}
