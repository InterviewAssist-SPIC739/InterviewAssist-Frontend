package com.simats.interviewassist.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.data.models.ChangePasswordRequest
import org.json.JSONObject
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChangePasswordScreen(
    onBack: () -> Unit = {}
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
                        fontSize = 20.sp, 
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LocalAppColors.current.background)
                .padding(padding)
                .padding(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Current Password", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LocalAppColors.current.textTitle)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        placeholder = { Text("Enter current password", color = LocalAppColors.current.iconTint, fontSize = 15.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                Icon(
                                    imageVector = if (currentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
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
                        textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text("New Password", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LocalAppColors.current.textTitle)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        placeholder = { Text("Enter new password", color = LocalAppColors.current.iconTint, fontSize = 15.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
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
                        textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text("Confirm New Password", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LocalAppColors.current.textTitle)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("Confirm new password", color = LocalAppColors.current.iconTint, fontSize = 15.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
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
                        textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = LocalAppColors.current.primaryHighlight,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Password Requirements",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E40AF)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val requirements = listOf(
                        "At least 8 characters long",
                        "Contains uppercase and lowercase letters",
                        "Contains at least one number",
                        "Contains at least one special character"
                    )
                    requirements.forEach { req ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Box(modifier = Modifier.size(4.dp).background(Color(0xFF2563EB), RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(req, fontSize = 13.sp, color = Color(0xFF2563EB))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

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
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = LocalAppColors.current.surface, modifier = Modifier.size(24.dp))
                } else {
                    Text("Update Password", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
