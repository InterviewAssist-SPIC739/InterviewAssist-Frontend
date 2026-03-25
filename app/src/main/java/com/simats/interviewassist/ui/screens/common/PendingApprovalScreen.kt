package com.simats.interviewassist.ui.screens.common

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.JakartaFontFamily
import com.simats.interviewassist.ui.theme.LocalAppColors

@Composable
fun PendingApprovalScreen(
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAppColors.current.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Pending Status Icon
            Surface(
                modifier = Modifier.size(120.dp),
                color = LocalAppColors.current.primaryHighlight,
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = LocalAppColors.current.primary,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Account Pending Approval",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = LocalAppColors.current.textTitle,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Thank you for joining Interview Assist! Your alumni account is currently being reviewed by our administration.\n\nYou will be able to access all features once your request is approved.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = JakartaFontFamily
                ),
                color = LocalAppColors.current.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalAppColors.current.surfaceVariant,
                    contentColor = LocalAppColors.current.textTitle
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Sign Out",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = JakartaFontFamily
                    )
                }
            }
        }
    }
}
