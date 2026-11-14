package com.niktype.prediction

import com.niktype.engine.geometry.LanguageMode
import com.niktype.engine.geometry.LayoutParams
import com.niktype.engine.geometry.LayoutBuilder
import com.niktype.engine.geometry.LayoutMode
import com.niktype.engine.touch.TouchPoint
import com.niktype.prediction.swipe.SwipeDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SwipeTest {

    private fun layout() = LayoutBuilder().build(
        LanguageMode.EN, LayoutMode.LETTERS, LayoutParams(56f, 4f, 10f, 8f), 1080f, 320f,
    )

    private fun keyCenters(): Map<Char, Pair<Float, Float>> =
        layout().charToKey.mapValues { (_, kr) -> kr.centerX to kr.centerY }

    @Test
    fun decodesStraightWordPath() {
        val centers = keyCenters()
        // مسیر hello
        val word = "hello"
        val path = word.map { c ->
            val (x, y) = centers[c]!!
            TouchPoint(x, y, 0L)
        }
        val decoder = SwipeDecoder()
        val out = decoder.decode(
            path, centers, LanguageMode.EN,
            com.niktype.prediction.dictionary.LanguageDictionary(LanguageMode.EN, com.niktype.prediction.data.WordData.ENGLISH_WORDS),
        )
        assertTrue("expected hello in candidates: $out", out.any { it.word == "hello" })
    }

    @Test
    fun tooShortPathIsRejected() {
        val centers = keyCenters()
        val path = listOf(
            TouchPoint(centers['q']!!.first, centers['q']!!.second, 0L),
            TouchPoint(centers['w']!!.first, centers['w']!!.second, 0L),
        )
        val out = SwipeDecoder().decode(
            path, centers, LanguageMode.EN,
            com.niktype.prediction.dictionary.LanguageDictionary(LanguageMode.EN, com.niktype.prediction.data.WordData.ENGLISH_WORDS),
        )
        assertTrue(out.isEmpty())
    }

    @Test
    fun straightLineTwoKeysIsNotGesture() {
        val centers = keyCenters()
        // q -> w -> q : فقط ۲ کلید متمایز
        val path = listOf(
            TouchPoint(centers['q']!!.first, centers['q']!!.second, 0L),
            TouchPoint(centers['w']!!.first, centers['w']!!.second, 0L),
            TouchPoint(centers['q']!!.first, centers['q']!!.second, 0L),
        )
        val out = SwipeDecoder().decode(
            path, centers, LanguageMode.EN,
            com.niktype.prediction.dictionary.LanguageDictionary(LanguageMode.EN, com.niktype.prediction.data.WordData.ENGLISH_WORDS),
        )
        assertTrue(out.isEmpty())
    }

    @Test
    fun persianPathDecodesKnownWord() {
        val layout = LayoutBuilder().build(
            LanguageMode.FA, LayoutMode.LETTERS, LayoutParams(56f, 4f, 10f, 8f), 1080f, 320f,
        )
        val centers = layout.charToKey.mapValues { (_, kr) -> kr.centerX to kr.centerY }
        val path = "سلام".map { c ->
            val (x, y) = centers[c]!!
            TouchPoint(x, y, 0L)
        }
        val out = SwipeDecoder().decode(
            path, centers, LanguageMode.FA,
            com.niktype.prediction.dictionary.LanguageDictionary(LanguageMode.FA, com.niktype.prediction.data.WordData.PERSIAN_WORDS),
        )
        assertTrue("expected سلام in candidates: $out", out.any { it.word == "سلام" })
    }
}
