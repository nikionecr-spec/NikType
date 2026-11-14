package com.niktype.engine.touch

/**
 * کالیبراسیون تطبیقی هر کلید: میانگین متحرک انحراف لمس واقعی کاربر از
 * مرکز کلید. اگر کاربر همیشه کمی بالای کلید را لمس می‌کند، موتور خودش
 * این جابه‌جایی را یاد می‌گیرد و جبران می‌کند.
 */
class KeyCalibration(
    private val offsets: MutableMap<String, FloatArray> = mutableMapOf(),
) {
    private var dirty = false

    fun offsetX(keyId: String): Float = offsets[keyId]?.get(0) ?: 0f
    fun offsetY(keyId: String): Float = offsets[keyId]?.get(1) ?: 0f

    /**
     * یادگیری از یک لمس مطمئن (احتمال بالا و داخل ناحیه مرکزی کلید).
     * جابه‌جایی در محدوده ۳۰٪ ابعاد کلید محدود می‌شود.
     */
    fun learn(keyId: String, dx: Float, dy: Float, keyWidth: Float, keyHeight: Float) {
        val maxDx = keyWidth * 0.3f
        val maxDy = keyHeight * 0.3f
        val cx = dx.coerceIn(-maxDx, maxDx)
        val cy = dy.coerceIn(-maxDy, maxDy)
        val cur = offsets[keyId]
        if (cur == null) {
            offsets[keyId] = floatArrayOf(cx, cy)
        } else {
            cur[0] = cur[0] * (1f - LEARN_RATE) + cx * LEARN_RATE
            cur[1] = cur[1] * (1f - LEARN_RATE) + cy * LEARN_RATE
        }
        dirty = true
    }

    fun isDirty(): Boolean = dirty
    fun clearDirty() { dirty = false }

    fun size(): Int = offsets.size

    fun toLines(): List<String> = offsets.map { (id, v) -> "$id ${v[0]} ${v[1]}" }

    fun restore(lines: List<String>) {
        for (line in lines) {
            val parts = line.trim().split(" ")
            if (parts.size == 3) {
                val dx = parts[1].toFloatOrNull() ?: continue
                val dy = parts[2].toFloatOrNull() ?: continue
                offsets[parts[0]] = floatArrayOf(dx, dy)
            }
        }
        dirty = false
    }

    companion object {
        private const val LEARN_RATE = 0.06f
    }
}
