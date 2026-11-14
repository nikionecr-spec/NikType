package com.niktype.engine

import com.niktype.engine.geometry.FaLayoutStyle
import com.niktype.engine.geometry.KeyboardLayout
import com.niktype.engine.geometry.LanguageMode
import com.niktype.engine.geometry.LayoutParams
import com.niktype.engine.geometry.LayoutBuilder
import com.niktype.engine.geometry.LayoutMode
import com.niktype.engine.touch.FingerProfiles
import com.niktype.engine.touch.KeyCalibration
import com.niktype.engine.touch.TouchResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TouchResolverTest {

    private fun buildLayout(
        width: Float = 1080f,
        height: Float = 320f,
        language: LanguageMode = LanguageMode.EN,
        mode: LayoutMode = LayoutMode.LETTERS,
    ): KeyboardLayout =
        LayoutBuilder().build(language, mode, LayoutParams(56f, 4f, 10f, 8f), width, height)

    private fun resolver(profile: com.niktype.engine.touch.TouchProfile = FingerProfiles.NORMAL_PROFILE) =
        TouchResolver(profile, KeyCalibration())

    @Test
    fun centerTouchResolvesToExpectedKey() {
        val layout = buildLayout()
        val d = layout.charToKey['d']!!
        val cands = resolver().resolve(d.centerX, d.centerY, layout)
        assertTrue(cands.isNotEmpty())
        assertEquals("d", cands[0].rect.key.id)
        assertTrue("top probability should dominate", cands[0].probability > 0.5f)
    }

    @Test
    fun gapTouchReturnsAdjacentKeys() {
        val layout = buildLayout()
        val c = layout.charToKey['c']!!
        val d = layout.charToKey['d']!!
        val gapX = (c.right + d.x) / 2f
        val cands = resolver().resolve(gapX, c.centerY, layout)
        val ids = cands.take(2).map { it.rect.key.id }
        assertTrue("c and d must dominate at shared edge", ids.contains("c") && ids.contains("d"))
    }

    @Test
    fun outsideKeyboardIsRejected() {
        val layout = buildLayout()
        assertTrue(resolver().resolve(-60f, -60f, layout).isEmpty())
        assertTrue(resolver().resolve(layout.width + 120f, layout.height + 120f, layout).isEmpty())
    }

    @Test
    fun fastTypingIncreasesTolerance() {
        val layout = buildLayout()
        val g = layout.charToKey['g']!!
        // لمس کمی جابه‌جا از مرکز
        val x = g.centerX + g.width * 0.22f
        val slow = resolver().resolve(x, g.centerY, layout, speedCps = 0f)
        val fast = resolver().resolve(x, g.centerY, layout, speedCps = 8f)
        val slowP = slow.firstOrNull { it.rect.key.id == "g" }?.probability ?: 0f
        val fastP = fast.firstOrNull { it.rect.key.id == "g" }?.probability ?: 0f
        assertTrue("fast typing should be more tolerant", fastP >= slowP)
    }

    @Test
    fun calibrationCompensatesSystematicOffset() {
        val layout = buildLayout()
        val k = layout.charToKey['k']!!
        val cal = KeyCalibration()
        cal.learn("k", 12f, 0f, k.width, k.height)
        val r = TouchResolver(FingerProfiles.NORMAL_PROFILE, cal)
        val cands = r.resolve(k.centerX - 12f, k.centerY, layout)
        assertEquals("k", cands[0].rect.key.id)
    }

    @Test
    fun largeProfileIsMoreTolerantThanSmall() {
        val layout = buildLayout()
        val g = layout.charToKey['g']!!
        val x = g.centerX + g.width * 0.22f
        val small = resolver(FingerProfiles.SMALL_PROFILE).resolve(x, g.centerY, layout)
        val large = resolver(FingerProfiles.LARGE_PROFILE).resolve(x, g.centerY, layout)
        val smallP = small.firstOrNull { it.rect.key.id == "g" }?.probability ?: 0f
        val largeP = large.firstOrNull { it.rect.key.id == "g" }?.probability ?: 0f
        assertTrue("large profile must accept sloppy touches more", largeP > smallP)
    }

    @Test
    fun probabilitiesAreNormalized() {
        val layout = buildLayout()
        val w = layout.charToKey['w']!!
        val cands = resolver().resolve(w.centerX + 3f, w.centerY - 3f, layout)
        val sum = cands.sumOf { it.probability.toDouble() }.toFloat()
        assertTrue("sum of probabilities should be high", sum > 0.9f && sum <= 1.0001f)
    }

    @Test
    fun persianLayoutResolvesCorrectly() {
        val layout = buildLayout(language = LanguageMode.FA)
        val sin = layout.charToKey['س']!!
        val cands = resolver().resolve(sin.centerX, sin.centerY, layout)
        assertEquals("س", cands[0].rect.key.output)
    }

    @Test
    fun verticalRowResolution() {
        val layout = buildLayout()
        val q = layout.charToKey['q']!!
        val a = layout.charToKey['a']!!
        assertTrue("q and a must be in different rows", q.centerY != a.centerY)
        val cands = resolver().resolve(a.centerX, a.centerY, layout)
        assertEquals("a", cands[0].rect.key.id)
    }
}
