package com.niktype.prediction.correct

import com.niktype.engine.geometry.LanguageMode
import com.niktype.prediction.dictionary.FuzzyMatch
import com.niktype.prediction.dictionary.LanguageDictionary
import com.niktype.prediction.text.TextNormalizer

data class Correction(
    val original: String,
    val corrected: String,
    val confidence: Float,
)

data class CorrectConfig(
    val minLength: Int = 3,
    val maxDistance: Int = 2,
    val minFreq: Int = 150,
    val veryFreq: Int = 1200,
)

/**
 * اصلاح خودکار محافظه‌کارانه — طراحی‌شده برای «جلوگیری از انتخاب اشتباه»:
 *
 * قواعد سخت‌گیرانه:
 * 1) واژه‌ای که در دیکشنری هست هرگز اصلاح نمی‌شود.
 * 2) واژه‌های کوتاه‌تر از ۳ نویسه اصلاح نمی‌شوند.
 * 3) واژه‌های دارای رقم یا شبیه نام خاص اصلاح نمی‌شوند.
 * 4) جایگزینی با فاصله ۲ فقط برای واژه‌های خیلی پرتکرار مجاز است.
 * 5) اگر نویسه‌ی جایگزین از میان کاندیداهای لمسیِ همان موقعیت نیامده،
 *    فقط با فاصله ۱ پذیرفته می‌شود.
 * 6) بسامد واژه جایگزین باید از حد آستانه بالاتر باشد.
 */
class AutoCorrector(
    private val faDict: LanguageDictionary,
    private val enDict: LanguageDictionary,
    private val config: CorrectConfig = CorrectConfig(),
) {

    fun correct(
        raw: String,
        language: LanguageMode,
        charAlts: List<List<Char>> = emptyList(),
    ): Correction? {
        if (raw.any { it.isDigit() }) return null
        if (raw.contains('/') || raw.contains('@') || raw.contains('.')) return null
        val norm = TextNormalizer.normalize(raw, language)
        if (norm.length < config.minLength) return null
        if (looksLikeProperNoun(raw, language)) return null

        val dict = if (language == LanguageMode.FA) faDict else enDict
        if (dict.lookup(norm) != null) return null

        val candidates = dict.fuzzy(norm, config.maxDistance, 12)
        if (candidates.isEmpty()) return null

        var best: FuzzyMatch? = null
        var bestScore = -1f
        for (c in candidates) {
            if (c.entry.freq < config.minFreq) continue
            if (c.distance >= config.maxDistance && c.entry.freq < config.veryFreq) continue

            var factor = 1f
            var reject = false
            for (i in 0 until minOf(c.entry.word.length, norm.length)) {
                if (c.entry.word[i] != norm[i]) {
                    val hasTouchAlt = charAlts.getOrNull(i)?.contains(c.entry.word[i]) == true
                    if (!hasTouchAlt) {
                        factor *= 0.35f
                        if (c.distance > 1) { reject = true; break }
                    }
                }
            }
            if (reject) continue

            val score = c.entry.freq * factor / (1f + c.distance * c.distance)
            if (score > bestScore) { bestScore = score; best = c }
        }

        val b = best ?: return null
        val display = dict.displayFor(b.entry.word) ?: b.entry.word
        if (display == raw) return null

        val confidence = ((b.entry.freq / 2000f).coerceIn(0.5f, 1f)) *
            (if (b.distance == 1) 1f else 0.8f)
        return Correction(raw, display, confidence)
    }

    private fun looksLikeProperNoun(raw: String, language: LanguageMode): Boolean = when (language) {
        LanguageMode.EN -> raw.any { it.isUpperCase() && raw.indexOf(it) > 0 } ||
            (raw.length > 1 && raw.all { it.isUpperCase() })
        LanguageMode.FA -> false
    }
}
