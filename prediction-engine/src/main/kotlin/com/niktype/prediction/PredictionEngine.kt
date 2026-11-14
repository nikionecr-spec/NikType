package com.niktype.prediction

import com.niktype.engine.geometry.LanguageMode
import com.niktype.engine.touch.TouchPoint
import com.niktype.prediction.context.BigramModel
import com.niktype.prediction.context.LearningStore
import com.niktype.prediction.context.PersistedModel
import com.niktype.prediction.context.UserModel
import com.niktype.prediction.correct.AutoCorrector
import com.niktype.prediction.correct.Correction
import com.niktype.prediction.dictionary.LanguageDictionary
import com.niktype.prediction.emoji.EmojiSuggester
import com.niktype.prediction.suggest.SuggestionEngine
import com.niktype.prediction.swipe.SwipeCandidate
import com.niktype.prediction.swipe.SwipeDecoder
import com.niktype.prediction.text.TextNormalizer

/**
 * نما (Facade) موتور پیش‌بینی — تنها نقطه اتصال لایه اپلیکیشن.
 * همه چیز محلی است؛ هیچ عملیات شبکه‌ای وجود ندارد.
 */
class PredictionEngine(
    private val learningStore: LearningStore? = null,
) {

    private val faDict = LanguageDictionary(LanguageMode.FA, WordData.PERSIAN_WORDS + WordData.COLLOQUIAL_FA)
    private val enDict = LanguageDictionary(LanguageMode.EN, WordData.ENGLISH_WORDS)

    private val user = UserModel()
    private val bigramsFa = BigramModel().apply { seed(WordData.BIGRAMS_FA) }
    private val bigramsEn = BigramModel().apply { seed(WordData.BIGRAMS_EN) }

    val emoji = EmojiSuggester(WordData.EMOJI_KEYWORDS)

    private val corrector = AutoCorrector(faDict, enDict)
    private val suggester = SuggestionEngine(faDict, enDict, user, bigramsFa, bigramsEn)
    private val decoder = SwipeDecoder()

    init {
        restore()
    }

    fun suggestions(rawPrefix: String, lang: LanguageMode, prevWord: String?, limit: Int = 3): List<String> =
        suggester.suggest(rawPrefix, lang, prevWord, limit)

    fun nextWords(rawPrev: String, lang: LanguageMode, limit: Int = 3): List<String> =
        suggester.nextWords(rawPrev, lang, limit)

    fun correct(raw: String, lang: LanguageMode, charAlts: List<List<Char>> = emptyList()): Correction? =
        corrector.correct(raw, lang, charAlts)

    fun isKnown(raw: String, lang: LanguageMode): Boolean =
        dictFor(lang).lookup(raw) != null

    fun learn(rawWord: String, prevWord: String?, lang: LanguageMode) {
        val norm = TextNormalizer.normalize(rawWord, lang)
        if (norm.length < 2 || rawWord.any { it.isDigit() }) return
        if (rawWord.contains('/') || rawWord.contains('@') || rawWord.contains("www")) return
        user.observe(rawWord)
        val freq = (user.freq(norm) + LanguageDictionary.USER_WORD_FREQ).coerceAtMost(4000)
        dictFor(lang).addUserWord(rawWord, freq)
        if (prevWord != null && prevWord.length >= 2) {
            bigramsFor(lang).observe(TextNormalizer.normalize(prevWord, lang), norm)
        }
    }

    fun swipe(
        path: List<TouchPoint>,
        keyCenters: Map<Char, Pair<Float, Float>>,
        lang: LanguageMode,
        limit: Int = 3,
    ): List<SwipeCandidate> = decoder.decode(path, keyCenters, lang, dictFor(lang), limit)

    fun emojiFor(rawWord: String): List<String> = emoji.suggest(rawWord)

    fun persist() {
        learningStore?.save(
            PersistedModel(
                userWords = user.toLines(),
                bigramsFa = bigramsFa.toLines(),
                bigramsEn = bigramsEn.toLines(),
            )
        )
    }

    private fun restore() {
        val model = learningStore?.load() ?: return
        user.restore(model.userWords)
        bigramsFa.restore(model.bigramsFa)
        bigramsEn.restore(model.bigramsEn)
    }

    private fun dictFor(lang: LanguageMode) = if (lang == LanguageMode.FA) faDict else enDict
    private fun bigramsFor(lang: LanguageMode) = if (lang == LanguageMode.FA) bigramsFa else bigramsEn

    companion object {
        /** بسامد تقریبی واژه‌های یادگرفته‌شده جدید برای رتبه‌بندی پیشنهادها. */
    }
}
