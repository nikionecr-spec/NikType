package com.niktype.keyboard.keyboard

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.min

/**
 * آیکون‌های برداری سبک برای کلیدهای خاص (بدون وابستگی به فونت):
 * Shift، Backspace، Enter و میکروفون — همه با مسیرهای ساده و دقیق.
 */

fun DrawScope.drawShiftIcon(center: Offset, size: Float, color: Color, locked: Boolean) {
    val s = size / 2f
    val path = Path().apply {
        moveTo(center.x, center.y - s)
        lineTo(center.x + s, center.y)
        lineTo(center.x + s * 0.55f, center.y)
        lineTo(center.x + s * 0.55f, center.y + s * 0.85f)
        lineTo(center.x - s * 0.55f, center.y + s * 0.85f)
        lineTo(center.x - s * 0.55f, center.y)
        lineTo(center.x - s, center.y)
        close()
    }
    val stroke = Stroke(width = size * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    if (locked) {
        drawPath(path, color)
    } else {
        drawPath(path, color, style = stroke)
    }
    if (locked) {
        drawLine(
            color,
            Offset(center.x - s * 0.35f, center.y + s * 1.25f),
            Offset(center.x + s * 0.35f, center.y + s * 1.25f),
            strokeWidth = size * 0.09f,
            cap = StrokeCap.Round,
        )
    }
}

fun DrawScope.drawBackspaceIcon(center: Offset, size: Float, color: Color) {
    val w = size / 2f
    val h = size / 2.6f
    val path = Path().apply {
        // بدنه پیکان‌شکل backspace
        moveTo(center.x - w * 0.9f, center.y)
        lineTo(center.x - w * 0.3f, center.y - h)
        lineTo(center.x + w * 0.95f, center.y - h)
        lineTo(center.x + w * 0.95f, center.y + h)
        lineTo(center.x - w * 0.3f, center.y + h)
        close()
    }
    val stroke = Stroke(width = size * 0.085f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    drawPath(path, color, style = stroke)
    // ضربدر داخل
    val cx = center.x + w * 0.35f
    val d = h * 0.45f
    drawLine(color, Offset(cx - d, center.y - d), Offset(cx + d, center.y + d), size * 0.085f, StrokeCap.Round)
    drawLine(color, Offset(cx + d, center.y - d), Offset(cx - d, center.y + d), size * 0.085f, StrokeCap.Round)
}

fun DrawScope.drawEnterIcon(center: Offset, size: Float, color: Color) {
    val w = size / 2f
    val stroke = Stroke(width = size * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    // خط افقی از راست به میانه
    drawLine(
        color,
        Offset(center.x + w * 0.9f, center.y - w * 0.35f),
        Offset(center.x - w * 0.5f, center.y - w * 0.35f),
        stroke.width, StrokeCap.Round,
    )
    // پرتاب به پایین-چپ (پیکان)
    val path = Path().apply {
        moveTo(center.x - w * 0.1f, center.y - w * 0.75f)
        lineTo(center.x - w * 0.55f, center.y - w * 0.35f)
        lineTo(center.x - w * 0.1f, center.y + w * 0.05f)
    }
    drawPath(path, color, style = stroke)
}

fun DrawScope.drawMicIcon(center: Offset, size: Float, color: Color) {
    val r = size * 0.22f
    // بدنه میکروفون (کپسول)
    drawRoundRect(
        color,
        topLeft = Offset(center.x - r, center.y - r * 1.6f),
        size = androidx.compose.ui.geometry.Size(r * 2f, r * 3.2f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
    )
    val stroke = Stroke(width = size * 0.075f, cap = StrokeCap.Round)
    // قوس نگهدارنده
    drawArc(
        color,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x - r * 1.7f, center.y - r * 1.0f),
        size = androidx.compose.ui.geometry.Size(r * 3.4f, r * 2.4f),
        style = stroke,
    )
    // پایه
    drawLine(
        color,
        Offset(center.x, center.y + r * 1.4f),
        Offset(center.x, center.y + r * 2.0f),
        stroke.width, StrokeCap.Round,
    )
    drawLine(
        color,
        Offset(center.x - r * 0.8f, center.y + r * 2.0f),
        Offset(center.x + r * 0.8f, center.y + r * 2.0f),
        stroke.width, StrokeCap.Round,
    )
}

fun DrawScope.drawGearIcon(center: Offset, size: Float, color: Color) {
    // آیکون تنظیمات ساده: سه خط با دستگیره (اسلایدرها)
    val stroke = Stroke(width = size * 0.085f, cap = StrokeCap.Round)
    val w = size * 0.36f
    val knobs = listOf(-1f, 0f, 1f)
    knobs.forEachIndexed { i, ky ->
        val y = center.y + ky * size * 0.26f
        drawLine(color, Offset(center.x - w, y), Offset(center.x + w, y), stroke.width)
        val kx = when (i) {
            0 -> center.x + w * 0.35f
            1 -> center.x - w * 0.4f
            else -> center.x + w * 0.1f
        }
        drawCircle(color, radius = size * 0.075f, center = Offset(kx, y))
    }
}

fun keyIconScale(keyWidth: Float, keyHeight: Float): Float = min(keyWidth, keyHeight) * 0.46f
