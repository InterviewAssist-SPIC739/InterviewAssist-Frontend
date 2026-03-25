package com.simats.interviewassist.ui.screens.alumni

import androidx.compose.foundation.Image
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import com.simats.interviewassist.ui.theme.*
import com.simats.interviewassist.ui.screens.student.EditProfileSectionTitle
import com.simats.interviewassist.ui.screens.student.EditProfileField
import com.simats.interviewassist.ui.screens.student.EditProfileDropdown
import com.simats.interviewassist.ui.screens.student.EditProfileBioField
import com.simats.interviewassist.ui.theme.LocalAppColors
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap


import com.simats.interviewassist.utils.PreferenceManager
import com.simats.interviewassist.utils.ProfilePicManager
import com.simats.interviewassist.data.network.RetrofitClient
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.widget.Toast


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniEditProfileScreen(
    preferenceManager: PreferenceManager,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) } // Start false — show local data immediately

    // Pre-populate from local cache instantly (zero wait time)
    val localName = remember { preferenceManager.getUserName().split(" ") }
    var firstName by remember { mutableStateOf(localName.getOrNull(0) ?: "") }
    var lastName by remember { mutableStateOf(if (localName.size > 1) localName.subList(1, localName.size).joinToString(" ") else "") }
    var email by remember { mutableStateOf(preferenceManager.getEmail()) }
    var phoneNumber by remember { mutableStateOf(preferenceManager.getPhoneNumber().ifBlank { "+91 " }) }
    
    var currentCompany by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var graduationYear by remember { mutableStateOf(preferenceManager.getGradYear()) }
    
    var linkedIn by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    
    var bio by remember { mutableStateOf(preferenceManager.getBio()) }
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
                        // Refresh from server (silently, user already sees local data)
                        firstName = it.firstName
                        lastName = it.lastName
                        email = it.email
                        it.profile?.let { p ->
                            if (p.phoneNumber != null) phoneNumber = p.phoneNumber
                            if (!p.currentCompany.isNullOrEmpty()) currentCompany = p.currentCompany
                            if (!p.designation.isNullOrEmpty()) designation = p.designation
                            if (!p.expectedGradYear.isNullOrEmpty()) graduationYear = p.expectedGradYear
                            if (!p.specialization.isNullOrEmpty()) specialization = p.specialization
                            if (!p.bio.isNullOrEmpty()) bio = p.bio
                            if (!p.profilePic.isNullOrEmpty()) {
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
                // Local data already shown, silently ignore network error
            }
        }
    }
    
    // Derived full name for display logic
    val fullName = "$firstName $lastName".trim()

    var expandedGrad by remember { mutableStateOf(false) }
    val gradYears = listOf("2020", "2021", "2022", "2023", "2024")

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
                                    profilePic!!.startsWith("/") -> java.io.File(profilePic!!)
                                    else -> "${RetrofitClient.BASE_URL.removeSuffix("/")}/${profilePic!!.removePrefix("/")}"
                                }
                            }
                            else -> null
                        }

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

                        if (currentImageModel != null) {
                            coil.compose.SubcomposeAsyncImage(
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
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(2.dp, LocalAppColors.current.surface)
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
                    EditProfileField("Email", email, icon = Icons.Outlined.Email) { email = it }
                    Spacer(modifier = Modifier.height(20.dp))
                    EditProfileField("Phone Number", phoneNumber) { phoneNumber = it }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Work Info
            EditProfileSectionTitle("Work Info")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    EditProfileField("Current Company", currentCompany) { currentCompany = it }
                    Spacer(modifier = Modifier.height(20.dp))
                    EditProfileField("Designation", designation) { designation = it }
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Box {
                        EditProfileDropdown("Graduation Year", graduationYear) { expandedGrad = true }
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
                    EditProfileField("Area of Specialization", specialization) { specialization = it }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Social Links
            EditProfileSectionTitle("Social Links")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    EditProfileField("LinkedIn Profile", linkedIn) { linkedIn = it }
                    Spacer(modifier = Modifier.height(20.dp))
                    EditProfileField("Personal Website", website) { website = it }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About
            EditProfileSectionTitle("About")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    EditProfileBioField("Bio", bio) { if (it.length <= 300) bio = it }
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
                        scope.launch {
                            try {
                                val userId = preferenceManager.getUserId()
                                val request = com.simats.interviewassist.data.models.ProfileRequest(
                                    userId = userId,
                                    phoneNumber = phoneNumber,
                                    major = null,
                                    expectedGradYear = graduationYear,
                                    currentYear = "Alumni",
                                    bio = bio,
                                    profilePic = if (profilePic?.startsWith("/data/user/") == true) null else profilePic,
                                    currentCompany = currentCompany,
                                    designation = designation,
                                    linkedinUrl = linkedIn,
                                    specialization = specialization
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
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
