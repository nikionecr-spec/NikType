package com.niktype.engine.touch

/**
 * داده‌ها و پروفایل‌های لمس.
 *
 * TouchProfile مشخص می‌کند موتور تشخیص لمس چقدر «سخت‌گیر» باشد:
 * - sigmaX/sigmaY: پراکندگی گاوسی لمس نسبت به مرکز کلید (نسبت به اندازه کلید)
 * - neighborWeight: میزان تأثیر کلیدهای مجاور هنگام لمس نزدیک مرز مشترک
 * - insideBonus: ضریب تقویت برای کلیدی که لمس داخل آن است
 * - speedAdaptMax: با تایپ سریع‌تر، تلورانس لمس بیشتر می‌شود
 */
data class TouchProfile(
    val sigmaXFactor: Float = 0.34f,
    val sigmaYFactor: Float = 0.30f,
    val neighborWeight: Float = 0.30f,
    val insideBonus: Float = 1.5f,
    val edgeRejectPx: Float = 3f,
    val speedAdaptMax: Float = 1.6f,
    val longPressMs: Long = 380L,
)

enum class FingerProfileKind(val displayName: String) {
    SMALL("Small Fingers"),
    NORMAL("Normal Fingers"),
    LARGE("Large Fingers"),
}

object FingerProfiles {

    /** انگشت‌های کوچک: تشخیص دقیق، تلورانس کم، اولویت با کلید تحت لمس. */
    val SMALL_PROFILE: TouchProfile = TouchProfile(
        sigmaXFactor = 0.30f, sigmaYFactor = 0.26f,
        neighborWeight = 0.15f, insideBonus = 1.6f,
        edgeRejectPx = 2f, speedAdaptMax = 1.5f, longPressMs = 400L,
    )

    /** حالت عادی: تعادل بین دقت و بخشندگی. */
    val NORMAL_PROFILE: TouchProfile = TouchProfile(
        sigmaXFactor = 0.34f, sigmaYFactor = 0.30f,
        neighborWeight = 0.30f, insideBonus = 1.5f,
        edgeRejectPx = 3f, speedAdaptMax = 1.6f, longPressMs = 380L,
    )

    /**
     * انگشت‌های درشت: حساسیت بیشتر روی کلیدهای مجاور؛ لمس‌های نزدیک مرز
     * کلیدها با تلورانس بالاتر پذیرفته می‌شوند و توزیع احتمال بین مجاورها
     * واقعی‌تر است تا خطای انگشت درشت کمتر شود.
     */
    val LARGE_PROFILE: TouchProfile = TouchProfile(
        sigmaXFactor = 0.44f, sigmaYFactor = 0.38f,
        neighborWeight = 0.55f, insideBonus = 1.4f,
        edgeRejectPx = 4f, speedAdaptMax = 1.7f, longPressMs = 350L,
    )

    fun profile(kind: FingerProfileKind): TouchProfile = when (kind) {
        FingerProfileKind.SMALL -> SMALL_PROFILE
        FingerProfileKind.NORMAL -> NORMAL_PROFILE
        FingerProfileKind.LARGE -> LARGE_PROFILE
    }
}

data class TouchPoint(
    val x: Float,
    val y: Float,
    val timeMs: Long,
    val pointerId: Long = 0L,
)

data class ScoredKey(
    val rect: com.niktype.engine.geometry.KeyRect,
    val probability: Float,
)
