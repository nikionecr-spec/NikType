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
 * 5) جایگزینی نویسه‌ای که از کاندیدهای لمسی همان موقعیت نیامده، جریمه
 *    امتیاز می‌گیرد (0.35×) تا اولویت با جایگزین‌های لمسی باشد.
 * 6) جابه‌جایی حروف مجاور (teh→the) به‌عنوان رایج‌ترین غلط تایپ با
 *    فاصله ۱ در نظر گرفته می‌شود.
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

        // امتیاز هر کاندید: freq × factor / (1 + d²)
        var bestFreq = -1
        var bestWord: String? = null
        var bestScore = -1f
        var bestDistance = 99

        // ۱) نامزدهای فازی دیکشنری
        for (c in dict.fuzzy(norm, config.maxDistance, 12)) {
            if (c.entry.freq < config.minFreq) continue
            if (c.distance >= config.maxDistance && c.entry.freq < config.veryFreq) continue

            var factor = 1f
            for (i in 0 until minOf(c.entry.word.length, norm.length)) {
                if (c.entry.word[i] != norm[i]) {
                    val hasTouchAlt = charAlts.getOrNull(i)?.contains(c.entry.word[i]) == true
                    if (!hasTouchAlt) factor *= 0.35f
                }
            }
            val score = c.entry.freq * factor / (1f + c.distance * c.distance)
            if (score > bestScore) {
                bestScore = score
                bestFreq = c.entry.freq
                bestWord = c.entry.word
                bestDistance = c.distance
            }
        }

        // ۲) جابه‌جایی حروف مجاور — رایج‌ترین خطای تایپ سریع
        for (i in 0 until norm.length - 1) {
            val swapped = swapAt(norm, i)
            if (swapped == norm) continue
            val entry = dict.lookup(swapped) ?: continue
            if (entry.freq < config.minFreq) continue
            val score = entry.freq / 2f
            if (score > bestScore) {
                bestScore = score
                bestFreq = entry.freq
                bestWord = entry.word
                bestDistance = 1
            }
        }

        val word = bestWord ?: return null
        val display = dict.displayFor(word) ?: word
        if (display == raw) return null

        val confidence = ((bestFreq / 2000f).coerceIn(0.5f, 1f)) *
            (if (bestDistance == 1) 1f else 0.8f)
        return Correction(raw, display, confidence)
    }

    private fun swapAt(s: String, i: Int): String {
        val sb = StringBuilder(s)
        val tmp = sb[i]
        sb[i] = sb[i + 1]
        sb[i + 1] = tmp
        return sb.toString()
    }

    private fun looksLikeProperNoun(raw: String, language: LanguageMode): Boolean = when (language) {
        LanguageMode.EN -> raw.any { it.isUpperCase() && raw.indexOf(it) > 0 } ||
            (raw.length > 1 && raw.all { it.isUpperCase() })
        LanguageMode.FA -> false
    }
}
