package com.niktype.engine.touch

import com.niktype.engine.geometry.KeyRect
import com.niktype.engine.geometry.KeyboardLayout
import com.niktype.engine.geometry.KeyType
import kotlin.math.abs
import kotlin.math.exp

/**
 * موتور تشخیص کلید بر اساس مختصات لمس.
 *
 * الگوریتم:
 * 1) برای هر کلید، احتمال پایه با مدل گاوسیِ فاصله لمس از مرکز کلید
 *    محاسبه می‌شود (با در نظر گرفتن اندازه/شکل کلید و کالیبراسیون کاربر).
 * 2) اگر لمس داخل مستطیل کلید باشد احتمال تقویت می‌شود.
 * 3) کلیدهای مجاورِ کلید تحت لمس، نزدیک مرز مشترک، تقویت می‌شوند —
 *    این همان «حساسیت روی کلیدهای مجاور» است که در حالت انگشت درشت
 *    وزن بالاتری می‌گیرد.
 * 4) با تایپ سریع، تلورانس (سیگما) افزایش می‌یابد تا لمس‌های شتاب‌زده
 *    درست تفسیر شوند.
 * 5) خروجی، فهرست کلیدهای مرتب‌شده با احتمال نرمال‌شده است؛ رده‌های
 *    بالاتر به لایه پیش‌بینی می‌روند تا در سطح واژه رفع ابهام شود.
 */
class TouchResolver(
    var profile: TouchProfile,
    private val calibration: KeyCalibration,
) {

    fun resolve(
        x: Float,
        y: Float,
        layout: KeyboardLayout,
        speedCps: Float = 0f,
        maxCandidates: Int = 4,
    ): List<ScoredKey> {
        val m = profile.edgeRejectPx
        if (x < -m || y < -m || x > layout.width + m || y > layout.height + m) return emptyList()

        val charKeys = layout.keys.filter { it.key.type == KeyType.CHAR }
        val avgW = if (charKeys.isEmpty()) layout.width / 10f
        else charKeys.sumOf { it.width.toDouble() }.toFloat() / charKeys.size
        val avgH = if (charKeys.isEmpty()) layout.height / 4f
        else charKeys.sumOf { it.height.toDouble() }.toFloat() / charKeys.size

        val speedNorm = speedCps.coerceIn(0f, 8f) / 8f
        val scale = 1f + speedNorm * (profile.speedAdaptMax - 1f)
        val sx = (avgW * profile.sigmaXFactor * scale).coerceAtLeast(1f)
        val sy = (avgH * profile.sigmaYFactor * scale).coerceAtLeast(1f)

        val n = layout.keys.size
        val scores = FloatArray(n)
        for (i in 0 until n) {
            val k = layout.keys[i]
            val dx = x - (k.centerX + calibration.offsetX(k.key.id))
            val dy = y - (k.centerY + calibration.offsetY(k.key.id))
            val nx = dx / sx
            val ny = dy / sy
            var p = exp(-0.5f * (nx * nx + ny * ny))
            if (k.contains(x, y)) p *= profile.insideBonus
            scores[i] = p
        }

        val containing = layout.keys.indexOfFirst { it.contains(x, y) }
        if (containing >= 0) {
            val c = layout.keys[containing]
            for (i in 0 until n) {
                if (i == containing) continue
                val other = layout.keys[i]
                val edgeDist = sharedEdgeDistance(c, other, x, y)
                if (edgeDist != null && edgeDist <= NEIGHBOR_BAND_PX) {
                    val rel = 1f - edgeDist / NEIGHBOR_BAND_PX
                    scores[i] *= 1f + profile.neighborWeight * rel
                }
            }
        }

        var sum = 0f
        for (s in scores) sum += s
        if (sum <= 0f) return emptyList()

        val result = ArrayList<ScoredKey>(n)
        for (i in 0 until n) {
            val p = scores[i] / sum
            if (p >= 0.01f) result.add(ScoredKey(layout.keys[i], p))
        }
        result.sortByDescending { it.probability }
        return if (result.size > maxCandidates) result.subList(0, maxCandidates).toList() else result
    }

    /** کلیدی که لمس داخل آن است (بدون کالیبراسیون، برای لایه رابط کاربری). */
    fun insideKey(x: Float, y: Float, layout: KeyboardLayout): KeyRect? =
        layout.keys.firstOrNull { it.contains(x, y) }

    /** نزدیک‌ترین کلید از نظر مرکز (برای سوایپ و لبه‌های صفحه). */
    fun nearestKey(x: Float, y: Float, layout: KeyboardLayout): KeyRect? {
        var best: KeyRect? = null
        var bestD = Float.MAX_VALUE
        for (k in layout.keys) {
            val dx = x - k.centerX
            val dy = y - k.centerY
            val d = dx * dx + dy * dy
            if (d < bestD) { bestD = d; best = k }
        }
        return best
    }

    /**
     * فاصله لمس از مرز مشترک دو کلید مجاور (افقی یا عمودی).
     * اگر مجاور نباشند null برمی‌گردد.
     */
    private fun sharedEdgeDistance(a: KeyRect, b: KeyRect, x: Float, y: Float): Float? {
        val sameRowVOverlap = abs(a.centerY - b.centerY) < (a.height + b.height) * 0.45f
        val sameColHOverlap = abs(a.centerX - b.centerX) < (a.width + b.width) * 0.45f

        // مجاورت افقی: b چپ یا راست a
        if (sameRowVOverlap && abs(a.centerX - b.centerX) <= (a.width + b.width) / 2f + 12f) {
            val edgeX = if (b.centerX > a.centerX) (a.right + b.x) / 2f else (b.right + a.x) / 2f
            return abs(x - edgeX)
        }
        // مجاورت عمودی: b بالا یا پایین a
        if (sameColHOverlap && abs(a.centerY - b.centerY) <= (a.height + b.height) / 2f + 12f) {
            val edgeY = if (b.centerY > a.centerY) (a.bottom + b.y) / 2f else (b.bottom + a.y) / 2f
            return abs(y - edgeY)
        }
        return null
    }

    companion object {
        private const val NEIGHBOR_BAND_PX = 10f
    }
}
