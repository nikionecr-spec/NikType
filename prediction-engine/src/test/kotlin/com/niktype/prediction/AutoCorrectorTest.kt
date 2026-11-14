package com.niktype.prediction

import com.niktype.engine.geometry.LanguageMode
import com.niktype.prediction.correct.AutoCorrector
import com.niktype.prediction.dictionary.LanguageDictionary
import com.niktype.prediction.text.PersianText
import com.niktype.prediction.text.TextNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoCorrectorTest {

    private val fa = LanguageDictionary(LanguageMode.FA, com.niktype.prediction.data.WordData.PERSIAN_WORDS)
    private val en = LanguageDictionary(LanguageMode.EN, com.niktype.prediction.data.WordData.ENGLISH_WORDS)
    private val corrector = AutoCorrector(fa, en)

    @Test
    fun correctsSingleEditPersianTypo() {
        // سلم ← سلام (فاصله ۱)
        val c = corrector.correct("سلم", LanguageMode.FA)
        assertNotNull(c)
        assertEquals("سلام", c!!.corrected)
    }

    @Test
    fun validWordIsNeverTouched() {
        assertNull(corrector.correct("سلام", LanguageMode.FA))
        assertNull(corrector.correct("کتاب", LanguageMode.FA))
    }

    @Test
    fun correctsTranspositionInEnglish() {
        // teh ← the (فاصله ۲، بسامد خیلی بالا)
        val c = corrector.correct("teh", LanguageMode.EN)
        assertNotNull(c)
        assertEquals("the", c!!.corrected)
    }

    @Test
    fun wordsWithDigitsAreNeverCorrected() {
        assertNull(corrector.correct("ab1c", LanguageMode.EN))
    }

    @Test
    fun properNounsAreNeverCorrected() {
        assertNull(corrector.correct("iPhone", LanguageMode.EN))
        assertNull(corrector.correct("NASA", LanguageMode.EN))
    }

    @Test
    fun shortWordsAreNeverCorrected() {
        assertNull(corrector.correct("hi", LanguageMode.EN))
        assertNull(corrector.correct("به", LanguageMode.FA))
    }

    @Test
    fun unknownLongWordIsNotInvented() {
        // هیچ واژه با فاصله ۲ از این رشته در دیکشنری نیست
        assertNull(corrector.correct("zxzqzv", LanguageMode.EN))
    }

    @Test
    fun touchAlternativesIncreaseConfidence() {
        val withAlts = corrector.correct("سلم", LanguageMode.FA, charAlts = listOf(listOf('خ'), listOf('ل'), listOf('ا'), listOf('م')))
        val withoutAlts = corrector.correct("سلم", LanguageMode.FA)
        assertNotNull(withAlts)
        assertNotNull(withoutAlts)
        assertTrue(withAlts!!.confidence >= withoutAlts!!.confidence)
    }
}

class PersianTextTest {

    @Test
    fun digitConversion() {
        assertEquals("۱۲۳", PersianText.toPersianDigits("123"))
        assertEquals("45", PersianText.toEnglishDigits("۴۵"))
        assertEquals("۱۳۹۸", PersianText.toPersianDigits("1398"))
    }

    @Test
    fun zwnjSuggestions() {
        assertTrue(PersianText.needsZwnjAfterPrefix("میخوام"))
        assertTrue(PersianText.needsZwnjAfterPrefix("میروم"))
        assertTrue(!PersianText.needsZwnjAfterPrefix("می‌خوام"))
        assertTrue(!PersianText.needsZwnjAfterPrefix("کتاب"))
        assertTrue(PersianText.needsZwnjBeforeSuffix("کتابها"))
        assertTrue(!PersianText.needsZwnjBeforeSuffix("هاها"))
    }

    @Test
    fun normalizationUnifiesArabicLetters() {
        assertEquals(
            TextNormalizer.normalize("كتاب", LanguageMode.FA),
            TextNormalizer.normalize("کتاب", LanguageMode.FA),
        )
        assertEquals(
            TextNormalizer.normalize("قím", LanguageMode.EN),
            "qim",
        )
    }

    @Test
    fun dictionaryLookupIgnoresZwnjAndArabicForms() {
        val dict = LanguageDictionary(LanguageMode.FA, com.niktype.prediction.data.WordData.PERSIAN_WORDS)
        assertNotNull(dict.lookup("می‌خواهم"))
        assertNotNull(dict.lookup("كتاب"))
        assertEquals("می‌خواهم", dict.displayFor(TextNormalizer.normalize("می‌خواهم", LanguageMode.FA)))
    }
}
