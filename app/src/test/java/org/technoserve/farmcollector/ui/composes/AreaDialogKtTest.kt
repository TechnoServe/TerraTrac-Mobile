package org.technoserve.farmcollector.ui.composes

import org.junit.Assert.*
import org.junit.Test


fun calculateThreshold(enteredArea: Double): Double {
    return enteredArea * 0.30
}

fun shouldShowWarning(calculatedArea: Double, enteredArea: Double): Boolean {
    val threshold = calculateThreshold(enteredArea)
    return Math.abs(calculatedArea - enteredArea) > threshold
}

fun formatArea(area: Double, decimalPlaces: Int): String {
    return String.format("%.${decimalPlaces}f", area)
}


class AreaDialogKtTest{
    @Test
    fun `calculateThreshold returns 30 percent of entered area`() {
        val enteredArea = 100.0
        val expectedThreshold = 30.0

        val result = calculateThreshold(enteredArea)

        assertEquals(expectedThreshold, result, 0.0)
    }

    @Test
    fun `shouldShowWarning returns true when difference exceeds threshold`() {
        val calculatedArea = 150.0
        val enteredArea = 100.0

        val result = shouldShowWarning(calculatedArea, enteredArea)

        assertTrue(result)
    }

    @Test
    fun `shouldShowWarning returns false when difference is within threshold`() {
        val calculatedArea = 120.0
        val enteredArea = 100.0

        val result = shouldShowWarning(calculatedArea, enteredArea)

        assertFalse(result)
    }

    @Test
    fun `formatArea formats area to specified decimal places`() {
        val area = 123.456789

        val result = formatArea(area, 2)

        assertEquals("123.46", result)
    }
}