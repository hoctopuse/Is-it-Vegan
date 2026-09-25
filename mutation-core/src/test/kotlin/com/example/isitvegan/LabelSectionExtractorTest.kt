package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LabelSectionExtractorTest {
    private val database = listOf(
        ingredient("water", "eau"), ingredient("sugar", "sucre"),
        ingredient("soy", "soja"), ingredient("rapeseed", "huile de colza"),
        ingredient("milk", "lait", VeganStatus.NON_VEGAN),
        ingredient("e471", "E471", VeganStatus.UNCERTAIN)
    )

    @Test fun multilingualLabelCreatesThreeBlocksAndUsesFrenchIngredients() {
        val text = """FR: Ingrédients : eau, sucre, lait.
            NL: Ingrediënten: water, suiker, melk.
            EN: Ingredients: water, sugar, milk.""".trimIndent()
        val segmentation = LabelLanguageSegmenter.segment(text)
        val diagnostics = CoreTestAnalyzer.analyzeWithDiagnostics(text, database)

        assertEquals(listOf(LabelLanguage.FRENCH, LabelLanguage.DUTCH, LabelLanguage.ENGLISH), segmentation.blocks.map { it.language })
        assertEquals(LabelLanguage.FRENCH, diagnostics.labelSections.language)
        assertTrue(diagnostics.labelSections.hasIngredientHeading)
        assertFalse(diagnostics.preprocessedInput.contains("water, sugar"))
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, diagnostics.result.verdict)
    }

    @Test fun marketTagsStaySeparateFromFrenchLanguage() {
        val text = "FR/BE/LUX Ingrédients : eau, tofu (soja, nigari)."
        val block = LabelLanguageSegmenter.segment(text).blocks.single()
        val result = CoreTestAnalyzer.analyze(text, database)

        assertEquals(LabelLanguage.FRENCH, block.language)
        assertEquals(setOf("BE", "LUX"), block.marketTags)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, result.verdict)
        assertFalse(result.unknown.any { it.contains("BE") || it.contains("LUX") })
    }

    @Test fun germanBlockSeparatesIngredientsAndTracesWithoutNeedingGermanAliases() {
        val text = "DE: Zutaten: Wasser, Zucker, Milch.\nKann Spuren von Ei enthalten."
        val block = LabelLanguageSegmenter.segment(text).blocks.single()
        val sections = LabelSectionExtractor.extract(block)

        assertEquals(LabelLanguage.GERMAN, block.language)
        assertEquals("Wasser, Zucker, Milch.", sections.ingredientsText!!.trim())
        assertTrue(sections.tracesText!!.contains("Kann Spuren von Ei enthalten"))
    }

    @Test fun tracesAndNutritionAreRemovedFromIngredientsAtTopLevel() {
        val text = """Ingrédients : protéines de soja réhydratées 37 %, eau, huile de colza.
            Peut contenir : lait, œuf.
            Valeurs nutritionnelles : énergie 850 kJ.""".trimIndent()
        val sections = LabelSectionExtractor.extract(LabelLanguage.FRENCH, text)
        val diagnostics = CoreTestAnalyzer.analyzeWithDiagnostics(text, database)

        assertTrue(sections.ingredientsText!!.contains("protéines de soja réhydratées 37 %"))
        assertFalse(sections.ingredientsText.contains("Peut contenir"))
        assertTrue(sections.tracesText!!.contains("lait, œuf"))
        assertTrue(sections.ignoredSections.single().contains("Valeurs nutritionnelles"))
        assertTrue(diagnostics.crossContactWarnings.single().contains("lait, œuf"))
        assertFalse(diagnostics.tokens.any { it.text.contains("850") || it.text.contains("œuf") })
    }

    @Test fun topLevelContainsIsCompositionButNestedContainsDoesNotSplitIt() {
        val contains = CoreTestAnalyzer.analyze("Contient : lait.", database)
        val nested = LabelSectionExtractor.extract(
            LabelLanguage.FRENCH,
            "Ingrédients : morceaux végétaliens (eau, soja), herbes préparées (épices (contient : moutarde), huile), sel."
        )

        assertEquals(AnalysisAvailability.NO_INGREDIENT_LIST, contains.availability)
        assertEquals(VeganAssessment.NOT_VEGAN, contains.veganAssessment)
        assertNull(contains.verdict)
        assertNull(nested.declaredContainsText)
        assertTrue(nested.ingredientsText!!.contains("contient : moutarde"))
    }

    @Test fun additivesPercentagesAndNestedStructureReachTheExistingPipeline() {
        val additive = CoreTestAnalyzer.analyze("Ingrédients : eau, huile de colza, émulsifiant : E471, sel.", database)
        val nested = LabelSectionExtractor.extract(LabelLanguage.FRENCH,
            "Ingrédients : cœur de tofu fumé 62,6 % (tofu 95 % [soja, eau, nigari]), enrobage 37,4 % (chapelure, flocons de maïs).")

        assertTrue(additive.matched.any { it.id == "e471" })
        assertEquals(AnalysisVerdict.UNCERTAIN, additive.verdict)
        assertTrue(nested.ingredientsText!!.contains("62,6 %"))
        assertTrue(nested.ingredientsText.contains("[soja, eau, nigari]"))
    }

    @Test fun titlelessIngredientListUsesUnknownFallbackAndOrdinaryWordsAreNotMarkers() {
        val text = "eau, sucre, 2,9 % noix de cajou, 1,7 % cacao, protéine de pois"
        val segmentation = LabelLanguageSegmenter.segment(text)
        val falseMarkers = LabelLanguageSegmenter.segment("prendre en de bonnes conditions, fruit belge lu hier")

        assertEquals(LabelLanguage.UNKNOWN, segmentation.selectedLanguage)
        assertEquals(text, segmentation.selectedText)
        assertTrue(segmentation.usedFallback)
        assertTrue(falseMarkers.usedFallback)
    }

    private fun ingredient(id: String, alias: String, status: VeganStatus = VeganStatus.VEGAN) = Ingredient(
        id, alias, listOf(alias), null, status, "Test"
    )
}
