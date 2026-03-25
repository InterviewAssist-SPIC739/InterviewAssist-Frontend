package com.simats.interviewassist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.simats.interviewassist.ui.navigation.AppNavigation
import com.simats.interviewassist.ui.theme.InterviewAssistTheme
import com.simats.interviewassist.utils.PreferenceManager
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(this)
        enableEdgeToEdge()
        setContent {
            val isDark by preferenceManager.isDarkModeState
            InterviewAssistTheme(darkTheme = isDark) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Navigation handles its own padding or full-screen layout
                    AppNavigation(preferenceManager = preferenceManager)
                }
            }
        }
    }
}