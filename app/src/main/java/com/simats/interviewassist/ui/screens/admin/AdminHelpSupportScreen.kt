package com.simats.interviewassist.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.*
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.utils.PdfManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHelpSupportScreen(
    onBack: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Help & Support", 
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LocalAppColors.current.background)
                .padding(padding),
            contentPadding = PaddingValues(24.dp)
        ) {
            item {
                ContactCard(
                    modifier = Modifier.fillMaxWidth().clickable {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                            data = android.net.Uri.parse("mailto:admin@gmail.com")
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Support Request - Admin")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback
                        }
                    },
                    icon = Icons.Outlined.Email,
                    title = "Email Us",
                    subtitle = "admin@gmail.com",
                    iconBg = Color(0xFFD1FAE5),
                    iconTint = LocalAppColors.current.success
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            // FAQs
            item {
                Text(
                    "Frequently Asked Questions",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = LocalAppColors.current.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface)
                ) {
                    Column {
                        FaqItem(
                            question = "How do I save an interview experience?",
                            answer = "Tap the bookmark icon on any experience card."
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = LocalAppColors.current.divider)
                        FaqItem(
                            question = "How can I ask a question to alumni?",
                            answer = "Go to any company page and use the Q&A section."
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = LocalAppColors.current.divider)
                        FaqItem(
                            question = "How do I report inappropriate content?",
                            answer = "Hold down on any review and select 'Report'."
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = LocalAppColors.current.divider)
                        FaqItem(
                            question = "How do I manage administrative settings?",
                            answer = "You can access system settings and security controls from the Admin Dashboard and Settings sections."
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = LocalAppColors.current.divider)
                        FaqItem(
                            question = "Can I recover a deleted user account?",
                            answer = "No, account deletion is permanent for security reasons. Users must register again if they wish to return."
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Resources
            item {
                Text(
                    "Resources",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = LocalAppColors.current.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                             .clickable { 
                                val uri = PdfManager.generateUserGuidePdf(context)
                                if (uri != null) {
                                    scope.launch { snackbarHostState.showSnackbar("User Guide downloaded to your Downloads folder.") }
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("Failed to download User Guide.") }
                                }
                            }
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = LocalAppColors.current.divider
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Description, null, modifier = Modifier.size(20.dp), tint = LocalAppColors.current.textTitle)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "User Guide",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = LocalAppColors.current.textTitle,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Outlined.Launch,
                            null,
                            tint = LocalAppColors.current.borderLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBg: Color,
    iconTint: Color
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = iconBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, modifier = Modifier.size(24.dp), tint = iconTint)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = LocalAppColors.current.textTitle
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = LocalAppColors.current.textSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun FaqItem(
    question: String,
    answer: String
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = question,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = LocalAppColors.current.textTitle,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (expanded) Icons.Default.ArrowBack else Icons.Default.ChevronRight,
                null,
                tint = LocalAppColors.current.borderLight,
                modifier = Modifier.size(20.dp).let { 
                    if (expanded) it.then(Modifier.background(Color.Transparent)) // lazy way to rotate icon
                    else it 
                }
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = answer,
                fontSize = 14.sp,
                color = LocalAppColors.current.textSecondary,
                lineHeight = 20.sp
            )
        }
    }
}
