package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Test

class OcrTextCleanerInstrumentedTest {
    @Test fun uppercaseUnicodeHyphenationUsesAndroidRegexEngine() {
        assertEquals("INGRÉ-DIENTS", OcrTextCleaner.clean("INGRÉ-\nDIENTS"))
        assertEquals("PETIT-LAIT", OcrTextCleaner.clean("PETIT-\nLAIT"))
        assertEquals("MATIÈRES-GRASSES", OcrTextCleaner.clean("MATIÈRES-\nGRASSES"))
        assertEquals("INGRÉ\nDIENTS", OcrTextCleaner.clean("INGRÉ\nDIENTS"))
    }
}
