package com.simats.interviewassist.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.interviewassist.R
import com.simats.interviewassist.ui.theme.JakartaFontFamily
import com.simats.interviewassist.ui.theme.LocalAppColors

@Composable
fun ConnectSucceedScreen(onGetStarted: () -> Unit, onSkip: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val isPressed by startInteractionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "StartButtonScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAppColors.current.surface)
    ) {
        // Decorative background blob 1
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(x = (-120).dp, y = (-80).dp)
                .graphicsLayer { alpha = 0.06f }
                .background(LocalAppColors.current.primary, CircleShape)
        )

        // Decorative background blob 2 
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .graphicsLayer { alpha = 0.05f }
                .background(LocalAppColors.current.primary, CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Onboarding Image Container - Animates first
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000)) + expandVertically(animationSpec = tween(1000))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFFF5F5F5)) 
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.onboarding_img_3),
                        contentDescription = "Connect & Succeed Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Indicator Dots - Animates with Image
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000, delayMillis = 100))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(LocalAppColors.current.borderLight))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(LocalAppColors.current.borderLight))
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(LocalAppColors.current.primary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Title - Staggered delay 300ms
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 300)) + 
                        slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessLow), initialOffsetY = { 40 })
            ) {
                Text(
                    text = "Connect & \nSucceed",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = JakartaFontFamily,
                        lineHeight = 38.sp,
                        letterSpacing = (-0.5).sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = LocalAppColors.current.textTitle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description - Staggered delay 500ms
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 500)) + 
                        slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessLow), initialOffsetY = { 40 })
            ) {
                Text(
                    text = "Ask questions, get answers, and prepare confidently for your dream job.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = JakartaFontFamily,
                        lineHeight = 24.sp,
                        letterSpacing = 0.2.sp
                    ),
                    color = LocalAppColors.current.textBody,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Button - Staggered delay 700ms - NOW CENTERED
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 700)) + 
                        scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 48.dp), // Moved up from 32.dp
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onGetStarted,
                        interactionSource = startInteractionSource,
                        modifier = Modifier
                            .width(200.dp) // Slightly wider for a centered primary button
                            .height(58.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            },
                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
                    ) {
                        Text(
                            text = "Get Started",
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LocalAppColors.current.surface
                        )
                    }
                }
            }
        }
    }
}
