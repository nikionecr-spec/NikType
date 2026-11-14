package com.niktype.prediction.suggest

import com.niktype.engine.geometry.LanguageMode
import com.niktype.prediction.context.BigramModel
import com.niktype.prediction.context.UserModel
import com.niktype.prediction.dictionary.LanguageDictionary
import com.niktype.prediction.text.TextNormalizer

/**
 * پیشنهاد کلمات حین تایپ و پیش‌بینی کلمه بعدی.
 * امتیاز = بسامد دیکشنری + بسامد کاربر (وزن ۳) + پاداش بی‌گرام زمینه.
 */
class SuggestionEngine(
    private val faDict: LanguageDictionary,
    private val enDict: LanguageDictionary,
    private val user: UserModel,
    private val bigramsFa: BigramModel,
    private val bigramsEn: BigramModel,
) {

    fun suggest(rawPrefix: String, lang: LanguageMode, prevWord: String?, limit: Int = 3): List<String> {
        val norm = TextNormalizer.normalize(rawPrefix, lang)
        if (norm.isEmpty()) {
            return if (prevWord != null) nextWords(prevWord, lang, limit) else emptyList()
        }

        val dict = dictFor(lang)
        val scored = HashMap<String, Int>()

        for (e in dict.prefixWords(rawPrefix, 24)) {
            scored[e.word] = (scored[e.word] ?: 0) + e.freq
        }
        for (e in user.prefix(rawPrefix, lang, 24)) {
            scored[e.word] = (scored[e.word] ?: 0) + e.freq * 3
        }

        val prevNorm = prevWord?.let { TextNormalizer.normalize(it, lang) }
        if (prevNorm != null && prevNorm.isNotEmpty()) {
            val counts = bigramsFor(lang).nextCounts(prevNorm)
            if (counts.isNotEmpty()) {
                for ((w, c) in counts) {
                    if (w.startsWith(norm)) {
                        val disp = dict.displayFor(w) ?: user.displayFor(w, lang) ?: w
                        scored[disp] = (scored[disp] ?: 0) + c * 60
                    }
                }
            }
        }

        return scored.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
    }

    fun nextWords(rawPrev: String, lang: LanguageMode, limit: Int = 3): List<String> {
        val prevNorm = TextNormalizer.normalize(rawPrev, lang)
        if (prevNorm.isEmpty()) return emptyList()
        val dict = dictFor(lang)
        val out = ArrayList<Pair<String, Int>>()
        for ((w, c) in bigramsFor(lang).nextCounts(prevNorm)) {
            val disp = dict.displayFor(w) ?: user.displayFor(w, lang)
            if (disp != null) {
                val freq = dict.lookup(w)?.freq ?: 60
                out.add(disp to c * (freq / 100 + 1))
            }
        }
        out.sortByDescending { it.second }
        return out.take(limit).map { it.first }
    }

    private fun dictFor(lang: LanguageMode) =
        if (lang == LanguageMode.FA) faDict else enDict

    private fun bigramsFor(lang: LanguageMode) =
        if (lang == LanguageMode.FA) bigramsFa else bigramsEn
}
