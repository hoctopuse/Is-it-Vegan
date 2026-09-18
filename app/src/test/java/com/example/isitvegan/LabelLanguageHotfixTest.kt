package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LabelLanguageHotfixTest {
    private val database = listOf(
        vegan("water", "eau"), vegan("soy", "soja", "protéines de soja"),
        vegan("starch", "amidon", "amidon de blé"), vegan("gluten", "gluten", "gluten de blé"),
        vegan("vinegar", "vinaigre"), vegan("rapeseed_oil", "huile de colza"),
        vegan("chickpeas", "pois chiches"), vegan("onion", "oignon", "oignons"),
        vegan("mustard", "moutarde"), vegan("glucose_syrup", "sirop de glucose"),
        vegan("spices", "épice", "épices"), vegan("salt", "sel"),
        Ingredient("e471", "E471", listOf("E471"), "E471", VeganStatus.UNCERTAIN, "Test")
    )

    @Test fun frenchBelgianLuxembourgBlockWithLongDashStaysVegan() {
        val text = "FR/BE/LUX — Ingrédients : morceaux végétaliens (36 %) [eau, protéines de SOJA, amidon de BLÉ, gluten de BLÉ, vinaigre] ; huile de colza ; pois chiches ; eau ; oignons ; préparation d’herbes (1,5 %) (épices (contient : MOUTARDE)) ; sirop de glucose ; sel."
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, database)

        assertEquals(LabelLanguage.FRENCH, diagnostics.labelSections.language)
        assertEquals(setOf("BE", "LUX"), diagnostics.languageSegmentation.blocks.single().marketTags)
        assertFalse(diagnostics.languageSegmentation.usedFallback)
        assertTrue(diagnostics.labelSections.ingredientsText!!.contains("contient : MOUTARDE"))
        listOf("morceaux végétaliens", "amidon", "gluten", "oignons", "préparation d’herbes", "MOUTARDE", "sirop de glucose")
            .forEach { forbidden -> assertFalse("$forbidden -> ${diagnostics.result.unknown}", diagnostics.result.unknown.any { it.contains(forbidden, true) }) }
        assertEquals(AnalysisVerdict.VEGAN, diagnostics.result.verdict)
    }

    @Test fun longDashMultilingualLabelKeepsOnlyFrenchTrace() {
        val text = """FR — Ingrédients : eau, protéines de soja, huile de colza, sel, épices.
            Peut contenir : lait, œuf.

            NL — Ingrediënten: water, soja-eiwitten, koolzaadolie, zout, specerijen.
            Kan melk en ei bevatten.

            EN — Ingredients: water, soy protein, rapeseed oil, salt, spices.
            May contain milk and egg.""".trimIndent()
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, database)

        assertEquals(listOf(LabelLanguage.FRENCH, LabelLanguage.DUTCH, LabelLanguage.ENGLISH), diagnostics.languageSegmentation.blocks.map { it.language })
        assertEquals(LabelLanguage.FRENCH, diagnostics.labelSections.language)
        assertEquals("FR", diagnostics.languageSegmentation.detectedMarker)
        assertEquals(listOf(LabelLanguage.DUTCH, LabelLanguage.ENGLISH), diagnostics.languageSegmentation.ignoredLanguages)
        assertFalse(diagnostics.languageSegmentation.usedFallback)
        assertEquals(listOf("Peut contenir : lait, œuf."), diagnostics.crossContactWarnings)
        assertFalse(diagnostics.preprocessedInput.contains("NL —"))
        assertFalse(diagnostics.preprocessedInput.contains("EN —"))
        assertTrue(diagnostics.result.unknown.isEmpty())
        assertEquals(AnalysisVerdict.VEGAN, diagnostics.result.verdict)
    }

    @Test fun dashMarkersAndOrdinarySmallWordsRemainDistinct() {
        listOf("FR — eau", "NL – water", "EN - water", "DE\nWasser").forEach { text ->
            assertFalse(text, LabelLanguageSegmenter.segment(text).usedFallback)
        }
        assertTrue(LabelLanguageSegmenter.segment("prendre en de bonnes conditions, fruit belge").usedFallback)
    }

    @Test fun resolvedCompositeContainerIsNotUnknownButE471StaysUncertain() {
        val container = VeganAnalyzer.analyze("morceaux végétaliens (eau, soja)", database)
        val additive = VeganAnalyzer.analyze("émulsifiant : E471", database)

        assertTrue(container.unknown.isEmpty())
        assertEquals(AnalysisVerdict.VEGAN, container.verdict)
        assertEquals(AnalysisVerdict.UNCERTAIN, additive.verdict)
        assertTrue(additive.matched.any { it.id == "e471" })
    }

    private fun vegan(id: String, vararg aliases: String) = Ingredient(
        id, aliases.first(), aliases.toList(), null, VeganStatus.VEGAN, "Test"
    )
}
