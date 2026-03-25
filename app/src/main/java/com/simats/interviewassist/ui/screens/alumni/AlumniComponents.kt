package com.simats.interviewassist.ui.screens.alumni

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.simats.interviewassist.ui.theme.LocalAppColors
import com.simats.interviewassist.ui.screens.student.ExperienceSectionCard

@Composable
fun EditSectionCard(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Write here..."
) {
    ExperienceSectionCard(
        title = title,
        icon = icon,
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            placeholder = { Text(placeholder, color = contentColor.copy(alpha = 0.6f)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = contentColor,
                unfocusedBorderColor = contentColor.copy(alpha = 0.4f),
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                focusedTextColor = contentColor,
                unfocusedTextColor = contentColor
            )
        )
    }
}
