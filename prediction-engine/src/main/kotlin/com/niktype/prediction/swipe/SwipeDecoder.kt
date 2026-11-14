package com.niktype.prediction.swipe

import com.niktype.engine.geometry.LanguageMode
import com.niktype.engine.touch.TouchPoint
import com.niktype.prediction.dictionary.LanguageDictionary
import kotlin.math.sqrt

data class SwipeCandidate(
    val word: String,
    val score: Float,
)

/**
 * رمزگشای تایپ کشیدنی (Swipe / Gesture Typing).
 *
 * روش: مسیر لمس به ~۱۴ نقطه نمونه‌برداری می‌شود؛ برای هر واژه‌ی نامزد
 * (که با نخستین و واپسین کلید مسیر شروع/پایان می‌شود) فاصله متوسط
 * نقاط مسیر تا مراکز حروف واژه (با هم‌ترازی تناسبی) محاسبه و با
 * پاداش بسامد جمع می‌شود. نامزدها با کمترین امتیاز برترند.
 */
class SwipeDecoder {

    fun decode(
        path: List<TouchPoint>,
        keyCenters: Map<Char, Pair<Float, Float>>,
        lang: LanguageMode,
        dict: LanguageDictionary,
        limit: Int = 3,
    ): List<SwipeCandidate> {
        if (path.size < 4) return emptyList()
        val pts = downsample(path, MAX_POINTS)
        if (pts.size < 4) return emptyList()

        // کلید نزدیک هر نقطه
        val hits = ArrayList<Char>(pts.size)
        for (p in pts) {
            var bestC: Char? = null
            var bestD = Float.MAX_VALUE
            for ((c, k) in keyCenters) {
                val dx = p.x - k.first
                val dy = p.y - k.second
                val d = dx * dx + dy * dy
                if (d < bestD) { bestD = d; bestC = c }
            }
            if (bestC != null && bestD <= MAX_HIT_DIST2) hits.add(bestC)
        }
        if (hits.isEmpty()) return emptyList()

        val seq = ArrayList<Char>(hits.size)
        for (c in hits) if (seq.isEmpty() || seq.last() != c) seq.add(c)
        if (seq.size < 3 || seq.first() == seq.last()) return emptyList()

        val firstNorm = TextNormalizerCompat(seq.first(), lang)
        val lastNorm = TextNormalizerCompat(seq.last(), lang)
        if (firstNorm.isEmpty() || lastNorm.isEmpty()) return emptyList()

        val candidates = dict.wordsStartingWith(firstNorm[0], 300)
        val out = ArrayList<SwipeCandidate>()

        for (entry in candidates) {
            val w = TextNormalizerCompat(entry.word, lang)
            if (w.length < 3 || w.length > 14) continue
            if (w.last() != lastNorm[0]) continue

            val centers = ArrayList<Pair<Float, Float>>(w.length)
            var missing = false
            for (c in w) {
                val k = keyCenters[c]
                if (k == null) { missing = true; break }
                centers.add(k)
            }
            if (missing) continue

            var sum = 0f
            for ((i, p) in pts.withIndex()) {
                val idx = (i * (w.length - 1)) / (pts.size - 1)
                val k = centers[idx]
                val dx = p.x - k.first
                val dy = p.y - k.second
                sum += sqrt(dx * dx + dy * dy)
            }
            val avg = sum / pts.size
            val freqBonus = (Math.log(entry.freq.toDouble()) * 6.0).toFloat()
            out.add(SwipeCandidate(entry.word, avg - freqBonus))
        }

        out.sortBy { it.score }
        return out.take(limit)
    }

    private fun TextNormalizerCompat(word: String, lang: LanguageMode): String =
        com.niktype.prediction.text.TextNormalizer.normalize(word, lang)

    private fun downsample(path: List<TouchPoint>, target: Int): List<TouchPoint> {
        if (path.size <= target) return path
        val out = ArrayList<TouchPoint>(target)
        val step = (path.size - 1).toDouble() / (target - 1)
        for (i in 0 until target) {
            out.add(path[(i * step).toInt()])
        }
        return out
    }

    companion object {
        private const val MAX_POINTS = 14
        private const val MAX_HIT_DIST2 = 2500f // حدود ۵۰ پیکسل
    }
}
