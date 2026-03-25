package com.simats.interviewassist.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.simats.interviewassist.R
import com.simats.interviewassist.ui.theme.JakartaFontFamily
import com.simats.interviewassist.ui.theme.LocalAppColors
import android.content.Intent
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import com.simats.interviewassist.ui.screens.subscription.SubscriptionActivity
import kotlinx.coroutines.delay
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun LoginSuccessScreen(
    role: String,
    onNavigateToHome: (String) -> Unit
) {
    val context = LocalContext.current
    var subscriptionStarted by rememberSaveable { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.success_animation)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    LaunchedEffect(Unit) {
        if (!subscriptionStarted) {
            delay(2200)
            if (role.trim().equals("Student", ignoreCase = true)) {
                subscriptionStarted = true
                val intent = Intent(context, SubscriptionActivity::class.java)
                context.startActivity(intent)
            } else {
                onNavigateToHome(role)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && subscriptionStarted) {
                onNavigateToHome(role)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9),
                        Color(0xFFFFFFFF)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!subscriptionStarted) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(220.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Login Successful",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = LocalAppColors.current.textTitle
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Welcome back, $role",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = JakartaFontFamily
                    ),
                    color = LocalAppColors.current.textBody
                )

            }
        }
    }
}