package com.niktype.keyboard.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niktype.keyboard.R
import com.niktype.keyboard.themes.KeyboardTheme

/**
 * ریشه رابط کیبورد: نوار پیشنهاد + بوم کلیدها.
 */
@Composable
fun NikKeyboardRoot(controller: KeyboardController) {
    val settings = controller.settings
    val theme = effectiveTheme(settings.themeId, settings.keyColorHex, settings.bgColorHex)
    Column(
        Modifier
            .fillMaxWidth()
            .background(theme.background.copy(alpha = (theme.backgroundAlpha * (1f - settings.transparency)).coerceIn(0.15f, 1f)))
    ) {
        SuggestionBar(controller, theme)
        KeyboardCanvas(controller)
    }
}

@Composable
fun SuggestionBar(controller: KeyboardController, theme: KeyboardTheme) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(46.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // کلید تغییر زبان
        Box(
            Modifier
                .size(width = 44.dp, height = 34.dp)
                .background(theme.chipColor, RoundedCornerShape(9.dp))
                .clickable { controller.toggleLanguage() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (controller.language == com.niktype.engine.geometry.LanguageMode.FA)
                    stringResource(R.string.lang_en_short)
                else stringResource(R.string.lang_fa_short),
                color = theme.chipText,
                fontSize = 14.sp,
            )
        }

        Spacer(Modifier.width(6.dp))

        // چیپ نیم‌فاصله (فارسی)
        if (controller.showZwnjChip) {
            Chip(controller, theme, stringResource(R.string.zwnj_chip), Modifier) {
                controller.onZwnjChipTap()
            }
            Spacer(Modifier.width(6.dp))
        }

        // جای خالی/واژه سوایپ + پیشنهادها
        val suggestions = controller.suggestions
        val gestureWord = controller.gestureWord
        if (suggestions.isEmpty() && gestureWord == null) {
            Spacer(Modifier.weight(1f))
        } else {
            Row(Modifier.weight(1f)) {
                if (gestureWord != null) {
                    Chip(controller, theme, gestureWord, Modifier.weight(1f)) {
                        controller.onSuggestionTap(gestureWord)
                    }
                } else {
                    suggestions.take(3).forEachIndexed { i, word ->
                        if (i > 0) Spacer(Modifier.width(4.dp))
                        Chip(controller, theme, word, Modifier.weight(1f)) {
                            controller.onSuggestionTap(word)
                        }
                    }
                }
            }
        }

        // ایموجی‌های پیشنهادی
        for (emoji in controller.emojiSuggestions.take(3)) {
            Box(
                Modifier
                    .size(width = 34.dp, height = 34.dp)
                    .clickable { controller.onEmojiTap(emoji) },
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, fontSize = 19.sp)
            }
        }

        Spacer(Modifier.width(4.dp))

        // کلید تایپ صوتی
        Box(
            Modifier
                .size(34.dp)
                .clickable { controller.startVoiceInput() },
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.foundation.Canvas(Modifier.size(22.dp)) {
                drawMicIcon(center = Offset(size.width / 2f, size.height / 2f), size.width, theme.chipText)
            }
        }
    }
}

@Composable
private fun Chip(
    controller: KeyboardController,
    theme: KeyboardTheme,
    label: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .height(34.dp)
            .background(theme.chipColor, RoundedCornerShape(9.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = theme.chipText, fontSize = 15.sp, maxLines = 1)
    }
}
