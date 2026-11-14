package com.niktype.keyboard.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.niktype.keyboard.data.KeyboardSettings
import com.niktype.keyboard.data.SettingsRepository
import com.niktype.keyboard.themes.ALL_THEMES
import com.niktype.keyboard.themes.COLOR_SWATCHES
import com.niktype.keyboard.themes.parseHexOrNull
import kotlinx.coroutines.launch

private val PURPLE = Color(0xFF7C4DFF)
private val SURFACE = Color(0xFF1C1C2E)
private val SURFACE2 = Color(0xFF232336)

@Composable
private fun ScreenScaffold(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF12121C))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .width(44.dp)
                    .height(40.dp)
                    .background(SURFACE, RoundedCornerShape(10.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Text("←", color = Color.White, fontSize = 18.sp)
            }
            Spacer(Modifier.width(14.dp))
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            content()
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(SURFACE, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Color(0xFFE4E4EF), fontSize = 15.sp)
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = PURPLE,
                uncheckedTrackColor = SURFACE2,
            ),
        )
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    valueText: String,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color(0xFFE4E4EF), fontSize = 15.sp)
            Text(valueText, color = Color(0xFF9C7BFF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = PURPLE,
                activeTrackColor = PURPLE,
                inactiveTrackColor = SURFACE2,
            ),
        )
    }
}

// -------------------------------------------------- صفحه اصلی تنظیمات --

@Composable
fun SettingsHome(container: AppContainer, onOpen: (String) -> Unit) {
    val context = LocalContext.current
    val enabled = remember { isImeEnabled(context) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF12121C))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(52.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF7C4DFF), Color(0xFF448AFF))),
                        RoundedCornerShape(14.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("N", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(stringResource(R.string.settings_title), color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("NikType", color = Color(0xFF9C7BFF), fontSize = 13.sp)
            }
        }

        if (!enabled) {
            Spacer(Modifier.height(16.dp))
            Card {
                Text(
                    stringResource(R.string.enable_first),
                    color = Color(0xFFFFC46B), fontSize = 14.sp,
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        MenuCard(stringResource(R.string.s_appearance)) { onOpen("appearance") }
        MenuCard(stringResource(R.string.s_typing)) { onOpen("typing") }
        MenuCard(stringResource(R.string.s_shortcuts)) { onOpen("shortcuts") }
        MenuCard(stringResource(R.string.s_about)) { onOpen("about") }
    }
}

@Composable
private fun MenuCard(title: String, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp)
            .background(SURFACE, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 20.dp, horizontal = 16.dp)
    ) {
        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

// -------------------------------------------------- ظاهر و تم --

@Composable
fun AppearanceScreen(container: AppContainer, onBack: () -> Unit) {
    val settings by container.settingsRepo.flow.collectAsState(initial = KeyboardSettings())
    val scope = rememberCoroutineScope()
    fun update(t: (KeyboardSettings) -> KeyboardSettings) = scope.launch { container.settingsRepo.update(t) }

    ScreenScaffold(stringResource(R.string.s_appearance), onBack) {
        Card {
            Text(stringResource(R.string.s_theme), color = Color(0xFFB9B9CC), fontSize = 14.sp)
            Spacer(Modifier.height(10.dp))
            ALL_THEMES.chunked(2).forEach { rowThemes ->
                Row(Modifier.fillMaxWidth()) {
                    rowThemes.forEach { theme ->
                        val active = settings.themeId == theme.id
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(4.dp)
                                .background(if (active) Color(0xFF2A2140) else SURFACE2, RoundedCornerShape(12.dp))
                                .border(
                                    if (active) 2.dp else 0.dp,
                                    if (active) PURPLE else Color.Transparent,
                                    RoundedCornerShape(12.dp),
                                )
                                .clickable { update { it.copy(themeId = theme.id) } }
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .background(
                                        Brush.linearGradient(listOf(theme.keyColor, theme.background)),
                                        RoundedCornerShape(8.dp),
                                    )
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(theme.name, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Card {
            SliderRow(
                stringResource(R.string.s_key_height), settings.keyHeightDp,
                "${settings.keyHeightDp.toInt()}", 40f..80f,
            ) { v -> update { it.copy(keyHeightDp = v, fingerProfile = com.niktype.engine.touch.FingerProfileKind.NORMAL) } }
            SliderRow(
                stringResource(R.string.s_key_gap), settings.gapDp,
                "${settings.gapDp.toInt()}", 1f..9f,
            ) { v -> update { it.copy(gapDp = v) } }
            SliderRow(
                stringResource(R.string.s_corner), settings.cornerRadiusDp,
                "${settings.cornerRadiusDp.toInt()}", 4f..18f,
            ) { v -> update { it.copy(cornerRadiusDp = v) } }
            SliderRow(
                stringResource(R.string.s_font_size), settings.fontScale,
                "%.2f".format(settings.fontScale), 0.85f..1.3f,
            ) { v -> update { it.copy(fontScale = v) } }
            SliderRow(
                stringResource(R.string.s_keyboard_height), settings.heightScale,
                "%.0f%%".format(settings.heightScale * 100), 0.85f..1.25f,
            ) { v -> update { it.copy(heightScale = v) } }
            SliderRow(
                stringResource(R.string.s_transparency), settings.transparency,
                "%.0f%%".format(settings.transparency * 100), 0f..0.6f,
            ) { v -> update { it.copy(transparency = v) } }
        }

        Card {
            Text(stringResource(R.string.s_key_color), color = Color(0xFFB9B9CC), fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            SwatchRow(settings.keyColorHex) { hex -> update { it.copy(keyColorHex = hex) } }
            Spacer(Modifier.height(14.dp))
            Text(stringResource(R.string.s_bg_color), color = Color(0xFFB9B9CC), fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            SwatchRow(settings.bgColorHex) { hex -> update { it.copy(bgColorHex = hex) } }
        }

        Card {
            Text(stringResource(R.string.s_animation), color = Color(0xFFB9B9CC), fontSize = 14.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "none" to stringResource(R.string.s_anim_none),
                    "standard" to stringResource(R.string.s_anim_standard),
                    "playful" to stringResource(R.string.s_anim_playful),
                ).forEach { (id, label) ->
                    val active = settings.animationStyle == id
                    Box(
                        Modifier
                            .weight(1f)
                            .background(if (active) PURPLE else SURFACE2, RoundedCornerShape(10.dp))
                            .clickable { update { it.copy(animationStyle = id) } }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(stringResource(R.string.s_fa_layout), color = Color(0xFFB9B9CC), fontSize = 14.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    com.niktype.engine.geometry.FaLayoutStyle.STANDARD to stringResource(R.string.s_fa_layout_standard),
                    com.niktype.engine.geometry.FaLayoutStyle.ALPHABETIC to stringResource(R.string.s_fa_layout_alpha),
                ).forEach { (style, label) ->
                    val active = settings.faLayoutStyle == style
                    Box(
                        Modifier
                            .weight(1f)
                            .background(if (active) PURPLE else SURFACE2, RoundedCornerShape(10.dp))
                            .clickable { update { it.copy(faLayoutStyle = style) } }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SwatchRow(selected: String?, onPick: (String?) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        COLOR_SWATCHES.forEach { hex ->
            val color = if (hex == null) Color(0xFF33334A) else (parseHexOrNull(hex) ?: Color.Gray)
            val active = selected == hex
            Box(
                Modifier
                    .size(30.dp)
                    .background(color, RoundedCornerShape(8.dp))
                    .border(
                        if (active) 2.dp else 1.dp,
                        if (active) Color.White else Color(0xFF44445E),
                        RoundedCornerShape(8.dp),
                    )
                    .clickable { onPick(hex) }
            )
        }
    }
}

// -------------------------------------------------- تایپ و هوشمندی --

@Composable
fun TypingScreen(container: AppContainer, onBack: () -> Unit) {
    val settings by container.settingsRepo.flow.collectAsState(initial = KeyboardSettings())
    val scope = rememberCoroutineScope()
    fun update(t: (KeyboardSettings) -> KeyboardSettings) = scope.launch { container.settingsRepo.update(t) }

    ScreenScaffold(stringResource(R.string.s_typing), onBack) {
        Card {
            Text(stringResource(R.string.s_profiles), color = Color(0xFFB9B9CC), fontSize = 14.sp)
            Spacer(Modifier.height(10.dp))
            listOf(
                com.niktype.engine.touch.FingerProfileKind.SMALL to R.string.profile_small,
                com.niktype.engine.touch.FingerProfileKind.NORMAL to R.string.profile_normal,
                com.niktype.engine.touch.FingerProfileKind.LARGE to R.string.profile_large,
            ).forEach { (profile, labelRes) ->
                val active = settings.fingerProfile == profile
                val preset = SettingsRepository.presetFor(profile)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .background(if (active) Color(0xFF2A2140) else SURFACE2, RoundedCornerShape(12.dp))
                        .border(
                            if (active) 2.dp else 0.dp,
                            if (active) PURPLE else Color.Transparent,
                            RoundedCornerShape(12.dp),
                        )
                        .clickable {
                            update {
                                it.copy(
                                    fingerProfile = profile,
                                    keyHeightDp = preset.first,
                                    gapDp = preset.second,
                                    keyWidthScale = preset.third,
                                )
                            }
                        }
                        .padding(vertical = 15.dp, horizontal = 14.dp)
                ) {
                    Text(stringResource(labelRes), color = Color.White, fontSize = 15.sp)
                }
            }
        }

        Card {
            SwitchRow(stringResource(R.string.s_autocorrect), settings.autocorrect) { v -> update { it.copy(autocorrect = v) } }
            SwitchRow(stringResource(R.string.s_next_word), settings.nextWord) { v -> update { it.copy(nextWord = v) } }
            SwitchRow(stringResource(R.string.s_swipe), settings.swipeEnabled) { v -> update { it.copy(swipeEnabled = v) } }
            SwitchRow(stringResource(R.string.s_persian_digits), settings.persianDigits) { v -> update { it.copy(persianDigits = v) } }
            SwitchRow(stringResource(R.string.s_auto_caps), settings.autoCaps) { v -> update { it.copy(autoCaps = v) } }
            SwitchRow(stringResource(R.string.s_colloquial), settings.learnColloquial) { v -> update { it.copy(learnColloquial = v) } }
        }

        Card {
            SwitchRow(stringResource(R.string.s_sound), settings.soundEnabled) { v -> update { it.copy(soundEnabled = v) } }
            SwitchRow(stringResource(R.string.s_vibration), settings.hapticStrength > 0) { v ->
                update { it.copy(hapticStrength = if (v) (if (it.hapticStrength == 0) 35 else it.hapticStrength) else 0) }
            }
            if (settings.hapticStrength > 0) {
                SliderRow(
                    stringResource(R.string.s_haptic_strength), settings.hapticStrength.toFloat(),
                    "${settings.hapticStrength}", 5f..100f,
                ) { v -> update { it.copy(hapticStrength = v.toInt()) } }
            }
        }
    }
}

// -------------------------------------------------- میانبرهای متنی --

@Composable
fun ShortcutsScreen(container: AppContainer, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var shortcuts by remember { mutableStateOf(container.shortcutsStore.load()) }
    var showDialog by remember { mutableStateOf(false) }
    var keyText by remember { mutableStateOf("") }
    var valueText by remember { mutableStateOf("") }

    ScreenScaffold(stringResource(R.string.s_shortcuts), onBack) {
        Text(
            stringResource(R.string.s_shortcut_hint),
            color = Color(0xFF9C7BFF), fontSize = 13.sp,
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = PURPLE),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(stringResource(R.string.s_add_shortcut), fontSize = 14.sp)
        }
        Spacer(Modifier.height(14.dp))

        if (shortcuts.isEmpty()) {
            Text(stringResource(R.string.s_shortcuts_empty), color = Color(0xFF8A8AA0), fontSize = 14.sp)
        } else {
            shortcuts.forEach { sc ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .background(SURFACE, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(sc.key, color = Color(0xFF9C7BFF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(sc.value, color = Color(0xFFE4E4EF), fontSize = 14.sp)
                    }
                    Text(
                        stringResource(R.string.s_delete),
                        color = Color(0xFFE74C6F),
                        fontSize = 13.sp,
                        modifier = Modifier.clickable {
                            scope.launch {
                                val newList = shortcuts.filter { it != sc }
                                container.shortcutsStore.save(newList)
                                shortcuts = newList
                            }
                        },
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = SURFACE,
            title = { Text(stringResource(R.string.s_add_shortcut), color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = keyText, onValueChange = { keyText = it },
                        label = { Text(stringResource(R.string.s_shortcut_key)) },
                        singleLine = true,
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = valueText, onValueChange = { valueText = it },
                        label = { Text(stringResource(R.string.s_shortcut_value)) },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (keyText.isNotBlank() && valueText.isNotBlank()) {
                        val newList = shortcuts +
                            com.niktype.keyboard.data.ShortcutsStore.Shortcut(keyText.trim(), valueText.trim())
                        container.shortcutsStore.save(newList)
                        shortcuts = newList
                        keyText = ""
                        valueText = ""
                        showDialog = false
                    }
                }) { Text(stringResource(R.string.save), color = PURPLE) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel), color = Color(0xFF9C9CB4))
                }
            },
        )
    }
}

// -------------------------------------------------- درباره و حریم خصوصی --

@Composable
fun AboutScreen(container: AppContainer, onBack: () -> Unit) {
    ScreenScaffold(stringResource(R.string.s_about), onBack) {
        Card {
            Text(stringResource(R.string.s_about_privacy_title), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.s_about_privacy_body), color = Color(0xFFC9C9DA), fontSize = 14.sp, lineHeight = 22.sp)
        }
        Card {
            Text("${stringResource(R.string.s_version)} 0.1.0", color = Color(0xFFE4E4EF), fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            Text("${stringResource(R.string.s_repo)}: github.com (NikType)", color = Color(0xFF9C7BFF), fontSize = 14.sp)
        }
    }
}

private fun isImeEnabled(context: android.content.Context): Boolean = try {
    val enabled = android.provider.Settings.Secure.getString(
        context.contentResolver, android.provider.Settings.Secure.ENABLED_INPUT_METHODS,
    )
    enabled?.contains(context.packageName) == true
} catch (e: Exception) {
    false
}
