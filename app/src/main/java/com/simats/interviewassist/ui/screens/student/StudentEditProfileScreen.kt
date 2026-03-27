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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.simats.interviewassist.ui.theme.*
import com.simats.interviewassist.ui.theme.LocalAppColors


import com.simats.interviewassist.utils.PreferenceManager
import com.simats.interviewassist.utils.ProfilePicManager
import com.simats.interviewassist.data.network.RetrofitClient
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.widget.Toast
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentEditProfileScreen(
    preferenceManager: PreferenceManager,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val savedPhone = preferenceManager.getPhoneNumber()
    var phoneNumber by remember { 
        mutableStateOf(
            if (savedPhone.isBlank()) "+91 " 
            else if (savedPhone.startsWith("+91")) savedPhone 
            else "+91 $savedPhone"
        ) 
    }
    var department by remember { mutableStateOf("") }
    var currentYear by remember { mutableStateOf("") }
    var graduationYear by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profilePic by remember { mutableStateOf<String?>(preferenceManager.getProfilePicPath()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                    val byteArray = outputStream.toByteArray()
                    profilePic = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                } catch (e: Exception) {
                    Toast.makeText(context, "Error processing image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val userId = preferenceManager.getUserId()
        if (userId != -1) {
            try {
                val response = RetrofitClient.apiService.getUserProfile(userId)
                if (response.isSuccessful) {
                    val profile = response.body()
                    profile?.let {
                        firstName = it.firstName
                        lastName = it.lastName
                        email = it.email
                        it.profile?.let { p ->
                            val pPhone = p.phoneNumber ?: ""
                            phoneNumber = if (pPhone.isBlank()) "+91 " 
                                        else if (pPhone.startsWith("+91")) pPhone 
                                        else "+91 $pPhone"
                            department = p.major ?: ""
                            currentYear = p.currentYear ?: ""
                            graduationYear = p.expectedGradYear ?: ""
                            bio = p.bio ?: ""
                            if (!p.profilePic.isNullOrEmpty()) {
                                // If backend returns base64, save it to disk and use the stable disk path to prevent UI lag/crashes
                                if (p.profilePic.length > 500) {
                                    ProfilePicManager.saveBase64Image(context, p.profilePic, preferenceManager)
                                    profilePic = preferenceManager.getProfilePicPath()
                                } else {
                                    profilePic = p.profilePic
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to basic prefs if any
                val localName = preferenceManager.getUserName().split(" ")
                firstName = localName.getOrNull(0) ?: ""
                lastName = if (localName.size > 1) localName.subList(1, localName.size).joinToString(" ") else ""
                email = preferenceManager.getEmail()
            } finally {
                isLoading = false
            }
        }
    }
    
    // Derived full name for display logic if needed
    val fullName = "$firstName $lastName".trim()

    var expandedYear by remember { mutableStateOf(false) }
    var expandedGrad by remember { mutableStateOf(false) }
    val years = listOf("1st Year", "2nd Year", "3rd Year", "Final Year")
    val gradYears = listOf("2024", "2025", "2026", "2027")

    val phoneRegex = Regex("^\\+91 ?[6-9][0-9]{9}$")
    val isPhoneValid = phoneRegex.matches(phoneNumber.trim())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Edit Profile", 
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
        containerColor = LocalAppColors.current.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Profile Photo Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = LocalAppColors.current.borderLight
                    ) {
                    var bitmap by remember(profilePic) { mutableStateOf<android.graphics.Bitmap?>(null) }
                        
                        val initialsText = (if (firstName.isNotEmpty() || lastName.isNotEmpty()) "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}" else "UP").uppercase()
                        val fallbackContent: @Composable () -> Unit = {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(LocalAppColors.current.primaryHighlight)) {
                                Text(
                                    text = initialsText,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LocalAppColors.current.primary
                                )
                            }
                        }

                        val currentImageModel: Any? = when {
                            selectedImageUri != null -> selectedImageUri
                            !profilePic.isNullOrEmpty() -> {
                                when {
                                    profilePic!!.length > 1000 -> {
                                        val cleanBase64 = if (profilePic!!.contains(",")) profilePic!!.substringAfter(",") else profilePic!!
                                        try {
                                            android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                                        } catch (e: Exception) {
                                            ByteArray(0)
                                        }
                                    }
                                    profilePic!!.startsWith("http") -> profilePic!!
                                    profilePic!!.startsWith("/") -> File(profilePic!!)
                                    else -> "${RetrofitClient.BASE_URL.removeSuffix("/")}/${profilePic!!.removePrefix("/")}"
                                }
                            }
                            else -> null
                        }

                        if (currentImageModel != null) {
                            SubcomposeAsyncImage(
                                model = currentImageModel,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                loading = { fallbackContent() },
                                error = { fallbackContent() }
                            )
                        } else {
                            fallbackContent()
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        color = LocalAppColors.current.primary,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CameraAlt, 
                                null, 
                                modifier = Modifier.size(16.dp), 
                                tint = LocalAppColors.current.surface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Change Profile Photo", 
                    color = LocalAppColors.current.primary, 
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Personal Info
            EditProfileSectionTitle("Personal Info")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    EditProfileField("First Name", firstName) { firstName = it }
                    Spacer(modifier = Modifier.height(20.dp))
                    EditProfileField("Last Name", lastName) { lastName = it }
                    Spacer(modifier = Modifier.height(20.dp))
                    EditProfileField("Email", email, icon = Icons.Outlined.Email, readOnly = true) { /* email is now non-editable */ }
                    Spacer(modifier = Modifier.height(20.dp))
                    EditProfileField("Phone Number", phoneNumber) { phoneNumber = it }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Academic Info
            EditProfileSectionTitle("Academic Info")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    EditProfileField("Major/Department", department) { department = it }
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Box {
                        EditProfileDropdown("Current Year", currentYear) { expandedYear = true }
                        DropdownMenu(
                            expanded = expandedYear,
                            onDismissRequest = { expandedYear = false },
                            modifier = Modifier.background(LocalAppColors.current.surface)
                        ) {
                            years.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year) },
                                    onClick = {
                                        currentYear = year
                                        expandedYear = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Box {
                        EditProfileDropdown("Expected Graduation", graduationYear) { expandedGrad = true }
                        DropdownMenu(
                            expanded = expandedGrad,
                            onDismissRequest = { expandedGrad = false },
                            modifier = Modifier.background(LocalAppColors.current.surface)
                        ) {
                            gradYears.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year) },
                                    onClick = {
                                        graduationYear = year
                                        expandedGrad = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    EditProfileBioField("Bio", bio) { if (it.length <= 200) bio = it }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.divider),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = LocalAppColors.current.textBody, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        if (!isPhoneValid) {
                            Toast.makeText(context, "Please enter 10 digits after +91 starting with 6, 7, 8, or 9", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch {
                            try {
                                val userId = preferenceManager.getUserId()
                                val request = com.simats.interviewassist.data.models.ProfileRequest(
                                    userId = userId,
                                    firstName = firstName,
                                    lastName = lastName,
                                    email = email,
                                    phoneNumber = phoneNumber,
                                    major = department,
                                    expectedGradYear = graduationYear,
                                    currentYear = currentYear,
                                    bio = bio,
                                    profilePic = if (profilePic?.startsWith("/data/user/") == true) null else profilePic
                                )
                                val response = RetrofitClient.apiService.completeProfile(request)
                                if (response.isSuccessful) {
                                    preferenceManager.saveUserDetails(firstName, lastName, email)
                                    // Cache profile picture
                                    ProfilePicManager.saveBase64Image(context, profilePic, preferenceManager)
                                    onSave()
                                } else {
                                    Toast.makeText(context, "Failed to save profile: ${response.message()}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error saving profile: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes", color = LocalAppColors.current.surface, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun EditProfileSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = LocalAppColors.current.textTitle,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )
}

@Composable
fun EditProfileField(
    label: String, 
    value: String, 
    icon: ImageVector? = null,
    readOnly: Boolean = false,
    onValueChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    Column(modifier = Modifier.clickable { focusRequester.requestFocus() }) {
        Text(label, fontSize = 13.sp, color = LocalAppColors.current.textSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = if (readOnly) ({}) else onValueChange,
            readOnly = readOnly,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            leadingIcon = if (icon != null) {
                { Icon(icon, null, tint = LocalAppColors.current.iconTint, modifier = Modifier.size(20.dp)) }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = LocalAppColors.current.primary,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = LocalAppColors.current.textTitle,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        )
        // Bottom line divider
        HorizontalDivider(color = LocalAppColors.current.divider, thickness = 1.dp)
    }
}

@Composable
fun EditProfileDropdown(label: String, value: String, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onClick() }) {
        Text(label, fontSize = 13.sp, color = LocalAppColors.current.textSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = LocalAppColors.current.textTitle)
            Icon(Icons.Default.KeyboardArrowDown, null, tint = LocalAppColors.current.iconTint)
        }
        HorizontalDivider(color = LocalAppColors.current.divider, thickness = 1.dp)
    }
}

@Composable
fun EditProfileBioField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, fontSize = 13.sp, color = LocalAppColors.current.textSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = LocalAppColors.current.primary,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = LocalAppColors.current.textTitle,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        )
        HorizontalDivider(color = LocalAppColors.current.divider, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "${value.length}/200", 
            modifier = Modifier.align(Alignment.End),
            fontSize = 11.sp,
            color = LocalAppColors.current.borderLight
        )
    }
}
