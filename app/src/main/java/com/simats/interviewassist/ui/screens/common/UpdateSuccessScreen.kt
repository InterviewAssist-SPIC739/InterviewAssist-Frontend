package com.simats.interviewassist.ui.screens.common

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.ui.theme.JakartaFontFamily
import com.simats.interviewassist.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

@Composable
fun UpdateSuccessScreen(
    role: String,
    onBackToProfile: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CircleScale"
    )

    val opacity by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "ContentOpacity"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAppColors.current.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Success Circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF4CAF50),
                                Color(0xFF81C784)
                            )
                        ),
                        RoundedCornerShape(60.dp)
                    )
                    .padding(30.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Success Text
            Text(
                text = "Profile Updated Successfully!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = LocalAppColors.current.textTitle,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = opacity
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your changes have been saved and applied to your profile.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = JakartaFontFamily
                ),
                color = LocalAppColors.current.textBody,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = opacity
                }
            )
            
            Spacer(modifier = Modifier.height(60.dp))

            // Functionable Button
            Button(
                onClick = onBackToProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .graphicsLayer {
                        alpha = opacity
                    },
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Back to Profile", 
                    color = LocalAppColors.current.surface, 
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
