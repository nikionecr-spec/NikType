package com.niktype.keyboard.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.niktype.engine.geometry.FaLayoutStyle
import com.niktype.engine.touch.FingerProfileKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "nik_settings")

/**
 * تنظیمات کامل کیبورد. مقادیر پیش‌فرض برای انگشت معمولی و تم Dark Neon.
 */
data class KeyboardSettings(
    val fingerProfile: FingerProfileKind = FingerProfileKind.NORMAL,
    val keyHeightDp: Float = 54f,
    val keyWidthScale: Float = 1.0f,   // بیشتر = کلیدهای پهن‌تر (فاصله کمتر)
    val gapDp: Float = 4f,
    val cornerRadiusDp: Float = 10f,
    val fontScale: Float = 1.0f,
    val heightScale: Float = 1.0f,     // ضریب ارتفاع کل کیبورد
    val transparency: Float = 0.0f,    // 0 تا 0.6
    val themeId: String = "dark_neon",
    val keyColorHex: String? = null,
    val bgColorHex: String? = null,
    val hapticStrength: Int = 35,      // 0..100
    val soundEnabled: Boolean = true,
    val autocorrect: Boolean = true,
    val nextWord: Boolean = true,
    val swipeEnabled: Boolean = true,
    val persianDigits: Boolean = true,
    val autoCaps: Boolean = true,
    val learnColloquial: Boolean = true,
    val animationStyle: String = "standard", // none | standard | playful
    val faLayoutStyle: FaLayoutStyle = FaLayoutStyle.STANDARD,
    val onboardingDone: Boolean = false,
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val PROFILE = stringPreferencesKey("finger_profile")
        val KEY_HEIGHT = floatPreferencesKey("key_height")
        val KEY_WIDTH = floatPreferencesKey("key_width_scale")
        val GAP = floatPreferencesKey("gap")
        val CORNER = floatPreferencesKey("corner")
        val FONT = floatPreferencesKey("font_scale")
        val HEIGHT = floatPreferencesKey("height_scale")
        val TRANSP = floatPreferencesKey("transparency")
        val THEME = stringPreferencesKey("theme_id")
        val KEY_COLOR = stringPreferencesKey("key_color")
        val BG_COLOR = stringPreferencesKey("bg_color")
        val HAPTIC = floatPreferencesKey("haptic_strength")
        val SOUND = booleanPreferencesKey("sound")
        val AUTOCORRECT = booleanPreferencesKey("autocorrect")
        val NEXT_WORD = booleanPreferencesKey("next_word")
        val SWIPE = booleanPreferencesKey("swipe")
        val FA_DIGITS = booleanPreferencesKey("persian_digits")
        val AUTO_CAPS = booleanPreferencesKey("auto_caps")
        val COLLOQUIAL = booleanPreferencesKey("colloquial")
        val ANIM = stringPreferencesKey("animation")
        val FA_LAYOUT = stringPreferencesKey("fa_layout")
        val ONBOARDING = booleanPreferencesKey("onboarding_done")
    }

    val flow: Flow<KeyboardSettings> = context.dataStore.data.map { p -> p.toSettings() }

    suspend fun current(): KeyboardSettings = flow.first()

    suspend fun update(transform: (KeyboardSettings) -> KeyboardSettings) {
        context.dataStore.edit { p ->
            val s = p.toSettings()
            val n = transform(s)
            p[Keys.PROFILE] = n.fingerProfile.name
            p[Keys.KEY_HEIGHT] = n.keyHeightDp
            p[Keys.KEY_WIDTH] = n.keyWidthScale
            p[Keys.GAP] = n.gapDp
            p[Keys.CORNER] = n.cornerRadiusDp
            p[Keys.FONT] = n.fontScale
            p[Keys.HEIGHT] = n.heightScale
            p[Keys.TRANSP] = n.transparency
            p[Keys.THEME] = n.themeId
            n.keyColorHex?.let { p[Keys.KEY_COLOR] = it }
            n.bgColorHex?.let { p[Keys.BG_COLOR] = it }
            p[Keys.HAPTIC] = n.hapticStrength.toFloat()
            p[Keys.SOUND] = n.soundEnabled
            p[Keys.AUTOCORRECT] = n.autocorrect
            p[Keys.NEXT_WORD] = n.nextWord
            p[Keys.SWIPE] = n.swipeEnabled
            p[Keys.FA_DIGITS] = n.persianDigits
            p[Keys.AUTO_CAPS] = n.autoCaps
            p[Keys.COLLOQUIAL] = n.learnColloquial
            p[Keys.ANIM] = n.animationStyle
            p[Keys.FA_LAYOUT] = n.faLayoutStyle.name
            p[Keys.ONBOARDING] = n.onboardingDone
        }
    }

    private fun androidx.datastore.preferences.core.Preferences.toSettings(): KeyboardSettings {
        val base = KeyboardSettings()
        return KeyboardSettings(
            fingerProfile = this[Keys.PROFILE]?.let { runCatching { FingerProfileKind.valueOf(it) }.getOrNull() } ?: base.fingerProfile,
            keyHeightDp = this[Keys.KEY_HEIGHT] ?: base.keyHeightDp,
            keyWidthScale = this[Keys.KEY_WIDTH] ?: base.keyWidthScale,
            gapDp = this[Keys.GAP] ?: base.gapDp,
            cornerRadiusDp = this[Keys.CORNER] ?: base.cornerRadiusDp,
            fontScale = this[Keys.FONT] ?: base.fontScale,
            heightScale = this[Keys.HEIGHT] ?: base.heightScale,
            transparency = this[Keys.TRANSP] ?: base.transparency,
            themeId = this[Keys.THEME] ?: base.themeId,
            keyColorHex = this[Keys.KEY_COLOR],
            bgColorHex = this[Keys.BG_COLOR],
            hapticStrength = (this[Keys.HAPTIC] ?: 35f).toInt(),
            soundEnabled = this[Keys.SOUND] ?: base.soundEnabled,
            autocorrect = this[Keys.AUTOCORRECT] ?: base.autocorrect,
            nextWord = this[Keys.NEXT_WORD] ?: base.nextWord,
            swipeEnabled = this[Keys.SWIPE] ?: base.swipeEnabled,
            persianDigits = this[Keys.FA_DIGITS] ?: base.persianDigits,
            autoCaps = this[Keys.AUTO_CAPS] ?: base.autoCaps,
            learnColloquial = this[Keys.COLLOQUIAL] ?: base.learnColloquial,
            animationStyle = this[Keys.ANIM] ?: base.animationStyle,
            faLayoutStyle = this[Keys.FA_LAYOUT]?.let { runCatching { FaLayoutStyle.valueOf(it) }.getOrNull() } ?: base.faLayoutStyle,
            onboardingDone = this[Keys.ONBOARDING] ?: base.onboardingDone,
        )
    }

    companion object {
        /** پیش‌تنظیم‌های پروفایل انگشت: (ارتفاع کلید، فاصله، ضریب عرض) */
        fun presetFor(profile: FingerProfileKind): Triple<Float, Float, Float> = when (profile) {
            FingerProfileKind.SMALL -> Triple(48f, 3f, 1.0f)
            FingerProfileKind.NORMAL -> Triple(54f, 4f, 1.0f)
            FingerProfileKind.LARGE -> Triple(62f, 6f, 1.06f)
        }
    }
}
