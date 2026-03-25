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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
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
fun ExperiencesScreen(onNext: () -> Unit, onSkip: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val nextInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val isNextPressed by nextInteractionSource.collectIsPressedAsState()
    val nextScale by animateFloatAsState(
        targetValue = if (isNextPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "NextButtonScale"
    )

    val skipInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val isSkipPressed by skipInteractionSource.collectIsPressedAsState()
    val skipAlpha by animateFloatAsState(
        targetValue = if (isSkipPressed) 0.6f else 1f,
        label = "SkipButtonAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAppColors.current.surface)
    ) {
        // Decorative background blob 1
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-50).dp)
                .graphicsLayer { alpha = 0.05f }
                .background(LocalAppColors.current.primary, CircleShape)
        )

        // Decorative background blob 2 
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 80.dp)
                .graphicsLayer { alpha = 0.07f }
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

            // Image Container - Animates first
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
                        painter = painterResource(id = R.drawable.onboarding_img_2),
                        contentDescription = "Experiences Image",
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
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(LocalAppColors.current.primary)
                    )
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(LocalAppColors.current.borderLight))
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
                    text = "Learn from \nReal Experiences",
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
                    text = "Access hundreds of verified interview experiences shared by seniors who've been there.",
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
            Spacer(modifier = Modifier.height(24.dp)) // Reduced to nudge up

            // Buttons - Staggered delay 700ms - NOW ALIGNED HORIZONTALLY
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 700)) + 
                        scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 48.dp), // Moved up from 32.dp
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onSkip,
                        interactionSource = skipInteractionSource,
                        modifier = Modifier.graphicsLayer { alpha = skipAlpha }
                    ) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.labelLarge,
                            color = LocalAppColors.current.textBody.copy(alpha = 0.8f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = onNext,
                        interactionSource = nextInteractionSource,
                        modifier = Modifier
                            .width(140.dp)
                            .height(58.dp)
                            .graphicsLayer {
                                scaleX = nextScale
                                scaleY = nextScale
                            },
                        colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Next",
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LocalAppColors.current.surface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = LocalAppColors.current.surface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
