package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LabelSectionExtractorTest {
    @Test fun implicitCompositionWithBracketedTraceKeepsSourceTextAndOffsets() {
        assertImplicitCompositionSource("eau, sucre [Peut contenir: noix], sel, farine, lait.", true)
    }

    @Test fun implicitCompositionWithParenthesizedTraceKeepsSourceTextAndOffsets() {
        assertImplicitCompositionSource("eau, sucre (Peut contenir: noix), sel, farine, lait.", true)
    }

    @Test fun implicitCompositionWithoutTraceKeepsSourceTextAndOffsets() {
        assertImplicitCompositionSource("eau, sucre, sel, farine, lait.", false)
    }

    private fun assertImplicitCompositionSource(composition: String, hasTrace: Boolean) {
        val knowledge = database + listOf(ingredient("salt", "sel"), ingredient("flour", "farine"))
        for (prefix in listOf("", "\n  ")) {
            val text = prefix + composition
            val sections = LabelSectionExtractor.extract(LabelLanguage.FRENCH, text)
            assertTrue(text, sections.implicitIngredientList != null)
            val section = requireNotNull(sections.ingredientSection)
            assertEquals(text, sections.rawText)
            assertEquals(prefix.length, section.start)
            assertEquals(text.length, section.end)
            assertEquals(text.substring(section.start, section.end), section.rawText)
            assertEquals(composition, section.rawText)
            assertFalse(sections.ingredientsText.orEmpty().contains("Peut contenir"))
            assertTrue(sections.ingredientsText.orEmpty().contains("sel, farine, lait"))
            assertEquals(if (hasTrace) listOf("Peut contenir: noix") else emptyList<String>(),
                sections.traceSections.map { it.rawText })
            for (trace in sections.traceSections) {
                assertEquals(trace.rawText, text.substring(trace.start, trace.end))
            }
            for (mode in InputMode.values()) {
                val diagnostics = CoreTestAnalyzer.analyzeWithDiagnostics(text, knowledge, mode)
                assertFalse(diagnostics.ambiguousOcrText)
                assertEquals(setOf("water", "sugar", "salt", "flour", "milk"),
                    diagnostics.result.matched.map { it.id }.toSet())
                assertTrue(diagnostics.visibleUnknownIngredients.isEmpty())
                assertEquals(VeganAssessment.NOT_VEGAN, diagnostics.result.veganAssessment)
                assertTrue(diagnostics.decision.tracesExcludedFromVerdict)
                assertEquals(sections.traceSections.map { it.rawText }, diagnostics.crossContactWarnings)
            }
        }
    }

    @Test fun multilingualInterleavedSectionsAndBoundaries() {
        val examples = listOf(
            Triple(LabelLanguage.FRENCH, "Ingrédients: eau. Contient: soja. Peut contenir: noix Contient: lait.", "lait"),
            Triple(LabelLanguage.DUTCH, "Ingrediënten: water. Bevat: soja. Kan bevatten: noten Bevat: melk.", "melk"),
            Triple(LabelLanguage.ENGLISH, "Ingredients: water. Contains: soy. May contain: nuts Contains: milk.", "milk"),
            Triple(LabelLanguage.GERMAN, "Zutaten: Wasser. Enthält: Soja. Kann enthalten: Nüsse Enthält: Milch.", "Milch")
        )
        for ((language, text, last) in examples) {
            val sections = LabelSectionExtractor.extract(language, text)
            assertTrue(text, sections.declaredContainsText.orEmpty().contains(last))
            assertFalse(text, sections.tracesText.orEmpty().contains(last))
            assertTrue(text, sections.traceSections.isNotEmpty())
        }
    }

    @Test fun declarationsStayWithinCompositionAndTracesStaySeparate() {
        val cases = listOf(
            "Ingrédients: eau. Contient: lait." to "lait",
            "Ingrédients: eau. Contient: soja. Contient: lait." to "soja, lait",
            "Ingrédients: eau. Contient: lait. Peut contenir: noix." to "lait",
            "Ingrédients: eau. Peut contenir: noix Contient: mystere." to "mystere"
        )
        for ((text, expected) in cases) {
            assertEquals(text, expected, LabelSectionExtractor.extract(LabelLanguage.FRENCH, text).declaredContainsText)
        }
        val traces = LabelSectionExtractor.extract(LabelLanguage.FRENCH, "Ingrédients: eau. Peut contenir: lait.")
        assertNull(traces.declaredContainsText)
        val separate = LabelSectionExtractor.extract(LabelLanguage.FRENCH, "Ingrédients: eau. Contient: soja. Valeurs nutritionnelles: contient: lait.")
        assertEquals("soja", separate.declaredContainsText)
        val translation = LabelSectionExtractor.extract(LabelLanguage.FRENCH, "Ingrédients: eau. Contient: soja. Ingredients: water. Contains: milk.")
        assertEquals("soja", translation.declaredContainsText)
    }
    private val database = listOf(
        ingredient("water", "eau"), ingredient("sugar", "sucre"),
        ingredient("soy", "soja"), ingredient("rapeseed", "huile de colza"),
        ingredient("milk", "lait", VeganStatus.NON_VEGAN),
        ingredient("e471", "E471", VeganStatus.UNCERTAIN)
    )

    @Test fun syntheticServiceKeepsEveryPresenceOutOfTraces() {
        for (mode in InputMode.values()) {
            val service = IngredientAnalysisService(IngredientKnowledge(database))
            val result = service.analyzeWithDiagnostics(
                "Ingredients: eau. Contient: soja. Peut contenir: noix. Contient: lait.", mode
            )
            assertEquals(setOf("water", "soy", "milk"), result.result.matched.map { it.id }.toSet())
            assertEquals(AnalysisVerdict.NON_VEGETARIAN, result.result.verdict)
            assertEquals(VeganAssessment.NOT_VEGAN, result.result.veganAssessment)
            assertTrue(result.crossContactWarnings.any { it.contains("noix") })
            assertFalse(result.crossContactWarnings.any { it.contains("lait") })
        }
    }

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

    @Test fun exactlyAdjacentTraceSectionsArePreservedAndExcludedFromVerdict() {
        val text = "Ingrédients : eau. Peut contenir : lait.May contain traces of egg."
        val baseline = CoreTestAnalyzer.analyzeWithDiagnostics("Ingrédients : eau.", database)
        val diagnostics = CoreTestAnalyzer.analyzeWithDiagnostics(text, database)
        val traces = diagnostics.labelSections.traceSections

        assertEquals(2, traces.size)
        assertEquals(traces[0].end, traces[1].start)
        assertTrue(traces[0].rawText.contains("lait"))
        assertTrue(traces[1].rawText.contains("egg"))
        assertEquals(baseline.result.verdict, diagnostics.result.verdict)
        assertEquals(AnalysisVerdict.VEGAN, diagnostics.result.verdict)
        assertTrue(diagnostics.decision.tracesExcludedFromVerdict)
        assertFalse(diagnostics.result.matched.any { it.id == "milk" })
        assertFalse(diagnostics.tokens.any { it.text.contains("lait", true) || it.text.contains("egg", true) })
    }

    @Test fun trulyOverlappingTraceMarkersStillProduceOneTraceSection() {
        val diagnostics = CoreTestAnalyzer.analyzeWithDiagnostics(
            "Ingrédients : eau. Peut contenir des traces éventuelles de : lait, œufs.",
            database
        )

        assertEquals(1, diagnostics.labelSections.traceSections.size)
        assertTrue(diagnostics.crossContactWarnings.single().contains("lait, œufs"))
        assertEquals(AnalysisVerdict.VEGAN, diagnostics.result.verdict)
        assertTrue(diagnostics.decision.tracesExcludedFromVerdict)
        assertFalse(diagnostics.result.matched.any { it.id == "milk" })
    }

    private fun ingredient(id: String, alias: String, status: VeganStatus = VeganStatus.VEGAN) = Ingredient(
        id, alias, listOf(alias), null, status, "Test"
    )
}
