package com.niktype.prediction.context

/**
 * پورت ذخیره‌سازی یادگیری. لایه اپلیکیشن (اندروید) پیاده‌سازی فایل‌محور
 * را فراهم می‌کند؛ هیچ داده‌ای از دستگاه خارج نمی‌شود.
 */
interface LearningStore {
    fun load(): PersistedModel?
    fun save(model: PersistedModel)
}

data class PersistedModel(
    val userWords: List<String>,
    val bigramsFa: List<String>,
    val bigramsEn: List<String>,
)
