package com.niktype.engine

import com.niktype.engine.geometry.FaLayoutStyle
import com.niktype.engine.geometry.KeyType
import com.niktype.engine.geometry.KeyboardLayout
import com.niktype.engine.geometry.LanguageMode
import com.niktype.engine.geometry.LayoutParams
import com.niktype.engine.geometry.LayoutBuilder
import com.niktype.engine.geometry.LayoutMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutTest {

    private fun build(
        language: LanguageMode,
        mode: LayoutMode = LayoutMode.LETTERS,
        persianDigits: Boolean = true,
        faStyle: FaLayoutStyle = FaLayoutStyle.STANDARD,
        width: Float = 1080f,
    ): KeyboardLayout =
        LayoutBuilder().build(language, mode, LayoutParams(56f, 4f, 10f, 8f), width, 320f, persianDigits, faStyle)

    @Test
    fun englishLayoutHasAllLetters() {
        val layout = build(LanguageMode.EN)
        for (c in 'a'..'z') {
            assertTrue("missing letter $c", layout.charToKey.containsKey(c))
        }
    }

    @Test
    fun persianStandardLayoutHasCoreLetters() {
        val layout = build(LanguageMode.FA)
        for (c in listOf('ا', 'ض', 'پ', 'گ', 'ژ', 'ص', 'ک', 'ی')) {
            assertTrue("missing $c", layout.charToKey.containsKey(c))
        }
        assertNotNull(layout.keyById("backspace"))
        assertNotNull(layout.keyById("space"))
        assertNotNull(layout.keyById("enter"))
    }

    @Test
    fun persianAlphabeticLayoutWorks() {
        val layout = build(LanguageMode.FA, faStyle = FaLayoutStyle.ALPHABETIC)
        assertTrue(layout.charToKey.containsKey('ا'))
        assertTrue(layout.charToKey.containsKey('ی'))
        // در چیدمان الفبایی بک‌اسپیس در ردیف پایین است
        val bs = layout.keyById("backspace")
        assertNotNull(bs)
        assertTrue(bs!!.y >= layout.height - 120f)
    }

    @Test
    fun keyIdsAreUnique() {
        val layout = build(LanguageMode.FA)
        val ids = layout.keys.map { it.key.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun rowsFitInWidth() {
        for (lang in listOf(LanguageMode.FA, LanguageMode.EN)) {
            val layout = build(lang, width = 720f) // صفحه باریک
            for (row in layout.rows) {
                assertTrue(row.last().right <= layout.width + 0.5f)
            }
        }
    }

    @Test
    fun persianDigitsToggle() {
        val fa = build(LanguageMode.FA, mode = LayoutMode.SYMBOLS, persianDigits = true)
        assertTrue(fa.charToKey.containsKey('۱'))
        val en = build(LanguageMode.FA, mode = LayoutMode.SYMBOLS, persianDigits = false)
        assertTrue(en.charToKey.containsKey('1'))
    }

    @Test
    fun symbolsLayoutHasZwnjKey() {
        val layout = build(LanguageMode.FA, mode = LayoutMode.SYMBOLS)
        val zwnj = layout.keyById("zwnj")
        assertNotNull(zwnj)
        assertEquals("\u200C", zwnj!!.key.output)
    }

    @Test
    fun faPeriodAndComma() {
        val layout = build(LanguageMode.FA)
        assertEquals("،", layout.charToKey['،']!!.key.output)
        assertTrue(layout.charToKey.containsKey('.'))
    }

    @Test
    fun narrowScreenKeepsMinimumKeyWidth() {
        // با عرض 320 نباید هیچ کلیدی عرض منفی یا صفر داشته باشد
        val layout = build(LanguageMode.EN, width = 320f)
        for (k in layout.keys) {
            assertTrue(k.width > 0f)
            assertTrue(k.height > 0f)
        }
    }
}
