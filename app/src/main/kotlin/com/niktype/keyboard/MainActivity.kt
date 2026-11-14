package com.niktype.keyboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.niktype.keyboard.data.KeyboardSettings
import com.niktype.keyboard.onboarding.OnboardingFlow
import com.niktype.keyboard.settings.AboutScreen
import com.niktype.keyboard.settings.AppearanceScreen
import com.niktype.keyboard.settings.SettingsHome
import com.niktype.keyboard.settings.ShortcutsScreen
import com.niktype.keyboard.settings.TypingScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as NikApp).container
        setContent {
            NikAppTheme {
                AppRoot(container)
            }
        }
    }
}

@Composable
fun NikAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF7C4DFF),
            secondary = Color(0xFF448AFF),
            background = Color(0xFF12121C),
            surface = Color(0xFF1A1A26),
            surfaceVariant = Color(0xFF232336),
            onBackground = Color(0xFFEDEDF5),
            onSurface = Color(0xFFEDEDF5),
        ),
        content = content,
    )
}

@Composable
fun AppRoot(container: com.niktype.keyboard.AppContainer) {
    val settings by container.settingsRepo.flow.collectAsState(initial = KeyboardSettings())
    var screen by remember { mutableStateOf("home") }

    if (!settings.onboardingDone) {
        OnboardingFlow(container)
    } else {
        when (screen) {
            "appearance" -> AppearanceScreen(container) { screen = "home" }
            "typing" -> TypingScreen(container) { screen = "home" }
            "shortcuts" -> ShortcutsScreen(container) { screen = "home" }
            "about" -> AboutScreen(container) { screen = "home" }
            else -> SettingsHome(container, onOpen = { screen = it })
        }
    }
}
