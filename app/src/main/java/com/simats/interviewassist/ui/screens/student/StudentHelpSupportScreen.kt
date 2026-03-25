package com.simats.interviewassist.ui.screens.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.*
import com.simats.interviewassist.ui.theme.LocalAppColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHelpSupportScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Help & Support", 
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
            val context = androidx.compose.ui.platform.LocalContext.current
            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

            SupportCard(
                icon = Icons.Outlined.Email,
                iconColor = LocalAppColors.current.success,
                iconBg = LocalAppColors.current.successBg,
                title = "Email Us",
                subtitle = "admin@gmail.com",
                modifier = Modifier.fillMaxWidth().clickable {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                        data = android.net.Uri.parse("mailto:admin@gmail.com")
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Support Request - InterviewAssist")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Fallback
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // FAQ Section
            Text(
                "Frequently Asked Questions",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = LocalAppColors.current.textSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface
            ) {
                Column {
                    FaqItem(
                        question = "How do I save an interview experience?",
                        answer = "Tap the bookmark icon on any experience card. You can view your saved items in the profile screen."
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = LocalAppColors.current.divider)
                    FaqItem(
                        question = "How can I ask a question to alumni?",
                        answer = "Go to any company page and use the Q&A section to post your question. Alumni who worked there will be notified."
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = LocalAppColors.current.divider)
                    FaqItem(
                        question = "How do I report inappropriate content?",
                        answer = "Click the three dots menu on any post or comment and select 'Report'. Our team will review it within 24 hours."
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = LocalAppColors.current.divider)
                    FaqItem(
                        question = "How do I enable Two-Factor Authentication?",
                        answer = "Go to Settings > Privacy & Security and toggle the 2FA switch. You'll need to provide a secondary email for security."
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = LocalAppColors.current.divider)
                    FaqItem(
                        question = "Can I delete my account?",
                        answer = "Yes, you can permanently delete your account and all associated data from Settings > Privacy & Security > Danger Zone."
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = LocalAppColors.current.divider)
                    FaqItem(
                        question = "What is a secondary email?",
                        answer = "A secondary email is used as a backup to receive 2FA codes and security alerts if your primary email is inaccessible."
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Resources Section
            Text(
                "Resources",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = LocalAppColors.current.textSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LocalAppColors.current.surface
            ) {
                Row(
                    modifier = Modifier
                            .clickable { 
                                val uri = com.simats.interviewassist.utils.PdfManager.generateUserGuidePdf(context)
                                if (uri != null) {
                                    scope.launch { snackbarHostState.showSnackbar("User Guide downloaded to your Downloads folder.") }
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("Failed to download User Guide.") }
                                }
                            }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = LocalAppColors.current.surfaceHighlight
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Description, null, tint = LocalAppColors.current.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "User Guide", 
                        fontSize = 15.sp, 
                        fontWeight = FontWeight.Medium, 
                        color = LocalAppColors.current.textTitle,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Outlined.OpenInNew, null, tint = LocalAppColors.current.iconTint, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SupportCard(
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(20.dp),
        color = LocalAppColors.current.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = iconBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LocalAppColors.current.textTitle)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                subtitle, 
                fontSize = 11.sp, 
                color = LocalAppColors.current.textSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                question, 
                fontSize = 15.sp, 
                fontWeight = FontWeight.Medium, 
                color = LocalAppColors.current.textTitle,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ChevronRight, 
                null, 
                tint = LocalAppColors.current.borderLight,
                modifier = Modifier.rotate(rotation)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    answer, 
                    fontSize = 14.sp, 
                    color = LocalAppColors.current.textSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
