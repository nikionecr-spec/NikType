package com.niktype.engine.geometry

/**
 * ساختارهای پایه هندسه کیبورد: کلیدها، مستطیل‌ها و یک چیدمان کامل.
 * همه مختصات برحسب پیکسل فضای نمای کیبورد (view space) هستند.
 */

enum class KeyType {
    CHAR,       // کلید نویسه (حرف، رقم، علائم)
    SHIFT,
    BACKSPACE,
    ENTER,
    SPACE,
    LANGUAGE,   // تغییر زبان
    SYMBOLS,    // رفتن به لایه علائم/اعداد یا بازگشت به حروف
}

enum class LanguageMode { FA, EN }

enum class LayoutMode { LETTERS, SYMBOLS }

enum class FaLayoutStyle { STANDARD, ALPHABETIC }

data class KeySpec(
    val id: String,
    val label: String,
    val output: String? = null,
    val type: KeyType = KeyType.CHAR,
    val altLabels: List<String> = emptyList(),
    val weight: Float = 1f,
)

data class KeyRect(
    val key: KeySpec,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
) {
    val centerX: Float get() = x + width / 2f
    val centerY: Float get() = y + height / 2f
    val right: Float get() = x + width
    val bottom: Float get() = y + height

    fun contains(px: Float, py: Float, margin: Float = 0f): Boolean =
        px >= x - margin && px <= right + margin && py >= y - margin && py <= bottom + margin
}

data class KeyboardLayout(
    val id: String,
    val language: LanguageMode,
    val mode: LayoutMode,
    val width: Float,
    val height: Float,
    val rows: List<List<KeyRect>>,
    val keys: List<KeyRect>,
) {
    val charToKey: Map<Char, KeyRect> = buildMap {
        for (kr in keys) {
            val out = kr.key.output
            if (kr.key.type == KeyType.CHAR && out != null && out.length == 1) {
                put(out[0], kr)
            }
        }
    }

    fun keyById(id: String): KeyRect? = keys.firstOrNull { it.key.id == id }
}

data class LayoutParams(
    val keyHeightPx: Float,
    val gapPx: Float,
    val radiusPx: Float,
    val sidePaddingPx: Float,
)
