package com.niktype.keyboard.onboarding

import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niktype.keyboard.AppContainer
import com.niktype.keyboard.R
import com.niktype.engine.touch.FingerProfileKind
import com.niktype.keyboard.data.SettingsRepository
import kotlinx.coroutines.launch

/**
 * راه‌اندازی مرحله‌ای شش‌گانه NikType.
 */
@Composable
fun OnboardingFlow(container: AppContainer) {
    val pagerState = rememberPagerState(pageCount = { 6 })
    val scope = rememberCoroutineScope()
    val settings by container.settingsRepo.flow.collectAsState(initial = com.niktype.keyboard.data.KeyboardSettings())

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF12121C))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.ob_step, pagerState.currentPage + 1),
            color = Color(0xFF9C7BFF),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(16.dp))

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            when (page) {
                0 -> WelcomeStep()
                1 -> LanguageStep()
                2 -> FingerStep(container, settings.fingerProfile)
                3 -> SizeStep(container, settings)
                4 -> EnableStep()
                5 -> DefaultStep(onFinish = {
                    scope.launch {
                        container.settingsRepo.update { it.copy(onboardingDone = true) }
                    }
                })
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(6) { i ->
                val active = pagerState.currentPage == i
                Box(
                    Modifier
                        .padding(4.dp)
                        .size(if (active) 10.dp else 8.dp)
                        .background(
                            if (active) Color(0xFF7C4DFF) else Color(0xFF33334A),
                            CircleShape,
                        )
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                scope.launch {
                    val next = (pagerState.currentPage + 1).coerceAtMost(5)
                    if (pagerState.currentPage == 5) {
                        container.settingsRepo.update { it.copy(onboardingDone = true) }
                    } else {
                        pagerState.animateScrollToPage(next)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(
                if (pagerState.currentPage == 5) stringResource(R.string.ob_finish)
                else stringResource(R.string.ob_next),
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun StepTitle(text: String) {
    Text(text, color = Color(0xFFEDEDF5), fontSize = 24.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun StepDesc(text: String) {
    Text(text, color = Color(0xFFB9B9CC), fontSize = 15.sp, lineHeight = 22.sp)
}

@Composable
private fun WelcomeStep() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(120.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF7C4DFF), Color(0xFF448AFF))),
                    RoundedCornerShape(30.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("N", color = Color.White, fontSize = 58.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(28.dp))
        Text("NikType", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(18.dp))
        StepDesc(stringResource(R.string.ob_welcome_desc))
    }
}

@Composable
private fun LanguageStep() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        StepTitle(stringResource(R.string.ob_lang_title))
        Spacer(Modifier.height(12.dp))
        StepDesc(stringResource(R.string.ob_lang_desc))
        Spacer(Modifier.height(28.dp))
        Row {
            LanguageCard("فارسی")
            Spacer(Modifier.width(14.dp))
            LanguageCard("English")
        }
    }
}

@Composable
private fun LanguageCard(name: String) {
    Column(
        Modifier
            .width(140.dp)
            .background(Color(0xFF1C1C2E), RoundedCornerShape(16.dp))
            .padding(vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text("✔", color = Color(0xFF66D9A0), fontSize = 16.sp)
    }
}

@Composable
private fun FingerStep(container: AppContainer, selected: FingerProfileKind) {
    val scope = rememberCoroutineScope()
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        StepTitle(stringResource(R.string.ob_finger_title))
        Spacer(Modifier.height(12.dp))
        StepDesc(stringResource(R.string.ob_finger_desc))
        Spacer(Modifier.height(28.dp))
        for (profile in listOf(
            FingerProfileKind.SMALL to R.string.profile_small,
            FingerProfileKind.NORMAL to R.string.profile_normal,
            FingerProfileKind.LARGE to R.string.profile_large,
        )) {
            val active = selected == profile.first
            val preset = SettingsRepository.presetFor(profile.first)
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp)
                    .background(if (active) Color(0xFF2A2140) else Color(0xFF1C1C2E), RoundedCornerShape(14.dp))
                    .border(
                        width = if (active) 2.dp else 0.dp,
                        color = if (active) Color(0xFF7C4DFF) else Color.Transparent,
                        shape = RoundedCornerShape(14.dp),
                    )
                    .clickable {
                        scope.launch {
                            container.settingsRepo.update {
                                it.copy(
                                    fingerProfile = profile.first,
                                    keyHeightDp = preset.first,
                                    gapDp = preset.second,
                                    keyWidthScale = preset.third,
                                )
                            }
                        }
                    }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(profile.second),
                    color = if (active) Color.White else Color(0xFFB9B9CC),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun SizeStep(container: AppContainer, settings: com.niktype.keyboard.data.KeyboardSettings) {
    val scope = rememberCoroutineScope()
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        StepTitle(stringResource(R.string.ob_size_title))
        Spacer(Modifier.height(12.dp))
        StepDesc(stringResource(R.string.ob_size_desc))
        Spacer(Modifier.height(30.dp))
        SliderLabeled(
            label = stringResource(R.string.s_key_height),
            value = settings.keyHeightDp,
            valueText = "${settings.keyHeightDp.toInt()}",
            range = 40f..80f,
        ) { v ->
            scope.launch { container.settingsRepo.update { it.copy(keyHeightDp = v) } }
        }
        SliderLabeled(
            label = stringResource(R.string.s_key_gap),
            value = settings.gapDp,
            valueText = "${settings.gapDp.toInt()}",
            range = 1f..9f,
        ) { v ->
            scope.launch { container.settingsRepo.update { it.copy(gapDp = v) } }
        }
    }
}

@Composable
private fun SliderLabeled(
    label: String,
    value: Float,
    valueText: String,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color(0xFFB9B9CC), fontSize = 14.sp)
            Text(valueText, color = Color(0xFF9C7BFF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = Color(0xFF7C4DFF),
                activeTrackColor = Color(0xFF7C4DFF),
                inactiveTrackColor = Color(0xFF2A2A3E),
            ),
        )
    }
}

@Composable
private fun EnableStep() {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(checkEnabled(context)) }

    DisposableEffect(Unit) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                enabled = checkEnabled(context)
            }
        }
        val lifecycle = (context as? androidx.lifecycle.LifecycleOwner)?.lifecycle
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        StepTitle(stringResource(R.string.ob_enable_title))
        Spacer(Modifier.height(12.dp))
        StepDesc(stringResource(R.string.ob_enable_desc))
        Spacer(Modifier.height(28.dp))
        if (enabled) {
            Text(stringResource(R.string.ob_enable_done), color = Color(0xFF66D9A0), fontSize = 16.sp)
        } else {
            Button(
                onClick = { context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(stringResource(R.string.ob_enable_action), fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun DefaultStep(onFinish: () -> Unit) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(checkSelected(context)) }

    DisposableEffect(Unit) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                selected = checkSelected(context)
            }
        }
        val lifecycle = (context as? androidx.lifecycle.LifecycleOwner)?.lifecycle
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        StepTitle(stringResource(R.string.ob_default_title))
        Spacer(Modifier.height(12.dp))
        StepDesc(stringResource(R.string.ob_default_desc))
        Spacer(Modifier.height(28.dp))
        if (selected) {
            Text(stringResource(R.string.ob_default_done), color = Color(0xFF66D9A0), fontSize = 16.sp)
        } else {
            Button(
                onClick = {
                    val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showInputMethodPicker()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(stringResource(R.string.ob_default_action), fontSize = 15.sp)
            }
        }
        Spacer(Modifier.height(20.dp))
        OutlinedButton(
            onClick = onFinish,
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(stringResource(R.string.ob_finish), fontSize = 15.sp)
        }
    }
}

private fun checkEnabled(context: android.content.Context): Boolean {
    return try {
        val enabled = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_INPUT_METHODS,
        )
        enabled?.contains(context.packageName) == true
    } catch (e: Exception) {
        false
    }
}

private fun checkSelected(context: android.content.Context): Boolean {
    return try {
        val def = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD,
        )
        def?.startsWith(context.packageName) == true
    } catch (e: Exception) {
        false
    }
}
