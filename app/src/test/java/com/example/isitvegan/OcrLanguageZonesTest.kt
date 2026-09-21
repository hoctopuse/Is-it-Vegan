package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrLanguageZonesTest {
    @Test fun frenchMarketHeaderIsOneFrenchBlock() {
        val result = LabelLanguageSegmenter.segment("FR BE CH — Ingrédients : eau, sucre")

        assertEquals(listOf(LabelLanguage.FRENCH), result.blocks.map { it.language })
        assertEquals(setOf("BE", "CH"), result.blocks.single().marketTags)
        assertFalse(result.usedFallback)
    }

    @Test fun scandinavianHeaderIsOneSwedishZoneWithMarketHints() {
        val result = LabelLanguageSegmenter.segment("SE DK NO — Ingredienser: vatten, socker")

        assertEquals(listOf(LabelLanguage.SWEDISH), result.blocks.map { it.language })
        assertEquals(setOf("DK", "NO"), result.blocks.single().marketTags)
    }

    @Test fun frenchAndDutchZonesRemainSeparate() {
        val result = LabelLanguageSegmenter.segment(
            "FR — Ingrédients : eau, sucre\nNL — Ingrediënten: water, suiker"
        )

        assertEquals(listOf(LabelLanguage.FRENCH, LabelLanguage.DUTCH), result.blocks.map { it.language })
        assertFalse(result.blocks.first().rawText.contains("suiker"))
    }

    @Test fun contentWithoutReliableMarkerStaysUnknown() {
        val text = "Eau, sucre, xylophz"
        val result = LabelLanguageSegmenter.segment(text)

        assertEquals(LabelLanguage.UNKNOWN, result.blocks.single().language)
        assertEquals(text, result.blocks.single().rawText)
        assertTrue(result.usedFallback)
    }

    @Test fun countryCodesAloneDoNotBecomeLanguages() {
        val result = LabelLanguageSegmenter.segment("BE CH LUX — distribution")
        assertEquals(LabelLanguage.UNKNOWN, result.selectedLanguage)
    }

    @Test fun mixedFrenchGermanCountryHeaderIsOnlyAHint() {
        val result = LabelLanguageSegmenter.segment("FR DE CH — composition commune")
        assertEquals(LabelLanguage.UNKNOWN, result.selectedLanguage)
        assertEquals("FR DE CH — composition commune", result.selectedText)
    }
}
