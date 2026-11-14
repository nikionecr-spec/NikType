package com.niktype.prediction.text

import com.niktype.engine.geometry.LanguageMode

/**
 * نرمال‌سازی متن برای تطبیق در دیکشنری.
 * فارسی: حذف نیم‌فاصله، یکسان‌سازی «ي/ک» عربی با «ی/ک» فارسی، حذف اعراب.
 * انگلیسی: حروف کوچک و حذف آپاستروف (don't → dont برای تطبیق).
 *
 * نکته مهم: «آ» با «ا» یکی نمی‌شود چون در فارسی حروف جداگانه‌ای هستند.
 */
object TextNormalizer {

    private val ARABIC_DIACRITICS = CharArray(0) // پر می‌شود در init پایین

    fun normalize(raw: String, language: LanguageMode): String = when (language) {
        LanguageMode.FA -> normalizeFa(raw)
        LanguageMode.EN -> normalizeEn(raw)
    }

    private fun normalizeFa(raw: String): String {
        if (raw.isEmpty()) return raw
        val sb = StringBuilder(raw.length)
        for (c in raw) {
            when (c) {
                '\u200C', '\u200D', '\u200E', '\u200F' -> { /* نیم‌فاصله و علائم کنترلی حذف */ }
                'ي' -> sb.append('ی')
                'ك' -> sb.append('ک')
                'ة' -> sb.append('ه')
                'ۀ' -> sb.append('ه')
                'ٱ' -> sb.append('ا')
                in '\u064B'..'\u0652', '\u0670' -> { /* اعراب و تشدید حذف */ }
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    private fun normalizeEn(raw: String): String {
        if (raw.isEmpty()) return raw
        val sb = StringBuilder(raw.length)
        for (c in raw) {
            when (c) {
                '\'', '\u2019' -> { /* آپاستروف حذف */ }
                else -> sb.append(c.lowercaseChar())
            }
        }
        return sb.toString()
    }
}

/**
 * ابزارهای متن فارسی: اعداد فارسی، نیم‌فاصله و الگوهای نیازمند نیم‌فاصله.
 */
object PersianText {
    const val ZWNJ = "\u200C"

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(s: String): String {
        val sb = StringBuilder(s.length)
        for (c in s) {
            if (c in '0'..'9') sb.append(persianDigits[c - '0']) else sb.append(c)
        }
        return sb.toString()
    }

    fun toEnglishDigits(s: String): String {
        val sb = StringBuilder(s.length)
        for (c in s) {
            when (c) {
                in '۰'..'۹' -> sb.append(('0' + (c - '۰')))
                in '٠'..'٩' -> sb.append(('0' + (c - '٠')))
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    /** پیشوند «می‌» بدون نیم‌فاصله تایپ شده است؛ پیشنهاد نیم‌فاصله بده. */
    fun needsZwnjAfterPrefix(chunk: String): Boolean {
        if (chunk.contains(ZWNJ)) return false
        return chunk.startsWith("می") && chunk.length > 2
    }

    /** پسوند‌های نیازمند نیم‌فاصله: ها، تر، ترین، ام، ایم، اش. */
    fun needsZwnjBeforeSuffix(chunk: String): Boolean {
        if (chunk.contains(ZWNJ)) return false
        for (suffix in SUFFIXES) {
            if (chunk.endsWith(suffix) && chunk.length > suffix.length + 1) return true
        }
        return false
    }

    private val SUFFIXES = listOf("ها", "تر", "ترین", "ام", "ایم", "ات", "اش", "مان", "تان", "شان")
}
