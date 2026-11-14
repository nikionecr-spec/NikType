package com.niktype.prediction.context

/**
 * مدل بی‌گرام: احتمال واژه بعدی بر اساس واژه فعلی.
 * هم با داده‌های بذر (seed) پر می‌شود و هم از تایپ کاربر یاد می‌گیرد.
 * داده‌ها فقط روی دستگاه ذخیره می‌شوند.
 */
class BigramModel(private val cap: Int = 20000) {

    private val map = HashMap<String, MutableMap<String, Int>>(1024)

    fun observe(w1: String, w2: String, count: Int = 1) {
        if (w1.isEmpty() || w2.isEmpty() || w1 == " " || w2 == " ") return
        val m = map.getOrPut(w1) { HashMap() }
        m[w2] = (m[w2] ?: 0) + count
        if (map.size > cap) evict()
    }

    fun seed(entries: List<Triple<String, String, Int>>) {
        for (e in entries) observe(e.first, e.second, e.third)
    }

    fun count(w1: String, w2: String): Int = map[w1]?.get(w2) ?: 0

    fun nextCounts(w1: String): Map<String, Int> = map[w1] ?: emptyMap()

    fun size(): Int = map.size

    private fun evict() {
        val target = (cap * 0.8f).toInt()
        // حذف زوج‌های کم‌شمارش تا رسیدن به ظرفیت هدف
        val entries = map.entries.sortedBy { (_, m) -> m.values.sum() }
        var removed = 0
        for (e in entries) {
            if (map.size - removed <= target) break
            map.remove(e.key)
            removed++
        }
    }

    fun toLines(): List<String> {
        val out = ArrayList<String>(map.size)
        for ((w1, m) in map) {
            for ((w2, c) in m) out.add("$w1 $w2\t$c")
        }
        return out
    }

    fun restore(lines: List<String>) {
        for (line in lines) {
            val idx = line.lastIndexOf('\t')
            if (idx <= 0) continue
            val c = line.substring(idx + 1).toIntOrNull() ?: continue
            val pair = line.substring(0, idx)
            val sp = pair.indexOf(' ')
            if (sp <= 0) continue
            observe(pair.substring(0, sp), pair.substring(sp + 1), c)
        }
    }
}
