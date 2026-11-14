package com.niktype.keyboard.voice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

/**
 * مسیر تحویل نتیجه تایپ صوتی از اکتیویتی شفاف به سرویس IME.
 */
object VoiceResultBus {

    private val result = MutableStateFlow<String?>(null)

    fun post(text: String) {
        result.value = text
    }

    fun reset() {
        result.value = null
    }

    suspend fun receive(): String? {
        val r = result.filterNotNull().first()
        result.value = null
        return r
    }
}
