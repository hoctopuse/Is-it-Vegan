package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Version064LanguageTest {
    @Test fun interfaceLanguageDefinesQualityThenPreference() {
        val text = "FR: ingrédients eau-\nEN: Ingredients: water, sugar, salt"
        assertEquals(LabelLanguage.ENGLISH, LabelLanguageSegmenter.segment(text, UiLanguage.FR).selectedLanguage)
        assertEquals(LabelLanguage.DUTCH, LabelLanguageSegmenter.segment("NL: Ingrediënten: water, zout\nEN: Ingredients: water", UiLanguage.NL).selectedLanguage)
        assertEquals(LabelLanguage.ENGLISH, LabelLanguageSegmenter.segment("FR: ingrédients eau\nEN: Ingredients: water, sugar", UiLanguage.EN).selectedLanguage)
    }

    @Test fun sameLineIngredientHeadingsAreSeparateSections() {
        val result = LabelLanguageSegmenter.segment("Ingredients: water, sugar. Ingrédients: eau, sucre. Zutaten: Wasser", UiLanguage.FR)
        assertEquals(LabelLanguage.FRENCH, result.selectedLanguage)
        assertTrue(result.blocks.size >= 3)
        assertFalse(result.blocks.first().rawText.contains("Ingrédients"))
    }

    @Test fun ordinaryFrenchDeIsNotGermanMarker() {
        val result = LabelLanguageSegmenter.segment("prendre en de bonnes conditions, fruit belge")
        assertTrue(result.usedFallback)
        assertEquals(LabelLanguage.UNKNOWN, result.selectedLanguage)
    }
}
