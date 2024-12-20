package org.technoserve.farmcollector.database.converters



import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull

import org.junit.Test
class AccuracyListConvertTest {
    private val converter = AccuracyListConvert()

    @Test
    fun `fromAccuracyList with null input returns null`() {
        val result = converter.fromAccuracyList(null)
        assertNull(result)
    }

    @Test
    fun `fromAccuracyList with empty list returns empty brackets`() {
        val result = converter.fromAccuracyList(emptyList())
        assertEquals("[]", result)
    }

    @Test
    fun `fromAccuracyList with list containing only nulls`() {
        val input = listOf<Float?>(null, null, null)
        val result = converter.fromAccuracyList(input)
        assertEquals("[null,null,null]", result)
    }

    @Test
    fun `fromAccuracyList with list containing mixed values and nulls`() {
        val input = listOf<Float?>(1.5f, null, 2.7f, null)
        val result = converter.fromAccuracyList(input)
        assertEquals("[1.5,null,2.7,null]", result)
    }

    @Test
    fun `fromAccuracyList with list containing only float values`() {
        val input = listOf<Float?>(1.5f, 2.7f, 3.0f)
        val result = converter.fromAccuracyList(input)
        assertEquals("[1.5,2.7,3.0]", result)
    }

    @Test
    fun `toAccuracyList with null input returns null`() {
        val result = converter.toAccuracyList(null)
        assertNull(result)
    }

    @Test
    fun `toAccuracyList with empty brackets returns empty list`() {
        val result = converter.toAccuracyList("[]")
        println(result)  // Debugging output
        assertEquals(emptyList<Float?>(), result)
    }

    @Test
    fun `toAccuracyList with list containing only nulls`() {
        val result = converter.toAccuracyList("[null,null,null]")
        assertEquals(listOf<Float?>(null, null, null), result)
    }

    @Test
    fun `toAccuracyList with list containing mixed values and nulls`() {
        val result = converter.toAccuracyList("[1.5,null,2.7,null]")
        assertEquals(listOf<Float?>(1.5f, null, 2.7f, null), result)
    }

    @Test
    fun `toAccuracyList with list containing only float values`() {
        val result = converter.toAccuracyList("[1.5,2.7,3.0]")
        assertEquals(listOf<Float?>(1.5f, 2.7f, 3.0f), result)
    }

    @Test
    fun `toAccuracyList with whitespace in string`() {
        val result = converter.toAccuracyList("[ 1.5, null , 2.7 , null ]")
        assertEquals(listOf<Float?>(1.5f, null, 2.7f, null), result)
    }

    @Test
    fun `toAccuracyList with invalid float values returns nulls`() {
        val result = converter.toAccuracyList("[1.5,invalid,2.7,xyz]")
        assertEquals(listOf<Float?>(1.5f, null, 2.7f, null), result)
    }
}