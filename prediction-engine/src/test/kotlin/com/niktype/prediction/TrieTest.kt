package com.niktype.prediction

import com.niktype.prediction.dictionary.Trie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrieTest {

    private fun build(): Trie {
        val t = Trie()
        t.insert("سلام", 1500)
        t.insert("سلامت", 300)
        t.insert("سلم", 10)
        t.insert("کتاب", 800)
        t.insert("کتابها", 100)
        return t
    }

    @Test
    fun lookupWorks() {
        val t = build()
        assertNotNull(t.lookup("سلام"))
        assertNull(t.lookup("سلا"))
        assertNull(t.lookup("ناموجود"))
    }

    @Test
    fun prefixSearch() {
        val t = build()
        val words = t.prefixWords("سلا", 10).map { it.word }
        assertTrue(words.contains("سلام"))
        assertTrue(words.contains("سلامت"))
        // مرتب‌سازی بر اساس بسامد
        assertEquals("سلام", words.first())
    }

    @Test
    fun fuzzyWithinRadius() {
        val t = build()
        val matches = t.fuzzy("سالم", 1, 10)
        assertTrue(matches.any { it.entry.word == "سلام" && it.distance == 1 })
        val none = t.fuzzy("زززز", 1, 10)
        assertTrue(none.isEmpty() || none.all { it.distance <= 1 })
    }

    @Test
    fun sizeCountsUniqueWords() {
        val t = build()
        assertEquals(5, t.size)
        t.insert("سلام", 900) // بسامد بالاتر قبلی حفظ می‌شود؟ نه — حداکثر
        assertEquals(5, t.size)
        assertTrue(t.lookup("سلام")!!.freq >= 900)
    }
}
