package com.niktype.engine

import com.niktype.engine.touch.FingerProfileKind
import com.niktype.engine.touch.FingerProfiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FingerProfileTest {

    @Test
    fun sigmaIncreasesWithFingerSize() {
        assertTrue(FingerProfiles.SMALL_PROFILE.sigmaXFactor < FingerProfiles.NORMAL_PROFILE.sigmaXFactor)
        assertTrue(FingerProfiles.NORMAL_PROFILE.sigmaXFactor < FingerProfiles.LARGE_PROFILE.sigmaXFactor)
        assertTrue(FingerProfiles.SMALL_PROFILE.sigmaYFactor < FingerProfiles.LARGE_PROFILE.sigmaYFactor)
    }

    @Test
    fun neighborWeightIncreasesWithFingerSize() {
        // در حالت انگشت درشت، حساسیت روی کلیدهای مجاور باید بیشتر باشد
        assertTrue(FingerProfiles.SMALL_PROFILE.neighborWeight < FingerProfiles.LARGE_PROFILE.neighborWeight)
    }

    @Test
    fun profileMapping() {
        assertEquals(FingerProfiles.SMALL_PROFILE, FingerProfiles.profile(FingerProfileKind.SMALL))
        assertEquals(FingerProfiles.NORMAL_PROFILE, FingerProfiles.profile(FingerProfileKind.NORMAL))
        assertEquals(FingerProfiles.LARGE_PROFILE, FingerProfiles.profile(FingerProfileKind.LARGE))
    }

    @Test
    fun largeProfileHasShorterLongPress() {
        assertTrue(FingerProfiles.LARGE_PROFILE.longPressMs < FingerProfiles.SMALL_PROFILE.longPressMs)
    }
}
