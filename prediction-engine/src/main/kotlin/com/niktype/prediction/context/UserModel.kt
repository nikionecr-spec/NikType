package com.niktype.prediction.context

import com.niktype.prediction.dictionary.WordEntry
import com.niktype.prediction.text.TextNormalizer

/**
 * مدل کاربر: بسامد واژه‌های تایپ‌شده (واژه‌نامه شخصی محلی).
 * ظرفیت محدود با جابجایی LRU تا مصرف حافظه کنترل شود.
 */
class UserModel(private val cap: Int = 25000) {

    private class Entry(val display: String, var count: Int)

    private val words = LinkedHashMap<String, Entry>(256, 0.75f, true)

    fun observe(raw: String, count: Int = 1) {
        val norm = TextNormalizer.normalize(raw, LanguageOfRaw(raw))
        if (norm.isEmpty() || norm.length < 2) return
        if (raw.any { it.isDigit() }) return
        if (raw.contains('/') || raw.contains('.') && raw.contains("http")) return
        val e = words[norm]
        if (e != null) {
            e.count += count
        } else {
            words[norm] = Entry(raw, count)
            if (words.size > cap) {
                val eldest = words.entries.iterator()
                if (eldest.hasNext()) { eldest.next(); eldest.remove() }
            }
        }
    }

    private fun LanguageOfRaw(raw: String): com.niktype.engine.geometry.LanguageMode =
        if (raw.any { it in 'a'..'z' || it in 'A'..'Z' })
            com.niktype.engine.geometry.LanguageMode.EN
        else com.niktype.engine.geometry.LanguageMode.FA

    fun freq(rawOrNorm: String): Int = words[TextNormalizer.normalize(rawOrNorm, LanguageOfRaw(rawOrNorm))]?.count ?: 0

    fun prefix(rawPrefix: String, lang: com.niktype.engine.geometry.LanguageMode, limit: Int = 20): List<WordEntry> {
        val norm = TextNormalizer.normalize(rawPrefix, lang)
        if (norm.isEmpty()) return emptyList()
        val out = ArrayList<WordEntry>()
        for ((k, e) in words) {
            if (k.startsWith(norm)) out.add(WordEntry(e.display, e.count, user = true))
        }
        out.sortByDescending { it.freq }
        return out.take(limit)
    }

    fun displayFor(raw: String, lang: com.niktype.engine.geometry.LanguageMode): String? =
        words[TextNormalizer.normalize(raw, lang)]?.display

    fun contains(raw: String, lang: com.niktype.engine.geometry.LanguageMode): Boolean =
        words.containsKey(TextNormalizer.normalize(raw, lang))

    fun size(): Int = words.size

    fun toLines(): List<String> = words.map { (k, e) -> "$k\t${e.display}\t${e.count}" }

    fun restore(lines: List<String>) {
        for (line in lines) {
            val parts = line.split('\t')
            if (parts.size == 3) {
                val count = parts[2].toIntOrNull() ?: continue
                words[parts[0]] = Entry(parts[1], count)
            }
        }
    }
}
