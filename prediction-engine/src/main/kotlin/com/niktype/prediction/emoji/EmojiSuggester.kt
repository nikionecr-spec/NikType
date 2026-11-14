package com.niktype.prediction.emoji

import com.niktype.prediction.text.TextNormalizer
import com.niktype.engine.geometry.LanguageMode

/**
 * پیشنهاد ایموجی بر اساس واژه قبلی (فارسی یا انگلیسی).
 * نگاشت کلیدواژه به ایموجی کاملاً محلی و بدون شبکه است.
 */
class EmojiSuggester(keywords: Map<String, List<String>>) {

    private val byNorm = HashMap<String, List<String>>(keywords.size * 2)

    init {
        for ((k, v) in keywords) {
            val lang = if (k.any { it in 'a'..'z' }) LanguageMode.EN else LanguageMode.FA
            val norm = TextNormalizer.normalize(k, lang)
            if (norm.isNotEmpty()) byNorm[norm] = v
        }
    }

    fun suggest(rawWord: String, limit: Int = 3): List<String> {
        val cleaned = rawWord.trim(*TRIM_CHARS)
        val lang = if (cleaned.any { it in 'a'..'z' }) LanguageMode.EN else LanguageMode.FA
        val w = TextNormalizer.normalize(cleaned, lang)
        if (w.length < 2) return emptyList()

        byNorm[w]?.let { return it.take(limit) }

        for ((k, v) in byNorm) {
            if (k.length >= 3 && w.contains(k)) return v.take(limit)
            if (w.length >= 3 && k.contains(w)) return v.take(limit)
        }
        return emptyList()
    }

    companion object {
        private val TRIM_CHARS = charArrayOf(' ', '\t', '\n', '.', '،', ',', '!', '?', '؟', ';', ':', '؛')
    }
}
