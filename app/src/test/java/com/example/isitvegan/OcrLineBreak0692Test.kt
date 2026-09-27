package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrLineBreak0692Test {
    @Test fun reviewedIngredientExpressionsAreRejoined() {
        assertEquals("huile de colza", preprocessed("huile de\ncolza"))
        assertEquals("farine de blé", preprocessed("farine de\nblé"))
        assertEquals("sirop de glucose-fructose", preprocessed("sirop de glucose-\nfructose"))
        assertEquals("huile de tournesol", preprocessed("huile de\ntournesol"))
        assertEquals("acide citrique", preprocessed("acide\ncitrique"))
    }

    @Test fun commaAndDistinctLinesRemainSeparate() {
        assertEquals("sucre,\nhuile de colza", LabelPreprocessor.preprocess("sucre,\nhuile de\ncolza").compositionText)
        assertEquals("eau\nwater", LabelPreprocessor.preprocess("eau\nwater").compositionText)
    }

    @Test fun continuedParenthesisKeepsItsStructure() {
        assertEquals(
            "huile de colza (eau, sel)",
            preprocessed("huile de\ncolza (eau, sel)")
        )
    }

    @Test fun traceSectionStaysOutsideComposition() {
        val result = LabelPreprocessor.preprocess("huile de\ncolza. Peut contenir : lait")
        assertEquals("huile de colza. ", result.compositionText)
        assertTrue(result.crossContactWarnings.any { it.contains("lait", ignoreCase = true) })
        assertFalse(result.compositionText.contains("lait", ignoreCase = true))
    }

    @Test fun cleanerPreservesTheGlucoseFructoseHyphenation() {
        assertEquals("sirop de glucose-fructose", OcrTextCleaner.clean("sirop de glucose-\nfructose"))
    }

    private fun preprocessed(value: String): String =
        LabelPreprocessor.preprocess(value).compositionText
}
