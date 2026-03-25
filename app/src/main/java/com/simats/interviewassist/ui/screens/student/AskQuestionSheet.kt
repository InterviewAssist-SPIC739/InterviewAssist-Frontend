package com.simats.interviewassist.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskQuestionSheet(
    companyName: String,
    onDismiss: () -> Unit,
    onPostQuestion: (String) -> Unit
) {
    var questionText by remember { mutableStateOf("") }
    val maxCharCount = 300

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .imePadding() // Handles keyboard overlap
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ask a Question",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LocalAppColors.current.textTitle
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = LocalAppColors.current.iconTint)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ask about $companyName's interview process, work culture, or anything else you'd like to know.",
            fontSize = 14.sp,
            color = LocalAppColors.current.textSecondary,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Input Field
        OutlinedTextField(
            value = questionText,
            onValueChange = { if (it.length <= maxCharCount) questionText = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            placeholder = {
                Text("Type your question here...", color = LocalAppColors.current.iconTint)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LocalAppColors.current.primary,
                unfocusedBorderColor = LocalAppColors.current.borderLight,
                focusedContainerColor = LocalAppColors.current.surfaceVariant,
                unfocusedContainerColor = LocalAppColors.current.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Counter
        Text(
            text = "${questionText.length}/$maxCharCount",
            fontSize = 12.sp,
            color = LocalAppColors.current.iconTint,
            modifier = Modifier.align(Alignment.End)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Post Button
        Button(
            onClick = {
                if (questionText.isNotBlank()) {
                    onPostQuestion(questionText)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LocalAppColors.current.primary,
                disabledContainerColor = LocalAppColors.current.primary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = questionText.isNotBlank()
        ) {
            Text(
                text = "Post Question",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
