package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrTextCleanerTest {
    @Test fun protectedFoodTokensStayExact() {
        val text = "Ingrédients; eau 11,9 %, E471, PETIT-LAIT (soja (huile, sel)) *"
        val cleaned = OcrTextCleaner.clean(text)

        listOf("11,9 %", "E471", "PETIT-LAIT", "(soja (huile, sel))", "*")
            .forEach { token -> assertTrue("$token dans $cleaned", cleaned.contains(token)) }
    }

    @Test fun cleaningOnlyAppliesDeterministicTypographyAndExplicitHyphenation() {
        assertEquals(
            "ingredients : eau, sucre\n\npeut contenir : lait\nPETIT-LAIT inconu 1,9 %",
            OcrTextCleaner.clean("ingredients;   eau,sucre\n\npeut contenir. lait\nPETIT-\nLAIT inconu 1,9 %")
        )
    }

    @Test fun unknownWordsAndNumbersAreNeverInvented() {
        val source = "xylophz 1,9 % E47I"
        assertEquals(source, OcrTextCleaner.clean(source))
    }

    @Test fun uppercaseUnicodeHyphenationIsAndroidCompatible() {
        assertEquals("INGRÉ-DIENTS", OcrTextCleaner.clean("INGRÉ-\nDIENTS"))
        assertEquals("PETIT-LAIT", OcrTextCleaner.clean("PETIT-\nLAIT"))
        assertEquals("MATIÈRES-GRASSES", OcrTextCleaner.clean("MATIÈRES-\nGRASSES"))
        assertEquals("INGRÉ\nDIENTS", OcrTextCleaner.clean("INGRÉ\nDIENTS"))
    }

    @Test fun commonFrenchIngredientHeadingOcrErrorIsCorrectedOnlyAsHeading() {
        assertEquals(
            "ingrédients : sucre, lait",
            OcrTextCleaner.clean("ingrédlents: sucre, lait")
        )
        assertEquals(
            "texte ingrédlents sans séparateur",
            OcrTextCleaner.clean("texte ingrédlents sans séparateur")
        )
    }
}
