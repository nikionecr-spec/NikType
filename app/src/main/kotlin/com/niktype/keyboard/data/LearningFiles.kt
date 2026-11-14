package com.niktype.keyboard.data

import android.content.Context
import com.niktype.prediction.context.LearningStore
import com.niktype.prediction.context.PersistedModel
import org.json.JSONArray
import java.io.File

/**
 * ذخیره‌سازی محلی یادگیری — فقط روی دستگاه، بدون هیچ خروجی.
 * سه فایل ساده متنی: واژه‌های کاربر، بی‌گرام فارسی، بی‌گرام انگلیسی.
 */
class FileLearningStore(context: Context) : LearningStore {

    private val dir = File(context.filesDir, "learning").apply { mkdirs() }
    private val wordsFile = File(dir, "user_words.txt")
    private val faFile = File(dir, "bigrams_fa.txt")
    private val enFile = File(dir, "bigrams_en.txt")

    override fun load(): PersistedModel? {
        if (!wordsFile.exists() && !faFile.exists() && !enFile.exists()) return null
        return runCatching {
            PersistedModel(
                userWords = readLines(wordsFile),
                bigramsFa = readLines(faFile),
                bigramsEn = readLines(enFile),
            )
        }.getOrNull()
    }

    override fun save(model: PersistedModel) {
        runCatching {
            writeLines(wordsFile, model.userWords)
            writeLines(faFile, model.bigramsFa)
            writeLines(enFile, model.bigramsEn)
        }
    }

    private fun readLines(f: File): List<String> =
        if (f.exists()) f.readLines(Charsets.UTF_8).filter { it.isNotBlank() } else emptyList()

    private fun writeLines(f: File, lines: List<String>) {
        val tmp = File(f.parentFile, f.name + ".tmp")
        tmp.writeText(lines.joinToString("\n"), Charsets.UTF_8)
        if (!tmp.renameTo(f)) {
            f.writeText(lines.joinToString("\n"), Charsets.UTF_8)
            tmp.delete()
        }
    }
}

/** ذخیره کالیبراسیون لمس هر کلید (offset یادگرفته‌شده). */
class CalibrationFileStore(context: Context) {

    private val file = File(File(context.filesDir, "learning").apply { mkdirs() }, "calibration.txt")

    fun load(): List<String> =
        if (file.exists()) file.readLines(Charsets.UTF_8).filter { it.isNotBlank() } else emptyList()

    fun save(lines: List<String>) {
        runCatching {
            val tmp = File(file.parentFile, file.name + ".tmp")
            tmp.writeText(lines.joinToString("\n"), Charsets.UTF_8)
            if (!tmp.renameTo(file)) {
                file.writeText(lines.joinToString("\n"), Charsets.UTF_8)
                tmp.delete()
            }
        }
    }
}

/** میانبرهای متنی کاربر: کلید کوتاه → متن کامل. */
class ShortcutsStore(context: Context) {

    private val file = File(File(context.filesDir, "learning").apply { mkdirs() }, "shortcuts.json")

    data class Shortcut(val key: String, val value: String)

    fun load(): List<Shortcut> {
        if (!file.exists()) return emptyList()
        return runCatching {
            val arr = JSONArray(file.readText(Charsets.UTF_8))
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.getJSONObject(i)
                val k = o.optString("key")
                val v = o.optString("value")
                if (k.isNotBlank() && v.isNotBlank()) Shortcut(k, v) else null
            }
        }.getOrDefault(emptyList())
    }

    fun save(shortcuts: List<Shortcut>) {
        runCatching {
            val arr = JSONArray()
            for (s in shortcuts) {
                val o = org.json.JSONObject()
                o.put("key", s.key)
                o.put("value", s.value)
                arr.put(o)
            }
            file.writeText(arr.toString(), Charsets.UTF_8)
        }
    }
}
