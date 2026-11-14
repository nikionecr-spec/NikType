package com.niktype.keyboard.keyboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import com.niktype.engine.geometry.KeyRect
import com.niktype.engine.geometry.KeyboardLayout
import com.niktype.engine.geometry.KeyType
import com.niktype.keyboard.themes.KeyboardTheme
import com.niktype.keyboard.themes.parseHexOrNull
import com.niktype.keyboard.themes.themeById

/**
 * تم مؤثر با رنگ‌های سفارشی کاربر.
 */
fun effectiveTheme(themeId: String, keyColorHex: String?, bgColorHex: String?): KeyboardTheme {
    val base = themeById(themeId)
    val key = parseHexOrNull(keyColorHex)
    val bg = parseHexOrNull(bgColorHex)
    return if (key == null && bg == null) base
    else base.copy(
        keyColor = key ?: base.keyColor,
        background = bg ?: base.background,
    )
}

/**
 * رندر کل کیبورد با یک Canvas و مدیریت مستقیم pointerها —
 * بدون گره‌های ترکیب به‌ازای هر کلید برای حداقل تأخیر و حداکثر روانی.
 */
@Composable
fun KeyboardCanvas(
    controller: KeyboardController,
    modifier: Modifier = Modifier,
) {
    val settings = controller.settings
    val layout = controller.layout
    val theme = effectiveTheme(settings.themeId, settings.keyColorHex, settings.bgColorHex)
    val density = LocalDensity.current
    val totalHeightPx = controller.totalHeightPx(density.density)
    val params = controller.geometryParams(density.density)
    val textMeasurer = rememberTextMeasurer()
    val textCache = remember(layout?.id, settings.fontScale) {
        HashMap<String, TextLayoutResult>()
    }

    Box(
        modifier
            .fillMaxWidth()
            .height(with(density) { totalHeightPx.toDp() })
            .onSizeChanged { controller.buildLayout(it.width.toFloat(), totalHeightPx) }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        for (change in event.changes) {
                            val pos = change.position
                            when {
                                change.changedToDown() ->
                                    controller.onDown(change.id.value, pos.x, pos.y, change.uptimeMillis)
                                change.changedToUp() ->
                                    controller.onUp(change.id.value, pos.x, pos.y, change.uptimeMillis)
                                change.pressed && change.positionChanged() ->
                                    controller.onMove(change.id.value, pos.x, pos.y, change.uptimeMillis)
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val l = layout ?: return@Canvas
            drawNikKeyboard(controller, l, theme, settings, params, textMeasurer, textCache)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNikKeyboard(
    controller: KeyboardController,
    layout: KeyboardLayout,
    theme: KeyboardTheme,
    settings: com.niktype.keyboard.data.KeyboardSettings,
    params: com.niktype.engine.geometry.LayoutParams,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    textCache: HashMap<String, TextLayoutResult>,
) {
    val bgAlpha = theme.backgroundAlpha * (1f - settings.transparency)
    drawRoundRect(
        color = theme.background.copy(alpha = bgAlpha.coerceIn(0.15f, 1f)),
        cornerRadius = CornerRadius(18f, 18f),
    )

    val pressOffset = when (settings.animationStyle) {
        "playful" -> 3f
        "standard" -> 1.5f
        else -> 0f
    }

    for (key in layout.keys) {
        drawKey(controller, key, theme, settings, params, pressOffset, textMeasurer, textCache)
    }

    drawPopup(controller.popup, theme, textMeasurer, textCache, params.radiusPx)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawKey(
    controller: KeyboardController,
    key: KeyRect,
    theme: KeyboardTheme,
    settings: com.niktype.keyboard.data.KeyboardSettings,
    params: com.niktype.engine.geometry.LayoutParams,
    pressOffset: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    textCache: HashMap<String, TextLayoutResult>,
) {
    val pressed = controller.pressedKey == key.key.id
    val y = key.y + if (pressed) pressOffset else 0f

    val fill: Any = when {
        key.key.type == KeyType.ENTER -> Brush.verticalGradient(
            listOf(theme.accentTop, theme.accentBottom),
            startY = y, endY = y + key.height,
        )
        pressed -> theme.pressedKey
        key.key.type == KeyType.CHAR -> theme.keyColor
        key.key.type == KeyType.SPACE -> theme.keyColor
        else -> theme.keySpecial
    }

    // سایه ظریف
    drawRoundRect(
        color = Color.Black.copy(alpha = if (theme.isLight) 0.10f else 0.22f),
        topLeft = Offset(key.x + 1f, y + 2.5f),
        size = Size(key.width - 2f, key.height),
        cornerRadius = CornerRadius(params.radiusPx, params.radiusPx),
    )

    when (fill) {
        is Brush -> drawRoundRect(fill, Offset(key.x, y), Size(key.width, key.height), CornerRadius(params.radiusPx, params.radiusPx))
        is Color -> drawRoundRect(fill, Offset(key.x, y), Size(key.width, key.height), CornerRadius(params.radiusPx, params.radiusPx))
    }

    if (theme.isGlass) {
        drawRoundRect(
            color = Color.White.copy(alpha = 0.10f),
            topLeft = Offset(key.x, y),
            size = Size(key.width, key.height),
            cornerRadius = CornerRadius(params.radiusPx, params.radiusPx),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f),
        )
    }

    val center = Offset(key.centerX, y + key.height / 2f)
    val iconSize = minOf(key.width, key.height) * 0.46f
    val labelColor = if (key.key.type == KeyType.ENTER) Color.White else theme.keyTextColor

    when (key.key.type) {
        KeyType.SHIFT -> drawShiftIcon(center, iconSize, labelColor, controller.shiftState == ShiftState.LOCK)
        KeyType.BACKSPACE -> drawBackspaceIcon(center, iconSize, labelColor)
        KeyType.ENTER -> drawEnterIcon(center, iconSize, labelColor)
        KeyType.SPACE -> { /* بدون برچسب — تمیز و مینیمال */ }
        else -> drawKeyLabel(key.key.label, center, theme, settings, pressed, textMeasurer, textCache)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawKeyLabel(
    label: String,
    center: Offset,
    theme: KeyboardTheme,
    settings: com.niktype.keyboard.data.KeyboardSettings,
    pressed: Boolean,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    textCache: HashMap<String, TextLayoutResult>,
) {
    if (label.isEmpty()) return
    val fontSize = (17f * settings.fontScale).sp
    val cacheKey = "$label|${settings.fontScale}"
    val result = textCache.getOrPut(cacheKey) {
        textMeasurer.measure(
            androidx.compose.ui.text.AnnotatedString(label),
            TextStyle(fontSize = fontSize, color = theme.keyTextColor),
            maxLines = 1,
        )
    }
    val alpha = if (pressed) 1f else 0.96f
    drawText(
        result,
        color = theme.keyTextColor.copy(alpha = alpha),
        topLeft = Offset(center.x - result.size.width / 2f, center.y - result.size.height / 2f),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPopup(
    popup: PopupState?,
    theme: KeyboardTheme,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    textCache: HashMap<String, TextLayoutResult>,
    radius: Float,
) {
    if (popup == null) return
    drawRoundRect(
        color = theme.keySpecial,
        topLeft = Offset(popup.panelX, popup.panelY),
        size = Size(popup.panelW, popup.panelH),
        cornerRadius = CornerRadius(radius + 4f, radius + 4f),
    )
    val cellW = popup.panelW / popup.options.size
    for ((i, opt) in popup.options.withIndex()) {
        val cx = popup.panelX + cellW * i + cellW / 2f
        val cy = popup.panelY + popup.panelH / 2f
        if (i == popup.selectedIndex) {
            drawRoundRect(
                color = theme.accent.copy(alpha = 0.85f),
                topLeft = Offset(popup.panelX + cellW * i + 2f, popup.panelY + 3f),
                size = Size(cellW - 4f, popup.panelH - 6f),
                cornerRadius = CornerRadius(radius, radius),
            )
        }
        val cacheKey = "pop_$opt"
        val result = textCache.getOrPut(cacheKey) {
            textMeasurer.measure(
                androidx.compose.ui.text.AnnotatedString(opt),
                TextStyle(fontSize = 18.sp, color = Color.White),
                maxLines = 1,
            )
        }
        drawText(
            result,
            color = Color.White,
            topLeft = Offset(cx - result.size.width / 2f, cy - result.size.height / 2f),
        )
    }
}
