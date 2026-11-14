package com.niktype.prediction.dictionary

import com.niktype.engine.geometry.LanguageMode
import com.niktype.prediction.text.TextNormalizer

/**
 * دیکشنری یک زبان: واژگان پایه + واژگان یادگرفته‌شده از کاربر.
 * تطبیق‌ها همه روی شکل نرمال‌شده انجام می‌شود ولی شکل نمایش اصلی حفظ می‌شود
 * (مثلاً «می‌خوام» با نیم‌فاصله یا حروف کوچک/بزرگ انگلیسی).
 */
class LanguageDictionary(
    val language: LanguageMode,
    baseWords: List<Pair<String, Int>>,
) {
    private val trie = Trie()
    private val display = HashMap<String, String>()      // norm -> نمایش اصلی پایه
    private val userDisplay = HashMap<String, String>()  // norm -> نمایش اصلی کاربر

    init {
        for ((w, f) in baseWords) {
            addInternal(w, f, false)
        }
    }

    private fun addInternal(word: String, freq: Int, user: Boolean) {
        val norm = TextNormalizer.normalize(word, language)
        if (norm.isEmpty()) return
        trie.insert(norm, freq)
        if (user) {
            userDisplay[norm] = word
        } else if (!display.containsKey(norm)) {
            display[norm] = word
        }
    }

    /** افزودن یا تقویت واژه یادگرفته‌شده از کاربر. */
    fun addUserWord(word: String, freq: Int = USER_WORD_FREQ) {
        addInternal(word, freq, true)
    }

    fun lookup(raw: String): WordEntry? {
        val norm = TextNormalizer.normalize(raw, language)
        if (norm.isEmpty()) return null
        return trie.lookup(norm)
    }

    fun prefixWords(rawPrefix: String, limit: Int = 20): List<WordEntry> {
        val norm = TextNormalizer.normalize(rawPrefix, language)
        if (norm.isEmpty()) return emptyList()
        return trie.prefixWords(norm, limit)
    }

    fun fuzzy(raw: String, maxDist: Int, limit: Int = 8): List<FuzzyMatch> {
        val norm = TextNormalizer.normalize(raw, language)
        if (norm.isEmpty()) return emptyList()
        return trie.fuzzy(norm, maxDist, limit)
    }

    fun wordsStartingWith(ch: Char, limit: Int = 300): List<WordEntry> {
        val norm = TextNormalizer.normalize(ch.toString(), language)
        if (norm.isEmpty()) return emptyList()
        return trie.prefixWords(norm, limit)
    }

    fun displayFor(norm: String): String? = userDisplay[norm] ?: display[norm]

    val size: Int get() = trie.size

    companion object {
        const val USER_WORD_FREQ = 120
    }
}
