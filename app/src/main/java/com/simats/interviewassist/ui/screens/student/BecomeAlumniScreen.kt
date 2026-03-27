package com.simats.interviewassist.ui.screens.student

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.data.network.RetrofitClient
import com.simats.interviewassist.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BecomeAlumniScreen(
    preferenceManager: com.simats.interviewassist.utils.PreferenceManager,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf(preferenceManager.getPhoneNumber().ifBlank { "" }) }
    var currentCompany by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var graduationYear by remember { mutableStateOf("") }
    var linkedInProfile by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    val phoneRegex = Regex("^[6-9][0-9]{9}$")
    val isPhoneValid = phoneRegex.matches(phoneNumber.trim())

    var expandedYear by remember { mutableStateOf(false) }
    val years = (2020..2030).map { it.toString() }

    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isPending = preferenceManager.getStatus() == "pending"


    val gradientBg = Brush.verticalGradient(
        colors = listOf(
            LocalAppColors.current.primary.copy(alpha = 0.05f),
            LocalAppColors.current.background
        )
    )

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBg)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.background(LocalAppColors.current.surface.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LocalAppColors.current.textTitle)
                    }
                    
                    Surface(
                        color = LocalAppColors.current.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Upgrade",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = LocalAppColors.current.primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Hero Section
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = LocalAppColors.current.primary,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AutoGraph,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Alumni Journey Starts Here",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LocalAppColors.current.textTitle,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    if (isPending) "Your request to become an alumni is currently under review by our administrators."
                    else "Fill in your details to help the next generation of students.",
                    fontSize = 15.sp,
                    color = LocalAppColors.current.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                if (isPending) {
                    Spacer(modifier = Modifier.height(60.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        color = LocalAppColors.current.primary.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, LocalAppColors.current.primary.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = LocalAppColors.current.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Review in Progress",
                                fontWeight = FontWeight.Bold,
                                color = LocalAppColors.current.textTitle,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "We've received your request. Our team is verifying your details to grant you alumni benefits. This usually takes 24-48 hours.",
                                color = LocalAppColors.current.textBody,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary)
                    ) {
                        Text("Back to Home", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.height(40.dp))

                    // Form Sections
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                "Professional Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = LocalAppColors.current.textTitle
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            AlumniFormField(
                                label = "Current Company",
                                value = currentCompany,
                                onValueChange = { currentCompany = it },
                                placeholder = "e.g. Google, Microsoft",
                                icon = Icons.Outlined.Business
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            AlumniFormField(
                                label = "Designation",
                                value = designation,
                                onValueChange = { designation = it },
                                placeholder = "e.g. Software Engineer",
                                icon = Icons.Outlined.WorkOutline
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            // Year Selection
                            Column {
                                Text("Graduation Year", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LocalAppColors.current.textTitle)
                                Spacer(modifier = Modifier.height(8.dp))
                                ExposedDropdownMenuBox(
                                    expanded = expandedYear,
                                    onExpandedChange = { expandedYear = it }
                                ) {
                                    OutlinedTextField(
                                        value = graduationYear,
                                        onValueChange = {},
                                        readOnly = true,
                                        placeholder = { Text("Select year", color = LocalAppColors.current.iconTint) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        leadingIcon = { Icon(Icons.Outlined.CalendarToday, null, tint = LocalAppColors.current.primary) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = LocalAppColors.current.primary,
                                            unfocusedBorderColor = LocalAppColors.current.divider,
                                            unfocusedContainerColor = LocalAppColors.current.surfaceVariant.copy(alpha = 0.3f),
                                            focusedContainerColor = LocalAppColors.current.surfaceVariant.copy(alpha = 0.3f)
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedYear,
                                        onDismissRequest = { expandedYear = false },
                                        modifier = Modifier.background(LocalAppColors.current.surface)
                                    ) {
                                        years.forEach { year ->
                                            DropdownMenuItem(
                                                text = { Text(year) },
                                                onClick = {
                                                    graduationYear = year
                                                    expandedYear = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                "Contact & Social",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = LocalAppColors.current.textTitle
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            AlumniFormField(
                                label = "Phone Number",
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                placeholder = "+91 XXXXX XXXXX",
                                icon = Icons.Outlined.Phone
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            AlumniFormField(
                                label = "LinkedIn Profile",
                                value = linkedInProfile,
                                onValueChange = { linkedInProfile = it },
                                placeholder = "linkedin.com/in/username",
                                icon = Icons.Outlined.Link
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                "About You",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = LocalAppColors.current.textTitle
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = bio,
                                onValueChange = { if (it.length <= 300) bio = it },
                                modifier = Modifier.fillMaxWidth().height(140.dp),
                                placeholder = { 
                                    Text(
                                        "Share a short bio about your achievement...", 
                                        color = LocalAppColors.current.iconTint,
                                        fontSize = 15.sp
                                    ) 
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LocalAppColors.current.primary,
                                    unfocusedBorderColor = LocalAppColors.current.divider,
                                    unfocusedContainerColor = LocalAppColors.current.surfaceVariant.copy(alpha = 0.3f),
                                    focusedContainerColor = LocalAppColors.current.surfaceVariant.copy(alpha = 0.3f)
                                )
                            )
                            Text(
                                "${bio.length}/300",
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalAppColors.current.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Submit Action
                    Button(
                        onClick = {
                            if (phoneNumber.isEmpty() || currentCompany.isEmpty() || designation.isEmpty()) {
                                Toast.makeText(context, "Please fill in essential fields", Toast.LENGTH_SHORT).show()
                            } else if (!isPhoneValid) {
                                Toast.makeText(context, "Please enter a valid 10-digit mobile number starting with 6, 7, 8, or 9", Toast.LENGTH_SHORT).show()
                            } else {
                                scope.launch {
                                    isSubmitting = true
                                    try {
                                        val request = com.simats.interviewassist.data.models.ProfileRequest(
                                            userId = preferenceManager.getUserId(),
                                            firstName = preferenceManager.getUserName().split(" ").getOrNull(0),
                                            lastName = preferenceManager.getUserName().split(" ").let { if (it.size > 1) it.drop(1).joinToString(" ") else "" },
                                            email = preferenceManager.getEmail(),
                                            phoneNumber = phoneNumber,
                                            major = designation, // Using designation as major/field for alumni
                                            expectedGradYear = graduationYear,
                                            currentYear = "Alumni",
                                            bio = bio,
                                            linkedinUrl = linkedInProfile,
                                            currentCompany = currentCompany,
                                            designation = designation
                                        )
                                        val response = RetrofitClient.apiService.requestAlumniUpgrade(request)
                                        if (response.isSuccessful) {
                                            preferenceManager.saveStatus("pending")
                                            Toast.makeText(context, "Success! Your profile is sent for verification.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Submission failed. Please try again.", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = LocalAppColors.current.primary.copy(alpha = 0.5f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.linearGradient(colors = listOf(LocalAppColors.current.primary, Color(0xFF1D4ED8)))),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 3.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Complete Upgrade", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ChevronRight, null, tint = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Maybe Later", color = LocalAppColors.current.textSecondary)
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

}

@Composable
fun AlumniFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label, 
            fontSize = 14.sp, 
            fontWeight = FontWeight.SemiBold, 
            color = LocalAppColors.current.textTitle,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = LocalAppColors.current.iconTint, fontSize = 15.sp) },
            leadingIcon = { Icon(icon, null, tint = LocalAppColors.current.primary, modifier = Modifier.size(22.dp)) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LocalAppColors.current.primary,
                unfocusedBorderColor = LocalAppColors.current.divider,
                unfocusedContainerColor = LocalAppColors.current.surfaceVariant.copy(alpha = 0.3f),
                focusedContainerColor = LocalAppColors.current.surfaceVariant.copy(alpha = 0.3f)
            )
        )
    }
}
