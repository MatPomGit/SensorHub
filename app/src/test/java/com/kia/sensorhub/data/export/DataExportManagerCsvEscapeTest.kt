package com.kia.sensorhub.data.export

import org.junit.Assert.assertEquals
import org.junit.Test

class DataExportManagerCsvEscapeTest {

    /**
     * Sprawdza poprawne escapowanie pola zawierającego przecinek.
     */
    @Test
    fun `should wrap value containing comma in quotes`() {
        val escapedValue = escapeCsvField("left,right")

        assertEquals("\"left,right\"", escapedValue)
    }

    /**
     * Sprawdza poprawne podwajanie cudzysłowów i otaczanie pola cudzysłowami.
     */
    @Test
    fun `should escape value containing quote`() {
        val escapedValue = escapeCsvField("He said \"hello\"")

        assertEquals("\"He said \"\"hello\"\"\"", escapedValue)
    }

    /**
     * Sprawdza poprawne escapowanie pola wieloliniowego.
     */
    @Test
    fun `should wrap multiline value in quotes`() {
        val escapedValue = escapeCsvField("line1\nline2")

        assertEquals("\"line1\nline2\"", escapedValue)
    }

    /**
     * Sprawdza ochronę przed formułami arkusza przez dodanie apostrofu.
     */
    @Test
    fun `should prepend apostrophe for spreadsheet formula`() {
        val escapedValue = escapeCsvField("=SUM(A1:A2)")

        assertEquals("'=SUM(A1:A2)", escapedValue)
    }
}
