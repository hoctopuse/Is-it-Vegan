package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LabelLanguageSegmenterTest {
    @Test fun frenchBlockWinsOverDutchAndEnglish() {
        val segmentation = LabelLanguageSegmenter.segment(
            "FR: sucre, eau.\nNL: suiker, water.\nEN: sugar, water."
        )

        assertEquals(LabelLanguage.FRENCH, segmentation.selectedLanguage)
        assertEquals("FR", segmentation.detectedMarker)
        assertTrue(segmentation.selectedText.contains("sucre, eau"))
        assertFalse(segmentation.selectedText.contains("suiker"))
        assertFalse(segmentation.selectedText.contains("sugar"))
        assertEquals(listOf(LabelLanguage.DUTCH, LabelLanguage.ENGLISH), segmentation.ignoredLanguages)
    }

    @Test fun dutchBlockWinsWhenFrenchIsAbsent() {
        val segmentation = LabelLanguageSegmenter.segment(
            "NL: suiker, water. EN: sugar, water."
        )

        assertEquals(LabelLanguage.DUTCH, segmentation.selectedLanguage)
        assertTrue(segmentation.selectedText.contains("suiker"))
        assertFalse(segmentation.selectedText.contains("sugar"))
    }

    @Test fun markedEnglishBlockIsSelected() {
        val segmentation = LabelLanguageSegmenter.segment("EN-GB: sugar, water")

        assertEquals(LabelLanguage.ENGLISH, segmentation.selectedLanguage)
        assertEquals("EN-GB", segmentation.detectedMarker)
        assertEquals("sugar, water", segmentation.selectedText)
    }

    @Test fun gbHeaderReturnsEnglishLanguage() {
        val segmentation = LabelLanguageSegmenter.segment("GB: sugar, water")

        assertEquals(LabelLanguage.ENGLISH, segmentation.selectedLanguage)
        assertEquals("GB", segmentation.detectedMarker)
        assertEquals("sugar, water", segmentation.selectedText)
    }

    @Test fun bracketedMarkersOpenTheirLanguageBlocks() {
        val french = LabelLanguageSegmenter.segment("[FR] sucre, eau")
        val german = LabelLanguageSegmenter.segment("[DE] Wasser, Zucker")

        assertEquals(LabelLanguage.FRENCH, french.selectedLanguage)
        assertEquals("sucre, eau", french.selectedText)
        assertEquals(LabelLanguage.GERMAN, german.selectedLanguage)
        assertEquals("Wasser, Zucker", german.selectedText)
    }

    @Test fun ordinaryFrenchListIsKeptStrictlyWhenThereIsNoMarker() {
        val text = "sucre, eau, huile de tournesol"
        val segmentation = LabelLanguageSegmenter.segment(text)

        assertEquals(LabelLanguage.UNKNOWN, segmentation.selectedLanguage)
        assertEquals(text, segmentation.selectedText)
        assertTrue(segmentation.usedFallback)
    }

    @Test fun countryCodesAloneDoNotOpenBlocks() {
        val text = "Fabriqué en BE, distribué au LU et au GB. sucre, eau"
        val segmentation = LabelLanguageSegmenter.segment(text)

        assertTrue(segmentation.usedFallback)
        assertEquals(text, segmentation.selectedText)
    }

    @Test fun frenchRegionalMarkersOpenFrenchBlocks() {
        listOf(
            "F: sucre, eau" to LabelLanguage.FRENCH,
            "FR: sucre, eau" to LabelLanguage.FRENCH,
            "FR-BE: sucre, eau" to LabelLanguage.FRENCH,
            "BE-FR: sucre, eau" to LabelLanguage.FRENCH,
            "FR / BE / LU\nsucre, eau" to LabelLanguage.FRENCH
        ).forEach { (text, expectedLanguage) ->
                val segmentation = LabelLanguageSegmenter.segment(text)
                assertEquals(text, expectedLanguage, segmentation.selectedLanguage)
                assertTrue(text, segmentation.selectedText.contains("sucre, eau"))
            }
    }

    @Test fun dutchMarkersReturnDutchLanguage() {
        listOf(
            "NL: suiker, water" to LabelLanguage.DUTCH,
            "NL-BE: suiker, water" to LabelLanguage.DUTCH,
            "BE-NL: suiker, water" to LabelLanguage.DUTCH
        ).forEach { (text, expectedLanguage) ->
            val segmentation = LabelLanguageSegmenter.segment(text)

            assertEquals(text, expectedLanguage, segmentation.selectedLanguage)
            assertTrue(text, segmentation.selectedText.contains("suiker, water"))
        }
    }

    @Test fun ingredientTitlesAreExplicitMarkers() {
        val french = LabelLanguageSegmenter.segment("Ingrédients : sucre, eau\nNL: suiker, water")
        val dutch = LabelLanguageSegmenter.segment("Ingrediënten : suiker, water")
        val english = LabelLanguageSegmenter.segment("Ingredients : sugar, water")
        val frenchEnglishTitle = LabelLanguageSegmenter.segment("Ingredients FR: sucre, eau")

        assertEquals(LabelLanguage.FRENCH, french.selectedLanguage)
        assertTrue(french.selectedText.startsWith("Ingrédients :"))
        assertEquals(LabelLanguage.DUTCH, dutch.selectedLanguage)
        assertEquals(LabelLanguage.ENGLISH, english.selectedLanguage)
        assertEquals(LabelLanguage.FRENCH, frenchEnglishTitle.selectedLanguage)
    }

    @Test fun selectedFrenchCompositionCannotMatchDiscardedGelatinTranslation() {
        val database = listOf(
            ingredient("sugar", "sucre"),
            ingredient("water", "eau"),
            Ingredient(
                id = "gelatin",
                name = "gélatine",
                aliases = listOf("gélatine", "gelatin"),
                eNumber = null,
                status = VeganStatus.NON_VEGAN,
                reason = "Test"
            )
        )
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "FR: sucre, eau.\nEN: sugar, water, gelatin.", database
        )

        assertEquals(LabelLanguage.FRENCH, diagnostics.languageSegmentation.selectedLanguage)
        assertEquals(AnalysisVerdict.VEGAN, diagnostics.result.verdict)
        assertFalse(diagnostics.result.matched.any { it.id == "gelatin" })
        assertFalse(diagnostics.result.unknown.any { it.contains("gelatin", ignoreCase = true) })
        assertFalse(diagnostics.tokens.any { it.text.contains("gelatin", ignoreCase = true) })
    }

    @Test fun traceInDiscardedBlockIsNotReported() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "FR: sucre, eau.\nNL: suiker, water. Peut contenir du lait.",
            listOf(ingredient("sugar", "sucre"), ingredient("water", "eau"))
        )

        assertTrue(diagnostics.crossContactWarnings.isEmpty())
        assertEquals(AnalysisVerdict.VEGAN, diagnostics.result.verdict)
    }

    private fun ingredient(id: String, alias: String) = Ingredient(
        id = id,
        name = alias,
        aliases = listOf(alias),
        eNumber = null,
        status = VeganStatus.VEGAN,
        reason = "Test"
    )
}
