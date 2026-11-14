package com.niktype.keyboard.themes

import androidx.compose.ui.graphics.Color

/**
 * کاتالوگ تم‌های NikType — شش تم اولیه با پالت بنفش/آبی/مشکی و حالت روشن.
 */
data class KeyboardTheme(
    val id: String,
    val name: String,
    val isLight: Boolean,
    val isGlass: Boolean = false,
    val background: Color,
    val backgroundAlpha: Float = 1f,
    val keyColor: Color,
    val keyTextColor: Color,
    val keySpecial: Color,
    val accentTop: Color,
    val accentBottom: Color,
    val pressedKey: Color,
    val chipColor: Color,
    val chipText: Color,
    val accent: Color,
)

val THEME_DARK_NEON = KeyboardTheme(
    id = "dark_neon", name = "Dark Neon",
    isLight = false,
    background = Color(0xFF101018),
    keyColor = Color(0xFF1C1C2E),
    keyTextColor = Color(0xFFE8E8F5),
    keySpecial = Color(0xFF262640),
    accentTop = Color(0xFF7C4DFF),
    accentBottom = Color(0xFF448AFF),
    pressedKey = Color(0xFF2A2A44),
    chipColor = Color(0xFF1C1C2E),
    chipText = Color(0xFFC9C6FF),
    accent = Color(0xFF7C4DFF),
)

val THEME_PURPLE_GALAXY = KeyboardTheme(
    id = "purple_galaxy", name = "Purple Galaxy",
    isLight = false,
    background = Color(0xFF170F26),
    keyColor = Color(0xFF2A1B45),
    keyTextColor = Color(0xFFF2E9FF),
    keySpecial = Color(0xFF3A2560),
    accentTop = Color(0xFFB388FF),
    accentBottom = Color(0xFF7C4DFF),
    pressedKey = Color(0xFF3A2560),
    chipColor = Color(0xFF2A1B45),
    chipText = Color(0xFFE3D2FF),
    accent = Color(0xFFB388FF),
)

val THEME_OCEAN_BLUE = KeyboardTheme(
    id = "ocean_blue", name = "Ocean Blue",
    isLight = false,
    background = Color(0xFF0B1626),
    keyColor = Color(0xFF16283F),
    keyTextColor = Color(0xFFE3F2FD),
    keySpecial = Color(0xFF1E3654),
    accentTop = Color(0xFF2196F3),
    accentBottom = Color(0xFF0D47A1),
    pressedKey = Color(0xFF204066),
    chipColor = Color(0xFF16283F),
    chipText = Color(0xFFB3D9FF),
    accent = Color(0xFF2196F3),
)

val THEME_MINIMAL_BLACK = KeyboardTheme(
    id = "minimal_black", name = "Minimal Black",
    isLight = false,
    background = Color(0xFF0A0A0A),
    keyColor = Color(0xFF161616),
    keyTextColor = Color(0xFFEEEEEE),
    keySpecial = Color(0xFF222222),
    accentTop = Color(0xFF3A3A3A),
    accentBottom = Color(0xFF2A2A2A),
    pressedKey = Color(0xFF242424),
    chipColor = Color(0xFF161616),
    chipText = Color(0xFFBBBBBB),
    accent = Color(0xFF888888),
)

val THEME_PURE_WHITE = KeyboardTheme(
    id = "pure_white", name = "Pure White",
    isLight = true,
    background = Color(0xFFFAFAFA),
    keyColor = Color(0xFFFFFFFF),
    keyTextColor = Color(0xFF1A1A1A),
    keySpecial = Color(0xFFF0F0F3),
    accentTop = Color(0xFF7C4DFF),
    accentBottom = Color(0xFF448AFF),
    pressedKey = Color(0xFFE4E4EA),
    chipColor = Color(0xFFF2F2F6),
    chipText = Color(0xFF5B4BC4),
    accent = Color(0xFF7C4DFF),
)

val THEME_GLASS_UI = KeyboardTheme(
    id = "glass_ui", name = "Glass UI",
    isLight = false,
    isGlass = true,
    background = Color(0xFF1A1A22),
    backgroundAlpha = 0.55f,
    keyColor = Color(0x24FFFFFF),
    keyTextColor = Color(0xFFFFFFFF),
    keySpecial = Color(0x38FFFFFF),
    accentTop = Color(0xCC7C4DFF),
    accentBottom = Color(0xCC448AFF),
    pressedKey = Color(0x48FFFFFF),
    chipColor = Color(0x24FFFFFF),
    chipText = Color(0xFFE0DBFF),
    accent = Color(0xFF9C7BFF),
)

val ALL_THEMES = listOf(
    THEME_DARK_NEON,
    THEME_PURPLE_GALAXY,
    THEME_OCEAN_BLUE,
    THEME_MINIMAL_BLACK,
    THEME_PURE_WHITE,
    THEME_GLASS_UI,
)

fun themeById(id: String): KeyboardTheme = ALL_THEMES.firstOrNull { it.id == id } ?: THEME_DARK_NEON

/** پالت رنگ پیشنهادی برای تغییر رنگ کلیدها / پس‌زمینه در تنظیمات. */
val COLOR_SWATCHES = listOf(
    null, // پیش‌فرض تم
    "#1C1C2E", "#2A1B45", "#16283F", "#161616", "#FFFFFF",
    "#7C4DFF", "#448AFF", "#2ECC8F", "#E74C6F", "#F39C12",
)

fun parseHexOrNull(hex: String?): Color? {
    if (hex == null) return null
    return runCatching {
        Color(android.graphics.Color.parseColor(hex))
    }.getOrNull()
}
