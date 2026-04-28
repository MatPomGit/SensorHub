package com.kia.sensorhub.utils

import com.kia.sensorhub.utils.HapticFeedbackHelper.HapticFeedbackType
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Testy jednostkowe dla mapowania haptyki i logiki warunkowej helpera.
 */
class HapticFeedbackHelperTest {

    @Test
    fun `mapHapticType maps click to short single pulse`() {
        // Sprawdzamy, czy kliknięcie mapuje się na krótki pojedynczy impuls.
        val config = HapticFeedbackHelper.mapHapticType(HapticFeedbackType.CLICK)

        assertContentEquals(longArrayOf(0, 20), config.timings)
        assertContentEquals(intArrayOf(0, 120), config.amplitudes)
        assertTrue(config.legacyDurationMs > 0)
    }

    @Test
    fun `mapHapticType maps error to strongest multi pulse pattern`() {
        // Wzorzec błędu powinien być dłuższy i bardziej intensywny niż prosty klik.
        val config = HapticFeedbackHelper.mapHapticType(HapticFeedbackType.ERROR)

        assertContentEquals(longArrayOf(0, 35, 30, 35, 30, 60), config.timings)
        assertContentEquals(intArrayOf(0, 220, 0, 220, 0, 180), config.amplitudes)
        assertTrue(config.legacyDurationMs >= 150)
    }

    @Test
    fun `canProvideHapticFeedback returns true only when both conditions are met`() {
        // Haptyka jest dozwolona wyłącznie, gdy accessibility i uprawnienie są aktywne jednocześnie.
        assertTrue(HapticFeedbackHelper.canProvideHapticFeedback(true, true))
        assertFalse(HapticFeedbackHelper.canProvideHapticFeedback(true, false))
        assertFalse(HapticFeedbackHelper.canProvideHapticFeedback(false, true))
        assertFalse(HapticFeedbackHelper.canProvideHapticFeedback(false, false))
    }
}
